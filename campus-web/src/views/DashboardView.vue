<template>
  <section ref="pageRef" class="dashboard-unified" :class="{ 'is-screen-mode': isScreenMode }">
    <header v-if="!isScreenMode" class="dashboard-hero">
      <div>
        <span>校园舆情监测</span>
        <h2>舆情态势工作台</h2>
      </div>
      <div class="dashboard-actions">
        <el-button type="primary" plain :loading="loading" @click="loadAll">
          <RefreshCw :size="16" />
          刷新
        </el-button>
        <el-button type="primary" @click="enterScreenMode">
          <Maximize2 :size="16" />
          大屏模式
        </el-button>
      </div>
    </header>

    <header v-else class="cockpit-header">
      <div class="cockpit-header-side">
        <span>{{ productName }} · 校园版</span>
        <strong>{{ productSubtitle }}</strong>
      </div>
      <div class="cockpit-title-block">
        <span>监测 · 研判 · 预警 · 处置态势</span>
        <h1>校园舆情智能驾驶舱</h1>
      </div>
      <div class="cockpit-header-side cockpit-header-right">
        <strong class="screen-time">{{ nowText }}</strong>
        <span>{{ lastRefreshText }}</span>
        <div class="cockpit-actions">
          <el-button type="primary" plain :loading="loading" @click="loadAll">
            <RefreshCw :size="16" />
            刷新
          </el-button>
          <el-button type="primary" plain @click="exitScreenMode">
          <Minimize2 :size="16" />
          退出大屏
        </el-button>
        </div>
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

    <section v-if="!isScreenMode" class="screen-metrics dashboard-metrics">
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

    <section v-else class="cockpit-metrics">
      <article v-for="card in screenMetricCards" :key="card.label" class="cockpit-metric-card" :class="`tone-${card.tone}`">
        <div class="screen-card-icon">
          <component :is="card.icon" :size="22" />
        </div>
        <div>
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
          <small>{{ card.note }}</small>
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
      <section class="dashboard-screen-stage cockpit-stage">
        <div class="cockpit-grid">
          <article class="screen-panel cockpit-panel cockpit-source-panel">
            <div class="panel-header">
              <h2>媒体来源排行</h2>
              <el-tag effect="plain" type="info">Top {{ Math.min(monitorSourceRows.length, 8) }}</el-tag>
            </div>
            <div ref="sourceChartRef" class="screen-chart cockpit-chart" />
          </article>

          <article class="screen-panel cockpit-panel cockpit-trend-panel">
            <div class="panel-header">
              <h2>近 7 日监测态势</h2>
              <el-tag effect="plain" type="warning">全部 / 风险 / 预警</el-tag>
            </div>
            <div ref="monitorTrendChartRef" class="screen-chart cockpit-main-chart" />
          </article>

          <article class="screen-panel cockpit-panel cockpit-sentiment-panel">
            <div class="panel-header">
              <h2>情感分布</h2>
              <el-tag effect="plain" :type="monitorNegativeRate >= 30 ? 'danger' : monitorNegativeRate >= 15 ? 'warning' : 'success'">
                负面 {{ monitorNegativeRate }}%
              </el-tag>
            </div>
            <div class="sentiment-cockpit-body">
              <div ref="sentimentChartRef" class="screen-chart sentiment-donut" />
              <div class="sentiment-fact-grid">
                <div v-for="item in screenSentimentFacts" :key="item.key" :class="['sentiment-fact', `tone-${item.key}`]">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                  <small>{{ item.percent }}%</small>
                </div>
              </div>
            </div>
          </article>

          <article class="screen-panel cockpit-panel cockpit-source-risk-panel">
            <div class="panel-header">
              <h2>来源风险构成</h2>
            </div>
            <div ref="sourceRiskChartRef" class="screen-chart cockpit-chart" />
          </article>

          <article class="screen-panel cockpit-panel cockpit-feed-panel">
            <div class="panel-header">
              <div class="panel-title-line">
                <h2>最新风险命中</h2>
                <el-tag v-if="monitorResultTotal" effect="plain" type="info">共 {{ monitorResultTotal }}</el-tag>
              </div>
            </div>
            <div class="screen-list cockpit-feed-list">
              <div v-for="item in monitorResults.slice(0, 10)" :key="item.monitorResultId || item.title" class="screen-list-row screen-feed-row">
                <div class="screen-list-main">
                  <span>{{ item.title || '未命名内容' }}</span>
                  <small>
                    {{ sourceLabel(item.platform || item.sourcePlatform) }} ·
                    {{ item.aiSummary ? `AI摘要：${item.aiSummary}` : (item.matchedNegativeWords || item.matchedKeywords || '监测命中') }}
                  </small>
                </div>
                <div class="cockpit-row-side">
                  <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
                  <small>{{ formatTime(item.collectTime || item.publishTime) }}</small>
                </div>
              </div>
              <el-empty v-if="!monitorResults.length && !loading" description="暂无风险命中" />
            </div>
          </article>

          <article class="screen-panel cockpit-panel cockpit-alert-panel">
            <div class="panel-header">
              <h2>待处理负面告警</h2>
            </div>
            <div class="screen-list cockpit-side-list">
              <div v-for="item in monitorAlerts.slice(0, 8)" :key="item.alertId" class="screen-list-row">
                <div class="screen-list-main">
                  <span>{{ item.alertTitle }}</span>
                  <small>{{ item.matchedKeywords || alertSourceLabel(item.alertSource) }} · {{ formatTime(item.createTime) }}</small>
                </div>
                <el-tag :type="riskTagType(item.riskLevel)" effect="plain">{{ riskLabel(item.riskLevel) }}</el-tag>
              </div>
              <el-empty v-if="!monitorAlerts.length && !loading" description="暂无待处理告警" />
            </div>
          </article>

          <article class="screen-panel cockpit-panel cockpit-topic-panel">
            <div class="panel-header">
              <h2>主题风险分布</h2>
            </div>
            <div ref="topicRiskChartRef" class="screen-chart cockpit-chart" />
          </article>

          <article class="screen-panel cockpit-panel cockpit-event-panel">
            <div class="panel-header">
              <h2>处置中事件热度</h2>
            </div>
            <div ref="eventHeatChartRef" class="screen-chart cockpit-chart" />
          </article>
        </div>

        <article class="screen-panel cockpit-task-strip task-strip-panel">
          <div class="panel-header">
            <h2>运行中的监测任务</h2>
            <el-tag effect="plain" type="success">自动巡航</el-tag>
          </div>
          <div class="task-strip-list">
            <div v-for="item in monitorTasks.slice(0, 8)" :key="item.monitorTaskId || item.taskName" class="task-strip-row">
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
  normalizeSentimentKey,
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
import { PRODUCT_NAME, PRODUCT_SUBTITLE } from '../config/brand';

