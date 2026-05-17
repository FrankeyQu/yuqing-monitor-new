<template>
  <section ref="pageRef" class="dashboard-unified" :class="{ 'is-screen-mode': isScreenMode }">
    <header class="dashboard-hero">
      <div>
        <span>{{ isScreenMode ? '校园舆情态势大屏' : '校园舆情监测' }}</span>
        <h2>{{ isScreenMode ? '任务运行、关键词命中、负面告警闭环' : '舆情态势工作台' }}</h2>
      </div>
      <div class="dashboard-actions">
        <strong v-if="isScreenMode" class="screen-time">{{ nowText }}</strong>
        <el-button type="primary" plain :loading="loading" @click="loadAll">
          <RefreshCw :size="16" />
          刷新
        </el-button>
        <el-button v-if="!isScreenMode" type="primary" @click="enterScreenMode">
          <Maximize2 :size="16" />
          大屏模式
        </el-button>
        <el-button v-else type="primary" plain @click="exitScreenMode">
          <Minimize2 :size="16" />
          退出大屏
        </el-button>
      </div>
    </header>

    <el-alert
      v-if="errorMessage"
      class="data-alert"
      :title="errorMessage"
      type="warning"
      show-icon
      :closable="false"
    />

    <section class="screen-metrics dashboard-metrics">
      <article v-for="card in metricCards" :key="card.label" class="screen-card" :class="`tone-${card.tone}`">
        <div class="screen-card-icon">
          <component :is="card.icon" :size="24" />
        </div>
        <div>
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
        </div>
      </article>
    </section>

    <template v-if="!isScreenMode">
      <section class="screen-grid dashboard-top-grid">
        <article class="screen-panel">
          <div class="panel-header">
            <h2>热点词云</h2>
          </div>
          <WordCloud
            :words="wordCloudData"
            :loading="loading"
            :min-height="320"
            @word-click="onWordClick"
          />
        </article>

        <article class="screen-panel screen-panel-large">
          <div class="panel-header">
            <h2>监测近 7 日趋势</h2>
            <ChartToolbar
              :chart-ref="monitorTrendChartRef"
              :chart-data="monitorTrendRows"
              title="监测近 7 日趋势"
              @refresh="loadAll"
            />
          </div>
          <div ref="monitorTrendChartRef" class="screen-chart screen-chart-lg" />
        </article>
      </section>

      <section class="screen-grid dashboard-monitor-grid">
        <article class="screen-panel">
          <div class="panel-header">
            <div class="panel-title-line">
              <h2>最新监测命中</h2>
              <el-tag v-if="monitorResultTotal" effect="plain" type="info">共 {{ monitorResultTotal }}</el-tag>
            </div>
            <el-button link type="primary" @click="router.push({ path: '/monitor', query: { hitScope: 'all' } })">查看全部</el-button>
          </div>
          <div class="screen-list">
            <div v-for="item in monitorResults" :key="item.monitorResultId || item.title" class="screen-list-row screen-feed-row">
              <div class="screen-list-main">
                <span>{{ item.title || '未命名内容' }}</span>
                <small>{{ item.platform || item.sourcePlatform || '监测命中' }} · {{ item.matchedKeywords || item.matchedNegativeWords || '-' }}</small>
              </div>
              <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
            </div>
            <el-empty v-if="!monitorResults.length && !loading" description="暂无监测命中" />
          </div>
        </article>

        <article class="screen-panel">
          <div class="panel-header">
            <h2>待处理负面告警</h2>
            <el-button link type="primary" @click="router.push('/monitor')">进入</el-button>
          </div>
          <div class="screen-list">
            <div v-for="item in monitorAlerts" :key="item.alertId" class="screen-list-row">
              <div class="screen-list-main">
                <span>{{ item.alertTitle }}</span>
                <small>{{ item.matchedKeywords || alertSourceLabel(item.alertSource) }}</small>
              </div>
              <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
            </div>
            <el-empty v-if="!monitorAlerts.length && !loading" description="暂无待处理告警" />
          </div>
        </article>
      </section>

      <section class="screen-grid screen-grid-main">
        <article class="screen-panel screen-panel-large">
          <div class="panel-header">
            <h2>近 7 日舆情流入趋势</h2>
            <ChartToolbar
              :chart-ref="trendChartRef"
              :chart-data="trendRows"
              title="近 7 日舆情流入趋势"
              @refresh="loadAll"
            />
          </div>
          <div ref="trendChartRef" class="screen-chart screen-chart-lg" />
        </article>

        <article class="screen-panel score-panel">
          <div class="panel-header">
            <h2>风险压力指数</h2>
            <ChartToolbar
              :chart-ref="riskGaugeRef"
              :chart-data="riskGaugeData"
              title="风险压力指数"
              @refresh="loadAll"
            />
            <el-tag :type="riskScoreTag" effect="plain">{{ riskScoreLevel }}</el-tag>
          </div>
          <div ref="riskGaugeRef" class="screen-chart screen-gauge" />
          <div class="score-facts">
            <div>
              <span>高风险事件</span>
              <strong>{{ statistics.overview.highRiskEventCount ?? 0 }}</strong>
            </div>
            <div>
              <span>待处理预警</span>
              <strong>{{ statistics.overview.pendingAlertCount ?? 0 }}</strong>
            </div>
            <div>
              <span>超期处置</span>
              <strong>{{ statistics.overview.overdueDisposalCount ?? 0 }}</strong>
            </div>
          </div>
        </article>
      </section>

      <section class="screen-panel">
        <div class="panel-header">
          <h2>最新舆情线索</h2>
          <el-button link type="primary" @click="router.push('/monitor')">进入监测信息</el-button>
        </div>
        <el-table
          :data="pendingClues"
          size="small"
          v-loading="loading"
          :show-header="true"
          stripe
          class="dashboard-clue-table"
        >
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="clueTitle" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="sourcePlatform" label="来源" width="120">
            <template #default="{ row }">
              <PlatformBadge :platform="row.sourcePlatform || row.clueSource || ''" show-icon />
            </template>
          </el-table-column>
          <el-table-column prop="sentiment" label="情感" width="90">
            <template #default="{ row }">
              <span :class="['sentiment-tag', `sentiment-${row.sentiment || 'unknown'}`]">
                {{ sentimentLabel(row.sentiment) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="publishTime" label="时间" width="160">
            <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section class="screen-grid screen-grid-three">
        <article class="screen-panel">
          <div class="panel-header">
            <h2>事件风险分布</h2>
            <ChartToolbar :chart-ref="riskChartRef" :chart-data="riskRows" title="事件风险分布" @refresh="loadAll" />
          </div>
          <div ref="riskChartRef" class="screen-chart screen-chart-sm" />
        </article>

        <article class="screen-panel">
          <div class="panel-header">
            <h2>情感分布</h2>
            <ChartToolbar :chart-ref="sentimentChartRef" :chart-data="sentimentRows" title="情感分布" @refresh="loadAll" />
          </div>
          <div ref="sentimentChartRef" class="screen-chart screen-chart-sm" />
        </article>

        <article class="screen-panel">
          <div class="panel-header">
            <h2>媒体来源分布</h2>
            <ChartToolbar :chart-ref="sourceChartRef" :chart-data="sourceRows" title="媒体来源分布" @refresh="loadAll" />
          </div>
          <div ref="sourceChartRef" class="screen-chart screen-chart-sm" />
        </article>
      </section>

      <section class="screen-grid screen-grid-three">
        <article class="screen-panel">
          <div class="panel-header">
            <h2>待处理预警</h2>
            <el-button link type="primary" @click="router.push('/alerts')">进入</el-button>
          </div>
          <div class="screen-list">
            <div v-for="item in pendingAlerts" :key="item.alertId" class="screen-list-row">
              <span>{{ item.alertTitle }}</span>
              <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
            </div>
            <el-empty v-if="!pendingAlerts.length && !loading" description="暂无待处理预警" />
          </div>
        </article>

        <article class="screen-panel">
          <div class="panel-header">
            <h2>检测命中</h2>
            <el-button link type="primary" @click="router.push('/admin/monitor-tasks')">进入</el-button>
          </div>
          <div class="screen-list">
            <div v-for="item in pendingHits" :key="item.hitId" class="screen-list-row">
              <span>{{ item.objectTitle }}</span>
              <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ item.matchedKeywords || '命中' }}</el-tag>
            </div>
            <el-empty v-if="!pendingHits.length && !loading" description="暂无待处理命中" />
          </div>
        </article>

        <article class="screen-panel">
          <div class="panel-header">
            <h2>处置中事件</h2>
            <el-button link type="primary" @click="router.push('/events')">进入</el-button>
          </div>
          <div class="screen-list">
            <div v-for="item in activeEvents" :key="item.eventId" class="screen-list-row">
              <span>{{ item.eventTitle }}</span>
              <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
            </div>
            <el-empty v-if="!activeEvents.length && !loading" description="暂无处置中事件" />
          </div>
        </article>
      </section>

      <section class="screen-panel task-strip-panel">
        <div class="panel-header">
          <h2>运行中的监测任务</h2>
          <el-button link type="primary" @click="router.push('/admin/monitor-tasks')">进入</el-button>
        </div>
        <div class="task-strip-list">
          <div v-for="item in monitorTasks" :key="item.monitorTaskId || item.taskName" class="task-strip-row">
            <div class="task-strip-main">
              <span>{{ item.taskName }}</span>
              <small>{{ item.monitorSubject || '未设置主体' }}</small>
            </div>
            <span class="task-strip-meta">{{ taskKeywordsLabel(item) }}</span>
            <span class="task-strip-meta">{{ taskAiAnalysisLabel(item) }}</span>
            <span class="task-strip-meta">{{ taskScheduleLabel(item) }}</span>
            <span class="task-strip-meta task-match-count">近次命中：{{ item.lastMatchCount ?? 0 }}</span>
            <el-tag :type="taskStatusTagType(item.taskStatus)" effect="plain">{{ taskStatusLabel(item.taskStatus) }}</el-tag>
          </div>
          <el-empty v-if="!monitorTasks.length && !loading" description="暂无运行任务" />
        </div>
      </section>
    </template>

    <template v-else>
      <section class="dashboard-screen-stage">
        <div class="dashboard-screen-main">
          <article class="screen-panel screen-cell-wide">
            <div class="panel-header">
              <h2>监测近 7 日趋势</h2>
              <el-tag effect="plain" type="warning">命中 / 告警</el-tag>
            </div>
            <div ref="monitorTrendChartRef" class="screen-chart screen-chart-lg" />
          </article>

          <article class="screen-panel monitor-command-panel">
            <div class="panel-header">
              <h2>监测运行</h2>
              <el-tag effect="plain" type="success">任务 / 频率</el-tag>
            </div>
            <div class="monitor-run-facts">
              <div v-for="item in monitorRunFacts" :key="item.label">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.note }}</small>
              </div>
            </div>
          </article>

          <article class="screen-panel score-panel">
            <div class="panel-header">
              <h2>风险压力指数</h2>
              <el-tag :type="riskScoreTag" effect="plain">{{ riskScoreLevel }}</el-tag>
            </div>
            <div ref="riskGaugeRef" class="screen-chart screen-gauge" />
            <div class="score-facts">
              <div>
                <span>高风险</span>
                <strong>{{ statistics.overview.highRiskEventCount ?? 0 }}</strong>
              </div>
              <div>
                <span>待处理</span>
                <strong>{{ statistics.overview.pendingAlertCount ?? 0 }}</strong>
              </div>
              <div>
                <span>超期</span>
                <strong>{{ statistics.overview.overdueDisposalCount ?? 0 }}</strong>
              </div>
            </div>
          </article>

          <article class="screen-panel">
            <div class="panel-header">
              <div class="panel-title-line">
                <h2>最新监测命中</h2>
                <el-tag v-if="monitorResultTotal" effect="plain" type="info">共 {{ monitorResultTotal }}</el-tag>
              </div>
            </div>
            <div class="screen-list compact-list">
              <div v-for="item in monitorResults.slice(0, 6)" :key="item.monitorResultId || item.title" class="screen-list-row screen-feed-row">
                <div class="screen-list-main">
                  <span>{{ item.title || '未命名内容' }}</span>
                  <small>{{ item.platform || item.sourcePlatform || '监测命中' }} · {{ item.matchedKeywords || item.matchedNegativeWords || '-' }}</small>
                </div>
                <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
              </div>
              <el-empty v-if="!monitorResults.length && !loading" description="暂无监测命中" />
            </div>
          </article>

          <article class="screen-panel">
            <div class="panel-header">
              <h2>待处理负面告警</h2>
            </div>
            <div class="screen-list compact-list">
              <div v-for="item in monitorAlerts.slice(0, 6)" :key="item.alertId" class="screen-list-row">
                <div class="screen-list-main">
                  <span>{{ item.alertTitle }}</span>
                  <small>{{ item.matchedKeywords || alertSourceLabel(item.alertSource) }}</small>
                </div>
                <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
              </div>
              <el-empty v-if="!monitorAlerts.length && !loading" description="暂无待处理告警" />
            </div>
          </article>
        </div>

        <div class="dashboard-screen-bottom">
          <article class="screen-panel">
            <div class="panel-header">
              <h2>事件处置状态</h2>
            </div>
            <div ref="eventStatusChartRef" class="screen-chart screen-chart-sm" />
          </article>

          <article class="screen-panel">
            <div class="panel-header">
              <h2>来源风险构成</h2>
            </div>
            <div ref="sourceRiskChartRef" class="screen-chart screen-chart-sm" />
          </article>

          <article class="screen-panel">
            <div class="panel-header">
              <h2>处置中事件热度</h2>
            </div>
            <div ref="eventHeatChartRef" class="screen-chart screen-chart-sm" />
          </article>
        </div>

        <article class="screen-panel task-strip-panel">
          <div class="panel-header">
            <h2>运行中的监测任务</h2>
          </div>
          <div class="task-strip-list">
            <div v-for="item in monitorTasks.slice(0, 4)" :key="item.monitorTaskId || item.taskName" class="task-strip-row">
              <div class="task-strip-main">
                <span>{{ item.taskName }}</span>
                <small>{{ item.monitorSubject || '未设置主体' }}</small>
              </div>
              <span class="task-strip-meta">{{ taskKeywordsLabel(item) }}</span>
              <span class="task-strip-meta">{{ taskAiAnalysisLabel(item) }}</span>
              <span class="task-strip-meta">{{ taskScheduleLabel(item) }}</span>
              <span class="task-strip-meta task-match-count">近次命中：{{ item.lastMatchCount ?? 0 }}</span>
              <el-tag :type="taskStatusTagType(item.taskStatus)" effect="plain">{{ taskStatusLabel(item.taskStatus) }}</el-tag>
            </div>
            <el-empty v-if="!monitorTasks.length && !loading" description="暂无运行任务" />
          </div>
        </article>
      </section>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import type { Component, Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import * as echarts from 'echarts';
import type { ECharts, EChartsOption } from 'echarts';
import {
  BellRing,
  Gauge,
  Maximize2,
  Minimize2,
  RadioTower,
  RefreshCw,
  ScanSearch,
  Siren,
  Target
} from 'lucide-vue-next';
import WordCloud from '../components/WordCloud.vue';
import ChartToolbar from '../components/ChartToolbar.vue';
import PlatformBadge from '../components/PlatformBadge.vue';
import {
  alertSourceLabel,
  formatTime,
  getDistributionValue,
  riskColors,
  riskLabel,
  riskOrder,
  riskTagType,
  sentimentLabel,
  sourceLabel,
  statusLabel,
  sumValues,
  taskAiAnalysisLabel,
  taskKeywordsLabel,
  taskScheduleLabel,
  taskStatusLabel,
  taskStatusTagType,
  toNumber,
  truncateText,
  useCampusSituationDashboard
} from '../composables/useCampusSituationDashboard';
import type { DistributionItem, SourceRiskDistributionItem } from '../types/api';

type ChartKey =
  | 'trend'
  | 'monitorTrend'
  | 'riskGauge'
  | 'risk'
  | 'eventStatus'
  | 'alertRisk'
  | 'sourceRisk'
  | 'eventHeat'
  | 'sentiment'
  | 'source';

interface MetricCard {
  label: string;
  value: string | number;
  icon: Component;
  tone: string;
}

const router = useRouter();
const route = useRoute();
const pageRef = ref<HTMLElement | null>(null);
const trendChartRef = ref<HTMLElement | null>(null);
const monitorTrendChartRef = ref<HTMLElement | null>(null);
const riskGaugeRef = ref<HTMLElement | null>(null);
const riskChartRef = ref<HTMLElement | null>(null);
const eventStatusChartRef = ref<HTMLElement | null>(null);
const alertRiskChartRef = ref<HTMLElement | null>(null);
const sourceRiskChartRef = ref<HTMLElement | null>(null);
const eventHeatChartRef = ref<HTMLElement | null>(null);
const sentimentChartRef = ref<HTMLElement | null>(null);
const sourceChartRef = ref<HTMLElement | null>(null);

const chartRefs: Record<ChartKey, Ref<HTMLElement | null>> = {
  trend: trendChartRef,
  monitorTrend: monitorTrendChartRef,
  riskGauge: riskGaugeRef,
  risk: riskChartRef,
  eventStatus: eventStatusChartRef,
  alertRisk: alertRiskChartRef,
  sourceRisk: sourceRiskChartRef,
  eventHeat: eventHeatChartRef,
  sentiment: sentimentChartRef,
  source: sourceChartRef
};
const charts: Partial<Record<ChartKey, ECharts>> = {};

const {
  activeEvents,
  alertRiskRows,
  dashboardTrendData,
  detectionRiskRows,
  errorMessage,
  eventRows,
  loadDashboard,
  loading,
  monitorAlerts,
  monitorNegativeRate,
  monitorOverview,
  monitorResults,
  monitorResultTotal,
  monitorRunFacts,
  monitorTasks,
  monitorTrendRows,
  now,
  nowText,
  pendingAlerts,
  pendingClues,
  pendingHits,
  riskRows,
  riskScore,
  riskScoreLevel,
  riskScoreTag,
  sentimentRows,
  sourceRiskRows,
  sourceRows,
  statistics,
  trendRows,
  wordCloudData
} = useCampusSituationDashboard();

let refreshTimer: number | undefined;
let clockTimer: number | undefined;
let syncingFullscreen = false;

const isScreenMode = computed(() => route.path === '/situation' || route.query.mode === 'screen');
const riskGaugeData = computed(() => ({
  score: riskScore.value,
  level: riskScoreLevel.value,
  overview: statistics.value.overview
}));
const metricCards = computed<MetricCard[]>(() => [
  { label: '启用任务', value: monitorOverview.value.activeTaskCount ?? 0, icon: RadioTower, tone: 'blue' },
  { label: '自动扫描', value: monitorOverview.value.scheduledTaskCount ?? 0, icon: ScanSearch, tone: 'cyan' },
  { label: '今日监测命中', value: monitorOverview.value.todayResultCount ?? 0, icon: Target, tone: 'green' },
  { label: '今日负面告警', value: monitorOverview.value.todayAlertCount ?? 0, icon: BellRing, tone: 'orange' },
  { label: '待处理告警', value: monitorOverview.value.pendingAlertCount ?? 0, icon: Siren, tone: 'red' },
  {
    label: '风险压力',
    value: isScreenMode.value ? riskScore.value : `${monitorNegativeRate.value}%`,
    icon: Gauge,
    tone: riskScore.value >= 75 ? 'red' : riskScore.value >= 45 ? 'orange' : 'green'
  }
]);

onMounted(async () => {
  await loadAll();
  refreshTimer = window.setInterval(loadAll, 60000);
  clockTimer = window.setInterval(() => {
    now.value = new Date();
  }, 1000);
  document.addEventListener('fullscreenchange', handleFullscreenChange);
  window.addEventListener('resize', resizeCharts);
  document.body.classList.toggle('campus-dashboard-screen-mode', isScreenMode.value);
});

onBeforeUnmount(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
  }
  if (clockTimer) {
    window.clearInterval(clockTimer);
  }
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
  window.removeEventListener('resize', resizeCharts);
  document.body.classList.remove('campus-dashboard-screen-mode');
  Object.values(charts).forEach((chart) => chart?.dispose());
});

