package com.huaan.data.service.center.share.config;


import com.alibaba.datax.common.util.Configuration;
import com.huaan.data.service.center.share.domain.DBinfo;

public class DefaultDBinfo {
    String dbInfo="";
    public static DBinfo buildDefaultDBinfo() {
        String defaultConfig = "{\n" +
                "    \"username\":\"root\",\n" +
                "    \"password\":\"chenks\",\n" +
                "    \"databaseType\":\"mysql\",\n" +
                "    \"querySql\":\"select  1 from dual\" ,\n" +
                "    \"prepareValue\":null,\n" +
                "                \"jdbcUrl\": \"jdbc:mysql://127.0.0.1:3306/data-service-center?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true\"\n" +
                "}";
        Configuration configuration = Configuration.from(defaultConfig);
        return new DBinfo(configuration);
    }
    public static Configuration buildEsConfiguration(){
        String defaultConfig = "{\"cleanup\": false,\n" +
                "            \"settings\": {\"index\" :{\"number_of_shards\": 1, \"number_of_replicas\": 0}},\n" +
                "            \"discovery\": false,\n" +
                "            \"batchSize\": 1000,\n" +
                "            \"splitter\": \",\"" +
                " }";
        return Configuration.from(defaultConfig);
    }
}
