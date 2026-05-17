<template>
  <section class="business-page">
    <section class="compare-header panel">
      <h2>对比分析</h2>
      <div class="compare-selectors">
        <el-select v-model="selfSubject" placeholder="选择本品主题" clearable filterable style="width: 240px">
          <el-option
            v-for="item in subjectOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <span class="vs-badge">VS</span>
        <el-select v-model="competitorSubject" placeholder="选择竞品主题" clearable filterable style="width: 240px">
          <el-option
            v-for="item in subjectOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" :loading="loading" @click="handleAnalyze">
          <Search :size="16" />
          分析
        </el-button>
      </div>
    </section>

    <!-- 四维对比雷达图 -->
    <section class="panel screen-panel-wide">
      <div class="panel-header">
        <h2>四维对比雷达图</h2>
      </div>
      <div ref="radarChartRef" class="screen-chart compare-chart-radar" />
    </section>

    <!-- 声量对比 + 情感分布对比 + 媒体分布对比 -->
    <section class="compare-grid">
      <section class="panel">
        <div class="panel-header">
          <h2>声量对比</h2>
        </div>
        <div ref="volumeChartRef" class="screen-chart" />
        <div class="compare-stats">
          <div class="stat-item stat-self">
            <span>本品</span>
            <strong>{{ selfVolumeTotal }}</strong>
          </div>
          <div class="stat-item stat-competitor">
            <span>竞品</span>
            <strong>{{ competitorVolumeTotal }}</strong>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>情感分布对比</h2>
        </div>
        <div ref="sentimentChartRef" class="screen-chart" />
        <div class="compare-sentiment-grid">
          <div class="sentiment-col">
            <span class="sentiment-label">本品</span>
            <div class="sentiment-bars">
              <div
                v-for="item in compareData?.selfSentiment || []"
                :key="item.name"
                class="sentiment-row"
              >
                <span class="sentiment-type" :class="sentimentClass(item.name)">{{ item.name }}</span>
                <span class="sentiment-value">{{ item.value }}</span>
              </div>
            </div>
          </div>
          <div class="sentiment-col">
            <span class="sentiment-label">竞品</span>
            <div class="sentiment-bars">
              <div
                v-for="item in compareData?.competitorSentiment || []"
                :key="item.name"
                class="sentiment-row"
              >
                <span class="sentiment-type" :class="sentimentClass(item.name)">{{ item.name }}</span>
                <span class="sentiment-value">{{ item.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>媒体分布对比</h2>
        </div>
        <div ref="mediaChartRef" class="screen-chart" />
        <div class="compare-media-grid">
          <div class="media-col">
            <span class="media-label">本品</span>
            <div
              v-for="item in compareData?.selfMediaDistribution || []"
              :key="item.name"
              class="media-tag"
            >
              <span>{{ item.name }}</span>
              <span class="media-val">{{ item.value }}</span>
            </div>
          </div>
          <div class="media-col">
            <span class="media-label">竞品</span>
            <div
              v-for="item in compareData?.competitorMediaDistribution || []"
              :key="item.name"
              class="media-tag"
            >
              <span>{{ item.name }}</span>
              <span class="media-val">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </section>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from 'lucide-vue-next';
import * as echarts from 'echarts';
import type { ECharts, EChartsOption } from 'echarts';
import { fetchCompareData } from '../services/compare';
import type { CompareData } from '../services/compare';

const chartPalette = ['#0f766e', '#2563eb', '#d97706', '#dc2626', '#7c3aed', '#0891b2'];

interface SubjectOption {
  value: string;
  label: string;
}

const subjectOptions: SubjectOption[] = [
  { value: 'apple', label: 'Apple iPhone 16' },
  { value: 'huawei', label: '华为 Mate 70' },
  { value: 'xiaomi', label: '小米 15 Pro' },
  { value: 'oppo', label: 'OPPO Find X8' },
  { value: 'vivo', label: 'vivo X200 Pro' },
  { value: 'samsung', label: 'Samsung Galaxy S25' }
];

const loading = ref(false);
const selfSubject = ref('');
const competitorSubject = ref('');
const compareData = ref<CompareData | null>(null);

const radarChartRef = ref<HTMLElement | null>(null);
const volumeChartRef = ref<HTMLElement | null>(null);
const sentimentChartRef = ref<HTMLElement | null>(null);
const mediaChartRef = ref<HTMLElement | null>(null);

let radarChart: ECharts | null = null;
let volumeChart: ECharts | null = null;
let sentimentChart: ECharts | null = null;
let mediaChart: ECharts | null = null;

const selfVolumeTotal = computed(() => {
  if (!compareData.value) return 0;
  return compareData.value.volumeTrend.reduce((sum, item) => sum + item.self, 0);
});

const competitorVolumeTotal = computed(() => {
  if (!compareData.value) return 0;
  return compareData.value.volumeTrend.reduce((sum, item) => sum + item.competitor, 0);
});

onMounted(() => {
  window.addEventListener('resize', resizeCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  disposeCharts();
});

async function handleAnalyze() {
  if (!selfSubject.value) {
    ElMessage.warning('请选择本品主题');
    return;
  }
  if (!competitorSubject.value) {
    ElMessage.warning('请选择竞品主题');
    return;
  }
  if (selfSubject.value === competitorSubject.value) {
    ElMessage.warning('本品和竞品不能相同');
    return;
  }
  loading.value = true;
  try {
    compareData.value = await fetchCompareData(selfSubject.value, competitorSubject.value);
    await nextTick();
    renderCharts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '对比分析加载失败');
  } finally {
    loading.value = false;
  }
}

function renderCharts() {
  renderRadarChart();
  renderVolumeChart();
  renderSentimentChart();
  renderMediaChart();
}

function disposeCharts() {
  radarChart?.dispose();
  volumeChart?.dispose();
  sentimentChart?.dispose();
  mediaChart?.dispose();
  radarChart = null;
  volumeChart = null;
  sentimentChart = null;
  mediaChart = null;
}

function resizeCharts() {
  radarChart?.resize();
  volumeChart?.resize();
  sentimentChart?.resize();
  mediaChart?.resize();
}

function initChart(ref: HTMLElement | null): ECharts | null {
  if (!ref) return null;
  const instance = echarts.init(ref);
  return instance;
}

function renderRadarChart() {
  if (!compareData.value) return;
  if (!radarChart) {
    radarChart = initChart(radarChartRef.value);
  }
  if (!radarChart) return;

  const data = compareData.value.radarData;
  const option: EChartsOption = {
    color: chartPalette,
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 0,
      right: 24,
      textStyle: { color: '#64748b' },
      data: ['本品', '竞品']
    },
    radar: {
      center: ['50%', '54%'],
      radius: '72%',
      indicator: data.map((item) => ({
        name: item.dimension,
        max: 100
      })),
      axisName: {
        color: '#334155',
        fontSize: 12
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(15, 118, 110, 0.02)', 'rgba(15, 118, 110, 0.06)']
        }
      },
      axisLine: {
        lineStyle: {
          color: '#d8e0ea'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#e5ebf2'
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            name: '本品',
            value: data.map((item) => item.self),
            areaStyle: {
              color: 'rgba(15, 118, 110, 0.2)'
            },
            lineStyle: {
              color: '#0f766e',
              width: 2
            },
            itemStyle: {
              color: '#0f766e'
            }
          },
          {
            name: '竞品',
            value: data.map((item) => item.competitor),
            areaStyle: {
              color: 'rgba(37, 99, 235, 0.2)'
            },
            lineStyle: {
              color: '#2563eb',
              width: 2
            },
            itemStyle: {
              color: '#2563eb'
            }
          }
        ]
      }
    ]
  };
  radarChart.setOption(option, true);
}

