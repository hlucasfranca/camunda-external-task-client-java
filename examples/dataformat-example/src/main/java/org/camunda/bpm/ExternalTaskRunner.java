package org.camunda.bpm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.primitives.Primitives;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExternalTaskRunner
//        implements CommandLineRunner
{

//    @Override
//    public void run(String... args) throws Exception {
//        start();
//    }

    public void start() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:10100/rest/engine/default")
                .asyncResponseTimeout(15000)
                .disableBackoffStrategy()
                .defaultSerializationFormat(ClientValues.SerializationDataFormats.JAVA.getName())
                .maxTasks(1)
                .lockDuration(20000)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        TopicSubscriptionBuilder jsonSubscriptionBuilder = client.subscribe("customerCreation")
                .lockDuration(15000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        List<Object> l = new ArrayList<>();
                        l.add(new Customer());

                        Object rawList = convertToRawList(l);

                        ObjectValue value = ClientValues.objectValue(rawList).create();

                        VariableMap variables = Variables.createVariables();

                        variables.put("nomeString", ClientValues.stringValue("nomeString"));
                        variables.put("nomeRaw", ClientValues.objectValue("nomeRaw").create());
                        variables.put("valorObjectMapper", convertToRawList("oi"));
                        variables.put("valorLista", value);
                        externalTaskService.complete(externalTask, variables);

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


        client.start();
        jsonSubscriptionBuilder.open();
    }

    private static Object convertToRawList(Object originalList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        if(originalList instanceof List){
            return ((List)originalList).stream().map(item -> objectMapper.convertValue(item, Map.class)).collect(Collectors.toList());
        } else if(Primitives.isWrapperType(originalList.getClass()) || originalList instanceof String) {
            return originalList;
        } {
            return objectMapper.convertValue(originalList, Map.class);
        }

    }
}
