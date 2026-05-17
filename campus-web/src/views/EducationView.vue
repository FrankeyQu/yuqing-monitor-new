<template>
  <section class="business-page">
    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="教育专题" name="topics">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-select v-model="topicQuery.topicType" placeholder="专题类型">
                <el-option label="重点新闻" value="education_news" />
                <el-option label="重点政策" value="policy" />
                <el-option label="招生政策" value="admission" />
              </el-select>
              <el-date-picker v-model="topicDateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
              <el-button type="primary" @click="loadTopics">查询</el-button>
            </div>
          </div>
          <el-table :data="topics" v-loading="topicLoading" size="small" height="560">
            <el-table-column prop="title" label="标题" min-width="280" show-overflow-tooltip />
            <el-table-column prop="sourcePlatform" label="平台" width="120" show-overflow-tooltip />
            <el-table-column prop="keywords" label="关键词" width="160" show-overflow-tooltip />
            <el-table-column prop="sentiment" label="情感" width="90" />
            <el-table-column prop="riskLevel" label="风险" width="90" />
            <el-table-column prop="publishTime" label="发布时间" width="168" show-overflow-tooltip />
            <el-table-column prop="originalUrl" label="原文链接" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="学校排名" name="ranking">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="rankingQuery.keyword" clearable placeholder="关键词" @keyup.enter="loadRanking" />
              <el-date-picker v-model="rankingDateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
              <el-button type="primary" @click="loadRanking">查询</el-button>
            </div>
          </div>
          <el-table :data="ranking" v-loading="rankingLoading" size="small" height="560">
            <el-table-column type="index" label="#" width="56" />
            <el-table-column prop="schoolName" label="学校" min-width="180" show-overflow-tooltip />
            <el-table-column prop="region" label="地区" width="120" show-overflow-tooltip />
            <el-table-column prop="educationStage" label="学段" width="100" />
            <el-table-column prop="totalCount" label="总声量" width="90" />
            <el-table-column prop="negativeCount" label="负面" width="90" />
            <el-table-column prop="positiveCount" label="正面" width="90" />
            <el-table-column prop="neutralCount" label="中性" width="90" />
            <el-table-column prop="highRiskCount" label="高风险" width="90" />
            <el-table-column label="负面占比" width="110">
              <template #default="{ row }">{{ percent(row.negativeRatio) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="学校主体" name="schools">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="schoolQuery.keyword" clearable placeholder="学校/别名" @keyup.enter="loadSchools" />
              <el-input v-model.trim="schoolQuery.region" clearable placeholder="地区" @keyup.enter="loadSchools" />
              <el-input v-model.trim="schoolQuery.educationStage" clearable placeholder="学段" @keyup.enter="loadSchools" />
              <el-button @click="loadSchools">查询</el-button>
            </div>
            <div class="toolbar-actions">
              <el-button @click="downloadTemplate">下载模板</el-button>
              <el-button :disabled="!canEducationOperate" :loading="schoolImporting" @click="triggerSchoolImport">导入学校</el-button>
              <el-button type="primary" :disabled="!canEducationOperate" @click="openSchoolCreate">新增学校</el-button>
              <input ref="schoolImportInputRef" class="hidden-file-input" type="file" accept=".csv,text/csv" @change="handleSchoolImportChange" />
            </div>
          </div>
          <el-table :data="schools" v-loading="schoolLoading" size="small" height="520">
            <el-table-column prop="schoolName" label="学校名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="schoolAliases" label="别名" min-width="180" show-overflow-tooltip />
            <el-table-column prop="region" label="地区" width="120" />
            <el-table-column prop="educationStage" label="学段" width="110" />
            <el-table-column prop="schoolType" label="类型" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="plain">{{ row.status === 0 ? '停用' : '启用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="!canEducationOperate" @click="openSchoolEdit(row)">编辑</el-button>
                <el-button link type="danger" :disabled="!canEducationOperate" @click="submitDeleteSchool(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-row">
            <el-pagination
              v-model:current-page="schoolQuery.pageNum"
              v-model:page-size="schoolQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="schoolTotal"
              @size-change="loadSchools"
              @current-change="loadSchools"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="百度任务" name="baidu">
          <div class="baidu-form">
            <el-form label-position="top">
              <div class="form-grid">
                <el-form-item label="接入来源" required>
                  <el-select v-model="baiduForm.sourceId" filterable placeholder="请选择百度接入来源" style="width: 100%">
                    <el-option
                      v-for="source in sources"
                      :key="source.sourceId"
                      :label="sourceOptionLabel(source)"
                      :value="source.sourceId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="专题类型">
                  <el-select v-model="baiduForm.topicType">
                    <el-option label="重点新闻" value="education_news" />
                    <el-option label="重点政策" value="policy" />
                    <el-option label="招生政策" value="admission" />
                  </el-select>
                </el-form-item>
                <el-form-item label="TopK">
                  <el-input-number v-model="baiduForm.topK" :min="1" :max="50" controls-position="right" />
                </el-form-item>
              </div>
              <div class="form-grid">
                <el-form-item label="地区">
                  <el-input v-model.trim="baiduForm.region" placeholder="新疆" />
                </el-form-item>
                <el-form-item label="学校">
                  <el-input v-model.trim="baiduForm.schoolName" placeholder="可选" />
                </el-form-item>
                <el-form-item label="补充关键词">
                  <el-input v-model.trim="baiduForm.keyword" />
                </el-form-item>
              </div>
              <el-form-item label="授权范围">
                <el-input v-model.trim="baiduForm.authorizationScope" type="textarea" :rows="2" />
              </el-form-item>
              <div class="baidu-actions">
                <el-checkbox v-model="baiduForm.runNow">创建后立即运行一次</el-checkbox>
                <el-button type="primary" :loading="baiduLoading" :disabled="!canEducationOperate" @click="submitBaiduTask">
                  {{ baiduForm.runNow ? '创建并运行百度任务' : '创建百度接入任务' }}
                </el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="schoolDialogVisible" :title="schoolForm.schoolId ? '编辑学校主体' : '新增学校主体'" width="680px">
      <el-form label-position="top">
        <el-form-item label="学校名称" required>
          <el-input v-model.trim="schoolForm.schoolName" />
        </el-form-item>
        <el-form-item label="别名">
          <el-input v-model.trim="schoolForm.schoolAliases" placeholder="多个别名用逗号分隔" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="地区">
            <el-input v-model.trim="schoolForm.region" />
          </el-form-item>
          <el-form-item label="学段">
            <el-input v-model.trim="schoolForm.educationStage" />
          </el-form-item>
          <el-form-item label="学校类型">
            <el-input v-model.trim="schoolForm.schoolType" />
          </el-form-item>
        </div>
        <el-form-item label="状态">
          <el-select v-model="schoolForm.status">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="schoolForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="schoolDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="schoolSaving" :disabled="!canEducationOperate" @click="submitSchool">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createAndRunEducationBaiduTask,
  createEducationBaiduTask,
  deleteSchool,
  downloadSchoolTemplate,
  fetchSchoolSentimentRanking,
  importSchools,
  listEducationTopics,
  listSchools,
  saveSchool
} from '../services/education';
import { listIngestSources } from '../services/detectionIngest';
import { getCurrentCampusUser } from '../services/permission';
import type {
  CampusEducationTopicItem,
  CampusIngestSource,
  CampusSchoolSentimentRank,
  CampusSchoolSubject
} from '../types/api';

const activeTab = ref('topics');
const topicLoading = ref(false);
const rankingLoading = ref(false);
const schoolLoading = ref(false);
const schoolSaving = ref(false);
const schoolImporting = ref(false);
const baiduLoading = ref(false);
const schoolDialogVisible = ref(false);
const schoolImportInputRef = ref<HTMLInputElement>();
const permissionReady = ref(false);
const permissionCodes = ref<Set<string>>(new Set());

const topics = ref<CampusEducationTopicItem[]>([]);
const ranking = ref<CampusSchoolSentimentRank[]>([]);
const schools = ref<CampusSchoolSubject[]>([]);
const sources = ref<CampusIngestSource[]>([]);
const schoolTotal = ref(0);
const topicDateRange = ref<[string, string] | null>(null);
const rankingDateRange = ref<[string, string] | null>(null);

const topicQuery = reactive({ topicType: 'education_news', limit: 50 });
const rankingQuery = reactive({ keyword: '', limit: 50 });
const schoolQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  region: '',
  educationStage: '',
  status: '' as number | ''
});
const schoolForm = reactive<CampusSchoolSubject>({
  schoolName: '',
  schoolAliases: '',
  region: '新疆',
  educationStage: '',
  schoolType: '',
  status: 1,
  remark: ''
});
const baiduForm = reactive({
  sourceId: undefined as number | undefined,
  topicType: 'education_news',
  region: '新疆',
  schoolName: '',
  keyword: '',
  topK: 20,
  credentialRef: 'BAIDU_API_KEY',
  authorizationScope: '教育专题公开搜索',
  runNow: true
});

