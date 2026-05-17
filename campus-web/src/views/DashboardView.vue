<template>
  <section class="screen-page dashboard-page">
    <div class="dashboard-header">
      <div>
        <span>校园舆情监测</span>
        <h2>舆情态势总览</h2>
      </div>
      <div class="dashboard-actions">
        <el-button type="primary" plain :loading="loading" @click="loadAll">
          <RefreshCw :size="16" />
          刷新
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="errorMessage"
      class="data-alert"
      :title="errorMessage"
      type="warning"
      show-icon
      :closable="false"
    />

    <section class="dashboard-summary">
      <span>今日线索 <strong>{{ statistics.overview.todayClueCount ?? 0 }}</strong></span>
      <span class="summary-sep">|</span>
      <span>待处理预警 <strong>{{ statistics.overview.pendingAlertCount ?? 0 }}</strong></span>
      <span class="summary-sep">|</span>
      <span>处置中事件 <strong>{{ statistics.overview.activeEventCount ?? 0 }}</strong></span>
      <span class="summary-sep">|</span>
      <span>高风险 <strong class="tone-red">{{ statistics.overview.highRiskEventCount ?? 0 }}</strong></span>
      <span class="summary-sep">|</span>
      <span>活跃监测 <strong>{{ statistics.monitorOverview?.activeTaskCount ?? 0 }}</strong></span>
    </section>

    <section class="screen-grid">
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

      <article class="screen-panel">
        <div class="panel-header">
          <h2>舆情趋势（近 7 天）</h2>
        </div>
        <div ref="trendChartRef" class="screen-chart" style="min-height: 320px;" />
      </article>
    </section>

    <section class="screen-panel">
      <div class="panel-header">
        <h2>最新舆情线索</h2>
        <el-button link type="primary" @click="router.push('/monitor')">进入监测信息</el-button>
      </div>
      <div ref="tableScrollRef" class="dashboard-table-wrap">
        <el-table
          :data="displayClueList"
          size="small"
          v-loading="loading"
          :show-header="true"
          stripe
          class="dashboard-clue-table"
        >
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="clueTitle" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sourcePlatform" label="来源" width="100">
            <template #default="{ row }">
              <span class="platform-label">{{ row.sourcePlatform || row.clueSource || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sentiment" label="情感" width="80">
            <template #default="{ row }">
              <span :class="['sentiment-tag', sentimentClass(row.sentiment)]">
                {{ sentimentLabel(row.sentiment) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="publishTime" label="时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.publishTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <section class="screen-grid">
      <article class="screen-panel">
        <div class="panel-header">
          <h2>情感分布</h2>
        </div>
        <div ref="sentimentChartRef" class="screen-chart screen-chart-sm" />
      </article>

      <article class="screen-panel">
        <div class="panel-header">
          <h2>媒体来源分布</h2>
        </div>
        <div ref="sourceChartRef" class="screen-chart screen-chart-sm" />
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import type { ECharts, EChartsOption } from 'echarts';
import {
  RefreshCw
} from 'lucide-vue-next';
import {
  fetchDashboardStatistics,
  fetchDashboardTrend,
  fetchPendingClues,
  fetchWordCloud
} from '../services/dashboard';
import WordCloud from '../components/WordCloud.vue';
import type {
  CampusClue,
  DashboardStatistics,
  DashboardTrendPoint,
  DistributionItem,
  WordCloudItem
} from '../types/api';

const router = useRouter();

const loading = ref(false);
const errorMessage = ref('');

const statistics = ref<DashboardStatistics>(emptyStatistics());
const wordCloudData = ref<WordCloudItem[]>([]);
const trendData = ref<DashboardTrendPoint[]>([]);
const clueList = ref<CampusClue[]>([]);

const trendChartRef = ref<HTMLElement | null>(null);
const sentimentChartRef = ref<HTMLElement | null>(null);
const sourceChartRef = ref<HTMLElement | null>(null);
const tableScrollRef = ref<HTMLElement | null>(null);

let trendChart: ECharts | null = null;
let sentimentChart: ECharts | null = null;
let sourceChart: ECharts | null = null;
let scrollTimer: number | undefined;
let refreshTimer: number | undefined;

const TONE_BLUE = '#3D5AFE';
const TONE_GREEN = '#10B981';
const TONE_ORANGE = '#F59E0B';
const TONE_RED = '#EF4444';
const TONE_PURPLE = '#8B5CF6';


const displayClueList = computed(() => clueList.value.slice(0, 15));

onMounted(() => {
  loadAll();
  refreshTimer = window.setInterval(loadAll, 120000);
  window.addEventListener('resize', resizeCharts);
});

onBeforeUnmount(() => {
  if (refreshTimer) window.clearInterval(refreshTimer);
  if (scrollTimer) window.clearInterval(scrollTimer);
  window.removeEventListener('resize', resizeCharts);
  trendChart?.dispose();
  sentimentChart?.dispose();
  sourceChart?.dispose();
});

async function loadAll() {
  loading.value = true;
  errorMessage.value = '';

  const [statsResult, wcResult, trendResult, cluesResult] = await Promise.allSettled([
    fetchDashboardStatistics(),
    fetchWordCloud(),
    fetchDashboardTrend(7),
    fetchPendingClues()
  ]);

  if (statsResult.status === 'fulfilled') {
    statistics.value = { ...emptyStatistics(), ...statsResult.value };
  }
  if (wcResult.status === 'fulfilled') {
    wordCloudData.value = wcResult.value || [];
  }
  if (trendResult.status === 'fulfilled') {
    trendData.value = trendResult.value || [];
  }
  if (cluesResult.status === 'fulfilled') {
    clueList.value = cluesResult.value.list || [];
  }

  const hasError = [statsResult, wcResult, trendResult, cluesResult].some(
    (r) => r.status === 'rejected'
  );
  if (hasError) {
    errorMessage.value = '部分数据暂时不可用，已使用占位数据展示';
  }

  if (!wordCloudData.value.length) {
    wordCloudData.value = DEFAULT_WORD_CLOUD;
  }
  if (!trendData.value.length) {
    trendData.value = DEFAULT_TREND;
  }
  if (!clueList.value.length) {
    clueList.value = [];
  }

  loading.value = false;
  await nextTick();
  renderCharts();
  startTableScroll();
}

function renderCharts() {
  renderTrendChart();
  renderSentimentChart();
  renderSourceChart();
}

function renderTrendChart() {
  if (!trendChartRef.value) return;
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value);
  }

  const dates = trendData.value.map((d) => d.date);
  const clues = trendData.value.map((d) => d.clueCount);
  const alerts = trendData.value.map((d) => d.alertCount);

  const option: EChartsOption = {
    color: [TONE_BLUE, TONE_ORANGE],
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      right: 8,
      textStyle: { color: '#64748b', fontSize: 12 }
    },
    grid: { left: 12, right: 18, top: 36, bottom: 12, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: '#d8e0ea' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    series: [
      {
        name: '线索',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 3 },
        areaStyle: { opacity: 0.08 },
        data: clues
      },
      {
        name: '预警',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 3 },
        areaStyle: { opacity: 0.08 },
        data: alerts
      }
    ]
  };
  trendChart.setOption(option, true);
}