type ChartKey =
  | 'trend'
  | 'monitorTrend'
  | 'riskGauge'
  | 'risk'
  | 'eventStatus'
  | 'alertRisk'
  | 'sourceRisk'
  | 'eventHeat'
  | 'topicRisk'
  | 'sentiment'
  | 'source';

interface MetricCard {
  label: string;
  value: string | number;
  icon: Component;
  tone: string;
  note?: string;
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
const topicRiskChartRef = ref<HTMLElement | null>(null);
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
  topicRisk: topicRiskChartRef,
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
  monitorSourceRows,
  monitorSentimentRows,
  monitorTopicRiskRows,
  monitorTrendAllRows,
  monitorTrendRiskRows,
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
const productName = PRODUCT_NAME;
const productSubtitle = PRODUCT_SUBTITLE;
const lastRefreshAt = ref(new Date());

const isScreenMode = computed(() => route.path === '/situation' || route.query.mode === 'screen');
const lastRefreshText = computed(() => `最近刷新 ${lastRefreshAt.value.toLocaleTimeString('zh-CN', { hour12: false })}`);
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
const screenMetricCards = computed<MetricCard[]>(() => [
  {
    label: '今日全部命中',
    value: monitorOverview.value.todayAllResultCount ?? monitorOverview.value.todayResultCount ?? 0,
    icon: Target,
    tone: 'blue',
    note: '监测任务真实命中'
  },
  {
    label: '风险命中',
    value: monitorOverview.value.todayRiskResultCount ?? monitorOverview.value.todayResultCount ?? 0,
    icon: Siren,
    tone: 'red',
    note: '负面词/风险等级/已预警'
  },
  {
    label: '负面占比',
    value: `${monitorNegativeRate.value}%`,
    icon: Gauge,
    tone: monitorNegativeRate.value >= 30 ? 'red' : monitorNegativeRate.value >= 15 ? 'orange' : 'green',
    note: '按监测情感统计'
  },
  {
    label: '待处理预警',
    value: monitorOverview.value.pendingAlertCount ?? statistics.value.overview.pendingAlertCount ?? 0,
    icon: BellRing,
    tone: 'orange',
    note: '预警中心待处理'
  },
  {
    label: '处置中事件',
    value: statistics.value.overview.activeEventCount ?? 0,
    icon: RadioTower,
    tone: 'cyan',
    note: '未归档事件'
  },
  {
    label: '运行任务',
    value: monitorOverview.value.activeTaskCount ?? 0,
    icon: ScanSearch,
    tone: 'green',
    note: `${monitorOverview.value.scheduledTaskCount ?? 0} 个自动调度`
  }
]);
const screenSentimentFacts = computed(() => {
  const order = [
    { key: 'positive', label: '正面' },
    { key: 'neutral', label: '中性' },
    { key: 'negative', label: '负面' },
    { key: 'none', label: '未识别' }
  ];
  const total = Math.max(1, sumValues(monitorSentimentRows.value));
  return order.map((item) => {
    const value = monitorSentimentRows.value
      .filter((row) => normalizeSentimentKey(row.name) === item.key)
      .reduce((sum, row) => sum + toNumber(row.value), 0);
    return {
      ...item,
      value,
      percent: Math.round((value / total) * 100)
    };
  });
});

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
  await loadAll();
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
    monitorSourceRows,
    monitorSentimentRows,
    monitorTopicRiskRows,
    monitorTrendAllRows,
    monitorTrendRiskRows,
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
  await loadDashboard(isScreenMode.value ? 'screen' : 'normal');
  lastRefreshAt.value = new Date();
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
  renderTopicRiskChart();
  renderSentimentChart();
  renderSourceChart();
}

