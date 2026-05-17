<template>
  <section class="settings-page">
    <SettingsNav />

    <section class="panel">
      <div class="toolbar audit-toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="query.moduleName" clearable placeholder="模块" @keyup.enter="loadLogs" />
          <el-input v-model.trim="query.operationType" clearable placeholder="操作" @keyup.enter="loadLogs" />
          <el-input v-model.trim="query.objectType" clearable placeholder="对象类型" @keyup.enter="loadLogs" />
          <el-input v-model.trim="query.operatorName" clearable placeholder="操作人" @keyup.enter="loadLogs" />
          <el-button @click="loadLogs">
            <Search :size="16" />
            查询
          </el-button>
        </div>
      </div>

      <el-table :data="rows" v-loading="loading" size="small" height="560">
        <el-table-column prop="createTime" label="时间" width="168" show-overflow-tooltip />
        <el-table-column prop="moduleName" label="模块" width="110" show-overflow-tooltip />
        <el-table-column prop="operationType" label="操作" width="116" show-overflow-tooltip />
        <el-table-column prop="objectType" label="对象类型" width="150" show-overflow-tooltip />
        <el-table-column prop="objectId" label="对象ID" width="170" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="120" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方法" width="82" />
        <el-table-column prop="requestUri" label="接口" min-width="220" show-overflow-tooltip />
        <el-table-column prop="operationResult" label="结果" width="88">
          <template #default="{ row }">
            <el-tag :type="row.operationResult === 0 ? 'danger' : 'success'" effect="plain">
              {{ row.operationResult === 0 ? '失败' : '成功' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failureReason" label="失败原因" min-width="180" show-overflow-tooltip />
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from 'lucide-vue-next';
import SettingsNav from '../../components/SettingsNav.vue';
import { listAuditLogs } from '../../services/settings';
import type { CampusAuditLog } from '../../types/api';

const loading = ref(false);
const rows = ref<CampusAuditLog[]>([]);
const total = ref(0);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  moduleName: '',
  operationType: '',
  objectType: '',
  operatorName: ''
});

onMounted(loadLogs);

async function loadLogs() {
  loading.value = true;
  try {
    const page = await listAuditLogs(query);
    rows.value = page.list || [];
    total.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '审计日志加载失败');
  } finally {
    loading.value = false;
  }
}
</script>
