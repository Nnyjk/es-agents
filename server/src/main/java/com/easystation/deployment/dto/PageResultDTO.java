package com.easystation.deployment.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResultDTO<T> {
    private List<T> data;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
}