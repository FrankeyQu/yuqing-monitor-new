<template>
  <section class="business-page">
    <section class="ingest-header">
      <div>
        <span>媒体接入中心</span>
        <h2>数据接入管理</h2>
        <p>统一管理公开、授权、上级移交和白名单公开网页数据接入。</p>
      </div>
      <div class="ingest-header-actions">
        <el-button @click="refreshAll">
          <RefreshCw :size="16" />
          刷新
        </el-button>
        <el-button type="primary" @click="openTaskCreate">
          <Plus :size="16" />
          新增接入任务
        </el-button>
      </div>
    </section>

    <section class="ingest-metrics">
      <article v-for="item in overviewCards" :key="item.label" class="ingest-metric">
        <span class="ingest-metric-icon" :class="item.tone">
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
        <el-tab-pane label="总览" name="overview">
          <div class="ingest-overview-grid">
            <section class="ingest-overview-panel">
              <div class="panel-header">
                <h2>接入边界</h2>
              </div>
              <div class="ingest-boundary-list">
                <div>
                  <ShieldCheck :size="18" />
                  <span>只维护已授权来源和任务配置</span>
                </div>
                <div>
                  <Globe2 :size="18" />
                  <span>公开网页必须先进入白名单，当前不执行真实抓取</span>
                </div>
                <div>
                  <FileSearch :size="18" />
                  <span>接入记录转入线索库后仍需人工研判</span>
                </div>
                <div>
                  <Ban :size="18" />
                  <span>不提供 Cookie、代理、账号池、浏览器指纹或签名配置</span>
                </div>
              </div>
            </section>
          </div>
        </el-tab-pane>

        <el-tab-pane label="接入来源" name="sources">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="sourceQuery.keyword" clearable placeholder="来源/端点/依据" @keyup.enter="loadSources">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-select v-model="sourceQuery.sourceType" clearable placeholder="来源类型">
                <el-option label="人工录入" value="manual" />
                <el-option label="授权接口" value="api" />
                <el-option label="公开RSS" value="rss" />
                <el-option label="公开网页" value="public_web" />
                <el-option label="上级移交" value="upper_transfer" />
              </el-select>
              <el-input v-model.trim="sourceQuery.platform" clearable placeholder="平台" @keyup.enter="loadSources" />
              <el-select v-model="sourceQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadSources">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openSourceCreate">
              <Plus :size="16" />
              新增来源
            </el-button>
          </div>

          <el-table :data="sources" v-loading="sourceLoading" size="small" height="560">
            <el-table-column prop="sourceName" label="来源名称" min-width="170" show-overflow-tooltip />
            <el-table-column prop="sourceType" label="类型" width="110">
              <template #default="{ row }">{{ sourceTypeLabel(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column prop="platform" label="平台" width="110" show-overflow-tooltip />
            <el-table-column prop="authorizationBasis" label="授权依据" min-width="210" show-overflow-tooltip />
            <el-table-column prop="authorizationScope" label="授权范围" min-width="210" show-overflow-tooltip />
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openSourceEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="danger" @click="submitSourceDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="sourceQuery.pageNum"
              v-model:page-size="sourceQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="sourceTotal"
              @size-change="loadSources"
              @current-change="loadSources"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="接入任务" name="tasks">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="taskQuery.keyword" clearable placeholder="任务名称" @keyup.enter="loadTasks">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input-number v-model="taskQuery.sourceId" :min="1" controls-position="right" placeholder="来源ID" />
              <el-select v-model="taskQuery.targetType" clearable placeholder="目标">
                <el-option label="线索库" value="clue" />
                <el-option label="重点账号公开动态" value="account_content" />
                <el-option label="网页占位" value="web_page" />
              </el-select>
              <el-select v-model="taskQuery.taskStatus" clearable placeholder="状态">
                <el-option label="启用" value="active" />
                <el-option label="暂停" value="paused" />
                <el-option label="禁用" value="disabled" />
              </el-select>
              <el-button @click="loadTasks">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openTaskCreate">
              <Plus :size="16" />
              新增任务
            </el-button>
          </div>

          <el-table :data="tasks" v-loading="taskLoading" size="small" height="560">
            <el-table-column prop="taskName" label="任务名称" min-width="170" show-overflow-tooltip />
            <el-table-column prop="sourceId" label="来源ID" width="90" />
            <el-table-column prop="targetType" label="目标" width="150">
              <template #default="{ row }">{{ targetTypeLabel(row.targetType) }}</template>
            </el-table-column>
            <el-table-column prop="adapterType" label="适配器" width="130">
              <template #default="{ row }">{{ adapterTypeLabel(row.adapterType) }}</template>
            </el-table-column>
            <el-table-column prop="taskStatus" label="状态" width="92">
              <template #default="{ row }">
                <el-tag :type="taskStatusTagType(row.taskStatus)" effect="plain">
                  {{ taskStatusLabel(row.taskStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scheduleEnabled" label="调度" width="82">
              <template #default="{ row }">
                <el-tag :type="row.scheduleEnabled === 1 ? 'success' : 'info'" effect="plain">
                  {{ row.scheduleEnabled === 1 ? '自动' : '手动' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scheduleCron" label="计划表达式" min-width="150" show-overflow-tooltip />
            <el-table-column label="重试" width="104">
              <template #default="{ row }">{{ retryLabel(row) }}</template>
            </el-table-column>
            <el-table-column label="日额度" width="116">
              <template #default="{ row }">
                {{ quotaLabel(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="consecutiveFailCount" label="连续失败" width="92">
              <template #default="{ row }">{{ row.consecutiveFailCount ?? 0 }}</template>
            </el-table-column>
            <el-table-column prop="lastRunTime" label="最近运行" width="168" show-overflow-tooltip />
            <el-table-column prop="nextRunTime" label="下次运行" width="168" show-overflow-tooltip />
            <el-table-column prop="scheduleLockUntil" label="运行锁" width="168" show-overflow-tooltip />
            <el-table-column prop="retentionDays" label="保留天数" width="92" />
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTaskEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="success" @click="submitStartRun(row)">
                  <Play :size="15" />
                  运行
                </el-button>
                <el-button link type="warning" @click="toggleTaskStatus(row)">
                  <PauseCircle :size="15" />
                  {{ row.taskStatus === 'active' ? '暂停' : '启用' }}
                </el-button>
                <el-button link type="danger" @click="submitTaskDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="taskQuery.pageNum"
              v-model:page-size="taskQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="taskTotal"
              @size-change="loadTasks"
              @current-change="loadTasks"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="接入记录" name="records">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="recordQuery.keyword" clearable placeholder="标题/内容/作者" @keyup.enter="loadRecords">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input-number v-model="recordQuery.sourceId" :min="1" controls-position="right" placeholder="来源ID" />
              <el-input-number v-model="recordQuery.taskId" :min="1" controls-position="right" placeholder="任务ID" />
              <el-select v-model="recordQuery.normalizedStatus" clearable placeholder="状态">
                <el-option label="待转换" value="pending" />
                <el-option label="已转换" value="converted" />
                <el-option label="已忽略" value="ignored" />
                <el-option label="失败" value="failed" />
              </el-select>
              <el-button @click="loadRecords">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openRecordCreate">
              <Plus :size="16" />
              提交记录
            </el-button>
          </div>

          <el-table :data="records" v-loading="recordLoading" size="small" height="560">
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="sourceId" label="来源ID" width="88" />
            <el-table-column prop="taskId" label="任务ID" width="88" />
            <el-table-column prop="platform" label="平台" width="105" show-overflow-tooltip />
            <el-table-column prop="authorName" label="公开作者" width="130" show-overflow-tooltip />
            <el-table-column prop="keywords" label="关键词" width="140" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="风险" width="82">
              <template #default="{ row }">
                <el-tag :type="riskTagType(row.riskLevel)" effect="plain">{{ riskLabel(row.riskLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="normalizedStatus" label="状态" width="92">
              <template #default="{ row }">
                <el-tag :type="recordStatusTagType(row.normalizedStatus)" effect="plain">
                  {{ recordStatusLabel(row.normalizedStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="168" show-overflow-tooltip />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="submitConvertClue(row)">
                  <ArrowRightCircle :size="15" />
                  转线索
                </el-button>
                <el-button link type="success" @click="openConvertAccount(row)">
                  <UserRoundPlus :size="15" />
                  转动态
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="recordQuery.pageNum"
              v-model:page-size="recordQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="recordTotal"
              @size-change="loadRecords"
              @current-change="loadRecords"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="公开网页白名单" name="whitelists">
          <div class="toolbar">
            <div class="toolbar-filters">
              <el-input v-model.trim="whitelistQuery.keyword" clearable placeholder="站点名称/授权依据" @keyup.enter="loadWhitelists">
                <template #prefix><Search :size="16" /></template>
              </el-input>
              <el-input v-model.trim="whitelistQuery.siteDomain" clearable placeholder="域名" @keyup.enter="loadWhitelists" />
              <el-select v-model="whitelistQuery.enabled" clearable placeholder="状态">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
              <el-button @click="loadWhitelists">
                <Search :size="16" />
                查询
              </el-button>
            </div>
            <el-button type="primary" @click="openWhitelistCreate">
              <Plus :size="16" />
              新增白名单
            </el-button>
          </div>

          <el-alert
            class="data-alert"
            type="info"
            :closable="false"
            show-icon
            title="公开网页白名单只用于合规边界预留，当前 public_web_pull 不执行真实网页抓取。"
          />

          <el-table :data="whitelists" v-loading="whitelistLoading" size="small" height="520">
            <el-table-column prop="siteName" label="站点名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="siteDomain" label="域名" width="180" show-overflow-tooltip />
            <el-table-column prop="allowedPathPrefix" label="路径范围" width="150" show-overflow-tooltip />
            <el-table-column prop="authorizationBasis" label="授权依据" min-width="220" show-overflow-tooltip />
            <el-table-column prop="authorizationScope" label="授权范围" min-width="220" show-overflow-tooltip />
            <el-table-column prop="rateLimitSeconds" label="间隔(秒)" width="92" />
            <el-table-column prop="enabled" label="状态" width="82">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 0 ? 'info' : 'success'" effect="plain">
                  {{ row.enabled === 0 ? '停用' : '启用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="238" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openWhitelistEdit(row)">
                  <Pencil :size="15" />
                  编辑
                </el-button>
                <el-button link type="warning" @click="toggleWhitelistStatus(row)">
                  <PauseCircle :size="15" />
                  {{ row.enabled === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button link type="danger" @click="submitWhitelistDelete(row)">
                  <Trash2 :size="15" />
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-row">
            <el-pagination
              v-model:current-page="whitelistQuery.pageNum"
              v-model:page-size="whitelistQuery.pageSize"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[10, 20, 50]"
              :total="whitelistTotal"
              @size-change="loadWhitelists"
              @current-change="loadWhitelists"
            />
          </div>
        </el-tab-pane>

      </el-tabs>
    </section>

    <el-dialog v-model="sourceDialogVisible" :title="sourceForm.sourceId ? '编辑接入来源' : '新增接入来源'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="来源名称" required>
            <el-input v-model.trim="sourceForm.sourceName" />
          </el-form-item>
          <el-form-item label="来源类型" required>
            <el-select v-model="sourceForm.sourceType">
              <el-option label="人工录入" value="manual" />
              <el-option label="授权接口" value="api" />
              <el-option label="公开RSS" value="rss" />
              <el-option label="公开网页" value="public_web" />
              <el-option label="上级移交" value="upper_transfer" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="平台">
            <el-input v-model.trim="sourceForm.platform" />
          </el-form-item>
          <el-form-item label="访问端点或来源说明">
            <el-input v-model.trim="sourceForm.accessEndpoint" />
          </el-form-item>
        </div>
        <el-form-item label="授权或来源依据" required>
          <el-input v-model.trim="sourceForm.authorizationBasis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="授权范围" required>
          <el-input v-model.trim="sourceForm.authorizationScope" type="textarea" :rows="2" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="责任部门ID">
            <el-input-number v-model="sourceForm.responsibleDepartmentId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="sourceEnabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model.trim="sourceForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sourceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitSource">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="whitelistDialogVisible" :title="whitelistForm.whitelistId ? '编辑公开网页白名单' : '新增公开网页白名单'" width="820px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="站点名称" required>
            <el-input v-model.trim="whitelistForm.siteName" />
          </el-form-item>
          <el-form-item label="站点域名" required>
            <el-input v-model.trim="whitelistForm.siteDomain" placeholder="example.edu.cn" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="基础URL" required>
            <el-input v-model.trim="whitelistForm.baseUrl" placeholder="https://www.example.edu.cn/news/" />
          </el-form-item>
          <el-form-item label="允许路径前缀">
            <el-input v-model.trim="whitelistForm.allowedPathPrefix" placeholder="/news/" />
          </el-form-item>
        </div>
        <el-form-item label="授权或来源依据" required>
          <el-input v-model.trim="whitelistForm.authorizationBasis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="授权范围" required>
          <el-input v-model.trim="whitelistForm.authorizationScope" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="robots或站点规则说明">
          <el-input v-model.trim="whitelistForm.robotsPolicy" type="textarea" :rows="2" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="最小间隔秒">
            <el-input-number v-model="whitelistForm.rateLimitSeconds" :min="60" controls-position="right" />
          </el-form-item>
          <el-form-item label="最大深度">
            <el-input-number v-model="whitelistForm.maxDepth" :min="0" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="责任部门ID">
            <el-input-number v-model="whitelistForm.responsibleDepartmentId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="whitelistEnabled" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model.trim="whitelistForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="whitelistDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitWhitelist">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskDialogVisible" :title="taskForm.taskId ? '编辑接入任务' : '新增接入任务'" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="接入来源" required>
            <el-select v-model="taskForm.sourceId" filterable placeholder="选择已授权来源">
              <el-option
                v-for="source in sources"
                :key="source.sourceId"
                :label="sourceOptionLabel(source)"
                :value="source.sourceId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="任务名称" required>
            <el-input v-model.trim="taskForm.taskName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="目标类型" required>
            <el-select v-model="taskForm.targetType">
              <el-option label="线索库" value="clue" />
              <el-option label="重点账号公开动态" value="account_content" />
              <el-option label="网页占位" value="web_page" />
            </el-select>
          </el-form-item>
          <el-form-item label="适配器">
            <el-select v-model="taskForm.adapterType">
              <el-option label="人工推送" value="manual_push" />
              <el-option v-if="taskForm.adapterType === 'third_party_api'" label="外部接口（历史）" value="third_party_api" disabled />
              <el-option label="百度搜索 (百度千帆)" value="baidu_search" />
              <el-option label="白名单公开网页" value="public_web_pull" />
              <el-option label="授权接口拉取（预留）" value="api_pull" disabled />
              <el-option label="公开RSS拉取（预留）" value="rss_pull" disabled />
              <el-option label="文件导入（预留）" value="file_import" disabled />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="自动调度">
            <el-switch v-model="taskForm.scheduleEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="任务状态">
            <el-select v-model="taskForm.taskStatus">
              <el-option label="启用" value="active" />
              <el-option label="暂停" value="paused" />
              <el-option label="禁用" value="disabled" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="计划表达式">
            <el-input v-model.trim="taskForm.scheduleCron" placeholder="0 0/30 * * * ?" />
          </el-form-item>
          <el-form-item label="失败重试">
            <div class="ingest-inline-controls">
              <el-input-number v-model="taskForm.maxRetryCount" :min="0" :max="10" controls-position="right" />
              <span>次</span>
              <el-input-number v-model="taskForm.retryIntervalMinutes" :min="1" :max="1440" controls-position="right" />
              <span>分钟</span>
            </div>
          </el-form-item>
        </div>
        <section v-if="taskForm.adapterType === 'public_web_pull'" class="adapter-config-panel">
          <div class="form-grid">
            <el-form-item label="公开网页白名单" required>
              <el-select v-model="publicWebConfig.whitelistId" filterable placeholder="选择白名单">
                <el-option
                  v-for="item in enabledWhitelists"
                  :key="item.whitelistId"
                  :label="whitelistOptionLabel(item)"
                  :value="item.whitelistId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="目标URL" required>
              <el-input v-model.trim="publicWebConfig.url" placeholder="https://www.example.edu.cn/news/" />
            </el-form-item>
          </div>
          <div class="form-grid">
            <el-form-item label="读取模式">
              <el-select v-model="publicWebConfig.mode">
                <el-option label="仅校验白名单" value="metadata_only" />
                <el-option label="Jina Reader 正文读取" value="jina_reader" />
              </el-select>
            </el-form-item>
            <el-form-item label="Reader超时">
              <div class="ingest-inline-controls">
                <el-input-number v-model="publicWebConfig.timeoutMs" :min="1000" :max="30000" controls-position="right" />
                <span>ms</span>
              </div>
            </el-form-item>
          </div>
          <el-alert
            class="data-alert"
            :type="publicWebConfig.mode === 'jina_reader' ? 'info' : 'warning'"
            :closable="false"
            show-icon
            :title="publicWebConfig.mode === 'jina_reader'
              ? 'Jina Reader 只读取白名单内单个公开URL正文，不做栏目递归、登录页、Cookie或反爬绕过。'
              : '仅校验白名单并返回空结果，不执行真实网页抓取。'"
          />
        </section>

        <section v-if="taskForm.adapterType === 'baidu_search'" class="adapter-config-panel">
          <el-form-item label="搜索关键词" required>
            <el-input
              v-model.trim="baiduConfig.query"
              maxlength="500"
              show-word-limit
              placeholder="支持 AND/OR/-排除词/site:限定，如：新疆大学 OR 新大 OR 心大 -录取分数线"
            />
          </el-form-item>
          <el-form-item label="资源类型">
            <el-checkbox-group v-model="baiduConfig.resourceTypes">
              <el-checkbox label="web">网页 (web)</el-checkbox>
              <el-checkbox label="news">新闻 (news)</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <div class="form-grid">
            <el-form-item label="单次返回数量">
              <el-input-number v-model="baiduConfig.topK" :min="1" :max="50" controls-position="right" />
            </el-form-item>
            <el-form-item label="API密钥环境变量名">
              <el-input v-model.trim="baiduConfig.credentialRef" placeholder="BAIDU_API_KEY" readonly />
            </el-form-item>
          </div>
          <div class="form-grid">
            <el-form-item label="正文增强">
              <el-switch v-model="baiduConfig.readerEnabled" />
            </el-form-item>
            <el-form-item label="Reader调用上限">
              <el-input-number v-model="baiduConfig.maxReaderCalls" :min="1" :max="50" controls-position="right" :disabled="!baiduConfig.readerEnabled" />
            </el-form-item>
          </div>
          <div class="form-grid" v-if="baiduConfig.readerEnabled">
            <el-form-item label="Reader超时">
              <div class="ingest-inline-controls">
                <el-input-number v-model="baiduConfig.readerTimeoutMs" :min="1000" :max="30000" controls-position="right" />
                <span>ms</span>
              </div>
            </el-form-item>
            <el-form-item label="失败兜底摘要">
              <el-switch v-model="baiduConfig.fallbackToSnippet" />
            </el-form-item>
          </div>
          <el-alert
            class="data-alert"
            type="info"
            :closable="false"
            show-icon
            title="百度负责发现公开URL；开启正文增强后，系统会按调用上限使用 Jina Reader 读取正文，失败时默认保留百度摘要。"
          />
        </section>

        <el-form-item label="接入配置(JSON预览)">
          <el-input v-model.trim="taskForm.fetchConfig" type="textarea" :rows="4" readonly />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="每日API额度">
            <el-input-number v-model="taskForm.dailyQuotaLimit" :min="0" controls-position="right" />
          </el-form-item>
          <el-form-item label="自动暂停阈值">
            <el-input-number v-model="taskForm.autoPauseAfterFailCount" :min="0" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="治理说明">
          <el-input v-model.trim="taskForm.governanceRemark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="任务授权范围" required>
          <el-input v-model.trim="taskForm.authorizationScope" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="原始记录保留天数">
          <el-input-number v-model="taskForm.retentionDays" :min="1" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitTask">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" title="提交接入记录" width="760px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="来源ID" required>
            <el-input-number v-model="recordForm.sourceId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="任务ID">
            <el-input-number v-model="recordForm.taskId" :min="1" controls-position="right" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="平台">
            <el-input v-model.trim="recordForm.platform" />
          </el-form-item>
          <el-form-item label="内容类型">
            <el-input v-model.trim="recordForm.contentType" placeholder="post/comment/article" />
          </el-form-item>
        </div>
        <el-form-item label="标题">
          <el-input v-model.trim="recordForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model.trim="recordForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="原始链接">
            <el-input v-model.trim="recordForm.originalUrl" />
          </el-form-item>
          <el-form-item label="公开作者或账号名">
            <el-input v-model.trim="recordForm.authorName" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="关联重点账号ID">
            <el-input-number v-model="recordForm.accountId" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="发布时间">
            <el-date-picker v-model="recordForm.publishTime" type="datetime" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="关键词">
            <el-input v-model.trim="recordForm.keywords" />
          </el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="recordForm.riskLevel">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="原始数据">
          <el-input v-model.trim="recordForm.rawData" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRecord">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="convertDialogVisible" title="转换为重点账号公开动态" width="520px">
      <el-form label-position="top">
        <el-form-item label="重点账号ID" required>
          <el-input-number v-model="convertAccountId" :min="1" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convertDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitConvertAccount">转换</el-button>
      </template>
    </el-dialog>

  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch, type Component } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ArrowRightCircle,
  Ban,
  FileSearch,
  Globe2,
  PauseCircle,
  Pencil,
  Play,
  Plus,
  RadioTower,
  RefreshCw,
  Search,
  ShieldCheck,
  Trash2,
  UserRoundPlus
} from 'lucide-vue-next';
import {
  convertRecordToAccountContent,
  convertRecordToClue,
  deleteIngestSource,
  deleteIngestTask,
  deletePublicWebWhitelist,
  listIngestRecords,
  listIngestSources,
  listIngestTasks,
  listPublicWebWhitelists,
  runIngestTask,
  saveIngestSource,
  saveIngestTask,
  savePublicWebWhitelist,
  submitIngestRecord,
  updateIngestTaskStatus,
  updatePublicWebWhitelistStatus
} from '../services/detectionIngest';
import { CAMPUS_RISK_OPTIONS, campusRiskLabel, campusRiskTagType } from '../config/campusTaxonomy';
import type {
  CampusIngestRecord,
  CampusIngestSource,
  CampusIngestTask,
  CampusPublicWebWhitelist
} from '../types/api';

const activeTab = ref('overview');
const saving = ref(false);
const sourceLoading = ref(false);
const taskLoading = ref(false);
const recordLoading = ref(false);
const whitelistLoading = ref(false);
const sourceDialogVisible = ref(false);
const taskDialogVisible = ref(false);
const recordDialogVisible = ref(false);
const convertDialogVisible = ref(false);
const whitelistDialogVisible = ref(false);

const sources = ref<CampusIngestSource[]>([]);
const tasks = ref<CampusIngestTask[]>([]);
const records = ref<CampusIngestRecord[]>([]);
const whitelists = ref<CampusPublicWebWhitelist[]>([]);
const sourceTotal = ref(0);
const taskTotal = ref(0);
const recordTotal = ref(0);
const whitelistTotal = ref(0);
const currentRecord = ref<CampusIngestRecord>();
const convertAccountId = ref<number>();

const sourceQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  sourceType: '',
  platform: '',
  enabled: undefined as number | undefined
});
const taskQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  sourceId: undefined as number | undefined,
  targetType: '',
  taskStatus: ''
});
const recordQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  sourceId: undefined as number | undefined,
  taskId: undefined as number | undefined,
  normalizedStatus: '',
  targetType: ''
});
const whitelistQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  siteDomain: '',
  enabled: undefined as number | undefined
});
const sourceForm = reactive<CampusIngestSource>({
  sourceName: '',
  sourceType: 'manual',
  platform: '',
  accessEndpoint: '',
  authorizationBasis: '',
  authorizationScope: '',
  responsibleDepartmentId: undefined,
  enabled: 1,
  remark: ''
});
const taskForm = reactive<CampusIngestTask>({
  sourceId: undefined,
  taskName: '',
  targetType: 'clue',
  adapterType: 'manual_push',
  scheduleCron: '',
  scheduleEnabled: 0,
  fetchConfig: '',
  taskStatus: 'paused',
  maxRetryCount: 0,
  retryIntervalMinutes: 10,
  dailyQuotaLimit: 0,
  autoPauseAfterFailCount: 0,
  governanceRemark: '',
  authorizationScope: '',
  retentionDays: 180
});
const publicWebConfig = reactive({
  whitelistId: undefined as number | undefined,
  url: '',
  mode: 'metadata_only',
  readerProvider: 'jina',
  maxDepth: 0,
  timeoutMs: 15000
});
const baiduConfig = reactive({
  query: '',
  resourceTypes: ['web'] as string[],
  topK: 20,
  credentialRef: 'BAIDU_API_KEY',
  readerEnabled: false,
  readerProvider: 'jina',
  maxReaderCalls: 5,
  readerTimeoutMs: 15000,
  fallbackToSnippet: true
});
const whitelistForm = reactive<CampusPublicWebWhitelist>({
  siteName: '',
  siteDomain: '',
  baseUrl: '',
  allowedPathPrefix: '/',
  authorizationBasis: '',
  authorizationScope: '',
  robotsPolicy: '',
  rateLimitSeconds: 60,
  maxDepth: 0,
  responsibleDepartmentId: undefined,
  enabled: 1,
  remark: ''
});
const recordForm = reactive<CampusIngestRecord>({
  sourceId: undefined,
  taskId: undefined,
  platform: '',
  contentType: 'post',
  title: '',
  content: '',
  originalUrl: '',
  publishTime: undefined,
  authorName: '',
  accountId: undefined,
  keywords: '',
  riskLevel: 'normal',
  rawData: ''
});
const sourceEnabled = computed({
  get: () => sourceForm.enabled !== 0,
  set: (value: boolean) => {
    sourceForm.enabled = value ? 1 : 0;
  }
});

const whitelistEnabled = computed({
  get: () => whitelistForm.enabled !== 0,
  set: (value: boolean) => {
    whitelistForm.enabled = value ? 1 : 0;
  }
});

const enabledWhitelists = computed(() => whitelists.value.filter((item) => item.enabled !== 0));
const usesStructuredConfig = computed(() => taskForm.adapterType === 'public_web_pull' || taskForm.adapterType === 'baidu_search');

const overviewCards = computed<Array<{ label: string; value: string | number; icon: Component; tone: string }>>(() => {
  const activeTasks = tasks.value.filter((item) => item.taskStatus === 'active').length;
  return [
    { label: '接入来源', value: sourceTotal.value, icon: RadioTower, tone: 'tone-blue' },
    { label: '启用任务', value: activeTasks, icon: Play, tone: 'tone-green' },
    { label: '公开网页白名单', value: whitelistTotal.value, icon: Globe2, tone: 'tone-cyan' }
  ];
});

onMounted(refreshAll);
watch(activeTab, (tab) => {
  if (tab === 'tasks') {
    loadTasks();
  }
  if (tab === 'records') {
    loadRecords();
  }
  if (tab === 'whitelists') {
    loadWhitelists();
  }
});

watch(
  () => taskForm.adapterType,
  () => {
    syncStructuredFetchConfig();
  }
);

watch(publicWebConfig, syncStructuredFetchConfig);
watch(baiduConfig, syncStructuredFetchConfig);

async function loadSources() {
  sourceLoading.value = true;
  try {
    const page = await listIngestSources(sourceQuery);
    sources.value = page.list || [];
    sourceTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '接入来源加载失败');
  } finally {
    sourceLoading.value = false;
  }
}

async function loadTasks() {
  taskLoading.value = true;
  try {
    const page = await listIngestTasks(taskQuery);
    tasks.value = page.list || [];
    taskTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '接入任务加载失败');
  } finally {
    taskLoading.value = false;
  }
}

async function loadRecords() {
  recordLoading.value = true;
  try {
    const page = await listIngestRecords(recordQuery);
    records.value = page.list || [];
    recordTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '接入记录加载失败');
  } finally {
    recordLoading.value = false;
  }
}

async function loadWhitelists() {
  whitelistLoading.value = true;
  try {
    const page = await listPublicWebWhitelists(whitelistQuery);
    whitelists.value = page.list || [];
    whitelistTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '公开网页白名单加载失败');
  } finally {
    whitelistLoading.value = false;
  }
}

async function refreshAll() {
  await Promise.all([
    loadSources(),
    loadTasks(),
    loadWhitelists()
  ]);
}

function resetSourceForm() {
  Object.assign(sourceForm, {
    sourceId: undefined,
    sourceName: '',
    sourceType: 'manual',
    platform: '',
    accessEndpoint: '',
    authorizationBasis: '',
    authorizationScope: '',
    responsibleDepartmentId: undefined,
    enabled: 1,
    remark: ''
  });
}

function openSourceCreate() {
  resetSourceForm();
  sourceDialogVisible.value = true;
}

function openSourceEdit(row: CampusIngestSource) {
  Object.assign(sourceForm, row);
  sourceDialogVisible.value = true;
}

async function submitSource() {
  if (!sourceForm.sourceName || !sourceForm.sourceType || !sourceForm.authorizationBasis || !sourceForm.authorizationScope) {
    ElMessage.warning('来源名称、来源类型、授权依据和授权范围不能为空');
    return;
  }
  saving.value = true;
  try {
    await saveIngestSource({ ...sourceForm });
    ElMessage.success('接入来源已保存');
    sourceDialogVisible.value = false;
    await loadSources();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function submitSourceDelete(row: CampusIngestSource) {
  if (!row.sourceId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该接入来源？相关任务请先确认不再使用。', '删除确认', { type: 'warning' });
    await deleteIngestSource(row.sourceId);
    ElMessage.success('接入来源已删除');
    await loadSources();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function resetWhitelistForm() {
  Object.assign(whitelistForm, {
    whitelistId: undefined,
    siteName: '',
    siteDomain: '',
    baseUrl: '',
    allowedPathPrefix: '/',
    authorizationBasis: '',
    authorizationScope: '',
    robotsPolicy: '',
    rateLimitSeconds: 60,
    maxDepth: 0,
    responsibleDepartmentId: undefined,
    enabled: 1,
    remark: ''
  });
}

function openWhitelistCreate() {
  resetWhitelistForm();
  whitelistDialogVisible.value = true;
}

function openWhitelistEdit(row: CampusPublicWebWhitelist) {
  Object.assign(whitelistForm, row);
  whitelistDialogVisible.value = true;
}

async function submitWhitelist() {
  if (!whitelistForm.siteName || !whitelistForm.siteDomain || !whitelistForm.baseUrl
      || !whitelistForm.authorizationBasis || !whitelistForm.authorizationScope) {
    ElMessage.warning('站点名称、域名、基础URL、授权依据和授权范围不能为空');
    return;
  }
  saving.value = true;
  try {
    await savePublicWebWhitelist({ ...whitelistForm });
    ElMessage.success('公开网页白名单已保存');
    whitelistDialogVisible.value = false;
    await loadWhitelists();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function toggleWhitelistStatus(row: CampusPublicWebWhitelist) {
  if (!row.whitelistId) {
    return;
  }
  const nextEnabled = row.enabled === 1 ? 0 : 1;
  try {
    await updatePublicWebWhitelistStatus(row.whitelistId, nextEnabled);
    ElMessage.success('白名单状态已更新');
    await loadWhitelists();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function submitWhitelistDelete(row: CampusPublicWebWhitelist) {
  if (!row.whitelistId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该公开网页白名单？已引用的任务将无法继续运行。', '删除确认', { type: 'warning' });
    await deletePublicWebWhitelist(row.whitelistId);
    ElMessage.success('公开网页白名单已删除');
    await loadWhitelists();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function resetTaskForm() {
  Object.assign(taskForm, {
    taskId: undefined,
    sourceId: taskQuery.sourceId || undefined,
    taskName: '',
    targetType: 'clue',
    adapterType: 'manual_push',
    scheduleCron: '',
    scheduleEnabled: 0,
    fetchConfig: '',
    taskStatus: 'paused',
    maxRetryCount: 0,
    retryIntervalMinutes: 10,
    dailyQuotaLimit: 0,
    autoPauseAfterFailCount: 0,
    governanceRemark: '',
    authorizationScope: '',
    retentionDays: 180
  });
  Object.assign(publicWebConfig, defaultPublicWebConfig());
  Object.assign(baiduConfig, defaultBaiduConfig());
  syncStructuredFetchConfig();
}

function openTaskCreate() {
  resetTaskForm();
  if (sources.value.length === 0) {
    loadSources();
  }
  if (whitelists.value.length === 0) {
    loadWhitelists();
  }
  taskDialogVisible.value = true;
}

function openTaskEdit(row: CampusIngestTask) {
  Object.assign(taskForm, row);
  hydrateStructuredConfig(row);
  if (sources.value.length === 0) {
    loadSources();
  }
  if (whitelists.value.length === 0) {
    loadWhitelists();
  }
  taskDialogVisible.value = true;
}

async function submitTask() {
  if (!taskForm.sourceId || !taskForm.taskName || !taskForm.targetType || !taskForm.authorizationScope) {
    ElMessage.warning('来源ID、任务名称、目标类型和任务授权范围不能为空');
    return;
  }
  if (taskForm.scheduleEnabled === 1 && !taskForm.scheduleCron) {
    ElMessage.warning('启用自动调度时计划表达式不能为空');
    return;
  }
  if (!validateStructuredConfig()) {
    return;
  }
  saving.value = true;
  try {
    await saveIngestTask(buildTaskSavePayload());
    ElMessage.success('接入任务已保存');
    taskDialogVisible.value = false;
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

function buildTaskSavePayload(): CampusIngestTask {
  syncStructuredFetchConfig();
  return {
    taskId: taskForm.taskId,
    sourceId: taskForm.sourceId,
    taskName: taskForm.taskName,
    targetType: taskForm.targetType,
    adapterType: taskForm.adapterType,
    scheduleCron: taskForm.scheduleCron,
    scheduleEnabled: taskForm.scheduleEnabled,
    fetchConfig: taskForm.fetchConfig,
    taskStatus: taskForm.taskStatus,
    maxRetryCount: taskForm.maxRetryCount,
    retryIntervalMinutes: taskForm.retryIntervalMinutes,
    dailyQuotaLimit: taskForm.dailyQuotaLimit,
    autoPauseAfterFailCount: taskForm.autoPauseAfterFailCount,
    governanceRemark: taskForm.governanceRemark,
    authorizationScope: taskForm.authorizationScope,
    retentionDays: taskForm.retentionDays
  };
}

function defaultPublicWebConfig() {
  return {
    whitelistId: undefined,
    url: '',
    mode: 'metadata_only',
    readerProvider: 'jina',
    maxDepth: 0,
    timeoutMs: 15000
  };
}

function defaultBaiduConfig() {
  return {
    query: '',
    resourceTypes: ['web'],
    topK: 20,
    credentialRef: 'BAIDU_API_KEY',
    readerEnabled: false,
    readerProvider: 'jina',
    maxReaderCalls: 5,
    readerTimeoutMs: 15000,
    fallbackToSnippet: true
  };
}

function hydrateStructuredConfig(row: CampusIngestTask) {
  const config = parseJsonObject(row.fetchConfig);
  if (row.adapterType === 'public_web_pull') {
    Object.assign(publicWebConfig, defaultPublicWebConfig(), config || {});
    publicWebConfig.mode = config?.mode || 'metadata_only';
    publicWebConfig.readerProvider = config?.readerProvider || 'jina';
    publicWebConfig.maxDepth = Number(config?.maxDepth ?? 0);
    publicWebConfig.timeoutMs = Number(config?.timeoutMs ?? 15000);
  }
  if (row.adapterType === 'baidu_search') {
    const merged = { ...defaultBaiduConfig(), ...config };
    Object.assign(baiduConfig, merged);
    if (typeof config?.resourceTypes === 'string') {
      baiduConfig.resourceTypes = config.resourceTypes.split(',').filter((s: string) => s);
    }
    if (Array.isArray(config?.resourceTypes)) {
      baiduConfig.resourceTypes = config.resourceTypes;
    }
    baiduConfig.credentialRef = config?.credentialRef || 'BAIDU_API_KEY';
    baiduConfig.readerEnabled = config?.readerEnabled === true || config?.readerEnabled === 'true';
    baiduConfig.readerProvider = config?.readerProvider || 'jina';
    baiduConfig.maxReaderCalls = Number(config?.maxReaderCalls ?? 5);
    baiduConfig.readerTimeoutMs = Number(config?.readerTimeoutMs ?? 15000);
    baiduConfig.fallbackToSnippet = config?.fallbackToSnippet !== false && config?.fallbackToSnippet !== 'false';
  }
  syncStructuredFetchConfig();
}

function syncStructuredFetchConfig() {
  if (taskForm.adapterType === 'public_web_pull') {
    taskForm.fetchConfig = JSON.stringify({
      whitelistId: publicWebConfig.whitelistId,
      url: publicWebConfig.url || '',
      mode: publicWebConfig.mode || 'metadata_only',
      readerProvider: 'jina',
      maxDepth: 0,
      timeoutMs: publicWebConfig.timeoutMs || 15000
    }, null, 2);
    return;
  }
  if (taskForm.adapterType === 'baidu_search') {
    taskForm.fetchConfig = JSON.stringify({
      provider: 'baidu',
      query: baiduConfig.query || '',
      resourceTypes: baiduConfig.resourceTypes || ['web'],
      topK: baiduConfig.topK || 20,
      credentialRef: baiduConfig.credentialRef || 'BAIDU_API_KEY',
      readerEnabled: Boolean(baiduConfig.readerEnabled),
      readerProvider: 'jina',
      maxReaderCalls: baiduConfig.maxReaderCalls || 5,
      readerTimeoutMs: baiduConfig.readerTimeoutMs || 15000,
      fallbackToSnippet: baiduConfig.fallbackToSnippet !== false
    }, null, 2);
    return;
  }
  if (taskForm.adapterType === 'manual_push') {
    taskForm.fetchConfig = '';
  }
}

function validateStructuredConfig() {
  if (taskForm.adapterType === 'third_party_api') {
    ElMessage.warning('历史外部接口任务不支持在后台编辑');
    return false;
  }
  if (taskForm.adapterType === 'public_web_pull' && (!publicWebConfig.whitelistId || !publicWebConfig.url)) {
    ElMessage.warning('公开网页任务必须选择白名单并填写目标URL');
    return false;
  }
  if (taskForm.adapterType === 'public_web_pull' && publicWebConfig.mode === 'jina_reader' && publicWebConfig.timeoutMs <= 0) {
    ElMessage.warning('公开网页 Reader 超时时间必须大于 0');
    return false;
  }
  if (taskForm.adapterType === 'baidu_search' && !baiduConfig.query) {
    ElMessage.warning('百度搜索任务必须填写搜索关键词');
    return false;
  }
  if (taskForm.adapterType === 'baidu_search' && (!baiduConfig.resourceTypes || baiduConfig.resourceTypes.length === 0)) {
    ElMessage.warning('百度搜索任务必须选择至少一个资源类型');
    return false;
  }
  if (taskForm.adapterType === 'baidu_search' && baiduConfig.readerEnabled && baiduConfig.maxReaderCalls <= 0) {
    ElMessage.warning('Reader 调用上限必须大于 0');
    return false;
  }
  if (!usesStructuredConfig.value && hasForbiddenConfigText(taskForm.fetchConfig)) {
    ElMessage.warning('接入配置不能包含密钥、Cookie、Token、代理、指纹或签名参数');
    return false;
  }
  return true;
}

function parseJsonObject(value?: string) {
  if (!value) {
    return undefined;
  }
  try {
    const parsed = JSON.parse(value);
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : undefined;
  } catch {
    return undefined;
  }
}

function hasForbiddenConfigText(value?: string) {
  return /(api[_-]?key|access[_-]?token|authorization|cookie|password|session|secret|token|proxy|fingerprint|device[_-]?id|xBogus|aBogus|sign|signature)/i.test(value || '');
}

async function submitTaskDelete(row: CampusIngestTask) {
  if (!row.taskId) {
    return;
  }
  try {
    await ElMessageBox.confirm('确认删除该接入任务？', '删除确认', { type: 'warning' });
    await deleteIngestTask(row.taskId);
    ElMessage.success('接入任务已删除');
    await loadTasks();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

async function toggleTaskStatus(row: CampusIngestTask) {
  if (!row.taskId) {
    return;
  }
  const nextStatus = row.taskStatus === 'active' ? 'paused' : 'active';
  try {
    await updateIngestTaskStatus(row.taskId, nextStatus);
    ElMessage.success('任务状态已更新');
    await loadTasks();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function submitStartRun(row: CampusIngestTask) {
  if (!row.taskId) {
    return;
  }
  if (row.taskStatus !== 'active') {
    ElMessage.warning('请先启用任务再运行');
    return;
  }
  try {
    const runLog = await runIngestTask(row.taskId);
    const failCount = runLog.failCount || 0;
    const message = `${runLog.runStatus === 'partial_success' ? '部分成功' : '运行完成'}：拉取 ${runLog.fetchedCount || 0} 条，成功 ${runLog.successCount || 0} 条`
      + (failCount > 0 ? `，失败 ${failCount} 条` : '');
    if (runLog.runStatus === 'partial_success') {
      ElMessage.warning(message);
    } else {
      ElMessage.success(message);
    }
    await Promise.all([loadTasks(), loadRecords()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '运行失败');
    await loadTasks();
  }
}

function resetRecordForm() {
  Object.assign(recordForm, {
    recordId: undefined,
    sourceId: recordQuery.sourceId || undefined,
    taskId: recordQuery.taskId || undefined,
    platform: '',
    contentType: 'post',
    title: '',
    content: '',
    originalUrl: '',
    publishTime: new Date(),
    authorName: '',
    accountId: undefined,
    keywords: '',
    riskLevel: 'normal',
    rawData: ''
  });
}

function openRecordCreate() {
  resetRecordForm();
  recordDialogVisible.value = true;
}

async function submitRecord() {
  if (!recordForm.sourceId || (!recordForm.title && !recordForm.content)) {
    ElMessage.warning('来源ID以及标题或内容不能为空');
    return;
  }
  saving.value = true;
  try {
    await submitIngestRecord({ ...recordForm });
    ElMessage.success('接入记录已提交');
    recordDialogVisible.value = false;
    await loadRecords();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交失败');
  } finally {
    saving.value = false;
  }
}

async function submitConvertClue(row: CampusIngestRecord) {
  if (!row.recordId) {
    return;
  }
  try {
    await convertRecordToClue(row.recordId);
    ElMessage.success('已转换为线索');
    await loadRecords();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '转换失败');
  }
}

function openConvertAccount(row: CampusIngestRecord) {
  currentRecord.value = row;
  convertAccountId.value = row.accountId;
  convertDialogVisible.value = true;
}

async function submitConvertAccount() {
  if (!currentRecord.value?.recordId || !convertAccountId.value) {
    ElMessage.warning('重点账号ID不能为空');
    return;
  }
  saving.value = true;
  try {
    await convertRecordToAccountContent(currentRecord.value.recordId, convertAccountId.value);
    ElMessage.success('已转换为重点账号公开动态');
    convertDialogVisible.value = false;
    await loadRecords();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '转换失败');
  } finally {
    saving.value = false;
  }
}

function sourceTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    manual: '人工录入',
    api: '授权接口',
    rss: '公开RSS',
    public_web: '公开网页',
    upper_transfer: '上级移交'
  };
  return labels[value || 'manual'] || value || '人工录入';
}

function sourceOptionLabel(source: CampusIngestSource) {
  const name = source.sourceName || '未命名来源';
  const platform = source.platform ? ` · ${source.platform}` : '';
  return `${name}${platform} · ${sourceTypeLabel(source.sourceType)}`;
}

function whitelistOptionLabel(item: CampusPublicWebWhitelist) {
  const name = item.siteName || '未命名站点';
  const path = item.allowedPathPrefix || '/';
  return `${name} · ${item.siteDomain}${path}`;
}

function targetTypeLabel(value?: string) {
  const labels: Record<string, string> = { clue: '线索库', account_content: '重点账号公开动态', web_page: '网页占位' };
  return labels[value || 'clue'] || value || '线索库';
}

function adapterTypeLabel(value?: string) {
  const labels: Record<string, string> = {
    manual_push: '人工推送',
    api_pull: '授权接口拉取',
    third_party_api: '外部接口（历史）',
    baidu_search: '百度搜索',
    public_web_pull: '白名单公开网页',
    rss_pull: '公开RSS拉取',
    file_import: '文件导入'
  };
  return labels[value || 'manual_push'] || value || '人工推送';
}

function taskStatusLabel(value?: string) {
  const labels: Record<string, string> = { active: '启用', paused: '暂停', disabled: '禁用' };
  return labels[value || 'paused'] || value || '暂停';
}

function taskStatusTagType(value?: string) {
  if (value === 'active') {
    return 'success';
  }
  if (value === 'paused') {
    return 'warning';
  }
  return 'info';
}

function quotaLabel(row: CampusIngestTask) {
  const limit = row.dailyQuotaLimit ?? 0;
  if (!limit) {
    return '不限';
  }
  return `${row.dailyQuotaUsed ?? 0}/${limit}`;
}

function retryLabel(row: CampusIngestTask) {
  const maxRetry = row.maxRetryCount ?? 0;
  if (!maxRetry) {
    return '不重试';
  }
  return `${row.currentRetryCount ?? 0}/${maxRetry}`;
}

function recordStatusLabel(value?: string) {
  const labels: Record<string, string> = { pending: '待转换', converted: '已转换', ignored: '已忽略', failed: '失败' };
  return labels[value || 'pending'] || value || '待转换';
}

function recordStatusTagType(value?: string) {
  if (value === 'converted') {
    return 'success';
  }
  if (value === 'failed') {
    return 'danger';
  }
  if (value === 'ignored') {
    return 'info';
  }
  return 'warning';
}

function riskLabel(value?: string) {
  return campusRiskLabel(value);
}

function riskTagType(value?: string) {
  return campusRiskTagType(value);
}

</script>
