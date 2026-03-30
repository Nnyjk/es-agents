package com.easystation.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时序数据点 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesData {
    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 值
     */
    private double value;

    /**
     * 指标名称
     */
    private String metric;
}