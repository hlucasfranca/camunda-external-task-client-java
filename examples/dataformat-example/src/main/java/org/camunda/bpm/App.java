/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.camunda.bpm.client.variable.value.XmlValue;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class App {

    public static void main(String... args) throws JAXBException {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/rest/engine/default")
                .asyncResponseTimeout(15000)
                .disableBackoffStrategy()
                .disableAutoFetching()
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

                        List rawList = convertToRawList(l);

                        ObjectValue value = ClientValues.objectValue(rawList)
                                .create();

                        VariableMap variables = Variables.createVariables().putValue("customer", value);
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

    private static List convertToRawList(List<Object> originalList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return originalList.stream().map(item -> objectMapper.convertValue(item, Map.class)).collect(Collectors.toList());
    }
}
