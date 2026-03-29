import React, { Suspense, lazy } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { Spin } from "antd";
import BasicLayout from "./layouts/BasicLayout";
import { ThemeProvider } from "./contexts/ThemeContext";

// ============================================
// Lazy Loaded Components (M5 Issue #343)
// 路由懒加载优化，减少初始包体积
// ============================================

// Auth
const Login = lazy(() => import("./pages/auth/Login"));

// System
const UserList = lazy(() => import("./pages/system/User"));
const RoleList = lazy(() => import("./pages/system/Role"));
const ModuleList = lazy(() => import("./pages/system/Module"));
const PermissionList = lazy(() => import("./pages/system/Permission"));
const SystemSettings = lazy(() => import("./pages/system/Settings"));

// Agent
const AgentList = lazy(() => import("./pages/agent/AgentList"));
const AgentDetail = lazy(() => import("./pages/agent/detail/AgentDetail"));
const AgentTemplateList = lazy(() => import("./pages/agent/Template"));
const AgentCommandList = lazy(() => import("./pages/agent/Command"));
const AgentResourceList = lazy(() => import("./pages/agent/Resource"));
const AgentCredentialList = lazy(() => import("./pages/agent/Credential"));
const AgentRepositoryList = lazy(() => import("./pages/agent/Repository"));
const AgentSourcePage = lazy(() => import("./pages/agent/AgentSourcePage"));
const CommandExecute = lazy(() => import("./pages/agent/CommandExecute"));
const CommandHistory = lazy(() => import("./pages/agent/CommandHistory"));
const AgentMonitorPage = lazy(() => import("./pages/agentMonitoring/AgentMonitorPage"));
const AgentInstancePage = lazy(() => import("./pages/agentMonitoring/AgentInstancePage"));

// Infrastructure
const EnvironmentList = lazy(() => import("./pages/infrastructure/Environment"));
const HostList = lazy(() => import("./pages/infrastructure/Host"));

// Goal Hub & Deployments
const GoalHub = lazy(() => import("./pages/goal-hub").then(m => ({ default: m.GoalHub })));
const DeploymentWizard = lazy(() => import("./pages/goals/DeploymentWizard"));
const DeploymentPage = lazy(() => import("./pages/deployment"));
const DeploymentListPage = lazy(() => import("./pages/deployments").then(m => ({ default: m.DeploymentListPage })));
const DeploymentDetailPage = lazy(() => import("./pages/deployments").then(m => ({ default: m.DeploymentDetailPage })));
const DeploymentCreatePage = lazy(() => import("./pages/deployments").then(m => ({ default: m.DeploymentCreatePage })));

// Profile & Settings
const ProfilePage = lazy(() => import("./pages/profile").then(m => ({ default: m.ProfilePage })));
const AuditLogList = lazy(() => import("./pages/profile/AuditLog"));
const ApiKeyList = lazy(() => import("./pages/settings/ApiKey"));
const NotificationChannelsList = lazy(() => import("./pages/settings/NotificationChannels"));
const NotificationTemplatesList = lazy(() => import("./pages/settings/NotificationTemplates"));
const NotificationHistoryList = lazy(() => import("./pages/settings/NotificationHistory"));

// Alerts
const AlertList = lazy(() => import("./pages/alert/AlertList"));
const AlertRulePage = lazy(() => import("./pages/alert/AlertRule"));
const AlertChannelPage = lazy(() => import("./pages/alert/AlertChannel"));

// Plugins
const PluginMarketPage = lazy(() => import("./pages/pluginMarketplace").then(m => ({ default: m.PluginMarketPage })));
const InstalledPluginsPage = lazy(() => import("./pages/pluginMarketplace").then(m => ({ default: m.InstalledPluginsPage })));

// Scheduled Tasks & Commands
const ScheduledTaskList = lazy(() => import("./pages/scheduledTask"));
const CommandTemplatePage = lazy(() => import("./pages/command/CommandTemplatePage"));

// Config & Batch
const ConfigPage = lazy(() => import("./pages/config").then(m => ({ default: m.ConfigPage })));
const BatchOperationsPage = lazy(() => import("./pages/batch").then(m => ({ default: m.BatchOperationsPage })));
const BatchOperationDetailPage = lazy(() => import("./pages/batch").then(m => ({ default: m.BatchOperationDetailPage })));

