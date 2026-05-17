export interface ApiResponse<T> {
  code: number;
  msg: string;
  data: T;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

export interface OverviewStats {
  todayClueCount?: number;
  pendingClueCount?: number;
  activeEventCount?: number;
  highRiskEventCount?: number;
  pendingAlertCount?: number;
  overdueDisposalCount?: number;
  activeAccountCount?: number;
}

export interface DistributionItem {
  name: string;
  value: number;
}

export interface DashboardTrendItem {
  name: string;
  clueCount?: number;
  alertCount?: number;
  hitCount?: number;
  eventCount?: number;
}

export interface WordCloudItem {
  name: string;
  value: number;
}

export interface DashboardTrendPoint {
  date: string;
  clueCount: number;
  alertCount: number;
}

export interface MonitorDashboardOverview {
  activeTaskCount?: number;
  scheduledTaskCount?: number;
  todayResultCount?: number;
  pendingAlertCount?: number;
  todayAlertCount?: number;
}

export interface MonitorTrendItem {
  name: string;
  monitorResultCount?: number;
  monitorAlertCount?: number;
}

export interface SourceRiskDistributionItem {
  name: string;
  totalCount?: number;
  normalCount?: number;
  concernCount?: number;
  majorCount?: number;
  urgentCount?: number;
}

export interface GovernanceMetrics {
  overdueTaskCount?: number;
  dueSoonTaskCount?: number;
  reviewedEventCount?: number;
  todayArchivedEventCount?: number;
  pendingAlertCount?: number;
}

export interface DashboardStatistics {
  overview: OverviewStats;
  monitorOverview?: MonitorDashboardOverview;
  riskDistribution: DistributionItem[];
  clueSourceDistribution: DistributionItem[];
  eventStatusDistribution: DistributionItem[];
  trendByDay?: DashboardTrendItem[];
  monitorTrendByDay?: MonitorTrendItem[];
  alertRiskDistribution?: DistributionItem[];
  detectionHitRiskDistribution?: DistributionItem[];
  sourceRiskDistribution?: SourceRiskDistributionItem[];
  topicRiskDistribution?: SourceRiskDistributionItem[];
  governanceMetrics?: GovernanceMetrics;
}

export interface CampusAlert {
  alertId: number;
  alertTitle: string;
  alertContent?: string;
  alertSource?: string;
  sourceObjectId?: number;
  riskLevel: string;
  alertStatus: string;
  handleOpinion?: string;
  matchedKeywords?: string;
  evidenceJson?: string;
  createTime?: string;
}

export interface MailConfig {
  host?: string;
  port?: string;
  username?: string;
  password?: string;
  to?: string;
  cc?: string[];
  toList?: string[];
}

export interface CampusMonitorOverview {
  activeTaskCount?: number;
  todayResultCount?: number;
  pendingAlertCount?: number;
}

export interface CampusMonitorTask {
  id?: number;
  monitorTaskId?: number;
  taskName: string;
  monitorSubject: string;
  subjectAliases?: string;
  keywords?: string;
  keywordsI18n?: string;
  negativeWords?: string;
  negativeWordsI18n?: string;
  excludeWords?: string;
  excludeWordsI18n?: string;
  platformScope?: string;
  scanFrequencyMinutes?: number;
  scheduleEnabled?: number;
  displayEnabled?: number;
  autoIngestEnabled?: number;
  alertMode?: string;
  taskStatus?: string;
  lastRunTime?: string;
  lastRunLogId?: number;
  lastCollectTime?: string;
  lastMatchCount?: number;
  displayResultCount?: number;
  lastErrorMessage?: string;
  ingestCapabilityStatus?: string;
  nextRunTime?: string;
  scheduleLockUntil?: string;
  ingestTaskIds?: string;
  ingestTaskNames?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusMonitorResult {
  id?: number;
  monitorResultId?: number;
  monitorTaskId?: number;
  ingestRecordId?: number;
  title?: string;
  content?: string;
  originalUrl?: string;
  platform?: string;
  authorName?: string;
  publishTime?: string;
  language?: string;
  matchedSubjects?: string;
  matchedKeywords?: string;
  matchedNegativeWords?: string;
  sentiment?: string;
  riskLevel?: string;
  riskScore?: number;
  schoolRelevanceScore?: number;
  schoolRelevanceReason?: string;
  matchedSchoolTerms?: string;
  excludedReason?: string;
  topicCategory?: string;
  topicSubCategory?: string;
  topicReason?: string;
  resultStatus?: string;
  alertId?: number;
  clueId?: number;
  likeCount?: number;
  commentCount?: number;
  shareCount?: number;
  collectCount?: number;
  viewCount?: number;
  createTime?: string;
}

export interface CampusMonitorInformation {
  infoType?: 'monitor_result' | 'clue';
  infoId?: number;
  monitorResultId?: number;
  clueId?: number;
  monitorTaskId?: number;
  ingestRecordId?: number;
  title?: string;
  content?: string;
  summary?: string;
  contentCaptureStatus?: string;
  contentCaptureLabel?: string;
  originalUrl?: string;
  platform?: string;
  sourcePlatform?: string;
  sourceSubPlatform?: string;
  authorName?: string;
  involvedAccount?: string;
  publishTime?: string | Date;
  collectTime?: string | Date;
  publishTimeStatus?: 'known' | 'missing' | 'inferred' | string;
  discoverTime?: string | Date;
  createTime?: string | Date;
  infoTime?: string | Date;
  language?: string;
  matchedSubjects?: string;
  matchedKeywords?: string;
  matchedNegativeWords?: string;
  keywords?: string;
  sentiment?: string;
  riskLevel?: string;
  riskScore?: number;
  schoolRelevanceScore?: number;
  schoolRelevanceReason?: string;
  matchedSchoolTerms?: string;
  excludedReason?: string;
  topicCategory?: string;
  topicSubCategory?: string;
  topicReason?: string;
  resultStatus?: string;
  clueStatus?: string;
  alertId?: number;
  riskMarked?: boolean;
  likeCount?: number;
  commentCount?: number;
  shareCount?: number;
  collectCount?: number;
  viewCount?: number;
}

export interface CampusMonitorWatchTarget {
  id?: number;
  targetId?: number;
  monitorTaskId?: number;
  targetType: 'account' | 'link';
  platform?: string;
  accountId?: number;
  accountName?: string;
  accountUid?: string;
  linkUrl?: string;
  sourceObjectType?: string;
  sourceObjectId?: number;
  authorizationScope?: string;
  keywordScope?: string;
  targetStatus?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusMonitorRunLog {
  id?: number;
  runLogId?: number;
  monitorTaskId?: number;
  runStatus?: string;
  triggerType?: string;
  startTime?: string;
  endTime?: string;
  scannedCount?: number;
  matchCount?: number;
  negativeCount?: number;
  alertCount?: number;
  errorMessage?: string;
  schedulerNode?: string;
  createTime?: string;
}

export interface CampusDetectionHit {
  hitId: number;
  detectionTaskId?: number;
  topicId?: number;
  ruleId?: number;
  objectTitle: string;
  objectType: string;
  objectId?: number;
  platform?: string;
  matchedKeywords?: string;
  riskLevel: string;
  hitContent?: string;
  hitStatus: string;
  alertId?: number;
  clueId?: number;
  createTime?: string;
}

export interface CampusDetectionTopic {
  id?: number;
  topicId?: number;
  topicName: string;
  topicCategory?: string;
  keywords?: string;
  excludeWords?: string;
  platformScope?: string;
  sourceScope?: string;
  riskLevel?: string;
  responsibleDepartmentId?: number;
  enabled?: number;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusDetectionRule {
  id?: number;
  ruleId?: number;
  topicId?: number;
  ruleName: string;
  ruleType?: string;
  ruleCondition?: string;
  excludeWords?: string;
  riskLevel?: string;
  enabled?: number;
  sortNo?: number;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusDetectionTask {
  id?: number;
  detectionTaskId?: number;
  topicId?: number;
  taskName: string;
  objectTypes?: string;
  taskStatus?: string;
  scanWindowHours?: number;
  autoAlert?: number;
  lastRunTime?: string;
  nextRunTime?: string;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusDetectionRunLog {
  id?: number;
  runId?: number;
  detectionTaskId?: number;
  runStatus?: string;
  startTime?: string;
  endTime?: string;
  scannedCount?: number;
  hitCount?: number;
  alertCount?: number;
  errorMessage?: string;
  createTime?: string;
}

export interface CampusClue {
  clueId?: number;
  clueTitle: string;
  clueContent?: string;
  clueSource?: string;
  sourcePlatform?: string;
  sourceSubPlatform?: string;
  originalUrl?: string;
  url?: string;
  publishTime?: string | Date;
  collectTime?: string;
  riskLevel?: string;
  schoolRelevanceScore?: number;
  schoolRelevanceReason?: string;
  matchedSchoolTerms?: string;
  excludedReason?: string;
  topicCategory?: string;
  topicSubCategory?: string;
  topicReason?: string;
  sentiment?: string;
  involvedAccount?: string;
  keywords?: string;
  matchedKeywords?: string;
  clueStatus?: string;
  articleStatus?: string;
  discoverTime?: string | Date;
  judgeOpinion?: string;
  eventId?: number;
  remark?: string;
  title?: string;
  summary?: string;
  language?: string;
  translationText?: string;
}

export interface ClueAdvancedQuery {
  keyword?: string;
  sentiment?: string;
  articleStatus?: string;
  publishTimeStart?: string;
  publishTimeEnd?: string;
  collectTimeStart?: string;
  collectTimeEnd?: string;
  matchScope?: string;
  similarDedup?: boolean;
  sortBy?: string;
  hitScope?: string;
  sourcePlatform?: string;
  sourceSubPlatform?: string;
  pageNum?: number;
  pageSize?: number;
  riskLevel?: string;
  clueStatus?: string;
  resultStatus?: string;
  language?: string;
}

export interface CampusDepartment {
  id?: number;
  departmentId?: number;
  parentId?: number;
  departmentName: string;
  departmentCode?: string;
  departmentType?: string;
  leaderUserId?: number;
  contactPhone?: string;
  sortNo?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface CampusDictType {
  id?: number;
  dictType: string;
  dictName: string;
  description?: string;
  sortNo?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface CampusDictItem {
  id?: number;
  dictType: string;
  itemCode: string;
  itemName: string;
  itemValue?: string;
  description?: string;
  sortNo?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAuditLog {
  auditId: number;
  operatorUserId?: number;
  operatorName?: string;
  operationType?: string;
  moduleName?: string;
  objectType?: string;
  objectId?: string;
  requestMethod?: string;
  requestUri?: string;
  requestIp?: string;
  operationResult?: number;
  failureReason?: string;
  taskNo?: string;
  createTime?: string;
}

export interface CampusAccount {
  id?: number;
  accountId?: number;
  platform: string;
  accountName: string;
  accountUid?: string;
  homepageUrl?: string;
  accountType?: string;
  relatedPersonDesc?: string;
  relatedDepartmentId?: number;
  sourceBasis: string;
  taskNo: string;
  authorizationScope: string;
  focusStartTime?: string | Date;
  focusEndTime?: string | Date;
  focusLevel?: string;
  responsibleDepartmentId?: number;
  responsibleUserId?: number;
  auditStatus?: string;
  auditOpinion?: string;
  auditUserId?: number;
  auditTime?: string;
  accountStatus?: string;
  tags?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAccountContent {
  id?: number;
  contentId?: number;
  accountId?: number;
  taskId?: number;
  platform?: string;
  contentType?: string;
  contentTitle?: string;
  contentText?: string;
  originalUrl?: string;
  publishTime?: string | Date;
  captureTime?: string | Date;
  riskLevel?: string;
  sentiment?: string;
  keywords?: string;
  likeCount?: number;
  commentCount?: number;
  shareCount?: number;
  collectCount?: number;
  viewCount?: number;
  clueId?: number;
  createTime?: string;
}

export interface CampusIngestSource {
  id?: number;
  sourceId?: number;
  sourceName: string;
  sourceType?: string;
  platform?: string;
  accessEndpoint?: string;
  authorizationBasis: string;
  authorizationScope: string;
  responsibleDepartmentId?: number;
  enabled?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusIngestTask {
  id?: number;
  taskId?: number;
  sourceId?: number;
  taskName: string;
  targetType?: string;
  adapterType?: string;
  scheduleCron?: string;
  scheduleEnabled?: number;
  fetchConfig?: string;
  taskStatus?: string;
  lastRunTime?: string;
  nextRunTime?: string;
  scheduleLockUntil?: string;
  maxRetryCount?: number;
  retryIntervalMinutes?: number;
  consecutiveFailCount?: number;
  currentRetryCount?: number;
  lastErrorType?: string;
  autoDetectEnabled?: number;
  detectionTaskIds?: string;
  dailyQuotaLimit?: number;
  dailyQuotaUsed?: number;
  quotaStatDate?: string;
  autoPauseAfterFailCount?: number;
  governanceRemark?: string;
  authorizationScope: string;
  retentionDays?: number;
  createTime?: string;
  updateTime?: string;
}

export interface CampusIngestRecord {
  id?: number;
  recordId?: number;
  runId?: number;
  sourceId?: number;
  taskId?: number;
  externalId?: string;
  contentHash?: string;
  platform?: string;
  contentType?: string;
  title?: string;
  content?: string;
  originalUrl?: string;
  publishTime?: string | Date;
  authorName?: string;
  accountId?: number;
  accountTaskId?: number;
  keywords?: string;
  riskLevel?: string;
  sentiment?: string;
  likeCount?: number;
  commentCount?: number;
  shareCount?: number;
  collectCount?: number;
  viewCount?: number;
  rawData?: string;
  normalizedStatus?: string;
  targetType?: string;
  targetId?: number;
  errorMessage?: string;
  language?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusIngestRunLog {
  id?: number;
  runId?: number;
  taskId?: number;
  runStatus?: string;
  triggerType?: string;
  startTime?: string;
  endTime?: string;
  durationMs?: number;
  fetchedCount?: number;
  successCount?: number;
  duplicateCount?: number;
  invalidCount?: number;
  detectionTriggerCount?: number;
  detectionHitCount?: number;
  detectionAlertCount?: number;
  detectionErrorMessage?: string;
  failCount?: number;
  errorMessage?: string;
  errorType?: string;
  retryCount?: number;
  schedulerNode?: string;
  createTime?: string;
}

export interface CampusIngestApiCallLog {
  id?: number;
  callId?: number;
  runId?: number;
  taskId?: number;
  sourceId?: number;
  provider?: string;
  endpointKey?: string;
  credentialRef?: string;
  requestTime?: string;
  durationMs?: number;
  callStatus?: string;
  httpStatus?: number;
  errorType?: string;
  errorMessage?: string;
  costUnits?: number;
  createTime?: string;
}

export interface CampusAiProvider {
  id?: number;
  providerId?: number;
  providerCode: string;
  providerName: string;
  providerType?: string;
  baseUrl?: string;
  authType?: string;
  credentialRef?: string;
  enabled?: number;
  timeoutMs?: number;
  maxRetries?: number;
  dailyQuotaLimit?: number;
  quotaUsedToday?: number;
  quotaStatDate?: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAiModel {
  id?: number;
  modelId?: number;
  providerCode: string;
  modelCode: string;
  modelName: string;
  contextLength?: number;
  defaultTemperature?: number;
  defaultMaxTokens?: number;
  supportStream?: number;
  enabled?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAiFeatureBinding {
  id?: number;
  bindingId?: number;
  featureCode: string;
  featureName: string;
  featureType?: string;
  providerCode?: string;
  modelCode?: string;
  fallbackProviderCode?: string;
  fallbackModelCode?: string;
  enabled?: number;
  failureStrategy?: string;
  timeoutMs?: number;
  dailyQuotaLimit?: number;
  quotaUsedToday?: number;
  quotaStatDate?: string;
  logPrompt?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAiPromptTemplate {
  id?: number;
  templateId?: number;
  featureCode: string;
  templateName: string;
  templateVersion?: string;
  systemPrompt?: string;
  userPrompt?: string;
  outputFormat?: string;
  enabled?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAiCallLog {
  id?: number;
  callId?: number;
  featureCode?: string;
  providerCode?: string;
  modelCode?: string;
  endpoint?: string;
  requestTime?: string;
  durationMs?: number;
  callStatus?: string;
  httpStatus?: number;
  errorType?: string;
  errorMessage?: string;
  promptTokens?: number;
  completionTokens?: number;
  totalTokens?: number;
  quotaUnits?: number;
  requestSnapshot?: string;
  responseSnapshot?: string;
  createTime?: string;
}

export interface CampusAiOverview {
  activeProviderCount?: number;
  enabledFeatureCount?: number;
  legacyFeatureCount?: number;
  failedCallCount24h?: number;
  callStatus24h?: DistributionItem[];
}

export interface CampusAiProviderTestResult {
  providerCode?: string;
  providerName?: string;
  enabled?: number;
  baseUrl?: string;
  credentialRef?: string;
  credentialConfigured?: boolean;
  ready?: boolean;
  status?: string;
  message?: string;
}

export interface CampusPublicWebWhitelist {
  id?: number;
  whitelistId?: number;
  siteName: string;
  siteDomain: string;
  baseUrl: string;
  allowedPathPrefix?: string;
  authorizationBasis: string;
  authorizationScope: string;
  robotsPolicy?: string;
  rateLimitSeconds?: number;
  maxDepth?: number;
  responsibleDepartmentId?: number;
  enabled?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAnalysisTask {
  id?: number;
  analysisTaskId?: number;
  objectType?: string;
  objectId?: number;
  analysisType?: string;
  taskStatus?: string;
  requestPayload?: string;
  modelProvider?: string;
  modelName?: string;
  errorMessage?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusAnalysisResult {
  id?: number;
  analysisResultId?: number;
  analysisTaskId?: number;
  objectType?: string;
  objectId?: number;
  analysisType?: string;
  sentiment?: string;
  suggestedRiskLevel?: string;
  summary?: string;
  keywords?: string;
  similarObjectIds?: string;
  confidence?: number;
  resultPayload?: string;
  assistiveLabel?: string;
  adoptionStatus?: string;
  reviewerUserId?: number;
  reviewTime?: string;
  reviewOpinion?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusReportTemplate {
  id?: number;
  templateId?: number;
  templateName: string;
  reportType?: string;
  templateContent?: string;
  status?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusReport {
  id?: number;
  reportId?: number;
  reportTitle: string;
  reportType?: string;
  reportStatus?: string;
  generationMode?: 'template' | 'ai';
  templateId?: number;
  eventId?: number;
  periodStartTime?: string | Date;
  periodEndTime?: string | Date;
  reportSummary?: string;
  reportContent?: string;
  reportFormat?: string;
  fileName?: string;
  filePath?: string;
  generatedBy?: number;
  generateTime?: string;
  archiveUserId?: number;
  archiveTime?: string;
  archiveOpinion?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusReportEvent {
  id?: number;
  relationId?: number;
  reportId?: number;
  eventId?: number;
  createTime?: string;
}

export interface CampusReportJob {
  id?: number;
  reportJobId?: number;
  jobName: string;
  reportType?: string;
  generationMode?: 'template' | 'ai';
  templateId?: number;
  periodRule?: string;
  scheduleCron?: string;
  outputFormat?: string;
  jobStatus?: string;
  lastRunTime?: string;
  nextRunTime?: string;
  reviewerUserId?: number;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusReportGenerationLog {
  id?: number;
  generationLogId?: number;
  reportJobId?: number;
  reportId?: number;
  runStatus?: string;
  startTime?: string;
  endTime?: string;
  errorMessage?: string;
  createUserId?: number;
  createTime?: string;
}

export interface CampusPermissionRole {
  id?: number;
  roleId?: number;
  roleCode: string;
  roleName: string;
  roleType?: string;
  dataScope?: string;
  status?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusPermissionMenu {
  id?: number;
  menuId?: number;
  parentId?: number;
  menuCode?: string;
  menuName: string;
  menuType?: string;
  routePath?: string;
  componentPath?: string;
  permissionCode?: string;
  icon?: string;
  sortNo?: number;
  visible?: number;
  status?: number;
  children?: CampusPermissionMenu[];
}

export interface CampusPermissionApi {
  id?: number;
  apiId?: number;
  apiCode?: string;
  apiName?: string;
  moduleName?: string;
  requestMethod?: string;
  requestPath?: string;
  status?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusCurrentUser {
  userId?: number;
  username?: string;
  telephone?: string;
  organizationId?: string;
  roles?: CampusPermissionRole[];
  permissions?: string[];
  menus?: CampusPermissionMenu[];
}

export interface CampusEvent {
  id?: number;
  eventId?: number;
  eventTitle: string;
  eventType?: string;
  eventSummary?: string;
  firstPublishTime?: string | Date;
  discoverTime?: string | Date;
  riskLevel?: string;
  impactScope?: string;
  involvedDepartmentId?: number;
  currentHeat?: number;
  eventStatus?: string;
  disposalRequirement?: string;
  archiveConclusion?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusDisposalTask {
  id?: number;
  disposalTaskId?: number;
  eventId?: number;
  taskTitle: string;
  assignedDepartmentId?: number;
  assignedUserId?: number;
  disposalRequirement?: string;
  dueTime?: string | Date;
  taskStatus?: string;
  feedbackSummary?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusDisposalRecord {
  id?: number;
  recordId?: number;
  disposalTaskId?: number;
  eventId?: number;
  recordType?: string;
  recordContent?: string;
  handlerName?: string;
  handleTime?: string;
  attachmentDesc?: string;
}

export interface CampusSchoolSubject {
  id?: number;
  schoolId?: number;
  schoolName: string;
  schoolAliases?: string;
  region?: string;
  educationStage?: string;
  schoolType?: string;
  status?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CampusEducationTopicItem {
  clueId?: number;
  title?: string;
  content?: string;
  topicType?: string;
  sourcePlatform?: string;
  originalUrl?: string;
  publishTime?: string;
  discoverTime?: string;
  riskLevel?: string;
  sentiment?: string;
  keywords?: string;
}

export interface CampusSchoolSentimentRank {
  schoolId?: number;
  schoolName: string;
  region?: string;
  educationStage?: string;
  totalCount: number;
  positiveCount: number;
  neutralCount: number;
  negativeCount: number;
  highRiskCount: number;
  negativeRatio?: number;
}

export interface CampusEducationBaiduTaskRequest {
  sourceId?: number;
  taskName?: string;
  topicType?: string;
  region?: string;
  schoolName?: string;
  keyword?: string;
  topK?: number;
  credentialRef?: string;
  authorizationScope?: string;
}
