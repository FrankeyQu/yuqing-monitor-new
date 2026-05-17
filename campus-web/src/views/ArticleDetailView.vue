<template>
  <section class="business-page">
    <!-- 顶部导航 -->
    <div class="detail-header">
      <el-button text @click="goBack">
        <ArrowLeft :size="16" style="margin-right: 4px" />
        返回监测信息
      </el-button>
      <h2 class="detail-title">{{ article?.title || '文章详情' }}</h2>
    </div>

    <!-- 文章元信息 -->
    <section class="panel detail-meta" v-if="article">
      <div class="meta-row">
        <EmotionBadge :emotion="article.sentiment" :show-icon="true" />
        <PlatformBadge :platform="article.sourcePlatform" :show-icon="true" />
        <span class="meta-time">发布时间：{{ article.publishTime || '未知' }}</span>
        <span v-if="article.discoverTime" class="meta-time">发现时间：{{ article.discoverTime }}</span>
        <el-tag
          v-for="kw in article.matchedKeywords"
          :key="kw"
          type=""
          effect="plain"
          size="small"
        >
          {{ kw }}
        </el-tag>
        <el-tag
          :type="riskTagType(article.riskLevel)"
          effect="dark"
          size="small"
        >
          {{ riskLabel(article.riskLevel) }}
        </el-tag>
      </div>
    </section>

    <!-- 正文内容 -->
    <section class="panel detail-content">
      <h3 class="section-title">正文内容</h3>
      <el-skeleton :loading="loading" animated :count="5">
        <div class="content-text" v-if="article?.content">
          {{ article.content }}
        </div>
        <el-empty v-else description="暂无正文内容" />
      </el-skeleton>
    </section>

    <!-- 相似文章 -->
    <section class="panel detail-similar" v-if="article?.similarArticles?.length">
      <h3 class="section-title">相似文章</h3>
      <div
        v-for="item in article.similarArticles"
        :key="item.id"
        class="similar-item"
        @click="goToSimilar(item.id)"
      >
        <div class="similar-item-left">
          <span class="similar-title">{{ item.title }}</span>
          <div class="similar-meta">
            <PlatformBadge :platform="item.sourcePlatform" />
            <EmotionBadge :emotion="item.sentiment" />
            <el-tag
              :type="riskTagType(item.riskLevel)"
              effect="plain"
              size="small"
            >
              {{ riskLabel(item.riskLevel) }}
            </el-tag>
          </div>
        </div>
        <ArrowRight :size="16" class="similar-arrow" />
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, ArrowRight } from 'lucide-vue-next';
import EmotionBadge from '../components/EmotionBadge.vue';
import PlatformBadge from '../components/PlatformBadge.vue';
import { fetchArticleDetail } from '../services/articleDetail';
import { campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type { ArticleDetailData } from '../services/articleDetail';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const article = ref<ArticleDetailData | null>(null);
const articleId = Number(route.params.id);

function goBack() {
  router.push('/monitor');
}

function goToSimilar(id: number) {
  router.push(`/monitor/article/${id}`);
}

function riskLabel(value?: string) {
  const labels: Record<string, string> = { high: '高风险', medium: '中风险', low: '低风险' };
  return labels[value || 'low'] || campusRiskLabel(value, '低风险');
}

function riskTagType(value?: string) {
  if (value === 'high') return 'danger';
  if (value === 'medium') return 'warning';
  return campusRiskTagType(value);
}

onMounted(async () => {
  try {
    article.value = await fetchArticleDetail(articleId);
  } catch (error) {
    // 错误由 http.ts 拦截处理，此处保持静默
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.detail-meta {
  margin-bottom: 16px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-time {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.detail-content {
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.content-text {
  line-height: 1.8;
  font-size: 14px;
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

.similar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  cursor: pointer;
  border-bottom: 1px solid var(--el-border-color-light);
  transition: background-color 0.2s;
}

.similar-item:last-child {
  border-bottom: none;
}

.similar-item:hover {
  background-color: var(--el-fill-color-light);
}

.similar-item-left {
  flex: 1;
  min-width: 0;
}

.similar-title {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-color-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.similar-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.similar-arrow {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
  margin-left: 12px;
}
</style>