function renderMonitorTrendChart() {
  if (isScreenMode.value) {
    const names = Array.from(new Set([
      ...monitorTrendAllRows.value.map((item) => item.name),
      ...monitorTrendRiskRows.value.map((item) => item.name)
    ]));
    const allValues = names.map((name) => getMonitorTrendValue(monitorTrendAllRows.value, name, 'monitorResultCount'));
    const riskValues = names.map((name) => getMonitorTrendValue(monitorTrendRiskRows.value, name, 'monitorResultCount'));
    const alertValues = names.map((name) => getMonitorTrendValue(monitorTrendAllRows.value, name, 'monitorAlertCount'));
    const hasData = [...allValues, ...riskValues, ...alertValues].some((value) => value > 0);
    setChartOption('monitorTrend', {
      color: ['#38bdf8', '#fb7185', '#f59e0b'],
      tooltip: { trigger: 'axis' },
      legend: { top: 0, right: 8, textStyle: { color: chartTextColor.value, fontSize: 12 } },
      grid: { left: 18, right: 24, top: 44, bottom: 18, containLabel: true },
      xAxis: buildCategoryAxis(names),
      yAxis: buildValueAxis(),
      series: [
        buildLineSeries('全部命中', allValues),
        buildLineSeries('风险命中', riskValues),
        buildLineSeries('监测预警', alertValues)
      ],
      graphic: emptyGraphic(hasData)
    });
    return;
  }
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

function renderTopicRiskChart() {
  const rows = monitorTopicRiskRows.value.slice(0, 6);
  setChartOption('topicRisk', {
    color: [riskColors.normal, riskColors.concern, riskColors.major, riskColors.urgent],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 0, right: 8, textStyle: { color: chartTextColor.value, fontSize: 11 } },
    grid: { left: 10, right: 18, top: 36, bottom: 8, containLabel: true },
    xAxis: buildValueAxis(),
    yAxis: {
      type: 'category',
      data: rows.map((item) => topicLabel(item.name)),
      axisLine: { lineStyle: { color: chartAxisColor.value } },
      axisLabel: { color: chartTitleColor.value, fontSize: 11 }
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

function renderSentimentChart() {
  const source = isScreenMode.value ? monitorSentimentRows.value : sentimentRows.value;
  const rows = normalizeSentimentRows(source);
  setChartOption('sentiment', {
    color: ['#10B981', '#F59E0B', '#EF4444', '#6B7280'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: chartTextColor.value, fontSize: isScreenMode.value ? 12 : 11 } },
    series: [
      {
        type: 'pie',
        radius: isScreenMode.value ? ['52%', '76%'] : ['48%', '76%'],
        center: isScreenMode.value ? ['50%', '45%'] : ['50%', '46%'],
        minAngle: 8,
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 4, borderColor: isScreenMode.value ? '#0b1726' : '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%', fontSize: isScreenMode.value ? 12 : 11, color: chartTextColor.value },
        data: rows
      }
    ],
    graphic: emptyGraphic(rows.length > 0)
  });
}

function renderSourceChart() {
  if (isScreenMode.value) {
    const rows = monitorSourceRows.value.slice(0, 8).reverse();
    const total = Math.max(1, sumValues(monitorSourceRows.value));
    setChartOption('source', {
      color: ['#38bdf8'],
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: unknown) => {
          const item = Array.isArray(params) ? params[0] as { name?: string; value?: number } : undefined;
          const value = toNumber(item?.value);
          return `${item?.name || '来源'}<br/>数量：${value}<br/>占比：${Math.round((value / total) * 100)}%`;
        }
      },
      grid: { left: 76, right: 46, top: 12, bottom: 10, containLabel: false },
      xAxis: buildValueAxis(),
      yAxis: {
        type: 'category',
        data: rows.map((item) => truncateText(sourceLabel(item.name), 8)),
        axisLine: { lineStyle: { color: chartAxisColor.value } },
        axisLabel: { color: chartTitleColor.value, fontSize: 12 }
      },
      series: [
        {
          type: 'bar',
          barWidth: 14,
          itemStyle: {
            borderRadius: [0, 7, 7, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#0ea5e9' },
              { offset: 1, color: '#67e8f9' }
            ])
          },
          label: {
            show: true,
            position: 'right',
            color: chartTitleColor.value,
            fontSize: 11,
            formatter: (params: { value?: unknown }) => {
              const value = toNumber(params.value);
              return `${value} / ${Math.round((value / total) * 100)}%`;
            }
          },
          data: rows.map((item) => toNumber(item.value))
        }
      ],
      graphic: emptyGraphic(rows.length > 0)
    });
    return;
  }
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

