package org.camunda.bpm;

import org.apache.http.client.HttpRequestRetryHandler;
import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl;
import org.camunda.bpm.client.impl.RequestExecutor;
import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;

public class ExternalTaskClientBuilderCustomImpl extends ExternalTaskClientBuilderImpl {

    private HttpRequestRetryHandler handler;

    public ExternalTaskClientBuilderCustomImpl(HttpRequestRetryHandler handler){
        super();
        this.handler = handler;
    }

    @Override
    protected void initEngineClient() {
        RequestInterceptorHandler requestInterceptorHandler = new RequestInterceptorHandler(this.interceptors);
        RequestExecutor requestExecutor = new RequestExecutorCustom(requestInterceptorHandler, this.objectMapper, handler);
        this.engineClient = new EngineClient(this.workerId, this.maxTasks, this.asyncResponseTimeout, this.baseUrl, requestExecutor);
    }

}
