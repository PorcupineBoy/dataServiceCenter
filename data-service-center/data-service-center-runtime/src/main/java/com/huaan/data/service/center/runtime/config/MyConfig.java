package com.huaan.data.service.center.runtime.config;

import com.huaan.data.service.center.share.domain.ESinfo;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class MyConfig {

    private RestHighLevelClient restHighLevelClient;

    public static RestHighLevelClient getEsConnectionClient(ESinfo esinfo){
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(esinfo.getUsername(), esinfo.getPassword()));

        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(esinfo.getIp(), esinfo.getPort(), "http")).setHttpClientConfigCallback(
                httpAsyncClientBuilder -> {
                    httpAsyncClientBuilder.disableAuthCaching();
                    return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
        ));
        return client;
    }
}