function renderSentimentChart() {
  if (!sentimentChartRef.value) return;
  if (!sentimentChart) {
    sentimentChart = echarts.init(sentimentChartRef.value);
  }

  const distribution = getSentimentDistribution();
  const data = distribution.map((d) => ({ name: sentimentLabel(d.name), value: d.value }));

  const option: EChartsOption = {
    color: ['#10B981', '#F59E0B', '#EF4444', '#6B7280'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#64748b', fontSize: 11 }
    },
    series: [
      {
        type: 'pie',
        radius: ['48%', '76%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{d}%',
          fontSize: 11,
          color: '#64748b'
        },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data
      }
    ]
  };
  sentimentChart.setOption(option, true);
}

function renderSourceChart() {
  if (!sourceChartRef.value) return;
  if (!sourceChart) {
    sourceChart = echarts.init(sourceChartRef.value);
  }

  const sources = getSourceDistribution();
  const names = sources.map((s) => sourceDisplayName(s.name));
  const values = sources.map((s) => s.value);

  const option: EChartsOption = {
    color: [TONE_BLUE],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: { left: 12, right: 18, top: 12, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#64748b', fontSize: 11, rotate: names.length > 6 ? 30 : 0 },
      axisLine: { lineStyle: { color: '#d8e0ea' } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    series: [
      {
        type: 'bar',
        barWidth: 24,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: TONE_BLUE },
            { offset: 1, color: '#818CF8' }
          ])
        },
        data: values
      }
    ]
  };
  sourceChart.setOption(option, true);
}

function getSentimentDistribution(): DistributionItem[] {
  if (!clueList.value.length) return DEFAULT_SENTIMENT;
  const counts: Record<string, number> = {};
  for (const clue of clueList.value) {
    const key = clue.sentiment || '未知';
    counts[key] = (counts[key] || 0) + 1;
  }
  return Object.entries(counts).map(([name, value]) => ({ name, value }));
}

function getSourceDistribution(): DistributionItem[] {
  const dist = statistics.value.clueSourceDistribution || [];
  if (dist.length) return dist;
  return DEFAULT_SOURCES;
}

function resizeCharts() {
  trendChart?.resize();
  sentimentChart?.resize();
  sourceChart?.resize();
}

function startTableScroll() {
  if (scrollTimer) window.clearInterval(scrollTimer);
  if (displayClueList.value.length <= 5) return;
  scrollTimer = window.setInterval(() => {
    const wrap = tableScrollRef.value?.querySelector('.el-table__body-wrapper');
    if (!wrap) return;
    const maxScroll = wrap.scrollHeight - wrap.clientHeight;
    if (wrap.scrollTop >= maxScroll - 4) {
      wrap.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      wrap.scrollBy({ top: 44, behavior: 'smooth' });
    }
  }, 3000);
}