const canEducationOperate = computed(() => hasPermission([
  'role:campus_admin',
  'campus:api:all',
  'campus:business:operate',
  'campus:education:operate'
]));

onMounted(() => {
  loadCurrentPermissions();
  loadTopics();
  loadSources();
});

watch(activeTab, (tab) => {
  if (tab === 'ranking' && ranking.value.length === 0) loadRanking();
  if (tab === 'schools' && schools.value.length === 0) loadSchools();
  if (tab === 'baidu' && sources.value.length === 0) loadSources();
});

function hasPermission(codes: string[]) {
  if (!permissionReady.value) {
    return true;
  }
  return codes.some((code) => permissionCodes.value.has(code));
}

async function loadCurrentPermissions() {
  try {
    const current = await getCurrentCampusUser();
    permissionCodes.value = new Set(current.permissions || []);
  } catch {
    permissionCodes.value = new Set();
  } finally {
    permissionReady.value = true;
  }
}

async function loadTopics() {
  topicLoading.value = true;
  try {
    topics.value = await listEducationTopics({
      topicType: topicQuery.topicType,
      startTime: topicDateRange.value?.[0],
      endTime: topicDateRange.value?.[1],
      limit: topicQuery.limit
    });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '教育专题加载失败');
  } finally {
    topicLoading.value = false;
  }
}

