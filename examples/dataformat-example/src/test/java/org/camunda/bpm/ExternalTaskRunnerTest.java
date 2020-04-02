package org.camunda.bpm;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.awaitility.Awaitility.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "app.baseUrl=http://localhost:10100", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 10100)
public class ExternalTaskRunnerTest {

    private final RestTemplate restTemplate = new RestTemplate();

    private AtomicBoolean complete = new AtomicBoolean(false);

    @Autowired
    private ExternalTaskRunner service;

    @Test
    public void shouldExecuteFetchAndLockSuccessfully() throws Exception {

        service.setSuccessCallback((Void) -> completed());

        stubFor(post(urlMatching("/rest/engine/default/external-task/fetchAndLock"))
                .willReturn(aResponse()
                .withBody("[{\"activityId\":\"Task_1giqci3\",\"activityInstanceId\":\"Task_1giqci3:b5fb0c67-73a8-11ea-a583-503eaa214820\",\"errorMessage\":null,\"errorDetails\":null,\"executionId\":\"b5fb0c66-73a8-11ea-a583-503eaa214820\",\"id\":\"b5fb0c68-73a8-11ea-a583-503eaa214820\",\"lockExpirationTime\":\"2020-03-31T21:01:55.154-0300\",\"processDefinitionId\":\"Sample:1:92c2a473-72c3-11ea-a583-503eaa214820\",\"processDefinitionKey\":\"Sample\",\"processDefinitionVersionTag\":null,\"processInstanceId\":\"b5f8c26a-73a8-11ea-a583-503eaa214820\",\"retries\":null,\"suspended\":false,\"workerId\":\"workerTeste\",\"topicName\":\"customerCreation\",\"tenantId\":null,\"variables\":{\"amount\":{\"type\":\"Long\",\"value\":1,\"valueInfo\":{}},\"item\":{\"type\":\"Object\",\"value\":\"rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABc3IAF2phdmEudXRpbC5MaW5rZWRIYXNoTWFwNMBOXBBswPsCAAFaAAthY2Nlc3NPcmRlcnhyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAF3CAAAAAIAAAABdAAEbm9tZXQACGl0ZW0teHl6eAB4\",\"valueInfo\":{\"objectTypeName\":\"java.util.ArrayList\",\"serializationDataFormat\":\"application/x-java-serialized-object\"}}},\"priority\":0,\"businessKey\":null}]")));

        stubFor(post(urlMatching(".*/complete"))
                .willReturn(aResponse()
                        .withStatus(204)));

        service.start();

        await().until(isComplete());
    }

    private Callable<Boolean> isComplete() {
        return () -> complete.get();
    }

    private void completed(){
        complete.set(true);
    }

}