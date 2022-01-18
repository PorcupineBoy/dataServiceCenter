package com.huaan.data.service.center.share.domain;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.common.util.RetryUtil;
import com.alibaba.datax.plugin.writer.elasticsearchwriter.*;
import com.alibaba.fastjson.JSONObject;
import com.huaan.data.service.center.share.config.DefaultDBinfo;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommonESWriter extends CommonWriter {


    public static class Task extends CommonWriter.Task {
        public Task(ESinfo eSinfo){
            this.conf = DefaultDBinfo.buildEsConfiguration();
            conf.set("endpoint",eSinfo.getJdbcUrl());
            conf.set("accessId",eSinfo.getUsername());
            conf.set("accessKey",eSinfo.getPassword());
            conf.set("index",eSinfo.getIndex());
            conf.set("type",eSinfo.getIndexType());
        }
        private static final Logger log = LoggerFactory.getLogger(Writer.Job.class);
        ESClient esClient = null;
        private Configuration conf;
        private List<ESFieldType> typeList;
        private List<ESColumn> columnList;

        private int trySize;
        private int batchSize;
        private String index;
        private String type;
        private String splitter;

        @Override
        public void init() {

            index = Key.getIndexName(conf);
            type = Key.getTypeName(conf);
            trySize = Key.getTrySize(conf);
            batchSize = Key.getBatchSize(conf);
            splitter = Key.getSplitter(conf);
            esClient = new ESClient();
        }

        @Override
        public void prepare() {
            esClient.createClient(Key.getEndpoint(conf),
                    Key.getAccessID(conf),
                    Key.getAccessKey(conf),
                    Key.isMultiThread(conf),
                    Key.getTimeout(conf),
                    Key.isCompression(conf),
                    Key.isDiscovery(conf));
        }

        @Override
        public void startWrite(final List<JSONObject> writerBuffer) {
            String size = String.format("task start, need write size :%d", writerBuffer.size());
            log.info(size);
            long total = doBatchInsert(writerBuffer);
            String msg = String.format("task end, write size :%d", total);
            log.info(msg);
            esClient.closeJestClient();
        }


        private long doBatchInsert(final List<JSONObject> writerBuffer) {

            final Bulk.Builder bulkaction = new Bulk.Builder().defaultIndex(this.index).defaultType(this.type);
            for (JSONObject data : writerBuffer) {

                String id = (String) data.get("clientId");

                if (id == null) {
                    //id = UUID.randomUUID().toString();
                    bulkaction.addAction(new Index.Builder(data).build());
                } else {
                    bulkaction.addAction(new Index.Builder(data).id(id).build());
                }
            }

            try {
                return RetryUtil.executeWithRetry(() -> {
                    JestResult jestResult = esClient.bulkInsert(bulkaction, 1);
                    if (jestResult.isSucceeded()) {
                        return writerBuffer.size();
                    }

                    String msg = String.format("response code: [%d] error :[%s]", jestResult.getResponseCode(), jestResult.getErrorMessage());
                    log.warn(msg);
                    if (esClient.isBulkResult(jestResult)) {
                        BulkResult brst = (BulkResult) jestResult;
                        List<BulkResult.BulkResultItem> failedItems = brst.getFailedItems();
                        for (BulkResult.BulkResultItem item : failedItems) {
                            if (item.status != 400) {
                                // 400 BAD_REQUEST  如果非数据异常,请求异常,则不允许忽略
                                throw DataXException.asDataXException(ESWriterErrorCode.ES_INDEX_INSERT, String.format("status:[%d], error: %s", item.status, item.error));
                            } else {
                                // 如果用户选择不忽略解析错误,则抛异常,默认为忽略
                                if (!Key.isIgnoreParseError(conf)) {
                                    throw DataXException.asDataXException(ESWriterErrorCode.ES_INDEX_INSERT, String.format("status:[%d], error: %s, config not ignoreParseError so throw this error", item.status, item.error));
                                }
                            }
                        }

                        List<BulkResult.BulkResultItem> items = brst.getItems();
                        for (int idx = 0; idx < items.size(); ++idx) {
                            BulkResult.BulkResultItem item = items.get(idx);
                            if (item.error != null && !"".equals(item.error)) {
                                //getTaskPluginCollector().collectDirtyRecord(writerBuffer.get(idx), String.format("status:[%d], error: %s", item.status, item.error));
                            }
                        }
                        return writerBuffer.size() - brst.getFailedItems().size();
                    } else {
                        Integer status = esClient.getStatus(jestResult);
                        switch (status) {
                            case 429: //TOO_MANY_REQUESTS
                                log.warn("server response too many requests, so auto reduce speed");
                                break;
                        }
                        throw DataXException.asDataXException(ESWriterErrorCode.ES_INDEX_INSERT, jestResult.getErrorMessage());
                    }
                }, trySize, 60000L, true);
            } catch (Exception e) {
                if (Key.isIgnoreWriteError(this.conf)) {
                    log.warn(String.format("重试[%d]次写入失败，忽略该错误，继续写入!", trySize));
                } else {
                    throw DataXException.asDataXException(ESWriterErrorCode.ES_INDEX_INSERT, e);
                }
            }
            return 0;
        }


        @Override
        public void destroy() {
            esClient.closeJestClient();
        }

    }
}
