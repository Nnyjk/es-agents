-- Add os_type to agent_template
ALTER TABLE agent_template ADD COLUMN os_type VARCHAR(20);

-- Remove host_id from agent_command
ALTER TABLE agent_command DROP COLUMN host_id;

-- Seed Data
DO $$
DECLARE
    v_linux_source_id UUID;
    v_win_source_id UUID;
    v_linux_tmpl_id UUID;
    v_win_tmpl_id UUID;
    v_docker_tmpl_id UUID;
BEGIN
    -- 1. Create Agent Sources (Initial Seeding, will be updated by AgentSourceBootstrapper)
    v_linux_source_id := gen_random_uuid();
    v_win_source_id := gen_random_uuid();

    INSERT INTO agent_source (id, name, type, config, createdat, updatedat) VALUES 
    (v_linux_source_id, 'Host Agent (Linux)', 'HTTP', '{"url": "https://github.com/easy-station/agent/releases/latest/download/host-agent-linux"}', NOW(), NOW()),
    (v_win_source_id, 'Host Agent (Windows)', 'HTTP', '{"url": "https://github.com/easy-station/agent/releases/latest/download/host-agent-windows.exe"}', NOW(), NOW());

    -- 2. Create Agent Templates
    -- Linux Template
    v_linux_tmpl_id := gen_random_uuid();
    INSERT INTO agent_template (id, name, description, os_type, source_id, createdat, updatedat)
    VALUES (v_linux_tmpl_id, 'Host Agent (Linux)', 'Standard commands for Linux Host Agent', 'LINUX', v_linux_source_id, NOW(), NOW());

    -- Linux Commands
    INSERT INTO agent_command (id, name, script, defaultargs, template_id) VALUES
    (gen_random_uuid(), 'ls', 'ls -la', NULL, v_linux_tmpl_id),
    (gen_random_uuid(), 'df', 'df -h', NULL, v_linux_tmpl_id),
    (gen_random_uuid(), 'top', 'top -b -n 1 | head -n 20', NULL, v_linux_tmpl_id),
    (gen_random_uuid(), 'ps', 'ps aux | head -n 20', NULL, v_linux_tmpl_id);

    -- Docker Template (Uses Linux Source)
    v_docker_tmpl_id := gen_random_uuid();
    INSERT INTO agent_template (id, name, description, os_type, source_id, createdat, updatedat)
    VALUES (v_docker_tmpl_id, 'Host Agent (Docker)', 'Standard commands for Linux Docker Host Agent', 'LINUX_DOCKER', v_linux_source_id, NOW(), NOW());
    
    -- Clone commands from Linux to Docker
    INSERT INTO agent_command (id, name, script, defaultargs, template_id)
    SELECT gen_random_uuid(), name, script, defaultargs, v_docker_tmpl_id
    FROM agent_command WHERE template_id = v_linux_tmpl_id;

    -- Windows Template
    v_win_tmpl_id := gen_random_uuid();
    INSERT INTO agent_template (id, name, description, os_type, source_id, createdat, updatedat)
    VALUES (v_win_tmpl_id, 'Host Agent (Windows)', 'Standard commands for Windows Host Agent', 'WINDOWS', v_win_source_id, NOW(), NOW());

    -- Windows Commands
    INSERT INTO agent_command (id, name, script, defaultargs, template_id) VALUES
    (gen_random_uuid(), 'dir', 'dir', NULL, v_win_tmpl_id),
    (gen_random_uuid(), 'systeminfo', 'systeminfo', NULL, v_win_tmpl_id),
    (gen_random_uuid(), 'ipconfig', 'ipconfig', NULL, v_win_tmpl_id);

END $$;
