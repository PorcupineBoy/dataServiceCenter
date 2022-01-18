package com.huaan.data.service.center.share.domain;

import com.alibaba.datax.common.element.*;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.ConfigParser;
import com.alibaba.datax.common.util.FrameworkErrorCode;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import com.alibaba.datax.plugin.rdbms.util.RdbmsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Reader extends BaseObject {


    public final byte[] EMPTY_CHAR_ARRAY = {};

    public String mandatoryEncoding;

    public Class<? extends Record> RECORD_CLASS;

    public Reader(String filePath) {
        filterConfiguration(ConfigParser.parse(filePath));
        dBinfo = new DBinfo(configuration, OperateType.READER.getValue());
        createConnection();
    }

    public Reader(DBinfo info) {
        dBinfo = info;
        createConnection();
    }

    /**
     * @param sql 无需预编译sql
     * @return
     * @throws DataXException
     * @throws SQLException
     */
    public List<Record> query(String sql) throws DataXException, SQLException {
        List<Record> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            long startTime = System.currentTimeMillis();

            rs = com.alibaba.datax.plugin.rdbms.util.DBUtil.query(getConnection(), sql);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnNumber = metaData.getColumnCount();

            while (rs.next()) {
                list.add(transportOneRecord(rs, metaData, columnNumber, mandatoryEncoding));
            }
            log.debug("查询 {} 条记录，耗时：{} ms", list.size(), (System.currentTimeMillis() - startTime));

        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw RdbmsException.asQueryException(dBinfo.getDataBaseType(), e, sql, dBinfo.getTable(), dBinfo.getUsername());
        } finally {
            DBUtil.closeDBResources(rs, null, connection);
        }

        return list;
    }

    /**
     * @param sql  预编译sql  如:select id,user_name,age from ? where name = ?
     * @param args 预编译数据  如: userTable,张三
     *             入参为: query(sql,["userTable","张三"]) 实际查询sql 为   select id,user_name,age from userTable where name = '张三'
     * @return
     * @throws DataXException
     * @throws SQLException
     */
    public List<Record> query(String sql, List args) throws DataXException, SQLException {
        List<Record> list = new ArrayList<>();
        ResultSet rs = null;
        try {
            long startTime = System.currentTimeMillis();
            rs = com.alibaba.datax.plugin.rdbms.util.DBUtil.query(getConnection(), sql, args);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnNumber = metaData.getColumnCount();

            while (rs.next()) {
                list.add(transportOneRecord(rs, metaData, columnNumber, mandatoryEncoding));
            }
            log.debug("查询 {} 条记录，耗时：{} ms", list.size(), (System.currentTimeMillis() - startTime));

        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw RdbmsException.asQueryException(dBinfo.getDataBaseType(), e, sql, dBinfo.getTable(), dBinfo.getUsername());
        } finally {
            com.alibaba.datax.plugin.rdbms.util.DBUtil.closeDBResources(rs, null, connection);
        }
        return list;
    }

    public Record createRecord() {
        try {
            RECORD_CLASS = (Class<? extends Record>) Class.forName("com.alibaba.datax.common.core.transport.record.DefaultRecord");
            return (Record) RECORD_CLASS.newInstance();
        } catch (Exception var2) {
            throw DataXException.asDataXException(FrameworkErrorCode.CONFIG_ERROR, var2);
        }
    }

    public Record transportOneRecord(ResultSet rs, ResultSetMetaData metaData, int columnNumber, String mandatoryEncoding) throws DataXException {
        return buildRecord(rs, metaData, columnNumber, mandatoryEncoding);
    }

    public Record buildRecord(ResultSet rs, ResultSetMetaData metaData, int columnNumber, String mandatoryEncoding) throws DataXException {
        Record record = createRecord();

        try {
            for (int i = 1; i <= columnNumber; i++) {
                switch (metaData.getColumnType(i)) {

                    case Types.CHAR:
                    case Types.NCHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NVARCHAR:
                    case Types.LONGNVARCHAR:
                        String rawData;
                        if (StringUtils.isBlank(mandatoryEncoding)) {
                            rawData = rs.getString(i);
                        } else {
                            rawData = new String((rs.getBytes(i) == null ? EMPTY_CHAR_ARRAY :
                                    rs.getBytes(i)), mandatoryEncoding);
                        }
                        record.addColumn(new StringColumn(rawData, metaData.getColumnName(i)));
                        break;

                    case Types.CLOB:
                    case Types.NCLOB:
                        record.addColumn(new StringColumn(rs.getString(i), metaData.getColumnName(i)));
                        break;

                    case Types.SMALLINT:
                    case Types.TINYINT:
                    case Types.INTEGER:
                    case Types.BIGINT:
                        record.addColumn(new LongColumn(rs.getString(i), metaData.getColumnName(i)));
                        break;

                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        record.addColumn(new DoubleColumn(rs.getString(i), metaData.getColumnName(i)));
                        break;

                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                        record.addColumn(new DoubleColumn(rs.getString(i), metaData.getColumnName(i)));
                        break;

                    case Types.TIME:
                        DateColumn timeColumn = new DateColumn(rs.getTime(i), metaData.getColumnName(i));
                        String timeStr = (ColumnCast.date2String(timeColumn));
                        record.addColumn(new StringColumn(timeStr, metaData.getColumnName(i)));
                        break;

                    // for mysql bug, see http://bugs.mysql.com/bug.php?id=35115
                    case Types.DATE:
                        if (metaData.getColumnTypeName(i).equalsIgnoreCase("year")) {
                            record.addColumn(new LongColumn(rs.getInt(i), metaData.getColumnName(i)));
                        } else {
                            record.addColumn(new DateColumn(rs.getDate(i), metaData.getColumnName(i)));
                        }
                        break;

                    case Types.TIMESTAMP:
                        DateColumn dateColumn = new DateColumn(rs.getTimestamp(i), metaData.getColumnName(i));
                        String dateStr = (ColumnCast.date2String(dateColumn));
                        record.addColumn(new StringColumn(dateStr, metaData.getColumnName(i)));
                        break;

                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.BLOB:
                    case Types.LONGVARBINARY:
                        record.addColumn(new BytesColumn(rs.getBytes(i), metaData.getColumnName(i)));
                        break;

                    // warn: bit(1) -> Types.BIT 可使用BoolColumn
                    // warn: bit(>1) -> Types.VARBINARY 可使用BytesColumn
                    case Types.BOOLEAN:
                    case Types.BIT:
                        record.addColumn(new BoolColumn(rs.getBoolean(i), metaData.getColumnName(i)));
                        break;

                    case Types.NULL:
                        String stringData = null;
                        if (rs.getObject(i) != null) {
                            stringData = rs.getObject(i).toString();
                        }
                        record.addColumn(new StringColumn(stringData, metaData.getColumnName(i)));
                        break;

                    default:
                        throw DataXException
                                .asDataXException(
                                        DBUtilErrorCode.UNSUPPORTED_TYPE,
                                        String.format(
                                                "您的配置文件中的列配置信息有误. 因为DataX 不支持数据库读取这种字段类型. 字段名:[%s], 字段名称:[%s], 字段Java类型:[%s]. 请尝试使用数据库函数将其转换datax支持的类型 或者不同步该字段 .",
                                                metaData.getColumnName(i),
                                                metaData.getColumnType(i),
                                                metaData.getColumnClassName(i)));
                }
            }
        } catch (Exception var11) {
            log.error("{}", var11.getMessage());
        }

        return record;
    }

}
