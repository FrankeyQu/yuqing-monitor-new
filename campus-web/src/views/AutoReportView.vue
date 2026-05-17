<template>
  <section class="business-page">
    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="query.keyword" clearable placeholder="任务名称/说明" @keyup.enter="loadJobs">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-select v-model="query.reportType" clearable placeholder="报告类型">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
            <el-option label="专报" value="special" />
            <el-option label="事件报告" value="event" />
          </el-select>
          <el-select v-model="query.jobStatus" clearable placeholder="状态">
            <el-option label="启用" value="active" />
            <el-option label="暂停" value="paused" />
            <el-option label="禁用" value="disabled" />
          </el-select>
          <el-button @click="loadJobs">
            <Search :size="16" />
            查询
          </el-button>
        </div>
        <el-button type="primary" @click="openCreate">
          <Plus :size="16" />
          新建任务
        </el-button>
      </div>

      <el-table :data="rows" v-loading="loading" size="small" height="600">
        <el-table-column prop="jobName" label="任务名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reportType" label="报告类型" width="92">
          <template #default="{ row }">{{ reportTypeLabel(row.reportType) }}</template>
        </el-table-column>
        <el-table-column prop="generationMode" label="生成方式" width="92">
          <template #default="{ row }">
            <el-tag :type="row.generationMode === 'ai' ? '' : 'info'" effect="plain" size="small">
              {{ row.generationMode === 'ai' ? 'AI智能' : '传统' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="periodRule" label="周期" width="82">
          <template #default="{ row }">{{ periodRuleLabel(row.periodRule) }}</template>
        </el-table-column>
        <el-table-column prop="eventId" label="关联事件" width="150" show-overflow-tooltip />
        <el-table-column prop="outputFormat" label="格式" width="92">
          <template #default="{ row }">{{ outputFormatLabel(row.outputFormat) }}</template>
        </el-table-column>
        <el-table-column prop="jobStatus" label="状态" width="92">
          <template #default="{ row }">
            <el-tag :type="jobStatusTagType(row.jobStatus)" effect="plain">
              {{ jobStatusLabel(row.jobStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastRunTime" label="最近运行" width="168" show-overflow-tooltip />
        <el-table-column prop="nextRunTime" label="下次运行" width="168" show-overflow-tooltip />
        <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              <Pencil :size="15" />
              编辑
            </el-button>
            <el-button link type="success" @click="submitRun(row)">
              <Play :size="15" />
              运行
            </el-button>
            <el-button link type="warning" @click="toggleStatus(row)">
              <PauseCircle :size="15" />
              {{ row.jobStatus === 'active' ? '暂停' : '启用' }}
            </el-button>
            <el-button link type="info" @click="openLogs(row)">
              <ListChecks :size="15" />
              日志
            </el-button>
            <el-button link type="danger" @click="submitDelete(row)">
              <Trash2 :size="15" />
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="loadJobs"
          @current-change="loadJobs"
        />
      </div>
    </section>

    <el-dialog v-model="formVisible" :title="form.reportJobId ? '编辑自动报告任务' : '新建自动报告任务'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="任务名称" required>
            <el-input v-model.trim="form.jobName" />
          </el-form-item>
          <el-form-item label="报告类型" required>
            <el-select v-model="form.reportType" @change="handleReportTypeChange">
              <el-option label="日报" value="daily" />
              <el-option label="周报" value="weekly" />
              <el-option label="月报" value="monthly" />
              <el-option label="专报" value="special" />
              <el-option label="事件报告" value="event" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="生成方式">
            <el-radio-group v-model="form.generationMode">
              <el-radio-button value="template">传统模板</el-radio-button>
              <el-radio-button value="ai">AI智能</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="统计周期">
            <el-select v-model="form.periodRule">
              <el-option label="日报周期" value="daily" />
              <el-option label="周报周期" value="weekly" />
              <el-option label="月报周期" value="monthly" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="报告模板">
            <el-select
              v-model="form.templateId"
              filterable
              remote
              clearable
              placeholder="搜索并选择模板"
              :remote-method="loadTemplateOptions"
              :loading="templateOptionLoading"
              style="width: 100%;"
            >
              <el-option
                v-for="tpl in templateOptions"
                :key="tpl.templateId"
                :label="templateOptionLabel(tpl)"
                :value="tpl.templateId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="计划表达式">
            <el-input v-model.trim="form.scheduleCron" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="关联事件">
            <el-select
              v-model="form.eventId"
              filterable
              remote
              clearable
              placeholder="搜索并选择事件"
              :remote-method="loadEventOptions"
              :loading="eventOptionLoading"
              style="width: 100%;"
            >
              <el-option
                v-for="event in eventOptions"
                :key="event.eventId"
                :label="eventOptionLabel(event)"
                :value="event.eventId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="分析档位">
            <el-select v-model="form.analysisProfile">
              <el-option label="概览简报" value="brief" />
              <el-option label="风险研判" value="risk" />
              <el-option label="处置建议" value="disposal" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item v-if="form.generationMode === 'ai'" label="AI 生成要求">
          <el-input v-model.trim="form.aiUserPrompt" type="textarea" :rows="4" />
          <div class="form-tip">
            可填写本任务每次 AI 生成时的补充要求，例如重点关注风险研判、处置建议、某类群体反馈或领导汇报口径；系统会先按规则统计数据，再让 AI 按该要求润色和分析。
          </div>
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="统计范围">
            <el-select v-model="form.scopeType">
              <el-option label="全量舆情" value="all" />
              <el-option label="关键词范围" value="keyword" />
              <el-option label="事件范围" value="event" />
              <el-option label="部门范围" value="department" />
              <el-option label="监测任务范围" value="monitor_task" />
              <el-option label="自定义范围" value="custom" />
            </el-select>
          </el-form-item>
          <el-form-item label="包含关键词">
            <el-input v-model.trim="form.scopeKeywords" />
          </el-form-item>
          <el-form-item label="排除关键词">
            <el-input v-model.trim="form.excludeKeywords" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="平台范围">
            <el-input v-model.trim="form.platformScope" />
          </el-form-item>
          <el-form-item label="风险等级">
            <el-input v-model.trim="form.riskLevels" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="部门ID范围">
            <el-input v-model.trim="form.departmentScope" />
          </el-form-item>
          <el-form-item label="监测任务ID范围">
            <el-input v-model.trim="form.monitorTaskIds" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="输出格式">
            <el-select v-model="form.outputFormat">
              <el-option label="Markdown" value="markdown" />
              <el-option label="HTML" value="html" />
              <el-option label="纯文本" value="text" />
              <el-option label="Word" value="docx" />
              <el-option label="PPT" value="pptx" />
            </el-select>
          </el-form-item>
          <el-form-item label="任务状态">
            <el-select v-model="form.jobStatus">
              <el-option label="启用" value="active" />
              <el-option label="暂停" value="paused" />
              <el-option label="禁用" value="disabled" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="默认审核人ID">
            <el-input v-model.trim="form.reviewerUserId" />
          </el-form-item>
        </div>
        <el-form-item label="任务说明">
          <el-input v-model.trim="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logVisible" title="自动报告生成日志" width="820px">
      <el-table :data="logs" v-loading="logLoading" size="small" max-height="440">
        <el-table-column prop="runStatus" label="状态" width="92">
          <template #default="{ row }">
            <el-tag :type="row.runStatus === 'success' ? 'success' : row.runStatus === 'failed' ? 'danger' : 'warning'" effect="plain">
              {{ runStatusLabel(row.runStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportId" label="报告ID" width="150" show-overflow-tooltip />
        <el-table-column prop="generationMode" label="方式" width="86">
          <template #default="{ row }">{{ row.generationMode === 'ai' ? 'AI' : '传统' }}</template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="168" show-overflow-tooltip />
        <el-table-column prop="endTime" label="结束时间" width="168" show-overflow-tooltip />
        <el-table-column prop="errorMessage" label="错误信息" min-width="210" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ListChecks, PauseCircle, Pencil, Play, Plus, Search, Trash2 } from 'lucide-vue-next';
import {
  deleteReportJob,
  listReportGenerationLogs,
  listReportJobs,
  listReportTemplates,
  runReportJob,
  saveReportJob,
  updateReportJobStatus
} from '../services/analysisReport';
import { listEvents } from '../services/eventCenter';
import type { CampusEvent, CampusReportGenerationLog, CampusReportJob, CampusReportTemplate } from '../types/api';

const loading = ref(false);
const saving = ref(false);
const logLoading = ref(false);
const formVisible = ref(false);
const logVisible = ref(false);
const rows = ref<CampusReportJob[]>([]);
const logs = ref<CampusReportGenerationLog[]>([]);
const templateOptions = ref<CampusReportTemplate[]>([]);
const eventOptions = ref<CampusEvent[]>([]);
const templateOptionLoading = ref(false);
const eventOptionLoading = ref(false);
const total = ref(0);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  reportType: '',
  jobStatus: ''
});
const form = reactive<CampusReportJob>({
  jobName: '',
  reportType: 'daily',
  generationMode: 'template',
  scopeType: 'all',
  scopeKeywords: '',
  excludeKeywords: '',
  platformScope: '',
  riskLevels: '',
  departmentScope: '',
  monitorTaskIds: '',
  analysisProfile: 'brief',
  templateId: undefined,
  eventId: undefined,
  aiUserPrompt: '',
  periodRule: 'daily',
  scheduleCron: '',
  outputFormat: 'markdown',
  jobStatus: 'paused',
  reviewerUserId: undefined,
  description: ''
});

onMounted(() => {
  loadJobs();
  loadTemplateOptions();
  loadEventOptions();
});

async function loadTemplateOptions(keyword = '') {
  templateOptionLoading.value = true;
  try {
    const page = await listReportTemplates({
      pageNum: 1,
      pageSize: 50,
      keyword,
      reportType: form.reportType,
      status: 1
    });
    templateOptions.value = page.list || [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '模板选项加载失败');
  } finally {
    templateOptionLoading.value = false;
  }
}

async function loadEventOptions(keyword = '') {
  eventOptionLoading.value = true;
  try {
    const page = await listEvents({
      pageNum: 1,
      pageSize: 50,
      keyword
    });
    eventOptions.value = page.list || [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '事件选项加载失败');
  } finally {
    eventOptionLoading.value = false;
  }
}

async function handleReportTypeChange() {
  form.templateId = undefined;
  await loadTemplateOptions();
}

async function loadJobs() {
  loading.value = true;
  try {
    const page = await listReportJobs(query);
    rows.value = page.list || [];
    total.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '自动报告任务加载失败');
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  Object.assign(form, {
    reportJobId: undefined,
    jobName: '',
    reportType: 'daily',
    generationMode: 'template',
    scopeType: 'all',
    scopeKeywords: '',
    excludeKeywords: '',
    platformScope: '',
    riskLevels: '',
    departmentScope: '',
    monitorTaskIds: '',
    analysisProfile: 'brief',
    templateId: undefined,
    eventId: undefined,
    aiUserPrompt: '',
    periodRule: 'daily',
    scheduleCron: '',
    outputFormat: 'markdown',
    jobStatus: 'paused',
    reviewerUserId: undefined,
    description: ''
  });
}

function openCreate() {
  resetForm();
  loadTemplateOptions();
  loadEventOptions();
  formVisible.value = true;
}

function openEdit(row: CampusReportJob) {
  Object.assign(form, row);
  loadTemplateOptions();
  loadEventOptions();
  formVisible.value = true;
}

async function submitForm() {
  if (!form.jobName || !form.reportType) {
    ElMessage.warning('任务名称和报告类型不能为空');
    return;
  }
  saving.value = true;
  try {
    const payload = { ...form };
    normalizeJobIds(payload);
    await saveReportJob(payload);
    ElMessage.success('自动报告任务已保存');
    formVisible.value = false;
    await loadJobs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function normalizeJobIds(payload: CampusReportJob) {
  if (payload.templateId === '') {
    payload.templateId = undefined;
  }
  if (payload.eventId === '') {
    payload.eventId = undefined;
  }
  if (payload.reviewerUserId === '') {
    payload.reviewerUserId = undefined;
  }
}

async function submitRun(row: CampusReportJob) {
  if (!row.reportJobId) {
    return;
  }
  try {
    const report = await runReportJob(row.reportJobId);
    ElMessage.success(`自动报告已生成：${report.reportTitle}`);
    await loadJobs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '运行失败');
  }
}

async function toggleStatus(row: CampusReportJob) {
  if (!row.reportJobId) {
    return;
  }
  const nextStatus = row.jobStatus === 'active' ? 'paused' : 'active';
  try {
    await updateReportJobStatus(row.reportJobId, nextStatus);
    ElMessage.success('任务状态已更新');
    await loadJobs();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function submitDelete(row: CampusReportJob) {
  if (!row.reportJobId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该自动报告任务？', '删除确认', { type: 'warning' });
    await deleteReportJob(row.reportJobId);
    ElMessage.success('自动报告任务已删除');
    await loadJobs();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

async function openLogs(row: CampusReportJob) {
  if (!row.reportJobId) {
    return;
  }
  logVisible.value = true;
  logLoading.value = true;
  try {
    logs.value = await listReportGenerationLogs(row.reportJobId);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '日志加载失败');
  } finally {
    logLoading.value = false;
  }
}

function reportTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    daily: '日报',
    weekly: '周报',
    monthly: '月报',
    special: '专报',
    event: '事件报告'
  };
  return labels[value || 'daily'] || value || '日报';
}

function templateOptionLabel(template: CampusReportTemplate) {
  return `${template.templateName}（${reportTypeLabel(template.reportType)}）`;
}

function eventOptionLabel(event: CampusEvent) {
  const risk = event.riskLevel ? ` / ${event.riskLevel}` : '';
  const status = event.eventStatus ? ` / ${event.eventStatus}` : '';
  return `${event.eventTitle}${risk}${status}`;
}

function periodRuleLabel(value?: string) {
  const labels: Record<string, string> = { daily: '日报周期', weekly: '周报周期', monthly: '月报周期' };
  return labels[value || 'daily'] || value || '日报周期';
}

function outputFormatLabel(value?: string) {
  const labels: Record<string, string> = { markdown: 'Markdown', html: 'HTML', text: '纯文本', docx: 'Word', pptx: 'PPT' };
  return labels[value || 'markdown'] || value || 'Markdown';
}

function jobStatusLabel(value?: string) {
  const labels: Record<string, string> = { active: '启用', paused: '暂停', disabled: '禁用' };
  return labels[value || 'paused'] || value || '暂停';
}

function jobStatusTagType(value?: string) {
  if (value === 'active') {
    return 'success';
  }
  if (value === 'paused') {
    return 'warning';
  }
  return 'info';
}

function runStatusLabel(value?: string) {
  const labels: Record<string, string> = { running: '运行中', success: '成功', failed: '失败' };
  return labels[value || 'running'] || value || '运行中';
}
</script>

<style scoped>
.form-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}
</style>
