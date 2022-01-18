package com.huaan.data.service.center.vendor.service;


import com.alibaba.datax.common.element.Record;
import com.alibaba.fastjson.JSONObject;
import com.huaan.data.service.center.share.domain.DBinfo;


import java.util.List;

public interface CommonDBService<T> {
    /**
     * 推荐使用预编译模式进行入参
     * @param dBinfo
     * @return
     */
    List<Record> queryRecordList(DBinfo dBinfo) ;


    /**
     * 转换成jsonObject
     * @param dBinfo
     * @return
     */

    List<JSONObject> getRecordList(DBinfo dBinfo);

    /**
     * 转换为对应实体类
     * @param dBinfo db信息
     * @param className 实体类
     * @return
     */
    List<T> getRecordList(DBinfo dBinfo,Class<T> className);

    /**
     *
     */
     boolean doTestDb(DBinfo dBinfo);
}