function onWordClick(word: WordCloudItem) {
  router.push({ path: '/search', query: { q: word.name } });
}

function sentimentLabel(value?: string): string {
  const labels: Record<string, string> = {
    positive: '正面',
    neutral: '中性',
    negative: '负面',
    unknown: '未知'
  };
  return labels[value || 'unknown'] || value || '未知';
}

function sentimentClass(value?: string): string {
  if (value === 'positive') return 'sentiment-positive';
  if (value === 'neutral') return 'sentiment-neutral';
  if (value === 'negative') return 'sentiment-negative';
  return 'sentiment-unknown';
}

function sourceDisplayName(name: string): string {
  return name.length > 6 ? name.slice(0, 5) + '...' : name;
}

function formatTime(value?: string | Date): string {
  if (!value) return '-';
  try {
    const d = new Date(value);
    if (isNaN(d.getTime())) return '-';
    return d.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return '-';
  }
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
    sourceRiskDistribution: []
  };
}

const DEFAULT_WORD_CLOUD: WordCloudItem[] = [
  { name: '校园安全', value: 120 },
  { name: '食堂卫生', value: 95 },
  { name: '宿舍管理', value: 88 },
  { name: '教学质量', value: 76 },
  { name: '就业指导', value: 68 },
  { name: '心理健康', value: 62 },
  { name: '奖学金', value: 55 },
  { name: '校园活动', value: 50 },
  { name: '考研', value: 46 },
  { name: '课程安排', value: 42 },
  { name: '实验室', value: 38 },
  { name: '招生政策', value: 34 },
  { name: '社团', value: 30 },
  { name: '疫情防控', value: 28 },
  { name: '交通出行', value: 24 },
  { name: '图书馆', value: 22 },
  { name: '校庆', value: 20 },
  { name: '学术科研', value: 18 },
  { name: '实习机会', value: 16 },
  { name: '校园网络', value: 14 }
];

const DEFAULT_TREND: DashboardTrendPoint[] = [
  { date: '05-06', clueCount: 112, alertCount: 5 },
  { date: '05-07', clueCount: 98, alertCount: 3 },
  { date: '05-08', clueCount: 135, alertCount: 7 },
  { date: '05-09', clueCount: 120, alertCount: 4 },
  { date: '05-10', clueCount: 108, alertCount: 6 },
  { date: '05-11', clueCount: 145, alertCount: 8 },
  { date: '05-12', clueCount: 128, alertCount: 5 }
];

const DEFAULT_SENTIMENT: DistributionItem[] = [
  { name: 'positive', value: 45 },
  { name: 'neutral', value: 30 },
  { name: 'negative', value: 20 },
  { name: 'unknown', value: 5 }
];

const DEFAULT_SOURCES: DistributionItem[] = [
  { name: '微博', value: 35 },
  { name: '微信', value: 28 },
  { name: '抖音', value: 22 },
  { name: '知乎', value: 18 },
  { name: '贴吧', value: 15 },
  { name: '小红书', value: 12 },
  { name: '头条', value: 10 }
];
</script>

<style scoped>
.dashboard-page {
  gap: 18px;
}

.dashboard-header {
  min-height: 80px;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  background: linear-gradient(135deg, #EEF1FF 0%, #ffffff 50%, #F5F3FF 100%);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}

.dashboard-header span {
  color: #3D5AFE;
  font-size: 14px;
  line-height: 20px;
}

.dashboard-header h2 {
  margin: 4px 0 0;
  color: #0f172a;
  font-size: 24px;
  line-height: 32px;
}

.dashboard-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dashboard-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 20px;
  background: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  font-size: 13px;
  color: var(--color-muted);
  flex-wrap: wrap;
}

.dashboard-summary strong {
  color: #0f172a;
  font-weight: 700;
  margin-left: 4px;
}

.dashboard-summary .tone-red {
  color: #EF4444;
}

.summary-sep {
  color: #e2e8f0;
}

.dashboard-table-wrap {
  max-height: 260px;
  overflow: hidden;
}

.dashboard-table-wrap .el-table__body-wrapper {
  overflow-y: auto;
}

.dashboard-clue-table {
  width: 100%;
}

.platform-label {
  color: #64748b;
  font-size: 12px;
}

.sentiment-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.sentiment-positive {
  color: #059669;
  background: #ECFDF5;
}

.sentiment-neutral {
  color: #D97706;
  background: #FFFBEB;
}

.sentiment-negative {
  color: #DC2626;
  background: #FEF2F2;
}

.sentiment-unknown {
  color: #6B7280;
  background: #F3F4F6;
}

@media (max-width: 768px) {
  .dashboard-summary {
    gap: 8px;
    font-size: 12px;
  }
}
</style>