watch(isScreenMode, async (mode) => {
  document.body.classList.toggle('campus-dashboard-screen-mode', mode);
  await nextTick();
  renderCharts();
  setTimeout(resizeCharts, 80);
});

watch(
  [
    monitorTrendRows,
    trendRows,
    riskRows,
    eventRows,
    alertRiskRows,
    detectionRiskRows,
    sourceRiskRows,
    activeEvents,
    sentimentRows,
    sourceRows
  ],
  async () => {
    await nextTick();
    renderCharts();
  },
  { deep: true }
);

async function loadAll() {
  await loadDashboard();
  await nextTick();
  renderCharts();
}

async function enterScreenMode() {
  if (route.path !== '/situation' && route.query.mode !== 'screen') {
    await router.push({ path: '/', query: { ...route.query, mode: 'screen' } });
  }
  await nextTick();
  const target = pageRef.value;
  if (target?.requestFullscreen && document.fullscreenElement !== target) {
    target.requestFullscreen().catch(() => undefined);
  }
}

async function exitScreenMode() {
  syncingFullscreen = true;
  if (document.fullscreenElement) {
    await document.exitFullscreen().catch(() => undefined);
  }
  await routeToNormalMode();
  syncingFullscreen = false;
}

function handleFullscreenChange() {
  if (syncingFullscreen || document.fullscreenElement || !isScreenMode.value) {
    return;
  }
  routeToNormalMode();
}

