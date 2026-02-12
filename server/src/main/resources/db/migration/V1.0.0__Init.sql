-- V1.0.0 Initial Schema and Data

-- Sys Module
CREATE TABLE sys_module (
    id uuid NOT NULL,
    code varchar(255) NOT NULL UNIQUE,
    name varchar(255) NOT NULL,
    type varchar(255) CHECK (type IN ('DIRECTORY','MENU','BUTTON')),
    path varchar(255),
    parentId uuid,
    sortOrder integer,
    PRIMARY KEY (id)
);

-- Sys Module Action
CREATE TABLE sys_module_action (
    id uuid NOT NULL,
    code varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    module_id uuid NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (module_id) REFERENCES sys_module(id)
);

-- Sys Role
CREATE TABLE sys_role (
    id uuid NOT NULL,
    code varchar(255) NOT NULL UNIQUE,
    name varchar(255) NOT NULL,
    description varchar(255),
    PRIMARY KEY (id)
);

-- Sys User
CREATE TABLE sys_user (
    id uuid NOT NULL,
    username varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    status varchar(255) CHECK (status IN ('ACTIVE','INACTIVE','LOCKED')),
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id)
);

-- Sys User Role
CREATE TABLE sys_user_role (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

-- Sys Role Module
CREATE TABLE sys_role_module (
    role_id uuid NOT NULL,
    module_id uuid NOT NULL,
    PRIMARY KEY (role_id, module_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id),
    FOREIGN KEY (module_id) REFERENCES sys_module(id)
);

-- Sys Role Module Action
CREATE TABLE sys_role_module_action (
    role_id uuid NOT NULL,
    module_action_id uuid NOT NULL,
    PRIMARY KEY (role_id, module_action_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id),
    FOREIGN KEY (module_action_id) REFERENCES sys_module_action(id)
);

-- Infra Environment
CREATE TABLE infra_environment (
    id uuid NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    code varchar(255) NOT NULL UNIQUE,
    description varchar(255),
    enabled boolean NOT NULL,
    color varchar(255),
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id)
);

-- Infra Host
CREATE TABLE infra_host (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    hostname varchar(255) NOT NULL,
    os varchar(255),
    cpuInfo varchar(255),
    memInfo varchar(255),
    environment_id uuid NOT NULL,
    status varchar(255) CHECK (status IN ('UNCONNECTED','ONLINE','OFFLINE','EXCEPTION')),
    secretKey varchar(255) NOT NULL,
    heartbeatInterval integer DEFAULT 30,
    config TEXT,
    gatewayUrl varchar(255),
    listenPort integer DEFAULT 9090,
    description varchar(255),
    createdAt timestamp(6),
    updatedAt timestamp(6),
    lastHeartbeat timestamp(6),
    PRIMARY KEY (id),
    FOREIGN KEY (environment_id) REFERENCES infra_environment(id)
);

-- Terminal Command
CREATE TABLE es_terminal_command (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    script varchar(2048) NOT NULL,
    description varchar(1024),
    os_type varchar(255),
    created_at timestamp(6),
    PRIMARY KEY (id)
);

-- Agent Source
CREATE TABLE agent_source (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    type varchar(255) NOT NULL CHECK (type IN ('GIT','DOCKER','HTTP','LOCAL','ALIYUN')),
    config TEXT,
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id)
);

-- Agent Template
CREATE TABLE agent_template (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    description varchar(255),
    source_id uuid,
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id),
    FOREIGN KEY (source_id) REFERENCES agent_source(id)
);

-- Agent Command
CREATE TABLE agent_command (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    script TEXT,
    timeout bigint,
    defaultArgs TEXT,
    template_id uuid NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (template_id) REFERENCES agent_template(id)
);

-- Agent Instance
CREATE TABLE agent_instance (
    id uuid NOT NULL,
    host_id uuid NOT NULL,
    template_id uuid NOT NULL,
    status varchar(255) CHECK (status IN ('UNCONFIGURED','PREPARING','READY','PACKAGING','PACKAGED','DEPLOYING','DEPLOYED','ONLINE','OFFLINE','ERROR')),
    version varchar(255),
    lastHeartbeatTime timestamp(6),
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id),
    FOREIGN KEY (host_id) REFERENCES infra_host(id),
    FOREIGN KEY (template_id) REFERENCES agent_template(id)
);

