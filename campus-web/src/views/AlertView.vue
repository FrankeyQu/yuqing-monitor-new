<template>
  <section class="business-page">
    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="query.keyword" clearable placeholder="标题/内容/关键词" @keyup.enter="loadAlerts">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-select v-model="query.alertSource" clearable placeholder="来源">
            <el-option label="线索" value="clue" />
            <el-option label="监测命中" value="monitor" />
            <el-option label="账号动态" value="account_content" />
            <el-option label="检测命中" value="detection" />
            <el-option label="人工创建" value="manual" />
          </el-select>
          <el-select v-model="query.riskLevel" clearable placeholder="风险">
            <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
          </el-select>
          <el-select v-model="query.alertStatus" clearable placeholder="状态">
            <el-option label="待处理" value="pending" />
            <el-option label="已处理" value="handled" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
          <el-button @click="loadAlerts">
            <Search :size="16" />
            查询
          </el-button>
        </div>
      </div>

      <el-table :data="rows" v-loading="loading" size="small" height="600">
        <el-table-column prop="alertTitle" label="预警标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="alertSource" label="来源" width="110">
          <template #default="{ row }">{{ sourceLabel(row.alertSource) }}</template>
        </el-table-column>
        <el-table-column prop="matchedKeywords" label="命中词" width="150" show-overflow-tooltip />
        <el-table-column label="依据" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ evidencePreview(row) }}</template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="级别" width="82">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertStatus" label="状态" width="96">
          <template #default="{ row }">
            <el-tag :type="row.alertStatus === 'pending' ? 'warning' : 'info'" effect="plain">
              {{ alertStatusLabel(row.alertStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="168" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.alertStatus !== 'pending'" @click="openHandle(row, 'handled')">
              <CheckCircle2 :size="15" />
              处理
            </el-button>
            <el-button link type="info" :disabled="row.alertStatus !== 'pending'" @click="openHandle(row, 'ignored')">
              <CircleOff :size="15" />
              忽略
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="loadAlerts"
          @current-change="loadAlerts"
        />
      </div>
    </section>

    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-tag :type="mailConfigured ? 'success' : 'info'" effect="plain">
            {{ mailConfigured ? '邮件通道已配置' : '邮件通道未配置' }}
          </el-tag>
        </div>
        <div class="toolbar-actions">
          <el-button @click="loadMailConfig">
            <RefreshCw :size="16" />
            刷新
          </el-button>
          <el-button type="primary" :loading="mailSaving" @click="submitMailConfig">
            <Save :size="16" />
            保存并测试
          </el-button>
        </div>
      </div>

      <el-form class="mail-config-form" label-position="top" v-loading="mailLoading">
        <div class="form-grid">
          <el-form-item label="SMTP主机" required>
            <el-input v-model.trim="mailForm.host" placeholder="smtp.example.com" />
          </el-form-item>
          <el-form-item label="端口" required>
            <el-input v-model.trim="mailForm.port" placeholder="465" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="发件邮箱" required>
            <el-input v-model.trim="mailForm.username" placeholder="monitor@example.com" />
          </el-form-item>
          <el-form-item label="邮箱授权码" required>
            <el-input v-model.trim="mailForm.password" type="password" show-password />
          </el-form-item>
        </div>
        <el-form-item label="收件人列表" required>
          <el-input
            v-model.trim="mailRecipientsText"
            type="textarea"
            :rows="3"
            placeholder="main@example.com, cc1@example.com, cc2@example.com"
          />
        </el-form-item>
      </el-form>
    </section>

    <el-dialog v-model="handleVisible" :title="handleForm.alertStatus === 'handled' ? '处理预警' : '忽略预警'" width="540px">
      <el-form label-position="top">
        <el-form-item label="处理意见">
          <el-input v-model.trim="handleForm.handleOpinion" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { CheckCircle2, CircleOff, RefreshCw, Save, Search } from 'lucide-vue-next';
import { handleAlert, listAlerts } from '../services/eventCenter';
import { getMailConfig, saveMailConfig } from '../services/mail';
import { CAMPUS_RISK_OPTIONS, campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type { CampusAlert, MailConfig } from '../types/api';

const loading = ref(false);
const saving = ref(false);
const mailLoading = ref(false);
const mailSaving = ref(false);
const handleVisible = ref(false);
const mailConfigured = ref(false);
const rows = ref<CampusAlert[]>([]);
const total = ref(0);
const currentAlert = ref<CampusAlert>();
const mailRecipientsText = ref('');
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  alertSource: '',
  riskLevel: '',
  alertStatus: ''
});
const handleForm = reactive({
  alertStatus: 'handled',
  handleOpinion: ''
});
const mailForm = reactive({
  host: '',
  port: '465',
  username: '',
  password: ''
});

onMounted(() => {
  loadAlerts();
  loadMailConfig();
});

async function loadAlerts() {
  loading.value = true;
  try {
    const page = await listAlerts(query);
    rows.value = page.list || [];
    total.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '预警列表加载失败');
  } finally {
    loading.value = false;
  }
}

async function loadMailConfig() {
  mailLoading.value = true;
  try {
    const config = await getMailConfig();
    if (!config) {
      resetMailForm();
      mailConfigured.value = false;
      return;
    }
    mailConfigured.value = true;
    mailForm.host = config.host || '';
    mailForm.port = config.port || '465';
    mailForm.username = config.username || '';
    mailForm.password = config.password || '';
    mailRecipientsText.value = formatRecipients(config);
  } catch (error) {
    mailConfigured.value = false;
    ElMessage.error(error instanceof Error ? error.message : '邮件通道加载失败');
  } finally {
    mailLoading.value = false;
  }
}

function openHandle(row: CampusAlert, alertStatus: string) {
  currentAlert.value = row;
  handleForm.alertStatus = alertStatus;
  handleForm.handleOpinion = '';
  handleVisible.value = true;
}

async function submitHandle() {
  if (!currentAlert.value?.alertId) {
    return;
  }
  saving.value = true;
  try {
    await handleAlert(currentAlert.value.alertId, handleForm.alertStatus, handleForm.handleOpinion);
    ElMessage.success('预警状态已更新');
    handleVisible.value = false;
    await loadAlerts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '处理失败');
  } finally {
    saving.value = false;
  }
}

