package com.easystation.deployment.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResultDTO<T> {
    public List<T> list;
    public Long total;
    public Integer pageNum;
    public Integer pageSize;
}