async function routeToNormalMode() {
  if (route.path === '/situation') {
    await router.replace('/');
    return;
  }
  if (route.query.mode === 'screen') {
    const query = { ...route.query };
    delete query.mode;
    await router.replace({ path: '/', query });
  }
}

function renderCharts() {
  renderMonitorTrendChart();
  renderTrendChart();
  renderRiskGauge();
  renderRiskChart();
  renderEventStatusChart();
  renderAlertRiskChart();
  renderSourceRiskChart();
  renderEventHeatChart();
  renderSentimentChart();
  renderSourceChart();
}

function renderMonitorTrendChart() {
  const rows = monitorTrendRows.value;
  const hasData = rows.some((item) => toNumber(item.monitorResultCount) + toNumber(item.monitorAlertCount) > 0);
  setChartOption('monitorTrend', {
    color: ['#2563eb', '#dc2626'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 8, textStyle: { color: chartTextColor.value } },
    grid: { left: 12, right: 18, top: 42, bottom: 12, containLabel: true },
    xAxis: buildCategoryAxis(rows.map((item) => item.name)),
    yAxis: buildValueAxis(),
    series: [
      buildLineSeries('监测命中', rows.map((item) => toNumber(item.monitorResultCount))),
      buildLineSeries('监测告警', rows.map((item) => toNumber(item.monitorAlertCount)))
    ],
    graphic: emptyGraphic(hasData)
  });
}

