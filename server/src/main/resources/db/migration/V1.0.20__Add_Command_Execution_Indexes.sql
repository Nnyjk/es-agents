-- Add indexes for command_execution table to optimize query performance

-- Index for agent_instance_id (filter by agent)
CREATE INDEX IF NOT EXISTS idx_command_execution_agent_instance_id ON command_execution(agent_instance_id);

-- Index for template_id (filter by template)
CREATE INDEX IF NOT EXISTS idx_command_execution_template_id ON command_execution(template_id);

-- Index for status (filter by execution status)
CREATE INDEX IF NOT EXISTS idx_command_execution_status ON command_execution(status);

-- Index for executed_by (filter by executor)
CREATE INDEX IF NOT EXISTS idx_command_execution_executed_by ON command_execution(executed_by);

-- Index for created_at (sorting and time-range queries)
CREATE INDEX IF NOT EXISTS idx_command_execution_created_at ON command_execution(created_at);

-- Composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_command_execution_agent_status_created ON command_execution(agent_instance_id, status, created_at);

-- Composite index for template-based queries
CREATE INDEX IF NOT EXISTS idx_command_execution_template_status_created ON command_execution(template_id, status, created_at);