import { apiGet, apiPost } from './http';
import type {
  CampusAiCallLog,
  CampusAiFeatureBinding,
  CampusAiModel,
  CampusAiOverview,
  CampusAiPromptTemplate,
  CampusAiProvider,
  CampusAiProviderTestResult,
  PageResult
} from '../types/api';

export interface AiPageQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  enabled?: number;
}

export interface AiProviderQuery extends AiPageQuery {
  providerType?: string;
}

export interface AiModelQuery extends AiPageQuery {
  providerCode?: string;
}

export interface AiFeatureQuery extends AiPageQuery {
  featureType?: string;
}

export interface AiPromptQuery extends AiPageQuery {
  featureCode?: string;
}

export interface AiCallLogQuery {
  pageNum: number;
  pageSize: number;
  featureCode?: string;
  providerCode?: string;
  callStatus?: string;
}

export function getAiOverview() {
  return apiGet<CampusAiOverview>('/campus/ai/overview');
}

export function listAiProviders(params: AiProviderQuery) {
  return apiGet<PageResult<CampusAiProvider>>('/campus/ai/provider/list', params);
}

export function saveAiProvider(data: CampusAiProvider) {
  return apiPost<CampusAiProvider>('/campus/ai/provider/save', data);
}

export function deleteAiProvider(providerCode: string) {
  return apiPost<void>('/campus/ai/provider/delete', undefined, { providerCode });
}

export function testAiProvider(providerCode: string) {
  return apiPost<CampusAiProviderTestResult>('/campus/ai/provider/test', undefined, { providerCode });
}

export function listAiModels(params: AiModelQuery) {
  return apiGet<PageResult<CampusAiModel>>('/campus/ai/model/list', params);
}

export function saveAiModel(data: CampusAiModel) {
  return apiPost<CampusAiModel>('/campus/ai/model/save', data);
}

export function deleteAiModel(providerCode: string, modelCode: string) {
  return apiPost<void>('/campus/ai/model/delete', undefined, { providerCode, modelCode });
}

export function listAiFeatures(params: AiFeatureQuery) {
  return apiGet<PageResult<CampusAiFeatureBinding>>('/campus/ai/feature/list', params);
}

export function saveAiFeature(data: CampusAiFeatureBinding) {
  return apiPost<CampusAiFeatureBinding>('/campus/ai/feature/save', data);
}

export function listAiPrompts(params: AiPromptQuery) {
  return apiGet<PageResult<CampusAiPromptTemplate>>('/campus/ai/prompt/list', params);
}

export function saveAiPrompt(data: CampusAiPromptTemplate) {
  return apiPost<CampusAiPromptTemplate>('/campus/ai/prompt/save', data);
}

export function deleteAiPrompt(templateId: number) {
  return apiPost<void>('/campus/ai/prompt/delete', undefined, { templateId });
}

export function listAiCallLogs(params: AiCallLogQuery) {
  return apiGet<PageResult<CampusAiCallLog>>('/campus/ai/call-log/list', params);
}
