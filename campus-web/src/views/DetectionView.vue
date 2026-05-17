<template>
  <section class="business-page">
    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="检测主题" name="topics">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="topicQuery.keyword" clearable placeholder="主题/关键词" @keyup.enter="loadTopics">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input v-model.trim="topicQuery.topicCategory" clearable placeholder="主题分类" @keyup.enter="loadTopics" />
              <el-select v-model="topicQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadTopics">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openTopicCreate">
              <Plus :size="16" />
              新增主题
            </el-button>
          </div>

          <el-table :data="topics" v-loading="topicLoading" size="small" height="560">
            <el-table-column prop="topicName" label="主题名称" min-width="170" show-overflow-tooltip />
            <el-table-column prop="topicCategory" label="分类" width="110" show-overflow-tooltip />
            <el-table-column prop="keywords" label="关键词" min-width="180" show-overflow-tooltip />
            <el-table-column prop="excludeWords" label="排除词" width="150" show-overflow-tooltip />
            <el-table-column prop="platformScope" label="平台范围" width="130" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="风险" width="82">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTopicEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="danger" @click="submitTopicDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="topicQuery.pageNum"
              v-model:page-size="topicQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="topicTotal"
              @size-change="loadTopics"
              @current-change="loadTopics"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="检测规则" name="rules">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input-number v-model="ruleQuery.topicId" :min="1" controls-position="right" placeholder="主题ID" />
              <el-select v-model="ruleQuery.ruleType" clearable placeholder="规则类型">
                <el-option label="任一关键词" value="keyword_any" />
                <el-option label="全部关键词" value="keyword_all" />
                <el-option label="精确匹配" value="exact" />
                <el-option label="正则表达式" value="regex" />
                <el-option label="风险等级" value="risk_level" />
              </el-select>
              <el-select v-model="ruleQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadRules">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openRuleCreate">
              <Plus :size="16" />
              新增规则
            </el-button>
          </div>

          <el-table :data="rules" v-loading="ruleLoading" size="small" height="560">
            <el-table-column prop="ruleName" label="规则名称" min-width="170" show-overflow-tooltip />
            <el-table-column prop="topicId" label="主题ID" width="90" />
            <el-table-column prop="ruleType" label="类型" width="118">
              <template #default="{ row }">{{ ruleTypeLabel(row.ruleType) }}</template>
            </el-table-column>
            <el-table-column prop="ruleCondition" label="规则条件" min-width="190" show-overflow-tooltip />
            <el-table-column prop="excludeWords" label="排除词" width="140" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="风险" width="82">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openRuleEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="danger" @click="submitRuleDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="ruleQuery.pageNum"
              v-model:page-size="ruleQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="ruleTotal"
              @size-change="loadRules"
              @current-change="loadRules"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="检测任务" name="tasks">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="taskQuery.keyword" clearable placeholder="任务名称" @keyup.enter="loadTasks">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input-number v-model="taskQuery.topicId" :min="1" controls-position="right" placeholder="主题ID" />
              <el-select v-model="taskQuery.taskStatus" clearable placeholder="状态">
                <el-option label="运行中" value="active" />
                <el-option label="暂停" value="paused" />
                <el-option label="停用" value="disabled" />
              </el-select>
              <el-button @click="loadTasks">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openTaskCreate">
              <Plus :size="16" />
              新增任务
            </el-button>
          </div>

          <el-table :data="tasks" v-loading="taskLoading" size="small" height="560">
            <el-table-column prop="taskName" label="任务名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="topicId" label="主题ID" width="90" />
            <el-table-column prop="objectTypes" label="扫描对象" width="145" show-overflow-tooltip />
            <el-table-column prop="scanWindowHours" label="窗口(小时)" width="100" />
            <el-table-column prop="autoAlert" label="自动预警" width="92">
              <template #default="{ row }">{{ row.autoAlert === 0 ? '否' : '是' }}</template>
            </el-table-column>
            <el-table-column prop="taskStatus" label="状态" width="96">
              <template #default="{ row }">
                <el-tag :type="taskStatusTagType(row.taskStatus)" effect="plain">
                  {{ taskStatusLabel(row.taskStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastRunTime" label="最近运行" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="370" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTaskEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="success" @click="submitRunTask(row)">
                  <Play :size="15" />
                  运行
                </el-button>
                <el-button link type="warning" @click="toggleTaskStatus(row)">
                  <PauseCircle :size="15" />
                  {{ row.taskStatus === 'active' ? '暂停' : '启用' }}
                </el-button>
                <el-button link type="info" @click="openRunLogs(row)">
                  <ListChecks :size="15" />
                  日志
                </el-button>
                <el-button link type="danger" @click="submitTaskDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="taskQuery.pageNum"
              v-model:page-size="taskQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="taskTotal"
              @size-change="loadTasks"
              @current-change="loadTasks"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="命中结果" name="hits">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="hitQuery.keyword" clearable placeholder="标题/内容/命中词" @keyup.enter="loadHits">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input-number v-model="hitQuery.detectionTaskId" :min="1" controls-position="right" placeholder="任务ID" />
              <el-select v-model="hitQuery.objectType" clearable placeholder="对象">
                <el-option label="接入记录" value="ingest_record" />
                <el-option label="线索" value="clue" />
                <el-option label="账号动态" value="account_content" />
              </el-select>
              <el-select v-model="hitQuery.hitStatus" clearable placeholder="状态">
                <el-option label="待处理" value="pending" />
                <el-option label="已预警" value="alerted" />
                <el-option label="已忽略" value="ignored" />
              </el-select>
              <el-button @click="loadHits">
                <Search :size="16" />
                查询
              </el-button>
            </div>
          </div>

          <el-table :data="hits" v-loading="hitLoading" size="small" height="560">
            <el-table-column prop="objectTitle" label="命中标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="objectType" label="对象" width="110">
              <template #default="{ row }">{{ objectTypeLabel(row.objectType) }}</template>
            </el-table-column>
            <el-table-column prop="platform" label="平台" width="100" show-overflow-tooltip />
            <el-table-column prop="matchedKeywords" label="命中词" width="150" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="风险" width="82">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="hitStatus" label="状态" width="92">
              <template #default="{ row }">
                <el-tag :type="hitStatusTagType(row.hitStatus)" effect="plain">{{ hitStatusLabel(row.hitStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="命中时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="178" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="row.hitStatus === 'alerted'" @click="submitAlertHit(row)">
                  <BellRing :size="15" />
                  转预警
                </el-button>
                <el-button link type="info" :disabled="row.hitStatus === 'ignored'" @click="submitIgnoreHit(row)">
                  <CircleOff :size="15" />
                  忽略
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="hitQuery.pageNum"
              v-model:page-size="hitQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="hitTotal"
              @size-change="loadHits"
              @current-change="loadHits"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="topicDialogVisible" :title="topicForm.topicId ? '编辑检测主题' : '新增检测主题'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="主题名称" required>
            <el-input v-model.trim="topicForm.topicName" />
          </el-form-item>
          <el-form-item label="主题分类">
            <el-input v-model.trim="topicForm.topicCategory" />
          </el-form-item>
        </div>
        <el-form-item label="关键词">
          <el-input v-model.trim="topicForm.keywords" placeholder="多个词用逗号分隔" />
        </el-form-item>
        <el-form-item label="排除词">
          <el-input v-model.trim="topicForm.excludeWords" placeholder="用于过滤误报，多个词用逗号分隔" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="平台范围">
            <el-input v-model.trim="topicForm.platformScope" placeholder="如 weibo,douyin,forum" />
          </el-form-item>
          <el-form-item label="来源范围">
            <el-input v-model.trim="topicForm.sourceScope" placeholder="限定接入来源或校内业务范围" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="风险等级">
            <el-select v-model="topicForm.riskLevel">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="topicEnabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item label="说明">
          <el-input v-model.trim="topicForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitTopic">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ruleDialogVisible" :title="ruleForm.ruleId ? '编辑检测规则' : '新增检测规则'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="主题ID" required>
            <el-input-number v-model="ruleForm.topicId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="规则名称" required>
            <el-input v-model.trim="ruleForm.ruleName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="规则类型">
            <el-select v-model="ruleForm.ruleType">
              <el-option label="任一关键词" value="keyword_any" />
              <el-option label="全部关键词" value="keyword_all" />
              <el-option label="精确匹配" value="exact" />
              <el-option label="正则表达式" value="regex" />
              <el-option label="风险等级" value="risk_level" />
            </el-select>
          </el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="ruleForm.riskLevel">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="规则条件" required>
          <el-input v-model.trim="ruleForm.ruleCondition" placeholder="关键词、短语、正则表达式或风险等级值" />
        </el-form-item>
        <el-form-item label="排除词">
          <el-input v-model.trim="ruleForm.excludeWords" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="排序">
            <el-input-number v-model="ruleForm.sortNo" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="ruleEnabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item label="说明">
          <el-input v-model.trim="ruleForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRule">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskDialogVisible" :title="taskForm.detectionTaskId ? '编辑检测任务' : '新增检测任务'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="主题ID" required>
            <el-input-number v-model="taskForm.topicId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="任务名称" required>
            <el-input v-model.trim="taskForm.taskName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="扫描对象">
            <el-select v-model="objectTypeValues" multiple collapse-tags collapse-tags-tooltip>
              <el-option label="接入记录" value="ingest_record" />
              <el-option label="线索" value="clue" />
              <el-option label="账号动态" value="account_content" />
            </el-select>
          </el-form-item>
          <el-form-item label="扫描窗口(小时)">
            <el-input-number v-model="taskForm.scanWindowHours" :min="1" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="任务状态">
            <el-select v-model="taskForm.taskStatus">
              <el-option label="运行中" value="active" />
              <el-option label="暂停" value="paused" />
              <el-option label="停用" value="disabled" />
            </el-select>
          </el-form-item>
          <el-form-item label="自动转预警">
            <el-switch v-model="taskAutoAlert" active-text="开启" inactive-text="关闭" />
          </el-form-item>
        </div>
        <el-form-item label="说明">
          <el-input v-model.trim="taskForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitTask">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="runLogVisible" title="检测运行日志" width="760px">
      <el-table :data="runLogs" v-loading="runLogLoading" size="small" max-height="420">
        <el-table-column prop="runStatus" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.runStatus === 'success' ? 'success' : row.runStatus === 'failed' ? 'danger' : 'warning'" effect="plain">
              {{ runStatusLabel(row.runStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scannedCount" label="扫描" width="86" />
        <el-table-column prop="hitCount" label="命中" width="86" />
        <el-table-column prop="alertCount" label="预警" width="86" />
        <el-table-column prop="startTime" label="开始时间" width="168" show-overflow-tooltip />
        <el-table-column prop="endTime" label="结束时间" width="168" show-overflow-tooltip />
        <el-table-column prop="errorMessage" label="错误信息" min-width="160" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  BellRing,
  CircleOff,
  ListChecks,
  PauseCircle,
  Pencil,
  Play,
  Plus,
  Search,
  Trash2
} from 'lucide-vue-next';
import {
  alertDetectionHit,
  deleteDetectionRule,
  deleteDetectionTask,
  deleteDetectionTopic,
  ignoreDetectionHit,
  listDetectionHits,
  listDetectionRules,
  listDetectionRunLogs,
  listDetectionTasks,
  listDetectionTopics,
  runDetectionTask,
  saveDetectionRule,
  saveDetectionTask,
  saveDetectionTopic,
  updateDetectionTaskStatus
} from '../services/detectionIngest';
import { CAMPUS_RISK_OPTIONS, campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type {
  CampusDetectionHit,
  CampusDetectionRule,
  CampusDetectionRunLog,
  CampusDetectionTask,
  CampusDetectionTopic
} from '../types/api';

const activeTab = ref('topics');
const saving = ref(false);
const topicLoading = ref(false);
const ruleLoading = ref(false);
const taskLoading = ref(false);
const hitLoading = ref(false);
const runLogLoading = ref(false);
const topicDialogVisible = ref(false);
const ruleDialogVisible = ref(false);
const taskDialogVisible = ref(false);
const runLogVisible = ref(false);

const topics = ref<CampusDetectionTopic[]>([]);
const rules = ref<CampusDetectionRule[]>([]);
const tasks = ref<CampusDetectionTask[]>([]);
const hits = ref<CampusDetectionHit[]>([]);
const runLogs = ref<CampusDetectionRunLog[]>([]);
const topicTotal = ref(0);
const ruleTotal = ref(0);
const taskTotal = ref(0);
const hitTotal = ref(0);

const topicQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  topicCategory: '',
  enabled: undefined as number | undefined
});
const ruleQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  topicId: undefined as number | undefined,
  ruleType: '',
  enabled: undefined as number | undefined
});
const taskQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  topicId: undefined as number | undefined,
  taskStatus: ''
});
const hitQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  detectionTaskId: undefined as number | undefined,
  topicId: undefined as number | undefined,
  objectType: '',
  hitStatus: '',
  riskLevel: ''
});

const topicForm = reactive<CampusDetectionTopic>({
  topicName: '',
  topicCategory: '',
  keywords: '',
  excludeWords: '',
  platformScope: '',
  sourceScope: '',
  riskLevel: 'normal',
  enabled: 1,
  description: ''
});
const ruleForm = reactive<CampusDetectionRule>({
  topicId: undefined,
  ruleName: '',
  ruleType: 'keyword_any',
  ruleCondition: '',
  excludeWords: '',
  riskLevel: 'normal',
  enabled: 1,
  sortNo: 0,
  description: ''
});
const taskForm = reactive<CampusDetectionTask>({
  topicId: undefined,
  taskName: '',
  objectTypes: 'ingest_record,clue,account_content',
  taskStatus: 'active',
  scanWindowHours: 24,
  autoAlert: 1,
  description: ''
});

const topicEnabled = computed({
  get: () => topicForm.enabled !== 0,
  set: (value: boolean) => {
    topicForm.enabled = value ? 1 : 0;
  }
});
const ruleEnabled = computed({
  get: () => ruleForm.enabled !== 0,
  set: (value: boolean) => {
    ruleForm.enabled = value ? 1 : 0;
  }
});
const taskAutoAlert = computed({
  get: () => taskForm.autoAlert !== 0,
  set: (value: boolean) => {
    taskForm.autoAlert = value ? 1 : 0;
  }
});
const objectTypeValues = computed({
  get: () => (taskForm.objectTypes || '').split(',').map((item) => item.trim()).filter(Boolean),
  set: (value: string[]) => {
    taskForm.objectTypes = value.join(',');
  }
});

onMounted(async () => {
  await loadTopics();
});

watch(activeTab, (tab) => {
  if (tab === 'rules') {
    loadRules();
  }
  if (tab === 'tasks') {
    loadTasks();
  }
  if (tab === 'hits') {
    loadHits();
  }
});

async function loadTopics() {
  topicLoading.value = true;
  try {
    const page = await listDetectionTopics(topicQuery);
    topics.value = page.list || [];
    topicTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '检测主题加载失败');
  } finally {
    topicLoading.value = false;
  }
}

