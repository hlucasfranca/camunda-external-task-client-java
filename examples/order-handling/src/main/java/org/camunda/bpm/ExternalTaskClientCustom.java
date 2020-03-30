package org.camunda.bpm;

import org.apache.http.client.HttpRequestRetryHandler;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;

public interface ExternalTaskClientCustom extends ExternalTaskClient {

    static ExternalTaskClientBuilder create(HttpRequestRetryHandler handler) {
        return new ExternalTaskClientBuilderCustomImpl(handler);
    }

}
