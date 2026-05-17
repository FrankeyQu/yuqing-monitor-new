<template>
  <section class="screen-page situation-screen">
    <header class="screen-header">
      <div>
        <span>校园舆情监测态势</span>
        <h2>任务运行、关键词命中、负面告警闭环</h2>
      </div>
      <div class="screen-clock">
        <strong>{{ nowText }}</strong>
        <el-button type="primary" plain :loading="loading" @click="loadScreen">
          <RefreshCw :size="16" />
          刷新
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

    <section class="screen-metrics">
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

    <section class="screen-grid screen-monitor-board">
      <article class="screen-panel screen-panel-large">
        <div class="panel-header">
          <h2>监测近 7 日趋势</h2>
          <ChartToolbar
            :chart-ref="monitorTrendChartRef"
            :chart-data="monitorTrendData"
            title="监测近 7 日趋势"
            @refresh="loadMonitorTrend"
          />
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
    </section>

    <section class="screen-grid screen-grid-three">
      <article class="screen-panel">
        <div class="panel-header">
          <h2>运行中的监测任务</h2>
          <el-button link type="primary" @click="$router.push('/monitor')">进入</el-button>
        </div>
        <div class="screen-list">
          <div v-for="item in monitorTasks" :key="item.monitorTaskId || item.taskName" class="screen-list-row screen-feed-row">
            <div class="screen-list-main">
              <span>{{ item.taskName }}</span>
              <small>{{ item.monitorSubject }} · {{ item.keywords || item.negativeWords || '未配置关键词' }}</small>
            </div>
            <div class="screen-list-tags">
              <el-tag :type="taskStatusTagType(item.taskStatus)" effect="plain">{{ taskStatusLabel(item.taskStatus) }}</el-tag>
              <el-tag effect="plain">{{ frequencyLabel(item.scanFrequencyMinutes) }}</el-tag>
            </div>
          </div>
          <el-empty v-if="!monitorTasks.length && !loading" description="暂无运行任务" />
        </div>
      </article>

      <article class="screen-panel">
        <div class="panel-header">
          <div class="panel-title-line">
            <h2>最新监测命中</h2>
            <el-tag v-if="monitorResultTotal" effect="plain" type="info">共 {{ monitorResultTotal }}</el-tag>
          </div>
          <el-button link type="primary" @click="$router.push({ path: '/monitor', query: { hitScope: 'all' } })">查看全部</el-button>
        </div>
        <div class="screen-list">
          <div v-for="item in monitorResults" :key="item.monitorResultId || item.title" class="screen-list-row screen-feed-row">
            <div class="screen-list-main">
              <span>{{ item.title || '未命名内容' }}</span>
              <small>{{ item.matchedSubjects || item.platform || '监测命中' }} · {{ item.matchedKeywords || item.matchedNegativeWords || '-' }}</small>
            </div>
            <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
          </div>
          <el-empty v-if="!monitorResults.length && !loading" description="暂无监测命中" />
        </div>
      </article>

      <article class="screen-panel">
        <div class="panel-header">
          <h2>待处理负面告警</h2>
          <el-button link type="primary" @click="$router.push('/monitor')">进入</el-button>
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
            :chart-data="trendData"
            title="近 7 日舆情流入趋势"
            @refresh="loadTrend"
          />
          <el-tag effect="plain">线索 / 预警 / 命中 / 事件</el-tag>
        </div>
        <div ref="trendChartRef" class="screen-chart screen-chart-lg" />
      </article>

      <article class="screen-panel score-panel">
        <div class="panel-header">
          <h2>风险压力指数</h2>
          <ChartToolbar
            :chart-ref="riskGaugeRef"
            :chart-data="riskData"
            title="风险压力指数"
            @refresh="loadRisk"
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

    <section class="screen-grid screen-grid-three">
      <article class="screen-panel">
        <div class="panel-header">
          <h2>事件风险分布</h2>
          <ChartToolbar
            :chart-ref="riskChartRef"
            :chart-data="riskData"
            title="事件风险分布"
            @refresh="loadRisk"
          />
          <el-tag effect="plain">事件库</el-tag>
        </div>
        <div ref="riskChartRef" class="screen-chart screen-chart-sm" />
      </article>

      <article class="screen-panel">
        <div class="panel-header">
          <h2>事件处置状态</h2>
          <ChartToolbar
            :chart-ref="eventStatusChartRef"
            :chart-data="eventStatusData"
            title="事件处置状态"
            @refresh="loadEventStatus"
          />
          <el-tag effect="plain" type="success">流转</el-tag>
        </div>
        <div ref="eventStatusChartRef" class="screen-chart screen-chart-sm" />
      </article>

      <article class="screen-panel">
        <div class="panel-header">
          <h2>预警与检测风险</h2>
          <ChartToolbar
            :chart-ref="alertRiskChartRef"
            :chart-data="alertRiskData"
            title="预警与检测风险"
            @refresh="loadAlertRisk"
          />
          <el-tag effect="plain" type="warning">对比</el-tag>
        </div>
        <div ref="alertRiskChartRef" class="screen-chart screen-chart-sm" />
      </article>
    </section>

    <section class="screen-grid">
      <article class="screen-panel screen-panel-wide">
        <div class="panel-header">
          <h2>来源风险构成</h2>
          <ChartToolbar
            :chart-ref="sourceRiskChartRef"
            :chart-data="sourceRiskData"
            title="来源风险构成"
            @refresh="loadSourceRisk"
          />
          <el-tag effect="plain" type="info">线索来源</el-tag>
        </div>
        <div ref="sourceRiskChartRef" class="screen-chart" />
      </article>

      <article class="screen-panel">
        <div class="panel-header">
          <h2>处置中事件热度</h2>
          <ChartToolbar
            :chart-ref="eventHeatChartRef"
            :chart-data="eventHeatData"
            title="处置中事件热度"
            @refresh="loadEventHeat"
          />
          <el-button link type="primary" @click="$router.push('/events')">进入</el-button>
        </div>
        <div ref="eventHeatChartRef" class="screen-chart" />
      </article>
    </section>

    <section class="screen-grid screen-grid-three">
      <article class="screen-panel">
        <div class="panel-header">
          <h2>待处理预警</h2>
          <el-button link type="primary" @click="$router.push('/alerts')">进入</el-button>
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
          <el-button link type="primary" @click="$router.push('/detection')">进入</el-button>
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
          <el-button link type="primary" @click="$router.push('/events')">进入</el-button>
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
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import type { Ref } from 'vue';
import * as echarts from 'echarts';
import type { ECharts, EChartsOption } from 'echarts';
import {
  Activity,
  Bell,
  ClipboardList,
  ClockAlert,
  RadioTower,
  RefreshCw,
  ShieldAlert
} from 'lucide-vue-next';
import {
  fetchActiveEvents,
  fetchDashboardStatistics,
  fetchPendingAlerts,
  fetchPendingDetectionHits
} from '../services/dashboard';
import { listMonitorAlerts, listMonitorInformation, listMonitorTasks } from '../services/monitor';
import ChartToolbar from '../components/ChartToolbar.vue';
import { campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type {
  CampusAlert,
  CampusDetectionHit,
  CampusEvent,
  DashboardStatistics,
  DistributionItem,
  MonitorDashboardOverview,
  CampusMonitorInformation,
  CampusMonitorTask,
  MonitorTrendItem,
  SourceRiskDistributionItem
} from '../types/api';

type ChartKey =
  | 'trend'
  | 'monitorTrend'
  | 'riskGauge'
  | 'risk'
  | 'eventStatus'
  | 'alertRisk'
  | 'sourceRisk'
  | 'eventHeat';

const chartPalette = ['#0f766e', '#2563eb', '#d97706', '#dc2626', '#7c3aed', '#0891b2'];
const riskColors: Record<string, string> = {
  normal: '#15803d',
  concern: '#d97706',
  major: '#ea580c',
  urgent: '#dc2626',
  unknown: '#64748b'
};
const riskOrder = ['normal', 'concern', 'major', 'urgent'];

const loading = ref(false);
const errorMessage = ref('');
const now = ref(new Date());
const statistics = ref<DashboardStatistics>(emptyStatistics());
const pendingAlerts = ref<CampusAlert[]>([]);
const pendingHits = ref<CampusDetectionHit[]>([]);
const activeEvents = ref<CampusEvent[]>([]);
const monitorTasks = ref<CampusMonitorTask[]>([]);
const monitorResults = ref<CampusMonitorInformation[]>([]);
const monitorResultTotal = ref(0);
const monitorAlerts = ref<CampusAlert[]>([]);
const trendChartRef = ref<HTMLElement | null>(null);
const monitorTrendChartRef = ref<HTMLElement | null>(null);
const riskGaugeRef = ref<HTMLElement | null>(null);
const riskChartRef = ref<HTMLElement | null>(null);
const eventStatusChartRef = ref<HTMLElement | null>(null);
const alertRiskChartRef = ref<HTMLElement | null>(null);
const sourceRiskChartRef = ref<HTMLElement | null>(null);
const eventHeatChartRef = ref<HTMLElement | null>(null);
const monitorTrendData = ref<any>([]);
const trendData = ref<any>([]);
const riskData = ref<any>({});
const eventStatusData = ref<any>([]);
const alertRiskData = ref<any>({});
const sourceRiskData = ref<any>([]);
const eventHeatData = ref<any>([]);
const chartRefs: Record<ChartKey, Ref<HTMLElement | null>> = {
  trend: trendChartRef,
  monitorTrend: monitorTrendChartRef,
  riskGauge: riskGaugeRef,
  risk: riskChartRef,
  eventStatus: eventStatusChartRef,
  alertRisk: alertRiskChartRef,
  sourceRisk: sourceRiskChartRef,
  eventHeat: eventHeatChartRef
};
const charts: Partial<Record<ChartKey, ECharts>> = {};
let timer: number | undefined;
let clockTimer: number | undefined;

const nowText = computed(() => now.value.toLocaleString('zh-CN', { hour12: false }));
const monitorOverview = computed<MonitorDashboardOverview>(() => statistics.value.monitorOverview || {});
const metricCards = computed(() => [
  { label: '启用任务', value: monitorOverview.value.activeTaskCount ?? 0, icon: RadioTower, tone: 'blue' },
  { label: '自动扫描', value: monitorOverview.value.scheduledTaskCount ?? 0, icon: ClockAlert, tone: 'cyan' },
  { label: '今日监测命中', value: monitorOverview.value.todayResultCount ?? 0, icon: ClipboardList, tone: 'green' },
  { label: '今日负面告警', value: monitorOverview.value.todayAlertCount ?? 0, icon: Bell, tone: 'orange' },
  { label: '待处理告警', value: monitorOverview.value.pendingAlertCount ?? 0, icon: ShieldAlert, tone: 'red' },
  {
    label: '负面率',
    value: `${monitorNegativeRate.value}%`,
    icon: Activity,
    tone: monitorNegativeRate.value >= 30 ? 'red' : monitorNegativeRate.value >= 15 ? 'orange' : 'green'
  }
]);
const monitorRunFacts = computed(() => {
  const activeTaskCount = toNumber(monitorOverview.value.activeTaskCount);
  const scheduledTaskCount = toNumber(monitorOverview.value.scheduledTaskCount);
  const todayResultCount = toNumber(monitorOverview.value.todayResultCount);
  const todayAlertCount = toNumber(monitorOverview.value.todayAlertCount);
  const pendingAlertCount = toNumber(monitorOverview.value.pendingAlertCount);
  return [
    {
      label: '自动覆盖',
      value: `${monitorScheduleRate.value}%`,
      note: `${scheduledTaskCount}/${activeTaskCount || 0}`
    },
    {
      label: '今日命中',
      value: String(todayResultCount),
      note: '近 24 小时监测结果'
    },
    {
      label: '今日负面',
      value: String(todayAlertCount),
      note: '已进入告警通道'
    },
    {
      label: '待处理',
      value: String(pendingAlertCount),
      note: '人工处置队列'
    }
  ];
});
const monitorNegativeRate = computed(() => {
  const resultCount = toNumber(monitorOverview.value.todayResultCount);
  const alertCount = toNumber(monitorOverview.value.todayAlertCount);
  if (!resultCount) {
    return 0;
  }
  return Math.min(100, Math.round((alertCount / resultCount) * 100));
});
const monitorScheduleRate = computed(() => {
  const activeTaskCount = toNumber(monitorOverview.value.activeTaskCount);
  const scheduledTaskCount = toNumber(monitorOverview.value.scheduledTaskCount);
  if (!activeTaskCount) {
    return 0;
  }
  return Math.min(100, Math.round((scheduledTaskCount / activeTaskCount) * 100));
});
const riskRows = computed(() => statistics.value.riskDistribution || []);
const eventRows = computed(() => statistics.value.eventStatusDistribution || []);
const trendRows = computed(() => statistics.value.trendByDay || []);
const monitorTrendRows = computed<MonitorTrendItem[]>(() => statistics.value.monitorTrendByDay || []);
const alertRiskRows = computed(() => statistics.value.alertRiskDistribution || []);
const detectionRiskRows = computed(() => statistics.value.detectionHitRiskDistribution || []);
const sourceRiskRows = computed(() => statistics.value.sourceRiskDistribution || []);
const riskScore = computed(() => {
  const overview = statistics.value.overview || {};
  const monitor = monitorOverview.value || {};
  const score =
    Number(overview.pendingAlertCount || 0) * 14 +
    Number(overview.highRiskEventCount || 0) * 22 +
    Number(overview.activeEventCount || 0) * 8 +
    Number(overview.overdueDisposalCount || 0) * 12 +
    Number(monitor.pendingAlertCount || 0) * 8 +
    Number(monitor.todayAlertCount || 0) * 6 +
    Number(monitor.todayResultCount || 0) * 2;
  return Math.min(100, score);
});
const riskScoreLevel = computed(() => {
  if (riskScore.value >= 75) {
    return '高压';
  }
  if (riskScore.value >= 45) {
    return '关注';
  }
  return '平稳';
});
const riskScoreTag = computed(() => {
  if (riskScore.value >= 75) {
    return 'danger';
  }
  if (riskScore.value >= 45) {
    return 'warning';
  }
  return 'success';
});

onMounted(() => {
  loadScreen();
  timer = window.setInterval(loadScreen, 60000);
  clockTimer = window.setInterval(() => {
    now.value = new Date();
  }, 1000);
  window.addEventListener('resize', resizeCharts);
});

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer);
  }
  if (clockTimer) {
    window.clearInterval(clockTimer);
  }
  window.removeEventListener('resize', resizeCharts);
  Object.values(charts).forEach((chart) => chart?.dispose());
});

