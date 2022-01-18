package com.huaan.data.service.center.share.domain;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.StringUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.huaan.data.service.center.share.model.DataCenterErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class CommonDBReader extends Reader {

    /**
     * sql过滤 内容  |线 为分隔符
     * 禁止添加，修改，删除
     */
    String sqlInjectStrList="insert|update|delete|grant|drop" ;

    public CommonDBReader(String filePath) {
        super(filePath);
    }

    public CommonDBReader(DBinfo dBinfo) {
        super(dBinfo);
    }

    /**
     * 校验SQL
     *
     * @param str
     * @param sqlInjectStrList
     * @return
     */
    protected static boolean sqlValidate(String str, String sqlInjectStrList) {
// 统一转为小写
        str = str.toLowerCase();
// 转换为数组
        String[] badStrs = sqlInjectStrList.split("\\|");
        for (int i = 0; i < badStrs.length; i++) {
// 检索
            if (str.contains(badStrs[i])) {
                return true;
            }
        }
        return false;

    }

    /**
     * 支持三种查询方式
     * 1、列名、表名，where 条件
     * 2、完整SQL语句执行
     * 3、预编译SQL+编译值 执行
     *
     * @return
     * @throws SQLException
     * @throws RuntimeException
     */
    public List<Record> queryRecordList() throws SQLException, RuntimeException {
        String sql = dBinfo.getQuerySql();

        if (StringUtils.isEmpty(sql)) {
            List<String> columns = dBinfo.getColumns();
            Assert.notEmpty(columns, "列名不能为空");
            sql = QUERY_SQL_TEMPLATE.replaceFirst("%s", StringUtil.replace(columns.toString()))
                    .replaceFirst("%s", dBinfo.getTable())
                    .replaceFirst("%s", dBinfo.getWhere());
        }
        log.debug("【queryRecordList】：【{}】", sql);
        if (sqlValidate(sql, sqlInjectStrList)) {
            throw DataXException.asDataXException(DataCenterErrorCode.ILLEGAL_SQL_ERROR, "数据服务sql只能查询，无法进行其他操作");
        }
        DBUtil.sqlValid(sql,dBinfo.getDataBaseType());
        if (CollectionUtil.isNotEmpty(dBinfo.getPrepareValue()) && sql.contains("?")) {
            return query(sql, dBinfo.getPrepareValue());
        }
        return query(sql);

    }


}
