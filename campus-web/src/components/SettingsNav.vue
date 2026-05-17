<template>
  <div class="settings-nav">
    <el-button
      v-for="item in items"
      :key="item.path"
      :type="$route.path === item.path ? 'primary' : 'default'"
      :plain="$route.path !== item.path"
      @click="$router.push(item.path)"
    >
      <component :is="item.icon" :size="16" />
      {{ item.label }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { BookOpenText, Building2, ScrollText, ShieldCheck } from 'lucide-vue-next';

const items = [
  { path: '/settings/departments', label: '部门', icon: Building2 },
  { path: '/settings/dicts', label: '字典', icon: BookOpenText },
  { path: '/settings/audit', label: '审计', icon: ScrollText },
  { path: '/settings/permissions', label: '权限', icon: ShieldCheck }
];
</script>
