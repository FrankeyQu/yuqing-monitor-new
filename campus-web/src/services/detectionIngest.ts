import { apiGet, apiPost } from './http';
import type {
  CampusAccountContent,
  CampusClue,
  CampusDetectionHit,
  CampusDetectionRule,
  CampusDetectionRunLog,
  CampusDetectionTask,
  CampusDetectionTopic,
  CampusIngestApiCallLog,
  CampusIngestRecord,
  CampusIngestRunLog,
  CampusIngestSource,
  CampusIngestTask,
  CampusPublicWebWhitelist,
  PageResult
} from '../types/api';

export interface DetectionTopicQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  topicCategory?: string;
  enabled?: number | '';
}

export interface DetectionRuleQuery {
  pageNum: number;
  pageSize: number;
  topicId?: number;
  ruleType?: string;
  enabled?: number | '';
}

export interface DetectionTaskQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  topicId?: number;
  taskStatus?: string;
}

export interface DetectionHitQuery {
  pageNum: number;
  pageSize: number;
  detectionTaskId?: number;
  topicId?: number;
  objectType?: string;
  hitStatus?: string;
  riskLevel?: string;
  keyword?: string;
}

export interface DetectionRunLogQuery {
  pageNum: number;
  pageSize: number;
  detectionTaskId: number;
}

export interface IngestSourceQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  sourceType?: string;
  platform?: string;
  enabled?: number | '';
}

export interface IngestTaskQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  sourceId?: number;
  targetType?: string;
  taskStatus?: string;
}

export interface IngestRecordQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  sourceId?: number;
  taskId?: number;
  normalizedStatus?: string;
  targetType?: string;
}

export interface IngestRunLogQuery {
  pageNum: number;
  pageSize: number;
  taskId?: number;
  runStatus?: string;
  errorType?: string;
  triggerType?: string;
}

export function listDetectionTopics(params: DetectionTopicQuery) {
  return apiGet<PageResult<CampusDetectionTopic>>('/campus/detection/topic/list', params);
}

export function saveDetectionTopic(data: CampusDetectionTopic) {
  return apiPost<CampusDetectionTopic>('/campus/detection/topic/save', data);
}

export function deleteDetectionTopic(topicId: number) {
  return apiPost<void>('/campus/detection/topic/delete', undefined, { topicId });
}

export function listDetectionRules(params: DetectionRuleQuery) {
  return apiGet<PageResult<CampusDetectionRule>>('/campus/detection/rule/list', params);
}

export function saveDetectionRule(data: CampusDetectionRule) {
  return apiPost<CampusDetectionRule>('/campus/detection/rule/save', data);
}

export function deleteDetectionRule(ruleId: number) {
  return apiPost<void>('/campus/detection/rule/delete', undefined, { ruleId });
}

export function listDetectionTasks(params: DetectionTaskQuery) {
  return apiGet<PageResult<CampusDetectionTask>>('/campus/detection/task/list', params);
}

export function saveDetectionTask(data: CampusDetectionTask) {
  return apiPost<CampusDetectionTask>('/campus/detection/task/save', data);
}

export function updateDetectionTaskStatus(detectionTaskId: number, taskStatus: string) {
  return apiPost<CampusDetectionTask>('/campus/detection/task/update-status', undefined, {
    detectionTaskId,
    taskStatus
  });
}

export function deleteDetectionTask(detectionTaskId: number) {
  return apiPost<void>('/campus/detection/task/delete', undefined, { detectionTaskId });
}

export function runDetectionTask(detectionTaskId: number) {
  return apiPost<CampusDetectionRunLog>('/campus/detection/task/run', undefined, { detectionTaskId });
}

export function listDetectionHits(params: DetectionHitQuery) {
  return apiGet<PageResult<CampusDetectionHit>>('/campus/detection/hit/list', params);
}

export function alertDetectionHit(hitId: number) {
  return apiPost<CampusDetectionHit>('/campus/detection/hit/alert', undefined, { hitId });
}

export function ignoreDetectionHit(hitId: number) {
  return apiPost<CampusDetectionHit>('/campus/detection/hit/ignore', undefined, { hitId });
}

