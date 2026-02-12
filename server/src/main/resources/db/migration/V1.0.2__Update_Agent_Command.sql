ALTER TABLE agent_command ADD COLUMN host_id UUID;
ALTER TABLE agent_command ALTER COLUMN template_id DROP NOT NULL;

DROP TABLE IF EXISTS es_terminal_command;
