package com.hazq.data.service.starter.properties;


import com.hazq.data.service.starter.config.LoadProperties;
import com.huaan.data.service.center.share.domain.ESinfo;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;

import java.io.IOException;

public class EsInfoConfiguration {
    private static ESinfo esinfo =null;

    public  static ESinfo getEsinfoInstance(String propertiesPath) throws IOException {
        if(esinfo==null) {
            if (propertiesPath == null) {
                LoadProperties.setProperties();
            } else {
                LoadProperties.setProperties(propertiesPath);
            }
            esinfo=new ESinfo();
            esinfo.setIndex(LoadProperties.getProperties().getProperty("index"));
            esinfo.setUsername(LoadProperties.getProperties().getProperty("username"));
            esinfo.setPassword(LoadProperties.getProperties().getProperty("password"));
            esinfo.setJdbcUrl(LoadProperties.getProperties().getProperty("jdbcurl"));
            esinfo.setIndexType(LoadProperties.getProperties().getProperty("indextype"));
        }
        return esinfo;
    }
}
