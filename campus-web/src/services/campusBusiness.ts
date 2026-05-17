import { apiGet, apiPost } from './http';
import type { CampusAccount, CampusAccountContent, CampusAlert, CampusClue, PageResult } from '../types/api';

export interface ClueQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  clueSource?: string;
  sourcePlatform?: string;
  riskLevel?: string;
  clueStatus?: string;
  language?: string;
}

export interface AccountQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  platform?: string;
  focusLevel?: string;
  auditStatus?: string;
  accountStatus?: string;
}

export interface AccountContentQuery {
  pageNum: number;
  pageSize: number;
  accountId?: number;
  taskId?: number;
  riskLevel?: string;
  keyword?: string;
}

export function suggestClues(keyword: string) {
  return apiGet<string[]>('/campus/clue/suggest', { keyword });
}

export function listClues(params: ClueQuery) {
  return apiGet<PageResult<CampusClue>>('/campus/clue/list', params);
}

export function saveClue(data: CampusClue) {
  return apiPost<CampusClue>('/campus/clue/save', data);
}

export function deleteClue(clueId: number) {
  return apiPost<void>('/campus/clue/delete', undefined, { clueId });
}

export function judgeClue(clueId: number, riskLevel: string, judgeOpinion?: string) {
  return apiPost<CampusClue>('/campus/clue/judge', undefined, { clueId, riskLevel, judgeOpinion });
}

export function archiveClue(clueId: number, archiveReason?: string) {
  return apiPost<CampusClue>('/campus/clue/archive', undefined, { clueId, archiveReason });
}

export function listAccounts(params: AccountQuery) {
  return apiGet<PageResult<CampusAccount>>('/campus/account/list', params);
}

export function saveAccount(data: CampusAccount) {
  return apiPost<CampusAccount>('/campus/account/save', data);
}

export function deleteAccount(accountId: number) {
  return apiPost<void>('/campus/account/delete', undefined, { accountId });
}

export function auditAccount(accountId: number, auditStatus: string, auditOpinion?: string) {
  return apiPost<CampusAccount>('/campus/account/audit', undefined, { accountId, auditStatus, auditOpinion });
}

export function updateAccountStatus(accountId: number, accountStatus: string) {
  return apiPost<CampusAccount>('/campus/account/update-status', undefined, { accountId, accountStatus });
}

export function listAccountContents(params: AccountContentQuery) {
  return apiGet<PageResult<CampusAccountContent>>('/campus/account/content/list', params);
}

export function addAccountContent(data: CampusAccountContent) {
  return apiPost<CampusAccountContent>('/campus/account/content/add', data);
}

// 舆情研判 API
export interface JudgmentQuery {
  pageNum: number;
  pageSize: number;
  language?: string;
  riskLevel?: string;
  clueStatus?: string;
  keyword?: string;
}

export function listCluesForJudgment(params: JudgmentQuery) {
  return apiGet<PageResult<CampusClue>>('/campus/clue/list', params);
}

export function createAlertFromClue(clueId: number) {
  return apiPost<CampusAlert[]>('/campus/alert/create-from-clue', undefined, { clueId });
}
