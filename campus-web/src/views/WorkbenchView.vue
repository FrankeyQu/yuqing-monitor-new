<template>
  <section class="workbench">
    <div class="stat-grid">
      <article v-for="card in statCards" :key="card.label" class="stat-card" :class="`tone-${card.tone}`">
        <div class="stat-icon">
          <component :is="card.icon" :size="22" />
        </div>
        <div>
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
        </div>
      </article>
    </div>

    <el-alert
      v-if="errorMessage"
      class="data-alert"
      :title="errorMessage"
      type="warning"
      show-icon
      :closable="false"
    />

    <section class="quick-actions">
      <el-button type="primary" @click="$router.push('/monitor')">
        <Radar :size="16" />
        监测信息
      </el-button>
      <el-button @click="$router.push('/situation')">
        <Activity :size="16" />
        态势大屏
      </el-button>
      <el-button @click="$router.push('/ingest')">
        <Database :size="16" />
        数据接入
      </el-button>
      <el-button @click="$router.push('/alerts')">
        <Bell :size="16" />
        预警处理
      </el-button>
      <el-button @click="$router.push('/analysis')">
        <BrainCircuit :size="16" />
        辅助研判
      </el-button>
    </section>

    <div class="dashboard-grid">
      <section class="panel panel-wide">
        <div class="panel-header">
          <h2>监测运行概览</h2>
          <el-tag effect="plain" type="success">任务 / 命中 / 告警</el-tag>
        </div>
        <div class="workbench-summary-grid">
          <div>
            <span>启用任务</span>
            <strong>{{ monitorOverview.activeTaskCount ?? 0 }}</strong>
          </div>
          <div>
            <span>自动扫描</span>
            <strong>{{ monitorOverview.scheduledTaskCount ?? 0 }}</strong>
          </div>
          <div>
            <span>今日命中</span>
            <strong>{{ monitorOverview.todayResultCount ?? 0 }}</strong>
          </div>
          <div>
            <span>今日负面告警</span>
            <strong>{{ monitorOverview.todayAlertCount ?? 0 }}</strong>
          </div>
          <div>
            <span>待处理告警</span>
            <strong>{{ monitorOverview.pendingAlertCount ?? 0 }}</strong>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>全局处置概况</h2>
          <el-tag effect="plain" type="info">闭环</el-tag>
        </div>
        <div class="status-stack">
          <div class="status-row">
            <span>待处理预警</span>
            <strong>{{ statistics.overview.pendingAlertCount ?? 0 }}</strong>
          </div>
          <div class="status-row">
            <span>处置中事件</span>
            <strong>{{ statistics.overview.activeEventCount ?? 0 }}</strong>
          </div>
          <div class="status-row">
            <span>高风险事件</span>
            <strong>{{ statistics.overview.highRiskEventCount ?? 0 }}</strong>
          </div>
          <div class="status-row">
            <span>待研判线索</span>
            <strong>{{ statistics.overview.pendingClueCount ?? 0 }}</strong>
          </div>
        </div>
      </section>
    </div>

    <div class="table-grid">
      <section class="panel">
        <div class="panel-header">
          <h2>监测负面告警</h2>
          <el-button link type="primary" @click="$router.push('/monitor')">查看</el-button>
        </div>
        <el-table :data="monitorAlerts" size="small" v-loading="loading" height="270">
          <el-table-column prop="alertTitle" label="告警" min-width="160" show-overflow-tooltip />
          <el-table-column prop="matchedKeywords" label="命中词" width="96" show-overflow-tooltip />
          <el-table-column prop="riskLevel" label="级别" width="72">
            <template #default="{ row }">
              <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>待处理预警</h2>
          <el-button link type="primary" @click="$router.push('/alerts')">查看</el-button>
        </div>
        <el-table :data="pendingAlerts" size="small" v-loading="loading" height="270">
          <el-table-column prop="alertTitle" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="riskLevel" label="级别" width="72">
            <template #default="{ row }">
              <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>检测命中</h2>
          <el-button link type="primary" @click="$router.push('/detection')">查看</el-button>
        </div>
        <el-table :data="pendingHits" size="small" v-loading="loading" height="270">
          <el-table-column prop="objectTitle" label="对象" min-width="132" show-overflow-tooltip />
          <el-table-column prop="matchedKeywords" label="命中" width="86" show-overflow-tooltip />
          <el-table-column prop="riskLevel" label="级别" width="72">
            <template #default="{ row }">
              <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>

    </div>

    <section v-if="hotRankReady" class="hot-rank-grid">
      <HotRankPanel
        title="微博热搜"
        :icon="Globe"
        color="#E6162D"
        :items="weiboHotRank"
        :loading="hotRankLoading"
      />
      <HotRankPanel
        title="抖音热榜"
        :icon="Music"
        color="#000000"
        :items="douyinHotRank"
        :loading="hotRankLoading"
      />
      <HotRankPanel
        title="今日头条"
        :icon="Newspaper"
        color="#0078FF"
        :items="toutiaoHotRank"
        :loading="hotRankLoading"
      />
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import {
  Activity,
  Bell,
  BrainCircuit,
  ClipboardList,
  ClockAlert,
  Database,
  Globe,
  Music,
  Newspaper,
  Radar,
  ShieldAlert,
  UsersRound
} from 'lucide-vue-next';
import {
  fetchDashboardStatistics,
  fetchPendingAlerts,
  fetchPendingDetectionHits
} from '../services/dashboard';
import { listMonitorAlerts } from '../services/monitor';
import HotRankPanel from '../components/HotRankPanel.vue';
import type { HotRankItem } from '../components/HotRankPanel.vue';
import {
  fetchWeiboHotRank,
  fetchDouyinHotRank,
  fetchToutiaoHotRank
} from '../services/hotRank';
import { campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type {
  CampusAlert,
  CampusDetectionHit,
  DashboardStatistics,
  MonitorDashboardOverview
} from '../types/api';

const loading = ref(false);
const errorMessage = ref('');
const statistics = ref<DashboardStatistics>({
  overview: {},
  monitorOverview: {},
  riskDistribution: [],
  clueSourceDistribution: [],
  eventStatusDistribution: [],
  monitorTrendByDay: []
});
const pendingAlerts = ref<CampusAlert[]>([]);
const pendingHits = ref<CampusDetectionHit[]>([]);
const monitorAlerts = ref<CampusAlert[]>([]);

const hotRankReady = ref(true);
const hotRankLoading = ref(false);
const weiboHotRank = ref<HotRankItem[]>([]);
const douyinHotRank = ref<HotRankItem[]>([]);
const toutiaoHotRank = ref<HotRankItem[]>([]);

const monitorOverview = computed<MonitorDashboardOverview>(() => statistics.value.monitorOverview || {});

const statCards = computed(() => [
  {
    label: '启用任务',
    value: monitorOverview.value.activeTaskCount ?? 0,
    icon: Radar,
    tone: 'blue'
  },
  {
    label: '自动扫描',
    value: monitorOverview.value.scheduledTaskCount ?? 0,
    icon: ClockAlert,
    tone: 'cyan'
  },
  {
    label: '今日命中',
    value: monitorOverview.value.todayResultCount ?? 0,
    icon: ClipboardList,
    tone: 'green'
  },
  {
    label: '今日告警',
    value: monitorOverview.value.todayAlertCount ?? 0,
    icon: Bell,
    tone: 'orange'
  },
  {
    label: '待处理告警',
    value: monitorOverview.value.pendingAlertCount ?? 0,
    icon: ShieldAlert,
    tone: 'red'
  },
  {
    label: '处置中事件',
    value: statistics.value.overview.activeEventCount ?? 0,
    icon: UsersRound,
    tone: 'cyan'
  }
]);

onMounted(() => {
  loadWorkbench();
  loadHotRank();
});

async function loadHotRank() {
  hotRankLoading.value = true;
  try {
    const [weibo, douyin, toutiao] = await Promise.all([
      fetchWeiboHotRank(),
      fetchDouyinHotRank(),
      fetchToutiaoHotRank()
    ]);
    weiboHotRank.value = weibo;
    douyinHotRank.value = douyin;
    toutiaoHotRank.value = toutiao;
  } catch {
    hotRankReady.value = false;
  } finally {
    hotRankLoading.value = false;
  }
}

async function loadWorkbench() {
  loading.value = true;
  errorMessage.value = '';
  const [stats, monitorAlertPage, alerts, hits] = await Promise.allSettled([
    fetchDashboardStatistics(),
    listMonitorAlerts({ pageNum: 1, pageSize: 6, alertStatus: 'pending' }),
    fetchPendingAlerts(),
    fetchPendingDetectionHits()
  ]);

  if (stats.status === 'fulfilled') {
    statistics.value = stats.value;
  }
  if (monitorAlertPage.status === 'fulfilled') {
    monitorAlerts.value = monitorAlertPage.value.list || [];
  }
  if (alerts.status === 'fulfilled') {
    pendingAlerts.value = alerts.value.list || [];
  }
  if (hits.status === 'fulfilled') {
    pendingHits.value = hits.value.list || [];
  }

  if ([stats, monitorAlertPage, alerts, hits].some((item) => item.status === 'rejected')) {
    errorMessage.value = '部分数据暂时不可用';
  }
  loading.value = false;
}

function riskLabel(value?: string) {
  return campusRiskLabel(value, '未知');
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}
</script>