function renderVolumeChart() {
  if (!compareData.value) return;
  if (!volumeChart) {
    volumeChart = initChart(volumeChartRef.value);
  }
  if (!volumeChart) return;

  const trend = compareData.value.volumeTrend;
  const option: EChartsOption = {
    color: ['#0f766e', '#2563eb'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      top: 0,
      right: 8,
      textStyle: { color: '#64748b' }
    },
    grid: {
      left: 12,
      right: 18,
      top: 36,
      bottom: 12,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trend.map((item) => item.date),
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
      {
        name: '本品',
        type: 'bar',
        barWidth: 12,
        itemStyle: { borderRadius: [2, 2, 0, 0] },
        data: trend.map((item) => item.self)
      },
      {
        name: '竞品',
        type: 'bar',
        barWidth: 12,
        itemStyle: { borderRadius: [2, 2, 0, 0] },
        data: trend.map((item) => item.competitor)
      }
    ]
  };
  volumeChart.setOption(option, true);
}

function renderSentimentChart() {
  if (!compareData.value) return;
  if (!sentimentChart) {
    sentimentChart = initChart(sentimentChartRef.value);
  }
  if (!sentimentChart) return;

  const selfData = compareData.value.selfSentiment;
  const competitorData = compareData.value.competitorSentiment;

  const option: EChartsOption = {
    color: ['#15803d', '#d97706', '#dc2626'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#64748b' }
    },
    series: [
      {
        name: '本品',
        type: 'pie',
        radius: ['36%', '58%'],
        center: ['28%', '46%'],
        minAngle: 5,
        avoidLabelOverlap: true,
        label: {
          formatter: '{b}\n{d}%',
          color: '#334155',
          fontSize: 11
        },
        labelLine: {
          length: 8,
          length2: 6
        },
        data: selfData
      },
      {
        name: '竞品',
        type: 'pie',
        radius: ['36%', '58%'],
        center: ['72%', '46%'],
        minAngle: 5,
        avoidLabelOverlap: true,
        label: {
          formatter: '{b}\n{d}%',
          color: '#334155',
          fontSize: 11
        },
        labelLine: {
          length: 8,
          length2: 6
        },
        data: competitorData
      }
    ]
  };
  sentimentChart.setOption(option, true);
}

