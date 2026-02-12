import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/auth/Login';
import BasicLayout from './layouts/BasicLayout';
import UserList from './pages/system/User';
import RoleList from './pages/system/Role';
import ModuleList from './pages/system/Module';
import AgentList from './pages/agent/AgentList';
import AgentTemplateList from './pages/agent/Template';
import AgentCommandList from './pages/agent/Command';
import AgentResourceList from './pages/agent/Resource';
import AgentCredentialList from './pages/agent/Credential';
import AgentRepositoryList from './pages/agent/Repository';
import EnvironmentList from './pages/infrastructure/Environment';
import HostList from './pages/infrastructure/Host';

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
           </Route>
           <Route path="infra">
             <Route path="envs" element={<EnvironmentList />} />
             <Route path="hosts" element={<HostList />} />
             <Route index element={<Navigate to="envs" replace />} />
           </Route>
           <Route path="users" element={<UserList />} />
           <Route path="roles" element={<RoleList />} />
           <Route path="roles/auth/:id" element={<RoleList />} />
           <Route path="modules" element={<ModuleList />} />
        </Route>
      </Routes>
    </Router>
  );
};

export default App;
