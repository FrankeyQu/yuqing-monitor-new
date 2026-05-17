<template>
  <section class="business-page">
    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="事件库" name="events">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="eventQuery.keyword" clearable placeholder="事件标题/摘要" @keyup.enter="loadEvents">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="eventQuery.riskLevel" clearable placeholder="风险">
                <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
              </el-select>
              <el-select v-model="eventQuery.eventStatus" clearable placeholder="状态">
                <el-option label="待研判" value="pending_judge" />
                <el-option label="已定级" value="rated" />
                <el-option label="处理中" value="processing" />
                <el-option label="已归档" value="archived" />
              </el-select>
              <el-button @click="loadEvents">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openEventCreate">
              <Plus :size="16" />
              新增事件
            </el-button>
          </div>

          <el-table
            :data="events"
            v-loading="eventLoading"
            size="small"
            height="560"
            highlight-current-row
            @current-change="selectEvent"
          >
            <el-table-column prop="eventTitle" label="事件标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="eventType" label="类型" width="110" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="级别" width="82">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentHeat" label="热度" width="82" />
            <el-table-column prop="eventStatus" label="状态" width="98">
              <template #default="{ row }">
                <el-tag effect="plain">{{ eventStatusLabel(row.eventStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="discoverTime" label="发现时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="320" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openEventEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="warning" @click.stop="openRate(row)">
                  <Gauge :size="15" />
                  定级
                </el-button>
                <el-button link type="success" @click.stop="openRecord(row)">
                  <MessageSquareText :size="15" />
                  记录
                </el-button>
                <el-button link type="info" @click.stop="openArchive(row)">
                  <Archive :size="15" />
                  归档
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="eventQuery.pageNum"
              v-model:page-size="eventQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="eventTotal"
              @size-change="loadEvents"
              @current-change="loadEvents"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="处置记录" name="records">
          <div class="selected-line">
            <div>
              <span>当前事件</span>
              <strong>{{ selectedEvent?.eventTitle || '未选择' }}</strong>
            </div>
            <el-button type="primary" size="small" :disabled="!selectedEvent?.eventId" @click="selectedEvent && openRecord(selectedEvent)">
              <MessageSquareText :size="15" />
              新增记录
            </el-button>
          </div>

          <el-table :data="records" v-loading="recordLoading" size="small" height="560">
            <el-table-column prop="recordType" label="类型" width="110">
              <template #default="{ row }">
                <el-tag effect="plain">{{ recordTypeLabel(row.recordType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordContent" label="记录内容" min-width="280" show-overflow-tooltip />
            <el-table-column prop="handlerName" label="记录人" width="120" show-overflow-tooltip />
            <el-table-column prop="handleTime" label="记录时间" width="168" show-overflow-tooltip />
            <el-table-column prop="attachmentDesc" label="附件/备注" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="相似线索" name="similar">
          <div class="selected-line">
            <span>当前事件</span>
            <strong>{{ selectedEvent?.eventTitle || '未选择' }}</strong>
          </div>

          <el-table :data="similarClues" v-loading="similarLoading" size="small" height="560">
            <el-table-column prop="clueTitle" label="线索标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="topicCategory" label="主题" width="130" show-overflow-tooltip />
            <el-table-column prop="sourcePlatform" label="平台" width="120" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="级别" width="92">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="schoolRelevanceScore" label="相关性" width="90" />
            <el-table-column prop="discoverTime" label="发现时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="addSimilarClue(row)">
                  <Plus :size="15" />
                  加入事件
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="传播分析" name="spread">
          <div class="spread-analyze">
            <div class="spread-toolbar">
              <span class="spread-toolbar-label">选择事件：</span>
              <el-select
                v-model="spreadEventId"
                clearable
                filterable
                placeholder="请选择事件"
                style="width:400px"
              >
                <el-option
                  v-for="evt in events"
                  :key="evt.eventId"
                  :label="evt.eventTitle"
                  :value="evt.eventId"
                />
              </el-select>
            </div>

            <div v-loading="spreadLoading" class="spread-content">
              <template v-if="spreadData">
                <div class="spread-card">
                  <div class="spread-card-header">传播源头</div>
                  <div class="spread-card-body">
                    <p>
                      <span class="spread-label">首发媒体：</span>
                      {{ spreadData.source.media }}
                    </p>
                    <p>
                      <span class="spread-label">首发时间：</span>
                      {{ spreadData.source.time }}
                    </p>
                    <p>
                      <span class="spread-label">标题：</span>
                      {{ spreadData.source.title }}
                    </p>
                  </div>
                </div>

                <div class="spread-card">
                  <div class="spread-card-header">传播时间线</div>
                  <div ref="timelineChartRef" class="spread-chart" />
                </div>

                <div class="spread-card">
                  <div class="spread-card-header">关键媒体排行</div>
                  <div ref="mediaChartRef" class="spread-chart" />
                </div>

                <div class="spread-card">
                  <div class="spread-card-header">传播关系网络</div>
                  <div ref="relationChartRef" class="spread-chart" />
                </div>
              </template>

              <el-empty v-else description="请选择事件查看传播分析" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="eventDialogVisible" :title="eventForm.eventId ? '编辑事件' : '新增事件'" width="720px">
      <el-form label-position="top">
        <el-form-item label="事件标题" required>
          <el-input v-model.trim="eventForm.eventTitle" />
        </el-form-item>
        <el-form-item label="事件摘要">
          <el-input v-model.trim="eventForm.eventSummary" type="textarea" :rows="4" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="事件类型">
            <el-input v-model.trim="eventForm.eventType" />
          </el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="eventForm.riskLevel">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="影响范围">
            <el-input v-model.trim="eventForm.impactScope" />
          </el-form-item>
          <el-form-item label="当前热度">
            <el-input-number v-model="eventForm.currentHeat" :min="0" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="首发时间">
            <el-date-picker v-model="eventForm.firstPublishTime" type="datetime" />
          </el-form-item>
          <el-form-item label="发现时间">
            <el-date-picker v-model="eventForm.discoverTime" type="datetime" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="eventDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEvent">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rateVisible" title="风险定级" width="520px">
      <el-form label-position="top">
        <el-form-item label="风险等级">
          <el-select v-model="rateForm.riskLevel">
            <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置要求">
          <el-input v-model.trim="rateForm.disposalRequirement" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordVisible" title="记录线下处置" width="540px">
      <el-form label-position="top">
        <el-form-item label="记录内容" required>
          <el-input v-model.trim="recordForm.recordContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="附件/备注">
          <el-input v-model.trim="recordForm.attachmentDesc" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRecord">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="archiveVisible" title="事件归档" width="540px">
      <el-form label-position="top">
        <el-form-item label="归档结论" required>
          <el-input v-model.trim="archiveConclusion" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="archiveVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitArchive">归档</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import type { ECharts, EChartsOption } from 'echarts';
import { fetchSpreadData } from '../services/spread';
import type { SpreadData } from '../services/spread';
import {
  Archive,
  Gauge,
  MessageSquareText,
  Pencil,
  Plus,
  Search
} from 'lucide-vue-next';
import {
  addEventClue,
  addOfflineDisposalRecord,
  archiveEvent,
  listEventRecords,
  listSimilarEventClues,
  listEvents,
  rateEvent,
  saveEvent
} from '../services/eventCenter';
import { CAMPUS_RISK_OPTIONS, campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type { CampusClue, CampusDisposalRecord, CampusEvent } from '../types/api';

const activeTab = ref('events');
const eventLoading = ref(false);
const recordLoading = ref(false);
const similarLoading = ref(false);
const saving = ref(false);
const eventDialogVisible = ref(false);
const rateVisible = ref(false);
const recordVisible = ref(false);
const archiveVisible = ref(false);
const events = ref<CampusEvent[]>([]);
const records = ref<CampusDisposalRecord[]>([]);
const similarClues = ref<CampusClue[]>([]);
const eventTotal = ref(0);
const selectedEvent = ref<CampusEvent>();
const archiveConclusion = ref('');
// Spread analysis state
const spreadEventId = ref<number | undefined>(undefined);
const spreadLoading = ref(false);
const spreadData = ref<SpreadData | null>(null);
const timelineChartRef = ref<HTMLElement | null>(null);
const mediaChartRef = ref<HTMLElement | null>(null);
const relationChartRef = ref<HTMLElement | null>(null);
let timelineChart: ECharts | null = null;
let mediaChart: ECharts | null = null;
let relationChart: ECharts | null = null;
const eventQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  riskLevel: '',
  eventStatus: ''
});
const eventForm = reactive<CampusEvent>({
  eventTitle: '',
  eventType: '',
  eventSummary: '',
  firstPublishTime: undefined,
  discoverTime: undefined,
  riskLevel: 'normal',
  impactScope: '',
  currentHeat: 0
});
const rateForm = reactive({
  riskLevel: 'normal',
  disposalRequirement: ''
});
const recordForm = reactive({
  recordContent: '',
  attachmentDesc: ''
});

// ---- Spread (propagation) analysis functions ----

function renderTimelineChart() {
  if (!timelineChartRef.value || !spreadData.value) return;
  if (!timelineChart) {
    timelineChart = echarts.init(timelineChartRef.value);
  }
  const d = spreadData.value.timeline;
  timelineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: d.map((i) => i.time), boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      data: d.map((i) => i.count),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15, color: '#2563eb' },
      lineStyle: { width: 2, color: '#2563eb' },
      itemStyle: { color: '#2563eb' }
    }]
  } satisfies EChartsOption, true);
}

