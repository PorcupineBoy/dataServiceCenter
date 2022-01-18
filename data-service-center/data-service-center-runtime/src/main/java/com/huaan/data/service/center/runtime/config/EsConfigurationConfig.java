package com.huaan.data.service.center.runtime.config;

import com.huaan.data.service.center.share.domain.ESinfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;

@Slf4j
public class EsConfigurationConfig extends AbstractFactoryBean
{
    public static int CONNECT_TIMEOUT_MILLIS = 5000;
    public static int SOCKET_TIMEOUT_MILLIS = 30000;
    public static int CONNECTION_REQUEST_TIMEOUT_MILLIS = 5000;
    public static int MAX_CONN_PER_ROUTE = 10;
    public static int MAX_CONN_TOTAL = 30;



            public EsConfigurationConfig(ESinfo eSinfo){
                this.elasticSearchProperties=eSinfo;
            }

        private ESinfo elasticSearchProperties;

        private RestHighLevelClient client;
        static final String COLON = ":";
        static final String COMMA = ",";

        @Override
        public void destroy() {
            try {
                log.info("Closing elasticSearch  client");
                if (client != null) {
                    client.close();
                }
            } catch (final Exception e) {
                log.error("Error closing ElasticSearch client: ", e);
            }
        }

        @Override
        public Class<RestHighLevelClient> getObjectType() {
            return RestHighLevelClient.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public RestHighLevelClient createInstance() throws IOReactorException {
            return buildClient();
        }

        private RestHighLevelClient buildClient() throws IOReactorException {

            Assert.hasText(elasticSearchProperties.getClusterNodes(), "[Assertion failed] clusterNodes settings missing.");
            /**
             * clusterNodes: 127.0.0.1:9200,192.168.0.1:9200
             */
            String[] nodes = (elasticSearchProperties.getClusterNodes().split(COMMA) );
            HttpHost[] hosts = new HttpHost[nodes.length];
            for (int i = 0, j = nodes.length; i < j; i++) {
                String hostName = nodes[i].split(COLON)[0];//substringBeforeLast(nodes[i], COLON);
                String port = nodes[i].split(COLON)[1];  //substringAfterLast(nodes[i], COLON);
                Assert.hasText(hostName, "[Assertion failed] missing host name in 'clusterNodes'");
                Assert.hasText(port, "[Assertion failed] missing port in 'clusterNodes'");
                log.info("adding transport node : " + nodes[i]);
                hosts[i] = new HttpHost(hostName, Integer.valueOf(port));
            }

            final IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(10).setConnectTimeout(10).setRcvBufSize(5).setSoKeepAlive(true).build();
            final PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(new
                    DefaultConnectingIOReactor(ioReactorConfig));
            connManager.setMaxTotal(100);
            connManager.setDefaultMaxPerRoute(100);
            /**
             * 创建ES授权，取消账号密码密码缓存
             */
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(elasticSearchProperties.getUsername(),
                            elasticSearchProperties.getPassword()));
            RestClientBuilder builder = RestClient.builder(hosts).setHttpClientConfigCallback(callback -> {
                // 异步httpclient连接数配置
                callback.setMaxConnTotal(MAX_CONN_PER_ROUTE);
                callback.setMaxConnPerRoute(MAX_CONN_TOTAL);
                callback.disableAuthCaching();
                return callback.setKeepAliveStrategy((response, context) -> {
                    Args.notNull(response, "HTTP response");
                    final HeaderElementIterator it = new BasicHeaderElementIterator(
                            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    while (it.hasNext()) {
                        final HeaderElement he = it.nextElement();
                        final String param = he.getName();
                        final String value = he.getValue();
                        if (value != null && param.equalsIgnoreCase("timeout")) {
                            try {
                                return Long.parseLong(value) * 1000;
                            } catch (final NumberFormatException ignore) {
                            }
                        }
                    }
                    return 10 * 1000;
                }).setDefaultCredentialsProvider(credentialsProvider).setConnectionManager(connManager);
            });
            // 异步httpclient连接延时配置
            builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                @Override
                public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                    builder.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
                    builder.setSocketTimeout(SOCKET_TIMEOUT_MILLIS);
                    builder.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS);
                    return builder;
                }
            });
            // 异步httpclient连接数配置

            client = new RestHighLevelClient(builder);
            return client;
        }

    }


