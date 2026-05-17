<template>
  <div class="app-shell">
    <aside class="app-aside" :class="{ 'is-collapsed': collapsed }">
      <div class="brand-row">
        <img class="brand-mark" src="../assets/campus-mark.svg" alt="" />
        <div v-if="!collapsed" class="brand-copy">
          <strong>{{ productName }}</strong>
          <span>{{ englishName }}</span>
        </div>
      </div>

      <el-menu
        class="side-menu"
        :default-active="activePath"
        :collapse="collapsed"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <component :is="item.icon" class="menu-icon" />
          <template #title>
            <span class="menu-title">
              <span>{{ item.label }}</span>
            </span>
          </template>
        </el-menu-item>
        <el-divider v-if="showAdminEntry" style="margin: 8px 12px" />
        <el-menu-item v-if="showAdminEntry" index="/admin" @click="$router.push('/admin')">
          <Settings :size="18" class="menu-icon" />
          <template #title>
            <span>后台管理</span>
          </template>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="app-main">
      <header class="topbar">
        <div class="topbar-left">
          <el-button class="icon-button" :aria-label="collapsed ? '展开菜单' : '收起菜单'" @click="collapsed = !collapsed">
            <PanelLeftOpen v-if="collapsed" :size="18" />
            <PanelLeftClose v-else :size="18" />
          </el-button>
          <div>
            <h1>{{ pageTitle }}</h1>
            <p>舆情监测 · 分析 · 报告</p>
          </div>
        </div>

        <div class="topbar-right">
          <el-autocomplete
            v-model="searchQuery"
            :fetch-suggestions="searchSuggestions"
            placeholder="搜索线索、关键词..."
            :trigger-on-focus="false"
            size="small"
            clearable
            style="width: 220px"
            @select="handleSearchSelect"
            @keyup.enter="handleSearchEnter"
          >
            <template #prefix>
              <Search :size="16" />
            </template>
          </el-autocomplete>
          <el-tag effect="plain" type="success">{{ companyName }}</el-tag>
          <span class="user-name">{{ loginName }}</span>
          <el-button type="success" plain @click="openPasswordDialog">
            <LockKeyhole :size="16" />
            设置新密码
          </el-button>
          <el-button type="primary" plain @click="handleLogout">
            <LogOut :size="16" />
            退出
          </el-button>
        </div>
      </header>

      <el-dialog v-model="passwordDialogVisible" title="设置新密码" width="420px" append-to-body>
        <el-form :model="passwordForm" label-position="top" @submit.prevent>
          <el-form-item label="新密码">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              autocomplete="new-password"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              autocomplete="new-password"
              show-password
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="passwordSubmitting" @click="submitNewPassword">
            保存
          </el-button>
        </template>
      </el-dialog>

      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, type Component } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Bell,
  BrainCircuit,
  ClipboardList,
  FileText,
  Gauge,
  LockKeyhole,
  LogOut,
  PanelLeftClose,
  PanelLeftOpen,
  Scale,
  Search,
  Settings,
  ShieldAlert,
} from 'lucide-vue-next';
import { clearLoginState, currentLoginName, setNewPassword } from '../services/auth';
import { COMPANY_NAME, PRODUCT_EN_NAME, PRODUCT_NAME } from '../config/brand';
import { getCampusMenuTree, getCurrentCampusUser } from '../services/permission';
import { suggestClues } from '../services/campusBusiness';
import type { CampusPermissionMenu } from '../types/api';

const collapsed = ref(false);
const route = useRoute();
const router = useRouter();
const companyName = COMPANY_NAME;
const englishName = PRODUCT_EN_NAME;
const productName = PRODUCT_NAME;
const passwordDialogVisible = ref(false);
const passwordSubmitting = ref(false);
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
});

interface MenuItem {
  path: string;
  label: string;
  icon: Component;
}

const iconMap: Record<string, Component> = {
  Bell,
  BrainCircuit,
  ClipboardList,
  FileText,
  Gauge,
  Scale,
  Settings,
  ShieldAlert,
};

