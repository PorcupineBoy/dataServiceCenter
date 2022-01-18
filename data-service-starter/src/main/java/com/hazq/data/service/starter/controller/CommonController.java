package com.hazq.data.service.starter.controller;


import com.hazq.data.service.starter.model.CustomerInfo;
import com.huaan.data.service.center.share.domain.DBinfo;
import com.huaan.data.service.center.share.domain.ESinfo;
import com.huaan.data.service.center.share.model.ApiResult;
import com.huaan.data.service.center.vendor.service.CommonDBService;
import com.huaan.data.service.center.vendor.service.CommonEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommonController {

    @Autowired
    private CommonDBService commonDBService;
    @Autowired
    private CommonEsService commonEsService;

    /**
     * @param dBinfo
     * @return
     */
    @PostMapping("/queryRecordList")
    public ResponseEntity getRecordList(@RequestBody DBinfo dBinfo, HttpRequest request) {
        HttpRequest httpRequest = request;
        List<CustomerInfo> results = commonDBService.getRecordList(dBinfo, CustomerInfo.class);
        return ResponseEntity.ok(results);
    }

    /**
     * @param dBinfo
     * @return
     */
    @PostMapping("/doTestDb")
    public ApiResult doTestDb(@RequestBody DBinfo dBinfo) {
        boolean testDb = true;
        try {
            testDb = commonDBService.doTestDb(dBinfo);

        } catch (Exception e) {
            ApiResult.fail("500", e.getMessage());
        }
        return ApiResult.success(testDb);
    }

    @PostMapping("/doTestEs")
    public ApiResult doTestES(@RequestBody ESinfo dBinfo) {
        boolean testDb = true;
        try {
            testDb = commonEsService.doTestDb(dBinfo);

        } catch (Exception e) {
            ApiResult.fail("500", e.getMessage());
        }
        return ApiResult.success(testDb);
    }

    /**
     * @param dBinfo
     * @return
     */
    @RequestMapping("/queryRecordListInEs")
    public ResponseEntity getRecordListInEs(@RequestBody ESinfo dBinfo) {
        return ResponseEntity.ok(commonEsService.getRecordList(dBinfo));
    }
}
