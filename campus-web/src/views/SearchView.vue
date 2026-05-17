<template>
  <div class="search-page">
    <!-- Header -->
    <div class="search-header">
      <h2>搜索: {{ query }}</h2>
      <span style="color: #64748b; font-size: 13px;">共 {{ total }} 条</span>
    </div>

    <!-- Search & Filters -->
    <div class="screen-panel">
      <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 12px;">
        <el-input
          v-model="localQuery"
          placeholder="输入关键词搜索..."
          clearable
          style="width: 320px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <Search :size="16" />
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-radio-group v-model="mediaType" @change="handleTypeChange">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="news">新闻</el-radio-button>
        <el-radio-button value="weibo">微博</el-radio-button>
        <el-radio-button value="wechat">微信</el-radio-button>
        <el-radio-button value="video">视频</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Results -->
    <div v-if="loading" style="text-align: center; padding: 40px 0; color: #94a3b8;">
      搜索中...
    </div>
    <div v-else-if="results.length === 0" style="text-align: center; padding: 40px 0; color: #94a3b8;">
      未找到相关结果
    </div>
    <template v-else>
      <div
        v-for="item in results"
        :key="item.id"
        class="search-result-card"
      >
        <div class="search-result-title">
          <router-link :to="`/monitor/article/${item.id}`">{{ item.title }}</router-link>
        </div>
        <div class="search-result-summary">{{ item.summary }}</div>
        <div class="search-result-meta">
          <EmotionBadge :emotion="item.sentiment" />
          <PlatformBadge :platform="item.sourcePlatform" show-icon />
          <span>发布时间：{{ item.publishTime || '未知' }}</span>
          <span v-if="item.discoverTime">发现时间：{{ item.discoverTime }}</span>
          <router-link :to="`/monitor/article/${item.id}`" style="margin-left: auto; font-size: 12px; color: var(--el-color-primary); text-decoration: none;">
            查看
          </router-link>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-row">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Search } from 'lucide-vue-next';
import { searchArticles, type SearchResultItem } from '../services/search';
import EmotionBadge from '../components/EmotionBadge.vue';
import PlatformBadge from '../components/PlatformBadge.vue';

const route = useRoute();
const router = useRouter();

const query = ref('');
const localQuery = ref('');
const mediaType = ref('');
const results = ref<SearchResultItem[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);

async function fetchResults() {
  if (!query.value.trim()) {
    results.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    const res = await searchArticles(query.value, mediaType.value, page.value, pageSize.value);
    results.value = res.items;
    total.value = res.total;
  } catch {
    results.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  if (!localQuery.value.trim()) return;
  query.value = localQuery.value.trim();
  page.value = 1;
  router.replace({ query: { q: query.value } });
  fetchResults();
}

function handleTypeChange() {
  page.value = 1;
  fetchResults();
}

function handlePageChange(newPage: number) {
  page.value = newPage;
  fetchResults();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Sync from URL query on mount and on navigation
onMounted(() => {
  if (route.query.q) {
    query.value = String(route.query.q);
    localQuery.value = query.value;
    fetchResults();
  }
});

watch(
  () => route.query.q,
  (newQ) => {
    if (newQ && newQ !== query.value) {
      query.value = String(newQ);
      localQuery.value = query.value;
      page.value = 1;
      fetchResults();
    }
  }
);
</script>
