import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Login from "./pages/auth/Login";
import BasicLayout from "./layouts/BasicLayout";
import UserList from "./pages/system/User";
import RoleList from "./pages/system/Role";
import ModuleList from "./pages/system/Module";
import PermissionList from "./pages/system/Permission";
import AgentList from "./pages/agent/AgentList";
import AgentDetail from "./pages/agent/detail/AgentDetail";
import AgentTemplateList from "./pages/agent/Template";
import AgentCommandList from "./pages/agent/Command";
import AgentResourceList from "./pages/agent/Resource";
import AgentCredentialList from "./pages/agent/Credential";
import AgentRepositoryList from "./pages/agent/Repository";
import AgentSourcePage from "./pages/agent/AgentSourcePage";
import CommandExecute from "./pages/agent/CommandExecute";
import CommandHistory from "./pages/agent/CommandHistory";
import EnvironmentList from "./pages/infrastructure/Environment";
import HostList from "./pages/infrastructure/Host";
import { GoalHub } from "./pages/goal-hub";
import DeploymentWizard from "./pages/goals/DeploymentWizard";
import AlertList from "./pages/alert/AlertList";
import AlertRulePage from "./pages/alert/AlertRule";
import AlertChannelPage from "./pages/alert/AlertChannel";
import SystemSettings from "./pages/system/Settings";
import DeploymentPage from "./pages/deployment";
import { ProfilePage } from "./pages/profile";
import { AgentMonitorPage, AgentInstancePage } from "./pages/agentMonitoring";
import { TemplateWizard } from "./pages/templates";
import ScheduledTaskList from "./pages/scheduledTask";
import {
  PluginMarketPage,
  InstalledPluginsPage,
} from "./pages/pluginMarketplace";
import AuditLogList from "./pages/profile/AuditLog";
import ApiKeyList from "./pages/settings/ApiKey";
import NotificationChannelsList from "./pages/settings/NotificationChannels";
import NotificationTemplatesList from "./pages/settings/NotificationTemplates";
import NotificationHistoryList from "./pages/settings/NotificationHistory";
import CommandTemplatePage from "./pages/command/CommandTemplatePage";
import { ConfigPage } from "./pages/config";

const App: React.FC = () => {
  return (
    <Router>
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
            <Route
              path="monitoring/instances"
              element={<AgentInstancePage />}
            />
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
          <Route path="settings">
            <Route path="api-keys" element={<ApiKeyList />} />
            <Route
              path="notification-channels"
              element={<NotificationChannelsList />}
            />
            <Route
              path="notification-templates"
              element={<NotificationTemplatesList />}
            />
            <Route
              path="notification-history"
              element={<NotificationHistoryList />}
            />
          </Route>
        </Route>
      </Routes>
    </Router>
  );
};

export default App;