async function loadScreen() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [stats, alerts, hits, events, monitorTaskPage, monitorResultPage, monitorAlertPage] = await Promise.allSettled([
      fetchDashboardStatistics(),
      fetchPendingAlerts(),
      fetchPendingDetectionHits(),
      fetchActiveEvents(),
      listMonitorTasks({ pageNum: 1, pageSize: 6, taskStatus: 'active' }),
      listMonitorInformation({ pageNum: 1, pageSize: 6, hitScope: 'risk' }),
      listMonitorAlerts({ pageNum: 1, pageSize: 5, alertStatus: 'pending' })
    ]);
    if (stats.status === 'fulfilled') {
      statistics.value = { ...emptyStatistics(), ...stats.value };
    }
    if (alerts.status === 'fulfilled') {
      pendingAlerts.value = alerts.value.list || [];
    }
    if (hits.status === 'fulfilled') {
      pendingHits.value = hits.value.list || [];
    }
    if (events.status === 'fulfilled') {
      activeEvents.value = events.value.list || [];
    }
    if (monitorTaskPage.status === 'fulfilled') {
      monitorTasks.value = monitorTaskPage.value.list || [];
    }
    if (monitorResultPage.status === 'fulfilled') {
      monitorResults.value = monitorResultPage.value.list || [];
      monitorResultTotal.value = monitorResultPage.value.total || 0;
    } else {
      monitorResults.value = [];
      monitorResultTotal.value = 0;
    }
    if (monitorAlertPage.status === 'fulfilled') {
      monitorAlerts.value = monitorAlertPage.value.list || [];
    }
    monitorTrendData.value = statistics.value.monitorTrendByDay || [];
    trendData.value = statistics.value.trendByDay || [];
    riskData.value = { score: riskScore.value, level: riskScoreLevel.value, ...statistics.value.overview, riskDistribution: statistics.value.riskDistribution };
    eventStatusData.value = statistics.value.eventStatusDistribution || [];
    alertRiskData.value = { alertRisk: statistics.value.alertRiskDistribution || [], detectionRisk: statistics.value.detectionHitRiskDistribution || [] };
    sourceRiskData.value = statistics.value.sourceRiskDistribution || [];
    eventHeatData.value = [...activeEvents.value];
    if ([stats, alerts, hits, events, monitorTaskPage, monitorResultPage, monitorAlertPage].some((item) => item.status === 'rejected')) {
      errorMessage.value = '部分态势数据暂时不可用';
    }
  } finally {
    now.value = new Date();
    loading.value = false;
    await nextTick();
    renderCharts();
  }
}

