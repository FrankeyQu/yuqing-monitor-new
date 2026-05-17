<template>
  <section class="business-page">
    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="reportQuery.keyword" clearable placeholder="标题/说明" @keyup.enter="loadReports">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-select v-model="reportQuery.reportType" clearable placeholder="报告类型">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
            <el-option label="专报" value="special" />
            <el-option label="事件报告" value="event" />
          </el-select>
          <el-select v-model="reportQuery.reportStatus" clearable placeholder="状态">
            <el-option label="草稿" value="draft" />
            <el-option label="已生成" value="generated" />
            <el-option label="已归档" value="archived" />
            <el-option label="已发布" value="published" />
          </el-select>
          <el-button @click="loadReports">
            <Search :size="16" />
            查询
          </el-button>
        </div>
        <el-button type="primary" @click="openReportCreate">
          <Plus :size="16" />
          新建报告
        </el-button>
      </div>

      <el-table :data="reports" v-loading="reportLoading" size="small" height="560">
        <el-table-column prop="reportTitle" label="报告标题" min-width="210" show-overflow-tooltip />
        <el-table-column prop="reportType" label="类型" width="102">
          <template #default="{ row }">{{ reportTypeLabel(row.reportType) }}</template>
        </el-table-column>
        <el-table-column prop="reportStatus" label="状态" width="96">
          <template #default="{ row }">
            <el-tag :type="reportStatusTagType(row.reportStatus)" effect="plain">
              {{ reportStatusLabel(row.reportStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generationMode" label="生成方式" width="96">
          <template #default="{ row }">
            <el-tag :type="row.generationMode === 'ai' ? '' : 'info'" effect="plain" size="small">
              {{ row.generationMode === 'ai' ? 'AI智能' : '传统' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventId" label="关联事件" width="150" show-overflow-tooltip />
        <el-table-column label="内容概览" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">{{ reportPreview(row) }}</template>
        </el-table-column>
        <el-table-column prop="generateTime" label="生成时间" width="168" show-overflow-tooltip />
        <el-table-column label="操作" width="400" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openReportEdit(row)">
              <Pencil :size="15" />
              编辑
            </el-button>
            <el-button link type="success" @click="openGenerateDialog(row)">
              <Sparkles :size="15" />
              生成
            </el-button>
            <el-button link type="info" @click="openReportDetail(row)">
              <Eye :size="15" />
              详情
            </el-button>
            <el-dropdown trigger="click" @command="(fmt: string) => handleDownload(row, fmt)">
              <el-button link type="primary">
                <Download :size="15" />
                下载
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="md"><FileText :size="14" />&nbsp;Markdown</el-dropdown-item>
                  <el-dropdown-item command="docx"><FileText :size="14" />&nbsp;Word</el-dropdown-item>
                  <el-dropdown-item command="pptx"><FileText :size="14" />&nbsp;PPT</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link type="warning" @click="openArchive(row)">
              <Archive :size="15" />
              归档
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="reportQuery.pageNum"
          v-model:page-size="reportQuery.pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="reportTotal"
          @size-change="loadReports"
          @current-change="loadReports"
        />
      </div>
    </section>

    <el-dialog v-model="reportDialogVisible" :title="reportForm.reportId ? '编辑报告' : '新建报告'" width="860px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="报告标题" required>
            <el-input v-model.trim="reportForm.reportTitle" />
          </el-form-item>
          <el-form-item label="报告类型" required>
            <el-select v-model="reportForm.reportType" @change="handleReportTypeChange">
              <el-option label="日报" value="daily" />
              <el-option label="周报" value="weekly" />
              <el-option label="月报" value="monthly" />
              <el-option label="专报" value="special" />
              <el-option label="事件报告" value="event" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="报告模板">
            <el-select
              v-model="reportForm.templateId"
              filterable
              remote
              clearable
              placeholder="搜索并选择模板"
              :remote-method="loadReportTemplateOptions"
              :loading="templateOptionLoading"
              style="width: 100%;"
            >
              <el-option
                v-for="tpl in reportTemplateOptions"
                :key="tpl.templateId"
                :label="templateOptionLabel(tpl)"
                :value="tpl.templateId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联事件">
            <el-select
              v-model="reportForm.eventId"
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
        </div>
        <div class="form-grid">
          <el-form-item label="生成方式">
            <el-radio-group v-model="reportForm.generationMode">
              <el-radio-button value="template">传统模板</el-radio-button>
              <el-radio-button value="ai">AI智能</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="分析档位">
            <el-select v-model="reportForm.analysisProfile">
              <el-option label="概览简报" value="brief" />
              <el-option label="风险研判" value="risk" />
              <el-option label="处置建议" value="disposal" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item v-if="reportForm.generationMode === 'ai'" label="AI 生成要求">
          <el-input v-model.trim="reportForm.aiUserPrompt" type="textarea" :rows="4" />
          <div class="form-tip">
            可填写本次报告的补充要求，例如重点关注风险研判、处置建议、某类群体反馈或领导汇报口径；系统会先按规则统计数据，再让 AI 按该要求润色和分析。
          </div>
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="统计范围">
            <el-select v-model="reportForm.scopeType">
              <el-option label="全量舆情" value="all" />
              <el-option label="关键词范围" value="keyword" />
              <el-option label="事件范围" value="event" />
              <el-option label="部门范围" value="department" />
              <el-option label="监测任务范围" value="monitor_task" />
              <el-option label="自定义范围" value="custom" />
            </el-select>
          </el-form-item>
          <el-form-item label="包含关键词">
            <el-input v-model.trim="reportForm.scopeKeywords" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="排除关键词">
            <el-input v-model.trim="reportForm.excludeKeywords" />
          </el-form-item>
          <el-form-item label="平台范围">
            <el-input v-model.trim="reportForm.platformScope" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="风险等级">
            <el-input v-model.trim="reportForm.riskLevels" />
          </el-form-item>
          <el-form-item label="部门ID范围">
            <el-input v-model.trim="reportForm.departmentScope" />
          </el-form-item>
        </div>
        <el-form-item label="监测任务ID范围">
          <el-input v-model.trim="reportForm.monitorTaskIds" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="统计开始">
            <el-date-picker v-model="reportForm.periodStartTime" type="datetime" />
          </el-form-item>
          <el-form-item label="统计结束">
            <el-date-picker v-model="reportForm.periodEndTime" type="datetime" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="报告格式">
            <el-select v-model="reportForm.reportFormat">
              <el-option label="Markdown" value="markdown" />
              <el-option label="HTML" value="html" />
              <el-option label="纯文本" value="text" />
            </el-select>
          </el-form-item>
          <el-form-item label="报告状态">
            <el-select v-model="reportForm.reportStatus">
              <el-option label="草稿" value="draft" />
              <el-option label="已生成" value="generated" />
              <el-option label="已归档" value="archived" />
              <el-option label="已发布" value="published" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="报告说明">
          <el-input v-model.trim="reportForm.reportSummary" type="textarea" :rows="3" />
          <div class="form-tip">报告说明用于记录人工备注或模板变量，不是最终正文；正文会在生成后进入详情页。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReport">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="报告详情" width="900px">
      <div class="detail-header">
        <div>
          <strong>{{ detailReport?.reportTitle }}</strong>
          <span>{{ reportTypeLabel(detailReport?.reportType) }}</span>
        </div>
        <el-tag :type="reportStatusTagType(detailReport?.reportStatus)" effect="plain">
          {{ reportStatusLabel(detailReport?.reportStatus) }}
        </el-tag>
      </div>
      <div class="markdown-body" v-html="renderMarkdown(detailReport?.reportContent || '')" v-if="detailReport?.reportContent"></div>
      <pre v-else>{{ detailReport?.reportSummary || '暂无报告内容' }}</pre>
    </el-dialog>

    <el-dialog v-model="genDialogVisible" title="生成报告" width="760px" @close="closeGenDialog">
      <div class="generate-info">
        <div class="gen-info-item"><strong>报告标题：</strong>{{ genReport?.reportTitle }}</div>
        <div class="gen-info-item"><strong>报告类型：</strong>{{ reportTypeLabel(genReport?.reportType) }}</div>
        <div class="gen-info-item" v-if="genReport?.periodStartTime || genReport?.periodEndTime">
          <strong>统计周期：</strong>{{ formatDate(genReport?.periodStartTime) }} ~ {{ formatDate(genReport?.periodEndTime) }}
        </div>
      </div>

      <el-divider />

      <el-form label-position="top">
        <el-form-item label="生成方式">
          <el-radio-group v-model="genMode" :disabled="genRunning">
            <el-radio-button value="template">传统模板</el-radio-button>
            <el-radio-button value="ai">AI智能</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <template v-if="genMode === 'template'">
          <el-form-item label="选择模板">
            <el-select v-model="genTemplateId" placeholder="请选择模板" :disabled="genRunning" style="width:100%;">
              <el-option
                v-for="tpl in availableTemplates"
                :key="tpl.templateId"
                :label="templateOptionLabel(tpl)"
                :value="tpl.templateId"
              />
            </el-select>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="AI 生成要求">
            <el-input v-model.trim="genAiUserPrompt" type="textarea" :rows="4" :disabled="genRunning" />
            <div class="form-tip">
              可填写本次报告的补充要求，例如重点关注风险研判、处置建议、某类群体反馈或领导汇报口径；系统会先按规则统计数据，再让 AI 按该要求润色和分析。
            </div>
          </el-form-item>
          <el-form-item label="流式生成">
            <el-switch v-model="genStreaming" :disabled="genRunning" active-text="开启" inactive-text="关闭" />
          </el-form-item>
        </template>
      </el-form>

      <div v-if="genRunning && genMode === 'ai' && genStreaming" class="streaming-preview">
        <div class="streaming-label">AI 正在生成...</div>
        <div class="streaming-content markdown-body" v-html="renderMarkdown(streamContent)"></div>
      </div>

      <div v-if="genCompleted" class="gen-result">
        <el-divider />
        <div class="gen-result-label">生成完成</div>
        <div class="gen-result-preview markdown-body" v-html="renderMarkdown(genResultContent)"></div>
      </div>

      <template #footer>
        <el-button @click="genDialogVisible = false">关闭</el-button>
        <el-button
          v-if="!genCompleted"
          type="primary"
          :loading="genRunning && !genStreaming"
          :disabled="genRunning || (genMode === 'template' && !genTemplateId)"
          @click="startGenerate"
        >
          {{ genRunning && genMode === 'ai' && genStreaming ? '生成中...' : '生成' }}
        </el-button>
        <template v-if="genCompleted">
          <el-dropdown trigger="click" @command="(fmt: string) => downloadGenResult(fmt)" style="margin-left:8px;">
            <el-button type="primary">下载 Markdown</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="md">Markdown</el-dropdown-item>
                <el-dropdown-item command="docx">Word</el-dropdown-item>
                <el-dropdown-item command="pptx">PPT</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button v-if="genReport?.reportId" type="warning" style="margin-left:8px;" @click="archiveGenReport">归档</el-button>
        </template>
      </template>
    </el-dialog>

    <el-dialog v-model="archiveDialogVisible" title="报告归档" width="560px">
      <el-form label-position="top">
        <el-form-item label="归档意见">
          <el-input v-model.trim="archiveOpinion" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="archiveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitArchive">归档</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Archive, Download, Eye, FileText, Pencil, Plus, Search, Sparkles } from 'lucide-vue-next';
import {
  archiveReport,
  downloadReport,
  downloadReportDocx,
  downloadReportPptx,
  generateReport,
  generateReportAi,
  getGenerateAiStreamUrl,
  getReportDetail,
  listReportTemplates,
  listReports,
  saveReport
} from '../services/analysisReport';
import { listEvents } from '../services/eventCenter';
import type { ApiId, CampusEvent, CampusReport, CampusReportTemplate } from '../types/api';

const saving = ref(false);
const reportLoading = ref(false);
const reportDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const archiveDialogVisible = ref(false);
const reports = ref<CampusReport[]>([]);
const detailReport = ref<CampusReport>();
const currentReport = ref<CampusReport>();
const archiveOpinion = ref('');
const reportTotal = ref(0);
const reportTemplateOptions = ref<CampusReportTemplate[]>([]);
const eventOptions = ref<CampusEvent[]>([]);
const templateOptionLoading = ref(false);
const eventOptionLoading = ref(false);

const genDialogVisible = ref(false);
const genReport = ref<CampusReport>();
const genMode = ref<'template' | 'ai'>('ai');
const genTemplateId = ref<ApiId>();
const genAiUserPrompt = ref('');
const genStreaming = ref(true);
const genRunning = ref(false);
const genCompleted = ref(false);
const streamContent = ref('');
const genResultContent = ref('');
const availableTemplates = ref<CampusReportTemplate[]>([]);
let eventSource: EventSource | null = null;

const reportQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  reportType: '',
  reportStatus: ''
});

const reportForm = reactive<CampusReport>({
  reportTitle: '',
  reportType: 'daily',
  reportStatus: 'draft',
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
  periodStartTime: undefined,
  periodEndTime: undefined,
  reportSummary: '',
  reportFormat: 'markdown',
  aiUserPrompt: ''
});

onMounted(() => {
  loadReports();
  loadReportTemplateOptions();
  loadEventOptions();
});

async function loadReports() {
  reportLoading.value = true;
  try {
    const page = await listReports(reportQuery);
    reports.value = page.list || [];
    reportTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '报告列表加载失败');
  } finally {
    reportLoading.value = false;
  }
}

async function loadReportTemplateOptions(keyword = '') {
  templateOptionLoading.value = true;
  try {
    const page = await listReportTemplates({
      pageNum: 1,
      pageSize: 50,
      keyword,
      reportType: reportForm.reportType,
      status: 1
    });
    reportTemplateOptions.value = page.list || [];
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
  reportForm.templateId = undefined;
  await loadReportTemplateOptions();
}

function openReportCreate() {
  Object.assign(reportForm, {
    reportId: undefined,
    reportTitle: '',
    reportType: 'daily',
    reportStatus: 'draft',
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
    periodStartTime: undefined,
    periodEndTime: undefined,
    reportSummary: '',
    reportContent: undefined,
    reportFormat: 'markdown',
    aiUserPrompt: ''
  });
  loadReportTemplateOptions();
  loadEventOptions();
  reportDialogVisible.value = true;
}

function openReportEdit(row: CampusReport) {
  Object.assign(reportForm, row);
  loadReportTemplateOptions();
  loadEventOptions();
  reportDialogVisible.value = true;
}

async function submitReport() {
  if (!reportForm.reportTitle || !reportForm.reportType) {
    ElMessage.warning('报告标题和报告类型不能为空');
    return;
  }
  saving.value = true;
  try {
    const payload = { ...reportForm };
    normalizeReportIds(payload);
    await saveReport(payload);
    ElMessage.success('报告已保存');
    reportDialogVisible.value = false;
    await loadReports();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function normalizeReportIds(payload: CampusReport) {
  if (payload.templateId === '') {
    payload.templateId = undefined;
  }
  if (payload.eventId === '') {
    payload.eventId = undefined;
  }
}

async function openGenerateDialog(row: CampusReport) {
  if (!row.reportId) {
    return;
  }
  genReport.value = row;
  genMode.value = row.generationMode === 'ai' ? 'ai' : 'template';
  genTemplateId.value = row.templateId;
  genAiUserPrompt.value = row.aiUserPrompt || '';
  genStreaming.value = true;
  genRunning.value = false;
  genCompleted.value = false;
  streamContent.value = '';
  genResultContent.value = '';

  if (row.reportType) {
    const page = await listReportTemplates({ pageNum: 1, pageSize: 200, reportType: row.reportType, status: 1 });
    availableTemplates.value = page.list || [];
  } else {
    availableTemplates.value = [];
  }
  genDialogVisible.value = true;
}

function closeGenDialog() {
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
  genRunning.value = false;
}

async function startGenerate() {
  if (!genReport.value?.reportId) {
    return;
  }
  const reportId = genReport.value.reportId;

  if (genMode.value === 'template') {
    genRunning.value = true;
    try {
      if (genTemplateId.value && genReport.value.templateId !== genTemplateId.value) {
        genReport.value = await saveReport({ ...genReport.value, templateId: genTemplateId.value, generationMode: 'template' });
      }
      await generateReport(reportId);
      genResultContent.value = (await getReportDetail(reportId)).reportContent || '生成完成';
      genCompleted.value = true;
      ElMessage.success('报告已生成');
      await loadReports();
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '生成失败');
    } finally {
      genRunning.value = false;
    }
  } else if (genStreaming.value) {
    await startStreamingGeneration(reportId);
  } else {
    genRunning.value = true;
    try {
      const result = await generateReportAi(reportId, genAiUserPrompt.value);
      genReport.value = result;
      genResultContent.value = result.reportContent || '';
      genCompleted.value = true;
      ElMessage.success('AI 报告生成完成');
      await loadReports();
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : 'AI 生成失败');
    } finally {
      genRunning.value = false;
    }
  }
}

function startStreamingGeneration(reportId: ApiId) {
  return new Promise<void>((resolve) => {
    genRunning.value = true;
    streamContent.value = '';
    genCompleted.value = false;

    const url = getGenerateAiStreamUrl(reportId, genAiUserPrompt.value);
    eventSource = new EventSource(url);

    const finish = async () => {
      eventSource?.close();
      eventSource = null;
      genRunning.value = false;
      genCompleted.value = true;
      try {
        const report = await getReportDetail(reportId);
        genReport.value = report;
        genResultContent.value = report.reportContent || streamContent.value;
      } catch {
        genResultContent.value = streamContent.value;
      }
      await loadReports();
      ElMessage.success('AI 流式生成完成');
      resolve();
    };

    eventSource.onmessage = async (event) => {
      if (event.data === '[DONE]') {
        await finish();
        return;
      }
      streamContent.value += event.data;
    };

    eventSource.addEventListener('done', finish);
    eventSource.addEventListener('error', (event) => {
      const message = event instanceof MessageEvent && event.data ? String(event.data) : '';
      eventSource?.close();
      eventSource = null;
      genRunning.value = false;
      if (streamContent.value) {
        genCompleted.value = true;
        genResultContent.value = streamContent.value;
        ElMessage.warning(message || 'AI 流式连接中断，已生成部分内容');
      } else {
        ElMessage.error(message || 'AI 流式生成失败');
      }
      resolve();
    });
  });
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
    .replace(/^# (.+)$/gm, '<h1>$1</h1>');

  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/^(\s*)- (.+)$/gm, '<li>$2</li>');

  let result = '';
  let inList = false;
  const lines = html.split('\n');
  for (const line of lines) {
    if (line.startsWith('<li>')) {
      if (!inList) {
        result += '<ul>';
        inList = true;
      }
      result += line;
    } else {
      if (inList) {
        result += '</ul>';
        inList = false;
      }
      if (line.startsWith('|') && line.endsWith('|')) {
        result += '<div class="md-table-row">' + line
          .replace(/^\|/, '')
          .replace(/\|$/, '')
          .replace(/\|/g, '<span class="md-table-cell">')
          .replace(/([^<])(<span)/g, '$1</span><span')
          .replace(/^<span class="md-table-cell">/, '<span class="md-table-cell">')
          + '</span></div>';
      } else {
        result += line.trim() ? `<p>${line}</p>` : '';
      }
    }
  }
  if (inList) {
    result += '</ul>';
  }

  return result;
}

function formatDate(value?: string | Date): string {
  if (!value) {
    return '';
  }
  const d = new Date(value);
  if (isNaN(d.getTime())) {
    return String(value);
  }
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

async function handleDownload(row: CampusReport, format: string) {
  if (!row.reportId) {
    return;
  }
  try {
    if (format === 'docx') {
      await downloadReportDocx(row.reportId);
    } else if (format === 'pptx') {
      await downloadReportPptx(row.reportId);
    } else {
      await downloadReport(row.reportId);
    }
    ElMessage.success('报告下载已开始');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '下载失败');
  }
}

async function downloadGenResult(format: string) {
  if (!genReport.value?.reportId) {
    return;
  }
  await handleDownload(genReport.value, format);
}

async function archiveGenReport() {
  if (!genReport.value?.reportId) {
    return;
  }
  try {
    await archiveReport(genReport.value.reportId, '');
    ElMessage.success('报告已归档');
    genDialogVisible.value = false;
    await loadReports();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '归档失败');
  }
}

async function openReportDetail(row: CampusReport) {
  if (!row.reportId) {
    return;
  }
  try {
    detailReport.value = await getReportDetail(row.reportId);
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '详情加载失败');
  }
}

