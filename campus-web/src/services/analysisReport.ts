import { apiGet, apiPost, http } from './http';
import type {
  ApiId,
  CampusAnalysisResult,
  CampusAnalysisTask,
  CampusReport,
  CampusReportGenerationLog,
  CampusReportJob,
  CampusReportTemplate,
  PageResult
} from '../types/api';

export interface AnalysisTaskQuery {
  pageNum: number;
  pageSize: number;
  objectType?: string;
  objectId?: number;
  analysisType?: string;
  taskStatus?: string;
}

export interface AnalysisResultQuery {
  pageNum: number;
  pageSize: number;
  analysisTaskId?: number;
  objectType?: string;
  objectId?: number;
  analysisType?: string;
  adoptionStatus?: string;
}

export interface ReportTemplateQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  reportType?: string;
  status?: number | '';
}

export interface ReportQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  reportType?: string;
  reportStatus?: string;
}

export interface ReportJobQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  reportType?: string;
  jobStatus?: string;
}

export function createAnalysisTask(data: CampusAnalysisTask) {
  return apiPost<CampusAnalysisTask>('/campus/analysis/task/create', data);
}

export function listAnalysisTasks(params: AnalysisTaskQuery) {
  return apiGet<PageResult<CampusAnalysisTask>>('/campus/analysis/task/list', params);
}

export function runAnalysisTask(analysisTaskId: number) {
  return apiPost<CampusAnalysisResult>('/campus/analysis/task/run', undefined, { analysisTaskId });
}

export function listAnalysisResults(params: AnalysisResultQuery) {
  return apiGet<PageResult<CampusAnalysisResult>>('/campus/analysis/result/list', params);
}

export function reviewAnalysisResult(analysisResultId: number, adoptionStatus: string, reviewOpinion?: string) {
  return apiPost<CampusAnalysisResult>('/campus/analysis/result/review', undefined, {
    analysisResultId,
    adoptionStatus,
    reviewOpinion
  });
}

export function listReportTemplates(params: ReportTemplateQuery) {
  return apiGet<PageResult<CampusReportTemplate>>('/campus/report/template/list', params);
}

export function saveReportTemplate(data: CampusReportTemplate) {
  return apiPost<CampusReportTemplate>('/campus/report/template/save', data);
}

export function deleteReportTemplate(templateId: ApiId) {
  return apiPost<void>('/campus/report/template/delete', undefined, { templateId });
}

export function listReports(params: ReportQuery) {
  return apiGet<PageResult<CampusReport>>('/campus/report/list', params);
}

export function getReportDetail(reportId: ApiId) {
  return apiGet<CampusReport>('/campus/report/detail', { reportId });
}

export function saveReport(data: CampusReport) {
  return apiPost<CampusReport>('/campus/report/save', data);
}

export function generateReport(reportId: ApiId) {
  return apiPost<CampusReport>('/campus/report/generate', undefined, { reportId });
}

export function generateReportAi(reportId: ApiId, aiUserPrompt?: string) {
  return apiPost<CampusReport>('/campus/report/generate-ai', undefined, { reportId, aiUserPrompt });
}

export function getGenerateAiStreamUrl(reportId: ApiId, aiUserPrompt?: string) {
  const base = http.defaults.baseURL || '';
  const params = new URLSearchParams({ reportId: String(reportId) });
  if (aiUserPrompt) {
    params.set('aiUserPrompt', aiUserPrompt);
  }
  return `${base}/campus/report/generate-ai-stream?${params.toString()}`;
}

export async function downloadReportDocx(reportId: ApiId) {
  const response = await http.get<Blob>('/campus/report/download-docx', {
    params: { reportId },
    responseType: 'blob'
  });
  triggerBlobDownload(response, reportId, 'docx');
}

export async function downloadReportPptx(reportId: ApiId) {
  const response = await http.get<Blob>('/campus/report/download-pptx', {
    params: { reportId },
    responseType: 'blob'
  });
  triggerBlobDownload(response, reportId, 'pptx');
}

function triggerBlobDownload(
  response: { data: Blob; headers: Record<string, unknown> },
  reportId: ApiId,
  ext: string
) {
  const disposition = String(response.headers['content-disposition'] || '');
  const fileNameMatch = disposition.match(/filename="?([^";]+)"?/i);
  const fileName = fileNameMatch?.[1]
    ? decodeURIComponent(fileNameMatch[1])
    : `campus-report-${reportId}.${ext}`;
  const url = URL.createObjectURL(response.data);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = fileName;
  anchor.click();
  URL.revokeObjectURL(url);
}

export function archiveReport(reportId: ApiId, archiveOpinion?: string) {
  return apiPost<CampusReport>('/campus/report/archive', undefined, { reportId, archiveOpinion });
}

export function deleteReport(reportId: ApiId) {
  return apiPost<void>('/campus/report/delete', undefined, { reportId });
}

export async function downloadReport(reportId: ApiId) {
  const response = await http.get<Blob>('/campus/report/download', {
    params: { reportId },
    responseType: 'blob'
  });
  triggerBlobDownload(response, reportId, 'md');
}

export function listReportJobs(params: ReportJobQuery) {
  return apiGet<PageResult<CampusReportJob>>('/campus/auto-report/job/list', params);
}

export function saveReportJob(data: CampusReportJob) {
  return apiPost<CampusReportJob>('/campus/auto-report/job/save', data);
}

export function updateReportJobStatus(reportJobId: ApiId, jobStatus: string) {
  return apiPost<CampusReportJob>('/campus/auto-report/job/update-status', undefined, {
    reportJobId,
    jobStatus
  });
}

export function deleteReportJob(reportJobId: ApiId) {
  return apiPost<void>('/campus/auto-report/job/delete', undefined, { reportJobId });
}

export function runReportJob(reportJobId: ApiId) {
  return apiPost<CampusReport>('/campus/auto-report/job/run', undefined, { reportJobId });
}

export function listReportGenerationLogs(reportJobId: ApiId) {
  return apiGet<CampusReportGenerationLog[]>('/campus/auto-report/log/list', { reportJobId });
}
