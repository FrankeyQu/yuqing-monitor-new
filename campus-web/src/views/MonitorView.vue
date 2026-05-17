<template>
  <section class="clue-list-page">
    <!-- ====== Filter Panel ====== -->
    <div class="filter-card">
      <div class="filter-card-header">
        <span class="filter-title">筛选条件</span>
        <div class="filter-header-right">
          <el-button text size="small" @click="filterExpanded = !filterExpanded">
            {{ filterExpanded ? '收起筛选' : '展开筛选' }}
          </el-button>
        </div>
      </div>

      <div v-show="filterExpanded" class="filter-body">
        <!-- 采集时间 -->
        <div class="filter-row">
          <span class="filter-label">采集时间：</span>
          <el-radio-group v-model="collectTimePreset" size="small" @change="onCollectTimePresetChange">
            <el-radio-button value="today">今天</el-radio-button>
            <el-radio-button value="week">本周</el-radio-button>
            <el-radio-button value="month">本月</el-radio-button>
            <el-radio-button value="year">本年</el-radio-button>
            <el-radio-button value="custom">自定义</el-radio-button>
          </el-radio-group>
          <template v-if="collectTimePreset === 'custom'">
            <el-date-picker
              v-model="collectTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              value-format="YYYY-MM-DD"
              @change="onCollectTimeCustomChange"
            />
          </template>
        </div>

        <!-- 发布时间 -->
        <div class="filter-row">
          <span class="filter-label">发布时间：</span>
          <el-radio-group v-model="publishTimePreset" size="small" @change="onPublishTimePresetChange">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="today">今天</el-radio-button>
            <el-radio-button value="week">本周</el-radio-button>
            <el-radio-button value="month">本月</el-radio-button>
            <el-radio-button value="custom">自定义</el-radio-button>
          </el-radio-group>
          <template v-if="publishTimePreset === 'custom'">
            <el-date-picker
              v-model="publishTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              value-format="YYYY-MM-DD"
              @change="onPublishTimeCustomChange"
            />
          </template>
        </div>

        <!-- 情感类型 -->
        <div class="filter-row">
          <span class="filter-label">情感类型：</span>
          <el-checkbox-group v-model="sentimentChecks" size="small" @change="onSentimentChange">
            <el-checkbox label="all">全部</el-checkbox>
            <el-checkbox label="positive">正面</el-checkbox>
            <el-checkbox label="neutral">中性</el-checkbox>
            <el-checkbox label="negative">负面</el-checkbox>
            <el-checkbox label="none">未知</el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 处理状态 -->
        <div class="filter-row">
          <span class="filter-label">处理状态：</span>
          <el-radio-group v-model="query.resultStatus" size="small" @change="handleFilterChange">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="pending">待处理</el-radio-button>
            <el-radio-button value="alerted">已预警</el-radio-button>
            <el-radio-button value="ignored">已忽略</el-radio-button>
            <el-radio-button value="handled">已处理</el-radio-button>
            <el-radio-button value="converted">已转线索</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 合并相似信息 + 匹配对象 -->
        <div class="filter-row">
          <span class="filter-label">合并相似信息：</span>
          <el-switch v-model="similarDedup" size="small" @change="onSimilarDedupChange" />
          <span class="filter-label" style="margin-left: 16px;">匹配对象：</span>
          <el-radio-group v-model="query.matchScope" size="small" :disabled="!similarDedup" @change="handleFilterChange">
            <el-radio-button value="title">仅标题</el-radio-button>
            <el-radio-button value="content">仅内容</el-radio-button>
            <el-radio-button value="both">标题+内容</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 排序 + 关键词 -->
        <div class="filter-row">
          <span class="filter-label">排序：</span>
          <el-select v-model="query.sortBy" size="small" placeholder="发布时间" style="width: 140px" @change="handleFilterChange">
            <el-option label="发布时间" value="publishTime" />
            <el-option label="采集时间" value="collectTime" />
            <el-option label="相关度" value="relevance" />
            <el-option label="情感" value="sentiment" />
          </el-select>
          <span class="filter-label" style="margin-left: 16px;">搜索关键词：</span>
          <el-input
            v-model="searchKeyword"
            size="small"
            placeholder="请输入关键词"
            style="width: 220px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-button size="small" type="primary" @click="handleSearch">搜索</el-button>
        </div>

        <!-- 风险等级 + 线索状态 + 语言 -->
        <div class="filter-row">
          <span class="filter-label">风险等级：</span>
          <el-select v-model="query.riskLevel" size="small" clearable placeholder="全部" style="width: 130px" @change="handleFilterChange">
            <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
          </el-select>
          <span class="filter-label" style="margin-left: 16px;">线索状态：</span>
          <el-select v-model="query.clueStatus" size="small" clearable placeholder="全部" style="width: 130px" @change="handleFilterChange">
            <el-option label="待研判" value="pending_judge" />
            <el-option label="已研判" value="judged" />
            <el-option label="已归档" value="archived" />
            <el-option label="已转事件" value="converted" />
          </el-select>
          <span class="filter-label" style="margin-left: 16px;">语言：</span>
          <el-select v-model="query.language" size="small" clearable placeholder="全部" style="width: 110px" @change="handleFilterChange">
            <el-option label="全部" value="" />
            <el-option label="中文" value="zh" />
            <el-option label="蒙语" value="mongolian" />
            <el-option label="维语" value="uyghur" />
          </el-select>
        </div>
      </div>
    </div>

    <!-- ====== Content Card ====== -->
    <div class="content-card monitor-info-card">
      <div class="content-card-title monitor-info-title">
        <span class="monitor-info-heading">
          <span>监测信息</span>
          <span class="monitor-info-count">共 {{ infoTotal }} 条</span>
        </span>
        <el-radio-group v-model="query.hitScope" size="small" @change="onHitScopeChange">
          <el-radio-button value="risk">风险命中</el-radio-button>
          <el-radio-button value="all">全部真实命中</el-radio-button>
        </el-radio-group>
      </div>
      <!-- 媒体类型 Tabs -->
      <el-tabs v-model="mediaTypeTab" class="media-tabs" @tab-change="onMediaTypeChange">
        <el-tab-pane v-for="mt in mediaTypes" :key="mt.value" :name="mt.value">
          <template #label>
            <span class="platform-tab-label">
              <span>{{ mt.name }}</span>
              <span v-if="mt.value === '全部' || mt.count > 0">({{ mt.count }})</span>
              <span v-else-if="mt.connectionLabel" class="platform-tab-status" :class="{ muted: mt.connectionLabel === '未接入', warning: mt.connectionLabel === '未启用' }">
                {{ mt.connectionLabel }}
              </span>
              <span v-else>(0)</span>
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 子平台 Tabs -->
      <el-tabs v-if="showSubTabs" v-model="subPlatformTab" class="sub-tabs" @tab-change="onSubPlatformChange">
        <el-tab-pane v-for="sp in subPlatforms" :key="sp.value" :name="sp.value">
          <template #label>
            <span>{{ sp.name }}({{ sp.count }})</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span
            v-for="s in sentimentTags"
            :key="s.value"
            class="sentiment-tag"
            :class="{ active: query.sentiment === s.value }"
            :style="{ color: s.color, borderColor: query.sentiment === s.value ? s.color : '#dcdfe6' }"
            @click="toggleSentiment(s.value)"
          >
            {{ s.label }}
          </span>
        </div>
        <div class="toolbar-right">
          <el-button size="small" @click="refreshInfoList">
            <RefreshCw :size="14" /> 刷新
          </el-button>
          <el-popover placement="bottom-end" trigger="click" width="240">
            <div class="column-settings">
              <div class="column-settings-title">列设置</div>
              <div
                v-for="col in infoColumns"
                :key="col.key"
                class="column-setting-row"
                draggable="true"
                @dragstart="onColumnDragStart(col.key)"
                @dragover.prevent
                @drop="onColumnDrop(col.key)"
              >
                <span class="column-drag-handle">::</span>
                <el-checkbox v-model="col.visible" :disabled="col.required">{{ col.label }}</el-checkbox>
              </div>
            </div>
            <template #reference>
              <el-button size="small">
                <Settings2 :size="14" /> 列设置
              </el-button>
            </template>
          </el-popover>
          <el-button size="small" @click="markPageRead">标记本页已读</el-button>
          <el-button size="small" @click="openCreateClue">
            <Plus :size="14" /> 新增人工线索
          </el-button>
          <el-button size="small" @click="handleExport">导出</el-button>
          <el-button size="small" @click="handleBatchOp">批量操作</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        ref="tableRef"
        :data="monitorInfos"
        v-loading="loading"
        size="small"
        class="clue-table"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column
          v-for="col in visibleInfoColumns"
          :key="col.key"
          :width="col.width"
          :min-width="col.minWidth"
          :align="col.align"
          :fixed="col.fixed"
          :show-overflow-tooltip="col.tooltip"
        >
          <template #header>
            <span
              class="draggable-column-header"
              draggable="true"
              @dragstart="onColumnDragStart(col.key)"
              @dragover.prevent
              @drop="onColumnDrop(col.key)"
            >
              {{ col.label }}
            </span>
          </template>
          <template #default="{ row, $index }">
            <span v-if="col.key === 'index'">{{ rowIndex($index) }}</span>
            <div v-else-if="col.key === 'sentiment'" class="sentiment-edit-cell" :title="sentimentEditDisabledReason(row)">
              <el-dropdown
                v-if="canEditMonitorSentiment(row)"
                trigger="click"
                @command="onMonitorSentimentCommand(row, $event)"
              >
                <button class="sentiment-badge-trigger" type="button" :disabled="isSentimentUpdating(row)">
                  <EmotionBadge :emotion="row.sentiment || ''" />
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="option in sentimentEditOptions"
                      :key="option.value"
                      :command="option.value"
                      :disabled="normalizeSentimentValue(row.sentiment) === option.value"
                    >
                      {{ option.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <EmotionBadge v-else :emotion="row.sentiment || ''" />
            </div>
            <div v-else-if="col.key === 'title'" class="title-summary-cell">
              <div class="clue-title" v-html="highlightTitle(row.title || '')" />
              <div class="clue-summary">{{ row.summary || row.content || '' }}</div>
            </div>
            <span v-else-if="col.key === 'publishTime'">{{ publishTimeLabel(row) }}</span>
            <PlatformBadge v-else-if="col.key === 'platform'" :platform="row.platform || row.sourcePlatform || ''" />
            <el-tag
              v-else-if="col.key === 'contentCapture'"
              :type="contentCaptureTagType(row.contentCaptureStatus)"
              effect="plain"
              size="small"
            >
              {{ contentCaptureLabel(row) }}
            </el-tag>
            <span v-else-if="col.key === 'author'" class="ellipsis-cell">{{ row.authorName || row.involvedAccount || '-' }}</span>
            <span v-else-if="col.key === 'keywords'" class="ellipsis-cell">{{ row.matchedKeywords || row.keywords || '-' }}</span>
            <span v-else-if="col.key === 'negativeWords'" class="ellipsis-cell">{{ row.matchedNegativeWords || '-' }}</span>
            <span v-else-if="col.key === 'interaction'">{{ interactionLabel(row) }}</span>
            <el-tag v-else-if="col.key === 'riskLevel'" :type="clueRiskTagType(row.riskLevel)" effect="plain" size="small">
              {{ clueRiskLabel(row.riskLevel) }}
            </el-tag>
            <el-tag v-else-if="col.key === 'topicCategory'" effect="plain" size="small">
              {{ topicLabel(row.topicCategory) }}
            </el-tag>
            <span v-else-if="col.key === 'schoolRelevance'">{{ relevanceLabel(row) }}</span>
            <el-tag v-else-if="col.key === 'language'" :type="languageTagType(row.language)" effect="plain" size="small">
              {{ languageLabel(row.language) }}
            </el-tag>
            <el-tooltip
              v-else-if="col.key === 'status'"
              placement="top"
              :disabled="!monitorInformationStatusReason(row)"
              :content="monitorInformationStatusReason(row)"
            >
              <el-tag effect="plain" size="small" class="status-tag-with-reason">
                {{ monitorInformationStatusLabel(row) }}
              </el-tag>
            </el-tooltip>
            <span v-else-if="col.key === 'infoTime'">{{ formatTime(row.collectTime || row.infoTime || row.discoverTime || row.createTime) }}</span>
            <div v-else-if="col.key === 'actions'" class="row-actions">
              <el-button link type="primary" size="small" @click="viewMonitorInformation(row)">详情</el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary" size="small">
                  更多 <ArrowDown :size="12" />
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="row.monitorResultId && !row.clueId" :disabled="!canMonitorOperate" @click="convertResult(toMonitorResult(row))">转线索</el-dropdown-item>
                    <el-dropdown-item v-if="row.clueId" @click="joinClue(toClue(row))">加入事件</el-dropdown-item>
                    <el-dropdown-item v-if="row.clueId && row.clueStatus !== 'archived'" @click="openEditClue(toClue(row))">编辑线索</el-dropdown-item>
                    <el-dropdown-item v-if="row.clueId && row.clueStatus !== 'judged' && row.clueStatus !== 'archived'" @click="openJudgeClue(toClue(row))">研判</el-dropdown-item>
                    <el-dropdown-item v-if="row.clueId && row.clueStatus !== 'archived'" @click="openArchiveClue(toClue(row))">归档</el-dropdown-item>
                    <el-dropdown-item v-if="row.clueId" divided @click="handleDeleteClue(toClue(row))">删除线索</el-dropdown-item>
                    <el-dropdown-item v-if="row.monitorResultId" :disabled="!canMonitorOperate || row.resultStatus === 'alerted'" @click="alertResult(toMonitorResult(row))">转预警</el-dropdown-item>
                    <el-dropdown-item v-if="row.monitorResultId" :disabled="!canMonitorOperate || row.resultStatus === 'ignored'" @click="ignoreResult(toMonitorResult(row))">忽略</el-dropdown-item>
                    <el-dropdown-item v-if="row.monitorResultId" :disabled="!canMonitorOperate" @click="addResultWatchTarget(toMonitorResult(row), 'account')">加入重点账号</el-dropdown-item>
                    <el-dropdown-item v-if="row.monitorResultId" :disabled="!canMonitorOperate" @click="addResultWatchTarget(toMonitorResult(row), 'link')">加入指定链接</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50, 100]"
          :total="infoTotal"
          background
          small
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <div v-if="false" class="content-card monitor-result-card">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-input v-model.trim="resultQuery.keyword" clearable placeholder="标题/正文/命中词" @keyup.enter="loadMonitorResults" />
          <el-input-number v-model="resultQuery.monitorTaskId" :min="1" controls-position="right" placeholder="任务ID" />
          <el-input v-model.trim="resultQuery.platform" clearable placeholder="平台" @keyup.enter="loadMonitorResults" />
          <el-select v-model="resultQuery.riskLevel" clearable placeholder="风险">
            <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
          </el-select>
          <el-select v-model="resultQuery.resultStatus" clearable placeholder="状态">
            <el-option label="待处理" value="pending" />
            <el-option label="已预警" value="alerted" />
            <el-option label="已忽略" value="ignored" />
            <el-option label="已处理" value="handled" />
            <el-option label="已转线索" value="converted" />
          </el-select>
          <el-select v-model="resultQuery.language" clearable placeholder="语言">
            <el-option label="中文" value="zh" />
            <el-option label="蒙语" value="mongolian" />
            <el-option label="维语" value="uyghur" />
          </el-select>
          <el-select v-model="resultConvertedFilter" clearable placeholder="转线索">
            <el-option label="已转线索" value="yes" />
            <el-option label="未转线索" value="no" />
          </el-select>
          <el-button type="primary" @click="loadMonitorResults">查询</el-button>
        </div>
        <div class="toolbar-right">
          <el-button size="small" @click="loadMonitorResults">
            <RefreshCw :size="14" /> 刷新
          </el-button>
        </div>
      </div>

      <div class="watch-target-panel">
        <div class="watch-target-header">
          <div>
            <strong>本任务重点账号/链接</strong>
            <span>按任务 ID 管理扫描范围，账号/链接命中后在范围内搜索关键词</span>
          </div>
          <div class="watch-target-actions">
            <el-input-number v-model="watchTargetQuery.monitorTaskId" :min="1" controls-position="right" placeholder="任务ID" />
            <el-select v-model="watchTargetQuery.targetType" clearable placeholder="类型">
              <el-option label="账号" value="account" />
              <el-option label="链接" value="link" />
            </el-select>
            <el-select v-model="watchTargetQuery.targetStatus" clearable placeholder="状态">
              <el-option label="启用" value="active" />
              <el-option label="暂停" value="paused" />
            </el-select>
            <el-button @click="loadWatchTargets">查询</el-button>
            <el-button type="primary" :disabled="!canMonitorOperate" @click="openWatchTargetCreate">新增</el-button>
          </div>
        </div>
        <el-table :data="watchTargets" v-loading="watchTargetLoading" size="small" border empty-text="暂无重点监控目标">
          <el-table-column prop="targetType" label="类型" width="80">
            <template #default="{ row }">{{ row.targetType === 'link' ? '链接' : '账号' }}</template>
          </el-table-column>
          <el-table-column prop="platform" label="平台" width="110" show-overflow-tooltip />
          <el-table-column prop="accountName" label="账号" width="140" show-overflow-tooltip />
          <el-table-column prop="linkUrl" label="链接/主页" min-width="220" show-overflow-tooltip />
          <el-table-column prop="keywordScope" label="补充关键词" min-width="160" show-overflow-tooltip />
          <el-table-column prop="authorizationScope" label="授权/来源" min-width="180" show-overflow-tooltip />
          <el-table-column prop="targetStatus" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.targetStatus === 'paused' ? 'info' : 'success'" effect="plain">
                {{ row.targetStatus === 'paused' ? '暂停' : '启用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="!canMonitorOperate" @click="openWatchTargetEdit(row)">编辑</el-button>
              <el-button link type="warning" :disabled="!canMonitorOperate" @click="toggleWatchTargetStatus(row)">
                {{ row.targetStatus === 'paused' ? '启用' : '暂停' }}
              </el-button>
              <el-button link type="danger" :disabled="!canMonitorOperate" @click="deleteWatchTargetRow(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-row compact">
          <el-pagination
            v-model:current-page="watchTargetQuery.pageNum"
            v-model:page-size="watchTargetQuery.pageSize"
            layout="total, prev, pager, next"
            :page-sizes="[5, 10, 20]"
            :total="watchTargetTotal"
            small
            @size-change="loadWatchTargets"
            @current-change="loadWatchTargets"
          />
        </div>
      </div>

      <el-table :data="monitorResults" v-loading="resultLoading" size="small" class="clue-table" empty-text="暂无监测结果">
        <el-table-column prop="title" label="命中内容" min-width="340" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="title-summary-cell">
              <div class="clue-title">{{ row.title || '未命名内容' }}</div>
              <div class="clue-summary">{{ row.content || '' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="monitorTaskId" label="任务ID" width="150" show-overflow-tooltip />
        <el-table-column prop="platform" label="平台" width="110" show-overflow-tooltip />
        <el-table-column prop="authorName" label="账号/作者" width="140" show-overflow-tooltip />
        <el-table-column prop="matchedSubjects" label="命中主体" width="140" show-overflow-tooltip />
        <el-table-column prop="matchedKeywords" label="关键词" width="150" show-overflow-tooltip />
        <el-table-column prop="matchedNegativeWords" label="负面词" width="130" show-overflow-tooltip />
        <el-table-column label="互动" width="160">
          <template #default="{ row }">
            <span>{{ interactionLabel(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险" width="82">
          <template #default="{ row }">
            <el-tag :type="clueRiskTagType(row.riskLevel)" effect="plain">{{ clueRiskLabel(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultStatus" label="状态" width="92">
          <template #default="{ row }">
            <el-tag effect="plain">{{ monitorResultStatusLabel(row.resultStatus, row.clueId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="312" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.clueId" link type="primary" @click="viewMonitorResultClue(row)">查看线索</el-button>
            <el-button v-else link type="primary" :disabled="!canMonitorOperate" @click="convertResult(row)">转线索</el-button>
            <el-button link type="success" :disabled="!canMonitorOperate" @click="addResultWatchTarget(row, 'account')">加账号</el-button>
            <el-button link type="success" :disabled="!canMonitorOperate" @click="addResultWatchTarget(row, 'link')">加链接</el-button>
            <el-button link type="warning" :disabled="!canMonitorOperate || row.resultStatus === 'alerted'" @click="alertResult(row)">转预警</el-button>
            <el-button link type="info" :disabled="!canMonitorOperate || row.resultStatus === 'ignored'" @click="ignoreResult(row)">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="resultQuery.pageNum"
          v-model:page-size="resultQuery.pageSize"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          :total="resultTotal"
          background
          small
          @size-change="loadMonitorResults"
          @current-change="loadMonitorResults"
        />
      </div>
    </div>

    <!-- ====== 主题分析 Tab ====== -->
    <div v-if="pageTab === 'topicAnalysis'" class="topic-analysis-panel">
      <section class="analysis-grid">
        <article class="analysis-panel">
          <div class="panel-header"><h3>关键词 Top 15</h3></div>
          <div ref="keywordChartRef" class="chart-box" style="min-height: 360px;" />
        </article>
        <article class="analysis-panel">
          <div class="panel-header"><h3>媒体类型分布</h3></div>
          <div ref="mediaTypeChartRef" class="chart-box" style="min-height: 360px;" />
        </article>
      </section>
      <section class="analysis-grid">
        <article class="analysis-panel">
          <div class="panel-header"><h3>情感分布</h3></div>
          <div ref="sentimentChartRef" class="chart-box" style="min-height: 360px;" />
        </article>
        <article class="analysis-panel">
          <div class="panel-header"><h3>关键词列表</h3></div>
          <el-table :data="topKeywords" size="small" max-height="340" class="analysis-table" empty-text="暂无关键词数据">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="name" label="关键词" min-width="160" show-overflow-tooltip />
            <el-table-column prop="value" label="出现次数" width="120" align="center" />
          </el-table>
        </article>
      </section>
    </div>

    <!-- ====== 主题预警 Tab ====== -->
    <div v-if="pageTab === 'topicAlert'" class="topic-alert-panel">
      <div class="content-card">
        <div class="toolbar">
          <div class="toolbar-left">
            <span class="filter-label">风险级别：</span>
            <el-select v-model="alertRiskFilter" size="small" clearable placeholder="全部" style="width: 140px" @change="loadAlertData">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </div>
          <div class="toolbar-right">
            <el-button size="small" @click="loadAlertData">
              <RefreshCw :size="14" /> 刷新
            </el-button>
          </div>
        </div>
        <el-table :data="alertList" v-loading="alertLoading" size="small" class="alert-table" empty-text="暂无预警数据">
          <el-table-column prop="alertTitle" label="预警名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="matchedKeywords" label="匹配关键词" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.matchedKeywords || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="riskLevel" label="风险级别" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="riskLevelTagType(row.riskLevel)" size="small">{{ clueRiskLabel(row.riskLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alertStatus" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="alertStatusTagType(row.alertStatus)" size="small">{{ alertStatusLabel(row.alertStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" align="center">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="viewAlertDetail(row)">查看</el-button>
              <el-button link type="primary" size="small" @click="handleAlertItem(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-row">
          <el-pagination
            v-model:current-page="alertQuery.pageNum"
            v-model:page-size="alertQuery.pageSize"
            layout="total, sizes, prev, pager, next"
            :page-sizes="[10, 20, 50]"
            :total="alertTotal"
            background
            small
            @size-change="loadAlertData"
            @current-change="loadAlertData"
          />
        </div>
      </div>
    </div>

    <!-- ====== 站内内容详情 Dialog ====== -->
    <el-dialog v-model="informationDetailVisible" title="内容详情" width="760px" destroy-on-close class="information-detail-dialog">
      <div v-if="currentInformation" class="information-detail">
        <div class="information-detail-header">
          <div class="information-detail-title">{{ currentInformation.title || '未命名内容' }}</div>
          <div class="information-detail-meta">
            <PlatformBadge :platform="currentInformation.platform || currentInformation.sourcePlatform || ''" />
            <span>{{ currentInformation.authorName || currentInformation.involvedAccount || '未知账号' }}</span>
            <span>{{ currentInformation.publishTime ? formatTime(currentInformation.publishTime) : '发布时间未知' }}</span>
            <span>采集于 {{ formatTime(currentInformation.collectTime || currentInformation.infoTime || currentInformation.discoverTime || currentInformation.createTime) }}</span>
            <span>{{ contentCaptureLabel(currentInformation) }}</span>
          </div>
        </div>
        <div class="detail-tag-row">
          <el-tag size="small" effect="plain">状态：{{ monitorInformationStatusLabel(currentInformation) }}</el-tag>
          <el-tag size="small" effect="plain" :type="clueRiskTagType(currentInformation.riskLevel)">
            风险：{{ clueRiskLabel(currentInformation.riskLevel) }}
          </el-tag>
          <el-tag size="small" effect="plain" :type="languageTagType(currentInformation.language)">
            语言：{{ languageLabel(currentInformation.language) }}
          </el-tag>
        </div>
        <div class="detail-section">
          <div class="detail-section-title">正文</div>
          <div class="information-detail-content">{{ informationDetailContent(currentInformation) }}</div>
        </div>
        <div class="detail-section compact">
          <div class="detail-section-title">命中信息</div>
          <div class="detail-meta-grid">
            <span>主体：{{ currentInformation.matchedSubjects || '-' }}</span>
            <span>关键词：{{ currentInformation.matchedKeywords || currentInformation.keywords || '-' }}</span>
            <span>风险词：{{ currentInformation.matchedNegativeWords || '-' }}</span>
            <span>互动：{{ informationInteractionLabel(currentInformation) }}</span>
            <span>主题：{{ topicLabel(currentInformation.topicCategory) }}</span>
            <span>相关性：{{ relevanceLabel(currentInformation) }}</span>
            <span>学校词：{{ currentInformation.matchedSchoolTerms || '-' }}</span>
            <span>分类依据：{{ currentInformation.topicReason || '-' }}</span>
          </div>
        </div>
        <div v-if="currentInformation.originalUrl" class="detail-original-url">
          <span>{{ safeOriginalUrl(currentInformation.originalUrl) || '原文链接不可用' }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="informationDetailVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!safeOriginalUrl(currentInformation?.originalUrl)" @click="openInformationOriginalUrl">查看原链接</el-button>
      </template>
    </el-dialog>

    <!-- ====== 任务重点目标 Dialog ====== -->
    <el-dialog
      v-model="watchTargetDialogVisible"
      :title="watchTargetForm.targetId ? '编辑重点监控目标' : '新增重点监控目标'"
      width="680px"
      destroy-on-close
      @closed="resetWatchTargetForm"
    >
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="任务ID" required>
            <el-input-number v-model="watchTargetForm.monitorTaskId" :min="1" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="类型" required>
            <el-select v-model="watchTargetForm.targetType" style="width: 100%">
              <el-option label="重点账号" value="account" />
              <el-option label="指定链接" value="link" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="watchTargetForm.targetStatus" style="width: 100%">
              <el-option label="启用" value="active" />
              <el-option label="暂停" value="paused" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="平台">
            <el-input v-model.trim="watchTargetForm.platform" />
          </el-form-item>
          <el-form-item label="账号名称">
            <el-input v-model.trim="watchTargetForm.accountName" :disabled="watchTargetForm.targetType === 'link'" />
          </el-form-item>
          <el-form-item label="账号UID">
            <el-input v-model.trim="watchTargetForm.accountUid" :disabled="watchTargetForm.targetType === 'link'" />
          </el-form-item>
        </div>
        <el-form-item label="链接/主页">
          <el-input v-model.trim="watchTargetForm.linkUrl" />
        </el-form-item>
        <el-form-item label="目标内补充关键词">
          <el-input v-model.trim="watchTargetForm.keywordScope" placeholder="多个关键词用逗号或空格分隔" />
        </el-form-item>
        <el-form-item label="授权/来源说明" required>
          <el-input v-model.trim="watchTargetForm.authorizationScope" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model.trim="watchTargetForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="watchTargetDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="watchTargetSaving" :disabled="!canMonitorOperate" @click="submitWatchTarget">保存</el-button>
      </template>
    </el-dialog>

    <!-- ====== 批量操作 Dialog ====== -->
    <el-dialog v-model="batchOpVisible" title="批量操作" width="520px" destroy-on-close @closed="resetBatchForm">
      <p style="margin-bottom: 12px; color: #606266;">
        已选择 <strong>{{ selectedBatchTotal }}</strong> 条，其中监测命中 <strong>{{ selectedMonitorResultCount }}</strong> 条，已转线索 <strong>{{ selectedClueCount }}</strong> 条
      </p>
      <el-radio-group v-model="batchAction" style="display: flex; flex-direction: column; gap: 12px;">
        <el-radio value="convert" :disabled="selectedMonitorResultCount === 0">批量转线索 — 处理 {{ selectedMonitorResultCount }} 条监测命中</el-radio>
        <el-radio value="alert" :disabled="selectedMonitorResultCount === 0">批量转预警 — 处理 {{ selectedMonitorResultCount }} 条监测命中</el-radio>
        <el-radio value="ignore" :disabled="selectedMonitorResultCount === 0">批量忽略 — 处理 {{ selectedMonitorResultCount }} 条监测命中</el-radio>
        <el-radio value="sentiment" :disabled="selectedMonitorResultCount === 0">批量修改情感 — 处理 {{ selectedMonitorResultCount }} 条监测信息</el-radio>
        <el-radio value="judge" :disabled="selectedClueCount === 0">批量研判 — 处理 {{ selectedClueCount }} 条已转线索</el-radio>
        <el-radio value="joinEvent" :disabled="selectedClueCount === 0">批量加入事件 — 处理 {{ selectedClueCount }} 条已转线索</el-radio>
      </el-radio-group>
      <div v-if="batchAction === 'sentiment'" style="margin-top: 16px;">
        <el-form label-position="top">
          <el-form-item label="目标情感" required>
            <el-select v-model="batchSentiment" placeholder="请选择情感" style="width: 100%">
              <el-option
                v-for="option in sentimentEditOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div v-if="batchAction === 'judge'" style="margin-top: 16px;">
        <el-form label-position="top">
          <el-form-item label="风险级别" required>
            <el-select v-model="batchJudgeForm.riskLevel" placeholder="请选择风险级别" style="width: 100%">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="研判意见">
            <el-input v-model="batchJudgeForm.judgeOpinion" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" placeholder="研判意见（可选）" />
          </el-form-item>
        </el-form>
      </div>
      <div v-if="batchAction === 'joinEvent'" style="margin-top: 16px;">
        <el-form label-position="top">
          <el-form-item label="目标事件" required>
            <el-select v-model="batchJoinEventId" filterable remote :remote-method="searchEvents" :loading="eventSearchLoading" placeholder="请输入事件名称搜索" style="width: 100%" clearable>
              <el-option v-for="ev in eventOptions" :key="ev.eventId" :label="ev.eventTitle" :value="ev.eventId" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="batchOpVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchExecuting" @click="executeBatchOp">执行</el-button>
      </template>
    </el-dialog>

    <!-- ====== 新增/编辑线索 Dialog ====== -->
    <el-dialog v-model="clueFormVisible" :title="clueForm.clueId ? '编辑线索' : '新增人工线索'" width="720px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="线索标题" required>
          <el-input v-model.trim="clueForm.clueTitle" />
        </el-form-item>
        <el-form-item label="线索内容">
          <el-input v-model.trim="clueForm.clueContent" type="textarea" :rows="4" />
        </el-form-item>
        <div class="form-grid" style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
          <el-form-item label="来源">
            <el-input v-model.trim="clueForm.clueSource" />
          </el-form-item>
          <el-form-item label="平台">
            <el-input v-model.trim="clueForm.sourcePlatform" />
          </el-form-item>
        </div>
        <div class="form-grid" style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
          <el-form-item label="原始链接">
            <el-input v-model.trim="clueForm.originalUrl" />
          </el-form-item>
          <el-form-item label="涉及账号">
            <el-input v-model.trim="clueForm.involvedAccount" />
          </el-form-item>
        </div>
        <div class="form-grid" style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
          <el-form-item label="关键词">
            <el-input v-model.trim="clueForm.keywords" />
          </el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="clueForm.riskLevel" style="width:100%">
              <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid" style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
          <el-form-item label="发布时间">
            <el-date-picker v-model="clueForm.publishTime" type="datetime" style="width:100%" />
          </el-form-item>
          <el-form-item label="发现时间">
            <el-date-picker v-model="clueForm.discoverTime" type="datetime" style="width:100%" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model.trim="clueForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clueFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingClue" @click="submitClueForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- ====== 研判 Dialog ====== -->
    <el-dialog v-model="judgeVisible" title="线索研判" width="520px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="风险等级">
          <el-select v-model="judgeForm.riskLevel" style="width:100%">
            <el-option v-for="risk in CAMPUS_RISK_OPTIONS" :key="risk.value" :label="risk.label" :value="risk.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="研判意见">
          <el-input v-model.trim="judgeForm.judgeOpinion" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="judgeVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingClue" @click="submitJudgeClue">保存</el-button>
      </template>
    </el-dialog>

    <!-- ====== 归档 Dialog ====== -->
    <el-dialog v-model="archiveVisible" title="线索归档" width="520px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="归档原因">
          <el-input v-model.trim="archiveReason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="archiveVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingClue" @click="submitArchiveClue">归档</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, Plus, RefreshCw, Settings2 } from 'lucide-vue-next';
import * as echarts from 'echarts';
import type { ECharts, EChartsOption } from 'echarts';
import EmotionBadge from '../components/EmotionBadge.vue';
import PlatformBadge from '../components/PlatformBadge.vue';
import { CAMPUS_RISK_OPTIONS, campusRiskLabel, campusRiskTagType, campusTopicLabel } from '../config/campusTaxonomy';
import {
  listMonitorInformation,
  fetchMonitorInformationPlatformCounts,
  fetchMonitorInformationSubPlatformCounts,
  listMonitorAlerts,
  listMonitorResults,
  alertMonitorResult,
  ignoreMonitorResult,
  updateMonitorResultSentiment,
  convertMonitorResultToClue,
  createMonitorWatchTargetFromResult,
  listMonitorWatchTargets,
  saveMonitorWatchTarget,
  deleteMonitorWatchTarget
} from '../services/monitor';
import { listIngestSources, listIngestTasks } from '../services/detectionIngest';
import { archiveClue, deleteClue, saveClue, judgeClue } from '../services/campusBusiness';
import { addEventClue, listEvents } from '../services/eventCenter';
import { getCurrentCampusUser } from '../services/permission';
import type {
  ApiId,
  CampusAlert,
  CampusClue,
  CampusEvent,
  CampusIngestSource,
  CampusIngestTask,
  ClueAdvancedQuery,
  CampusMonitorInformation,
  CampusMonitorResult,
  CampusMonitorWatchTarget
} from '../types/api';

const router = useRouter();
const route = useRoute();

// ========== 线索 CRUD 状态 ==========
const clueFormVisible = ref(false);
const savingClue = ref(false);
const judgeVisible = ref(false);
const archiveVisible = ref(false);
const currentClue = ref<CampusClue>();
const archiveReason = ref('');
const clueForm = reactive<CampusClue>({
  clueTitle: '',
  clueContent: '',
  clueSource: 'manual',
  sourcePlatform: '',
  originalUrl: '',
  publishTime: undefined,
  discoverTime: undefined,
  involvedAccount: '',
  keywords: '',
  riskLevel: 'normal',
  remark: ''
});
const judgeForm = reactive({
  riskLevel: 'normal',
  judgeOpinion: ''
});

// ========== 基础数据 ==========
const pageTab = ref('infoList');

type CountTab = {
  name: string;
  value: string;
  count: number;
  connected?: boolean;
  connectionLabel?: string;
};

type CountRow = {
  name?: string;
  value?: number | string;
};

type InfoColumn = {
  key: string;
  label: string;
  width?: number | string;
  minWidth?: number | string;
  align?: 'left' | 'center' | 'right';
  fixed?: true | 'left' | 'right';
  tooltip?: boolean;
  visible: boolean;
  required?: boolean;
};

// ========== 筛选面板 ==========
const filterExpanded = ref(true);
const collectTimePreset = ref('year');
const publishTimePreset = ref('');
const collectTimeRange = ref<[string, string] | null>(null);
const publishTimeRange = ref<[string, string] | null>(null);
const sentimentChecks = ref<string[]>(['all']);
const similarDedup = ref(false);
const searchKeyword = ref('');
const informationDetailVisible = ref(false);
const currentInformation = ref<CampusMonitorInformation | null>(null);

const query = reactive<ClueAdvancedQuery>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  sentiment: '',
  articleStatus: '',
  sourcePlatform: '',
  sourceSubPlatform: '',
  matchScope: '',
  similarDedup: false,
  sortBy: 'publishTime',
  collectTimeStart: '',
  collectTimeEnd: '',
  publishTimeStart: '',
  publishTimeEnd: '',
  hitScope: routeHitScope(),
  riskLevel: '',
  clueStatus: '',
  resultStatus: '',
  language: ''
});

// ========== 表格数据 ==========
const monitorInfos = ref<CampusMonitorInformation[]>([]);
const infoTotal = ref(0);
const loading = ref(false);
const selectedInfos = ref<CampusMonitorInformation[]>([]);
const selectedClues = ref<CampusClue[]>([]);
const selectedMonitorResults = computed(() => selectedInfos.value
  .filter((row) => row.monitorResultId)
  .map((row) => toMonitorResult(row)));
const selectedMonitorResultCount = computed(() => selectedMonitorResults.value.length);
const selectedClueCount = computed(() => selectedClues.value.length);
const selectedBatchTotal = computed(() => Math.max(selectedInfos.value.length, selectedClues.value.length));
const tableRef = ref();
const draggedColumnKey = ref('');

const infoColumns = ref<InfoColumn[]>([
  { key: 'index', label: '#', width: 55, align: 'center', visible: true, required: true },
  { key: 'sentiment', label: '情感', width: 80, align: 'center', visible: true },
  { key: 'title', label: '标题-摘要', minWidth: 380, tooltip: true, visible: true, required: true },
  { key: 'contentCapture', label: '正文', width: 94, align: 'center', visible: true },
  { key: 'publishTime', label: '发布时间', width: 150, align: 'center', visible: true },
  { key: 'platform', label: '来源', width: 110, align: 'center', visible: true },
  { key: 'author', label: '账号/作者', width: 130, tooltip: true, visible: true },
  { key: 'keywords', label: '匹配关键词', width: 140, tooltip: true, visible: true },
  { key: 'negativeWords', label: '负面词', width: 120, tooltip: true, visible: false },
  { key: 'interaction', label: '互动', width: 150, visible: true },
  { key: 'riskLevel', label: '风险等级', width: 90, align: 'center', visible: true },
  { key: 'topicCategory', label: '主题', width: 112, align: 'center', visible: true },
  { key: 'schoolRelevance', label: '相关性', width: 96, align: 'center', visible: true },
  { key: 'language', label: '语言', width: 76, align: 'center', visible: true },
  { key: 'status', label: '状态', width: 100, align: 'center', visible: true },
  { key: 'infoTime', label: '采集时间', width: 155, align: 'center', visible: true },
  { key: 'actions', label: '操作', width: 132, fixed: 'right', align: 'center', visible: true, required: true }
]);
const visibleInfoColumns = computed(() => infoColumns.value.filter((col) => col.visible));

// ========== 内容 Tabs ==========
const mediaTypes = ref<CountTab[]>([]);
const mediaTypeTab = ref('全部');
const subPlatforms = ref<CountTab[]>([]);
const subPlatformTab = ref('全部');
const showSubTabs = computed(() => isForumPlatform(mediaTypeTab.value));

type PlatformConnection = {
  sourceCount: number;
  activeTaskCount: number;
};

const platformConnections = ref<Record<string, PlatformConnection>>({});

// ========== 主题分析 ==========
const keywordChartRef = ref<HTMLElement | null>(null);
const mediaTypeChartRef = ref<HTMLElement | null>(null);
const sentimentChartRef = ref<HTMLElement | null>(null);
let keywordChart: ECharts | null = null;
let mediaTypeChart: ECharts | null = null;
let sentimentChart: ECharts | null = null;

const topKeywords = computed(() => {
  const keywordMap: Record<string, number> = {};
  for (const item of monitorInfos.value) {
    const kwStr = item.keywords || item.matchedKeywords || '';
    if (!kwStr) continue;
    const kws = kwStr.split(/[,，\s]+/).filter(Boolean);
    for (const kw of kws) {
      keywordMap[kw] = (keywordMap[kw] || 0) + 1;
    }
  }
  return Object.entries(keywordMap)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 15);
});

const sentimentDist = computed(() => {
  const dist: Record<string, number> = {};
  for (const item of monitorInfos.value) {
    const key = item.sentiment || '未知';
    dist[key] = (dist[key] || 0) + 1;
  }
  return Object.entries(dist).map(([name, value]) => ({ name, value }));
});

const mediaDist = computed(() => {
  const dist: Record<string, number> = {};
  for (const item of monitorInfos.value) {
    const key = platformDisplayName(item.platform || item.sourcePlatform || '未知');
    dist[key] = (dist[key] || 0) + 1;
  }
  return Object.entries(dist).map(([name, value]) => ({ name, value }));
});

function renderTopicAnalysisCharts() {
  nextTick(() => {
    renderKeywordBarChart();
    renderMediaTypePieChart();
    renderSentimentPieChart();
  });
}

function renderKeywordBarChart() {
  if (!keywordChartRef.value) return;
  if (!keywordChart) {
    keywordChart = echarts.init(keywordChartRef.value);
  }
  const data = topKeywords.value;
  if (data.length === 0) {
    keywordChart.clear();
    return;
  }
  const names = data.map((d) => d.name);
  const values = data.map((d) => d.value);
  const option: EChartsOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 12, right: 20, top: 12, bottom: 12, containLabel: true },
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf2f7' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: names.reverse(),
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: '#d8e0ea' } }
    },
    series: [
      {
        type: 'bar',
        barWidth: 16,
        data: values.reverse(),
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#3D5AFE' },
            { offset: 1, color: '#818CF8' }
          ])
        }
      }
    ]
  };
  keywordChart.setOption(option, true);
}

function renderMediaTypePieChart() {
  if (!mediaTypeChartRef.value) return;
  if (!mediaTypeChart) {
    mediaTypeChart = echarts.init(mediaTypeChartRef.value);
  }
  const data = mediaDist.value.map((d) => ({ name: d.name, value: d.value }));
  if (data.length === 0) {
    mediaTypeChart.clear();
    return;
  }
  const option: EChartsOption = {
    color: ['#3D5AFE', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#06B6D4', '#84CC16'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 11 } },
    series: [
      {
        type: 'pie',
        radius: ['48%', '76%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, position: 'outside', formatter: '{b}\n{d}%', fontSize: 11, color: '#64748b' },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data
      }
    ]
  };
  mediaTypeChart.setOption(option, true);
}

function renderSentimentPieChart() {
  if (!sentimentChartRef.value) return;
  if (!sentimentChart) {
    sentimentChart = echarts.init(sentimentChartRef.value);
  }
  const data = sentimentDist.value.map((d) => ({ name: d.name, value: d.value }));
  if (data.length === 0) {
    sentimentChart.clear();
    return;
  }
  const option: EChartsOption = {
    color: ['#10B981', '#F59E0B', '#EF4444', '#6B7280', '#3D5AFE', '#8B5CF6'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 11 } },
    series: [
      {
        type: 'pie',
        radius: ['48%', '76%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, position: 'outside', formatter: '{b}\n{d}%', fontSize: 11, color: '#64748b' },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data
      }
    ]
  };
  sentimentChart.setOption(option, true);
}

function disposeTopicAnalysisCharts() {
  keywordChart?.dispose();
  keywordChart = null;
  mediaTypeChart?.dispose();
  mediaTypeChart = null;
  sentimentChart?.dispose();
  sentimentChart = null;
}

// ========== 主题预警 ==========
const alertList = ref<CampusAlert[]>([]);
const alertTotal = ref(0);
const alertLoading = ref(false);
const alertRiskFilter = ref('');
const alertQuery = reactive({
  pageNum: 1,
  pageSize: 20
});

function riskLevelTagType(level?: string) {
  return campusRiskTagType(level);
}

function alertStatusTagType(status?: string): 'warning' | 'success' | 'info' | '' {
  if (status === 'pending' || status === '待处理') return 'warning';
  if (status === 'handled' || status === '已处理') return 'success';
  if (status === 'ignored' || status === '已忽略') return 'info';
  return '';
}

function alertStatusLabel(status?: string): string {
  const labels: Record<string, string> = {
    pending: '待处理',
    handled: '已处理',
    ignored: '已忽略'
  };
  return labels[status || ''] || status || '-';
}

async function loadAlertData() {
  alertLoading.value = true;
  try {
    const params: Record<string, unknown> = {
      pageNum: alertQuery.pageNum,
      pageSize: alertQuery.pageSize
    };
    if (alertRiskFilter.value) {
      params.riskLevel = alertRiskFilter.value;
    }
    const result = await listMonitorAlerts(params as any);
    alertList.value = result.list || [];
    alertTotal.value = result.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '预警数据加载失败');
    alertList.value = [];
    alertTotal.value = 0;
  } finally {
    alertLoading.value = false;
  }
}

function viewAlertDetail(row: CampusAlert) {
  ElMessage.info(`预警详情: ${row.alertTitle}`);
}

function handleAlertItem(row: CampusAlert) {
  ElMessage.info(`处理预警: ${row.alertTitle}`);
}

// ========== 批量操作 ==========
const batchOpVisible = ref(false);
const batchAction = ref('convert');
const batchExecuting = ref(false);
const batchJudgeForm = reactive({
  riskLevel: 'concern',
  judgeOpinion: ''
});
const batchSentiment = ref('negative');
const batchJoinEventId = ref<ApiId | null>(null);
const eventOptions = ref<CampusEvent[]>([]);
const eventSearchLoading = ref(false);

function resetBatchForm() {
  batchAction.value = selectedMonitorResultCount.value > 0 ? 'convert' : 'judge';
  batchJudgeForm.riskLevel = 'concern';
  batchJudgeForm.judgeOpinion = '';
  batchSentiment.value = 'negative';
  batchJoinEventId.value = null;
  eventOptions.value = [];
}

async function searchEvents(query: string) {
  if (!query) {
    eventOptions.value = [];
    return;
  }
  eventSearchLoading.value = true;
  try {
    const result = await listEvents({ pageNum: 1, pageSize: 20, keyword: query });
    eventOptions.value = result.list || [];
  } catch {
    eventOptions.value = [];
  } finally {
    eventSearchLoading.value = false;
  }
}

async function executeBatchOp() {
  if (selectedBatchTotal.value === 0) {
    ElMessage.warning('请先选择数据');
    return;
  }
  if (['convert', 'alert', 'ignore'].includes(batchAction.value)) {
    const targets = selectedMonitorResults.value;
    if (targets.length === 0) {
      ElMessage.warning('当前操作没有可处理的监测命中');
      return;
    }
    batchExecuting.value = true;
    let successCount = 0;
    let failCount = 0;
    let skipCount = selectedInfos.value.length - targets.length;
    for (const result of targets) {
      try {
        if (batchAction.value === 'convert') {
          if (result.clueId) {
            skipCount++;
            continue;
          }
          await convertMonitorResultToClue(result.monitorResultId!);
        } else if (batchAction.value === 'alert') {
          if (result.resultStatus === 'alerted') {
            skipCount++;
            continue;
          }
          await alertMonitorResult(result.monitorResultId!);
        } else {
          if (result.resultStatus === 'ignored') {
            skipCount++;
            continue;
          }
          await ignoreMonitorResult(result.monitorResultId!);
        }
        successCount++;
      } catch {
        failCount++;
      }
    }
    batchExecuting.value = false;
    showBatchResult('批量操作完成', successCount, failCount, skipCount);
    batchOpVisible.value = false;
    loadData();
  } else if (batchAction.value === 'sentiment') {
    if (!canMonitorOperate.value) {
      ElMessage.warning('当前账号没有监测操作权限');
      return;
    }
    const targets = selectedInfos.value.filter((row) => row.monitorResultId);
    if (targets.length === 0) {
      ElMessage.warning('当前操作没有可处理的监测信息');
      return;
    }
    if (!batchSentiment.value) {
      ElMessage.warning('请选择目标情感');
      return;
    }
    const normalized = normalizeSentimentValue(batchSentiment.value);
    batchExecuting.value = true;
    let successCount = 0;
    let failCount = 0;
    let skipCount = selectedInfos.value.length - targets.length;
    for (const row of targets) {
      if (isArchivedLinkedClue(row) || normalizeSentimentValue(row.sentiment) === normalized) {
        skipCount++;
        continue;
      }
      try {
        await updateMonitorResultSentiment(row.monitorResultId!, normalized);
        successCount++;
      } catch {
        failCount++;
      }
    }
    batchExecuting.value = false;
    showBatchResult(`批量修改情感为${sentimentOptionLabel(normalized)}完成`, successCount, failCount, skipCount);
    batchOpVisible.value = false;
    await Promise.all([loadMonitorResults(), refreshInfoList()]);
  } else if (batchAction.value === 'judge') {
    if (selectedClues.value.length === 0) {
      ElMessage.warning('当前操作没有可处理的已转线索');
      return;
    }
    if (!batchJudgeForm.riskLevel) {
      ElMessage.warning('请选择风险级别');
      return;
    }
    batchExecuting.value = true;
    let successCount = 0;
    let failCount = 0;
    for (const clue of selectedClues.value) {
      try {
        await judgeClue(clue.clueId!, batchJudgeForm.riskLevel, batchJudgeForm.judgeOpinion || undefined);
        successCount++;
      } catch {
        failCount++;
      }
    }
    batchExecuting.value = false;
    showBatchResult('批量研判完成', successCount, failCount, selectedBatchTotal.value - selectedClues.value.length);
    batchOpVisible.value = false;
    loadData();
  } else if (batchAction.value === 'joinEvent') {
    if (selectedClues.value.length === 0) {
      ElMessage.warning('当前操作没有可处理的已转线索');
      return;
    }
    if (!batchJoinEventId.value) {
      ElMessage.warning('请选择目标事件');
      return;
    }
    batchExecuting.value = true;
    let successCount = 0;
    let failCount = 0;
    for (const clue of selectedClues.value) {
      try {
        await addEventClue(batchJoinEventId.value, clue.clueId!);
        successCount++;
      } catch {
        failCount++;
      }
    }
    batchExecuting.value = false;
    showBatchResult('加入事件完成', successCount, failCount, selectedBatchTotal.value - selectedClues.value.length);
    batchOpVisible.value = false;
    loadData();
  }
}

function showBatchResult(title: string, successCount: number, failCount: number, skipCount: number) {
  const message = `${title}，成功 ${successCount} 条，失败 ${failCount} 条，跳过 ${skipCount} 条`;
  if (failCount > 0) {
    ElMessage.warning(message);
  } else {
    ElMessage.success(message);
  }
}

const monitorResults = ref<CampusMonitorResult[]>([]);
const resultLoading = ref(false);
const resultTotal = ref(0);
const resultConvertedFilter = ref('');
const resultQuery = reactive({
  pageNum: 1,
  pageSize: 20,
  monitorTaskId: undefined as ApiId | undefined,
  keyword: '',
  riskLevel: '',
  resultStatus: '',
  platform: '',
  language: ''
});

const permissionReady = ref(false);
const permissionCodes = ref<Set<string>>(new Set());
const canMonitorOperate = computed(() => hasPermission([
  'role:campus_admin',
  'campus:api:all',
  'campus:business:operate',
  'campus:monitor:operate'
]));

const watchTargets = ref<CampusMonitorWatchTarget[]>([]);
const watchTargetLoading = ref(false);
const watchTargetSaving = ref(false);
const watchTargetDialogVisible = ref(false);
const watchTargetTotal = ref(0);
const watchTargetQuery = reactive({
  pageNum: 1,
  pageSize: 5,
  monitorTaskId: undefined as ApiId | undefined,
  targetType: '',
  targetStatus: ''
});
const watchTargetForm = reactive<CampusMonitorWatchTarget>({
  monitorTaskId: undefined,
  targetType: 'account',
  platform: '',
  accountName: '',
  accountUid: '',
  linkUrl: '',
  sourceObjectType: 'manual',
  authorizationScope: '',
  keywordScope: '',
  targetStatus: 'active',
  remark: ''
});

// ========== 情感快捷标签 ==========
const sentimentTags = [
  { label: '负面', value: 'negative', color: '#f56c6c' },
  { label: '中性', value: 'neutral', color: '#e6a23c' },
  { label: '正面', value: 'positive', color: '#67c23a' },
  { label: '未知', value: 'none', color: '#909399' }
];
const sentimentEditOptions = [
  { label: '负面', value: 'negative' },
  { label: '中性', value: 'neutral' },
  { label: '正面', value: 'positive' },
  { label: '未识别', value: 'none' }
];
const sentimentUpdatingIds = ref<Set<string>>(new Set());

function normalizeSentimentValue(value?: string | null): string {
  const raw = String(value || '').trim();
  if (!raw) return 'none';
  const lower = raw.toLowerCase();
  if (lower.includes('negative') || lower.includes('neg') || raw.includes('负')) return 'negative';
  if (lower.includes('positive') || lower.includes('pos') || raw.includes('正')) return 'positive';
  if (lower.includes('neutral') || raw.includes('中')) return 'neutral';
  return 'none';
}

function sentimentOptionLabel(value?: string | null): string {
  const normalized = normalizeSentimentValue(value);
  return sentimentEditOptions.find((item) => item.value === normalized)?.label || '未识别';
}

function isArchivedLinkedClue(row: CampusMonitorInformation): boolean {
  return Boolean(row.clueId && row.clueStatus === 'archived');
}

function sentimentEditDisabledReason(row: CampusMonitorInformation): string {
  if (!row.monitorResultId) return '当前记录缺少监测结果ID';
  if (!canMonitorOperate.value) return '当前账号没有监测操作权限';
  if (isArchivedLinkedClue(row)) return '已归档线索不能修改情感';
  return '';
}

function canEditMonitorSentiment(row: CampusMonitorInformation): boolean {
  return !sentimentEditDisabledReason(row) && !isSentimentUpdating(row);
}

function isSentimentUpdating(row: CampusMonitorInformation): boolean {
  return row.monitorResultId ? sentimentUpdatingIds.value.has(String(row.monitorResultId)) : false;
}

function setSentimentUpdating(monitorResultId: ApiId, updating: boolean) {
  const next = new Set(sentimentUpdatingIds.value);
  const key = String(monitorResultId);
  if (updating) {
    next.add(key);
  } else {
    next.delete(key);
  }
  sentimentUpdatingIds.value = next;
}

function onMonitorSentimentCommand(row: CampusMonitorInformation, value: string | number | boolean) {
  updateMonitorSentiment(row, String(value));
}

// ========== 线索 CRUD 函数 ==========
function resetClueForm() {
  Object.assign(clueForm, {
    clueId: undefined,
    clueTitle: '',
    clueContent: '',
    clueSource: 'manual',
    sourcePlatform: '',
    originalUrl: '',
    publishTime: undefined,
    discoverTime: new Date(),
    involvedAccount: '',
    keywords: '',
    riskLevel: 'normal',
    remark: ''
  });
}

function openCreateClue() {
  resetClueForm();
  clueFormVisible.value = true;
}

function openEditClue(row: CampusClue) {
  Object.assign(clueForm, row);
  clueFormVisible.value = true;
}

async function submitClueForm() {
  if (!clueForm.clueTitle) {
    ElMessage.warning('线索标题不能为空');
    return;
  }
  savingClue.value = true;
  try {
    const creating = !clueForm.clueId;
    await saveClue({ ...clueForm });
    ElMessage.success(creating ? '人工线索已新增' : '保存成功');
    clueFormVisible.value = false;
    loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    savingClue.value = false;
  }
}

function openJudgeClue(row: CampusClue) {
  currentClue.value = row;
  judgeForm.riskLevel = row.riskLevel || 'normal';
  judgeForm.judgeOpinion = row.judgeOpinion || '';
  judgeVisible.value = true;
}

async function submitJudgeClue() {
  if (!currentClue.value?.clueId) return;
  savingClue.value = true;
  try {
    await judgeClue(currentClue.value.clueId, judgeForm.riskLevel, judgeForm.judgeOpinion);
    ElMessage.success('研判已保存');
    judgeVisible.value = false;
    loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '研判失败');
  } finally {
    savingClue.value = false;
  }
}

function openArchiveClue(row: CampusClue) {
  currentClue.value = row;
  archiveReason.value = '';
  archiveVisible.value = true;
}

async function submitArchiveClue() {
  if (!currentClue.value?.clueId) return;
  savingClue.value = true;
  try {
    await archiveClue(currentClue.value.clueId, archiveReason.value);
    ElMessage.success('已归档');
    archiveVisible.value = false;
    loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '归档失败');
  } finally {
    savingClue.value = false;
  }
}

async function handleDeleteClue(row: CampusClue) {
  if (!row.clueId) return;
  try {
    await ElMessageBox.confirm(
      `确认删除线索「${row.clueTitle}」吗？删除后不可恢复。`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    );
  } catch {
    return;
  }
  try {
    await deleteClue(row.clueId);
    ElMessage.success('已删除');
    loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败');
  }
}

// ========== 标签映射 ==========
function clueRiskLabel(value?: string) {
  return campusRiskLabel(value);
}

function clueRiskTagType(value?: string) {
  return campusRiskTagType(value);
}

function topicLabel(value?: string | null) {
  return campusTopicLabel(value);
}

function relevanceLabel(row?: Pick<CampusMonitorInformation, 'schoolRelevanceScore' | 'schoolRelevanceReason'> | null) {
  if (!row || row.schoolRelevanceScore === undefined || row.schoolRelevanceScore === null) {
    return '-';
  }
  return row.schoolRelevanceReason ? `${row.schoolRelevanceScore} / ${row.schoolRelevanceReason}` : String(row.schoolRelevanceScore);
}

function clueStatusLabel(value?: string) {
  const labels: Record<string, string> = {
    pending_judge: '待研判', judged: '已研判', archived: '已归档', converted: '已转事件'
  };
  return labels[value || 'pending_judge'] || value || '待研判';
}

function monitorResultStatusLabel(value?: string, clueId?: ApiId) {
  if (clueId) {
    return '已转线索';
  }
  const labels: Record<string, string> = {
    pending: '待处理',
    alerted: '已预警',
    ignored: '已忽略',
    handled: '已处理',
    converted: '已转线索'
  };
  return labels[value || 'pending'] || value || '待处理';
}

function interactionLabel(row: CampusMonitorResult) {
  const items = [
    ['赞', row.likeCount],
    ['评', row.commentCount],
    ['转', row.shareCount],
    ['藏', row.collectCount],
    ['看', row.viewCount]
  ].filter(([, value]) => value !== undefined && value !== null);
  return items.length ? items.map(([label, value]) => `${label}${value}`).join(' / ') : '未采集';
}

function informationInteractionLabel(row: CampusMonitorInformation) {
  return interactionLabel(toMonitorResult(row));
}

function languageLabel(value?: string) {
  const labels: Record<string, string> = { zh: '中文', mongolian: '蒙语', uyghur: '维语' };
  return labels[value || ''] || '未知';
}

function languageTagType(value?: string) {
  if (value === 'zh') return '';
  if (value === 'mongolian') return 'success';
  if (value === 'uyghur') return 'warning';
  return 'info';
}

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

// ========== 工具函数 ==========
function formatDateStr(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function initCurrentYear() {
  const now = new Date();
  const start = new Date(now.getFullYear(), 0, 1);
  query.collectTimeStart = formatDateStr(start);
  query.collectTimeEnd = formatDateStr(now);
}

function routeHitScope(): 'risk' | 'all' {
  const hitScope = Array.isArray(route.query.hitScope) ? route.query.hitScope[0] : route.query.hitScope;
  return hitScope === 'risk' ? 'risk' : 'all';
}

function collectAllTabLabel(): string {
  return '全部';
}

const preferredPlatformTabs: Array<{ name: string; value: string }> = [
  { name: '抖音', value: 'douyin' },
  { name: '小红书', value: 'xiaohongshu' },
  { name: '知乎', value: 'zhihu' },
  { name: '新闻/网页', value: 'news' },
  { name: '微博', value: 'weibo' },
  { name: '微信公众号', value: 'wechat_official' },
  { name: 'B站', value: 'bilibili' },
  { name: '快手', value: 'kuaishou' }
];

const forumSubPlatformTabs: Array<{ name: string; value: string }> = [
  { name: '贴吧', value: 'tieba' },
  { name: '豆瓣', value: 'douban' },
  { name: '本地论坛', value: 'local_forum' },
  { name: '其它论坛', value: 'forum_other' }
];

function canonicalPlatformValue(rawName: string): string {
  const normalized = (rawName || '').trim().toLowerCase();
  const map: Record<string, string> = {
    all: '全部',
    '全部': '全部',
    douyin: 'douyin',
    douyin_search_video_v2: 'douyin',
    '抖音': 'douyin',
    '抖音短视频': 'douyin',
    xiaohongshu: 'xiaohongshu',
    xiaohongshu_search_notes: 'xiaohongshu',
    red: 'xiaohongshu',
    '小红书': 'xiaohongshu',
    zhihu: 'zhihu',
    '知乎': 'zhihu',
    kuaishou: 'kuaishou',
    '快手': 'kuaishou',
    news: 'news',
    news_media: 'news',
    public_web: 'news',
    '公开网页': 'news',
    '新闻媒体': 'news',
    '百度': 'news',
    '人民网': 'news',
    '新华网': 'news',
    '腾讯网': 'news',
    '搜狐': 'news',
    '新浪网': 'news',
    '今日头条': 'news',
    '头条': 'news',
    '百家号': 'news',
    school_website: 'news',
    college_website: 'news',
    '学校官网': 'news',
    '学院网站': 'news',
    weibo: 'weibo',
    weibo_search_all: 'weibo',
    '微博': 'weibo',
    '新浪微博': 'weibo',
    wechat: 'wechat_official',
    wechat_official: 'wechat_official',
    '微信': 'wechat_official',
    '微信公众号': 'wechat_official',
    video_account: 'video_account',
    '视频号': 'video_account',
    bilibili: 'bilibili',
    'b站': 'bilibili',
    forum: 'news',
    '公开论坛': 'news',
    '论坛': 'news',
    tieba: 'news',
    '贴吧': 'news',
    '百度贴吧': 'news',
    douban: 'news',
    '豆瓣': 'news',
    manual: 'manual',
    '人工录入': 'manual',
    upper_transfer: 'upper_transfer',
    '上级移交': 'upper_transfer',
    api: 'api',
    '接口接入': 'api',
    other: 'other',
    unknown: 'other',
    '未知': 'other',
    '其它': 'other',
    '其他': 'other'
  };
  return map[normalized] || rawName || 'other';
}

function canonicalSubPlatformValue(rawName: string): string {
  const normalized = (rawName || '').trim().toLowerCase();
  const map: Record<string, string> = {
    tieba: 'tieba',
    '贴吧': 'tieba',
    '百度贴吧': 'tieba',
    douban: 'douban',
    '豆瓣': 'douban',
    local_forum: 'local_forum',
    '本地论坛': 'local_forum',
    forum_other: 'forum_other',
    forum: 'forum_other',
    '论坛': 'forum_other',
    '公开论坛': 'forum_other',
    other: 'forum_other',
    '其它': 'forum_other',
    '其他': 'forum_other'
  };
  return map[normalized] || 'forum_other';
}

function subPlatformDisplayName(rawName: string): string {
  const normalized = canonicalSubPlatformValue(rawName);
  const map: Record<string, string> = {
    tieba: '贴吧',
    douban: '豆瓣',
    local_forum: '本地论坛',
    forum_other: '其它论坛'
  };
  return map[normalized] || rawName || '其它论坛';
}

function platformDisplayName(rawName: string): string {
  const normalized = canonicalPlatformValue(rawName);
  const map: Record<string, string> = {
    all: '全部',
    '全部': '全部',
    douyin: '抖音',
    xiaohongshu: '小红书',
    zhihu: '知乎',
    news: '新闻/网页',
    news_media: '新闻/网页',
    public_web: '新闻/网页',
    school_website: '学校官网',
    college_website: '学院网站',
    weibo: '微博',
    wechat: '微信',
    wechat_official: '微信公众号',
    video_account: '视频号',
    bilibili: 'B站',
    kuaishou: '快手',
    forum: '新闻/网页',
    tieba: '新闻/网页',
    manual: '人工录入',
    upper_transfer: '上级移交',
    api: '接口接入',
    other: '其它',
    unknown: '其它'
  };
  return map[normalized] || rawName || '其它';
}

function isForumPlatform(value: string): boolean {
  return canonicalPlatformValue(value) === 'forum';
}

function toCount(value: number | string | undefined): number {
  const n = Number(value || 0);
  return Number.isFinite(n) ? n : 0;
}

function platformConnection(value: string): PlatformConnection | undefined {
  return platformConnections.value[canonicalPlatformValue(value)];
}

function platformConnectionLabel(value: string, count: number): string {
  if (value === '全部' || count > 0) {
    return '';
  }
  const connection = platformConnection(value);
  if (connection?.activeTaskCount) {
    return '';
  }
  if (connection?.sourceCount) {
    return '未启用';
  }
  return '未接入';
}

function normalizeCountTabs(rows: CountRow[] | undefined, allLabel: string): CountTab[] {
  const list = Array.isArray(rows) ? rows : [];
  let explicitTotal = 0;
  const counts = new Map<string, number>();
  const labels = new Map<string, string>();

  for (const row of list) {
    const rawName = String(row.name || '其它').trim() || '其它';
    const count = toCount(row.value);
    const value = canonicalPlatformValue(rawName);
    if (value === '全部') {
      explicitTotal = count;
      continue;
    }
    counts.set(value, (counts.get(value) || 0) + count);
    labels.set(value, platformDisplayName(rawName));
  }

  const fixedValues = new Set(preferredPlatformTabs.map((item) => item.value));
  const fixedTabs = preferredPlatformTabs.map((item) => {
    const count = counts.get(item.value) || 0;
    return {
      name: item.name,
      value: item.value,
      count,
      connected: !!platformConnection(item.value),
      connectionLabel: platformConnectionLabel(item.value, count)
    };
  });
  const dynamicTabs = Array.from(counts.entries())
    .filter(([value]) => !fixedValues.has(value))
    .map(([value, count]) => ({
      name: labels.get(value) || platformDisplayName(value),
      value,
      count
    }));
  const total = explicitTotal || Array.from(counts.values()).reduce((sum, count) => sum + count, 0);
  return [
    { name: allLabel, value: '全部', count: total },
    ...fixedTabs,
    ...dynamicTabs
  ];
}

function normalizeForumSubTabs(rows: CountRow[] | undefined, allLabel: string): CountTab[] {
  const list = Array.isArray(rows) ? rows : [];
  const counts = new Map<string, number>();
  const labels = new Map<string, string>();
  for (const row of list) {
    const rawName = String(row.name || '其它论坛').trim() || '其它论坛';
    const value = canonicalSubPlatformValue(rawName);
    const count = toCount(row.value);
    counts.set(value, (counts.get(value) || 0) + count);
    labels.set(value, subPlatformDisplayName(rawName));
  }
  const fixedValues = new Set(forumSubPlatformTabs.map((item) => item.value));
  const fixedTabs = forumSubPlatformTabs.map((item) => ({
    name: item.name,
    value: item.value,
    count: counts.get(item.value) || 0
  }));
  const dynamicTabs = Array.from(counts.entries())
    .filter(([value]) => !fixedValues.has(value))
    .map(([value, count]) => ({
      name: labels.get(value) || subPlatformDisplayName(value),
      value,
      count
    }));
  const total = Array.from(counts.values()).reduce((sum, count) => sum + count, 0);
  return [
    { name: allLabel, value: '全部', count: total },
    ...fixedTabs,
    ...dynamicTabs
  ];
}

function rowIndex(idx: number): number {
  return ((query.pageNum || 1) - 1) * (query.pageSize || 20) + idx + 1;
}

function formatTime(val?: string | Date): string {
  if (!val) return '-';
  const d = new Date(val);
  if (isNaN(d.getTime())) return String(val);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function publishTimeLabel(row: CampusMonitorInformation): string {
  return row.publishTime ? formatTime(row.publishTime) : '发布时间未知';
}

function contentCaptureLabel(row: CampusMonitorInformation): string {
  if (row.contentCaptureLabel) {
    return row.contentCaptureLabel;
  }
  const status = row.contentCaptureStatus || '';
  if (status === 'full') return '完整正文';
  if (status === 'partial') return '摘要/标题';
  return '未采集';
}

function contentCaptureTagType(status?: string) {
  if (status === 'full') return 'success';
  if (status === 'partial') return 'warning';
  return 'info';
}

function escapeHtml(str: string): string {
  const el = document.createElement('span');
  el.textContent = str;
  return el.innerHTML;
}

function highlightTitle(title: string): string {
  if (!title) return '';
  const kw = searchKeyword.value || query.keyword || '';
  if (!kw) return escapeHtml(title);
  const escaped = escapeHtml(title);
  const keywords = kw.split(/[\s,，]+/).filter(Boolean);
  let result = escaped;
  for (const k of keywords) {
    const escapedKw = escapeHtml(k);
    if (escapedKw) {
      result = result.replace(
        new RegExp(escapedKw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi'),
        `<mark>$&</mark>`
      );
    }
  }
  return result;
}

// ========== 筛选事件 ==========
function onCollectTimePresetChange(val: string) {
  const now = new Date();
  if (val === 'today') {
    query.collectTimeStart = formatDateStr(now);
    query.collectTimeEnd = formatDateStr(now);
  } else if (val === 'week') {
    const day = now.getDay() || 7;
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - day + 1);
    query.collectTimeStart = formatDateStr(start);
    query.collectTimeEnd = formatDateStr(now);
  } else if (val === 'month') {
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    query.collectTimeStart = formatDateStr(start);
    query.collectTimeEnd = formatDateStr(now);
  } else if (val === 'year') {
    const start = new Date(now.getFullYear(), 0, 1);
    query.collectTimeStart = formatDateStr(start);
    query.collectTimeEnd = formatDateStr(now);
  }
  // custom: handled by date-picker callback
  handleFilterChange();
}

function onCollectTimeCustomChange(val: [string, string] | null) {
  if (val && val.length === 2) {
    query.collectTimeStart = val[0];
    query.collectTimeEnd = val[1];
  } else {
    query.collectTimeStart = '';
    query.collectTimeEnd = '';
  }
  handleFilterChange();
}

function onPublishTimePresetChange(val: string) {
  const now = new Date();
  if (val === 'today') {
    query.publishTimeStart = formatDateStr(now);
    query.publishTimeEnd = formatDateStr(now);
  } else if (val === 'week') {
    const day = now.getDay() || 7;
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - day + 1);
    query.publishTimeStart = formatDateStr(start);
    query.publishTimeEnd = formatDateStr(now);
  } else if (val === 'month') {
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    query.publishTimeStart = formatDateStr(start);
    query.publishTimeEnd = formatDateStr(now);
  } else {
    query.publishTimeStart = '';
    query.publishTimeEnd = '';
  }
  handleFilterChange();
}

function onPublishTimeCustomChange(val: [string, string] | null) {
  if (val && val.length === 2) {
    query.publishTimeStart = val[0];
    query.publishTimeEnd = val[1];
  } else {
    query.publishTimeStart = '';
    query.publishTimeEnd = '';
  }
  handleFilterChange();
}

function onSentimentChange(values: string[]) {
  if (values.includes('all')) {
    if (values.length > 1) {
      sentimentChecks.value = values.filter((v) => v !== 'all');
      query.sentiment = sentimentChecks.value.join(',');
    } else {
      query.sentiment = '';
    }
  } else if (values.length === 0) {
    sentimentChecks.value = ['all'];
    query.sentiment = '';
  } else {
    query.sentiment = values.join(',');
  }
  handleFilterChange();
}

function resetInformationNarrowFilters() {
  mediaTypeTab.value = '全部';
  subPlatformTab.value = '全部';
  subPlatforms.value = [];
  query.sourcePlatform = '';
  query.sourceSubPlatform = '';
  query.sentiment = '';
  sentimentChecks.value = ['all'];
  query.resultStatus = '';
  query.riskLevel = '';
  query.clueStatus = '';
  query.language = '';
  query.keyword = '';
  searchKeyword.value = '';
  query.sortBy = 'publishTime';
  similarDedup.value = false;
  query.similarDedup = false;
  query.matchScope = '';
  publishTimePreset.value = '';
  publishTimeRange.value = null;
  query.publishTimeStart = '';
  query.publishTimeEnd = '';
  collectTimePreset.value = 'year';
  collectTimeRange.value = null;
  initCurrentYear();
}

function onHitScopeChange() {
  resetInformationNarrowFilters();
  handleFilterChange();
}

function onSimilarDedupChange(val: boolean) {
  query.similarDedup = val;
  if (!val) {
    query.matchScope = '';
  } else if (!query.matchScope) {
    query.matchScope = 'both';
  }
  handleFilterChange();
}

function toggleSentiment(val: string) {
  query.sentiment = query.sentiment === val ? '' : val;
  query.pageNum = 1;
  refreshInfoList();
}

function handleSearch() {
  query.keyword = searchKeyword.value;
  query.pageNum = 1;
  refreshInfoList();
}

function handleFilterChange() {
  query.pageNum = 1;
  refreshInfoList();
}

// ========== 表格事件 ==========
function onSelectionChange(rows: CampusMonitorInformation[]) {
  selectedInfos.value = rows;
  selectedClues.value = rows.filter((row) => row.clueId).map((row) => toClue(row));
}

function viewClue(row: CampusClue) {
  if (row.clueId) {
    router.push(`/monitor/article/${row.clueId}`);
  } else if (row.url || row.originalUrl) {
    window.open(row.url || row.originalUrl, '_blank');
  } else {
    ElMessage.info('暂无链接');
  }
}

function joinClue(row: CampusClue) {
  // Single clue join to event — select target event
  selectedClues.value = [row];
  resetBatchForm();
  batchAction.value = 'joinEvent';
  batchOpVisible.value = true;
}

function toMonitorResult(row: CampusMonitorInformation): CampusMonitorResult {
  return {
    monitorResultId: row.monitorResultId,
    monitorTaskId: row.monitorTaskId,
    ingestRecordId: row.ingestRecordId,
    title: row.title,
    content: row.content,
    originalUrl: row.originalUrl,
    platform: row.platform || row.sourcePlatform,
    authorName: row.authorName || row.involvedAccount,
    publishTime: row.publishTime ? String(row.publishTime) : undefined,
    language: row.language,
    matchedSubjects: row.matchedSubjects,
    matchedKeywords: row.matchedKeywords || row.keywords,
    matchedNegativeWords: row.matchedNegativeWords,
    sentiment: row.sentiment,
    riskLevel: row.riskLevel,
    riskScore: row.riskScore,
    resultStatus: row.resultStatus,
    alertId: row.alertId,
    clueId: row.clueId,
    likeCount: row.likeCount,
    commentCount: row.commentCount,
    shareCount: row.shareCount,
    collectCount: row.collectCount,
    viewCount: row.viewCount,
    createTime: row.createTime ? String(row.createTime) : undefined
  };
}

function toClue(row: CampusMonitorInformation): CampusClue {
  return {
    clueId: row.clueId,
    clueTitle: row.title || '',
    clueContent: row.content || row.summary || '',
    clueSource: row.sourceSubPlatform || row.infoType || '',
    sourcePlatform: row.sourcePlatform || row.platform || '',
    sourceSubPlatform: row.sourceSubPlatform || '',
    originalUrl: row.originalUrl || '',
    publishTime: row.publishTime,
    discoverTime: row.collectTime || row.discoverTime || row.infoTime,
    involvedAccount: row.involvedAccount || row.authorName || '',
    keywords: row.keywords || row.matchedKeywords || '',
    sentiment: row.sentiment || '',
    riskLevel: row.riskLevel || 'normal',
    clueStatus: row.clueStatus,
    summary: row.summary || row.content || ''
  };
}

function viewMonitorInformation(row: CampusMonitorInformation) {
  currentInformation.value = { ...row };
  informationDetailVisible.value = true;
}

function informationDetailContent(row?: CampusMonitorInformation | null): string {
  if (!row) {
    return '';
  }
  return row.content || row.summary || row.title || '暂无正文';
}

function safeOriginalUrl(url?: string | null): string {
  const value = String(url || '').trim();
  return /^https?:\/\//i.test(value) ? value : '';
}

function openInformationOriginalUrl() {
  const originalUrl = safeOriginalUrl(currentInformation.value?.originalUrl);
  if (originalUrl) {
    window.open(originalUrl, '_blank');
    return;
  }
  ElMessage.info('暂无可打开的原文链接');
}

function monitorInformationStatusLabel(row: CampusMonitorInformation) {
  if (row.clueId) {
    return row.clueStatus ? `线索：${clueStatusLabel(row.clueStatus)}` : '已转线索';
  }
  return monitorResultStatusLabel(row.resultStatus, row.clueId);
}

function monitorInformationStatusReason(row: CampusMonitorInformation): string {
  if (row.clueId) {
    return '已进入线索库，可用于研判、归档或加入事件。';
  }
  if (row.resultStatus === 'alerted') {
    if (row.matchedNegativeWords) {
      return `命中负面/风险词：${row.matchedNegativeWords}`;
    }
    if (row.riskLevel && row.riskLevel !== 'normal') {
      return `风险等级：${clueRiskLabel(row.riskLevel)}`;
    }
    if (row.sentiment && (row.sentiment.includes('负') || row.sentiment.toLowerCase() === 'negative')) {
      return '情感判定为负面。';
    }
    return '历史规则或人工操作产生的预警。';
  }
  if (row.resultStatus === 'pending' || !row.resultStatus) {
    return '已命中监测任务，等待人工研判。';
  }
  if (row.resultStatus === 'ignored') {
    return '该监测结果已被忽略。';
  }
  return '';
}

function onColumnDragStart(key: string) {
  draggedColumnKey.value = key;
}

function onColumnDrop(targetKey: string) {
  const sourceKey = draggedColumnKey.value;
  draggedColumnKey.value = '';
  if (!sourceKey || sourceKey === targetKey) {
    return;
  }
  const sourceIndex = infoColumns.value.findIndex((col) => col.key === sourceKey);
  const targetIndex = infoColumns.value.findIndex((col) => col.key === targetKey);
  if (sourceIndex < 0 || targetIndex < 0) {
    return;
  }
  const next = [...infoColumns.value];
  const [moved] = next.splice(sourceIndex, 1);
  next.splice(targetIndex, 0, moved);
  infoColumns.value = next;
}

// ========== 工具栏操作 ==========
function markPageRead() {
  if (monitorInfos.value.length === 0) {
    ElMessage.info('当前没有可标记的数据');
    return;
  }
  const storageKey = 'monitor_read_info_ids';
  const existing = JSON.parse(localStorage.getItem(storageKey) || '[]') as string[];
  const newIds = monitorInfos.value
    .map((item) => `${item.infoType || 'info'}:${item.infoId || item.monitorResultId || item.clueId || ''}`)
    .filter((id) => !id.endsWith(':'));
  const merged = [...new Set([...existing, ...newIds])];
  localStorage.setItem(storageKey, JSON.stringify(merged));
  ElMessage.success(`已标记 ${newIds.length} 条为已读`);
}

function handleExport() {
  if (monitorInfos.value.length === 0) {
    ElMessage.info('当前没有可导出的数据');
    return;
  }
  const headers = ['标题', '摘要', '正文状态', '平台', '子平台', '账号/作者', '匹配关键词', '负面词', '情感', '状态', '发布时间', '采集时间', '原文链接'];
  const rows = monitorInfos.value.map((item) => [
    item.title || '',
    item.summary || item.content || '',
    contentCaptureLabel(item),
    platformDisplayName(item.platform || item.sourcePlatform || ''),
    item.sourceSubPlatform || '',
    item.authorName || item.involvedAccount || '',
    item.matchedKeywords || item.keywords || '',
    item.matchedNegativeWords || '',
    item.sentiment || '',
    monitorInformationStatusLabel(item),
    item.publishTime ? formatTime(item.publishTime) : '',
    item.collectTime || item.infoTime || item.discoverTime || item.createTime ? formatTime(item.collectTime || item.infoTime || item.discoverTime || item.createTime) : '',
    item.originalUrl || ''
  ]);
  const csvContent = [headers, ...rows]
    .map((row) => row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\n');
  const bom = '﻿';
  const blob = new Blob([bom + csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `监测信息导出_${new Date().toISOString().slice(0, 10)}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  ElMessage.success(`已导出 ${monitorInfos.value.length} 条数据`);
}

function handleBatchOp() {
  if (selectedInfos.value.length === 0) {
    ElMessage.warning('请先选择数据');
    return;
  }
  resetBatchForm();
  batchOpVisible.value = true;
}

// ========== 内容 Tabs 变更 ==========
function onMediaTypeChange(tab: string | number) {
  const tabValue = String(tab);
  if (tabValue === '全部') {
    query.sourcePlatform = '';
  } else {
    query.sourcePlatform = tabValue;
  }
  query.sourceSubPlatform = '';
  subPlatformTab.value = '全部';
  query.pageNum = 1;
  if (isForumPlatform(tabValue)) {
    loadSubPlatformCounts();
  } else {
    subPlatforms.value = [];
  }
  loadData();
}

function onSubPlatformChange(tab: string | number) {
  const tabValue = String(tab);
  if (tabValue === '全部') {
    query.sourceSubPlatform = '';
  } else {
    query.sourceSubPlatform = tabValue;
  }
  query.pageNum = 1;
  loadData();
}

// ========== 数据加载 ==========
/** 过滤空字符串和 undefined，避免 Spring 解析空串 Date → 400 */
function cleanQuery(raw: ClueAdvancedQuery): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(raw)) {
    if (v !== '' && v !== null && v !== undefined) {
      out[k] = v;
    }
  }
  return out;
}

function buildCountQuery(options: { includeSourcePlatform?: boolean; includeSourceSubPlatform?: boolean } = {}) {
  const params = cleanQuery(query);
  delete params.pageNum;
  delete params.pageSize;
  if (options.includeSourcePlatform === false) {
    delete params.sourcePlatform;
  }
  if (options.includeSourceSubPlatform === false) {
    delete params.sourceSubPlatform;
  }
  return params;
}

function entityId(value?: { id?: number; sourceId?: number; taskId?: number }): number | undefined {
  return value?.sourceId || value?.taskId || value?.id;
}

function platformFromFetchConfig(fetchConfig?: string): string {
  if (!fetchConfig) {
    return '';
  }
  try {
    const parsed = JSON.parse(fetchConfig) as { platform?: string; endpointKey?: string };
    if (parsed.platform) {
      return canonicalPlatformValue(parsed.platform);
    }
    if (parsed.endpointKey) {
      return canonicalPlatformValue(parsed.endpointKey);
    }
  } catch {
    return '';
  }
  return '';
}

async function loadPlatformConnections() {
  try {
    const [sourcePage, taskPage] = await Promise.all([
      listIngestSources({ pageNum: 1, pageSize: 500 }),
      listIngestTasks({ pageNum: 1, pageSize: 500 })
    ]);
    const sources: CampusIngestSource[] = sourcePage.list || [];
    const tasks: CampusIngestTask[] = taskPage.list || [];
    const sourcePlatformById = new Map<number, string>();
    const next: Record<string, PlatformConnection> = {};

    for (const source of sources) {
      const sourceId = entityId(source);
      const platform = canonicalPlatformValue(source.platform || '');
      if (!platform || platform === '全部') {
        continue;
      }
      if (sourceId) {
        sourcePlatformById.set(sourceId, platform);
      }
      next[platform] = next[platform] || { sourceCount: 0, activeTaskCount: 0 };
      next[platform].sourceCount += 1;
    }

    for (const task of tasks) {
      if (task.taskStatus && task.taskStatus !== 'active') {
        continue;
      }
      const sourceId = task.sourceId;
      const platform = sourceId ? sourcePlatformById.get(sourceId) : platformFromFetchConfig(task.fetchConfig);
      if (!platform || platform === '全部') {
        continue;
      }
      next[platform] = next[platform] || { sourceCount: 0, activeTaskCount: 0 };
      next[platform].activeTaskCount += 1;
    }

    platformConnections.value = next;
  } catch {
    platformConnections.value = {};
  }
}

async function refreshInfoList() {
  const tasks = [loadData(), loadMediaTypeCounts()];
  if (showSubTabs.value) {
    tasks.push(loadSubPlatformCounts());
  }
  await Promise.all(tasks);
}

async function loadData() {
  loading.value = true;
  try {
    const page = await listMonitorInformation(cleanQuery(query));
    monitorInfos.value = page.list || [];
    infoTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '监测信息加载失败');
    monitorInfos.value = [];
    infoTotal.value = 0;
  } finally {
    loading.value = false;
  }
}

function cleanMonitorResultQuery() {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(resultQuery)) {
    if (value !== '' && value !== null && value !== undefined) {
      params[key] = value;
    }
  }
  if (resultConvertedFilter.value === 'yes') {
    params.converted = true;
  }
  if (resultConvertedFilter.value === 'no') {
    params.converted = false;
  }
  return params;
}

async function loadMonitorResults() {
  resultLoading.value = true;
  try {
    const page = await listMonitorResults(cleanMonitorResultQuery() as any);
    monitorResults.value = page.list || [];
    resultTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '监测结果加载失败');
    monitorResults.value = [];
    resultTotal.value = 0;
  } finally {
    resultLoading.value = false;
  }
}

async function convertResult(row: CampusMonitorResult) {
  if (!row.monitorResultId) {
    ElMessage.warning('当前记录缺少监测结果ID，请刷新后重试');
    return;
  }
  try {
    const clue = await convertMonitorResultToClue(row.monitorResultId);
    ElMessage.success(`已转入线索库：${clue.clueTitle || row.title || ''}`);
    await Promise.all([loadMonitorResults(), refreshInfoList()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '转线索失败');
  }
}

async function updateMonitorSentiment(row: CampusMonitorInformation, sentiment: string) {
  if (!row.monitorResultId) {
    ElMessage.warning('当前记录缺少监测结果ID，请刷新后重试');
    return;
  }
  if (!canMonitorOperate.value) {
    ElMessage.warning('当前账号没有监测操作权限');
    return;
  }
  if (isArchivedLinkedClue(row)) {
    ElMessage.warning('已归档线索不能修改情感');
    return;
  }
  const normalized = normalizeSentimentValue(sentiment);
  if (normalizeSentimentValue(row.sentiment) === normalized) {
    return;
  }
  setSentimentUpdating(row.monitorResultId, true);
  try {
    const saved = await updateMonitorResultSentiment(row.monitorResultId, normalized);
    row.sentiment = saved.sentiment || normalized;
    ElMessage.success(`情感已修改为${sentimentOptionLabel(normalized)}`);
    await Promise.all([loadMonitorResults(), refreshInfoList()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '情感修改失败');
  } finally {
    setSentimentUpdating(row.monitorResultId, false);
  }
}

async function alertResult(row: CampusMonitorResult) {
  if (!row.monitorResultId) {
    ElMessage.warning('当前记录缺少监测结果ID，请刷新后重试');
    return;
  }
  try {
    await alertMonitorResult(row.monitorResultId);
    ElMessage.success('已转为预警');
    await Promise.all([loadMonitorResults(), loadData()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '转预警失败');
  }
}

async function ignoreResult(row: CampusMonitorResult) {
  if (!row.monitorResultId) {
    ElMessage.warning('当前记录缺少监测结果ID，请刷新后重试');
    return;
  }
  try {
    await ignoreMonitorResult(row.monitorResultId);
    ElMessage.success('监测结果已忽略');
    await Promise.all([loadMonitorResults(), loadData()]);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '忽略失败');
  }
}

async function addResultWatchTarget(row: CampusMonitorResult, targetType: 'account' | 'link') {
  if (!row.monitorResultId) {
    ElMessage.warning('当前记录缺少监测结果ID，请刷新后重试');
    return;
  }
  if (!row.monitorTaskId) {
    ElMessage.warning('当前记录缺少监测任务ID，请刷新后重试');
    return;
  }
  if (targetType === 'account' && !row.authorName && !row.originalUrl) {
    ElMessage.warning('该结果没有可加入的账号或主页信息');
    return;
  }
  if (targetType === 'link' && !row.originalUrl) {
    ElMessage.warning('该结果没有原始链接');
    return;
  }
  try {
    await createMonitorWatchTargetFromResult(row.monitorResultId, row.monitorTaskId, targetType);
    ElMessage.success(targetType === 'account' ? '已加入本任务重点账号监控' : '已加入本任务指定链接监控');
    if (String(watchTargetQuery.monitorTaskId || '') === String(row.monitorTaskId)) {
      await loadWatchTargets();
    }
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加入重点监控失败');
  }
}

function cleanWatchTargetQuery() {
  const params: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(watchTargetQuery)) {
    if (value !== '' && value !== null && value !== undefined) {
      params[key] = value;
    }
  }
  return params;
}

async function loadWatchTargets() {
  watchTargetLoading.value = true;
  try {
    const page = await listMonitorWatchTargets(cleanWatchTargetQuery() as any);
    watchTargets.value = page.list || [];
    watchTargetTotal.value = page.total || 0;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '重点监控目标加载失败');
    watchTargets.value = [];
    watchTargetTotal.value = 0;
  } finally {
    watchTargetLoading.value = false;
  }
}

function resetWatchTargetForm() {
  Object.assign(watchTargetForm, {
    targetId: undefined,
    monitorTaskId: watchTargetQuery.monitorTaskId || resultQuery.monitorTaskId,
    targetType: 'account',
    platform: '',
    accountId: undefined,
    accountName: '',
    accountUid: '',
    linkUrl: '',
    sourceObjectType: 'manual',
    sourceObjectId: undefined,
    authorizationScope: '',
    keywordScope: '',
    targetStatus: 'active',
    remark: ''
  });
}

function openWatchTargetCreate() {
  resetWatchTargetForm();
  if (!watchTargetForm.monitorTaskId) {
    ElMessage.warning('请先填写任务ID');
    return;
  }
  watchTargetDialogVisible.value = true;
}

function openWatchTargetEdit(row: CampusMonitorWatchTarget) {
  Object.assign(watchTargetForm, row);
  watchTargetDialogVisible.value = true;
}

async function submitWatchTarget() {
  if (!watchTargetForm.monitorTaskId) {
    ElMessage.warning('任务ID不能为空');
    return;
  }
  if (watchTargetForm.targetType === 'account' && !watchTargetForm.accountName && !watchTargetForm.accountUid && !watchTargetForm.linkUrl) {
    ElMessage.warning('重点账号至少填写账号名称、UID 或主页链接');
    return;
  }
  if (watchTargetForm.targetType === 'link' && !watchTargetForm.linkUrl) {
    ElMessage.warning('指定链接不能为空');
    return;
  }
  if (!watchTargetForm.authorizationScope) {
    ElMessage.warning('请填写授权/来源说明');
    return;
  }
  watchTargetSaving.value = true;
  try {
    const saved = await saveMonitorWatchTarget({ ...watchTargetForm });
    watchTargetQuery.monitorTaskId = saved.monitorTaskId;
    ElMessage.success('重点监控目标已保存');
    watchTargetDialogVisible.value = false;
    await loadWatchTargets();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    watchTargetSaving.value = false;
  }
}

async function toggleWatchTargetStatus(row: CampusMonitorWatchTarget) {
  if (!row.targetId) return;
  const nextStatus = row.targetStatus === 'paused' ? 'active' : 'paused';
  try {
    await saveMonitorWatchTarget({ ...row, targetStatus: nextStatus });
    ElMessage.success(nextStatus === 'active' ? '已启用' : '已暂停');
    await loadWatchTargets();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function deleteWatchTargetRow(row: CampusMonitorWatchTarget) {
  if (!row.targetId) return;
  try {
    await ElMessageBox.confirm('确认删除该重点监控目标吗？', '删除确认', { type: 'warning' });
    await deleteMonitorWatchTarget(row.targetId);
    ElMessage.success('已删除');
    await loadWatchTargets();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败');
    }
  }
}

function viewMonitorResultClue(row: CampusMonitorResult) {
  if (row.clueId) {
    router.push(`/monitor/article/${row.clueId}`);
  }
}

async function loadMediaTypeCounts() {
  try {
    const data = await fetchMonitorInformationPlatformCounts(buildCountQuery({
      includeSourcePlatform: false,
      includeSourceSubPlatform: false
    }));
    if (Array.isArray(data)) {
      mediaTypes.value = normalizeCountTabs(data, collectAllTabLabel());
    } else {
      mediaTypes.value = normalizeCountTabs([], collectAllTabLabel());
    }
  } catch {
    mediaTypes.value = normalizeCountTabs([], collectAllTabLabel());
  }
}

async function loadSubPlatformCounts() {
  try {
    const data = await fetchMonitorInformationSubPlatformCounts(buildCountQuery({
      includeSourcePlatform: true,
      includeSourceSubPlatform: false
    }));
    if (Array.isArray(data)) {
      subPlatforms.value = normalizeForumSubTabs(data, '全部');
    } else {
      subPlatforms.value = normalizeForumSubTabs([], '全部');
    }
  } catch {
    subPlatforms.value = normalizeForumSubTabs([], '全部');
  }
}

// ========== 初始化 ==========
onMounted(async () => {
  initCurrentYear();
  loadCurrentPermissions();
  loadData();
  await loadPlatformConnections();
  loadMediaTypeCounts();
  window.addEventListener('resize', resizeTopicCharts);
});

watch(
  () => route.query.hitScope,
  () => {
    const nextHitScope = routeHitScope();
    if (query.hitScope === nextHitScope) {
      return;
    }
    query.hitScope = nextHitScope;
    resetInformationNarrowFilters();
    handleFilterChange();
  }
);

// Watch pageTab to load data when switching tabs
watch(pageTab, (val) => {
  if (val === 'topicAnalysis') {
    nextTick(() => renderTopicAnalysisCharts());
  } else if (val === 'monitorResults') {
    if (monitorResults.value.length === 0) {
      loadMonitorResults();
    }
    if (watchTargets.value.length === 0) {
      loadWatchTargets();
    }
  } else if (val === 'topicAlert') {
    if (alertList.value.length === 0) {
      loadAlertData();
    }
  }
});

// Watch unified monitor information data to update analysis charts when data changes while on analysis tab
watch(monitorInfos, () => {
  if (pageTab.value === 'topicAnalysis') {
    nextTick(() => renderTopicAnalysisCharts());
  }
});

watch(
  () => resultQuery.monitorTaskId,
  (monitorTaskId) => {
    if (monitorTaskId !== watchTargetQuery.monitorTaskId) {
      watchTargetQuery.monitorTaskId = monitorTaskId;
      watchTargetQuery.pageNum = 1;
      if (pageTab.value === 'monitorResults') {
        loadWatchTargets();
      }
    }
  }
);

onBeforeUnmount(() => {
  disposeTopicAnalysisCharts();
  window.removeEventListener('resize', resizeTopicCharts);
});

function resizeTopicCharts() {
  keywordChart?.resize();
  mediaTypeChart?.resize();
  sentimentChart?.resize();
}
</script>

<style scoped>
.clue-list-page {
  padding: 16px;
  background: #f5f6fa;
  min-height: calc(100vh - 60px);
}

/* ====== 面包屑 ====== */
.breadcrumb-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 10px 20px;
  margin-bottom: 12px;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.breadcrumb-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.breadcrumb-divider {
  display: inline-block;
  width: 0;
  height: 18px;
  border-left: 3px solid #3D5AFE;
  border-radius: 2px;
}

.breadcrumb-label {
  font-size: 13px;
  color: #909399;
}

.breadcrumb-sep {
  color: #c0c4cc;
  font-size: 13px;
}

.breadcrumb-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

/* ====== 筛选卡片 ====== */
.filter-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
}

.filter-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  border-bottom: 1px solid #ebeef5;
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.filter-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-create-btn {
  width: 32px;
  height: 32px;
}

.filter-body {
  padding: 14px 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

/* ====== 内容卡片 ====== */
.content-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.content-card-title {
  padding: 12px 20px 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.monitor-info-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.monitor-info-heading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.monitor-info-count {
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.monitor-result-card {
  padding: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

/* el-tabs 内嵌样式覆盖 */
.content-card :deep(.media-tabs) {
  padding: 0 20px;
}

.content-card :deep(.media-tabs .el-tabs__header) {
  margin-bottom: 0;
}

.platform-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.platform-tab-status {
  color: #606266;
  font-size: 12px;
}

.platform-tab-status.muted {
  color: #a8abb2;
}

.platform-tab-status.warning {
  color: #e6a23c;
}

.content-card :deep(.sub-tabs) {
  padding: 0 20px;
  background: #fafbfc;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}

.content-card :deep(.sub-tabs .el-tabs__header) {
  margin-bottom: 0;
}

.content-card :deep(.sub-tabs .el-tabs__item) {
  font-size: 12px;
  height: 34px;
  line-height: 34px;
}

/* ====== 工具栏 ====== */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px 10px;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sentiment-tag {
  display: inline-block;
  padding: 3px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
  color: #606266;
}

.sentiment-tag:hover {
  opacity: 0.85;
}

.sentiment-tag.active {
  background: #f0f2f5;
}

.sentiment-edit-cell {
  display: flex;
  justify-content: center;
  min-width: 0;
}

.sentiment-badge-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.sentiment-badge-trigger:disabled {
  cursor: wait;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.column-settings {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.column-settings-title {
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.column-setting-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 2px;
  border-radius: 4px;
  cursor: grab;
}

.column-setting-row:hover {
  background: #f5f7fa;
}

.column-drag-handle {
  color: #a8abb2;
  font-size: 12px;
  letter-spacing: 1px;
}

.watch-target-panel {
  margin: 12px 0 16px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafcff;
}

.watch-target-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.watch-target-header strong {
  display: block;
  font-size: 13px;
  color: #303133;
  margin-bottom: 3px;
}

.watch-target-header span {
  font-size: 12px;
  color: #909399;
}

.watch-target-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.watch-target-actions :deep(.el-input-number),
.watch-target-actions :deep(.el-select) {
  width: 132px;
}

.pagination-row.compact {
  padding: 10px 0 0;
}

/* ====== 表格 ====== */
.clue-table {
  width: 100%;
}

.draggable-column-header {
  display: inline-flex;
  align-items: center;
  min-width: 100%;
  cursor: grab;
}

.ellipsis-cell {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.status-tag-with-reason {
  cursor: help;
}

.row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
}

/* 标题-摘要列 */
.title-summary-cell {
  line-height: 1.5;
}

.clue-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
  word-break: break-all;
}

.clue-title :deep(mark) {
  background: #fff3cd;
  color: #f56c6c;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 700;
}

.clue-summary {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
  line-height: 1.4;
  max-height: 34px;
  overflow: hidden;
}

.information-detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.information-detail-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.information-detail-title {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.5;
  color: #1f2d3d;
  word-break: break-word;
}

.information-detail-meta,
.detail-tag-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #606266;
  font-size: 12px;
}

.detail-section {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.detail-section.compact {
  padding-top: 10px;
}

.detail-section-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.information-detail-content {
  max-height: 360px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  color: #303133;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
}

.detail-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
  color: #606266;
  font-size: 13px;
}

.detail-meta-grid span {
  min-width: 0;
  word-break: break-word;
}

.detail-original-url {
  color: #909399;
  font-size: 12px;
  word-break: break-all;
  background: #fafafa;
  border-radius: 4px;
  padding: 8px 10px;
}

/* ====== 分页 ====== */
.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding: 14px 20px;
}

/* ====== 主题分析 ====== */
.topic-analysis-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.analysis-panel {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 16px 20px;
}

.panel-header h3 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.chart-box {
  width: 100%;
}

.analysis-table {
  width: 100%;
}

/* ====== 主题预警 ====== */
.topic-alert-panel {
  margin-top: 0;
}

.alert-table {
  width: 100%;
}

/* ====== 响应式 ====== */
@media (max-width: 900px) {
  .analysis-grid {
    grid-template-columns: 1fr;
  }

  .watch-target-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