function renderCharts() {
  renderTrendChart();
  renderMonitorTrendChart();
  renderRiskGauge();
  renderRiskChart();
  renderEventStatusChart();
  renderAlertRiskChart();
  renderSourceRiskChart();
  renderEventHeatChart();
}

function renderTrendChart() {
  const rows = trendRows.value;
  const hasData = rows.some((item) => toNumber(item.clueCount) + toNumber(item.alertCount) + toNumber(item.hitCount) + toNumber(item.eventCount) > 0);
  setChartOption('trend', {
    color: ['#2563eb', '#d97706', '#0f766e', '#dc2626'],
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      right: 8,
      textStyle: { color: '#64748b' }
    },
    grid: { left: 12, right: 18, top: 42, bottom: 12, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: rows.map((item) => item.name),
      axisLine: { lineStyle: { color: '#d8e0ea' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b' }
    },
    series: [
      buildLineSeries('线索', rows.map((item) => toNumber(item.clueCount))),
      buildLineSeries('预警', rows.map((item) => toNumber(item.alertCount))),
      buildLineSeries('检测命中', rows.map((item) => toNumber(item.hitCount))),
      buildLineSeries('事件', rows.map((item) => toNumber(item.eventCount)))
    ],
    graphic: emptyGraphic(hasData)
  });
}

function renderMonitorTrendChart() {
  const rows = monitorTrendRows.value;
  const hasData = rows.some((item) => toNumber(item.monitorResultCount) + toNumber(item.monitorAlertCount) > 0);
  setChartOption('monitorTrend', {
    color: ['#2563eb', '#dc2626'],
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      right: 8,
      textStyle: { color: '#64748b' }
    },
    grid: { left: 12, right: 18, top: 42, bottom: 12, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: rows.map((item) => item.name),
      axisLine: { lineStyle: { color: '#d8e0ea' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b' }
    },
    series: [
      buildLineSeries('监测命中', rows.map((item) => toNumber(item.monitorResultCount))),
      buildLineSeries('监测告警', rows.map((item) => toNumber(item.monitorAlertCount)))
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
        progress: {
          show: true,
          roundCap: true,
          width: 16,
          itemStyle: { color }
        },
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 16,
            color: [[1, '#e5edf4']]
          }
        },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        title: {
          offsetCenter: [0, '42%'],
          color: '#64748b',
          fontSize: 13
        },
        detail: {
          valueAnimation: true,
          offsetCenter: [0, '4%'],
          formatter: '{value}',
          color: '#0f172a',
          fontSize: 34,
          fontWeight: 700
        },
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
    legend: {
      bottom: 0,
      textStyle: { color: '#64748b' }
    },
    series: [
      {
        name: '事件风险',
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['50%', '44%'],
        minAngle: 8,
        avoidLabelOverlap: true,
        label: {
          formatter: '{b} {c}',
          color: '#334155'
        },
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
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => item.name),
      axisLine: { lineStyle: { color: '#d8e0ea' } },
      axisLabel: { color: '#334155' }
    },
    series: [
      {
        name: '事件数',
        type: 'bar',
        barWidth: 14,
        itemStyle: { borderRadius: [0, 6, 6, 0] },
        data: rows.map((item) => item.value)
      }
    ],
    graphic: emptyGraphic(sumValues(eventRows.value) > 0)
  });
}

function renderAlertRiskChart() {
  const maxValue = Math.max(1, ...riskOrder.map((risk) => getDistributionValue(alertRiskRows.value, risk)), ...riskOrder.map((risk) => getDistributionValue(detectionRiskRows.value, risk)));
  const hasData = sumValues(alertRiskRows.value) + sumValues(detectionRiskRows.value) > 0;
  setChartOption('alertRisk', {
    color: ['#d97706', '#2563eb'],
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 0,
      textStyle: { color: '#64748b' }
    },
    radar: {
      radius: '62%',
      center: ['50%', '43%'],
      indicator: riskOrder.map((risk) => ({ name: riskLabel(risk), max: Math.max(maxValue, 4) })),
      splitLine: { lineStyle: { color: '#e5edf4' } },
      splitArea: { areaStyle: { color: ['#ffffff', '#f8fafc'] } },
      axisName: { color: '#334155' }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            name: '预警',
            value: riskOrder.map((risk) => getDistributionValue(alertRiskRows.value, risk)),
            areaStyle: { opacity: 0.16 }
          },
          {
            name: '检测命中',
            value: riskOrder.map((risk) => getDistributionValue(detectionRiskRows.value, risk)),
            areaStyle: { opacity: 0.12 }
          }
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
    legend: {
      top: 0,
      right: 8,
      textStyle: { color: '#64748b' }
    },
    grid: { left: 12, right: 18, top: 42, bottom: 10, containLabel: true },
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => sourceLabel(item.name)),
      axisLine: { lineStyle: { color: '#d8e0ea' } },
      axisLabel: { color: '#334155' }
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
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => truncateText(item.name, 12)),
      axisLine: { lineStyle: { color: '#d8e0ea' } },
      axisLabel: { color: '#334155' }
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
  if (!charts[key]) {
    charts[key] = echarts.init(element);
  }
  return charts[key];
}