async function loadRanking() {
  rankingLoading.value = true;
  try {
    ranking.value = await fetchSchoolSentimentRanking({
      keyword: rankingQuery.keyword,
      startTime: rankingDateRange.value?.[0],
      endTime: rankingDateRange.value?.[1],
      limit: rankingQuery.limit
    });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '学校排名加载失败');
  } finally {
    rankingLoading.value = false;
  }
}

async function loadSchools() {
  schoolLoading.value = true;
  try {
    const page = await listSchools(schoolQuery);
    schools.value = page.list || [];
    schoolTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '学校主体加载失败');
  } finally {
    schoolLoading.value = false;
  }
}

async function loadSources() {
  try {
    const page = await listIngestSources({
      pageNum: 1,
      pageSize: 100,
      platform: '百度',
      enabled: 1
    });
    sources.value = (page.list || []).filter((source) => {
      if (!source.sourceId) {
        return false;
      }
      const platform = source.platform || '';
      const type = source.sourceType || '';
      const name = source.sourceName || '';
      return [platform, type, name].some((value) => value.includes('百度') || value.toLowerCase().includes('baidu'));
    });
    if (!baiduForm.sourceId && sources.value.length > 0) {
      baiduForm.sourceId = sources.value[0].sourceId;
    }
  } catch {
    sources.value = [];
  }
}

function openSchoolCreate() {
  Object.assign(schoolForm, {
    schoolId: undefined,
    schoolName: '',
    schoolAliases: '',
    region: '新疆',
    educationStage: '',
    schoolType: '',
    status: 1,
    remark: ''
  });
  schoolDialogVisible.value = true;
}

function openSchoolEdit(row: CampusSchoolSubject) {
  Object.assign(schoolForm, row);
  schoolDialogVisible.value = true;
}

async function submitSchool() {
  if (!schoolForm.schoolName) {
    ElMessage.warning('学校名称不能为空');
    return;
  }
  schoolSaving.value = true;
  try {
    await saveSchool({ ...schoolForm });
    ElMessage.success('学校主体已保存');
    schoolDialogVisible.value = false;
    await loadSchools();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    schoolSaving.value = false;
  }
}

async function submitDeleteSchool(row: CampusSchoolSubject) {
  if (!row.schoolId) return;
  try {
    await ElMessageBox.confirm(`确认删除学校「${row.schoolName}」吗？`, '删除确认', { type: 'warning' });
    await deleteSchool(row.schoolId);
    ElMessage.success('学校主体已删除');
    await loadSchools();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function triggerSchoolImport() {
  schoolImportInputRef.value?.click();
}

async function handleSchoolImportChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file) {
    return;
  }
  schoolImporting.value = true;
  try {
    const result = await importSchools(file);
    ElMessage.success(`导入完成：新增 ${result.inserted || 0}，更新 ${result.updated || 0}，跳过 ${result.skipped || 0}，失败 ${result.failed || 0}`);
    await loadSchools();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '导入失败');
  } finally {
    schoolImporting.value = false;
  }
}

async function downloadTemplate() {
  try {
    const blob = await downloadSchoolTemplate();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'campus-school-template.csv';
    a.click();
    URL.revokeObjectURL(url);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '模板下载失败');
  }
}

async function submitBaiduTask() {
  if (!baiduForm.sourceId) {
    ElMessage.warning('请先选择百度接入来源');
    return;
  }
  baiduLoading.value = true;
  try {
    const { runNow, ...payload } = baiduForm;
    if (runNow) {
      const result = await createAndRunEducationBaiduTask(payload);
      const fetchedCount = result.runLog?.fetchedCount ?? 0;
      const successCount = result.runLog?.successCount ?? 0;
      ElMessage.success(`已运行百度任务：抓取 ${fetchedCount} 条，入库 ${successCount} 条`);
      await loadTopics();
      if (activeTab.value === 'ranking') {
        await loadRanking();
      }
    } else {
      const saved = await createEducationBaiduTask(payload);
      ElMessage.success(`已创建百度接入任务：${saved.taskName}`);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '创建失败');
  } finally {
    baiduLoading.value = false;
  }
}

function sourceOptionLabel(source: CampusIngestSource) {
  const name = source.sourceName || '未命名来源';
  const platform = source.platform || source.sourceType || '未知平台';
  return `${name}（${platform} / ${source.sourceId}）`;
}

function percent(value?: number) {
  if (value === undefined || value === null) return '-';
  return `${(value * 100).toFixed(1)}%`;
}
</script>

<style scoped>
.baidu-form {
  max-width: 920px;
}

.toolbar-actions,
.baidu-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hidden-file-input {
  display: none;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
