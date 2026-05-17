import { computed, ref } from 'vue';
import {
  fetchActiveEvents,
  fetchDashboardStatistics,
  fetchDashboardTrend,
  fetchPendingAlerts,
  fetchPendingClues,
  fetchPendingDetectionHits,
  fetchWordCloud
} from '../services/dashboard';
import { listMonitorAlerts, listMonitorInformation, listMonitorTasks } from '../services/monitor';
import { campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type {
  CampusAlert,
  CampusClue,
  CampusDetectionHit,
  CampusEvent,
  CampusMonitorInformation,
  CampusMonitorTask,
  DashboardStatistics,
  DashboardTrendPoint,
  DistributionItem,
  MonitorDashboardOverview,
  MonitorTrendItem,
  SourceRiskDistributionItem,
  WordCloudItem
} from '../types/api';

export type DashboardMode = 'normal' | 'screen';

export const riskColors: Record<string, string> = {
  normal: '#15803d',
  concern: '#d97706',
  major: '#ea580c',
  urgent: '#dc2626',
  unknown: '#64748b'
};

export const riskOrder = ['normal', 'concern', 'major', 'urgent'];

export function useCampusSituationDashboard() {
  const loading = ref(false);
  const errorMessage = ref('');
  const now = ref(new Date());
  const statistics = ref<DashboardStatistics>(emptyStatistics());
  const wordCloudData = ref<WordCloudItem[]>([]);
  const dashboardTrendData = ref<DashboardTrendPoint[]>([]);
  const pendingClues = ref<CampusClue[]>([]);
  const pendingAlerts = ref<CampusAlert[]>([]);
  const pendingHits = ref<CampusDetectionHit[]>([]);
  const activeEvents = ref<CampusEvent[]>([]);
  const monitorTasks = ref<CampusMonitorTask[]>([]);
  const monitorResults = ref<CampusMonitorInformation[]>([]);
  const monitorResultTotal = ref(0);
  const monitorAlerts = ref<CampusAlert[]>([]);

  const nowText = computed(() => now.value.toLocaleString('zh-CN', { hour12: false }));
  const monitorOverview = computed<MonitorDashboardOverview>(() => statistics.value.monitorOverview || {});
  const monitorTrendRows = computed<MonitorTrendItem[]>(() => statistics.value.monitorTrendByDay || []);
  const trendRows = computed(() => statistics.value.trendByDay || []);
  const riskRows = computed(() => statistics.value.riskDistribution || []);
  const eventRows = computed(() => statistics.value.eventStatusDistribution || []);
  const alertRiskRows = computed(() => statistics.value.alertRiskDistribution || []);
  const detectionRiskRows = computed(() => statistics.value.detectionHitRiskDistribution || []);
  const sourceRiskRows = computed(() => statistics.value.sourceRiskDistribution || []);
  const sourceRows = computed(() => statistics.value.clueSourceDistribution || []);
  const sentimentRows = computed<DistributionItem[]>(() => {
    const counts: Record<string, number> = {};
    for (const clue of pendingClues.value) {
      const key = clue.sentiment || 'unknown';
      counts[key] = (counts[key] || 0) + 1;
    }
    return Object.entries(counts).map(([name, value]) => ({ name, value }));
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

  const monitorRunFacts = computed(() => {
    const activeTaskCount = toNumber(monitorOverview.value.activeTaskCount);
    const scheduledTaskCount = toNumber(monitorOverview.value.scheduledTaskCount);
    const todayResultCount = toNumber(monitorOverview.value.todayResultCount);
    const todayAlertCount = toNumber(monitorOverview.value.todayAlertCount);
    const pendingAlertCount = toNumber(monitorOverview.value.pendingAlertCount);
    return [
      { label: '自动覆盖', value: `${monitorScheduleRate.value}%`, note: `${scheduledTaskCount}/${activeTaskCount || 0}` },
      { label: '今日命中', value: String(todayResultCount), note: '近 24 小时监测结果' },
      { label: '今日负面', value: String(todayAlertCount), note: '已进入告警通道' },
      { label: '待处理', value: String(pendingAlertCount), note: '人工处置队列' }
    ];
  });

  const riskScore = computed(() => {
    const overview = statistics.value.overview || {};
    const monitor = monitorOverview.value || {};
    const score =
      toNumber(overview.pendingAlertCount) * 14 +
      toNumber(overview.highRiskEventCount) * 22 +
      toNumber(overview.activeEventCount) * 8 +
      toNumber(overview.overdueDisposalCount) * 12 +
      toNumber(monitor.pendingAlertCount) * 8 +
      toNumber(monitor.todayAlertCount) * 6 +
      toNumber(monitor.todayResultCount) * 2;
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

  async function loadDashboard() {
    loading.value = true;
    errorMessage.value = '';
    try {
      const [
        stats,
        words,
        dashboardTrend,
        clues,
        alerts,
        hits,
        events,
        monitorTaskPage,
        monitorResultPage,
        monitorAlertPage
      ] = await Promise.allSettled([
        fetchDashboardStatistics(),
        fetchWordCloud(),
        fetchDashboardTrend(7),
        fetchPendingClues(),
        fetchPendingAlerts(),
        fetchPendingDetectionHits(),
        fetchActiveEvents(),
        listMonitorTasks({ pageNum: 1, pageSize: 6, taskStatus: 'active' }),
        listMonitorInformation({ pageNum: 1, pageSize: 8, hitScope: 'risk' }),
        listMonitorAlerts({ pageNum: 1, pageSize: 6, alertStatus: 'pending' })
      ]);

      if (stats.status === 'fulfilled') {
        statistics.value = { ...emptyStatistics(), ...stats.value };
      }
      if (words.status === 'fulfilled') {
        wordCloudData.value = words.value || [];
      }
      if (dashboardTrend.status === 'fulfilled') {
        dashboardTrendData.value = dashboardTrend.value || [];
      }
      if (clues.status === 'fulfilled') {
        pendingClues.value = clues.value.list || [];
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
      }
      if (monitorAlertPage.status === 'fulfilled') {
        monitorAlerts.value = monitorAlertPage.value.list || [];
      }

      const hasError = [
        stats,
        words,
        dashboardTrend,
        clues,
        alerts,
        hits,
        events,
        monitorTaskPage,
        monitorResultPage,
        monitorAlertPage
      ].some((item) => item.status === 'rejected');
      if (hasError) {
        errorMessage.value = '部分态势数据暂时不可用';
      }
    } finally {
      now.value = new Date();
      loading.value = false;
    }
  }

  return {
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
    monitorScheduleRate,
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
  };
}

export function emptyStatistics(): DashboardStatistics {
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

export function toNumber(value: unknown) {
  const numberValue = Number(value || 0);
  return Number.isFinite(numberValue) ? numberValue : 0;
}

export function sumValues(rows: DistributionItem[]) {
  return rows.reduce((total, item) => total + toNumber(item.value), 0);
}

export function getDistributionValue(rows: DistributionItem[], risk: string) {
  return toNumber(rows.find((item) => item.name === risk)?.value);
}

export function truncateText(value: string | undefined, length: number) {
  if (!value || value.length <= length) {
    return value || '-';
  }
  return `${value.slice(0, length)}...`;
}

export function riskLabel(value?: string) {
  return campusRiskLabel(value, '未知');
}

export function riskTagType(value?: string) {
  return campusRiskTagType(value);
}

export function sourceLabel(value?: string) {
  return value && value !== 'unknown' ? value : '未知来源';
}

export function alertSourceLabel(value?: string) {
  const labels: Record<string, string> = {
    monitor: '监测',
    detection: '检测',
    clue: '线索',
    event: '事件'
  };
  return labels[value || 'monitor'] || value || '监测';
}

export function statusLabel(value?: string) {
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

export function frequencyLabel(value?: number) {
  const labels: Record<number, string> = { 10: '10分钟', 30: '30分钟', 60: '1小时', 360: '6小时', 1440: '每天' };
  return value ? labels[value] || `${value}分钟` : '-';
}

export function taskStatusLabel(value?: string) {
  const labels: Record<string, string> = { active: '启用', paused: '暂停', disabled: '禁用' };
  return labels[value || 'active'] || value || '启用';
}

export function taskStatusTagType(value?: string) {
  if (value === 'active') {
    return 'success';
  }
  if (value === 'paused') {
    return 'warning';
  }
  return 'info';
}

export function sentimentLabel(value?: string) {
  const labels: Record<string, string> = {
    positive: '正面',
    neutral: '中性',
    negative: '负面',
    none: '无倾向',
    unknown: '未知'
  };
  return labels[value || 'unknown'] || value || '未知';
}

export function formatTime(value?: string | Date) {
  if (!value) {
    return '-';
  }
  try {
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) {
      return '-';
    }
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
