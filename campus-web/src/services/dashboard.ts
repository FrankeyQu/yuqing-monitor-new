import { apiGet } from './http';
import type {
  CampusAlert,
  CampusClue,
  CampusDetectionHit,
  CampusEvent,
  DashboardStatistics,
  DashboardTrendPoint,
  PageResult,
  WordCloudItem
} from '../types/api';

export function fetchDashboardStatistics() {
  return apiGet<DashboardStatistics>('/campus/dashboard/statistics');
}

export function fetchPendingAlerts() {
  return apiGet<PageResult<CampusAlert>>('/campus/alert/list', {
    pageNum: 1,
    pageSize: 6,
    alertStatus: 'pending'
  });
}

export function fetchPendingDetectionHits() {
  return apiGet<PageResult<CampusDetectionHit>>('/campus/detection/hit/list', {
    pageNum: 1,
    pageSize: 6,
    hitStatus: 'pending'
  });
}

export function fetchPendingClues() {
  return apiGet<PageResult<CampusClue>>('/campus/clue/list', {
    pageNum: 1,
    pageSize: 6,
    clueStatus: 'pending_judge'
  });
}

export function fetchActiveEvents() {
  return apiGet<PageResult<CampusEvent>>('/campus/event/list', {
    pageNum: 1,
    pageSize: 6,
    eventStatus: 'handling'
  });
}

export function fetchWordCloud() {
  return apiGet<WordCloudItem[]>('/campus/dashboard/word-cloud');
}

export function fetchDashboardTrend(days?: number) {
  return apiGet<DashboardTrendPoint[]>('/campus/dashboard/trend', { days: days || 7 });
}
