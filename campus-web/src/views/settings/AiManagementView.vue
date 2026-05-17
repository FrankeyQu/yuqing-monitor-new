<template>
  <section class="ai-page">
    <section class="ai-header">
      <div>
        <span>AI 管控台</span>
        <h2>AI 能力管理</h2>
        <p>统一维护模型、接入点、功能绑定、提示词和调用日志。</p>
      </div>
      <el-button @click="refreshCurrent">
        <RefreshCw :size="16" />
        刷新
      </el-button>
    </section>

    <el-alert
      v-if="pageErrorMessages.length"
      type="warning"
      show-icon
      :closable="true"
      title="AI 能力数据未完整加载"
      :description="pageErrorMessages.join('；')"
      @close="pageErrorMessages = []"
    />

    <section class="metric-grid">
      <article v-for="item in overviewCards" :key="item.label" class="metric">
        <span :class="['metric-icon', item.tone]">
          <component :is="item.icon" :size="20" />
        </span>
        <div>
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="功能绑定" name="features">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="featureQuery.keyword" clearable placeholder="功能编码/名称" @keyup.enter="loadFeatures">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="featureQuery.featureType" clearable placeholder="功能类型">
                <el-option label="大模型" value="llm" />
                <el-option label="接入" value="ingest" />
                <el-option label="正文提取" value="extract" />
                <el-option label="本地规则" value="rule" />
                <el-option label="历史能力" value="legacy" />
              </el-select>
              <el-select v-model="featureQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadFeatures">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openFeatureCreate">
              <Plus :size="16" />
              新增绑定
            </el-button>
          </div>

          <el-table :data="features" v-loading="featureLoading" size="small" height="560">
            <el-table-column prop="featureName" label="功能" min-width="160" show-overflow-tooltip />
            <el-table-column prop="featureCode" label="编码" min-width="180" show-overflow-tooltip />
            <el-table-column prop="featureType" label="类型" width="92">
              <template #default="{ row }">{{ featureTypeLabel(row.featureType) }}</template>
            </el-table-column>
            <el-table-column prop="providerCode" label="供应商" width="130" show-overflow-tooltip />
            <el-table-column prop="modelCode" label="模型" min-width="150" show-overflow-tooltip />
            <el-table-column prop="failureStrategy" label="失败策略" width="140" show-overflow-tooltip />
            <el-table-column prop="timeoutMs" label="超时(ms)" width="105" />
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="说明" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openFeatureEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <PaginationBar :total="featureTotal" v-model:page="featureQuery.pageNum" v-model:size="featureQuery.pageSize" @change="loadFeatures" />
        </el-tab-pane>

        <el-tab-pane label="供应商" name="providers">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="providerQuery.keyword" clearable placeholder="供应商编码/名称" @keyup.enter="loadProviders">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="providerQuery.providerType" clearable placeholder="供应商类型">
                <el-option label="大模型" value="llm" />
                <el-option label="搜索" value="web_search" />
                <el-option label="社媒接入" value="social_ingest" />
                <el-option label="正文提取" value="content_extract" />
                <el-option label="历史写作" value="legacy_writer" />
                <el-option label="历史 NLP" value="legacy_nlp" />
              </el-select>
              <el-select v-model="providerQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadProviders">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openProviderCreate">
              <Plus :size="16" />
              新增供应商
            </el-button>
          </div>

          <el-table :data="providers" v-loading="providerLoading" size="small" height="560">
            <el-table-column prop="providerName" label="供应商" min-width="150" show-overflow-tooltip />
            <el-table-column prop="providerCode" label="编码" width="150" show-overflow-tooltip />
            <el-table-column prop="providerType" label="类型" width="120">
              <template #default="{ row }">{{ providerTypeLabel(row.providerType) }}</template>
            </el-table-column>
            <el-table-column prop="baseUrl" label="接入点" min-width="260" show-overflow-tooltip />
            <el-table-column prop="credentialRef" label="密钥引用" width="170" show-overflow-tooltip />
            <el-table-column prop="timeoutMs" label="超时(ms)" width="105" />
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openProviderEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="success" @click="submitProviderTest(row)">
                  <FlaskConical :size="15" />
                  测试
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <PaginationBar :total="providerTotal" v-model:page="providerQuery.pageNum" v-model:size="providerQuery.pageSize" @change="loadProviders" />
        </el-tab-pane>

        <el-tab-pane label="模型" name="models">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-select v-model="modelQuery.providerCode" clearable placeholder="供应商">
                <el-option v-for="item in providers" :key="item.providerCode" :label="item.providerName" :value="item.providerCode" />
              </el-select>
              <el-input v-model.trim="modelQuery.keyword" clearable placeholder="模型编码/名称" @keyup.enter="loadModels">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="modelQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadModels">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openModelCreate">
              <Plus :size="16" />
              新增模型
            </el-button>
          </div>

          <el-table :data="models" v-loading="modelLoading" size="small" height="560">
            <el-table-column prop="modelName" label="模型名称" min-width="170" show-overflow-tooltip />
            <el-table-column prop="providerCode" label="供应商" width="140" show-overflow-tooltip />
            <el-table-column prop="modelCode" label="模型编码" min-width="170" show-overflow-tooltip />
            <el-table-column prop="contextLength" label="上下文" width="96" />
            <el-table-column prop="defaultTemperature" label="温度" width="82" />
            <el-table-column prop="defaultMaxTokens" label="Max Tokens" width="112" />
            <el-table-column prop="supportStream" label="流式" width="72">
              <template #default="{ row }">{{ row.supportStream === 0 ? '否' : '是' }}</template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openModelEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <PaginationBar :total="modelTotal" v-model:page="modelQuery.pageNum" v-model:size="modelQuery.pageSize" @change="loadModels" />
        </el-tab-pane>

        <el-tab-pane label="提示词" name="prompts">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="promptQuery.featureCode" clearable placeholder="功能编码" @keyup.enter="loadPrompts" />
              <el-input v-model.trim="promptQuery.keyword" clearable placeholder="模板名称/版本" @keyup.enter="loadPrompts">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="promptQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadPrompts">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openPromptCreate">
              <Plus :size="16" />
              新增提示词
            </el-button>
          </div>

          <el-table :data="prompts" v-loading="promptLoading" size="small" height="560">
            <el-table-column prop="templateName" label="模板" min-width="180" show-overflow-tooltip />
            <el-table-column prop="featureCode" label="功能编码" width="160" show-overflow-tooltip />
            <el-table-column prop="templateVersion" label="版本" width="90" />
            <el-table-column prop="outputFormat" label="输出格式" min-width="220" show-overflow-tooltip />
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPromptEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="danger" @click="submitPromptDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <PaginationBar :total="promptTotal" v-model:page="promptQuery.pageNum" v-model:size="promptQuery.pageSize" @change="loadPrompts" />
        </el-tab-pane>

        <el-tab-pane label="调用日志" name="logs">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="logQuery.featureCode" clearable placeholder="功能编码" @keyup.enter="loadLogs" />
              <el-input v-model.trim="logQuery.providerCode" clearable placeholder="供应商" @keyup.enter="loadLogs" />
              <el-select v-model="logQuery.callStatus" clearable placeholder="状态">
                <el-option label="成功" value="success" />
                <el-option label="失败" value="failed" />
              </el-select>
              <el-button @click="loadLogs">
                <Search :size="16" />
                查询
              </el-button>
            </div>
          </div>

          <el-table :data="logs" v-loading="logLoading" size="small" height="560">
            <el-table-column prop="requestTime" label="时间" width="168" show-overflow-tooltip />
            <el-table-column prop="featureCode" label="功能" width="160" show-overflow-tooltip />
            <el-table-column prop="providerCode" label="供应商" width="130" show-overflow-tooltip />
            <el-table-column prop="modelCode" label="模型" min-width="150" show-overflow-tooltip />
            <el-table-column prop="callStatus" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.callStatus === 'success' ? 'success' : 'danger'" effect="plain">
                  {{ row.callStatus === 'success' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="httpStatus" label="HTTP" width="80" />
            <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
            <el-table-column prop="totalTokens" label="Tokens" width="90" />
            <el-table-column prop="errorMessage" label="错误" min-width="240" show-overflow-tooltip />
          </el-table>
          <PaginationBar :total="logTotal" v-model:page="logQuery.pageNum" v-model:size="logQuery.pageSize" @change="loadLogs" />
        </el-tab-pane>

        <el-tab-pane label="历史能力" name="legacy">
          <div class="legacy-list">
            <div v-for="item in legacyFeatures" :key="item.featureCode" class="legacy-row">
              <div>
                <strong>{{ item.featureName }}</strong>
                <span>{{ item.featureCode }} · {{ providerTypeLabel(item.providerCode) || item.providerCode || '未绑定' }}</span>
              </div>
              <el-tag :type="item.enabled === 1 ? 'warning' : 'info'" effect="plain">
                {{ item.enabled === 1 ? '已启用' : '默认停用' }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="providerDialogVisible" :title="providerForm.providerId ? '编辑供应商' : '新增供应商'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="供应商编码" required>
            <el-input v-model.trim="providerForm.providerCode" :disabled="Boolean(providerForm.providerId)" />
          </el-form-item>
          <el-form-item label="供应商名称" required>
            <el-input v-model.trim="providerForm.providerName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="类型" required>
            <el-select v-model="providerForm.providerType">
              <el-option label="大模型" value="llm" />
              <el-option label="搜索" value="web_search" />
              <el-option label="正文提取" value="content_extract" />
              <el-option label="社媒接入" value="social_ingest" />
              <el-option label="历史写作" value="legacy_writer" />
              <el-option label="历史 NLP" value="legacy_nlp" />
            </el-select>
          </el-form-item>
          <el-form-item label="鉴权方式">
            <el-select v-model="providerForm.authType">
              <el-option label="Bearer" value="bearer" />
              <el-option label="Header" value="header" />
              <el-option label="Custom" value="custom" />
              <el-option label="无" value="none" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="接入点">
          <el-input v-model.trim="providerForm.baseUrl" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="密钥环境变量">
            <el-input v-model.trim="providerForm.credentialRef" />
          </el-form-item>
          <el-form-item label="超时(ms)">
            <el-input-number v-model="providerForm.timeoutMs" :min="1000" :max="180000" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="状态">
            <el-switch v-model="providerForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model.trim="providerForm.remark" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="providerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitProvider">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="modelDialogVisible" :title="modelForm.modelId ? '编辑模型' : '新增模型'" width="720px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="供应商" required>
            <el-select v-model="modelForm.providerCode" :disabled="Boolean(modelForm.modelId)">
              <el-option v-for="item in providers" :key="item.providerCode" :label="item.providerName" :value="item.providerCode" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型编码" required>
            <el-input v-model.trim="modelForm.modelCode" :disabled="Boolean(modelForm.modelId)" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="模型名称" required>
            <el-input v-model.trim="modelForm.modelName" />
          </el-form-item>
          <el-form-item label="上下文长度">
            <el-input-number v-model="modelForm.contextLength" :min="0" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="默认温度">
            <el-input-number v-model="modelForm.defaultTemperature" :min="0" :max="2" :step="0.05" controls-position="right" />
          </el-form-item>
          <el-form-item label="默认 Max Tokens">
            <el-input-number v-model="modelForm.defaultMaxTokens" :min="1" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="支持流式">
            <el-switch v-model="modelForm.supportStream" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="modelForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model.trim="modelForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitModel">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="featureDialogVisible" :title="featureForm.bindingId ? '编辑功能绑定' : '新增功能绑定'" width="780px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="功能编码" required>
            <el-input v-model.trim="featureForm.featureCode" :disabled="Boolean(featureForm.bindingId)" />
          </el-form-item>
          <el-form-item label="功能名称" required>
            <el-input v-model.trim="featureForm.featureName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="功能类型">
            <el-select v-model="featureForm.featureType">
              <el-option label="大模型" value="llm" />
              <el-option label="接入" value="ingest" />
              <el-option label="正文提取" value="extract" />
              <el-option label="本地规则" value="rule" />
              <el-option label="历史能力" value="legacy" />
            </el-select>
          </el-form-item>
          <el-form-item label="失败策略">
            <el-select v-model="featureForm.failureStrategy">
              <el-option label="失败" value="fail" />
              <el-option label="跳过" value="skip" />
              <el-option label="回退规则" value="fallback_rule" />
              <el-option label="回退关键词" value="fallback_keywords" />
              <el-option label="保留摘要" value="preserve_summary" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="供应商">
            <el-select v-model="featureForm.providerCode" clearable>
              <el-option v-for="item in providers" :key="item.providerCode" :label="item.providerName" :value="item.providerCode" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型">
            <el-select v-model="featureForm.modelCode" clearable>
              <el-option v-for="item in modelOptionsForFeature" :key="item.modelCode" :label="item.modelName" :value="item.modelCode" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="超时(ms)">
            <el-input-number v-model="featureForm.timeoutMs" :min="1000" :max="180000" controls-position="right" />
          </el-form-item>
          <el-form-item label="记录提示词">
            <el-switch v-model="featureForm.logPrompt" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="状态">
            <el-switch v-model="featureForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model.trim="featureForm.remark" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="featureDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitFeature">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="promptDialogVisible" :title="promptForm.templateId ? '编辑提示词' : '新增提示词'" width="820px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="功能编码" required>
            <el-input v-model.trim="promptForm.featureCode" />
          </el-form-item>
          <el-form-item label="模板名称" required>
            <el-input v-model.trim="promptForm.templateName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="版本">
            <el-input v-model.trim="promptForm.templateVersion" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="promptForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item label="System Prompt">
          <el-input v-model="promptForm.systemPrompt" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="User Prompt">
          <el-input v-model="promptForm.userPrompt" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="输出格式">
          <el-input v-model="promptForm.outputFormat" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="promptForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promptDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPrompt">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, reactive, ref, resolveComponent, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { BrainCircuit, FlaskConical, History, Pencil, Plus, RefreshCw, Search, ServerCog, Trash2, Zap } from 'lucide-vue-next';
import {
  deleteAiModel,
  deleteAiPrompt,
  getAiOverview,
  listAiCallLogs,
  listAiFeatures,
  listAiModels,
  listAiPrompts,
  listAiProviders,
  saveAiFeature,
  saveAiModel,
  saveAiPrompt,
  saveAiProvider,
  testAiProvider
} from '../../services/ai';
import type {
  CampusAiCallLog,
  CampusAiFeatureBinding,
  CampusAiModel,
  CampusAiOverview,
  CampusAiPromptTemplate,
  CampusAiProvider
} from '../../types/api';

const PaginationBar = defineComponent({
  props: {
    total: { type: Number, required: true },
    page: { type: Number, required: true },
    size: { type: Number, required: true }
  },
  emits: ['update:page', 'update:size', 'change'],
  setup(props, { emit }) {
    const ElPagination = resolveComponent('el-pagination');
    return () => h('div', { class: 'pagination-row' }, [
      h(ElPagination, {
        currentPage: props.page,
        pageSize: props.size,
        layout: 'total, sizes, prev, pager, next',
        pageSizes: [10, 20, 50],
        total: props.total,
        'onUpdate:currentPage': (value: number) => emit('update:page', value),
        'onUpdate:pageSize': (value: number) => emit('update:size', value),
        onSizeChange: () => emit('change'),
        onCurrentChange: () => emit('change')
      })
    ]);
  }
});

const activeTab = ref('features');
const saving = ref(false);
const providerLoading = ref(false);
const modelLoading = ref(false);
const featureLoading = ref(false);
const promptLoading = ref(false);
const logLoading = ref(false);
const providerDialogVisible = ref(false);
const modelDialogVisible = ref(false);
const featureDialogVisible = ref(false);
const promptDialogVisible = ref(false);
const overview = ref<CampusAiOverview>({});
const pageErrorMessages = ref<string[]>([]);
const providers = ref<CampusAiProvider[]>([]);
const models = ref<CampusAiModel[]>([]);
const features = ref<CampusAiFeatureBinding[]>([]);
const prompts = ref<CampusAiPromptTemplate[]>([]);
const logs = ref<CampusAiCallLog[]>([]);
const providerTotal = ref(0);
const modelTotal = ref(0);
const featureTotal = ref(0);
const promptTotal = ref(0);
const logTotal = ref(0);

const providerQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', providerType: '', enabled: undefined as number | undefined });
const modelQuery = reactive({ pageNum: 1, pageSize: 10, providerCode: '', keyword: '', enabled: undefined as number | undefined });
const featureQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', featureType: '', enabled: undefined as number | undefined });
const promptQuery = reactive({ pageNum: 1, pageSize: 10, featureCode: '', keyword: '', enabled: undefined as number | undefined });
const logQuery = reactive({ pageNum: 1, pageSize: 10, featureCode: '', providerCode: '', callStatus: '' });

const providerForm = reactive<CampusAiProvider>(defaultProvider());
const modelForm = reactive<CampusAiModel>(defaultModel());
const featureForm = reactive<CampusAiFeatureBinding>(defaultFeature());
const promptForm = reactive<CampusAiPromptTemplate>(defaultPrompt());

const overviewCards = computed(() => [
  { label: '启用供应商', value: overview.value.activeProviderCount || 0, icon: ServerCog, tone: 'blue' },
  { label: '启用功能', value: overview.value.enabledFeatureCount || 0, icon: Zap, tone: 'green' },
  { label: '历史能力', value: overview.value.legacyFeatureCount || 0, icon: History, tone: 'gray' },
  { label: '24小时失败', value: overview.value.failedCallCount24h || 0, icon: BrainCircuit, tone: 'red' }
]);
const legacyFeatures = computed(() => features.value.filter((item) => item.featureType === 'legacy'));
const modelOptionsForFeature = computed(() => models.value.filter((item) => !featureForm.providerCode || item.providerCode === featureForm.providerCode));

onMounted(async () => {
  await Promise.all([loadOverview(), loadProviders(), loadModels(), loadFeatures()]);
});

watch(activeTab, async (tab) => {
  if (tab === 'providers') await loadProviders();
  if (tab === 'models') await Promise.all([loadProviders(), loadModels()]);
  if (tab === 'features' || tab === 'legacy') await loadFeatures();
  if (tab === 'prompts') await loadPrompts();
  if (tab === 'logs') await loadLogs();
});

async function refreshCurrent() {
  pageErrorMessages.value = [];
  await loadOverview();
  if (activeTab.value === 'providers') await loadProviders();
  if (activeTab.value === 'models') await loadModels();
  if (activeTab.value === 'features' || activeTab.value === 'legacy') await loadFeatures();
  if (activeTab.value === 'prompts') await loadPrompts();
  if (activeTab.value === 'logs') await loadLogs();
}

async function loadOverview() {
  try {
    overview.value = await getAiOverview();
  } catch (error) {
    recordPageError('AI总览加载失败', error);
    ElMessage.error(error instanceof Error ? error.message : 'AI总览加载失败');
  }
}

async function loadProviders() {
  providerLoading.value = true;
  try {
    const page = await listAiProviders(providerQuery);
    providers.value = page.list || [];
    providerTotal.value = page.total || 0;
  } catch (error) {
    recordPageError('供应商加载失败', error);
    ElMessage.error(error instanceof Error ? error.message : '供应商加载失败');
  } finally {
    providerLoading.value = false;
  }
}

async function loadModels() {
  modelLoading.value = true;
  try {
    const page = await listAiModels(modelQuery);
    models.value = page.list || [];
    modelTotal.value = page.total || 0;
  } catch (error) {
    recordPageError('模型加载失败', error);
    ElMessage.error(error instanceof Error ? error.message : '模型加载失败');
  } finally {
    modelLoading.value = false;
  }
}

async function loadFeatures() {
  featureLoading.value = true;
  try {
    const page = await listAiFeatures(featureQuery);
    features.value = page.list || [];
    featureTotal.value = page.total || 0;
  } catch (error) {
    recordPageError('功能绑定加载失败', error);
    ElMessage.error(error instanceof Error ? error.message : '功能绑定加载失败');
  } finally {
    featureLoading.value = false;
  }
}

async function loadPrompts() {
  promptLoading.value = true;
  try {
    const page = await listAiPrompts(promptQuery);
    prompts.value = page.list || [];
    promptTotal.value = page.total || 0;
  } catch (error) {
    recordPageError('提示词加载失败', error);
    ElMessage.error(error instanceof Error ? error.message : '提示词加载失败');
  } finally {
    promptLoading.value = false;
  }
}

async function loadLogs() {
  logLoading.value = true;
  try {
    const page = await listAiCallLogs(logQuery);
    logs.value = page.list || [];
    logTotal.value = page.total || 0;
  } catch (error) {
    recordPageError('调用日志加载失败', error);
    ElMessage.error(error instanceof Error ? error.message : '调用日志加载失败');
  } finally {
    logLoading.value = false;
  }
}

function recordPageError(label: string, error: unknown) {
  const detail = error instanceof Error ? error.message : label;
  const message = detail && detail !== label ? `${label}: ${detail}` : label;
  if (!pageErrorMessages.value.includes(message)) {
    pageErrorMessages.value = [...pageErrorMessages.value, message];
  }
}

function openProviderCreate() {
  Object.assign(providerForm, defaultProvider());
  providerDialogVisible.value = true;
}

function openProviderEdit(row: CampusAiProvider) {
  Object.assign(providerForm, defaultProvider(), row);
  providerDialogVisible.value = true;
}

async function submitProvider() {
  if (!providerForm.providerCode || !providerForm.providerName || !providerForm.providerType) {
    ElMessage.warning('供应商编码、名称和类型不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveAiProvider({ ...providerForm });
    ElMessage.success('供应商已保存');
    providerDialogVisible.value = false;
    await Promise.all([loadProviders(), loadOverview()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitProviderTest(row: CampusAiProvider) {
  try {
    const result = await testAiProvider(row.providerCode);
    const type = result.ready ? 'success' : 'warning';
    ElMessage({ type, message: result.message || '测试完成' });
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '测试失败');
  }
}

function openModelCreate() {
  Object.assign(modelForm, defaultModel());
  if (providers.value.length > 0) {
    modelForm.providerCode = providers.value[0].providerCode;
  }
  modelDialogVisible.value = true;
}

function openModelEdit(row: CampusAiModel) {
  Object.assign(modelForm, defaultModel(), row);
  modelDialogVisible.value = true;
}

async function submitModel() {
  if (!modelForm.providerCode || !modelForm.modelCode || !modelForm.modelName) {
    ElMessage.warning('供应商、模型编码和模型名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveAiModel({ ...modelForm });
    ElMessage.success('模型已保存');
    modelDialogVisible.value = false;
    await loadModels();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function openFeatureCreate() {
  Object.assign(featureForm, defaultFeature());
  featureDialogVisible.value = true;
}

function openFeatureEdit(row: CampusAiFeatureBinding) {
  Object.assign(featureForm, defaultFeature(), row);
  featureDialogVisible.value = true;
}

async function submitFeature() {
  if (!featureForm.featureCode || !featureForm.featureName) {
    ElMessage.warning('功能编码和功能名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveAiFeature({ ...featureForm });
    ElMessage.success('功能绑定已保存');
    featureDialogVisible.value = false;
    await Promise.all([loadFeatures(), loadOverview()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function openPromptCreate() {
  Object.assign(promptForm, defaultPrompt());
  promptDialogVisible.value = true;
}

function openPromptEdit(row: CampusAiPromptTemplate) {
  Object.assign(promptForm, defaultPrompt(), row);
  promptDialogVisible.value = true;
}

async function submitPrompt() {
  if (!promptForm.featureCode || !promptForm.templateName) {
    ElMessage.warning('功能编码和模板名称不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveAiPrompt({ ...promptForm });
    ElMessage.success('提示词已保存');
    promptDialogVisible.value = false;
    await loadPrompts();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitPromptDelete(row: CampusAiPromptTemplate) {
  if (!row.templateId) return;
  try {
    await ElMessageBox.confirm('确认删除该提示词模板？', '删除确认', { type: 'warning' });
    await deleteAiPrompt(row.templateId);
    ElMessage.success('提示词已删除');
    await loadPrompts();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function defaultProvider(): CampusAiProvider {
  return {
    providerCode: '',
    providerName: '',
    providerType: 'llm',
    authType: 'bearer',
    baseUrl: '',
    credentialRef: '',
    enabled: 1,
    timeoutMs: 30000,
    maxRetries: 0,
    remark: ''
  };
}

function defaultModel(): CampusAiModel {
  return {
    providerCode: '',
    modelCode: '',
    modelName: '',
    contextLength: 64000,
    defaultTemperature: 0.2,
    defaultMaxTokens: 4096,
    supportStream: 1,
    enabled: 1,
    remark: ''
  };
}

function defaultFeature(): CampusAiFeatureBinding {
  return {
    featureCode: '',
    featureName: '',
    featureType: 'llm',
    providerCode: '',
    modelCode: '',
    enabled: 1,
    failureStrategy: 'fail',
    timeoutMs: 30000,
    logPrompt: 0,
    remark: ''
  };
}

function defaultPrompt(): CampusAiPromptTemplate {
  return {
    featureCode: '',
    templateName: '',
    templateVersion: 'v1',
    systemPrompt: '',
    userPrompt: '',
    outputFormat: '',
    enabled: 1,
    remark: ''
  };
}

function providerTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    llm: '大模型',
    web_search: '搜索',
    social_ingest: '社媒接入',
    content_extract: '正文提取',
    legacy_writer: '历史写作',
    legacy_nlp: '历史 NLP',
    legacy_nlp_provider: '旧 NLP',
    deepseek: 'DeepSeek',
    tikhub: 'TikHub',
    baidu_qianfan: '百度千帆',
    jina_reader: 'Jina Reader',
    xie_writer: '写作宝'
  };
  return labels[value || ''] || value || '';
}

function featureTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    llm: '大模型',
    ingest: '接入',
    extract: '提取',
    rule: '规则',
    legacy: '历史'
  };
  return labels[value || ''] || value || '';
}
</script>

<style scoped>
.ai-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.ai-header {
  min-height: 74px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border: 1px solid #d8e0ea;
  border-radius: 6px;
}
.ai-header span {
  font-size: 12px;
  color: #64748b;
}
.ai-header h2 {
  margin: 2px 0;
  font-size: 20px;
  color: #0f172a;
}
.ai-header p {
  margin: 0;
  font-size: 13px;
  color: #475569;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 10px;
}
.metric {
  min-height: 72px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #ffffff;
  border: 1px solid #d8e0ea;
  border-radius: 6px;
}
.metric-icon {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: #f1f5f9;
  color: #334155;
}
.metric-icon.blue {
  background: #e0f2fe;
  color: #0369a1;
}
.metric-icon.green {
  background: #dcfce7;
  color: #166534;
}
.metric-icon.red {
  background: #fee2e2;
  color: #b91c1c;
}
.metric span {
  display: block;
  font-size: 12px;
  color: #64748b;
}
.metric strong {
  font-size: 22px;
  color: #0f172a;
}
.panel {
  padding: 14px;
  background: #ffffff;
  border: 1px solid #d8e0ea;
  border-radius: 6px;
}
.toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.toolbar-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.toolbar-filters .el-input,
.toolbar-filters .el-select {
  width: 190px;
}
.pagination-row {
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.legacy-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.legacy-row {
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
}
.legacy-row strong {
  display: block;
  color: #0f172a;
}
.legacy-row span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}
@media (max-width: 900px) {
  .metric-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
  .toolbar {
    flex-direction: column;
  }
}
</style>
