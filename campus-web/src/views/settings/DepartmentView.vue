<template>
  <section class="settings-page">
    <SettingsNav />

    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="query.departmentName" clearable placeholder="部门名称" @keyup.enter="loadDepartments">
            <template #prefix><Search :size="16" /></template>
          </el-input>
          <el-select v-model="query.status" clearable placeholder="状态">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-button @click="loadDepartments">
            <Search :size="16" />
            查询
          </el-button>
        </div>
        <el-button type="primary" @click="openCreate">
          <Plus :size="16" />
          新增部门
        </el-button>
      </div>

      <el-table :data="rows" v-loading="loading" size="small" height="560">
        <el-table-column prop="departmentName" label="部门名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="departmentCode" label="编码" width="130" show-overflow-tooltip />
        <el-table-column prop="departmentType" label="类型" width="120" show-overflow-tooltip />
        <el-table-column prop="contactPhone" label="联系电话" width="140" show-overflow-tooltip />
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">
              {{ row.status === 0 ? '停用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              <Pencil :size="15" />
              编辑
            </el-button>
            <el-button link type="danger" @click="removeDepartment(row)">
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
          @size-change="loadDepartments"
          @current-change="loadDepartments"
        />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.departmentId ? '编辑部门' : '新增部门'" width="520px">
      <el-form label-position="top">
        <el-form-item label="部门名称" required>
          <el-input v-model.trim="form.departmentName" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="部门编码">
            <el-input v-model.trim="form.departmentCode" />
          </el-form-item>
          <el-form-item label="部门类型">
            <el-input v-model.trim="form.departmentType" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="上级部门ID">
            <el-input-number v-model="form.parentId" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="联系电话">
            <el-input v-model.trim="form.contactPhone" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Pencil, Plus, Search, Trash2 } from 'lucide-vue-next';
import SettingsNav from '../../components/SettingsNav.vue';
import { deleteDepartment, listDepartments, saveDepartment } from '../../services/settings';
import type { CampusDepartment } from '../../types/api';

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const rows = ref<CampusDepartment[]>([]);
const total = ref(0);
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  departmentName: '',
  status: undefined as number | undefined
});
const form = reactive<CampusDepartment>({
  departmentName: '',
  departmentCode: '',
  departmentType: '',
  parentId: undefined,
  contactPhone: '',
  sortNo: 0,
  status: 1
});

onMounted(loadDepartments);

async function loadDepartments() {
  loading.value = true;
  try {
    const page = await listDepartments(query);
    rows.value = page.list || [];
    total.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '部门列表加载失败');
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  Object.assign(form, {
    departmentId: undefined,
    departmentName: '',
    departmentCode: '',
    departmentType: '',
    parentId: undefined,
    contactPhone: '',
    sortNo: 0,
    status: 1
  });
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: CampusDepartment) {
  Object.assign(form, row);
  dialogVisible.value = true;
}

async function submitForm() {
  if (!form.departmentName) {
    ElMessage.warning('部门名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveDepartment({ ...form });
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    loadDepartments();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function removeDepartment(row: CampusDepartment) {
  if (!row.departmentId) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除部门“${row.departmentName}”？`, '删除确认', {
      type: 'warning'
    });
    await deleteDepartment(row.departmentId);
    ElMessage.success('删除成功');
    loadDepartments();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}
</script>