function renderMediaChart() {
  if (!mediaChartRef.value || !spreadData.value) return;
  if (!mediaChart) {
    mediaChart = echarts.init(mediaChartRef.value);
  }
  const sorted = [...spreadData.value.mediaRanking].sort((a, b) => b.articles - a.articles);
  mediaChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map((i) => i.name), inverse: true },
    series: [{
      data: sorted.map((i) => i.articles),
      type: 'bar',
      itemStyle: { color: '#0f766e', borderRadius: [0, 3, 3, 0] }
    }]
  } satisfies EChartsOption, true);
}

function renderRelationChart() {
  if (!relationChartRef.value || !spreadData.value) return;
  if (!relationChart) {
    relationChart = echarts.init(relationChartRef.value);
  }
  const categories = [
    { name: '事件源', itemStyle: { color: '#dc2626' } },
    { name: '媒体账号', itemStyle: { color: '#2563eb' } },
    { name: '普通用户', itemStyle: { color: '#d97706' } }
  ];
  relationChart.setOption({
    tooltip: {},
    series: [{
      type: 'graph',
      layout: 'force',
      force: { repulsion: 350, edgeLength: [80, 150] },
      roam: true,
      draggable: true,
      data: spreadData.value.relationNodes.map((n) => ({
        id: String(n.id),
        name: n.name,
        category: n.category
      })),
      links: spreadData.value.relationLinks.map((l) => ({
        source: String(l.source),
        target: String(l.target)
      })),
      categories,
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 10],
      label: { show: true, position: 'right', fontSize: 12 },
      lineStyle: { color: 'source', curveness: 0.3, width: 1.5 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3 }
      }
    }]
  } satisfies EChartsOption, true);
}