async function loadRules() {
  ruleLoading.value = true;
  try {
    const page = await listDetectionRules(ruleQuery);
    rules.value = page.list || [];
    ruleTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '检测规则加载失败');
  } finally {
    ruleLoading.value = false;
  }
}

async function loadTasks() {
  taskLoading.value = true;
  try {
    const page = await listDetectionTasks(taskQuery);
    tasks.value = page.list || [];
    taskTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '检测任务加载失败');
  } finally {
    taskLoading.value = false;
  }
}

async function loadHits() {
  hitLoading.value = true;
  try {
    const page = await listDetectionHits(hitQuery);
    hits.value = page.list || [];
    hitTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '命中结果加载失败');
  } finally {
    hitLoading.value = false;
  }
}

function resetTopicForm() {
  Object.assign(topicForm, {
    topicId: undefined,
    topicName: '',
    topicCategory: '',
    keywords: '',
    excludeWords: '',
    platformScope: '',
    sourceScope: '',
    riskLevel: 'normal',
    enabled: 1,
    description: ''
  });
}

function openTopicCreate() {
  resetTopicForm();
  topicDialogVisible.value = true;
}

function openTopicEdit(row: CampusDetectionTopic) {
  Object.assign(topicForm, row);
  topicDialogVisible.value = true;
}

