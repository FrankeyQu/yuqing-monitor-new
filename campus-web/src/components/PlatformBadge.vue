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

const props = withDefaults(defineProps<{
  platform: string;
  showIcon?: boolean;
}>(), {
  showIcon: false,
});

type PlatformEntry = {
  label: string;
  cssClass: string;
};

const platformMap: { match: string[]; entry: PlatformEntry }[] = [
  { match: ['新浪微博', '微博', 'weibo'], entry: { label: '微博', cssClass: 'platform-weibo' } },
  { match: ['抖音短视频', '抖音', 'douyin'], entry: { label: '抖音', cssClass: 'platform-douyin' } },
  { match: ['微信公众号', '微信', 'wechat'], entry: { label: '微信', cssClass: 'platform-wechat' } },
  { match: ['百度贴吧', '贴吧', 'tieba'], entry: { label: '贴吧', cssClass: 'platform-tieba' } },
  { match: ['知乎', 'zhihu'], entry: { label: '知乎', cssClass: 'platform-zhihu' } },
  { match: ['小红书', 'red', 'xiaohongshu'], entry: { label: '小红书', cssClass: 'platform-xiaohongshu' } },
  { match: ['腾讯网', '腾讯', 'tencent'], entry: { label: '腾讯', cssClass: 'platform-tencent' } },
  { match: ['搜狐', 'sohu'], entry: { label: '搜狐', cssClass: 'platform-sohu' } },
  { match: ['新浪网', '新浪', 'sina'], entry: { label: '新浪', cssClass: 'platform-sina' } },
  { match: ['今日头条', '头条', 'toutiao'], entry: { label: '头条', cssClass: 'platform-toutiao' } },
  { match: ['人民网', '人民', 'people'], entry: { label: '人民网', cssClass: 'platform-people' } },
  { match: ['新华网', '新华', 'xinhua'], entry: { label: '新华网', cssClass: 'platform-xinhua' } },
  { match: ['网易', '网易号', '163'], entry: { label: '网易', cssClass: 'platform-163' } },
  { match: ['百家号', 'baijia'], entry: { label: '百家号', cssClass: 'platform-baijiahao' } },
];

const DEFAULT_ENTRY: PlatformEntry = { label: '其他', cssClass: 'platform-other' };

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
const iconComponent = computed(() => null);
</script>
