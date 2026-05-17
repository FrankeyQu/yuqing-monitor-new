<template>
  <section class="business-page">
    <section class="judgment-header">
      <div>
        <span>线索研判</span>
        <h2>舆情研判工作台</h2>
        <p>统一线索研判与处置。支持多语言线索的翻译、AI辅助研判与告警联动。</p>
      </div>
    </section>

    <section class="panel judgment-panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-select v-model="query.language" clearable placeholder="语言" style="width: 105px">
            <el-option label="全部" value="" />
            <el-option label="中文" value="zh" />
            <el-option label="蒙语" value="mongolian" />
            <el-option label="维语" value="uyghur" />
          </el-select>
          <el-select v-model="query.riskLevel" clearable placeholder="风险等级" style="width: 110px">
            <el-option label="全部" value="" />
            <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
          </el-select>
          <el-select v-model="query.clueStatus" clearable placeholder="状态" style="width: 110px">
            <el-option label="全部" value="" />
            <el-option label="待研判" value="pending_judge" />
            <el-option label="已研判" value="judged" />
          </el-select>
          <el-input v-model.trim="query.keyword" clearable placeholder="关键词" style="width: 180px" @keyup.enter="loadClues">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-button @click="loadClues">
            <Search :size="16" />
            搜索
          </el-button>
        </div>
      </div>

      <div class="judgment-workspace">
        <section class="judgment-table-panel">
          <el-table
            ref="tableRef"
            :data="rows"
            v-loading="loading"
            size="small"
            height="520"
            highlight-current-row
            @current-change="handleRowClick"
          >
            <el-table-column type="selection" width="40" />
            <el-table-column prop="clueTitle" label="线索标题" min-width="200" show-overflow-tooltip />
            <el-table-column label="语言" width="72">
              <template #default="{ row }">
                <el-tag :type="languageTagType(row.language)" effect="plain" size="small">
                  {{ languageLabel(row.language) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险" width="72">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain" size="small">
                  {{ riskLabel(row.riskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourcePlatform" label="来源" width="82" show-overflow-tooltip />
            <el-table-column prop="discoverTime" label="发现时间" width="155" show-overflow-tooltip />
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="query.pageNum"
              v-model:page-size="query.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="total"
              @size-change="loadClues"
              @current-change="loadClues"
            />
          </div>
        </section>

        <section class="judgment-detail-panel">
          <template v-if="selectedClue">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="线索详情" name="detail">
                <div class="detail-section">
                  <h4>原文内容</h4>
                  <div class="detail-content">
                    <p class="detail-title">{{ selectedClue.clueTitle }}</p>
                    <p v-if="selectedClue.clueContent" class="detail-body">{{ selectedClue.clueContent }}</p>
                    <p class="detail-meta">
                      来源：{{ selectedClue.sourcePlatform || '-' }}
                      | 发现时间：{{ selectedClue.discoverTime || '-' }}
                      | 涉及账号：{{ selectedClue.involvedAccount || '-' }}
                    </p>
                  </div>

                  <template v-if="selectedClue.language && selectedClue.language !== 'zh'">
                    <h4>AI 翻译结果</h4>
                    <div class="detail-content translation-box">
                      <p>{{ selectedClue.translationText || '暂无翻译结果' }}</p>
                    </div>
                  </template>
                </div>
              </el-tab-pane>
              <el-tab-pane label="AI研判建议" name="ai">
                <div class="detail-section">
                  <div v-if="aiSuggestion" class="detail-content ai-box">
                    <p>{{ aiSuggestion }}</p>
                  </div>
                  <div v-else class="detail-content empty-hint">
                    <p>暂无AI研判建议，请人工研判后填写意见。</p>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>

            <div class="judge-form">
              <el-form label-position="top">
                <el-form-item label="风险等级">
                  <el-select v-model="judgeForm.riskLevel">
                    <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="研判意见">
                  <el-input
                    v-model.trim="judgeForm.judgeOpinion"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入研判意见..."
                  />
                </el-form-item>
              </el-form>
            </div>

            <div class="judge-actions">
              <el-button type="primary" :loading="saving" @click="handleConfirmJudge">
                <CheckCircle2 :size="16" />
                确认研判
              </el-button>
              <el-button type="warning" plain :loading="saving" @click="handleRejectJudge">
                <RotateCcw :size="16" />
                驳回重判
              </el-button>
              <el-button type="danger" plain :loading="saving" @click="handleCreateAlert">
                <Bell :size="16" />
                产生告警
              </el-button>
              <el-button type="info" plain :loading="saving" @click="handleIgnore">
                <Ban :size="16" />
                忽略
              </el-button>
            </div>
          </template>
          <template v-else>
            <div class="detail-empty">
              <el-icon :size="36"><MousePointerClick /></el-icon>
              <p>请在左侧列表中选择一条线索开始研判</p>
            </div>
          </template>
        </section>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Ban, Bell, CheckCircle2, MousePointerClick, RotateCcw, Search } from 'lucide-vue-next';
import {
  archiveClue,
  createAlertFromClue,
  judgeClue,
  listCluesForJudgment
} from '../services/campusBusiness';
import { CAMPUS_RISK_OPTIONS, campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type { CampusClue } from '../types/api';

const loading = ref(false);
const saving = ref(false);
const rows = ref<CampusClue[]>([]);
const total = ref(0);
const selectedClue = ref<CampusClue>();
const activeTab = ref('detail');
const aiSuggestion = ref('');
const tableRef = ref();

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  language: '',
  riskLevel: '',
  clueStatus: '',
  keyword: ''
});

const judgeForm = reactive({
  riskLevel: 'normal',
  judgeOpinion: ''
});

onMounted(loadClues);

async function loadClues() {
  loading.value = true;
  try {
    const page = await listCluesForJudgment(query);
    rows.value = page.list || [];
    total.value = page.total || 0;
    if (selectedClue.value) {
      const found = rows.value.find((r) => r.clueId === selectedClue.value?.clueId);
      if (found) {
        selectedClue.value = found;
      } else {
        selectedClue.value = undefined;
        aiSuggestion.value = '';
      }
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '线索列表加载失败');
  } finally {
    loading.value = false;
  }
}

function handleRowClick(row: CampusClue | undefined) {
  selectedClue.value = row;
  activeTab.value = 'detail';
  if (row) {
    judgeForm.riskLevel = row.riskLevel || 'normal';
    judgeForm.judgeOpinion = row.judgeOpinion || '';
    aiSuggestion.value = row.translationText
      ? `AI分析：该内容属于${languageLabel(row.language)}线索，建议关注相关舆情发展。`
      : '';
  }
}

async function handleConfirmJudge() {
  if (!selectedClue.value?.clueId) {
    return;
  }
  if (!judgeForm.judgeOpinion) {
    ElMessage.warning('请输入研判意见');
    return;
  }
  saving.value = true;
  try {
    await judgeClue(selectedClue.value.clueId, judgeForm.riskLevel, judgeForm.judgeOpinion);
    ElMessage.success('研判已保存');
    selectedClue.value = undefined;
    aiSuggestion.value = '';
    await loadClues();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '研判保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleRejectJudge() {
  if (!selectedClue.value?.clueId) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认驳回线索「${selectedClue.value.clueTitle}」的研判结果，将重新进入待研判队列？`,
      '驳回确认',
      { confirmButtonText: '确认驳回', cancelButtonText: '取消', type: 'warning' }
    );
  } catch {
    return;
  }
  saving.value = true;
  try {
    await judgeClue(selectedClue.value.clueId, 'normal', '驳回重判：' + (judgeForm.judgeOpinion || '需重新研判'));
    ElMessage.success('已驳回，线索重新进入待研判队列');
    selectedClue.value = undefined;
    aiSuggestion.value = '';
    await loadClues();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '驳回失败');
  } finally {
    saving.value = false;
  }
}

async function handleCreateAlert() {
  if (!selectedClue.value?.clueId) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认根据线索「${selectedClue.value.clueTitle}」产生告警？`,
      '产生告警',
      { confirmButtonText: '确认产生', cancelButtonText: '取消', type: 'warning' }
    );
  } catch {
    return;
  }
  saving.value = true;
  try {
    await createAlertFromClue(selectedClue.value.clueId);
    ElMessage.success('告警已生成');
    await loadClues();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '告警生成失败');
  } finally {
    saving.value = false;
  }
}

async function handleIgnore() {
  if (!selectedClue.value?.clueId) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认忽略线索「${selectedClue.value.clueTitle}」？忽略后将归档该线索。`,
      '忽略确认',
      { confirmButtonText: '确认忽略', cancelButtonText: '取消', type: 'warning' }
    );
  } catch {
    return;
  }
  saving.value = true;
  try {
    await archiveClue(selectedClue.value.clueId, '研判忽略：' + (judgeForm.judgeOpinion || '不构成舆情风险'));
    ElMessage.success('线索已归档');
    selectedClue.value = undefined;
    aiSuggestion.value = '';
    await loadClues();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '忽略操作失败');
  } finally {
    saving.value = false;
  }
}

function riskLabel(value?: string) {
  return campusRiskLabel(value);
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}

function languageLabel(value?: string) {
  const labels: Record<string, string> = {
    zh: '中文',
    mongolian: '蒙语',
    uyghur: '维语'
  };
  return labels[value || ''] || '未知';
}

function languageTagType(value?: string) {
  if (value === 'zh') return '';
  if (value === 'mongolian') return 'success';
  if (value === 'uyghur') return 'warning';
  return 'info';
}
</script>

<style scoped>
.judgment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}
.judgment-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}
.judgment-header span {
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.judgment-header p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.judgment-panel {
  padding: 0;
  overflow: hidden;
}
.judgment-panel .toolbar {
  padding: 12px 16px;
  border-bottom: 1px solid #edf1f6;
}

.judgment-workspace {
  display: flex;
  height: calc(100vh - 260px);
  min-height: 560px;
}

.judgment-table-panel {
  flex: 1;
  min-width: 0;
  border-right: 1px solid #edf1f6;
  display: flex;
  flex-direction: column;
}
.judgment-table-panel .pagination-row {
  padding: 10px 16px;
  border-top: 1px solid #edf1f6;
  flex-shrink: 0;
}

.judgment-detail-panel {
  width: 420px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  background: #fafbfc;
}

.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  gap: 12px;
}
.detail-empty p {
  font-size: 14px;
  margin: 0;
}

.detail-section {
  padding: 12px 16px;
}
.detail-section h4 {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.detail-content {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
}
.detail-content p {
  margin: 0;
}
.detail-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 8px;
}
.detail-body {
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.detail-meta {
  margin-top: 10px;
  font-size: 12px;
  color: #94a3b8;
}
.translation-box {
  background: #f0f9ff;
  border-color: #bae6fd;
}
.translation-box p {
  font-size: 13px;
  color: #0c4a6e;
  line-height: 1.6;
}
.ai-box {
  background: #fefce8;
  border-color: #fde68a;
}
.ai-box p {
  font-size: 13px;
  color: #713f12;
  line-height: 1.6;
}
.empty-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
  color: #94a3b8;
}

.judge-form {
  padding: 0 16px;
  border-top: 1px solid #edf1f6;
}
.judge-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.judge-actions {
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  border-top: 1px solid #edf1f6;
  flex-shrink: 0;
}
.judge-actions .el-button {
  flex: 1 1 calc(50% - 4px);
  min-width: 140px;
  justify-content: center;
}

:deep(.el-tabs__header) {
  margin: 0 0 0 16px;
}
</style>
