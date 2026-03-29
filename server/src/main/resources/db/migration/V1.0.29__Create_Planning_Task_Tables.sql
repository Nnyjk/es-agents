-- 任务规划表
CREATE TABLE planning_task (
    id UUID PRIMARY KEY,
    goal_id UUID,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    priority_value INTEGER,
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    parent_task_id UUID REFERENCES planning_task(id) ON DELETE CASCADE,
    depth INTEGER NOT NULL DEFAULT 0,
    estimated_duration_seconds BIGINT,
    actual_duration_seconds BIGINT,
    parameters TEXT,
    result TEXT,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry_count INTEGER NOT NULL DEFAULT 3,
    executor_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- 任务依赖关系表
CREATE TABLE planning_task_dependency (
    id UUID PRIMARY KEY,
    depends_on_task_id UUID NOT NULL REFERENCES planning_task(id) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES planning_task(id) ON DELETE CASCADE,
    dependency_type VARCHAR(50) NOT NULL DEFAULT 'HARD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 任务执行日志表
CREATE TABLE planning_task_execution_log (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES planning_task(id) ON DELETE CASCADE,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    message TEXT,
    executed_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_planning_task_goal_id ON planning_task(goal_id);
CREATE INDEX idx_planning_task_status ON planning_task(status);
CREATE INDEX idx_planning_task_priority ON planning_task(priority_value);
CREATE INDEX idx_planning_task_parent_id ON planning_task(parent_task_id);
CREATE INDEX idx_planning_task_depth ON planning_task(depth);
CREATE INDEX idx_planning_task_executor_type ON planning_task(executor_type);
CREATE INDEX idx_planning_task_created_at ON planning_task(created_at);

CREATE INDEX idx_planning_task_depends_on ON planning_task_dependency(depends_on_task_id);
CREATE INDEX idx_planning_task_dep_task ON planning_task_dependency(task_id);
CREATE UNIQUE INDEX idx_planning_task_dep_unique ON planning_task_dependency(task_id, depends_on_task_id);

CREATE INDEX idx_planning_task_log_task_id ON planning_task_execution_log(task_id);
CREATE INDEX idx_planning_task_log_to_status ON planning_task_execution_log(to_status);
CREATE INDEX idx_planning_task_log_created_at ON planning_task_execution_log(created_at);

-- 注释
COMMENT ON TABLE planning_task IS '任务规划表，存储Agent任务规划的基本信息，支持多层分解和依赖关系';
COMMENT ON COLUMN planning_task.goal_id IS '目标ID，关联同一目标的任务';
COMMENT ON COLUMN planning_task.description IS '任务描述';
COMMENT ON COLUMN planning_task.status IS '任务状态：CREATED, DECOMPOSING, READY, SCHEDULED, RUNNING, COMPLETED, FAILED, CANCELLED, PAUSED, RETRYING';
COMMENT ON COLUMN planning_task.priority_value IS '任务优先级数值(1-100)';
COMMENT ON COLUMN planning_task.priority IS '优先级枚举：LOW, NORMAL, HIGH, URGENT';
COMMENT ON COLUMN planning_task.parent_task_id IS '父任务ID，用于多层分解';
COMMENT ON COLUMN planning_task.depth IS '任务深度层级，根任务为0';
COMMENT ON COLUMN planning_task.executor_type IS '执行器类型标识';

COMMENT ON TABLE planning_task_dependency IS '任务依赖关系表，定义任务之间的依赖关系';
COMMENT ON COLUMN planning_task_dependency.depends_on_task_id IS '被依赖的任务（前置任务）';
COMMENT ON COLUMN planning_task_dependency.task_id IS '依赖的任务（后置任务）';
COMMENT ON COLUMN planning_task_dependency.dependency_type IS '依赖类型：HARD（必须完成）或 SOFT（期望完成）';

COMMENT ON TABLE planning_task_execution_log IS '任务执行日志表，记录任务状态变更和执行过程';
COMMENT ON COLUMN planning_task_execution_log.from_status IS '状态变更前的状态';
COMMENT ON COLUMN planning_task_execution_log.to_status IS '状态变更后的状态';
COMMENT ON COLUMN planning_task_execution_log.executed_by IS '执行者';