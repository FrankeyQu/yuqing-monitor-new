<template>
  <section class="business-page">
    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="研判任务" name="tasks">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-select v-model="taskQuery.objectType" clearable placeholder="对象类型">
                <el-option label="线索" value="clue" />
                <el-option label="事件" value="event" />
                <el-option label="账号公开动态" value="account_content" />
              </el-select>
              <el-input-number v-model="taskQuery.objectId" :min="1" controls-position="right" placeholder="对象ID" />
              <el-select v-model="taskQuery.analysisType" clearable placeholder="分析类型">
                <el-option label="情感分析" value="sentiment" />
                <el-option label="风险建议" value="risk" />
                <el-option label="摘要生成" value="summary" />
                <el-option label="关键词提取" value="keywords" />
                <el-option label="综合研判" value="comprehensive" />
              </el-select>
              <el-select v-model="taskQuery.taskStatus" clearable placeholder="状态">
                <el-option label="待运行" value="pending" />
                <el-option label="运行中" value="running" />
                <el-option label="已完成" value="completed" />
                <el-option label="失败" value="failed" />
              </el-select>
              <el-button @click="loadTasks">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openTaskCreate">
              <Plus :size="16" />
              创建任务
            </el-button>
          </div>

          <el-table :data="tasks" v-loading="taskLoading" size="small" height="560">
            <el-table-column prop="analysisTaskId" label="任务ID" width="156" show-overflow-tooltip />
            <el-table-column prop="objectType" label="对象" width="120">
              <template #default="{ row }">{{ objectTypeLabel(row.objectType) }}</template>
            </el-table-column>
            <el-table-column prop="objectId" label="对象ID" width="120" />
            <el-table-column prop="analysisType" label="分析类型" width="128">
              <template #default="{ row }">{{ analysisTypeLabel(row.analysisType) }}</template>
            </el-table-column>
            <el-table-column prop="modelProvider" label="模型来源" width="140" show-overflow-tooltip />
            <el-table-column prop="taskStatus" label="状态" width="96">
              <template #default="{ row }">
                <el-tag :type="taskStatusTagType(row.taskStatus)" effect="plain">
                  {{ taskStatusLabel(row.taskStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="错误信息" min-width="180" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="136" fixed="right">
              <template #default="{ row }">
                <el-button link type="success" :disabled="row.taskStatus === 'running'" @click="submitRunTask(row)">
                  <Play :size="15" />
                  运行
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="taskQuery.pageNum"
              v-model:page-size="taskQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="taskTotal"
              @size-change="loadTasks"
              @current-change="loadTasks"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="研判结果" name="results">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input-number v-model="resultQuery.analysisTaskId" :min="1" controls-position="right" placeholder="任务ID" />
              <el-select v-model="resultQuery.objectType" clearable placeholder="对象类型">
                <el-option label="线索" value="clue" />
                <el-option label="事件" value="event" />
                <el-option label="账号公开动态" value="account_content" />
              </el-select>
              <el-select v-model="resultQuery.analysisType" clearable placeholder="分析类型">
                <el-option label="情感分析" value="sentiment" />
                <el-option label="风险建议" value="risk" />
                <el-option label="摘要生成" value="summary" />
                <el-option label="关键词提取" value="keywords" />
                <el-option label="综合研判" value="comprehensive" />
              </el-select>
              <el-select v-model="resultQuery.adoptionStatus" clearable placeholder="复核状态">
                <el-option label="待复核" value="pending" />
                <el-option label="已采纳" value="adopted" />
                <el-option label="已驳回" value="rejected" />
              </el-select>
              <el-button @click="loadResults">
                <Search :size="16" />
                查询
              </el-button>
            </div>
          </div>

          <el-table :data="results" v-loading="resultLoading" size="small" height="560">
            <el-table-column prop="summary" label="摘要/建议" min-width="220" show-overflow-tooltip />
            <el-table-column prop="assistiveLabel" label="标识" width="120" show-overflow-tooltip />
            <el-table-column prop="analysisType" label="类型" width="112">
              <template #default="{ row }">{{ analysisTypeLabel(row.analysisType) }}</template>
            </el-table-column>
            <el-table-column prop="sentiment" label="情感" width="86">
              <template #default="{ row }">{{ sentimentLabel(row.sentiment) }}</template>
            </el-table-column>
            <el-table-column prop="suggestedRiskLevel" label="建议风险" width="92">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.suggestedRiskLevel)" effect="plain">
                  {{ riskLabel(row.suggestedRiskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="keywords" label="关键词" width="140" show-overflow-tooltip />
            <el-table-column prop="confidence" label="置信度" width="90">
              <template #default="{ row }">{{ formatConfidence(row.confidence) }}</template>
            </el-table-column>
            <el-table-column prop="adoptionStatus" label="复核" width="92">
              <template #default="{ row }">
                <el-tag :type="adoptionTagType(row.adoptionStatus)" effect="plain">
                  {{ adoptionLabel(row.adoptionStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="生成时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openReview(row, 'adopted')">
                  <CheckCircle2 :size="15" />
                  采纳
                </el-button>
                <el-button link type="warning" @click="openReview(row, 'rejected')">
                  <XCircle :size="15" />
                  驳回
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="resultQuery.pageNum"
              v-model:page-size="resultQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="resultTotal"
              @size-change="loadResults"
              @current-change="loadResults"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="taskDialogVisible" title="创建辅助研判任务" width="720px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="对象类型" required>
            <el-select v-model="taskForm.objectType">
              <el-option label="线索" value="clue" />
              <el-option label="事件" value="event" />
              <el-option label="账号公开动态" value="account_content" />
            </el-select>
          </el-form-item>
          <el-form-item label="对象ID" required>
            <el-input-number v-model="taskForm.objectId" :min="1" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="分析类型" required>
            <el-select v-model="taskForm.analysisType">
              <el-option label="情感分析" value="sentiment" />
              <el-option label="风险建议" value="risk" />
              <el-option label="摘要生成" value="summary" />
              <el-option label="关键词提取" value="keywords" />
              <el-option label="综合研判" value="comprehensive" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型来源">
            <el-input v-model.trim="taskForm.modelProvider" disabled />
          </el-form-item>
        </div>
        <el-form-item label="请求参数">
          <el-input v-model.trim="taskForm.requestPayload" type="textarea" :rows="3" placeholder="可填写 JSON 备注，默认使用本地启发式研判" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitTask">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" :title="reviewForm.adoptionStatus === 'adopted' ? '采纳研判结果' : '驳回研判结果'" width="560px">
      <el-alert
        class="data-alert"
        title="辅助研判结果必须由人工复核后才能作为处置参考"
        type="info"
        show-icon
        :closable="false"
      />
      <el-form label-position="top" class="dialog-form">
        <el-form-item label="复核意见">
          <el-input v-model.trim="reviewForm.reviewOpinion" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReview">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { CheckCircle2, Play, Plus, Search, XCircle } from 'lucide-vue-next';
import {
  createAnalysisTask,
  listAnalysisResults,
  listAnalysisTasks,
  reviewAnalysisResult,
  runAnalysisTask
} from '../services/analysisReport';
import { campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type { CampusAnalysisResult, CampusAnalysisTask } from '../types/api';

const activeTab = ref('tasks');
const saving = ref(false);
const taskLoading = ref(false);
const resultLoading = ref(false);
const taskDialogVisible = ref(false);
const reviewDialogVisible = ref(false);
const tasks = ref<CampusAnalysisTask[]>([]);
const results = ref<CampusAnalysisResult[]>([]);
const taskTotal = ref(0);
const resultTotal = ref(0);
const currentResult = ref<CampusAnalysisResult>();

const taskQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  objectType: '',
  objectId: undefined as number | undefined,
  analysisType: '',
  taskStatus: ''
});
const resultQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  analysisTaskId: undefined as number | undefined,
  objectType: '',
  objectId: undefined as number | undefined,
  analysisType: '',
  adoptionStatus: ''
});
const taskForm = reactive<CampusAnalysisTask>({
  objectType: 'clue',
  objectId: undefined,
  analysisType: 'comprehensive',
  requestPayload: '',
  modelProvider: 'local_heuristic',
  modelName: 'local_heuristic_v1'
});
const reviewForm = reactive({
  adoptionStatus: 'adopted',
  reviewOpinion: ''
});

onMounted(loadTasks);
watch(activeTab, (tab) => {
  if (tab === 'results') {
    loadResults();
  }
});

async function loadTasks() {
  taskLoading.value = true;
  try {
    const page = await listAnalysisTasks(taskQuery);
    tasks.value = page.list || [];
    taskTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '研判任务加载失败');
  } finally {
    taskLoading.value = false;
  }
}

async function loadResults() {
  resultLoading.value = true;
  try {
    const page = await listAnalysisResults(resultQuery);
    results.value = page.list || [];
    resultTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '研判结果加载失败');
  } finally {
    resultLoading.value = false;
  }
}

function openTaskCreate() {
  Object.assign(taskForm, {
    analysisTaskId: undefined,
    objectType: taskQuery.objectType || 'clue',
    objectId: taskQuery.objectId,
    analysisType: taskQuery.analysisType || 'comprehensive',
    requestPayload: '',
    modelProvider: 'local_heuristic',
    modelName: 'local_heuristic_v1'
  });
  taskDialogVisible.value = true;
}

async function submitTask() {
  if (!taskForm.objectType || !taskForm.objectId || !taskForm.analysisType) {
    ElMessage.warning('对象类型、对象ID和分析类型不能为空');
    return;
  }
  saving.value = true;
  try {
    await createAnalysisTask({ ...taskForm });
    ElMessage.success('辅助研判任务已创建');
    taskDialogVisible.value = false;
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '创建失败');
  } finally {
    saving.value = false;
  }
}

async function submitRunTask(row: CampusAnalysisTask) {
  if (!row.analysisTaskId) {
    return;
  }
  try {
    const result = await runAnalysisTask(row.analysisTaskId);
    ElMessage.success(`研判完成：${analysisTypeLabel(result.analysisType)}`);
    await Promise.all([loadTasks(), loadResults()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '运行失败');
  }
}

function openReview(row: CampusAnalysisResult, adoptionStatus: string) {
  currentResult.value = row;
  reviewForm.adoptionStatus = adoptionStatus;
  reviewForm.reviewOpinion = row.reviewOpinion || '';
  reviewDialogVisible.value = true;
}

async function submitReview() {
  if (!currentResult.value?.analysisResultId) {
    return;
  }
  saving.value = true;
  try {
    await reviewAnalysisResult(
      currentResult.value.analysisResultId,
      reviewForm.adoptionStatus,
      reviewForm.reviewOpinion
    );
    ElMessage.success('复核结果已保存');
    reviewDialogVisible.value = false;
    await loadResults();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '复核失败');
  } finally {
    saving.value = false;
  }
}

function objectTypeLabel(value?: string) {
  const labels: Record<string, string> = { clue: '线索', event: '事件', account_content: '账号公开动态' };
  return labels[value || 'clue'] || value || '线索';
}

function analysisTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    sentiment: '情感分析',
    risk: '风险建议',
    summary: '摘要生成',
    keywords: '关键词提取',
    comprehensive: '综合研判'
  };
  return labels[value || 'comprehensive'] || value || '综合研判';
}

function taskStatusLabel(value?: string) {
  const labels: Record<string, string> = { pending: '待运行', running: '运行中', completed: '已完成', failed: '失败' };
  return labels[value || 'pending'] || value || '待运行';
}

function taskStatusTagType(value?: string) {
  if (value === 'completed') {
    return 'success';
  }
  if (value === 'failed') {
    return 'danger';
  }
  if (value === 'running') {
    return 'warning';
  }
  return 'info';
}

function sentimentLabel(value?: string) {
  const labels: Record<string, string> = { positive: '正向', neutral: '中性', negative: '负向' };
  return labels[value || 'neutral'] || value || '中性';
}

function riskLabel(value?: string) {
  return campusRiskLabel(value);
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}

function adoptionLabel(value?: string) {
  const labels: Record<string, string> = { pending: '待复核', adopted: '已采纳', rejected: '已驳回' };
  return labels[value || 'pending'] || value || '待复核';
}

function adoptionTagType(value?: string) {
  if (value === 'adopted') {
    return 'success';
  }
  if (value === 'rejected') {
    return 'danger';
  }
  return 'warning';
}

function formatConfidence(value?: number) {
  if (value === undefined || value === null) {
    return '-';
  }
  return `${Number(value).toFixed(2)}`;
}
</script>
