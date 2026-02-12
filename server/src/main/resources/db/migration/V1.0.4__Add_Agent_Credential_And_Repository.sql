CREATE TABLE agent_credential (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    type varchar(255) NOT NULL CHECK (type IN ('STATIC_TOKEN','API_TOKEN','SCRIPT_TOKEN','SSO_TOKEN')),
    config TEXT,
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id)
);

CREATE TABLE agent_repository (
    id uuid NOT NULL,
    name varchar(255) NOT NULL,
    type varchar(255) NOT NULL CHECK (type IN ('GITLAB','MAVEN','NEXTCLOUD')),
    base_url varchar(1024) NOT NULL,
    project_path varchar(1024) NOT NULL,
    default_branch varchar(255),
    credential_id uuid,
    createdAt timestamp(6),
    updatedAt timestamp(6),
    PRIMARY KEY (id),
    FOREIGN KEY (credential_id) REFERENCES agent_credential(id)
);

ALTER TABLE agent_source ADD COLUMN repository_id uuid;
ALTER TABLE agent_source ADD COLUMN credential_id uuid;
ALTER TABLE agent_source DROP CONSTRAINT IF EXISTS agent_source_type_check;
ALTER TABLE agent_source ADD CONSTRAINT agent_source_type_check CHECK (type IN ('GITLAB','MAVEN','NEXTCLOUD','GIT','DOCKER','HTTPS','HTTP','LOCAL','ALIYUN'));
ALTER TABLE agent_source ADD CONSTRAINT fk_agent_source_repository FOREIGN KEY (repository_id) REFERENCES agent_repository(id);
ALTER TABLE agent_source ADD CONSTRAINT fk_agent_source_credential FOREIGN KEY (credential_id) REFERENCES agent_credential(id);

INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('d0f5b1b4-4f85-4ef0-9c2a-b82286f6aef2', 'agent-repository', 'Agent仓库', 'MENU', '/agents/repositories', 5, (SELECT id FROM sys_module WHERE code='agent-root'))
ON CONFLICT (code) DO NOTHING;

INSERT INTO sys_module (id, code, name, type, path, sortorder, parentid)
VALUES ('aa14bb0b-7f1f-4b45-9c2f-cf4e1e1bde62', 'agent-credential', '凭证管理', 'MENU', '/agents/credentials', 6, (SELECT id FROM sys_module WHERE code='agent-root'))
ON CONFLICT (code) DO NOTHING;

INSERT INTO sys_role_module (role_id, module_id)
SELECT r.id, m.id FROM sys_role r, sys_module m WHERE r.code='admin' AND m.code IN ('agent-repository','agent-credential')
ON CONFLICT DO NOTHING;