function renderTrendChart() {
  const rows = trendRows.value;
  const hasData = rows.some((item) => toNumber(item.clueCount) + toNumber(item.alertCount) + toNumber(item.hitCount) + toNumber(item.eventCount) > 0);
  setChartOption('trend', {
    color: ['#2563eb', '#d97706', '#0f766e', '#dc2626'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 8, textStyle: { color: chartTextColor.value } },
    grid: { left: 12, right: 18, top: 42, bottom: 12, containLabel: true },
    xAxis: buildCategoryAxis(rows.map((item) => item.name)),
    yAxis: buildValueAxis(),
    series: [
      buildLineSeries('线索', rows.map((item) => toNumber(item.clueCount))),
      buildLineSeries('预警', rows.map((item) => toNumber(item.alertCount))),
      buildLineSeries('检测命中', rows.map((item) => toNumber(item.hitCount))),
      buildLineSeries('事件', rows.map((item) => toNumber(item.eventCount)))
    ],
    graphic: emptyGraphic(hasData)
  });
}

function renderRiskGauge() {
  const score = riskScore.value;
  const color = score >= 75 ? '#dc2626' : score >= 45 ? '#d97706' : '#0f766e';
  setChartOption('riskGauge', {
    series: [
      {
        type: 'gauge',
        min: 0,
        max: 100,
        radius: '92%',
        center: ['50%', '56%'],
        startAngle: 210,
        endAngle: -30,
        pointer: { show: false },
        progress: { show: true, roundCap: true, width: 16, itemStyle: { color } },
        axisLine: { roundCap: true, lineStyle: { width: 16, color: [[1, isScreenMode.value ? '#24415f' : '#e5edf4']] } },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        title: { show: !isScreenMode.value, offsetCenter: [0, '42%'], color: chartMutedColor.value, fontSize: 13 },
        detail: { valueAnimation: true, offsetCenter: [0, '4%'], formatter: '{value}', color: chartTitleColor.value, fontSize: 34, fontWeight: 700 },
        data: [{ value: score, name: '风险压力指数' }]
      }
    ]
  });
}