export function listDetectionRunLogs(params: DetectionRunLogQuery) {
  return apiGet<PageResult<CampusDetectionRunLog>>('/campus/detection/run-log/list', params);
}

export function listIngestSources(params: IngestSourceQuery) {
  return apiGet<PageResult<CampusIngestSource>>('/campus/ingest/source/list', params);
}

export function saveIngestSource(data: CampusIngestSource) {
  return apiPost<CampusIngestSource>('/campus/ingest/source/save', data);
}

export function deleteIngestSource(sourceId: number) {
  return apiPost<void>('/campus/ingest/source/delete', undefined, { sourceId });
}

export function listIngestTasks(params: IngestTaskQuery) {
  return apiGet<PageResult<CampusIngestTask>>('/campus/ingest/task/list', params);
}

export function saveIngestTask(data: CampusIngestTask) {
  return apiPost<CampusIngestTask>('/campus/ingest/task/save', data);
}

export function updateIngestTaskStatus(taskId: number, taskStatus: string) {
  return apiPost<CampusIngestTask>('/campus/ingest/task/update-status', undefined, { taskId, taskStatus });
}

export function deleteIngestTask(taskId: number) {
  return apiPost<void>('/campus/ingest/task/delete', undefined, { taskId });
}

export function runIngestTask(taskId: number) {
  return apiPost<CampusIngestRunLog>('/campus/ingest/task/run', undefined, { taskId });
}

export function listIngestRecords(params: IngestRecordQuery) {
  return apiGet<PageResult<CampusIngestRecord>>('/campus/ingest/record/list', params);
}

export function submitIngestRecord(data: CampusIngestRecord) {
  return apiPost<CampusIngestRecord>('/campus/ingest/record/submit', data);
}

export function convertRecordToClue(recordId: number) {
  return apiPost<CampusClue>('/campus/ingest/record/convert-clue', undefined, { recordId });
}

export function convertRecordToAccountContent(recordId: number, accountId?: number) {
  return apiPost<CampusAccountContent>('/campus/ingest/record/convert-account-content', undefined, {
    recordId,
    accountId
  });
}

export function startIngestRun(taskId: number) {
  return apiPost<CampusIngestRunLog>('/campus/ingest/run/start', undefined, { taskId });
}

export function finishIngestRun(
  runId: number,
  runStatus: string,
  fetchedCount?: number,
  successCount?: number,
  failCount?: number,
  errorMessage?: string
) {
  return apiPost<CampusIngestRunLog>('/campus/ingest/run/finish', undefined, {
    runId,
    runStatus,
    fetchedCount,
    successCount,
    failCount,
    errorMessage
  });
}

export function listIngestRunLogs(taskId: number) {
  return apiGet<CampusIngestRunLog[]>('/campus/ingest/run/list', { taskId });
}

export function listIngestRunLogPage(params: IngestRunLogQuery) {
  return apiGet<PageResult<CampusIngestRunLog>>('/campus/ingest/run/page', params);
}

export function listIngestApiCallLogs(params: { taskId?: number; runId?: number; provider?: string; callStatus?: string }) {
  return apiGet<CampusIngestApiCallLog[]>('/campus/ingest/api-call/list', params);
}

export function listPublicWebWhitelists(params: {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  siteDomain?: string;
  enabled?: number | '';
}) {
  return apiGet<PageResult<CampusPublicWebWhitelist>>('/campus/ingest/public-web/whitelist/list', params);
}

export function savePublicWebWhitelist(data: CampusPublicWebWhitelist) {
  return apiPost<CampusPublicWebWhitelist>('/campus/ingest/public-web/whitelist/save', data);
}

export function updatePublicWebWhitelistStatus(whitelistId: number, enabled: number) {
  return apiPost<CampusPublicWebWhitelist>('/campus/ingest/public-web/whitelist/update-status', undefined, {
    whitelistId,
    enabled
  });
}

export function deletePublicWebWhitelist(whitelistId: number) {
  return apiPost<void>('/campus/ingest/public-web/whitelist/delete', undefined, { whitelistId });
}
