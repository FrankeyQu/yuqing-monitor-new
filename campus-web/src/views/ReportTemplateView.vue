<template>
  <section class="business-page">
    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="query.keyword" clearable placeholder="模板名称/备注" @keyup.enter="loadTemplates">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-select v-model="query.reportType" clearable placeholder="报告类型">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
            <el-option label="专报" value="special" />
            <el-option label="事件报告" value="event" />
          </el-select>
          <el-select v-model="query.status" clearable placeholder="状态">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-button @click="loadTemplates">
            <Search :size="16" />
            查询
          </el-button>
        </div>
        <el-button type="primary" @click="router.push('/report-templates/create')">
          <Plus :size="16" />
          新增模板
        </el-button>
      </div>

      <el-table :data="templates" v-loading="loading" size="small" height="560">
        <el-table-column prop="templateName" label="模板名称" min-width="210" show-overflow-tooltip />
        <el-table-column prop="reportType" label="类型" width="102">
          <template #default="{ row }">{{ reportTypeLabel(row.reportType) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="82">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">
              {{ row.status === 0 ? '停用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="适用场景" min-width="260" show-overflow-tooltip />
        <el-table-column label="模板预览" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ preview(row.templateContent) }}</template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="168" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              <Pencil :size="15" />
              编辑
            </el-button>
            <el-button link type="danger" @click="submitDelete(row)">
              <Trash2 :size="15" />
              删除
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
          @size-change="loadTemplates"
          @current-change="loadTemplates"
        />
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Pencil, Plus, Search, Trash2 } from 'lucide-vue-next';
import { deleteReportTemplate, listReportTemplates } from '../services/analysisReport';
import type { CampusReportTemplate } from '../types/api';

const router = useRouter();
const loading = ref(false);
const templates = ref<CampusReportTemplate[]>([]);
const total = ref(0);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  reportType: '',
  status: undefined as number | undefined
});

onMounted(loadTemplates);

async function loadTemplates() {
  loading.value = true;
  try {
    const page = await listReportTemplates(query);
    templates.value = page.list || [];
    total.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '模板列表加载失败');
  } finally {
    loading.value = false;
  }
}

function openEdit(row: CampusReportTemplate) {
  if (row.templateId) {
    router.push(`/report-templates/${row.templateId}/edit`);
  }
}

async function submitDelete(row: CampusReportTemplate) {
  if (!row.templateId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该报告模板？', '删除确认', { type: 'warning' });
    await deleteReportTemplate(row.templateId);
    ElMessage.success('模板已删除');
    await loadTemplates();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function reportTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    daily: '日报',
    weekly: '周报',
    monthly: '月报',
    special: '专报',
    event: '事件报告',
    event_review: '事件复盘'
  };
  return labels[value || 'daily'] || value || '日报';
}

function preview(content?: string) {
  return (content || '')
    .replace(/[#*_`|>\-\n\r]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 100) || '暂无内容';
}
</script>
