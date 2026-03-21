package com.easystation.agent.metrics.enums;

/**
 * 指标类型
 */
public enum MetricType {
    // 主机指标
    HOST_CPU_USAGE("主机CPU使用率", "%"),
    HOST_MEMORY_USAGE("主机内存使用率", "%"),
    HOST_DISK_USAGE("主机磁盘使用率", "%"),
    HOST_DISK_IO_READ("主机磁盘读取速率", "MB/s"),
    HOST_DISK_IO_WRITE("主机磁盘写入速率", "MB/s"),
    HOST_NETWORK_IN("主机网络入流量", "MB/s"),
    HOST_NETWORK_OUT("主机网络出流量", "MB/s"),
    HOST_LOAD_1("主机1分钟负载", ""),
    HOST_LOAD_5("主机5分钟负载", ""),
    HOST_LOAD_15("主机15分钟负载", ""),

    // Agent 进程指标
    AGENT_CPU_USAGE("Agent进程CPU使用率", "%"),
    AGENT_MEMORY_USAGE("Agent进程内存使用率", "%"),
    AGENT_MEMORY_RSS("Agent进程RSS内存", "MB"),
    AGENT_UPTIME("Agent运行时长", "s"),
    AGENT_THREAD_COUNT("Agent线程数", ""),
    AGENT_CONNECTION_COUNT("Agent连接数", ""),

    // Agent 业务指标
    AGENT_TASK_TOTAL("Agent任务总数", ""),
    AGENT_TASK_SUCCESS("Agent成功任务数", ""),
    AGENT_TASK_FAILED("Agent失败任务数", ""),
    AGENT_HEARTBEAT_COUNT("Agent心跳次数", ""),
    AGENT_COMMAND_EXECUTED("Agent已执行命令数", "");

    private final String description;
    private final String unit;

    MetricType(String description, String unit) {
        this.description = description;
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }
}