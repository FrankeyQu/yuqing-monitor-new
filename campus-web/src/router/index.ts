import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { ensureSession, isLoggedIn } from '../services/auth';
import MainLayout from '../layouts/MainLayout.vue';
import AdminLayout from '../layouts/AdminLayout.vue';
import LoginView from '../views/LoginView.vue';
import DashboardView from '../views/DashboardView.vue';
import MonitorView from '../views/MonitorView.vue';
import ArticleDetailView from '../views/ArticleDetailView.vue';
import AccountView from '../views/AccountView.vue';
import EventView from '../views/EventView.vue';
import AlertView from '../views/AlertView.vue';
import MonitorTaskAdminView from '../views/MonitorTaskAdminView.vue';
import EducationView from '../views/EducationView.vue';
import JudgmentView from '../views/JudgmentView.vue';
import AnalysisView from '../views/AnalysisView.vue';
import ReportView from '../views/ReportView.vue';
import ReportTemplateView from '../views/ReportTemplateView.vue';
import ReportTemplateEditorView from '../views/ReportTemplateEditorView.vue';
import AutoReportView from '../views/AutoReportView.vue';
import SearchView from '../views/SearchView.vue';
import CompareView from '../views/CompareView.vue';
import DepartmentView from '../views/settings/DepartmentView.vue';
import DictView from '../views/settings/DictView.vue';
import AuditLogView from '../views/settings/AuditLogView.vue';
import PermissionView from '../views/settings/PermissionView.vue';
import AiManagementView from '../views/settings/AiManagementView.vue';
import { COMPANY_NAME, DEFAULT_DOCUMENT_TITLE } from '../config/brand';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'dashboard',
        component: DashboardView,
        meta: { title: '舆情态势' }
      },
      {
        path: 'situation',
        name: 'situation',
        component: DashboardView,
        meta: { title: '态势大屏' }
      },
      {
        path: 'monitor',
        name: 'monitor',
        component: MonitorView,
        meta: { title: '监测信息' }
      },
      {
        path: 'clues',
        redirect: '/monitor'
      },
      {
        path: 'monitor/article/:id',
        name: 'clue-detail',
        component: ArticleDetailView,
        meta: { title: '文章详情' }
      },
      {
        path: 'clues/:id',
        redirect: (to) => ({ path: `/monitor/article/${String(to.params.id)}` })
      },
      {
        path: 'events',
        name: 'events',
        component: EventView,
        meta: { title: '事件处置' }
      },
      {
        path: 'alerts',
        name: 'alerts',
        component: AlertView,
        meta: { title: '预警中心' }
      },
      {
        path: 'analysis',
        name: 'analysis',
        component: AnalysisView,
        meta: { title: '规则辅助研判' }
      },
      {
        path: 'judgment',
        name: 'judgment',
        component: JudgmentView,
        meta: { title: '舆情研判' }
      },
      {
        path: 'reports',
        name: 'reports',
        component: ReportView,
        meta: { title: '报告归档' }
      },
      {
        path: 'report-templates',
        name: 'report-templates',
        component: ReportTemplateView,
        meta: { title: '报告模板' }
      },
      {
        path: 'report-templates/create',
        name: 'report-template-create',
        component: ReportTemplateEditorView,
        meta: { title: '新增报告模板' }
      },
      {
        path: 'report-templates/:templateId/edit',
        name: 'report-template-edit',
        component: ReportTemplateEditorView,
        meta: { title: '编辑报告模板' }
      },
      {
        path: 'auto-reports',
        name: 'auto-reports',
        component: AutoReportView,
        meta: { title: '自动报告' }
      },
      {
        path: 'compare',
        name: 'compare',
        component: CompareView,
        meta: { title: '对比分析' }
      },
      {
        path: 'search',
        name: 'search',
        component: SearchView,
        meta: { title: '搜索' }
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/monitor-tasks',
    children: [
      {
        path: 'accounts',
        name: 'admin-accounts',
        component: AccountView,
        meta: { title: '重点账号' }
      },
      {
        path: 'monitor-tasks',
        name: 'admin-monitor-tasks',
        component: MonitorTaskAdminView,
        meta: { title: '监测任务管理' }
      },
      {
        path: 'events',
        redirect: '/events'
      },
      {
        path: 'alerts',
        redirect: '/alerts'
      },
      {
        path: 'detection',
        redirect: '/admin/monitor-tasks'
      },
      {
        path: 'ingest',
        redirect: '/admin/monitor-tasks'
      },
      {
        path: 'education',
        name: 'admin-education',
        component: EducationView,
        meta: { title: '教育专题' }
      },
      {
        path: 'settings',
        redirect: '/admin/settings/departments'
      },
      {
        path: 'settings/departments',
        name: 'admin-settings-departments',
        component: DepartmentView,
        meta: { title: '部门管理' }
      },
      {
        path: 'settings/dicts',
        name: 'admin-settings-dicts',
        component: DictView,
        meta: { title: '数据字典' }
      },
      {
        path: 'settings/audit',
        name: 'admin-settings-audit',
        component: AuditLogView,
        meta: { title: '审计日志' }
      },
      {
        path: 'settings/permissions',
        name: 'admin-settings-permissions',
        component: PermissionView,
        meta: { title: '权限管理' }
      },
      {
        path: 'settings/ai',
        name: 'admin-settings-ai',
        component: AiManagementView,
        meta: { title: 'AI能力管理' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${String(to.meta.title)} - ${COMPANY_NAME}` : DEFAULT_DOCUMENT_TITLE;
  if (to.meta.public) {
    return isLoggedIn() && await ensureSession() ? '/' : true;
  }
  return await ensureSession() ? true : '/login';
});

export default router;
