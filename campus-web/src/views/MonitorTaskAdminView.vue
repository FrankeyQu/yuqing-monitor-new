<template>
  <section class="business-page monitor-task-admin">
    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="taskQuery.keyword" clearable placeholder="任务/主体/关键词" @keyup.enter="loadTasks">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-select v-model="taskQuery.taskStatus" clearable placeholder="状态">
            <el-option label="运行中" value="active" />
            <el-option label="暂停" value="paused" />
            <el-option label="停用" value="disabled" />
          </el-select>
          <el-select v-model="taskQuery.platform" clearable placeholder="平台">
            <el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button @click="loadTasks">
            <Search :size="16" />
            查询
          </el-button>
          <el-button @click="resetTaskQuery">
            <RefreshCw :size="16" />
            重置
          </el-button>
        </div>
        <el-button type="primary" :disabled="!canMonitorOperate" @click="openTaskCreate">
          <Plus :size="16" />
          新增任务
        </el-button>
      </div>

      <el-table :data="tasks" v-loading="taskLoading" size="small" height="620">
        <el-table-column label="任务名称" min-width="210" fixed="left">
          <template #default="{ row }">
            <div class="task-title-cell">
              <span class="task-name">{{ row.taskName }}</span>
              <span class="task-meta">ID {{ row.monitorTaskId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="monitorSubject" label="监测主体" min-width="160" show-overflow-tooltip />
        <el-table-column label="关键词" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatKeywords(row) }}
          </template>
        </el-table-column>
        <el-table-column label="平台" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ platformScopeLabel(row.platformScope) }}
          </template>
        </el-table-column>
        <el-table-column label="前台展示" width="110">
          <template #default="{ row }">
            <el-switch
              :model-value="row.displayEnabled !== 0"
              :disabled="!canMonitorOperate"
              size="small"
              @change="submitTaskDisplayChange(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column label="接入状态" width="116">
          <template #default="{ row }">
            <el-tooltip v-if="row.lastErrorMessage" :content="row.lastErrorMessage" placement="top">
              <el-tag :type="ingestCapabilityTagType(row.ingestCapabilityStatus)" effect="plain">
                {{ ingestCapabilityLabel(row.ingestCapabilityStatus) }}
              </el-tag>
            </el-tooltip>
            <el-tag v-else :type="ingestCapabilityTagType(row.ingestCapabilityStatus)" effect="plain">
              {{ ingestCapabilityLabel(row.ingestCapabilityStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近命中" width="96">
          <template #default="{ row }">{{ row.lastMatchCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="展示数据" width="96">
          <template #default="{ row }">{{ row.displayResultCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="调度" width="116">
          <template #default="{ row }">
            <el-tag :type="row.scheduleEnabled === 0 ? 'info' : 'success'" effect="plain">
              {{ row.scheduleEnabled === 0 ? '手动' : `${row.scanFrequencyMinutes || 60}分钟` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警" width="118">
          <template #default="{ row }">{{ alertModeLabel(row.alertMode) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="96">
          <template #default="{ row }">
            <el-tag :type="taskStatusTagType(row.taskStatus)" effect="plain">
              {{ taskStatusLabel(row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCollectTime" label="最近采集" width="168" show-overflow-tooltip />
        <el-table-column prop="lastRunTime" label="最近运行" width="168" show-overflow-tooltip />
        <el-table-column prop="nextRunTime" label="下次运行" width="168" show-overflow-tooltip />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!canMonitorOperate" @click="openTaskEdit(row)">
              <Pencil :size="15" />
              编辑
            </el-button>
            <el-button
              link
              type="success"
              :disabled="!canMonitorOperate || row.taskStatus === 'disabled'"
              @click="submitRunTask(row)"
            >
              <Play :size="15" />
              运行
            </el-button>
            <el-dropdown :disabled="!canMonitorOperate" @command="handleTaskStatusCommand(row, $event)">
              <el-button link type="warning" :disabled="!canMonitorOperate">
                <PauseCircle :size="15" />
                状态
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="active" :disabled="row.taskStatus === 'active'">启用</el-dropdown-item>
                  <el-dropdown-item command="paused" :disabled="row.taskStatus === 'paused'">暂停</el-dropdown-item>
                  <el-dropdown-item command="disabled" :disabled="row.taskStatus === 'disabled'">停用</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link type="primary" @click="openWatchTargets(row)">
              <Target :size="15" />
              目标
            </el-button>
            <el-button link type="danger" :disabled="!canMonitorOperate" @click="submitTaskDelete(row)">
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
    </section>

    <el-dialog v-model="taskDialogVisible" :title="taskForm.monitorTaskId ? '编辑监测任务' : '新增监测任务'" width="900px">
      <el-form label-width="120px" class="admin-form">
        <div class="form-grid">
          <el-form-item label="任务名称" required>
            <el-input v-model.trim="taskForm.taskName" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="监测主体" required>
            <el-input v-model.trim="taskForm.monitorSubject" maxlength="120" show-word-limit />
          </el-form-item>
        </div>
        <el-form-item label="主体别名">
          <el-input v-model.trim="taskForm.subjectAliases" placeholder="多个别名用逗号分隔" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="平台范围">
            <el-select v-model="platformScopeValues" multiple collapse-tags collapse-tags-tooltip>
              <el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="自动采集">
            <el-switch v-model="taskAutoIngestEnabled" active-text="开启" inactive-text="关闭" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="运行间隔">
            <el-input-number v-model="taskForm.scanFrequencyMinutes" :min="5" :max="1440" controls-position="right" />
          </el-form-item>
          <el-form-item label="启用调度">
            <el-switch v-model="taskScheduleEnabled" active-text="启用" inactive-text="手动" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="前台展示">
            <el-switch v-model="taskDisplayEnabled" active-text="展示" inactive-text="隐藏" />
          </el-form-item>
          <el-form-item label="接入状态">
            <el-tag :type="ingestCapabilityTagType(taskForm.ingestCapabilityStatus)" effect="plain">
              {{ ingestCapabilityLabel(taskForm.ingestCapabilityStatus) }}
            </el-tag>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="预警模式">
            <el-select v-model="taskForm.alertMode">
              <el-option label="仅负面" value="negative_only" />
              <el-option label="风险命中" value="all_hits" />
              <el-option label="人工转预警" value="manual" />
            </el-select>
          </el-form-item>
          <el-form-item label="任务状态">
            <el-select v-model="taskForm.taskStatus">
              <el-option label="运行中" value="active" />
              <el-option label="暂停" value="paused" />
              <el-option label="停用" value="disabled" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="关键词">
          <el-input v-model.trim="taskForm.keywords" type="textarea" :rows="2" placeholder="多个词用逗号分隔" />
        </el-form-item>
        <div class="language-grid">
          <el-input v-model.trim="taskLanguageForm.keywordsZh" placeholder="中文关键词" />
          <el-input v-model.trim="taskLanguageForm.keywordsMongolian" placeholder="蒙语关键词" />
          <el-input v-model.trim="taskLanguageForm.keywordsUyghur" placeholder="维语关键词" />
        </div>
        <el-form-item label="负面词">
          <el-input v-model.trim="taskForm.negativeWords" type="textarea" :rows="2" placeholder="多个词用逗号分隔" />
        </el-form-item>
        <div class="language-grid">
          <el-input v-model.trim="taskLanguageForm.negativeZh" placeholder="中文负面词" />
          <el-input v-model.trim="taskLanguageForm.negativeMongolian" placeholder="蒙语负面词" />
          <el-input v-model.trim="taskLanguageForm.negativeUyghur" placeholder="维语负面词" />
        </div>
        <el-form-item label="排除词">
          <el-input v-model.trim="taskForm.excludeWords" type="textarea" :rows="2" placeholder="多个词用逗号分隔" />
        </el-form-item>
        <div class="language-grid">
          <el-input v-model.trim="taskLanguageForm.excludeZh" placeholder="中文排除词" />
          <el-input v-model.trim="taskLanguageForm.excludeMongolian" placeholder="蒙语排除词" />
          <el-input v-model.trim="taskLanguageForm.excludeUyghur" placeholder="维语排除词" />
        </div>
        <el-form-item label="备注">
          <el-input v-model.trim="taskForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-collapse class="advanced-panel">
          <el-collapse-item title="高级诊断" name="advanced">
            <div class="advanced-grid">
              <div>
                <span>自动接入任务</span>
                <strong>{{ taskForm.ingestTaskNames || taskForm.ingestTaskIds || '未生成' }}</strong>
              </div>
              <div>
                <span>最近错误</span>
                <strong>{{ taskForm.lastErrorMessage || '-' }}</strong>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!canMonitorOperate" @click="submitTask">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="watchTargetDialogVisible" :title="watchTargetDialogTitle" width="1050px">
      <div class="target-toolbar">
        <div class="toolbar-filters">
          <el-select v-model="watchTargetQuery.targetType" clearable placeholder="类型">
            <el-option label="账号" value="account" />
            <el-option label="链接" value="link" />
          </el-select>
          <el-select v-model="watchTargetQuery.targetStatus" clearable placeholder="状态">
            <el-option label="启用" value="active" />
            <el-option label="暂停" value="paused" />
            <el-option label="停用" value="disabled" />
          </el-select>
          <el-input v-model.trim="watchTargetQuery.keyword" clearable placeholder="账号/链接" @keyup.enter="loadWatchTargets">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-button @click="loadWatchTargets">
            <Search :size="16" />
            查询
          </el-button>
        </div>
        <el-button type="primary" :disabled="!canMonitorOperate" @click="openWatchTargetCreate">
          <Plus :size="16" />
          新增目标
        </el-button>
      </div>
      <el-table :data="watchTargets" v-loading="watchTargetLoading" size="small" height="420">
        <el-table-column label="类型" width="86">
          <template #default="{ row }">{{ targetTypeLabel(row.targetType) }}</template>
        </el-table-column>
        <el-table-column prop="platform" label="平台" width="110" show-overflow-tooltip />
        <el-table-column prop="accountName" label="账号名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="accountUid" label="账号ID" min-width="145" show-overflow-tooltip />
        <el-table-column prop="linkUrl" label="链接" min-width="220" show-overflow-tooltip />
        <el-table-column prop="authorizationScope" label="授权范围" width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="86">
          <template #default="{ row }">
            <el-tag :type="targetStatusTagType(row.targetStatus)" effect="plain">
              {{ targetStatusLabel(row.targetStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!canMonitorOperate" @click="openWatchTargetEdit(row)">
              <Pencil :size="15" />
              编辑
            </el-button>
            <el-button link type="warning" :disabled="!canMonitorOperate" @click="toggleWatchTargetStatus(row)">
              <PauseCircle :size="15" />
              {{ row.targetStatus === 'active' ? '暂停' : '启用' }}
            </el-button>
            <el-button link type="danger" :disabled="!canMonitorOperate" @click="submitWatchTargetDelete(row)">
              <Trash2 :size="15" />
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-row compact">
        <el-pagination
          v-model:current-page="watchTargetQuery.pageNum"
          v-model:page-size="watchTargetQuery.pageSize"
          layout="total, prev, pager, next"
          :total="watchTargetTotal"
          @current-change="loadWatchTargets"
        />
      </div>
    </el-dialog>

    <el-dialog v-model="watchTargetFormVisible" :title="watchTargetForm.targetId ? '编辑监测目标' : '新增监测目标'" width="720px">
      <el-form label-width="110px">
        <div class="form-grid">
          <el-form-item label="目标类型" required>
            <el-select v-model="watchTargetForm.targetType">
              <el-option label="账号" value="account" />
              <el-option label="链接" value="link" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标状态">
            <el-select v-model="watchTargetForm.targetStatus">
              <el-option label="启用" value="active" />
              <el-option label="暂停" value="paused" />
              <el-option label="停用" value="disabled" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="平台">
            <el-select v-model="watchTargetForm.platform" clearable>
              <el-option v-for="item in platformOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="授权范围" required>
            <el-input v-model.trim="watchTargetForm.authorizationScope" placeholder="授权说明或来源" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="账号名称">
            <el-input v-model.trim="watchTargetForm.accountName" />
          </el-form-item>
          <el-form-item label="账号ID">
            <el-input v-model.trim="watchTargetForm.accountUid" />
          </el-form-item>
        </div>
        <el-form-item label="链接地址">
          <el-input v-model.trim="watchTargetForm.linkUrl">
            <template #prefix><Link2 :size="16" /></template>
          </el-input>
        </el-form-item>
        <el-form-item label="关键词范围">
          <el-input v-model.trim="watchTargetForm.keywordScope" placeholder="多个词用逗号分隔" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="watchTargetForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="watchTargetFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!canMonitorOperate" @click="submitWatchTarget">保存</el-button>
      </template>
    </el-dialog>

  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Link2,
  PauseCircle,
  Pencil,
  Play,
  Plus,
  RefreshCw,
  Search,
  Target,
  Trash2
} from 'lucide-vue-next';
import {
  deleteMonitorTask,
  deleteMonitorWatchTarget,
  listMonitorTasks,
  listMonitorWatchTargets,
  runMonitorTask,
  saveMonitorTask,
  saveMonitorWatchTarget,
  updateMonitorTaskDisplay,
  updateMonitorTaskStatus
} from '../services/monitor';
import { getCurrentCampusUser } from '../services/permission';
import type {
  ApiId,
  CampusMonitorTask,
  CampusMonitorWatchTarget
} from '../types/api';

type TaskStatus = 'active' | 'paused' | 'disabled';
type WatchTargetType = 'account' | 'link';

interface TaskLanguageForm {
  keywordsZh: string;
  keywordsMongolian: string;
  keywordsUyghur: string;
  negativeZh: string;
  negativeMongolian: string;
  negativeUyghur: string;
  excludeZh: string;
  excludeMongolian: string;
  excludeUyghur: string;
}

const platformOptions = [
  { label: '微博', value: 'weibo' },
  { label: '微信公众号', value: 'wechat' },
  { label: '抖音', value: 'douyin' },
  { label: '快手', value: 'kuaishou' },
  { label: '小红书', value: 'xiaohongshu' },
  { label: 'B站', value: 'bilibili' },
  { label: '知乎', value: 'zhihu' },
  { label: '新闻/网页', value: 'news' },
  { label: '网页', value: 'web' }
];

const taskQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  taskStatus: '',
  platform: ''
});
const tasks = ref<CampusMonitorTask[]>([]);
const taskTotal = ref(0);
const taskLoading = ref(false);
const taskDialogVisible = ref(false);
const saving = ref(false);

const taskForm = reactive<CampusMonitorTask>({
  taskName: '',
  monitorSubject: '',
  subjectAliases: '',
  keywords: '',
  keywordsI18n: '',
  negativeWords: '',
  negativeWordsI18n: '',
  excludeWords: '',
  excludeWordsI18n: '',
  platformScope: '*',
  scanFrequencyMinutes: 60,
  scheduleEnabled: 1,
  displayEnabled: 1,
  autoIngestEnabled: 1,
  alertMode: 'negative_only',
  taskStatus: 'active',
  ingestTaskIds: '',
  remark: ''
});
const taskLanguageForm = reactive<TaskLanguageForm>({
  keywordsZh: '',
  keywordsMongolian: '',
  keywordsUyghur: '',
  negativeZh: '',
  negativeMongolian: '',
  negativeUyghur: '',
  excludeZh: '',
  excludeMongolian: '',
  excludeUyghur: ''
});

const selectedTask = ref<CampusMonitorTask | null>(null);
const watchTargetDialogVisible = ref(false);
const watchTargetFormVisible = ref(false);
const watchTargetLoading = ref(false);
const watchTargets = ref<CampusMonitorWatchTarget[]>([]);
const watchTargetTotal = ref(0);
const watchTargetQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  monitorTaskId: undefined as ApiId | undefined,
  targetType: '',
  platform: '',
  keyword: '',
  targetStatus: ''
});
const watchTargetForm = reactive<CampusMonitorWatchTarget>({
  monitorTaskId: undefined,
  targetType: 'account',
  platform: '',
  accountName: '',
  accountUid: '',
  linkUrl: '',
  authorizationScope: '',
  keywordScope: '',
  targetStatus: 'active',
  remark: ''
});

const permissionReady = ref(false);
const permissionCodes = ref<Set<string>>(new Set());

const canMonitorOperate = computed(() => hasPermission([
  'role:campus_admin',
  'campus:api:all',
  'campus:business:operate',
  'campus:monitor:operate'
]));
const taskScheduleEnabled = computed({
  get: () => taskForm.scheduleEnabled !== 0,
  set: (value: boolean) => {
    taskForm.scheduleEnabled = value ? 1 : 0;
  }
});
const taskDisplayEnabled = computed({
  get: () => taskForm.displayEnabled !== 0,
  set: (value: boolean) => {
    taskForm.displayEnabled = value ? 1 : 0;
  }
});
const taskAutoIngestEnabled = computed({
  get: () => taskForm.autoIngestEnabled !== 0,
  set: (value: boolean) => {
    taskForm.autoIngestEnabled = value ? 1 : 0;
  }
});
const platformScopeValues = computed({
  get: () => {
    const scope = taskForm.platformScope || '*';
    return scope === '*' ? [] : scope.split(',').map((item) => item.trim()).filter(Boolean);
  },
  set: (value: string[]) => {
    taskForm.platformScope = value.length > 0 ? value.join(',') : '*';
  }
});
const watchTargetDialogTitle = computed(() => {
  return selectedTask.value ? `监测目标 - ${selectedTask.value.taskName}` : '监测目标';
});
onMounted(async () => {
  await Promise.all([loadCurrentPermissions(), loadTasks()]);
});

async function loadCurrentPermissions() {
  try {
    const current = await getCurrentCampusUser();
    permissionCodes.value = new Set(current.permissions || []);
  } catch {
    permissionCodes.value = new Set();
  } finally {
    permissionReady.value = true;
  }
}

function hasPermission(codes: string[]) {
  if (!permissionReady.value) {
    return true;
  }
  return codes.some((code) => permissionCodes.value.has(code));
}

async function loadTasks() {
  taskLoading.value = true;
  try {
    const page = await listMonitorTasks(taskQuery);
    tasks.value = page.list || [];
    taskTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '监测任务加载失败');
  } finally {
    taskLoading.value = false;
  }
}

function resetTaskQuery() {
  Object.assign(taskQuery, {
    pageNum: 1,
    pageSize: taskQuery.pageSize,
    keyword: '',
    taskStatus: '',
    platform: ''
  });
  loadTasks();
}

function resetTaskForm() {
  Object.assign(taskForm, {
    id: undefined,
    monitorTaskId: undefined,
    taskName: '',
    monitorSubject: '',
    subjectAliases: '',
    keywords: '',
    keywordsI18n: '',
    negativeWords: '',
    negativeWordsI18n: '',
    excludeWords: '',
    excludeWordsI18n: '',
    platformScope: '*',
    scanFrequencyMinutes: 60,
    scheduleEnabled: 1,
    displayEnabled: 1,
    autoIngestEnabled: 1,
    alertMode: 'negative_only',
    taskStatus: 'active',
    ingestTaskIds: '',
    ingestTaskNames: '',
    lastMatchCount: 0,
    displayResultCount: 0,
    lastErrorMessage: '',
    ingestCapabilityStatus: 'pending',
    remark: ''
  });
  resetTaskLanguageForm();
}

function resetTaskLanguageForm() {
  Object.assign(taskLanguageForm, {
    keywordsZh: '',
    keywordsMongolian: '',
    keywordsUyghur: '',
    negativeZh: '',
    negativeMongolian: '',
    negativeUyghur: '',
    excludeZh: '',
    excludeMongolian: '',
    excludeUyghur: ''
  });
}

function openTaskCreate() {
  resetTaskForm();
  taskDialogVisible.value = true;
}

function openTaskEdit(row: CampusMonitorTask) {
  resetTaskForm();
  Object.assign(taskForm, row);
  applyLanguageForm(row);
  taskDialogVisible.value = true;
}

async function submitTask() {
  if (!taskForm.taskName || !taskForm.monitorSubject) {
    ElMessage.warning('任务名称和监测主体不能为空');
    return;
  }
  if (!hasMonitorCriteria()) {
    ElMessage.warning('至少填写一组关键词、负面词或多语言词');
    return;
  }
  saving.value = true;
  try {
    const payload: CampusMonitorTask = {
      ...taskForm,
      ingestTaskIds: taskForm.autoIngestEnabled === 0 ? taskForm.ingestTaskIds : undefined,
      keywordsI18n: buildI18nText({
        zh: taskLanguageForm.keywordsZh,
        mongolian: taskLanguageForm.keywordsMongolian,
        uyghur: taskLanguageForm.keywordsUyghur
      }),
      negativeWordsI18n: buildI18nText({
        zh: taskLanguageForm.negativeZh,
        mongolian: taskLanguageForm.negativeMongolian,
        uyghur: taskLanguageForm.negativeUyghur
      }),
      excludeWordsI18n: buildI18nText({
        zh: taskLanguageForm.excludeZh,
        mongolian: taskLanguageForm.excludeMongolian,
        uyghur: taskLanguageForm.excludeUyghur
      })
    };
    await saveMonitorTask(payload);
    ElMessage.success('监测任务已保存');
    taskDialogVisible.value = false;
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitTaskStatus(row: CampusMonitorTask, status: string) {
  if (!row.monitorTaskId || row.taskStatus === status) {
    return;
  }
  try {
    await updateMonitorTaskStatus(row.monitorTaskId, status);
    ElMessage.success('任务状态已更新');
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function submitTaskDisplay(row: CampusMonitorTask, display: boolean) {
  if (!row.monitorTaskId) {
    return;
  }
  const previous = row.displayEnabled;
  row.displayEnabled = display ? 1 : 0;
  try {
    await updateMonitorTaskDisplay(row.monitorTaskId, row.displayEnabled);
    ElMessage.success(display ? '已在前台展示' : '已从前台隐藏');
    await loadTasks();
  } catch (error) {
    row.displayEnabled = previous;
    ElMessage.error(error instanceof Error ? error.message : '展示状态更新失败');
  }
}

function submitTaskDisplayChange(row: CampusMonitorTask, value: unknown) {
  submitTaskDisplay(row, Boolean(value));
}

function handleTaskStatusCommand(row: CampusMonitorTask, status: unknown) {
  submitTaskStatus(row, String(status));
}

async function submitRunTask(row: CampusMonitorTask) {
  if (!row.monitorTaskId) {
    return;
  }
  try {
    await runMonitorTask(row.monitorTaskId);
    ElMessage.success('任务已触发');
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '运行失败');
  }
}

async function submitTaskDelete(row: CampusMonitorTask) {
  if (!row.monitorTaskId) {
    return;
  }
  try {
    await ElMessageBox.confirm('删除后任务将停用，关联数据从前台监测信息隐藏，历史记录仍保留。确认删除？', '删除确认', { type: 'warning' });
    await deleteMonitorTask(row.monitorTaskId);
    ElMessage.success('监测任务已删除');
    await loadTasks();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function openWatchTargets(row: CampusMonitorTask) {
  if (!row.monitorTaskId) {
    return;
  }
  selectedTask.value = row;
  Object.assign(watchTargetQuery, {
    pageNum: 1,
    monitorTaskId: row.monitorTaskId,
    targetType: '',
    platform: '',
    keyword: '',
    targetStatus: ''
  });
  watchTargetDialogVisible.value = true;
  loadWatchTargets();
}

async function loadWatchTargets() {
  if (!watchTargetQuery.monitorTaskId) {
    return;
  }
  watchTargetLoading.value = true;
  try {
    const page = await listMonitorWatchTargets(watchTargetQuery);
    watchTargets.value = page.list || [];
    watchTargetTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '监测目标加载失败');
  } finally {
    watchTargetLoading.value = false;
  }
}

function resetWatchTargetForm() {
  Object.assign(watchTargetForm, {
    id: undefined,
    targetId: undefined,
    monitorTaskId: watchTargetQuery.monitorTaskId,
    targetType: 'account',
    platform: '',
    accountId: undefined,
    accountName: '',
    accountUid: '',
    linkUrl: '',
    sourceObjectType: '',
    sourceObjectId: undefined,
    authorizationScope: '',
    keywordScope: '',
    targetStatus: 'active',
    remark: ''
  });
}

function openWatchTargetCreate() {
  resetWatchTargetForm();
  watchTargetFormVisible.value = true;
}

function openWatchTargetEdit(row: CampusMonitorWatchTarget) {
  resetWatchTargetForm();
  Object.assign(watchTargetForm, row);
  watchTargetFormVisible.value = true;
}

async function submitWatchTarget() {
  if (!watchTargetForm.monitorTaskId) {
    ElMessage.warning('请选择监测任务');
    return;
  }
  if (!watchTargetForm.authorizationScope) {
    ElMessage.warning('授权范围不能为空');
    return;
  }
  if (watchTargetForm.targetType === 'account' && !watchTargetForm.accountName && !watchTargetForm.accountUid && !watchTargetForm.linkUrl) {
    ElMessage.warning('账号目标需要填写账号名称、账号ID或链接');
    return;
  }
  if (watchTargetForm.targetType === 'link' && !watchTargetForm.linkUrl) {
    ElMessage.warning('链接目标需要填写链接地址');
    return;
  }
  saving.value = true;
  try {
    await saveMonitorWatchTarget({ ...watchTargetForm });
    ElMessage.success('监测目标已保存');
    watchTargetFormVisible.value = false;
    await loadWatchTargets();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function toggleWatchTargetStatus(row: CampusMonitorWatchTarget) {
  const nextStatus = row.targetStatus === 'active' ? 'paused' : 'active';
  try {
    await saveMonitorWatchTarget({ ...row, targetStatus: nextStatus });
    ElMessage.success('监测目标状态已更新');
    await loadWatchTargets();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function submitWatchTargetDelete(row: CampusMonitorWatchTarget) {
  if (!row.targetId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该监测目标？', '删除确认', { type: 'warning' });
    await deleteMonitorWatchTarget(row.targetId);
    ElMessage.success('监测目标已删除');
    await loadWatchTargets();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function applyLanguageForm(row: CampusMonitorTask) {
  const keywords = parseI18nText(row.keywordsI18n);
  const negative = parseI18nText(row.negativeWordsI18n);
  const exclude = parseI18nText(row.excludeWordsI18n);
  Object.assign(taskLanguageForm, {
    keywordsZh: keywords.zh || '',
    keywordsMongolian: keywords.mongolian || '',
    keywordsUyghur: keywords.uyghur || '',
    negativeZh: negative.zh || '',
    negativeMongolian: negative.mongolian || '',
    negativeUyghur: negative.uyghur || '',
    excludeZh: exclude.zh || '',
    excludeMongolian: exclude.mongolian || '',
    excludeUyghur: exclude.uyghur || ''
  });
}

function hasMonitorCriteria() {
  return Boolean(
    taskForm.keywords
    || taskForm.negativeWords
    || taskLanguageForm.keywordsZh
    || taskLanguageForm.keywordsMongolian
    || taskLanguageForm.keywordsUyghur
    || taskLanguageForm.negativeZh
    || taskLanguageForm.negativeMongolian
    || taskLanguageForm.negativeUyghur
  );
}

function parseI18nText(text?: string) {
  if (!text) {
    return {} as Record<string, string>;
  }
  try {
    return JSON.parse(text) as Record<string, string>;
  } catch {
    return {};
  }
}

function buildI18nText(values: Record<string, string>) {
  const compact = Object.entries(values).reduce<Record<string, string>>((result, [key, value]) => {
    if (value?.trim()) {
      result[key] = value.trim();
    }
    return result;
  }, {});
  return Object.keys(compact).length > 0 ? JSON.stringify(compact) : '';
}

function formatKeywords(row: CampusMonitorTask) {
  const parts = [row.keywords, row.negativeWords].filter(Boolean);
  return parts.length > 0 ? parts.join(' / ') : '-';
}

function platformScopeLabel(scope?: string) {
  if (!scope || scope === '*') {
    return '全部';
  }
  const labelMap = new Map(platformOptions.map((item) => [item.value, item.label]));
  return scope.split(',').map((item) => labelMap.get(item.trim()) || item.trim()).filter(Boolean).join('、');
}

function taskStatusLabel(status?: string) {
  const labels: Record<string, string> = {
    active: '运行中',
    paused: '暂停',
    disabled: '停用'
  };
  return labels[status || ''] || status || '-';
}

function taskStatusTagType(status?: string) {
  const map: Record<string, 'success' | 'warning' | 'info'> = {
    active: 'success',
    paused: 'warning',
    disabled: 'info'
  };
  return map[status || ''] || 'info';
}

function alertModeLabel(mode?: string) {
  const labels: Record<string, string> = {
    negative_only: '仅负面',
    all_hits: '风险命中',
    manual: '人工转预警'
  };
  return labels[mode || ''] || mode || '-';
}

function ingestCapabilityLabel(status?: string) {
  const labels: Record<string, string> = {
    ready: '可用',
    partial: '部分可用',
    unsupported: '未接入',
    failed: '调用失败',
    pending: '待运行'
  };
  return labels[status || ''] || status || '待运行';
}

function ingestCapabilityTagType(status?: string) {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    ready: 'success',
    partial: 'warning',
    unsupported: 'info',
    failed: 'danger',
    pending: 'info'
  };
  return map[status || ''] || 'info';
}

function targetTypeLabel(type?: WatchTargetType) {
  return type === 'link' ? '链接' : '账号';
}

function targetStatusLabel(status?: string) {
  return taskStatusLabel(status);
}

function targetStatusTagType(status?: string) {
  return taskStatusTagType(status);
}

</script>

<style scoped>
.monitor-task-admin :deep(.el-select),
.monitor-task-admin :deep(.el-input),
.monitor-task-admin :deep(.el-input-number) {
  width: 100%;
}

.task-title-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.task-name {
  font-weight: 600;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-meta {
  font-size: 12px;
  color: #64748b;
}

.admin-form {
  padding-right: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.language-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin: -10px 0 18px 120px;
}

.advanced-panel {
  margin-left: 120px;
}

.advanced-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  color: #64748b;
}

.advanced-grid div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.advanced-grid strong {
  color: #0f172a;
  font-weight: 500;
  overflow-wrap: anywhere;
}

.target-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.pagination-row.compact {
  margin-top: 12px;
}

@media (max-width: 900px) {
  .form-grid,
  .language-grid {
    grid-template-columns: 1fr;
  }

  .language-grid {
    margin-left: 0;
  }

  .advanced-panel {
    margin-left: 0;
  }

  .advanced-grid {
    grid-template-columns: 1fr;
  }

  .target-toolbar {
    flex-direction: column;
  }
}
</style>
