<template>
  <section class="business-page">
    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="报告归档" name="reports">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="reportQuery.keyword" clearable placeholder="标题/摘要" @keyup.enter="loadReports">
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
            <el-table-column prop="reportFormat" label="格式" width="92">
              <template #default="{ row }">{{ reportFormatLabel(row.reportFormat) }}</template>
            </el-table-column>
            <el-table-column prop="generationMode" label="生成方式" width="96">
              <template #default="{ row }">
                <el-tag :type="row.generationMode === 'ai' ? '' : 'info'" effect="plain" size="small">
                  {{ row.generationMode === 'ai' ? 'AI智能' : '传统' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="eventId" label="事件ID" width="96" />
            <el-table-column prop="reportSummary" label="摘要" min-width="160" show-overflow-tooltip />
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
                      <el-dropdown-item command="md">
                        <FileText :size="14" />&nbsp;Markdown
                      </el-dropdown-item>
                      <el-dropdown-item command="docx">
                        <FileText :size="14" />&nbsp;Word
                      </el-dropdown-item>
                      <el-dropdown-item command="pptx">
                        <FileText :size="14" />&nbsp;PPT
                      </el-dropdown-item>
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
        </el-tab-pane>

        <el-tab-pane label="报告模板" name="templates">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="templateQuery.keyword" clearable placeholder="模板名称/备注" @keyup.enter="loadTemplates">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="templateQuery.reportType" clearable placeholder="报告类型">
                <el-option label="日报" value="daily" />
                <el-option label="周报" value="weekly" />
                <el-option label="月报" value="monthly" />
                <el-option label="专报" value="special" />
                <el-option label="事件报告" value="event" />
              </el-select>
              <el-select v-model="templateQuery.status" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadTemplates">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openTemplateCreate">
              <Plus :size="16" />
              新增模板
            </el-button>
          </div>

          <el-table :data="templates" v-loading="templateLoading" size="small" height="560">
            <el-table-column prop="templateName" label="模板名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="reportType" label="类型" width="102">
              <template #default="{ row }">{{ reportTypeLabel(row.reportType) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.status === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="210" show-overflow-tooltip />
            <el-table-column prop="updateTime" label="更新时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTemplateEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="danger" @click="submitTemplateDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="templateQuery.pageNum"
              v-model:page-size="templateQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="templateTotal"
              @size-change="loadTemplates"
              @current-change="loadTemplates"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="reportDialogVisible" :title="reportForm.reportId ? '编辑报告' : '新建报告'" width="820px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="报告标题" required>
            <el-input v-model.trim="reportForm.reportTitle" />
          </el-form-item>
          <el-form-item label="报告类型" required>
            <el-select v-model="reportForm.reportType">
              <el-option label="日报" value="daily" />
              <el-option label="周报" value="weekly" />
              <el-option label="月报" value="monthly" />
              <el-option label="专报" value="special" />
              <el-option label="事件报告" value="event" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="模板ID">
            <el-input-number v-model="reportForm.templateId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="关联事件ID">
            <el-input-number v-model="reportForm.eventId" :min="1" controls-position="right" />
          </el-form-item>
        </div>
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
        <el-form-item label="报告摘要">
          <el-input v-model.trim="reportForm.reportSummary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="报告正文">
          <el-input v-model="reportForm.reportContent" type="textarea" :rows="7" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReport">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateDialogVisible" :title="templateForm.templateId ? '编辑报告模板' : '新增报告模板'" width="820px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="模板名称" required>
            <el-input v-model.trim="templateForm.templateName" />
          </el-form-item>
          <el-form-item label="报告类型" required>
            <el-select v-model="templateForm.reportType">
              <el-option label="日报" value="daily" />
              <el-option label="周报" value="weekly" />
              <el-option label="月报" value="monthly" />
              <el-option label="专报" value="special" />
              <el-option label="事件报告" value="event" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="模板内容">
          <el-input v-model="templateForm.templateContent" type="textarea" :rows="9" />
        </el-form-item>
        <el-collapse style="margin-bottom: 16px;">
          <el-collapse-item title="可用变量参考" name="vars">
            <div class="variable-reference">
              <div class="var-group">
                <div class="var-group-title">基础变量</div>
                <el-tag size="small" v-for="v in baseVars" :key="v" effect="plain" @click="insertVar(v)">{{ v }}</el-tag>
              </div>
              <div class="var-group">
                <div class="var-group-title">数据变量</div>
                <el-tag size="small" v-for="v in dataVars" :key="v" effect="plain" @click="insertVar(v)">{{ v }}</el-tag>
              </div>
              <div class="var-group">
                <div class="var-group-title">表格变量</div>
                <el-tag size="small" v-for="v in tableVars" :key="v" effect="plain" @click="insertVar(v)">{{ v }}</el-tag>
              </div>
              <div class="var-group">
                <div class="var-group-title">列表变量</div>
                <el-tag size="small" v-for="v in listVars" :key="v" effect="plain" @click="insertVar(v)">{{ v }}</el-tag>
              </div>
              <div class="var-group">
                <div class="var-group-title">事件变量</div>
                <el-tag size="small" v-for="v in eventVars" :key="v" effect="plain" @click="insertVar(v)">{{ v }}</el-tag>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
        <div class="form-grid">
          <el-form-item label="状态">
            <el-switch v-model="templateEnabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model.trim="templateForm.remark" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitTemplate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="报告详情" width="860px">
      <div class="report-detail">
        <h2>{{ detailReport?.reportTitle }}</h2>
        <div class="selected-line">
          <span>{{ reportTypeLabel(detailReport?.reportType) }} · {{ reportStatusLabel(detailReport?.reportStatus) }}</span>
          <strong>{{ detailReport?.fileName || reportFormatLabel(detailReport?.reportFormat) }}</strong>
        </div>
        <div class="markdown-body" v-html="renderMarkdown(detailReport?.reportContent || '')" v-if="detailReport?.reportContent"></div>
        <pre v-else>{{ detailReport?.reportSummary || '暂无报告内容' }}</pre>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-dropdown v-if="detailReport?.reportId" trigger="click" @command="(fmt: string) => handleDownload(detailReport!, fmt)" style="margin-left:8px;">
          <el-button type="primary">
            下载
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="md">Markdown</el-dropdown-item>
              <el-dropdown-item command="docx">Word</el-dropdown-item>
              <el-dropdown-item command="pptx">PPT</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
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
                :label="tpl.templateName"
                :value="tpl.templateId"
              />
            </el-select>
          </el-form-item>
        </template>

        <template v-else>
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
            <el-button type="primary">
              下载 Markdown
            </el-button>
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
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Archive,
  Download,
  Eye,
  FileText,
  Pencil,
  Plus,
  Search,
  Sparkles,
  Trash2
} from 'lucide-vue-next';
import {
  archiveReport,
  deleteReportTemplate,
  downloadReport,
  downloadReportDocx,
  downloadReportPptx,
  generateReport,
  generateReportAi,
  getGenerateAiStreamUrl,
  getReportDetail,
  listReportTemplates,
  listReports,
  saveReport,
  saveReportTemplate
} from '../services/analysisReport';
import type { CampusReport, CampusReportTemplate } from '../types/api';

const activeTab = ref('reports');
const saving = ref(false);
const reportLoading = ref(false);
const templateLoading = ref(false);
const reportDialogVisible = ref(false);
const templateDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const archiveDialogVisible = ref(false);
const reports = ref<CampusReport[]>([]);
const templates = ref<CampusReportTemplate[]>([]);
const detailReport = ref<CampusReport>();
const currentReport = ref<CampusReport>();
const archiveOpinion = ref('');
const reportTotal = ref(0);
const templateTotal = ref(0);

const baseVars = ['${reportTitle}', '${reportType}', '${reportSummary}', '${periodStart}', '${periodEnd}'];
const dataVars = ['${totalCount}', '${negativeCount}', '${neutralCount}', '${positiveCount}'];
const tableVars = ['${trendTable}', '${mediaTable}', '${sentimentTable}', '${keywordTable}'];
const listVars = ['${hotArticles}', '${platformRanking}'];
const eventVars = ['${eventTitle}', '${eventSummary}', '${riskLevel}', '${eventStatus}'];

function insertVar(variable: string) {
  templateForm.templateContent = (templateForm.templateContent || '') + ' ' + variable;
}

const genDialogVisible = ref(false);
const genReport = ref<CampusReport>();
const genMode = ref<'template' | 'ai'>('ai');
const genTemplateId = ref<number>();
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
const templateQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  reportType: '',
  status: undefined as number | undefined
});
const reportForm = reactive<CampusReport>({
  reportTitle: '',
  reportType: 'daily',
  reportStatus: 'draft',
  templateId: undefined,
  eventId: undefined,
  periodStartTime: undefined,
  periodEndTime: undefined,
  reportSummary: '',
  reportContent: '',
  reportFormat: 'markdown'
});
const templateForm = reactive<CampusReportTemplate>({
  templateName: '',
  reportType: 'daily',
  templateContent: defaultTemplateContent(),
  status: 1,
  remark: ''
});