function renderRiskChart() {
  const data = normalizeRiskRows(riskRows.value);
  setChartOption('risk', {
    color: data.map((item) => riskColors[item.rawName] || '#64748b'),
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value } },
    series: [
      {
        name: '事件风险',
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['50%', '44%'],
        minAngle: 8,
        avoidLabelOverlap: true,
        label: { formatter: '{b} {c}', color: chartTitleColor.value },
        data
      }
    ],
    graphic: emptyGraphic(sumValues(riskRows.value) > 0)
  });
}

function renderEventStatusChart() {
  const rows = eventRows.value.map((item) => ({ ...item, name: statusLabel(item.name), value: toNumber(item.value) }));
  setChartOption('eventStatus', {
    color: ['#0f766e'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 10, right: 18, top: 8, bottom: 10, containLabel: true },
    xAxis: buildValueAxis(),
    yAxis: {
      type: 'category',
      data: rows.map((item) => item.name),
      axisLine: { lineStyle: { color: chartAxisColor.value } },
      axisLabel: { color: chartTitleColor.value }
    },
    series: [{ name: '事件数', type: 'bar', barWidth: 14, itemStyle: { borderRadius: [0, 6, 6, 0] }, data: rows.map((item) => item.value) }],
    graphic: emptyGraphic(sumValues(eventRows.value) > 0)
  });
}

