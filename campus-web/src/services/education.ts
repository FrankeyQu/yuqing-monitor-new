import { apiGet, apiPost, http } from './http';
import type {
  CampusEducationBaiduTaskRequest,
  CampusEducationTopicItem,
  CampusIngestRunLog,
  CampusIngestTask,
  CampusSchoolSentimentRank,
  CampusSchoolSubject,
  PageResult
} from '../types/api';

export function listSchools(params: {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  region?: string;
  educationStage?: string;
  status?: number | '';
}) {
  return apiGet<PageResult<CampusSchoolSubject>>('/campus/education/school/list', params);
}

export function saveSchool(data: CampusSchoolSubject) {
  return apiPost<CampusSchoolSubject>('/campus/education/school/save', data);
}

export function deleteSchool(schoolId: number) {
  return apiPost<void>('/campus/education/school/delete', undefined, { schoolId });
}

export function listEducationTopics(params: {
  topicType?: string;
  startTime?: string;
  endTime?: string;
  limit?: number;
}) {
  return apiGet<CampusEducationTopicItem[]>('/campus/education/topic/list', params);
}

export function fetchSchoolSentimentRanking(params: {
  keyword?: string;
  startTime?: string;
  endTime?: string;
  limit?: number;
}) {
  return apiGet<CampusSchoolSentimentRank[]>('/campus/education/ranking/school-sentiment', params);
}

export function createEducationBaiduTask(data: CampusEducationBaiduTaskRequest) {
  return apiPost<CampusIngestTask>('/campus/education/baidu-task/create', data);
}

export function createAndRunEducationBaiduTask(data: CampusEducationBaiduTaskRequest) {
  return apiPost<{ task: CampusIngestTask; runLog: CampusIngestRunLog }>('/campus/education/baidu-task/create-and-run', data);
}

export function importSchools(file: File) {
  const form = new FormData();
  form.append('file', file);
  return apiPost<Record<string, number>>('/campus/education/school/import', form);
}

export async function downloadSchoolTemplate() {
  const response = await http.get<Blob>('/campus/education/school/template', { responseType: 'blob' });
  return response.data;
}