async function submitTopic() {
  if (!topicForm.topicName) {
    ElMessage.warning('主题名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveDetectionTopic({ ...topicForm });
    ElMessage.success('检测主题已保存');
    topicDialogVisible.value = false;
    await loadTopics();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitTopicDelete(row: CampusDetectionTopic) {
  if (!row.topicId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该检测主题？相关规则和任务请先确认不再使用。', '删除确认', { type: 'warning' });
    await deleteDetectionTopic(row.topicId);
    ElMessage.success('检测主题已删除');
    await loadTopics();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function resetRuleForm() {
  Object.assign(ruleForm, {
    ruleId: undefined,
    topicId: ruleQuery.topicId || undefined,
    ruleName: '',
    ruleType: 'keyword_any',
    ruleCondition: '',
    excludeWords: '',
    riskLevel: 'normal',
    enabled: 1,
    sortNo: 0,
    description: ''
  });
}

function openRuleCreate() {
  resetRuleForm();
  ruleDialogVisible.value = true;
}

function openRuleEdit(row: CampusDetectionRule) {
  Object.assign(ruleForm, row);
  ruleDialogVisible.value = true;
}

async function submitRule() {
  if (!ruleForm.topicId || !ruleForm.ruleName || !ruleForm.ruleCondition) {
    ElMessage.warning('主题ID、规则名称和规则条件不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveDetectionRule({ ...ruleForm });
    ElMessage.success('检测规则已保存');
    ruleDialogVisible.value = false;
    await loadRules();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitRuleDelete(row: CampusDetectionRule) {
  if (!row.ruleId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该检测规则？', '删除确认', { type: 'warning' });
    await deleteDetectionRule(row.ruleId);
    ElMessage.success('检测规则已删除');
    await loadRules();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function resetTaskForm() {
  Object.assign(taskForm, {
    detectionTaskId: undefined,
    topicId: taskQuery.topicId || undefined,
    taskName: '',
    objectTypes: 'ingest_record,clue,account_content',
    taskStatus: 'active',
    scanWindowHours: 24,
    autoAlert: 1,
    description: ''
  });
}

function openTaskCreate() {
  resetTaskForm();
  taskDialogVisible.value = true;
}

function openTaskEdit(row: CampusDetectionTask) {
  Object.assign(taskForm, row);
  taskDialogVisible.value = true;
}

async function submitTask() {
  if (!taskForm.topicId || !taskForm.taskName) {
    ElMessage.warning('主题ID和任务名称不能为空');
    return;
  }
  if (!taskForm.objectTypes) {
    ElMessage.warning('至少选择一个扫描对象');
    return;
  }
  saving.value = true;
  try {
    await saveDetectionTask({ ...taskForm });
    ElMessage.success('检测任务已保存');
    taskDialogVisible.value = false;
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitRunTask(row: CampusDetectionTask) {
  if (!row.detectionTaskId) {
    return;
  }
  try {
    const result = await runDetectionTask(row.detectionTaskId);
    ElMessage.success(`运行完成：扫描 ${result.scannedCount || 0} 条，命中 ${result.hitCount || 0} 条`);
    await Promise.all([loadTasks(), loadHits()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '运行失败');
  }
}

async function toggleTaskStatus(row: CampusDetectionTask) {
  if (!row.detectionTaskId) {
    return;
  }
  const nextStatus = row.taskStatus === 'active' ? 'paused' : 'active';
  try {
    await updateDetectionTaskStatus(row.detectionTaskId, nextStatus);
    ElMessage.success('任务状态已更新');
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function submitTaskDelete(row: CampusDetectionTask) {
  if (!row.detectionTaskId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该检测任务？', '删除确认', { type: 'warning' });
    await deleteDetectionTask(row.detectionTaskId);
    ElMessage.success('检测任务已删除');
    await loadTasks();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

async function openRunLogs(row: CampusDetectionTask) {
  if (!row.detectionTaskId) {
    return;
  }
  runLogVisible.value = true;
  runLogLoading.value = true;
  try {
    const page = await listDetectionRunLogs({ pageNum: 1, pageSize: 20, detectionTaskId: row.detectionTaskId });
    runLogs.value = page.list || [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '运行日志加载失败');
  } finally {
    runLogLoading.value = false;
  }
}

async function submitAlertHit(row: CampusDetectionHit) {
  if (!row.hitId) {
    return;
  }
  try {
    await alertDetectionHit(row.hitId);
    ElMessage.success('已转为预警');
    await loadHits();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '转预警失败');
  }
}

async function submitIgnoreHit(row: CampusDetectionHit) {
  if (!row.hitId) {
    return;
  }
  try {
    await ignoreDetectionHit(row.hitId);
    ElMessage.success('命中已忽略');
    await loadHits();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '忽略失败');
  }
}

function riskLabel(value?: string) {
  return campusRiskLabel(value);
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}

function ruleTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    keyword_any: '任一关键词',
    keyword_all: '全部关键词',
    exact: '精确匹配',
    regex: '正则表达式',
    risk_level: '风险等级'
  };
  return labels[value || 'keyword_any'] || value || '任一关键词';
}

function taskStatusLabel(value?: string) {
  const labels: Record<string, string> = { active: '运行中', paused: '暂停', disabled: '停用' };
  return labels[value || 'active'] || value || '运行中';
}

function taskStatusTagType(value?: string) {
  if (value === 'active') {
    return 'success';
  }
  if (value === 'paused') {
    return 'warning';
  }
  return 'info';
}

function objectTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    ingest_record: '接入记录',
    clue: '线索',
    account_content: '账号动态'
  };
  return labels[value || 'ingest_record'] || value || '接入记录';
}

function hitStatusLabel(value?: string) {
  const labels: Record<string, string> = { pending: '待处理', alerted: '已预警', ignored: '已忽略' };
  return labels[value || 'pending'] || value || '待处理';
}

function hitStatusTagType(value?: string) {
  if (value === 'alerted') {
    return 'success';
  }
  if (value === 'ignored') {
    return 'info';
  }
  return 'warning';
}

function runStatusLabel(value?: string) {
  const labels: Record<string, string> = { running: '运行中', success: '成功', failed: '失败' };
  return labels[value || 'running'] || value || '运行中';
}
</script>
