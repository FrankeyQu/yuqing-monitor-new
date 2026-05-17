<template>
  <section class="settings-page">
    <SettingsNav />

    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="角色管理" name="roles">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="roleQuery.keyword" clearable placeholder="角色编码/名称" @keyup.enter="loadRoles">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="roleQuery.roleType" clearable placeholder="角色类型">
                <el-option label="管理员" value="admin" />
                <el-option label="业务角色" value="business" />
                <el-option label="查看角色" value="viewer" />
              </el-select>
              <el-select v-model="roleQuery.status" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadRoles">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openRoleCreate">
              <Plus :size="16" />
              新增角色
            </el-button>
          </div>

          <el-table :data="roles" v-loading="roleLoading" size="small" height="540">
            <el-table-column prop="roleName" label="角色名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="roleCode" label="角色编码" width="160" show-overflow-tooltip />
            <el-table-column prop="roleType" label="类型" width="92">
              <template #default="{ row }">{{ roleTypeLabel(row.roleType) }}</template>
            </el-table-column>
            <el-table-column prop="dataScope" label="数据范围" width="102">
              <template #default="{ row }">{{ dataScopeLabel(row.dataScope) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.status === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="190" show-overflow-tooltip />
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openRoleEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="success" @click="openMenuAssign(row)">
                  <LayoutList :size="15" />
                  菜单
                </el-button>
                <el-button link type="warning" @click="openApiAssign(row)">
                  <ShieldCheck :size="15" />
                  接口
                </el-button>
                <el-button link type="danger" :disabled="row.roleCode === 'campus_admin'" @click="submitRoleDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="roleQuery.pageNum"
              v-model:page-size="roleQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="roleTotal"
              @size-change="loadRoles"
              @current-change="loadRoles"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="接口权限" name="apis">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="apiQuery.keyword" clearable placeholder="接口编码/路径" @keyup.enter="loadApis">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input v-model.trim="apiQuery.moduleName" clearable placeholder="模块" @keyup.enter="loadApis" />
              <el-select v-model="apiQuery.status" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadApis">
                <Search :size="16" />
                查询
              </el-button>
            </div>
          </div>

          <el-table :data="apis" v-loading="apiLoading" size="small" height="540">
            <el-table-column prop="apiName" label="接口名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="apiCode" label="权限编码" min-width="170" show-overflow-tooltip />
            <el-table-column prop="moduleName" label="模块" width="110" show-overflow-tooltip />
            <el-table-column prop="requestMethod" label="方法" width="78" />
            <el-table-column prop="requestPath" label="路径" min-width="220" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.status === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="apiQuery.pageNum"
              v-model:page-size="apiQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="apiTotal"
              @size-change="loadApis"
              @current-change="loadApis"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="roleDialogVisible" :title="roleForm.roleId ? '编辑角色' : '新增角色'" width="680px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="角色编码" required>
            <el-input v-model.trim="roleForm.roleCode" :disabled="roleForm.roleCode === 'campus_admin'" />
          </el-form-item>
          <el-form-item label="角色名称" required>
            <el-input v-model.trim="roleForm.roleName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="角色类型">
            <el-select v-model="roleForm.roleType">
              <el-option label="管理员" value="admin" />
              <el-option label="业务角色" value="business" />
              <el-option label="查看角色" value="viewer" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据范围">
            <el-select v-model="roleForm.dataScope">
              <el-option label="全校" value="school" />
              <el-option label="本部门" value="department" />
              <el-option label="本人" value="self" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="状态">
            <el-switch v-model="roleEnabled" active-text="启用" inactive-text="停用" :disabled="roleForm.roleCode === 'campus_admin'" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model.trim="roleForm.remark" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="680px">
      <div class="permission-checks">
        <el-checkbox-group v-model="checkedMenuIds">
          <el-checkbox v-for="item in flatMenus" :key="item.menuId" :label="item.menuId">
            {{ item.menuName }} <span>{{ item.routePath }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitMenus">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="apiDialogVisible" title="分配接口权限" width="760px">
      <div class="permission-checks">
        <el-checkbox-group v-model="checkedApiIds">
          <el-checkbox v-for="item in allApis" :key="item.apiId" :label="item.apiId">
            {{ item.apiName }} <span>{{ item.requestMethod }} {{ item.requestPath }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="apiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitApis">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { LayoutList, Pencil, Plus, Search, ShieldCheck, Trash2 } from 'lucide-vue-next';
import SettingsNav from '../../components/SettingsNav.vue';
import {
  assignRoleApis,
  assignRoleMenus,
  deleteRole,
  listApis,
  listMenus,
  listRoleApiIds,
  listRoleMenuIds,
  listRoles,
  saveRole
} from '../../services/permission';
import type { CampusPermissionApi, CampusPermissionMenu, CampusPermissionRole } from '../../types/api';

const activeTab = ref('roles');
const saving = ref(false);
const roleLoading = ref(false);
const apiLoading = ref(false);
const roleDialogVisible = ref(false);
const menuDialogVisible = ref(false);
const apiDialogVisible = ref(false);
const roles = ref<CampusPermissionRole[]>([]);
const apis = ref<CampusPermissionApi[]>([]);
const allApis = ref<CampusPermissionApi[]>([]);
const menuTree = ref<CampusPermissionMenu[]>([]);
const roleTotal = ref(0);
const apiTotal = ref(0);
const currentRole = ref<CampusPermissionRole>();
const checkedMenuIds = ref<number[]>([]);
const checkedApiIds = ref<number[]>([]);

const roleQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  roleType: '',
  status: undefined as number | undefined
});
const apiQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  moduleName: '',
  status: undefined as number | undefined
});
const roleForm = reactive<CampusPermissionRole>({
  roleCode: '',
  roleName: '',
  roleType: 'business',
  dataScope: 'school',
  status: 1,
  remark: ''
});

