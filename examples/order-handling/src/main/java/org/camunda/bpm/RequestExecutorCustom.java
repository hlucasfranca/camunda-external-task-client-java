package org.camunda.bpm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.camunda.bpm.client.impl.RequestExecutor;
import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;

import java.io.IOException;

public class RequestExecutorCustom extends RequestExecutor {

    private HttpRequestRetryHandler handler;

    public RequestExecutorCustom(RequestInterceptorHandler requestInterceptorHandler, ObjectMapper objectMapper, HttpRequestRetryHandler handler) {
        super(requestInterceptorHandler, objectMapper);
        this.handler = handler;
        this.initHttpClient(requestInterceptorHandler);
    }

    @Override
    public void initHttpClient(RequestInterceptorHandler requestInterceptorHandler) {

        if(this.handler == null){
            return;
        }

        HttpClientBuilder httpClientBuilder = HttpClients
                .custom()
                .useSystemProperties()
                .setRetryHandler(handler)
                .addInterceptorLast(requestInterceptorHandler);

        this.httpClient = httpClientBuilder.build();
    }
}
