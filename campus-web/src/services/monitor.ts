import { apiGet, apiPost } from './http';
import type {
  CampusAlert,
  CampusClue,
  CampusMonitorInformation,
  CampusMonitorOverview,
  CampusMonitorResult,
  CampusMonitorRunLog,
  CampusMonitorTask,
  CampusMonitorWatchTarget,
  ClueAdvancedQuery,
  PageResult
} from '../types/api';

export interface MonitorTaskQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  taskStatus?: string;
  platform?: string;
}

export interface MonitorResultQuery {
  pageNum: number;
  pageSize: number;
  monitorTaskId?: number;
  keyword?: string;
  riskLevel?: string;
  resultStatus?: string;
  platform?: string;
  language?: string;
  converted?: boolean;
}

export interface MonitorInformationQuery extends Record<string, unknown> {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  monitorTaskId?: number;
  sourcePlatform?: string;
  sourceSubPlatform?: string;
  riskLevel?: string;
  clueStatus?: string;
  language?: string;
  sentiment?: string;
  resultStatus?: string;
  publishTimeStart?: string;
  publishTimeEnd?: string;
  collectTimeStart?: string;
  collectTimeEnd?: string;
  matchScope?: string;
  similarDedup?: boolean;
  sortBy?: string;
  hitScope?: string;
}

export interface MonitorAlertQuery {
  pageNum: number;
  pageSize: number;
  monitorTaskId?: number;
  keyword?: string;
  riskLevel?: string;
  alertStatus?: string;
}

export function fetchMonitorOverview() {
  return apiGet<CampusMonitorOverview>('/campus/monitor/overview');
}

export function listMonitorTasks(params: MonitorTaskQuery) {
  return apiGet<PageResult<CampusMonitorTask>>('/campus/monitor/task/list', params);
}

export function saveMonitorTask(data: CampusMonitorTask) {
  return apiPost<CampusMonitorTask>('/campus/monitor/task/save', data);
}

export function updateMonitorTaskStatus(monitorTaskId: number, taskStatus: string) {
  return apiPost<CampusMonitorTask>('/campus/monitor/task/update-status', undefined, {
    monitorTaskId,
    taskStatus
  });
}

export function updateMonitorTaskDisplay(monitorTaskId: number, displayEnabled: number) {
  return apiPost<CampusMonitorTask>('/campus/monitor/task/update-display', undefined, {
    monitorTaskId,
    displayEnabled
  });
}

export function deleteMonitorTask(monitorTaskId: number) {
  return apiPost<void>('/campus/monitor/task/delete', undefined, { monitorTaskId });
}

export function runMonitorTask(monitorTaskId: number) {
  return apiPost<CampusMonitorRunLog>('/campus/monitor/task/run', undefined, { monitorTaskId });
}

export function listMonitorRunLogs(params: { pageNum: number; pageSize: number; monitorTaskId: number }) {
  return apiGet<PageResult<CampusMonitorRunLog>>('/campus/monitor/task/run-log/list', params);
}

export function listMonitorResults(params: MonitorResultQuery) {
  return apiGet<PageResult<CampusMonitorResult>>('/campus/monitor/result/list', params);
}

export function listMonitorInformation(params: MonitorInformationQuery) {
  return apiGet<PageResult<CampusMonitorInformation>>('/campus/monitor/information/list', params);
}

export function fetchMonitorInformationPlatformCounts(params?: MonitorInformationQuery | Record<string, unknown>) {
  return apiGet<Array<{ name: string; value: number }>>('/campus/monitor/information/count-by-platform', params);
}

export function fetchMonitorInformationSubPlatformCounts(params?: MonitorInformationQuery | Record<string, unknown>) {
  return apiGet<Array<{ name: string; value: number }>>('/campus/monitor/information/count-by-sub-platform', params);
}

export function alertMonitorResult(monitorResultId: number) {
  return apiPost<CampusMonitorResult>('/campus/monitor/result/alert', undefined, { monitorResultId });
}

export function ignoreMonitorResult(monitorResultId: number) {
  return apiPost<CampusMonitorResult>('/campus/monitor/result/ignore', undefined, { monitorResultId });
}

export function convertMonitorResultToClue(monitorResultId: number) {
  return apiPost<CampusClue>('/campus/monitor/result/convert-clue', undefined, { monitorResultId });
}

export function listMonitorWatchTargets(params: {
  pageNum: number;
  pageSize: number;
  monitorTaskId?: number;
  targetType?: string;
  platform?: string;
  keyword?: string;
  targetStatus?: string;
}) {
  return apiGet<PageResult<CampusMonitorWatchTarget>>('/campus/monitor/watch-target/list', params);
}

export function saveMonitorWatchTarget(data: CampusMonitorWatchTarget) {
  return apiPost<CampusMonitorWatchTarget>('/campus/monitor/watch-target/save', data);
}

export function createMonitorWatchTargetFromResult(monitorResultId: number, monitorTaskId: number, targetType: 'account' | 'link') {
  return apiPost<CampusMonitorWatchTarget>('/campus/monitor/watch-target/create-from-result', undefined, {
    monitorResultId,
    monitorTaskId,
    targetType
  });
}

export function deleteMonitorWatchTarget(targetId: number) {
  return apiPost<void>('/campus/monitor/watch-target/delete', undefined, { targetId });
}

export function listMonitorAlerts(params: MonitorAlertQuery) {
  return apiGet<PageResult<CampusAlert>>('/campus/monitor/alert/list', params);
}

export function handleMonitorAlert(alertId: number, alertStatus: string, handleOpinion?: string) {
  return apiPost<CampusAlert>('/campus/monitor/alert/handle', undefined, {
    alertId,
    alertStatus,
    handleOpinion
  });
}

/** 高级筛选获取线索列表 */
export function listCluesAdvanced(params: ClueAdvancedQuery | Record<string, unknown>) {
  return apiGet<PageResult<CampusClue>>('/campus/clue/list', params);
}

/** 获取各媒体类型数量 */
export function fetchMediaTypeCounts(params?: Partial<ClueAdvancedQuery> | Record<string, unknown>) {
  return apiGet<Array<{ name: string; value: number }>>('/campus/clue/count-by-media-type', params);
}

/** 获取子平台数量 */
export function fetchSubPlatformCounts(params?: Partial<ClueAdvancedQuery> | Record<string, unknown>) {
  return apiGet<Array<{ name: string; value: number }>>('/campus/clue/count-by-sub-platform', params);
}
