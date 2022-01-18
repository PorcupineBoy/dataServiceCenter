package com.huaan.data.service.center.runtime.vendor.service;

import com.alibaba.datax.common.element.Record;
import com.alibaba.fastjson.JSONObject;
import com.huaan.data.service.center.runtime.config.EsConfigurationConfig;
import com.huaan.data.service.center.runtime.config.MyConfig;
import com.huaan.data.service.center.share.domain.ESinfo;
import com.huaan.data.service.center.vendor.service.CommonEsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class CommonEsServiceImpl implements CommonEsService {



    /**
     * 推荐使用预编译模式进行入参
     *
     * @param dBinfo
     * @return
     */
    @Override
    public List<Record> queryRecordList(ESinfo dBinfo) {
        return null;
    }

    /**
     * 转换成jsonObject
     *
     * @param dbInfo
     * @return
     */
    @Override
    public List<JSONObject> getRecordList(ESinfo dbInfo) {

        EsConfigurationConfig esConfigurationConfig = new EsConfigurationConfig(dbInfo);
        List<JSONObject> list = new ArrayList<>();
        try {

            RestHighLevelClient restHighLevelClient=
                    esConfigurationConfig.createInstance();
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            SearchRequest searchRequest = new SearchRequest(dbInfo.getIndex());

            dbInfo.getCondition().forEach((key,value)->{
                TermQueryBuilder termTerminalBuilder = QueryBuilders.termQuery(key, value);
                boolBuilder.must(termTerminalBuilder);
            });
            sourceBuilder.query(boolBuilder);
            searchRequest.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                list.add((JSONObject) JSONObject.parse(hit.getSourceAsString()));
                log.info("search -> {}", hit.getSourceAsString());
            }
            return list;
        } catch (IOException e) {
            log.info(e.getMessage());
            return Collections.EMPTY_LIST;
        }finally {
            esConfigurationConfig.destroy();
        }

    }

    /**
     * 转换为对应实体类
     *
     * @param dBinfo    db信息
     * @param className 实体类
     * @return
     */
    @Override
    public List getRecordList(ESinfo dBinfo, Class className) {
        return null;
    }

    /**
     * @param dBinfo
     */
    @Override
    public boolean doTestDb(ESinfo dBinfo) {
        try {
            String[] data = dBinfo.getJdbcUrl().split(":");
            RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(data[0], Integer.parseInt(data[1]), "http")));
            GetAliasesRequest searchRequest = new GetAliasesRequest();
            GetAliasesResponse getAliasesResponse = client.indices().getAlias(searchRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            log.info(e.getMessage()) ;
            return false;
        }

    }

}