function resizeCharts() {
  Object.values(charts).forEach((chart) => chart?.resize());
}

function loadMonitorTrend() { loadScreen(); }
function loadTrend() { loadScreen(); }
function loadRisk() { loadScreen(); }
function loadEventStatus() { loadScreen(); }
function loadAlertRisk() { loadScreen(); }
function loadSourceRisk() { loadScreen(); }
function loadEventHeat() { loadScreen(); }

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

function getDistributionValue(rows: DistributionItem[], risk: string) {
  return toNumber(rows.find((item) => item.name === risk)?.value);
}

function sumValues(rows: DistributionItem[]) {
  return rows.reduce((total, item) => total + toNumber(item.value), 0);
}

function toNumber(value: unknown) {
  const numberValue = Number(value || 0);
  return Number.isFinite(numberValue) ? numberValue : 0;
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
      fill: '#94a3b8',
      fontSize: 14
    }
  };
}

function emptyStatistics(): DashboardStatistics {
  return {
    overview: {},
    monitorOverview: {},
    riskDistribution: [],
    clueSourceDistribution: [],
    eventStatusDistribution: [],
    trendByDay: [],
    monitorTrendByDay: [],
    alertRiskDistribution: [],
    detectionHitRiskDistribution: [],
    sourceRiskDistribution: [],
    topicRiskDistribution: [],
    governanceMetrics: {}
  };
}