function renderMediaChart() {
  if (!compareData.value) return;
  if (!mediaChart) {
    mediaChart = initChart(mediaChartRef.value);
  }
  if (!mediaChart) return;

  const selfMedia = compareData.value.selfMediaDistribution;
  const competitorMedia = compareData.value.competitorMediaDistribution;
  const categories = selfMedia.map((item) => item.name);

  const option: EChartsOption = {
    color: ['#0f766e', '#2563eb'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      top: 0,
      right: 8,
      textStyle: { color: '#64748b' }
    },
    grid: {
      left: 12,
      right: 18,
      top: 36,
      bottom: 12,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
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
      {
        name: '本品',
        type: 'bar',
        barWidth: 12,
        itemStyle: { borderRadius: [2, 2, 0, 0] },
        data: selfMedia.map((item) => item.value)
      },
      {
        name: '竞品',
        type: 'bar',
        barWidth: 12,
        itemStyle: { borderRadius: [2, 2, 0, 0] },
        data: competitorMedia.map((item) => item.value)
      }
    ]
  };
  mediaChart.setOption(option, true);
}

function sentimentClass(name: string): string {
  if (name === '正面') return 'emotion-positive';
  if (name === '负面') return 'emotion-negative';
  return 'emotion-neutral';
}
</script>

<style scoped>
.compare-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.compare-header h2 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  line-height: 26px;
  white-space: nowrap;
}

.compare-selectors {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.vs-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 24px;
  color: var(--color-muted, #64748b);
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
  background: #f8fafc;
  border: 1px solid #e5ebf2;
  border-radius: var(--radius, 8px);
}

.compare-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.compare-chart-radar {
  height: 340px;
}

.compare-stats {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.stat-item {
  min-height: 52px;
  padding: 10px 14px;
  border-radius: var(--radius, 8px);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-item span {
  font-size: 13px;
  line-height: 18px;
}

.stat-item strong {
  font-size: 22px;
  line-height: 28px;
}

.stat-self {
  background: rgba(15, 118, 110, 0.08);
  color: #0f766e;
}

.stat-competitor {
  background: rgba(37, 99, 235, 0.08);
  color: #2563eb;
}

.compare-sentiment-grid {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.sentiment-col {
  padding: 8px 10px;
  background: #f8fafc;
  border: 1px solid #e5ebf2;
  border-radius: var(--radius, 8px);
}

.sentiment-label {
  display: block;
  margin-bottom: 6px;
  color: var(--color-muted, #64748b);
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.sentiment-bars {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sentiment-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.sentiment-type {
  font-size: 12px;
  line-height: 18px;
}

.sentiment-value {
  font-size: 14px;
  line-height: 20px;
  font-weight: 700;
  color: #0f172a;
}

.compare-media-grid {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.media-col {
  padding: 8px 10px;
  background: #f8fafc;
  border: 1px solid #e5ebf2;
  border-radius: var(--radius, 8px);
}

.media-label {
  display: block;
  margin-bottom: 6px;
  color: var(--color-muted, #64748b);
  font-size: 12px;
  line-height: 18px;
  font-weight: 600;
}

.media-tag {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 3px 0;
  font-size: 12px;
  line-height: 18px;
  color: #334155;
}

.media-val {
  color: #0f172a;
  font-weight: 700;
}

@media (max-width: 1180px) {
  .compare-grid {
    grid-template-columns: 1fr;
  }

  .compare-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