const roleEnabled = computed({
  get: () => roleForm.status !== 0,
  set: (value: boolean) => {
    roleForm.status = value ? 1 : 0;
  }
});
const flatMenus = computed(() => flattenMenus(menuTree.value));

onMounted(loadRoles);
watch(activeTab, (tab) => {
  if (tab === 'apis') {
    loadApis();
  }
});

async function loadRoles() {
  roleLoading.value = true;
  try {
    const page = await listRoles(roleQuery);
    roles.value = page.list || [];
    roleTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '角色列表加载失败');
  } finally {
    roleLoading.value = false;
  }
}

async function loadApis() {
  apiLoading.value = true;
  try {
    const page = await listApis(apiQuery);
    apis.value = page.list || [];
    apiTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '接口权限加载失败');
  } finally {
    apiLoading.value = false;
  }
}

function openRoleCreate() {
  Object.assign(roleForm, {
    roleId: undefined,
    roleCode: '',
    roleName: '',
    roleType: 'business',
    dataScope: 'school',
    status: 1,
    remark: ''
  });
  roleDialogVisible.value = true;
}

function openRoleEdit(row: CampusPermissionRole) {
  Object.assign(roleForm, row);
  roleDialogVisible.value = true;
}

async function submitRole() {
  if (!roleForm.roleCode || !roleForm.roleName) {
    ElMessage.warning('角色编码和角色名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveRole({ ...roleForm });
    ElMessage.success('角色已保存');
    roleDialogVisible.value = false;
    await loadRoles();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitRoleDelete(row: CampusPermissionRole) {
  if (!row.roleId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该角色？', '删除确认', { type: 'warning' });
    await deleteRole(row.roleId);
    ElMessage.success('角色已删除');
    await loadRoles();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

async function openMenuAssign(row: CampusPermissionRole) {
  if (!row.roleId) {
    return;
  }
  currentRole.value = row;
  try {
    const [menus, menuIds] = await Promise.all([listMenus(), listRoleMenuIds(row.roleId)]);
    menuTree.value = menus;
    checkedMenuIds.value = menuIds;
    menuDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '菜单权限加载失败');
  }
}

async function openApiAssign(row: CampusPermissionRole) {
  if (!row.roleId) {
    return;
  }
  currentRole.value = row;
  try {
    const [page, apiIds] = await Promise.all([
      listApis({ pageNum: 1, pageSize: 100, keyword: '', moduleName: '', status: 1 }),
      listRoleApiIds(row.roleId)
    ]);
    allApis.value = page.list || [];
    checkedApiIds.value = apiIds;
    apiDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '接口权限加载失败');
  }
}

async function submitMenus() {
  if (!currentRole.value?.roleId) {
    return;
  }
  saving.value = true;
  try {
    await assignRoleMenus(currentRole.value.roleId, checkedMenuIds.value);
    ElMessage.success('菜单权限已保存');
    menuDialogVisible.value = false;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitApis() {
  if (!currentRole.value?.roleId) {
    return;
  }
  saving.value = true;
  try {
    await assignRoleApis(currentRole.value.roleId, checkedApiIds.value);
    ElMessage.success('接口权限已保存');
    apiDialogVisible.value = false;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function flattenMenus(menus: CampusPermissionMenu[]) {
  const rows: CampusPermissionMenu[] = [];
  menus.forEach((menu) => {
    rows.push(menu);
    if (menu.children?.length) {
      rows.push(...flattenMenus(menu.children));
    }
  });
  return rows;
}

function roleTypeLabel(value?: string) {
  const labels: Record<string, string> = { admin: '管理员', business: '业务角色', viewer: '查看角色' };
  return labels[value || 'business'] || value || '业务角色';
}

function dataScopeLabel(value?: string) {
  const labels: Record<string, string> = { school: '全校', department: '本部门', self: '本人' };
  return labels[value || 'school'] || value || '全校';
}
</script>