function getMonitorTrendValue(
  rows: Array<{ name: string; monitorResultCount?: number; monitorAlertCount?: number }>,
  name: string,
  key: 'monitorResultCount' | 'monitorAlertCount'
) {
  const row = rows.find((item) => item.name === name);
  return toNumber(row?.[key]);
}

function normalizeRiskRows(rows: DistributionItem[]) {
  return rows.map((item) => ({
    name: riskLabel(item.name),
    rawName: item.name || 'unknown',
    value: toNumber(item.value)
  }));
}

function normalizeSentimentRows(rows: DistributionItem[]) {
  const order = ['positive', 'neutral', 'negative', 'none'];
  return order
    .map((key) => ({
      name: sentimentLabel(key),
      value: rows
        .filter((item) => normalizeSentimentKey(item.name) === key)
        .reduce((sum, item) => sum + toNumber(item.value), 0)
    }))
    .filter((item) => item.value > 0);
}

function topicLabel(value?: string) {
  const text = (value || '').trim();
  const labels: Record<string, string> = {
    safety: '安全稳定',
    stability: '安全稳定',
    teacher_ethics: '师德师风',
    teaching: '教学管理',
    food: '食品宿舍',
    logistics: '后勤服务',
    student_rights: '学生权益',
    employment: '招生就业',
    public_opinion: '综合舆情',
    other: '未分类',
    unknown: '未分类'
  };
  return labels[text] || labels[text.toLowerCase()] || text || '未分类';
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

.cockpit-header {
  min-height: 84px;
  padding: 12px 18px;
  display: grid;
  grid-template-columns: minmax(220px, 0.9fr) minmax(420px, 1.4fr) minmax(280px, 1fr);
  align-items: center;
  gap: 16px;
  background:
    linear-gradient(90deg, rgba(14, 165, 233, 0.18), rgba(7, 17, 31, 0.82) 28%, rgba(20, 184, 166, 0.14)),
    #0b1726;
  border: 1px solid #1f3a56;
  border-radius: 10px;
  box-shadow: 0 0 32px rgba(14, 165, 233, 0.12) inset;
}

.cockpit-header-side {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cockpit-header-side span {
  color: #7dd3fc;
  font-size: 12px;
  line-height: 16px;
}

.cockpit-header-side strong {
  color: #e5eefb;
  font-size: 14px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cockpit-title-block {
  min-width: 0;
  text-align: center;
}

.cockpit-title-block span {
  color: #93c5fd;
  font-size: 13px;
  line-height: 18px;
}

.cockpit-title-block h1 {
  margin: 2px 0 0;
  color: #f8fafc;
  font-size: 32px;
  line-height: 40px;
  font-weight: 800;
  text-shadow: 0 0 18px rgba(56, 189, 248, 0.28);
}

.cockpit-header-right {
  align-items: flex-end;
  text-align: right;
}

.cockpit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.cockpit-metrics {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.cockpit-metric-card {
  min-width: 0;
  min-height: 78px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(180deg, rgba(16, 36, 58, 0.96), rgba(11, 23, 38, 0.96));
  border: 1px solid #1f3a56;
  border-radius: 10px;
}

.cockpit-metric-card > div:last-child {
  min-width: 0;
}

.cockpit-metric-card span,
.cockpit-metric-card small {
  display: block;
  overflow: hidden;
  color: #9db2c7;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cockpit-metric-card span {
  font-size: 12px;
  line-height: 16px;
}

.cockpit-metric-card small {
  margin-top: 2px;
  font-size: 11px;
  line-height: 14px;
}

.cockpit-metric-card strong {
  display: block;
  margin-top: 2px;
  color: #f8fafc;
  font-size: 24px;
  line-height: 28px;
}

.cockpit-metric-card.tone-blue .screen-card-icon {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.14);
}

.cockpit-metric-card.tone-cyan .screen-card-icon {
  color: #22d3ee;
  background: rgba(34, 211, 238, 0.13);
}

.cockpit-metric-card.tone-green .screen-card-icon {
  color: #34d399;
  background: rgba(52, 211, 153, 0.13);
}

.cockpit-metric-card.tone-orange .screen-card-icon,
.cockpit-metric-card.tone-amber .screen-card-icon {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.14);
}

.cockpit-metric-card.tone-red .screen-card-icon {
  color: #fb7185;
  background: rgba(251, 113, 133, 0.14);
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

.cockpit-stage {
  grid-template-rows: minmax(0, 1fr) 112px;
}

.cockpit-grid {
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(270px, 0.92fr) minmax(560px, 1.65fr) minmax(300px, 1fr);
  grid-template-rows: minmax(0, 1fr) minmax(0, 1fr) minmax(0, 0.92fr);
  grid-template-areas:
    "source trend sentiment"
    "sourceRisk trend alerts"
    "topic feed events";
  gap: 12px;
}

.cockpit-source-panel {
  grid-area: source;
}

.cockpit-trend-panel {
  grid-area: trend;
}

.cockpit-sentiment-panel {
  grid-area: sentiment;
}

.cockpit-source-risk-panel {
  grid-area: sourceRisk;
}

.cockpit-feed-panel {
  grid-area: feed;
}

.cockpit-alert-panel {
  grid-area: alerts;
}

.cockpit-topic-panel {
  grid-area: topic;
}

.cockpit-event-panel {
  grid-area: events;
}

.cockpit-task-strip {
  min-height: 0;
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
  background:
    linear-gradient(180deg, rgba(12, 28, 46, 0.98), rgba(8, 20, 35, 0.98)),
    #0b1726;
  border-color: #1f3a56;
  border-radius: 10px;
  box-shadow: 0 0 20px rgba(8, 145, 178, 0.08) inset;
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

.is-screen-mode .cockpit-main-chart {
  height: calc(100% - 34px);
  min-height: 300px;
}

.is-screen-mode .cockpit-chart {
  height: calc(100% - 34px);
  min-height: 118px;
}

.sentiment-cockpit-body {
  height: calc(100% - 34px);
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 116px;
  gap: 8px;
}

.is-screen-mode .sentiment-donut {
  height: 100%;
  min-height: 128px;
}

.sentiment-fact-grid {
  min-width: 0;
  display: grid;
  grid-template-columns: 1fr;
  gap: 6px;
}

.sentiment-fact {
  min-width: 0;
  padding: 6px 8px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 4px 8px;
  background: rgba(16, 36, 58, 0.92);
  border: 1px solid #1f3a56;
  border-radius: 8px;
}

.sentiment-fact span,
.sentiment-fact small {
  color: #9db2c7;
  font-size: 11px;
  line-height: 14px;
}

.sentiment-fact strong {
  grid-row: 1 / span 2;
  grid-column: 2;
  color: #f8fafc;
  font-size: 18px;
  line-height: 22px;
}

.sentiment-fact.tone-positive {
  border-color: rgba(16, 185, 129, 0.38);
}

.sentiment-fact.tone-neutral {
  border-color: rgba(245, 158, 11, 0.38);
}

.sentiment-fact.tone-negative {
  border-color: rgba(239, 68, 68, 0.48);
}

.sentiment-fact.tone-none {
  border-color: rgba(148, 163, 184, 0.32);
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

.cockpit-feed-list,
.cockpit-side-list {
  max-height: calc(100% - 38px);
  overflow: hidden;
}

.cockpit-row-side {
  display: flex;
  flex: 0 0 auto;
  align-items: flex-end;
  flex-direction: column;
  gap: 4px;
}

.cockpit-row-side small {
  color: #7992ad;
  font-size: 10px;
  line-height: 12px;
  white-space: nowrap;
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

@media (min-width: 2200px) {
  .dashboard-unified.is-screen-mode {
    padding: 18px;
    gap: 14px;
  }

  .cockpit-header {
    min-height: 96px;
  }

  .cockpit-title-block h1 {
    font-size: 38px;
    line-height: 46px;
  }

  .cockpit-stage {
    grid-template-rows: minmax(0, 1fr) 128px;
  }

  .cockpit-grid {
    grid-template-columns: minmax(340px, 0.95fr) minmax(760px, 1.7fr) minmax(380px, 1fr);
  }

  .cockpit-metric-card strong {
    font-size: 30px;
    line-height: 34px;
  }

  .is-screen-mode .panel-header h2 {
    font-size: 16px;
  }

  .is-screen-mode .task-strip-list {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1180px) {
  .dashboard-top-grid,
  .dashboard-monitor-grid,
  .dashboard-screen-main,
  .dashboard-screen-bottom,
  .cockpit-grid {
    grid-template-columns: 1fr;
  }

  .cockpit-header {
    grid-template-columns: 1fr;
    text-align: left;
  }

  .cockpit-title-block,
  .cockpit-header-right {
    align-items: flex-start;
    text-align: left;
  }

  .cockpit-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-unified.is-screen-mode {
    min-height: 100vh;
    overflow: auto;
  }

  .dashboard-screen-stage {
    grid-template-rows: auto;
  }

  .cockpit-grid {
    grid-template-areas: none;
  }

  .cockpit-grid > .screen-panel {
    grid-area: auto;
    min-height: 260px;
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
