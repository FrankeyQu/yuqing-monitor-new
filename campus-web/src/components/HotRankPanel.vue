<template>
  <el-card class="hot-rank-panel" shadow="never">
    <div class="hot-rank-header">
      <div class="hot-rank-title">
        <component :is="icon" :size="18" :color="color" />
        <span>{{ title }}</span>
      </div>
      <el-button
        v-if="items.length > 0"
        link
        type="primary"
        size="small"
        @click="openFirstLink"
      >
        <ExternalLink :size="14" style="margin-right: 2px;" />
        查看
      </el-button>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #default>
        <el-empty v-if="items.length === 0" description="暂无数据" />
        <ul v-else class="hot-rank-list">
          <li
            v-for="item in items"
            :key="item.rank"
            class="hot-rank-item"
          >
            <span class="hot-rank-num" :class="rankClass(item.rank)">
              {{ item.rank }}
            </span>
            <span class="hot-rank-title-text">{{ item.title }}</span>
            <span class="hot-rank-hot">{{ formatHot(item.hot) }}</span>
          </li>
        </ul>
      </template>

      <template #template>
        <div v-for="n in 10" :key="n" class="hot-rank-skel-row">
          <el-skeleton-item variant="text" style="width: 20px; height: 16px; border-radius: 4px;" />
          <el-skeleton-item variant="text" style="flex: 1; height: 16px;" />
          <el-skeleton-item variant="text" style="width: 48px; height: 16px;" />
        </div>
      </template>
    </el-skeleton>

    <div class="hot-rank-footer">
      数据来自 {{ title }}
    </div>
  </el-card>
</template>

<script lang="ts">
export interface HotRankItem {
  rank: number;
  title: string;
  hot: string;
  url?: string;
}
</script>

<script setup lang="ts">
import type { Component } from 'vue';
import { ExternalLink } from 'lucide-vue-next';

const props = withDefaults(defineProps<{
  title: string;
  icon: Component;
  color: string;
  items: HotRankItem[];
  loading?: boolean;
}>(), {
  loading: false
});

function formatHot(hot: string): string {
  const num = parseInt(hot.replace(/,/g, ''), 10);
  if (isNaN(num)) return hot;
  if (num >= 1000000) {
    return (num / 10000).toFixed(1) + '万';
  }
  if (num >= 10000) {
    return Math.floor(num / 10000) + '万';
  }
  return num.toLocaleString();
}

function rankClass(rank: number): string {
  if (rank === 1) return 'rank-gold';
  if (rank === 2) return 'rank-silver';
  if (rank === 3) return 'rank-bronze';
  return '';
}

function openFirstLink() {
  const item = props.items.find(item => item.url);
  if (item?.url) {
    window.open(item.url, '_blank');
  }
}
</script>

<style scoped>
.hot-rank-panel {
  border-radius: var(--radius);
}
.hot-rank-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.hot-rank-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}
.hot-rank-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.hot-rank-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 0;
  font-size: 13px;
  line-height: 20px;
  color: #334155;
}
.hot-rank-item + .hot-rank-item {
  border-top: 1px solid #f1f5f9;
}
.hot-rank-num {
  width: 20px;
  flex-shrink: 0;
  text-align: center;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  line-height: 20px;
  border-radius: 4px;
}
.rank-gold {
  color: #fff;
  background: linear-gradient(135deg, #f59e0b, #d97706);
}
.rank-silver {
  color: #fff;
  background: linear-gradient(135deg, #94a3b8, #64748b);
}
.rank-bronze {
  color: #fff;
  background: linear-gradient(135deg, #cd7f32, #a0522d);
}
.hot-rank-title-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hot-rank-hot {
  flex-shrink: 0;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
}
.hot-rank-footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e5ebf2;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
}
.hot-rank-skel-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
}
</style>
