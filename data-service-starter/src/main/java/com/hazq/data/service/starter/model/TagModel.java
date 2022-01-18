package com.hazq.data.service.starter.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TagModel implements Serializable {

    private String attrCode;
    private String attrName;
    private String tagOperationRule;
    private String targetCode;
    private String targetRule;
    private String targetColumn;
    private List<String> targetIds;
    /***
     * 指标对应库表
     */
    private String targetBelong;

    /**
     * 查全量指标 字段， from 原始表。
     * select columns from table left join
     */
}
