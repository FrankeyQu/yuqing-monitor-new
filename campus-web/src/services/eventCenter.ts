import { apiGet, apiPost } from './http';
import type { CampusAlert, CampusClue, CampusDisposalRecord, CampusDisposalTask, CampusEvent, PageResult } from '../types/api';

export interface AlertQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  alertSource?: string;
  riskLevel?: string;
  alertStatus?: string;
}

export interface EventQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  riskLevel?: string;
  eventStatus?: string;
}

export function listAlerts(params: AlertQuery) {
  return apiGet<PageResult<CampusAlert>>('/campus/alert/list', params);
}

export function handleAlert(alertId: number, alertStatus: string, handleOpinion?: string) {
  return apiPost<CampusAlert>('/campus/alert/handle', undefined, { alertId, alertStatus, handleOpinion });
}

export function listEvents(params: EventQuery) {
  return apiGet<PageResult<CampusEvent>>('/campus/event/list', params);
}

export function saveEvent(data: CampusEvent) {
  return apiPost<CampusEvent>('/campus/event/save', data);
}

export function rateEvent(eventId: number, riskLevel: string, disposalRequirement?: string) {
  return apiPost<CampusEvent>('/campus/event/rate', undefined, { eventId, riskLevel, disposalRequirement });
}

export function archiveEvent(eventId: number, archiveConclusion: string) {
  return apiPost<CampusEvent>('/campus/event/archive', undefined, { eventId, archiveConclusion });
}

export function assignDisposalTask(data: CampusDisposalTask) {
  return apiPost<CampusDisposalTask>('/campus/event/assign', data);
}

export function listDisposalTasks(eventId: number) {
  return apiGet<CampusDisposalTask[]>('/campus/event/task/list', { eventId });
}

export function listSimilarEventClues(eventId: number, limit = 10) {
  return apiGet<CampusClue[]>('/campus/event/clue/suggest', { eventId, limit });
}

export function feedbackDisposalTask(disposalTaskId: number, recordContent: string, attachmentDesc?: string) {
  return apiPost<CampusDisposalRecord>('/campus/event/feedback', undefined, {
    disposalTaskId,
    recordContent,
    attachmentDesc
  });
}

export function returnDisposalTask(disposalTaskId: number, recordContent: string) {
  return apiPost<CampusDisposalRecord>('/campus/event/return', undefined, { disposalTaskId, recordContent });
}

export function confirmDisposalTask(disposalTaskId: number, recordContent: string) {
  return apiPost<CampusDisposalRecord>('/campus/event/confirm', undefined, { disposalTaskId, recordContent });
}