function openArchive(row: CampusReport) {
  currentReport.value = row;
  archiveOpinion.value = row.archiveOpinion || '';
  archiveDialogVisible.value = true;
}

async function submitArchive() {
  if (!currentReport.value?.reportId) {
    return;
  }
  saving.value = true;
  try {
    await archiveReport(currentReport.value.reportId, archiveOpinion.value);
    ElMessage.success('报告已归档');
    archiveDialogVisible.value = false;
    await loadReports();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '归档失败');
  } finally {
    saving.value = false;
  }
}

function reportTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    daily: '日报',
    weekly: '周报',
    monthly: '月报',
    special: '专报',
    event: '事件报告',
    event_review: '事件复盘'
  };
  return labels[value || 'daily'] || value || '日报';
}

function reportStatusLabel(value?: string) {
  const labels: Record<string, string> = { draft: '草稿', generated: '已生成', archived: '已归档', published: '已发布' };
  return labels[value || 'draft'] || value || '草稿';
}

function reportStatusTagType(value?: string) {
  if (value === 'generated') {
    return 'success';
  }
  if (value === 'archived' || value === 'published') {
    return 'info';
  }
  return 'warning';
}

function reportPreview(row: CampusReport) {
  const source = row.reportContent || row.reportSummary || '';
  return source.replace(/[#*_`|>\-\n\r]+/g, ' ').replace(/\s+/g, ' ').trim().slice(0, 80) || '暂无内容';
}

function templateOptionLabel(template: CampusReportTemplate) {
  return `${template.templateName}（${reportTypeLabel(template.reportType)}）`;
}

function eventOptionLabel(event: CampusEvent) {
  const risk = event.riskLevel ? ` / ${event.riskLevel}` : '';
  const status = event.eventStatus ? ` / ${event.eventStatus}` : '';
  return `${event.eventTitle}${risk}${status}`;
}
</script>

<style scoped>
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.detail-header div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.detail-header strong {
  font-size: 16px;
  color: #303133;
}
.detail-header span {
  font-size: 13px;
  color: #909399;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}

.generate-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
  font-size: 13px;
  color: #303133;
}
.gen-info-item strong {
  color: #606266;
}
.gen-info-item {
  line-height: 1.8;
}

.streaming-preview {
  margin-top: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px 16px;
  max-height: 360px;
  overflow-y: auto;
  background: #fafafa;
}
.streaming-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}
.streaming-content {
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.gen-result {
  margin-top: 4px;
}
.gen-result-label {
  font-size: 13px;
  font-weight: 600;
  color: #67c23a;
  margin-bottom: 8px;
}
.gen-result-preview {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px 16px;
  max-height: 400px;
  overflow-y: auto;
  background: #fafafa;
  font-size: 13px;
  line-height: 1.7;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  margin: 8px 0 4px;
  font-weight: 600;
}
.markdown-body h1 { font-size: 18px; }
.markdown-body h2 { font-size: 16px; }
.markdown-body h3 { font-size: 14px; }
.markdown-body p {
  margin: 4px 0;
  line-height: 1.7;
}
.markdown-body ul {
  margin: 4px 0;
  padding-left: 20px;
}
.markdown-body li {
  line-height: 1.7;
}
.markdown-body strong {
  font-weight: 600;
}
.md-table-row {
  display: flex;
  gap: 0;
  border-bottom: 1px solid #ebeef5;
  padding: 2px 0;
}
.md-table-cell {
  flex: 1;
  padding: 2px 6px;
  font-size: 12px;
}
</style>
