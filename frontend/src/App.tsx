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
import AgentList from "./pages/agent/AgentList";
import AgentTemplateList from "./pages/agent/Template";
import AgentCommandList from "./pages/agent/Command";
import AgentResourceList from "./pages/agent/Resource";
import AgentCredentialList from "./pages/agent/Credential";
import AgentRepositoryList from "./pages/agent/Repository";
import CommandExecute from "./pages/agent/CommandExecute";
import CommandHistory from "./pages/agent/CommandHistory";
import EnvironmentList from "./pages/infrastructure/Environment";
import HostList from "./pages/infrastructure/Host";
import GoalHub from "./pages/goals/GoalHub";
import DeploymentWizard from "./pages/goals/DeploymentWizard";
import AlertList from "./pages/alert/AlertList";
import AlertRulePage from "./pages/alert/AlertRule";
import AlertChannelPage from "./pages/alert/AlertChannel";
import BackupManagement from "./pages/system/Backup";
import SystemSettings from "./pages/system/Settings";
import CMDBPage from "./pages/cmdb";

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<BasicLayout />}>
          <Route index element={<Navigate to="/agents" replace />} />
          <Route path="agents">
            <Route index element={<AgentList />} />
            <Route path="instances" element={<AgentList />} />
            <Route path="templates" element={<AgentTemplateList />} />
            <Route path="commands" element={<AgentCommandList />} />
            <Route path="resources" element={<AgentResourceList />} />
            <Route path="credentials" element={<AgentCredentialList />} />
            <Route path="repositories" element={<AgentRepositoryList />} />
            <Route path="execute" element={<CommandExecute />} />
            <Route path="history" element={<CommandHistory />} />
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
          <Route path="backup" element={<BackupManagement />} />
          <Route path="settings" element={<SystemSettings />} />
          <Route path="cmdb" element={<CMDBPage />} />
          <Route path="goals" element={<GoalHub />} />
          <Route path="goals/deploy" element={<DeploymentWizard />} />
        </Route>
      </Routes>
    </Router>
  );
};

export default App;