function renderSpreadCharts() {
  renderTimelineChart();
  renderMediaChart();
  renderRelationChart();
}

async function loadSpreadData() {
  if (!spreadEventId.value) {
    spreadData.value = null;
    return;
  }
  spreadLoading.value = true;
  try {
    spreadData.value = await fetchSpreadData(spreadEventId.value);
    await nextTick();
    renderSpreadCharts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '传播数据加载失败');
  } finally {
    spreadLoading.value = false;
  }
}

function handleSpreadResize() {
  timelineChart?.resize();
  mediaChart?.resize();
  relationChart?.resize();
}

onMounted(loadEvents);
onMounted(() => { window.addEventListener('resize', handleSpreadResize); });
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleSpreadResize);
  timelineChart?.dispose();
  mediaChart?.dispose();
  relationChart?.dispose();
});

watch(activeTab, (tab) => {
  if (tab === 'records' && selectedEvent.value?.eventId) {
    loadRecords();
  }
  if (tab === 'similar' && selectedEvent.value?.eventId) {
    loadSimilarClues();
  }
  if (tab === 'spread') {
    if (spreadEventId.value) {
      loadSpreadData();
    }
    nextTick(() => {
      handleSpreadResize();
    });
  }
});

watch(spreadEventId, (id) => {
  if (id && activeTab.value === 'spread') {
    loadSpreadData();
  }
});

async function loadEvents() {
  eventLoading.value = true;
  try {
    const page = await listEvents(eventQuery);
    events.value = page.list || [];
    eventTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '事件列表加载失败');
  } finally {
    eventLoading.value = false;
  }
}

async function selectEvent(row?: CampusEvent) {
  selectedEvent.value = row;
  if (activeTab.value === 'records' && row?.eventId) {
    await loadRecords();
  }
  if (activeTab.value === 'similar' && row?.eventId) {
    await loadSimilarClues();
  }
}

async function loadRecords() {
  if (!selectedEvent.value?.eventId) {
    records.value = [];
    return;
  }
  recordLoading.value = true;
  try {
    records.value = await listEventRecords(selectedEvent.value.eventId);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '处置记录加载失败');
  } finally {
    recordLoading.value = false;
  }
}

