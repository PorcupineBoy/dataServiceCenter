package com.huaan.data.service.center.constan;

public class ConstantSql {
    public static String QUERY_SQL_TAG = "SELECT\n" +
            "\ttag.tag_operation_rule,\n" +
            "\tattribute.attr_code,\n" +
            "\tattribute.attr_name \n" +
            "FROM\n" +
            "\tsdp_data_tag tag\n" +
            "left join sdp_data_asset_table_attribute attribute on tag.table_attribute_id = attribute .id";

    /**
     * 获取用户ID集合
     */
    public static String QUERY_SQL_USER_ID_LIST = "SELECT DISTINCT client_id  FROM  customer_info limit 0,2000000";

    public static String QUERY_SQL_TARGETS = "Select target.id, table_a.attr_code ,table_a.attr_type, table_a.attr_name ,target.target_operation_rule \n" +
            "from sdp_data_target as target\n" +
            "Left join sdp_data_asset_target_attribute as a on  a.table_attribute_id =target.target_attribute_id\n" +
            "Left join sdp_data_asset_table_attribute table_a  on table_a.id = a.table_attribute_id";


    public static String QUERY_SQL_TEMPLATE = "SELECT %s FROM %s WHERE ( %s )";
    public static String TEST_DB_SQL = "SELECT 1 FROM DUAL ";
    public static String DEFAULT_USER_SOURCE_TABLE = " CUSTOMER_INFO C LEFT JOIN customer_trade_statistics S ON C.CLIENT_ID = S.CLIENT_ID  ";

}