function renderAlertRiskChart() {
  const maxValue = Math.max(1, ...riskOrder.map((risk) => getDistributionValue(alertRiskRows.value, risk)), ...riskOrder.map((risk) => getDistributionValue(detectionRiskRows.value, risk)));
  const hasData = sumValues(alertRiskRows.value) + sumValues(detectionRiskRows.value) > 0;
  setChartOption('alertRisk', {
    color: ['#d97706', '#2563eb'],
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value } },
    radar: {
      radius: '62%',
      center: ['50%', '43%'],
      indicator: riskOrder.map((risk) => ({ name: riskLabel(risk), max: Math.max(maxValue, 4) })),
      splitLine: { lineStyle: { color: chartGridColor.value } },
      splitArea: { areaStyle: { color: isScreenMode.value ? ['#10243a', '#0d1d30'] : ['#ffffff', '#f8fafc'] } },
      axisName: { color: chartTitleColor.value }
    },
    series: [
      {
        type: 'radar',
        data: [
          { name: '预警', value: riskOrder.map((risk) => getDistributionValue(alertRiskRows.value, risk)), areaStyle: { opacity: 0.16 } },
          { name: '检测命中', value: riskOrder.map((risk) => getDistributionValue(detectionRiskRows.value, risk)), areaStyle: { opacity: 0.12 } }
        ]
      }
    ],
    graphic: emptyGraphic(hasData)
  });
}

function renderSourceRiskChart() {
  const rows = sourceRiskRows.value;
  setChartOption('sourceRisk', {
    color: [riskColors.normal, riskColors.concern, riskColors.major, riskColors.urgent],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 0, right: 8, textStyle: { color: chartTextColor.value } },
    grid: { left: 12, right: 18, top: 42, bottom: 10, containLabel: true },
    xAxis: buildValueAxis(),
    yAxis: {
      type: 'category',
      data: rows.map((item) => sourceLabel(item.name)),
      axisLine: { lineStyle: { color: chartAxisColor.value } },
      axisLabel: { color: chartTitleColor.value }
    },
    series: [
      buildStackBarSeries('一般', rows, 'normalCount'),
      buildStackBarSeries('关注', rows, 'concernCount'),
      buildStackBarSeries('较大', rows, 'majorCount'),
      buildStackBarSeries('紧急', rows, 'urgentCount')
    ],
    graphic: emptyGraphic(rows.some((item) => toNumber(item.totalCount) > 0))
  });
}

function renderEventHeatChart() {
  const rows = activeEvents.value.slice(0, 6).map((item) => ({
    name: item.eventTitle,
    value: toNumber(item.currentHeat),
    riskLevel: item.riskLevel || 'normal'
  }));
  setChartOption('eventHeat', {
    color: ['#2563eb'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 8, right: 18, top: 8, bottom: 10, containLabel: true },
    xAxis: buildValueAxis(),
    yAxis: {
      type: 'category',
      data: rows.map((item) => truncateText(item.name, 12)),
      axisLine: { lineStyle: { color: chartAxisColor.value } },
      axisLabel: { color: chartTitleColor.value }
    },
    series: [
      {
        name: '热度',
        type: 'bar',
        barWidth: 14,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: (params: { dataIndex: number }) => riskColors[rows[params.dataIndex]?.riskLevel] || '#2563eb'
        },
        data: rows.map((item) => item.value || 1)
      }
    ],
    graphic: emptyGraphic(rows.length > 0)
  });
}

function renderSentimentChart() {
  const rows = sentimentRows.value.map((item) => ({ name: sentimentLabel(item.name), value: toNumber(item.value) }));
  setChartOption('sentiment', {
    color: ['#10B981', '#F59E0B', '#EF4444', '#6B7280'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value, fontSize: 11 } },
    series: [
      {
        type: 'pie',
        radius: ['48%', '76%'],
        center: ['50%', '46%'],
        minAngle: 8,
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 4, borderColor: isScreenMode.value ? '#0b1726' : '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%', fontSize: 11, color: chartTextColor.value },
        data: rows
      }
    ],
    graphic: emptyGraphic(rows.length > 0)
  });
}

function renderSourceChart() {
  const rows = sourceRows.value;
  setChartOption('source', {
    color: ['#2563eb'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 12, right: 18, top: 12, bottom: 24, containLabel: true },
    xAxis: buildCategoryAxis(rows.map((item) => sourceLabel(item.name))),
    yAxis: buildValueAxis(),
    series: [
      {
        type: 'bar',
        barWidth: 24,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2563eb' },
            { offset: 1, color: '#60a5fa' }
          ])
        },
        data: rows.map((item) => toNumber(item.value))
      }
    ],
    graphic: emptyGraphic(rows.length > 0)
  });
}

function setChartOption(key: ChartKey, option: EChartsOption) {
  const chart = ensureChart(key);
  if (!chart) {
    return;
  }
  chart.clear();
  chart.setOption(option, true);
}

function ensureChart(key: ChartKey) {
  const element = chartRefs[key].value;
  if (!element) {
    return undefined;
  }
  const chart = charts[key];
  if (chart && chart.getDom() !== element) {
    chart.dispose();
    charts[key] = undefined;
  }
  if (!charts[key]) {
    charts[key] = echarts.init(element);
  }
  return charts[key];
}

