-- M5 Issue #342: Database Query Optimization
-- Add missing indexes to improve query performance

-- ============================================
-- Plugin Marketplace Indexes
-- ============================================

-- Composite index for plugin list queries (category + status)
CREATE INDEX IF NOT EXISTS idx_plugin_category_status ON plugin(category_id, status);

-- Composite index for developer's plugins (developer + status + created_at)
CREATE INDEX IF NOT EXISTS idx_plugin_developer_status_created ON plugin(developer_id, status, created_at);

-- Index for plugin search by name
CREATE INDEX IF NOT EXISTS idx_plugin_name_search ON plugin USING gin(name gin_trgm_ops);

-- Index for plugin review queries (status + created_at for pending reviews)
CREATE INDEX IF NOT EXISTS idx_plugin_review_status_created ON plugin_review(status, created_at);

-- Composite index for plugin version queries
CREATE INDEX IF NOT EXISTS idx_plugin_version_plugin_status ON plugin_version(plugin_id, status);

-- Index for plugin comment queries (plugin + created_at for listing)
CREATE INDEX IF NOT EXISTS idx_plugin_comment_plugin_created ON plugin_comment(plugin_id, created_at);

-- Index for plugin installation queries (agent + status)
CREATE INDEX IF NOT EXISTS idx_plugin_installation_agent_status ON plugin_installation(agent_id, status);

-- ============================================
-- Agent Instance & Task Indexes
-- ============================================

-- Index for agent instance by host
CREATE INDEX IF NOT EXISTS idx_agent_instance_host ON agent_instance(host_id);

-- Index for agent instance by template
CREATE INDEX IF NOT EXISTS idx_agent_instance_template ON agent_instance(template_id);

-- Composite index for agent instance queries (status + updated_at for monitoring)
CREATE INDEX IF NOT EXISTS idx_agent_instance_status_updated ON agent_instance(status, updated_at);

-- Index for agent task by status
CREATE INDEX IF NOT EXISTS idx_agent_task_status ON agent_task(status);

-- Composite index for agent task queries (agent + status + created_at)
CREATE INDEX IF NOT EXISTS idx_agent_task_agent_status_created ON agent_task(agent_instance_id, status, created_at);

-- ============================================
-- Infrastructure Indexes
-- ============================================

-- Index for host by environment
CREATE INDEX IF NOT EXISTS idx_infra_host_environment ON infra_host(environment_id);

-- Index for host by status
CREATE INDEX IF NOT EXISTS idx_infra_host_status ON infra_host(status);

-- Index for environment by project
CREATE INDEX IF NOT EXISTS idx_infra_environment_project ON infra_environment(project_id);

-- ============================================
-- Deployment Indexes
-- ============================================

-- Index for deployment by agent instance
CREATE INDEX IF NOT EXISTS idx_deployment_agent ON deployment(agent_instance_id);

-- Index for deployment by status
CREATE INDEX IF NOT EXISTS idx_deployment_status ON deployment(status);

-- Composite index for deployment history queries
CREATE INDEX IF NOT EXISTS idx_deployment_agent_created ON deployment(agent_instance_id, created_at);

-- ============================================
-- Audit & System Logs
-- ============================================

-- Index for audit log by resource type and id
CREATE INDEX IF NOT EXISTS idx_audit_log_resource ON audit_log(resource_type, resource_id);

-- Index for system event log by type
CREATE INDEX IF NOT EXISTS idx_system_event_log_type ON system_event_log(event_type);

-- Composite index for system event log queries (type + created_at)
CREATE INDEX IF NOT EXISTS idx_system_event_log_type_created ON system_event_log(event_type, created_at);

-- ============================================
-- User & Session Indexes
-- ============================================

-- Index for user session by token
CREATE INDEX IF NOT EXISTS idx_user_session_token ON user_session(token);

-- Index for user preference by key
CREATE INDEX IF NOT EXISTS idx_user_preference_key ON user_preference(preference_key);

-- ============================================
-- Alert System Indexes
-- ============================================

-- Composite index for alert event queries (environment + status + created_at)
CREATE INDEX IF NOT EXISTS idx_alert_event_env_status_created ON alert_event(environment_id, status, created_at);

-- Index for alert rule by metric
CREATE INDEX IF NOT EXISTS idx_alert_rule_metric_enabled ON alert_rule(metric, enabled);

-- ============================================
-- Update Statistics
-- ============================================

-- Analyze all tables to update query planner statistics
ANALYZE plugin;
ANALYZE plugin_review;
ANALYZE plugin_version;
ANALYZE plugin_comment;
ANALYZE plugin_installation;
ANALYZE agent_instance;
ANALYZE agent_task;
ANALYZE infra_host;
ANALYZE infra_environment;
ANALYZE deployment;
ANALYZE audit_log;
ANALYZE system_event_log;
ANALYZE user_session;
ANALYZE alert_event;
ANALYZE alert_rule;
