<template>
  <div class="admin-shell">
    <header class="admin-topbar">
      <div class="admin-topbar-left">
        <el-button text @click="goBack">
          <ArrowLeft :size="18" />
          返回前台
        </el-button>
        <el-divider direction="vertical" />
        <span class="admin-title">后台管理</span>
      </div>
      <div class="admin-topbar-right">
        <span class="user-name">{{ loginName }}</span>
        <el-button type="primary" plain size="small" @click="handleLogout">
          <LogOut :size="16" />
          退出
        </el-button>
      </div>
    </header>
    <div class="admin-nav">
      <el-button
        v-for="item in navItems"
        :key="item.path"
        :type="isNavActive(item) ? 'primary' : 'default'"
        @click="$router.push(item.path)"
        size="small"
      >
        {{ item.label }}
      </el-button>
    </div>
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft, LogOut } from 'lucide-vue-next';
import { currentLoginName, clearLoginState } from '../services/auth';
import { getCurrentCampusUser } from '../services/permission';
import type { CampusPermissionMenu } from '../types/api';

const router = useRouter();
const route = useRoute();

interface AdminNavItem {
  path: string;
  label: string;
  permissions: string[];
  routes: string[];
}

const allNavItems: AdminNavItem[] = [
  { path: '/admin/accounts', label: '重点账号', permissions: ['campus:account:view'], routes: ['/admin/accounts', '/accounts'] },
  { path: '/admin/monitor-tasks', label: '监测任务管理', permissions: ['campus:monitor:view'], routes: ['/admin/monitor-tasks'] },
  { path: '/admin/settings/ai', label: 'AI能力', permissions: ['campus:ai:view'], routes: ['/admin/settings/ai'] },
  { path: '/admin/education', label: '教育专题', permissions: ['campus:education:view'], routes: ['/admin/education'] },
  {
    path: '/admin/settings/departments',
    label: '系统设置',
    permissions: ['campus:system:view', 'campus:permission:view'],
    routes: ['/admin/settings/departments', '/admin/settings/dicts', '/admin/settings/audit', '/admin/settings/permissions']
  }
];
const navItems = ref<AdminNavItem[]>([]);

const loginName = computed(() => currentLoginName());
const currentPath = computed(() => route.path);

onMounted(() => {
  loadAdminNav();
});

async function loadAdminNav() {
  try {
    const current = await getCurrentCampusUser();
    const permissionSet = new Set(current.permissions || []);
    const routeSet = new Set(flattenMenus(current.menus || []).map((menu) => menu.routePath).filter(Boolean) as string[]);
    if (permissionSet.has('role:campus_admin') || permissionSet.has('campus:api:all')) {
      navItems.value = allNavItems;
      redirectIfCurrentRouteUnavailable();
      return;
    }
    const filtered = allNavItems.filter((item) => {
      return item.permissions.some((code) => permissionSet.has(code))
        || item.routes.some((path) => routeSet.has(path));
    });
    navItems.value = filtered;
    redirectIfCurrentRouteUnavailable();
  } catch {
    navItems.value = [];
    router.replace('/');
  }
}

function redirectIfCurrentRouteUnavailable() {
  if (navItems.value.length === 0) {
    router.replace('/');
    return;
  }
  if (!navItems.value.some((item) => isNavActive(item))) {
    router.replace(navItems.value[0].path);
  }
}

function flattenMenus(menus: CampusPermissionMenu[]) {
  const result: CampusPermissionMenu[] = [];
  const stack = [...menus];
  while (stack.length > 0) {
    const item = stack.shift();
    if (!item) continue;
    result.push(item);
    if (item.children?.length) {
      stack.push(...item.children);
    }
  }
  return result;
}

function goBack() {
  router.push('/');
}

function isNavActive(item: AdminNavItem) {
  return item.routes.some((path) => currentPath.value === path || currentPath.value.startsWith(path + '/'));
}

function handleLogout() {
  clearLoginState();
  router.replace('/login');
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  background: #edf1f6;
  display: flex;
  flex-direction: column;
}
.admin-topbar {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #d8e0ea;
  flex-shrink: 0;
}
.admin-topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}
.admin-nav {
  padding: 10px 20px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  background: #ffffff;
  border-bottom: 1px solid #d8e0ea;
  flex-shrink: 0;
}
.admin-content {
  padding: 18px;
  flex: 1;
  overflow-y: auto;
}
.user-name {
  color: #334155;
  font-size: 13px;
}
</style>