function resizeCharts() {
  Object.values(charts).forEach((chart) => chart?.resize());
}

function onWordClick(word: { name: string }) {
  router.push({ path: '/search', query: { q: word.name } });
}

const chartTextColor = computed(() => (isScreenMode.value ? '#b9c8d9' : '#64748b'));
const chartTitleColor = computed(() => (isScreenMode.value ? '#e5eefb' : '#334155'));
const chartMutedColor = computed(() => (isScreenMode.value ? '#94a3b8' : '#64748b'));
const chartAxisColor = computed(() => (isScreenMode.value ? '#2f4d6c' : '#d8e0ea'));
const chartGridColor = computed(() => (isScreenMode.value ? '#223c57' : '#edf2f7'));

function buildCategoryAxis(data: string[]) {
  return {
    type: 'category' as const,
    boundaryGap: false,
    data,
    axisLine: { lineStyle: { color: chartAxisColor.value } },
    axisLabel: { color: chartTextColor.value, fontSize: 11 }
  };
}

function buildValueAxis() {
  return {
    type: 'value' as const,
    minInterval: 1,
    splitLine: { lineStyle: { color: chartGridColor.value } },
    axisLabel: { color: chartTextColor.value, fontSize: 11 }
  };
}

function buildLineSeries(name: string, data: number[]) {
  return {
    name,
    type: 'line' as const,
    smooth: true,
    symbol: 'circle',
    symbolSize: 7,
    lineStyle: { width: 3 },
    areaStyle: { opacity: 0.08 },
    data
  };
}

function buildStackBarSeries(label: string, rows: SourceRiskDistributionItem[], key: keyof SourceRiskDistributionItem) {
  return {
    name: label,
    type: 'bar' as const,
    stack: 'sourceRisk',
    barWidth: 16,
    itemStyle: { borderRadius: 4 },
    data: rows.map((item) => toNumber(item[key]))
  };
}

function normalizeRiskRows(rows: DistributionItem[]) {
  return rows.map((item) => ({
    name: riskLabel(item.name),
    rawName: item.name || 'unknown',
    value: toNumber(item.value)
  }));
}

function emptyGraphic(hasData: boolean) {
  if (hasData) {
    return undefined;
  }
  return {
    type: 'text',
    left: 'center',
    top: 'middle',
    style: {
      text: '暂无数据',
      fill: chartMutedColor.value,
      fontSize: 14
    }
  };
}
</script>