function truncateText(value: string, length: number) {
  if (!value || value.length <= length) {
    return value;
  }
  return `${value.slice(0, length)}...`;
}

function riskLabel(value?: string) {
  return campusRiskLabel(value, '未知');
}

function sourceLabel(value?: string) {
  return value && value !== 'unknown' ? value : '未知来源';
}

function alertSourceLabel(value?: string) {
  const labels: Record<string, string> = {
    monitor: '监测',
    detection: '检测',
    clue: '线索',
    event: '事件'
  };
  return labels[value || 'monitor'] || value || '监测';
}

function statusLabel(value?: string) {
  const labels: Record<string, string> = {
    pending: '待处理',
    pending_judge: '待研判',
    judged: '已研判',
    handling: '处置中',
    archived: '已归档',
    converted: '已转事件',
    unknown: '未知'
  };
  return labels[value || 'unknown'] || value || '未知';
}

function frequencyLabel(value?: number) {
  const labels: Record<number, string> = { 10: '10分钟', 30: '30分钟', 60: '1小时', 360: '6小时', 1440: '每天' };
  return value ? labels[value] || `${value}分钟` : '-';
}

function taskStatusLabel(value?: string) {
  const labels: Record<string, string> = { active: '启用', paused: '暂停', disabled: '禁用' };
  return labels[value || 'active'] || value || '启用';
}

function taskStatusTagType(value?: string) {
  if (value === 'active') {
    return 'success';
  }
  if (value === 'paused') {
    return 'warning';
  }
  return 'info';
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}
</script>