const menuItems = ref<MenuItem[]>([]);
const showAdminEntry = ref(false);

onMounted(() => {
  loadMenus();
});

const activePath = computed(() => {
  if (route.path === '/') {
    return '/';
  }
  if (route.path.startsWith('/settings')) {
    return '/settings/departments';
  }
  return `/${route.path.split('/')[1]}`;
});
const pageTitle = computed(() => String(route.meta.title || '工作台'));
const loginName = computed(() => currentLoginName());
const searchQuery = ref('');

interface SearchSuggestion {
  value: string;
  label?: string;
}

async function searchSuggestions(query: string, cb: (results: SearchSuggestion[]) => void) {
  if (!query || query.length < 2) {
    cb([]);
    return;
  }
  try {
    const titles = await suggestClues(query);
    const suggestions: SearchSuggestion[] = titles.map((title) => ({
      value: title,
      label: title
    }));
    cb(suggestions);
  } catch {
    cb([{ value: query, label: `搜索 "${query}"` }]);
  }
}

function handleSearchSelect(item: SearchSuggestion) {
  router.push(`/search?q=${encodeURIComponent(item.value)}`);
}

function handleSearchEnter() {
  if (searchQuery.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchQuery.value.trim())}`);
  }
}

function handleLogout() {
  clearLoginState();
  router.replace('/login');
}

function openPasswordDialog() {
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordDialogVisible.value = true;
}

async function submitNewPassword() {
  if (!passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写新密码');
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致');
    return;
  }
  passwordSubmitting.value = true;
  try {
    await setNewPassword({
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    });
    ElMessage.success('新密码已设置');
    passwordDialogVisible.value = false;
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '设置失败');
  } finally {
    passwordSubmitting.value = false;
  }
}

const FRONT_MENU_PATHS = new Set([
  '/',
  '/situation',
  '/monitor',
  '/judgment',
  '/events',
  '/alerts',
  '/analysis',
  '/reports',
  '/report-templates',
  '/auto-reports',
  '/compare',
  '/search'
]);
const ADMIN_MENU_HINTS = new Set([
  '/accounts',
  '/monitor-tasks',
  '/detection',
  '/education',
  '/settings/departments',
  '/settings/dicts',
  '/settings/audit',
  '/settings/permissions',
  '/settings/ai'
]);
const LEGACY_HIDDEN_PATHS = new Set(['/clues']);

async function loadMenus() {
  try {
    const [menus, currentUser] = await Promise.all([getCampusMenuTree(), getCurrentCampusUser()]);
    const flattened = flattenMenus(menus);
    const permissionSet = new Set(currentUser.permissions || []);
    const userMenuRoutes = flattenMenus(currentUser.menus || []).map((item) => item.routePath || '');
    showAdminEntry.value = permissionSet.has('role:campus_admin')
      || permissionSet.has('campus:api:all')
      || userMenuRoutes.some((path) => path.startsWith('/admin') || ADMIN_MENU_HINTS.has(path));
    const items = flattenMenus(menus)
      .filter((item) =>
        item.routePath
        && FRONT_MENU_PATHS.has(item.routePath)
        && !LEGACY_HIDDEN_PATHS.has(item.routePath)
      )
      .map<MenuItem>((item) => ({
        path: item.routePath || '/',
        label: item.menuName,
        icon: iconMap[item.icon || ''] || Settings
      }));
    if (items.length > 0) {
      menuItems.value = items;
    } else {
      menuItems.value = flattened.some((item) => FRONT_MENU_PATHS.has(item.routePath || '')) ? items : [];
    }
  } catch {
    menuItems.value = [];
    showAdminEntry.value = false;
  }
}

function flattenMenus(menus: CampusPermissionMenu[]) {
  const items: CampusPermissionMenu[] = [];
  menus.forEach((menu) => {
    items.push(menu);
    if (menu.children?.length) {
      items.push(...flattenMenus(menu.children));
    }
  });
  return items;
}
</script>