<style scoped>
.dashboard-unified {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-hero {
  min-height: 86px;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  background: linear-gradient(135deg, #f3f7ff 0%, #ffffff 50%, #eefbf6 100%);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}

.dashboard-hero span {
  color: var(--color-primary);
  font-size: 14px;
  line-height: 20px;
}

.dashboard-hero h2 {
  margin: 4px 0 0;
  color: #0f172a;
  font-size: 24px;
  line-height: 32px;
}

.dashboard-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.dashboard-actions .lucide {
  margin-right: 6px;
}

.dashboard-top-grid {
  grid-template-columns: minmax(360px, 0.95fr) minmax(0, 1.45fr);
}

.dashboard-monitor-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.dashboard-clue-table {
  width: 100%;
}

.task-strip-panel {
  overflow: hidden;
}

.task-strip-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-strip-row {
  min-height: 42px;
  padding: 8px 10px;
  display: grid;
  grid-template-columns: minmax(160px, 1fr) minmax(220px, 1.5fr) minmax(90px, 0.55fr) minmax(92px, 0.55fr) minmax(92px, 0.5fr) auto;
  align-items: center;
  gap: 10px;
  background: #f8fafc;
  border: 1px solid #e5ebf2;
  border-radius: 8px;
}

.task-strip-main,
.task-strip-meta {
  min-width: 0;
}

.task-strip-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.task-strip-main span,
.task-strip-main small,
.task-strip-meta {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-strip-main span {
  color: #0f172a;
  font-size: 13px;
  line-height: 20px;
}

.task-strip-main small,
.task-strip-meta {
  color: var(--color-muted);
  font-size: 12px;
  line-height: 18px;
}

.sentiment-tag {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
}

.sentiment-positive {
  color: #059669;
  background: #ecfdf5;
}

.sentiment-neutral {
  color: #d97706;
  background: #fffbeb;
}

.sentiment-negative {
  color: #dc2626;
  background: #fef2f2;
}

.sentiment-none,
.sentiment-unknown {
  color: #6b7280;
  background: #f3f4f6;
}

.screen-time {
  color: #dbeafe;
  font-size: 15px;
  line-height: 24px;
  white-space: nowrap;
}

.dashboard-unified.is-screen-mode {
  width: 100%;
  height: 100vh;
  min-height: 720px;
  padding: 14px;
  overflow: hidden;
  gap: 12px;
  background: #07111f;
}

.is-screen-mode .dashboard-hero {
  min-height: 70px;
  padding: 12px 16px;
  background: #0b1726;
  border-color: #1f3a56;
  border-radius: 8px;
}

.is-screen-mode .dashboard-hero span {
  color: #7dd3fc;
}

.is-screen-mode .dashboard-hero h2 {
  color: #e5eefb;
  font-size: 22px;
  line-height: 28px;
}

.is-screen-mode .dashboard-metrics {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.is-screen-mode .screen-card {
  min-height: 78px;
  padding: 10px 12px;
  background: #0b1726;
  border-color: #1f3a56;
  border-radius: 8px;
}

.is-screen-mode .screen-card-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
}

.is-screen-mode .screen-card span {
  color: #9db2c7;
  font-size: 12px;
}

.is-screen-mode .screen-card strong {
  color: #f8fafc;
  font-size: 24px;
  line-height: 28px;
}

.dashboard-screen-stage {
  min-height: 0;
  flex: 1;
  display: grid;
  grid-template-rows: minmax(0, 1fr) 178px 108px;
  gap: 12px;
}

.dashboard-screen-main {
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(260px, 0.95fr) minmax(280px, 1fr);
  grid-template-rows: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
}

.dashboard-screen-bottom {
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.screen-cell-wide {
  grid-row: span 2;
}

.is-screen-mode .screen-panel {
  min-height: 0;
  padding: 12px;
  overflow: hidden;
  background: #0b1726;
  border-color: #1f3a56;
  border-radius: 8px;
}

.is-screen-mode .panel-header {
  min-height: 26px;
  margin-bottom: 8px;
}

.is-screen-mode .panel-header h2 {
  color: #e5eefb;
  font-size: 14px;
  line-height: 20px;
}

.is-screen-mode .screen-chart {
  height: calc(100% - 34px);
  min-height: 150px;
}

.is-screen-mode .screen-chart-lg {
  height: calc(100% - 34px);
}

.is-screen-mode .screen-chart-sm {
  height: calc(100% - 34px);
  min-height: 120px;
}

.is-screen-mode .screen-gauge {
  height: calc(100% - 34px);
  min-height: 90px;
}

.is-screen-mode .screen-list {
  gap: 8px;
}

.compact-list {
  max-height: calc(100% - 38px);
  overflow: hidden;
}

.is-screen-mode .screen-list-row,
.is-screen-mode .monitor-run-facts > div,
.is-screen-mode .score-facts > div {
  background: #10243a;
  border-color: #1f3a56;
  border-radius: 8px;
}

.is-screen-mode .screen-list-row span,
.is-screen-mode .screen-list-main span,
.is-screen-mode .score-facts strong,
.is-screen-mode .monitor-run-facts strong {
  color: #f8fafc;
}

.is-screen-mode .screen-list-main small,
.is-screen-mode .score-facts span,
.is-screen-mode .monitor-run-facts span,
.is-screen-mode .monitor-run-facts small {
  color: #9db2c7;
}

.is-screen-mode .score-facts {
  display: none;
}

.is-screen-mode .score-facts > div {
  padding: 8px;
}

.is-screen-mode .score-facts strong {
  font-size: 20px;
  line-height: 24px;
}

.is-screen-mode .monitor-run-facts {
  height: calc(100% - 38px);
  gap: 6px;
}

.is-screen-mode .monitor-run-facts > div {
  min-height: 0;
  padding: 6px 10px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  grid-template-rows: auto auto;
  align-content: center;
  align-items: center;
  column-gap: 8px;
}

.is-screen-mode .monitor-run-facts strong {
  grid-column: 2;
  grid-row: 1 / span 2;
  margin: 0;
  font-size: 22px;
  line-height: 24px;
}

.is-screen-mode .monitor-run-facts span,
.is-screen-mode .monitor-run-facts small {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.is-screen-mode .monitor-run-facts span {
  font-size: 11px;
  line-height: 14px;
}

.is-screen-mode .monitor-run-facts small {
  margin-top: 0;
  font-size: 11px;
  line-height: 14px;
}

.is-screen-mode .task-strip-panel {
  min-height: 0;
  padding: 10px 12px;
}

.is-screen-mode .task-strip-list {
  max-height: calc(100% - 34px);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px 8px;
  overflow: hidden;
}

.is-screen-mode .task-strip-row {
  min-height: 30px;
  padding: 5px 8px;
  grid-template-columns: minmax(112px, 0.9fr) minmax(160px, 1.35fr) minmax(82px, 0.5fr) minmax(88px, 0.5fr) auto;
  gap: 8px;
  background: #10243a;
  border-color: #1f3a56;
  border-radius: 8px;
}

.is-screen-mode .task-strip-main span,
.is-screen-mode .task-strip-main small,
.is-screen-mode .task-strip-meta {
  color: #9db2c7;
  font-size: 11px;
  line-height: 14px;
}

.is-screen-mode .task-strip-main span {
  color: #f8fafc;
}

.is-screen-mode .task-match-count {
  display: none;
}

@media (max-width: 1180px) {
  .dashboard-top-grid,
  .dashboard-monitor-grid,
  .dashboard-screen-main,
  .dashboard-screen-bottom {
    grid-template-columns: 1fr;
  }

  .dashboard-unified.is-screen-mode {
    min-height: 100vh;
    overflow: auto;
  }

  .dashboard-screen-stage {
    grid-template-rows: auto;
  }

  .task-strip-row,
  .is-screen-mode .task-strip-row {
    grid-template-columns: minmax(160px, 1fr) minmax(220px, 1.5fr) auto;
  }

  .task-strip-meta:nth-of-type(3),
  .task-match-count {
    display: none;
  }

  .is-screen-mode .task-strip-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .dashboard-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
