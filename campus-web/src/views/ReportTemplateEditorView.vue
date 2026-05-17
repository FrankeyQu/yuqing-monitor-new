<template>
  <section class="business-page">
    <section class="panel">
      <div class="editor-header">
        <div>
          <h2>{{ isEdit ? '编辑报告模板' : '新增报告模板' }}</h2>
          <p>{{ form.templateName || '未命名模板' }}</p>
        </div>
        <div class="editor-actions">
          <el-button @click="router.push('/report-templates')">返回</el-button>
          <el-button type="primary" :loading="saving" @click="submitTemplate">
            <Save :size="16" />
            保存
          </el-button>
        </div>
      </div>

      <el-form label-position="top" class="template-form">
        <div class="form-grid">
          <el-form-item label="模板名称" required>
            <el-input v-model.trim="form.templateName" />
          </el-form-item>
          <el-form-item label="报告类型" required>
            <el-select v-model="form.reportType">
              <el-option label="日报" value="daily" />
              <el-option label="周报" value="weekly" />
              <el-option label="月报" value="monthly" />
              <el-option label="专报" value="special" />
              <el-option label="事件报告" value="event" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="状态">
            <el-switch v-model="templateEnabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
          <el-form-item label="适用场景">
            <el-input v-model.trim="form.remark" />
          </el-form-item>
        </div>
        <div class="editor-layout">
          <div class="template-editor">
            <el-form-item label="模板正文">
              <el-input v-model="form.templateContent" type="textarea" :rows="22" />
            </el-form-item>
            <div class="variable-reference">
              <span class="var-group-title">变量</span>
              <el-tag v-for="variable in variables" :key="variable" size="small" @click="insertVar(variable)">
                {{ variable }}
              </el-tag>
            </div>
          </div>
          <div class="template-preview">
            <div class="preview-title">预览</div>
            <div class="markdown-body" v-html="renderMarkdown(form.templateContent || '')"></div>
          </div>
        </div>
      </el-form>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Save } from 'lucide-vue-next';
import { listReportTemplates, saveReportTemplate } from '../services/analysisReport';
import type { CampusReportTemplate } from '../types/api';

const route = useRoute();
const router = useRouter();
const saving = ref(false);
const isEdit = computed(() => Boolean(route.params.templateId));

const form = reactive<CampusReportTemplate>({
  templateName: '',
  reportType: 'daily',
  templateContent: defaultTemplateContent(),
  status: 1,
  remark: ''
});

const templateEnabled = computed({
  get: () => form.status !== 0,
  set: (value: boolean) => {
    form.status = value ? 1 : 0;
  }
});

const variables = [
  '${reportTitle}',
  '${reportType}',
  '${reportSummary}',
  '${periodStart}',
  '${periodEnd}',
  '${totalCount}',
  '${negativeCount}',
  '${neutralCount}',
  '${positiveCount}',
  '${trendTable}',
  '${mediaTable}',
  '${sentimentTable}',
  '${keywordTable}',
  '${hotArticles}',
  '${platformRanking}',
  '${governanceTable}',
  '${eventTitle}',
  '${eventSummary}',
  '${riskLevel}',
  '${eventStatus}'
];

onMounted(loadTemplate);

async function loadTemplate() {
  const templateId = route.params.templateId ? String(route.params.templateId) : '';
  if (!templateId) {
    return;
  }
  try {
    const page = await listReportTemplates({ pageNum: 1, pageSize: 500 });
    const found = (page.list || []).find((item) => String(item.templateId) === templateId);
    if (!found) {
      ElMessage.error('模板不存在');
      router.push('/report-templates');
      return;
    }
    Object.assign(form, found);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '模板加载失败');
  }
}

function insertVar(variable: string) {
  form.templateContent = `${form.templateContent || ''} ${variable}`;
}

async function submitTemplate() {
  if (!form.templateName || !form.reportType) {
    ElMessage.warning('模板名称和报告类型不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveReportTemplate({ ...form });
    ElMessage.success('模板已保存');
    router.push('/report-templates');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function renderMarkdown(md: string): string {
  if (!md) {
    return '';
  }
  let html = md
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');

  html = html
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^(\s*)- (.+)$/gm, '<li>$2</li>');

  return html
    .split('\n')
    .map((line) => line.startsWith('<') ? line : (line.trim() ? `<p>${line}</p>` : ''))
    .join('');
}

function defaultTemplateContent() {
  return [
    '# ${reportTitle}',
    '',
    '## 一、舆情概况',
    '${reportSummary}',
    '',
    '## 二、统计周期',
    '${periodStart} 至 ${periodEnd}',
    '',
    '## 三、关键数据',
    '| 指标 | 数值 |',
    '|------|------|',
    '| 监测文章总数 | ${totalCount} |',
    '| 负面文章数 | ${negativeCount} |',
    '',
    '## 四、传播与风险',
    '${trendTable}',
    '',
    '${sentimentTable}',
    '',
    '## 五、处置建议',
    '${governanceTable}'
  ].join('\n');
}
</script>

<style scoped>
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}
.editor-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.editor-header p {
  margin: 5px 0 0;
  font-size: 13px;
  color: #909399;
}
.editor-actions {
  display: flex;
  gap: 8px;
}
.template-form {
  max-width: 1180px;
}
.editor-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 18px;
  align-items: start;
}
.template-editor,
.template-preview {
  min-width: 0;
}
.variable-reference {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}
.var-group-title {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}
.variable-reference .el-tag {
  cursor: pointer;
}
.preview-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}
.template-preview {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 14px;
  min-height: 560px;
  background: #fafafa;
}
.markdown-body {
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
  word-break: break-word;
}
.markdown-body h1 { font-size: 18px; margin: 8px 0; }
.markdown-body h2 { font-size: 16px; margin: 8px 0; }
.markdown-body h3 { font-size: 14px; margin: 8px 0; }
.markdown-body p { margin: 4px 0; }
.markdown-body li { margin-left: 18px; }

@media (max-width: 980px) {
  .editor-layout {
    grid-template-columns: 1fr;
  }
}
</style>