const templateEnabled = computed({
  get: () => templateForm.status !== 0,
  set: (value: boolean) => {
    templateForm.status = value ? 1 : 0;
  }
});

onMounted(loadReports);
watch(activeTab, (tab) => {
  if (tab === 'templates') {
    loadTemplates();
  }
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

async function loadTemplates() {
  templateLoading.value = true;
  try {
    const page = await listReportTemplates(templateQuery);
    templates.value = page.list || [];
    templateTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '模板列表加载失败');
  } finally {
    templateLoading.value = false;
  }
}

function openReportCreate() {
  Object.assign(reportForm, {
    reportId: undefined,
    reportTitle: '',
    reportType: 'daily',
    reportStatus: 'draft',
    templateId: undefined,
    eventId: undefined,
    periodStartTime: undefined,
    periodEndTime: undefined,
    reportSummary: '',
    reportContent: '',
    reportFormat: 'markdown'
  });
  reportDialogVisible.value = true;
}

function openReportEdit(row: CampusReport) {
  Object.assign(reportForm, row);
  reportDialogVisible.value = true;
}

async function submitReport() {
  if (!reportForm.reportTitle || !reportForm.reportType) {
    ElMessage.warning('报告标题和报告类型不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveReport({ ...reportForm });
    ElMessage.success('报告已保存');
    reportDialogVisible.value = false;
    await loadReports();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function openGenerateDialog(row: CampusReport) {
  if (!row.reportId) {
    return;
  }
  genReport.value = row;
  genMode.value = row.generationMode === 'ai' ? 'ai' : 'template';
  genTemplateId.value = row.templateId;
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
  } else {
    if (genStreaming.value) {
      await startStreamingGeneration(reportId);
    } else {
      genRunning.value = true;
      try {
        const result = await generateReportAi(reportId);
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
}

function startStreamingGeneration(reportId: number) {
  return new Promise<void>((resolve) => {
    genRunning.value = true;
    streamContent.value = '';
    genCompleted.value = false;

    const url = getGenerateAiStreamUrl(reportId);
    eventSource = new EventSource(url);

    eventSource.onmessage = async (event) => {
      if (event.data === '[DONE]') {
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
        return;
      }
      streamContent.value += event.data;
    };

    eventSource.onerror = () => {
      eventSource?.close();
      eventSource = null;
      genRunning.value = false;
      if (streamContent.value) {
        genCompleted.value = true;
        genResultContent.value = streamContent.value;
        ElMessage.warning('AI 流式连接中断，已生成部分内容');
      } else {
        ElMessage.error('AI 流式生成失败');
      }
      resolve();
    };
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

function openTemplateCreate() {
  Object.assign(templateForm, {
    templateId: undefined,
    templateName: '',
    reportType: 'daily',
    templateContent: defaultTemplateContent(),
    status: 1,
    remark: ''
  });
  templateDialogVisible.value = true;
}

function openTemplateEdit(row: CampusReportTemplate) {
  Object.assign(templateForm, row);
  templateDialogVisible.value = true;
}

async function submitTemplate() {
  if (!templateForm.templateName || !templateForm.reportType) {
    ElMessage.warning('模板名称和报告类型不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveReportTemplate({ ...templateForm });
    ElMessage.success('模板已保存');
    templateDialogVisible.value = false;
    await loadTemplates();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitTemplateDelete(row: CampusReportTemplate) {
  if (!row.templateId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该报告模板？', '删除确认', { type: 'warning' });
    await deleteReportTemplate(row.templateId);
    ElMessage.success('模板已删除');
    await loadTemplates();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
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

function reportFormatLabel(value?: string) {
  const labels: Record<string, string> = { markdown: 'Markdown', html: 'HTML', text: '纯文本' };
  return labels[value || 'markdown'] || value || 'Markdown';
}

function defaultTemplateContent() {
  return [
    '# ${reportTitle}',
    '',
    '## 一、基本情况',
    '${reportSummary}',
    '',
    '## 二、统计周期',
    '${periodStart} 至 ${periodEnd}',
    '',
    '## 三、事件情况',
    '- 事件标题：${eventTitle}',
    '- 当前状态：${eventStatus}',
    '',
    '## 四、处置建议',
    '请结合人工研判和部门反馈形成最终意见。'
  ].join('\n');
}
</script>

<style scoped>
.variable-reference {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.var-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}
.var-group-title {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  min-width: 64px;
}
.var-group .el-tag {
  cursor: pointer;
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