function resetMailForm() {
  mailForm.host = '';
  mailForm.port = '465';
  mailForm.username = '';
  mailForm.password = '';
  mailRecipientsText.value = '';
}

async function submitMailConfig() {
  if (!mailForm.host || !mailForm.port || !mailForm.username || !mailForm.password) {
    ElMessage.warning('SMTP主机、端口、发件邮箱和授权码不能为空');
    return;
  }
  const recipients = parseRecipients(mailRecipientsText.value);
  const primary = recipients[0] || mailForm.username;
  mailSaving.value = true;
  try {
    await saveMailConfig({
      host: mailForm.host,
      port: mailForm.port,
      username: mailForm.username,
      password: mailForm.password,
      to: primary,
      cc: recipients.slice(1),
      toList: recipients.length > 0 ? recipients : [primary]
    });
    mailConfigured.value = true;
    ElMessage.success('邮件通道已保存并完成测试');
    await loadMailConfig();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '邮件通道保存失败');
  } finally {
    mailSaving.value = false;
  }
}

function parseRecipients(value: string) {
  return Array.from(
    new Set(
      value
        .split(/[\n,;；\s]+/)
        .map((item) => item.trim())
        .filter(Boolean)
    )
  );
}

function formatRecipients(config: MailConfig | null | undefined) {
  const recipients = new Set<string>();
  if (config?.toList?.length) {
    config.toList.forEach((item) => {
      if (item && item.trim()) {
        recipients.add(item.trim());
      }
    });
  }
  if (config?.to) {
    recipients.add(config.to.trim());
  }
  if (config?.cc?.length) {
    config.cc.forEach((item) => {
      if (item && item.trim()) {
        recipients.add(item.trim());
      }
    });
  }
  return Array.from(recipients).join(', ');
}

function sourceLabel(value?: string) {
  const labels: Record<string, string> = {
    clue: '线索',
    account_content: '账号动态',
    detection: '检测命中',
    monitor: '监测命中',
    manual: '人工创建'
  };
  return labels[value || 'manual'] || value || '人工创建';
}

function evidencePreview(row: CampusAlert) {
  if (!row.evidenceJson) {
    return row.alertContent || '-';
  }
  try {
    const parsed = JSON.parse(row.evidenceJson) as Record<string, unknown>;
    const topic = parsed.topicCategory ? `主题:${String(parsed.topicCategory)}` : '';
    const relevance = parsed.schoolRelevanceScore !== undefined ? `相关性:${String(parsed.schoolRelevanceScore)}` : '';
    const riskScore = parsed.riskScore !== undefined ? `风险分:${String(parsed.riskScore)}` : '';
    const matched = parsed.matchedKeywords ? `命中:${String(parsed.matchedKeywords)}` : '';
    return [topic, relevance, riskScore, matched].filter(Boolean).join(' / ') || row.alertContent || '-';
  } catch {
    return row.alertContent || '-';
  }
}

function alertStatusLabel(value?: string) {
  const labels: Record<string, string> = { pending: '待处理', handled: '已处理', ignored: '已忽略' };
  return labels[value || 'pending'] || value || '待处理';
}

function riskLabel(value?: string) {
  return campusRiskLabel(value);
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}
</script>
