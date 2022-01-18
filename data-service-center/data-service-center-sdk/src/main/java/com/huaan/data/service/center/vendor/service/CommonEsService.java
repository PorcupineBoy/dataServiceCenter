package com.huaan.data.service.center.vendor.service;

import com.alibaba.datax.common.element.Record;
import com.alibaba.fastjson.JSONObject;
import com.huaan.data.service.center.share.domain.DBinfo;
import com.huaan.data.service.center.share.domain.ESinfo;

import java.util.List;

public interface CommonEsService <T>{
    /**
     * 推荐使用预编译模式进行入参
     * @param dBinfo
     * @return
     */
    List<Record> queryRecordList(ESinfo dBinfo) ;


    /**
     * 转换成jsonObject
     * @param dBinfo
     * @return
     */

    List<JSONObject> getRecordList(ESinfo dBinfo);

    /**
     * 转换为对应实体类
     * @param dBinfo db信息
     * @param className 实体类
     * @return
     */
    List<T> getRecordList(ESinfo dBinfo,Class<T> className);

    /**
     *
     */
    boolean doTestDb(ESinfo dBinfo);
}