async function loadSimilarClues() {
  if (!selectedEvent.value?.eventId) {
    similarClues.value = [];
    return;
  }
  similarLoading.value = true;
  try {
    similarClues.value = await listSimilarEventClues(selectedEvent.value.eventId, 10);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '相似线索加载失败');
  } finally {
    similarLoading.value = false;
  }
}

function resetEventForm() {
  Object.assign(eventForm, {
    eventId: undefined,
    eventTitle: '',
    eventType: '',
    eventSummary: '',
    firstPublishTime: undefined,
    discoverTime: new Date(),
    riskLevel: 'normal',
    impactScope: '',
    currentHeat: 0
  });
}

function openEventCreate() {
  resetEventForm();
  eventDialogVisible.value = true;
}

function openEventEdit(row: CampusEvent) {
  Object.assign(eventForm, row);
  eventDialogVisible.value = true;
}

async function submitEvent() {
  if (!eventForm.eventTitle) {
    ElMessage.warning('事件标题不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveEvent({ ...eventForm });
    ElMessage.success('保存成功');
    eventDialogVisible.value = false;
    await loadEvents();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function openRate(row: CampusEvent) {
  selectedEvent.value = row;
  rateForm.riskLevel = row.riskLevel || 'normal';
  rateForm.disposalRequirement = row.disposalRequirement || '';
  rateVisible.value = true;
}

async function submitRate() {
  if (!selectedEvent.value?.eventId) {
    return;
  }
  saving.value = true;
  try {
    await rateEvent(selectedEvent.value.eventId, rateForm.riskLevel, rateForm.disposalRequirement);
    ElMessage.success('风险等级已更新');
    rateVisible.value = false;
    await loadEvents();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '定级失败');
  } finally {
    saving.value = false;
  }
}

function openRecord(row: CampusEvent) {
  selectedEvent.value = row;
  Object.assign(recordForm, {
    recordContent: '',
    attachmentDesc: ''
  });
  recordVisible.value = true;
}

function openArchive(row: CampusEvent) {
  selectedEvent.value = row;
  archiveConclusion.value = row.archiveConclusion || '';
  archiveVisible.value = true;
}

async function submitArchive() {
  if (!selectedEvent.value?.eventId || !archiveConclusion.value) {
    ElMessage.warning('归档结论不能为空');
    return;
  }
  saving.value = true;
  try {
    await archiveEvent(selectedEvent.value.eventId, archiveConclusion.value);
    ElMessage.success('事件已归档');
    archiveVisible.value = false;
    await loadEvents();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '归档失败');
  } finally {
    saving.value = false;
  }
}

async function submitRecord() {
  if (!selectedEvent.value?.eventId || !recordForm.recordContent) {
    ElMessage.warning('记录内容不能为空');
    return;
  }
  saving.value = true;
  try {
    await addOfflineDisposalRecord(selectedEvent.value.eventId, recordForm.recordContent, recordForm.attachmentDesc);
    ElMessage.success('处置记录已保存');
    recordVisible.value = false;
    await loadEvents();
    if (activeTab.value === 'records') {
      await loadRecords();
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function addSimilarClue(row: CampusClue) {
  if (!selectedEvent.value?.eventId || !row.clueId) {
    ElMessage.warning('请先选择事件和线索');
    return;
  }
  saving.value = true;
  try {
    await addEventClue(selectedEvent.value.eventId, row.clueId);
    ElMessage.success('线索已加入事件');
    await loadSimilarClues();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加入事件失败');
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

function eventStatusLabel(value?: string) {
  const labels: Record<string, string> = {
    pending_judge: '待研判',
    rated: '已定级',
    assigned: '已分派',
    processing: '处理中',
    feedback: '已记录',
    reviewed: '已处置',
    archived: '已归档'
  };
  return labels[value || 'pending_judge'] || value || '待研判';
}

function recordTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    offline: '线下处置',
    feedback: '处置反馈',
    return: '退回记录',
    confirm: '复核记录',
    assign: '分派记录'
  };
  return labels[value || 'offline'] || value || '处置记录';
}
</script>

<style scoped>
.spread-analyze {
  padding: 8px 0;
}

.spread-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.spread-toolbar-label {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
}

.spread-content {
  min-height: 400px;
}

.spread-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 16px;
  overflow: hidden;
  background: #fff;
}

.spread-card-header {
  padding: 10px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.spread-card-body {
  padding: 14px 16px;
  font-size: 14px;
  line-height: 2;
  color: #606266;
}

.spread-card-body p {
  margin: 4px 0;
}

.spread-label {
  display: inline-block;
  width: 80px;
  color: #909399;
  font-weight: 500;
}

.spread-chart {
  width: 100%;
  height: 320px;
}
</style>