-- Agent Task
CREATE TABLE agent_task (
    id uuid NOT NULL,
    agent_instance_id uuid NOT NULL,
    command_id uuid NOT NULL,
    status varchar(255) CHECK (status IN ('PENDING','SENT','RUNNING','SUCCESS','FAILED')),
    args TEXT,
    result TEXT,
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id),
    FOREIGN KEY (agent_instance_id) REFERENCES agent_instance(id),
    FOREIGN KEY (command_id) REFERENCES agent_command(id)
);

-- Data Import (from import.sql)

-- Role: admin
INSERT INTO sys_role (id, code, name, description) VALUES ('a6f8ff6f-7192-4b44-95b1-5b9d1f92b5a4', 'admin', 'Administrator', 'System Administrator') ON CONFLICT (code) DO NOTHING;

-- User: admin (password: admin123)
INSERT INTO sys_user (id, username, password, status, createdat, updatedat) VALUES ('fdfbb51a-6e8f-423d-a398-f07a2758818b', 'admin', '$2b$12$I1G7ohMpQAgIQTtLzTkCuOGbQuf.qWVdFAtfixsQmbFRUR166rY5i', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (username) DO NOTHING;

-- User Role Association
INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username='admin' AND r.code='admin' ON CONFLICT DO NOTHING;

-- Modules
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('7dd60cc8-36f9-4a56-bb1b-a047fd1f23d1', 'agent-root', 'Agent管理', 'DIRECTORY', '/agents', 100, NULL) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('5aaa1628-c094-4853-8c62-1b84d37addc3', 'infra-root', '基础设施', 'DIRECTORY', '/infra', 150, NULL) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('b51c402c-3e3a-4746-be3e-dddd4692d863', 'system-root', '系统管理', 'DIRECTORY', '/system', 200, NULL) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('c8f141de-72c6-4597-8425-71643017fea5', 'env-mgmt', '环境管理', 'MENU', '/infra/envs', 1, (SELECT id FROM sys_module WHERE code='infra-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('8d4f2c56-f338-4442-b5aa-3e305101d2ea', 'host-mgmt', '主机管理', 'MENU', '/infra/hosts', 2, (SELECT id FROM sys_module WHERE code='infra-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('98a61aef-1867-467a-8222-47d49b92587b', 'user-mgmt', '用户管理', 'MENU', '/users', 1, (SELECT id FROM sys_module WHERE code='system-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('8d80e824-83aa-4b56-9ccd-078bde592971', 'role-mgmt', '角色管理', 'MENU', '/roles', 2, (SELECT id FROM sys_module WHERE code='system-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('1a6c19e7-a77b-4bf2-b523-08e2be11dd4b', 'module-mgmt', '菜单管理', 'MENU', '/modules', 3, (SELECT id FROM sys_module WHERE code='system-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('fae95da8-efe9-4bb6-84e3-6fc4207f3966', 'agent-instance', 'Agent实例', 'MENU', '/agents/instances', 1, (SELECT id FROM sys_module WHERE code='agent-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('a8f9c33d-818f-42b2-bc41-bbd3d09e5dcf', 'agent-template', 'Agent模板', 'MENU', '/agents/templates', 2, (SELECT id FROM sys_module WHERE code='agent-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('6fb508d1-6e1b-4933-a3f0-1e61ad32e359', 'agent-command', 'Agent命令', 'MENU', '/agents/commands', 3, (SELECT id FROM sys_module WHERE code='agent-root')) ON CONFLICT (code) DO NOTHING;
INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid) VALUES ('1ee5863b-04c2-4130-b78d-ccf177ba6f70', 'agent-resource', 'Agent资源', 'MENU', '/agents/resources', 4, (SELECT id FROM sys_module WHERE code='agent-root')) ON CONFLICT (code) DO NOTHING;

-- Role Modules (Admin gets all)
INSERT INTO sys_role_module (role_id, module_id) SELECT r.id, m.id FROM sys_role r, sys_module m WHERE r.code='admin' ON CONFLICT DO NOTHING;
