<template>
  <el-tag
    :type="tagType"
    :class="emotionClass"
    size="small"
    effect="plain"
  >
    <component :is="iconComponent" :size="14" v-if="showIcon" style="margin-right: 3px; vertical-align: middle;" />
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ThumbsUp, Minus, ThumbsDown, HelpCircle } from 'lucide-vue-next';

const props = withDefaults(defineProps<{
  emotion: number | string;
  showIcon?: boolean;
}>(), {
  showIcon: false,
});

type EmotionMeta = {
  label: string;
  tagType: string;
  emotionClass: string;
  iconComponent: any;
};

function resolveEmotion(value: number | string): EmotionMeta {
  const normalized = String(value).toLowerCase();
  // positive / 正面
  if (normalized === '1' || normalized === 'positive' || normalized === '正面' || normalized.includes('正面')) {
    return {
      label: '正面',
      tagType: 'success',
      emotionClass: 'emotion-positive',
      iconComponent: ThumbsUp,
    };
  }
  // neutral / 中性
  if (normalized === '2' || normalized === 'neutral' || normalized === '中性' || normalized.includes('中性')) {
    return {
      label: '中性',
      tagType: 'warning',
      emotionClass: 'emotion-neutral',
      iconComponent: Minus,
    };
  }
  // negative / 负面
  if (normalized === '3' || normalized === 'negative' || normalized === '负面' || normalized.includes('负面')) {
    return {
      label: '负面',
      tagType: 'danger',
      emotionClass: 'emotion-negative',
      iconComponent: ThumbsDown,
    };
  }
  return {
    label: '未知',
    tagType: 'info',
    emotionClass: 'emotion-unknown',
    iconComponent: HelpCircle,
  };
}

const meta = computed(() => resolveEmotion(props.emotion));
const label = computed(() => meta.value.label);
const tagType = computed(() => meta.value.tagType);
const emotionClass = computed(() => meta.value.emotionClass);
const iconComponent = computed(() => props.showIcon ? meta.value.iconComponent : null);
</script>
