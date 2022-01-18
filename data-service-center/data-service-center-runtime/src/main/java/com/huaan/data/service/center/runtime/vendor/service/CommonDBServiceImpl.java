package com.huaan.data.service.center.runtime.vendor.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.fastjson.JSONObject;
import com.huaan.data.service.center.share.domain.CommonDBReader;
import com.huaan.data.service.center.share.domain.DBinfo;
import com.huaan.data.service.center.vendor.service.CommonDBService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CommonDBServiceImpl implements CommonDBService {
    CommonDBReader dbReader;

    @SneakyThrows
    @Override
    public List<Record> queryRecordList(DBinfo dBinfo) {
        List<Record> records = null;
        try {
            dbReader = new CommonDBReader(dBinfo);
            records = dbReader.queryRecordList();
        } catch (RuntimeException | SQLException exception) {
            log.error(exception.getMessage());
            throw exception;
        }
        return records;
    }

    /**
     * 转换成jsonObject
     *
     * @param dBinfo
     * @param className
     * @return
     */
    @Override
    public List<Class> getRecordList(DBinfo dBinfo, Class className) {
        List<JSONObject> records = getRecordList(dBinfo);
        return BeanUtil.copyToList(records, className);
    }

    /**
     * @param dBinfo
     */
    @Override
    public boolean doTestDb(DBinfo dBinfo) {
        return DBUtil.testConnWithoutRetry(dBinfo.getDataBaseType(), dBinfo.getJdbcUrl(), dBinfo.getUsername(), dBinfo.getPassword(), false);
    }

    /**
     * 转换成jsonObject
     *
     * @param dBinfo
     * @return
     */
    @Override
    public List<JSONObject> getRecordList(DBinfo dBinfo) {
        List<Record> records = queryRecordList(dBinfo);
        if (CollectionUtil.isNotEmpty(records)) {
            List<JSONObject> results = new ArrayList<>(records.size());
            records.forEach(o -> results.add(o.toJsonObject()));
            return results;
        }
        return Collections.EMPTY_LIST;
    }


}
