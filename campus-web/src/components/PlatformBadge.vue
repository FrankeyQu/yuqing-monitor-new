<template>
  <el-tag
    :class="['platform-badge', resolvedClass]"
    size="small"
    effect="plain"
    disable-transitions
  >
    <component :is="iconComponent" :size="14" v-if="showIcon" />
    {{ resolvedLabel }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import {
  BookOpenText,
  Globe2,
  MessageCircle,
  MessagesSquare,
  Newspaper,
  Video
} from 'lucide-vue-next';

const props = withDefaults(defineProps<{
  platform: string;
  showIcon?: boolean;
}>(), {
  showIcon: false,
});

type PlatformEntry = {
  label: string;
  cssClass: string;
  icon: Component;
};

const platformMap: { match: string[]; entry: PlatformEntry }[] = [
  { match: ['新浪微博', '微博', 'weibo'], entry: { label: '微博', cssClass: 'platform-weibo', icon: MessageCircle } },
  { match: ['抖音短视频', '抖音', 'douyin', '视频'], entry: { label: '抖音', cssClass: 'platform-douyin', icon: Video } },
  { match: ['微信公众号', '微信', 'wechat'], entry: { label: '微信', cssClass: 'platform-wechat', icon: MessagesSquare } },
  { match: ['百度贴吧', '贴吧', 'tieba', '论坛'], entry: { label: '贴吧', cssClass: 'platform-tieba', icon: MessageCircle } },
  { match: ['知乎', 'zhihu'], entry: { label: '知乎', cssClass: 'platform-zhihu', icon: BookOpenText } },
  { match: ['小红书', 'red', 'xiaohongshu'], entry: { label: '小红书', cssClass: 'platform-xiaohongshu', icon: BookOpenText } },
  { match: ['腾讯网', '腾讯', 'tencent'], entry: { label: '腾讯', cssClass: 'platform-tencent', icon: Newspaper } },
  { match: ['搜狐', 'sohu'], entry: { label: '搜狐', cssClass: 'platform-sohu', icon: Newspaper } },
  { match: ['新浪网', '新浪', 'sina'], entry: { label: '新浪', cssClass: 'platform-sina', icon: Newspaper } },
  { match: ['今日头条', '头条', 'toutiao'], entry: { label: '头条', cssClass: 'platform-toutiao', icon: Newspaper } },
  { match: ['人民网', '人民', 'people'], entry: { label: '人民网', cssClass: 'platform-people', icon: Newspaper } },
  { match: ['新华网', '新华', 'xinhua'], entry: { label: '新华网', cssClass: 'platform-xinhua', icon: Newspaper } },
  { match: ['网易', '网易号', '163'], entry: { label: '网易', cssClass: 'platform-163', icon: Newspaper } },
  { match: ['百家号', 'baijia'], entry: { label: '百家号', cssClass: 'platform-baijiahao', icon: Newspaper } },
];

const DEFAULT_ENTRY: PlatformEntry = { label: '其他', cssClass: 'platform-other', icon: Globe2 };

function resolvePlatform(input: string): PlatformEntry {
  const normalized = (input || '').trim().toLowerCase();
  if (!normalized) {
    return DEFAULT_ENTRY;
  }
  for (const group of platformMap) {
    for (const keyword of group.match) {
      if (normalized.includes(keyword.toLowerCase())) {
        return group.entry;
      }
    }
  }
  return DEFAULT_ENTRY;
}

const resolved = computed(() => resolvePlatform(props.platform));
const resolvedLabel = computed(() => resolved.value.label);
const resolvedClass = computed(() => resolved.value.cssClass);
const iconComponent = computed(() => props.showIcon ? resolved.value.icon : null);
</script>
