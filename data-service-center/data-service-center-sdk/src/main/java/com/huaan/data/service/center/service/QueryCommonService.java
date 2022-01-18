package com.huaan.data.service.center.service;

import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.List;

public interface QueryCommonService {
    /**
     * 查询服务
     * @param tenantId  租户ID
     * @param inputParam 入参
     * @param config   查询配置
     * @return
     */
    List<HashMap> queryByCondition(Long tenantId,HashMap inputParam,HashMap config);

    Configuration buildDbConfig(String config);

}
