<template>
  <section class="settings-page">
    <SettingsNav />

    <div class="settings-grid">
      <section class="panel">
        <div class="toolbar">
          <div class="toolbar-filters">
            <el-input v-model.trim="typeQuery.keyword" clearable placeholder="类型名称/编码" @keyup.enter="loadTypes">
              <template #prefix><Search :size="16" /></template>
            </el-input>
            <el-button @click="loadTypes">
              <Search :size="16" />
              查询
            </el-button>
          </div>
          <el-button type="primary" @click="openTypeCreate">
            <Plus :size="16" />
            新增类型
          </el-button>
        </div>

        <el-table
          :data="typeRows"
          v-loading="typeLoading"
          size="small"
          height="560"
          highlight-current-row
          @current-change="selectType"
        >
          <el-table-column prop="dictName" label="字典类型" min-width="150" show-overflow-tooltip />
          <el-table-column prop="dictType" label="编码" min-width="150" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="82">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">
                {{ row.status === 0 ? '停用' : '启用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="138" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openTypeEdit(row)">
                <Pencil :size="15" />
                编辑
              </el-button>
              <el-button link type="danger" @click.stop="removeType(row)">
                <Trash2 :size="15" />
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="toolbar">
          <div class="toolbar-filters">
            <el-input v-model.trim="itemQuery.keyword" clearable placeholder="字典项" @keyup.enter="loadItems">
              <template #prefix><Search :size="16" /></template>
            </el-input>
            <el-button :disabled="!selectedType" @click="loadItems">
              <Search :size="16" />
              查询
            </el-button>
          </div>
          <el-button type="primary" :disabled="!selectedType" @click="openItemCreate">
            <Plus :size="16" />
            新增字典项
          </el-button>
        </div>

        <div class="selected-line">
          <span>当前类型</span>
          <strong>{{ selectedType?.dictName || '未选择' }}</strong>
        </div>

        <el-table :data="itemRows" v-loading="itemLoading" size="small" height="512">
          <el-table-column prop="itemName" label="名称" min-width="130" show-overflow-tooltip />
          <el-table-column prop="itemCode" label="编码" min-width="120" show-overflow-tooltip />
          <el-table-column prop="itemValue" label="值" min-width="120" show-overflow-tooltip />
          <el-table-column prop="sortNo" label="排序" width="70" />
          <el-table-column prop="status" label="状态" width="82">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">
                {{ row.status === 0 ? '停用' : '启用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="138" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openItemEdit(row)">
                <Pencil :size="15" />
                编辑
              </el-button>
              <el-button link type="danger" @click="removeItem(row)">
                <Trash2 :size="15" />
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <el-dialog v-model="typeDialogVisible" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="520px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="类型编码" required>
            <el-input v-model.trim="typeForm.dictType" :disabled="Boolean(typeForm.id)" />
          </el-form-item>
          <el-form-item label="类型名称" required>
            <el-input v-model.trim="typeForm.dictName" />
          </el-form-item>
        </div>
        <el-form-item label="说明">
          <el-input v-model.trim="typeForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="排序">
            <el-input-number v-model="typeForm.sortNo" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="typeForm.status">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="itemForm.id ? '编辑字典项' : '新增字典项'" width="560px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="所属类型">
            <el-input v-model="itemForm.dictType" disabled />
          </el-form-item>
          <el-form-item label="字典项编码" required>
            <el-input v-model.trim="itemForm.itemCode" :disabled="Boolean(itemForm.id)" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="字典项名称" required>
            <el-input v-model.trim="itemForm.itemName" />
          </el-form-item>
          <el-form-item label="字典项值">
            <el-input v-model.trim="itemForm.itemValue" />
          </el-form-item>
        </div>
        <el-form-item label="说明">
          <el-input v-model.trim="itemForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="排序">
            <el-input-number v-model="itemForm.sortNo" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="itemForm.status">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitItem">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Pencil, Plus, Search, Trash2 } from 'lucide-vue-next';
import SettingsNav from '../../components/SettingsNav.vue';
import {
  deleteDictItem,
  deleteDictType,
  listDictItems,
  listDictTypes,
  saveDictItem,
  saveDictType
} from '../../services/settings';
import type { CampusDictItem, CampusDictType } from '../../types/api';

const typeLoading = ref(false);
const itemLoading = ref(false);
const saving = ref(false);
const typeRows = ref<CampusDictType[]>([]);
const itemRows = ref<CampusDictItem[]>([]);
const selectedType = ref<CampusDictType>();
const typeDialogVisible = ref(false);
const itemDialogVisible = ref(false);
const typeQuery = reactive({ pageNum: 1, pageSize: 50, keyword: '' });
const itemQuery = reactive({ pageNum: 1, pageSize: 50, keyword: '', dictType: '' });
const typeForm = reactive<CampusDictType>({
  dictType: '',
  dictName: '',
  description: '',
  sortNo: 0,
  status: 1
});
const itemForm = reactive<CampusDictItem>({
  dictType: '',
  itemCode: '',
  itemName: '',
  itemValue: '',
  description: '',
  sortNo: 0,
  status: 1
});

onMounted(loadTypes);

async function loadTypes() {
  typeLoading.value = true;
  try {
    const page = await listDictTypes(typeQuery);
    typeRows.value = page.list || [];
    if (!selectedType.value && typeRows.value.length) {
      selectType(typeRows.value[0]);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '字典类型加载失败');
  } finally {
    typeLoading.value = false;
  }
}

async function selectType(row?: CampusDictType) {
  selectedType.value = row;
  itemQuery.dictType = row?.dictType || '';
  itemQuery.keyword = '';
  itemRows.value = [];
  if (row?.dictType) {
    await loadItems();
  }
}

async function loadItems() {
  if (!selectedType.value) {
    return;
  }
  itemLoading.value = true;
  try {
    const page = await listDictItems(itemQuery);
    itemRows.value = page.list || [];
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '字典项加载失败');
  } finally {
    itemLoading.value = false;
  }
}

function openTypeCreate() {
  Object.assign(typeForm, {
    id: undefined,
    dictType: '',
    dictName: '',
    description: '',
    sortNo: 0,
    status: 1
  });
  typeDialogVisible.value = true;
}

function openTypeEdit(row: CampusDictType) {
  Object.assign(typeForm, row);
  typeDialogVisible.value = true;
}

function openItemCreate() {
  if (!selectedType.value) {
    return;
  }
  Object.assign(itemForm, {
    id: undefined,
    dictType: selectedType.value.dictType,
    itemCode: '',
    itemName: '',
    itemValue: '',
    description: '',
    sortNo: 0,
    status: 1
  });
  itemDialogVisible.value = true;
}

function openItemEdit(row: CampusDictItem) {
  Object.assign(itemForm, row);
  itemDialogVisible.value = true;
}

async function submitType() {
  if (!typeForm.dictType || !typeForm.dictName) {
    ElMessage.warning('类型编码和名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveDictType({ ...typeForm });
    ElMessage.success('保存成功');
    typeDialogVisible.value = false;
    selectedType.value = undefined;
    await loadTypes();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitItem() {
  if (!itemForm.dictType || !itemForm.itemCode || !itemForm.itemName) {
    ElMessage.warning('字典项编码和名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveDictItem({ ...itemForm });
    ElMessage.success('保存成功');
    itemDialogVisible.value = false;
    await loadItems();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function removeType(row: CampusDictType) {
  try {
    await ElMessageBox.confirm(`确认删除字典类型“${row.dictName}”？`, '删除确认', { type: 'warning' });
    await deleteDictType(row.dictType);
    ElMessage.success('删除成功');
    selectedType.value = undefined;
    itemRows.value = [];
    await loadTypes();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

async function removeItem(row: CampusDictItem) {
  try {
    await ElMessageBox.confirm(`确认删除字典项“${row.itemName}”？`, '删除确认', { type: 'warning' });
    await deleteDictItem(row.dictType, row.itemCode);
    ElMessage.success('删除成功');
    await loadItems();
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}
</script>
