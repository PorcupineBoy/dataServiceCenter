package com.hazq.data.service.starter.controller;

import com.hazq.data.service.starter.service.impl.UserTagSync;
import com.huaan.data.service.center.share.model.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequestMapping
@RestController
@Slf4j
@EnableScheduling
public class AutoWriteUserTagController {
    @Value("${service.auto.registry.enabled:true}")
    private String ENABLED;

    @Autowired
    private UserTagSync userTagSync;

    @Scheduled(cron = "0 */1 * * * ?")
    @RequestMapping("/user/tag/auto_Sync")
    public ApiResult autoSync(){

        if (ENABLED.equalsIgnoreCase(Boolean.TRUE.toString())) {
            try {
                userTagSync.autoSync();
            } catch (Exception e) {
                log.error("service automatic registration had failed:", e);
                return ApiResult.fail("500",e.getMessage());
            }
            log.info("service automatic registration is success。current time is:{}", new Date());
            return ApiResult.success();
        }
        log.error("service automatic registration is failed。you should make autoRegister enable .current time is:{}", new Date());
        return ApiResult.fail("403","用户标签同步功能为开启");
    }
}
