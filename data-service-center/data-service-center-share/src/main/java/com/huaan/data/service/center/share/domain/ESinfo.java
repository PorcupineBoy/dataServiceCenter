package com.huaan.data.service.center.share.domain;

import com.huaan.data.service.center.share.domain.DBinfo;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class ESinfo extends DBinfo {
    private String index;
    private String indexType;
    /**
     * 查询ES的source 条件
     * 如果涉及多曾嵌套，则使用. 往下嵌套
     * 如ES的source 数据如下：
     * {
     *         "clientId": "22",
     *         "tags": {
     *             "richMan": "1"
     *         }
     * }
     * 则查询结构体为：
     * "condition":{"tags.richMan": "1","clientId":"22"}
     */
    private HashMap<String,Object> condition;
    /**
     * IP端口
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;

    private String clusterNodes;
}
