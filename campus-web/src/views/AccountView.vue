<template>
  <section class="business-page">
    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账号库" name="accounts">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="accountQuery.keyword" clearable placeholder="账号/标签/说明" @keyup.enter="loadAccounts">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input v-model.trim="accountQuery.platform" clearable placeholder="平台" @keyup.enter="loadAccounts" />
              <el-select v-model="accountQuery.auditStatus" clearable placeholder="审核">
                <el-option label="待审核" value="pending" />
                <el-option label="已通过" value="approved" />
                <el-option label="已驳回" value="rejected" />
              </el-select>
              <el-select v-model="accountQuery.accountStatus" clearable placeholder="状态">
                <el-option label="待审核" value="pending" />
                <el-option label="关注中" value="active" />
                <el-option label="已驳回" value="rejected" />
                <el-option label="已停用" value="disabled" />
              </el-select>
              <el-button @click="loadAccounts">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openAccountCreate">
              <Plus :size="16" />
              登记账号
            </el-button>
          </div>

          <el-table :data="accounts" v-loading="accountLoading" size="small" height="560">
            <el-table-column prop="accountName" label="账号名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="platform" label="平台" width="100" show-overflow-tooltip />
            <el-table-column prop="accountUid" label="账号ID" width="140" show-overflow-tooltip />
            <el-table-column prop="focusLevel" label="级别" width="82">
              <template #default="{ row }">
                <el-tag :type="focusTagType(row.focusLevel)" effect="plain">{{ focusLabel(row.focusLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditStatus" label="审核" width="90">
              <template #default="{ row }">
                <el-tag :type="auditTagType(row.auditStatus)" effect="plain">{{ auditLabel(row.auditStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="accountStatus" label="状态" width="130">
              <template #default="{ row }">
                <el-dropdown trigger="click" @command="(status: string) => handleStatusChange(row, status)">
                  <el-tag effect="plain" style="cursor: pointer;">
                    {{ accountStatusLabel(row.accountStatus) }}
                    <ChevronDown :size="12" style="margin-left: 4px;" />
                  </el-tag>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-for="opt in statusOptions"
                        :key="opt.value"
                        :command="opt.value"
                        :disabled="opt.value === row.accountStatus"
                      >
                        {{ opt.label }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
            <el-table-column prop="taskNo" label="任务编号" width="130" show-overflow-tooltip />
            <el-table-column prop="focusEndTime" label="关注截止" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="310" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openAccountEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="success" @click="submitAudit(row, 'approved')">
                  <CheckCircle2 :size="15" />
                  通过
                </el-button>
                <el-button link type="warning" @click="submitAudit(row, 'rejected')">
                  <XCircle :size="15" />
                  驳回
                </el-button>
                <el-button link type="danger" @click="handleDeleteAccount(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="公开动态" name="contents">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input-number v-model="contentQuery.accountId" :min="0" controls-position="right" placeholder="账号ID" />
              <el-input v-model.trim="contentQuery.keyword" clearable placeholder="标题/内容/关键词" @keyup.enter="loadContents" />
              <el-select v-model="contentQuery.riskLevel" clearable placeholder="风险">
                <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
              </el-select>
              <el-button @click="loadContents">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openContentCreate">
              <Plus :size="16" />
              新增动态
            </el-button>
          </div>

          <el-table :data="contents" v-loading="contentLoading" size="small" height="560">
            <el-table-column prop="contentTitle" label="标题" min-width="210" show-overflow-tooltip />
            <el-table-column prop="accountId" label="账号ID" width="150" />
            <el-table-column prop="platform" label="平台" width="110" show-overflow-tooltip />
            <el-table-column prop="keywords" label="关键词" width="140" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="级别" width="82">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="168" show-overflow-tooltip />
            <el-table-column prop="originalUrl" label="原始链接" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="accountDialogVisible" :title="accountForm.accountId ? '编辑重点账号' : '登记重点账号'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="平台" required>
            <el-input v-model.trim="accountForm.platform" />
          </el-form-item>
          <el-form-item label="账号名称" required>
            <el-input v-model.trim="accountForm.accountName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="平台账号ID">
            <el-input v-model.trim="accountForm.accountUid" />
          </el-form-item>
          <el-form-item label="主页链接">
            <el-input v-model.trim="accountForm.homepageUrl" />
          </el-form-item>
        </div>
        <el-form-item label="关联人员或说明">
          <el-input v-model.trim="accountForm.relatedPersonDesc" type="textarea" :rows="2" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="来源依据" required>
            <el-input v-model.trim="accountForm.sourceBasis" />
          </el-form-item>
          <el-form-item label="任务编号" required>
            <el-input v-model.trim="accountForm.taskNo" />
          </el-form-item>
        </div>
        <el-form-item label="授权范围" required>
          <el-input v-model.trim="accountForm.authorizationScope" type="textarea" :rows="2" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="关注开始" required>
            <el-date-picker v-model="accountForm.focusStartTime" type="datetime" />
          </el-form-item>
          <el-form-item label="关注结束" required>
            <el-date-picker v-model="accountForm.focusEndTime" type="datetime" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="关注级别">
            <el-select v-model="accountForm.focusLevel">
              <el-option label="一般" value="normal" />
              <el-option label="重点" value="important" />
              <el-option label="紧急" value="urgent" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model.trim="accountForm.tags" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model.trim="accountForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAccount">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contentDialogVisible" title="新增公开动态" width="720px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="账号ID" required>
            <el-input-number v-model="contentForm.accountId" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="平台">
            <el-input v-model.trim="contentForm.platform" />
          </el-form-item>
        </div>
        <el-form-item label="标题">
          <el-input v-model.trim="contentForm.contentTitle" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model.trim="contentForm.contentText" type="textarea" :rows="4" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="原始链接">
            <el-input v-model.trim="contentForm.originalUrl" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model.trim="contentForm.keywords" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="风险等级">
            <el-select v-model="contentForm.riskLevel">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布时间">
            <el-date-picker v-model="contentForm.publishTime" type="datetime" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="contentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitContent">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CheckCircle2, ChevronDown, Pencil, Plus, Search, Trash2, XCircle } from 'lucide-vue-next';
import {
  addAccountContent,
  auditAccount,
  deleteAccount,
  listAccountContents,
  listAccounts,
  saveAccount,
  updateAccountStatus
} from '../services/campusBusiness';
import { CAMPUS_RISK_OPTIONS, campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type { CampusAccount, CampusAccountContent } from '../types/api';

const activeTab = ref('accounts');
const accountLoading = ref(false);
const contentLoading = ref(false);
const saving = ref(false);
const accountDialogVisible = ref(false);
const contentDialogVisible = ref(false);
const accounts = ref<CampusAccount[]>([]);
const contents = ref<CampusAccountContent[]>([]);

const accountQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  platform: '',
  focusLevel: '',
  auditStatus: '',
  accountStatus: ''
});
const contentQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  accountId: undefined as number | undefined,
  keyword: '',
  riskLevel: ''
});
const accountForm = reactive<CampusAccount>({
  platform: '',
  accountName: '',
  accountUid: '',
  homepageUrl: '',
  relatedPersonDesc: '',
  sourceBasis: '',
  taskNo: '',
  authorizationScope: '',
  focusStartTime: undefined,
  focusEndTime: undefined,
  focusLevel: 'normal',
  tags: '',
  remark: ''
});
const contentForm = reactive<CampusAccountContent>({
  accountId: undefined,
  platform: '',
  contentTitle: '',
  contentText: '',
  originalUrl: '',
  keywords: '',
  riskLevel: 'normal',
  publishTime: undefined
});

onMounted(loadAccounts);
watch(activeTab, (tab) => {
  if (tab === 'contents') {
    loadContents();
  }
});

async function loadAccounts() {
  accountLoading.value = true;
  try {
    const page = await listAccounts(accountQuery);
    accounts.value = page.list || [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '账号列表加载失败');
  } finally {
    accountLoading.value = false;
  }
}

async function loadContents() {
  contentLoading.value = true;
  try {
    const page = await listAccountContents(contentQuery);
    contents.value = page.list || [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '公开动态加载失败');
  } finally {
    contentLoading.value = false;
  }
}

function defaultEndDate() {
  const date = new Date();
  date.setMonth(date.getMonth() + 3);
  return date;
}

function resetAccountForm() {
  Object.assign(accountForm, {
    accountId: undefined,
    platform: '',
    accountName: '',
    accountUid: '',
    homepageUrl: '',
    relatedPersonDesc: '',
    sourceBasis: '',
    taskNo: '',
    authorizationScope: '',
    focusStartTime: new Date(),
    focusEndTime: defaultEndDate(),
    focusLevel: 'normal',
    tags: '',
    remark: ''
  });
}

function openAccountCreate() {
  resetAccountForm();
  accountDialogVisible.value = true;
}

function openAccountEdit(row: CampusAccount) {
  Object.assign(accountForm, row);
  accountDialogVisible.value = true;
}

async function submitAccount() {
  if (!accountForm.platform || !accountForm.accountName || !accountForm.sourceBasis
      || !accountForm.taskNo || !accountForm.authorizationScope
      || !accountForm.focusStartTime || !accountForm.focusEndTime) {
    ElMessage.warning('平台、账号名称、来源依据、任务编号、授权范围和关注期限不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveAccount({ ...accountForm });
    ElMessage.success('保存成功');
    accountDialogVisible.value = false;
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitAudit(row: CampusAccount, auditStatus: string) {
  if (!row.accountId) {
    return;
  }
  try {
    await auditAccount(row.accountId, auditStatus, auditStatus === 'approved' ? '审核通过' : '审核驳回');
    ElMessage.success('审核已更新');
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '审核失败');
  }
}

function openContentCreate() {
  Object.assign(contentForm, {
    accountId: contentQuery.accountId,
    platform: '',
    contentTitle: '',
    contentText: '',
    originalUrl: '',
    keywords: '',
    riskLevel: 'normal',
    publishTime: new Date()
  });
  contentDialogVisible.value = true;
}

async function submitContent() {
  if (!contentForm.accountId || (!contentForm.contentTitle && !contentForm.contentText)) {
    ElMessage.warning('账号ID以及标题或内容不能为空');
    return;
  }
  saving.value = true;
  try {
    await addAccountContent({ ...contentForm });
    ElMessage.success('保存成功');
    contentDialogVisible.value = false;
    await loadContents();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function riskLabel(value?: string) {
  return campusRiskLabel(value);
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}

function focusLabel(value?: string) {
  const labels: Record<string, string> = { normal: '一般', important: '重点', urgent: '紧急' };
  return labels[value || 'normal'] || value || '一般';
}

function focusTagType(value?: string) {
  if (value === 'urgent') {
    return 'danger';
  }
  if (value === 'important') {
    return 'warning';
  }
  return 'info';
}

function auditLabel(value?: string) {
  const labels: Record<string, string> = { pending: '待审核', approved: '通过', rejected: '驳回' };
  return labels[value || 'pending'] || value || '待审核';
}

function auditTagType(value?: string) {
  if (value === 'approved') {
    return 'success';
  }
  if (value === 'rejected') {
    return 'danger';
  }
  return 'warning';
}

function accountStatusLabel(value?: string) {
  const labels: Record<string, string> = {
    pending: '待审核',
    active: '关注中',
    rejected: '已驳回',
    disabled: '已停用',
    expired: '已到期'
  };
  return labels[value || 'pending'] || value || '待审核';
}

const statusOptions = [
  { label: '待审核', value: 'pending' },
  { label: '关注中', value: 'active' },
  { label: '已驳回', value: 'rejected' },
  { label: '已停用', value: 'disabled' }
];

async function handleDeleteAccount(row: CampusAccount) {
  if (!row.accountId) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认删除账号「${row.accountName}」吗？删除后不可恢复。`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    );
  } catch {
    return;
  }
  try {
    await deleteAccount(row.accountId);
    ElMessage.success('已删除');
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  }
}

async function handleStatusChange(row: CampusAccount, newStatus: string) {
  if (!row.accountId || newStatus === row.accountStatus) {
    return;
  }
  try {
    await updateAccountStatus(row.accountId, newStatus);
    ElMessage.success('状态已更新');
    await loadAccounts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}
</script>