// Monitoring
const MonitoringPage = lazy(() => import("./pages/monitoring/MonitoringPage"));
const GrafanaDashboardPage = lazy(() => import("./pages/monitoring/GrafanaDashboardPage"));

// System Event Log
const SystemEventLogPage = lazy(() => import("./pages/systemEventLog"));

// Template Wizard
const TemplateWizard = lazy(() => import("./pages/templates").then(m => ({ default: m.TemplateWizard })));

// ============================================
// Loading Component
// ============================================
const PageLoader: React.FC = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <Spin size="large" tip="Loading..." />
  </div>
);

const App: React.FC = () => {
  return (
    <ThemeProvider>
      <Router>
        <Suspense fallback={<PageLoader />}>
          <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<BasicLayout />}>
            <Route index element={<Navigate to="/goals" replace />} />
            <Route path="agents">
              <Route index element={<AgentList />} />
              <Route path=":id" element={<AgentDetail />} />
              <Route path="instances" element={<AgentList />} />
              <Route path="templates" element={<AgentTemplateList />} />
              <Route path="templates/new" element={<TemplateWizard />} />
              <Route path="commands" element={<AgentCommandList />} />
              <Route path="resources" element={<AgentResourceList />} />
              <Route path="credentials" element={<AgentCredentialList />} />
              <Route path="repositories" element={<AgentRepositoryList />} />
              <Route path="sources" element={<AgentSourcePage />} />
              <Route path="execute" element={<CommandExecute />} />
              <Route path="history" element={<CommandHistory />} />
              <Route path="monitoring" element={<AgentMonitorPage />} />
              <Route path="monitoring/instances" element={<AgentInstancePage />} />
            </Route>
            <Route path="infra">
              <Route path="envs" element={<EnvironmentList />} />
              <Route path="hosts" element={<HostList />} />
              <Route index element={<Navigate to="envs" replace />} />
            </Route>
            <Route path="alerts">
              <Route index element={<AlertList />} />
              <Route path="rules" element={<AlertRulePage />} />
              <Route path="channels" element={<AlertChannelPage />} />
            </Route>
            <Route path="users" element={<UserList />} />
            <Route path="roles" element={<RoleList />} />
            <Route path="roles/auth/:id" element={<RoleList />} />
            <Route path="modules" element={<ModuleList />} />
            <Route path="permissions" element={<PermissionList />} />
            <Route path="settings" element={<SystemSettings />} />
            <Route path="goals" element={<GoalHub />} />
            <Route path="goals/deploy" element={<DeploymentWizard />} />
            <Route path="deployment" element={<DeploymentPage />} />
            <Route path="deployments">
              <Route index element={<DeploymentListPage />} />
              <Route path=":id" element={<DeploymentDetailPage />} />
              <Route path="create" element={<DeploymentCreatePage />} />
            </Route>
            <Route path="profile" element={<ProfilePage />} />
            <Route path="audit-logs" element={<AuditLogList />} />
            <Route path="plugins">
              <Route index element={<PluginMarketPage />} />
              <Route path="market" element={<PluginMarketPage />} />
              <Route path="installed" element={<InstalledPluginsPage />} />
            </Route>
            <Route path="scheduled-tasks" element={<ScheduledTaskList />} />
            <Route path="command-templates" element={<CommandTemplatePage />} />
            <Route path="configs" element={<ConfigPage />} />
            <Route path="batch">
              <Route index element={<BatchOperationsPage />} />
              <Route path=":id" element={<BatchOperationDetailPage />} />
            </Route>
            <Route path="monitoring">
              <Route index element={<MonitoringPage />} />
              <Route path="grafana" element={<GrafanaDashboardPage />} />
            </Route>
            <Route path="system-event-log" element={<SystemEventLogPage />} />
            <Route path="settings">
              <Route path="api-keys" element={<ApiKeyList />} />
              <Route path="notification-channels" element={<NotificationChannelsList />} />
              <Route path="notification-templates" element={<NotificationTemplatesList />} />
              <Route path="notification-history" element={<NotificationHistoryList />} />
            </Route>
          </Route>
        </Routes>
      </Suspense>
    </Router>
    </ThemeProvider>
  );
};

export default App;
