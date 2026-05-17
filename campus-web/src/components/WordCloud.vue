<template>
  <div ref="containerRef" class="word-cloud-container" :style="{ minHeight: minHeight + 'px' }">
    <canvas
      ref="canvasRef"
      class="word-cloud-canvas"
      @click="handleCanvasClick"
    />
    <div v-if="!words.length && !loading" class="wc-empty">
      <span>暂无词云数据</span>
    </div>
    <div v-if="loading" class="wc-loading">
      <span>加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch, type PropType } from 'vue';

interface WordItem {
  name: string;
  value: number;
}

interface PlacedWord {
  text: string;
  x: number;
  y: number;
  w: number;
  h: number;
  color: string;
  fontSize: number;
  value: number;
}

const props = defineProps({
  words: {
    type: Array as PropType<WordItem[]>,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  minHeight: {
    type: Number,
    default: 280
  }
});

const emit = defineEmits<{
  (e: 'word-click', word: WordItem): void;
}>();

const containerRef = ref<HTMLElement | null>(null);
const canvasRef = ref<HTMLCanvasElement | null>(null);

const COLORS = [
  '#3D5AFE', '#2563eb', '#1d4ed8', '#4f46e5', '#7c3aed',
  '#0891b2', '#0d9488', '#0f766e', '#15803d', '#16a34a',
  '#ca8a04', '#d97706', '#ea580c', '#dc2626', '#e11d48',
  '#db2777', '#9333ea', '#0284c7', '#059669', '#b91c1c'
];

const FONT_FAMILY = '"PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif';
const MIN_FONT = 12;
const MAX_FONT = 46;
const PADDING = 4;

let placedWords: PlacedWord[] = [];
let resizeObserver: ResizeObserver | null = null;
let dpr = 1;

onMounted(() => {
  if (containerRef.value) {
    resizeObserver = new ResizeObserver(() => {
      renderCloud();
    });
    resizeObserver.observe(containerRef.value);
  }
  renderCloud();
});

onBeforeUnmount(() => {
  resizeObserver?.disconnect();
});

watch(
  () => props.words,
  () => {
    renderCloud();
  },
  { deep: true }
);

function renderCloud() {
  const container = containerRef.value;
  const canvas = canvasRef.value;
  if (!container || !canvas) return;

  const rect = container.getBoundingClientRect();
  const w = rect.width || 400;
  const h = Math.max(rect.height || props.minHeight, props.minHeight);

  dpr = window.devicePixelRatio || 1;
  canvas.width = w * dpr;
  canvas.height = h * dpr;
  canvas.style.width = w + 'px';
  canvas.style.height = h + 'px';

  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  ctx.scale(dpr, dpr);
  ctx.clearRect(0, 0, w, h);

  const words = [...props.words];
  if (!words.length) {
    drawEmptyText(ctx, w, h);
    return;
  }

  words.sort((a, b) => b.value - a.value);

  const maxValue = words[0].value || 1;
  const minValue = words[words.length - 1].value || 0;

  placedWords = [];
  const centerX = w / 2;
  const centerY = h / 2;
  const spiralStep = 1;
  const angleStep = 0.15;

  for (const word of words) {
    const fontSize = mapFontSize(word.value, minValue, maxValue);
    ctx.font = `bold ${fontSize}px ${FONT_FAMILY}`;
    const metrics = ctx.measureText(word.name);
    const textW = metrics.width + PADDING * 2;
    const textH = fontSize + PADDING * 2;

    const color = COLORS[Math.floor(Math.random() * COLORS.length)];
    const placed = tryPlaceWord(word.name, textW, textH, centerX, centerY, spiralStep, angleStep, w, h);

    if (placed) {
      placedWords.push({
        text: word.name,
        x: placed.x,
        y: placed.y,
        w: textW,
        h: textH,
        color,
        fontSize,
        value: word.value
      });
      drawWord(ctx, word.name, placed.x, placed.y, fontSize, color, PADDING);
    }
  }
}

function mapFontSize(value: number, minVal: number, maxVal: number): number {
  if (maxVal === minVal) return (MIN_FONT + MAX_FONT) / 2;
  const ratio = (value - minVal) / (maxVal - minVal);
  return MIN_FONT + ratio * (MAX_FONT - MIN_FONT);
}

function tryPlaceWord(
  text: string,
  w: number,
  h: number,
  centerX: number,
  centerY: number,
  spiralStep: number,
  angleStep: number,
  canvasW: number,
  canvasH: number
): { x: number; y: number } | null {
  let angle = Math.random() * Math.PI * 2;
  let radius = 0;
  const maxAttempts = 800;

  for (let i = 0; i < maxAttempts; i++) {
    const x = centerX + radius * Math.cos(angle) - w / 2;
    const y = centerY + radius * Math.sin(angle) - h / 2;

    const clampedX = Math.max(2, Math.min(x, canvasW - w - 2));
    const clampedY = Math.max(2, Math.min(y, canvasH - h - 2));

    if (!hasOverlap(clampedX, clampedY, w, h)) {
      return { x: clampedX, y: clampedY };
    }

    angle += angleStep;
    radius += spiralStep;
  }

  return null;
}

function hasOverlap(x: number, y: number, w: number, h: number): boolean {
  for (const pw of placedWords) {
    if (
      x < pw.x + pw.w &&
      x + w > pw.x &&
      y < pw.y + pw.h &&
      y + h > pw.y
    ) {
      return true;
    }
  }
  return false;
}

function drawWord(
  ctx: CanvasRenderingContext2D,
  text: string,
  x: number,
  y: number,
  fontSize: number,
  color: string,
  pad: number
) {
  ctx.save();

  ctx.shadowColor = 'rgba(0, 0, 0, 0.08)';
  ctx.shadowBlur = 3;
  ctx.shadowOffsetX = 1;
  ctx.shadowOffsetY = 1;

  ctx.fillStyle = color;
  ctx.font = `bold ${fontSize}px ${FONT_FAMILY}`;
  ctx.textBaseline = 'top';
  ctx.fillText(text, x + pad, y + pad);

  ctx.shadowColor = 'transparent';
  ctx.restore();
}

function drawEmptyText(ctx: CanvasRenderingContext2D, w: number, h: number) {
  ctx.fillStyle = '#94a3b8';
  ctx.font = `14px ${FONT_FAMILY}`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('暂无词云数据', w / 2, h / 2);
  ctx.textAlign = 'start';
  ctx.textBaseline = 'alphabetic';
}

function handleCanvasClick(event: MouseEvent) {
  const canvas = canvasRef.value;
  if (!canvas) return;

  const rect = canvas.getBoundingClientRect();
  const scaleX = canvas.width / (dpr * rect.width);
  const scaleY = canvas.height / (dpr * rect.height);

  const mx = (event.clientX - rect.left) * scaleX;
  const my = (event.clientY - rect.top) * scaleY;

  for (const pw of placedWords) {
    if (mx >= pw.x && mx <= pw.x + pw.w && my >= pw.y && my <= pw.y + pw.h) {
      emit('word-click', { name: pw.text, value: pw.value });
      return;
    }
  }
}
</script>

<style scoped>
.word-cloud-container {
  position: relative;
  width: 100%;
  min-height: 280px;
  overflow: hidden;
}

.word-cloud-canvas {
  display: block;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.wc-empty,
.wc-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
  pointer-events: none;
}
</style>
