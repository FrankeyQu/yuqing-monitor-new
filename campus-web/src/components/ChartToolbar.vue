<template>
  <div class="chart-toolbar" @click.stop>
    <el-tooltip content="复制图表数据" placement="top">
      <el-button text bg size="small" @click="handleCopy">
        <Copy :size="14" />
      </el-button>
    </el-tooltip>
    <el-tooltip content="全屏显示" placement="top">
      <el-button text bg size="small" @click="handleFullscreen">
        <Expand :size="14" />
      </el-button>
    </el-tooltip>
    <el-tooltip content="刷新" placement="top">
      <el-button text bg size="small" @click="handleRefresh">
        <RefreshCw :size="14" />
      </el-button>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { Copy, Expand, RefreshCw } from 'lucide-vue-next';
import { ElMessage } from 'element-plus';

const props = defineProps<{
  chartRef: HTMLElement | null;
  chartData: any;
  title: string;
}>();

const emit = defineEmits<{
  (e: 'refresh'): void;
}>();

function toCsv(data: any): string {
  if (!data) {
    return '';
  }
  if (Array.isArray(data)) {
    if (data.length === 0) {
      return '';
    }
    const headers = Object.keys(data[0]);
    const rows = data.map((item: Record<string, any>) =>
      headers.map((h: string) => {
        const val = item[h];
        if (val === null || val === undefined) {
          return '';
        }
        const str = String(val);
        if (str.includes(',') || str.includes('"') || str.includes('\n')) {
          return `"${str.replace(/"/g, '""')}"`;
        }
        return str;
      }).join(',')
    );
    return [headers.join(','), ...rows].join('\n');
  }
  if (typeof data === 'object') {
    return JSON.stringify(data, null, 2);
  }
  return String(data);
}

function handleCopy() {
  const csv = toCsv(props.chartData);
  if (!csv) {
    ElMessage.warning('暂无可复制的数据');
    return;
  }
  navigator.clipboard.writeText(csv).then(() => {
    ElMessage.success('数据已复制到剪贴板');
  }).catch(() => {
    ElMessage.warning('剪贴板写入失败');
  });
}

function handleFullscreen() {
  if (!props.chartRef) {
    ElMessage.warning('全屏暂不支持');
    return;
  }
  props.chartRef.requestFullscreen().catch(() => {
    ElMessage.warning('全屏暂不支持');
  });
}

function handleRefresh() {
  emit('refresh');
}
</script>
