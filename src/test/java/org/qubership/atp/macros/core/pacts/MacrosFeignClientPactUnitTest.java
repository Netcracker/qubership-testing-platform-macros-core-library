/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.macros.core.pacts;

import static org.qubership.atp.macros.core.pacts.Constants.ISO_DATE_TIME_1;
import static org.qubership.atp.macros.core.pacts.Constants.ISO_DATE_TIME_2;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.atp.auth.springbootstarter.config.FeignConfiguration;
import org.qubership.atp.macros.core.clients.api.dto.macros.MacrosDto;
import org.qubership.atp.macros.core.client.MacrosFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.matchingrules.DateMatcher;

@RunWith(SpringRunner.class)
@EnableFeignClients(clients = {MacrosFeignClient.class})
@ContextConfiguration(classes = {MacrosFeignClientPactUnitTest.TestApp.class})
@Import({JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class, FeignConfiguration.class,
        FeignAutoConfiguration.class})
@TestPropertySource(
        properties = {"feign.atp.macros.name=atp-macros", "feign.atp.macros.route=",
                "feign.atp.macros.url=http://localhost:8888", "feign.httpclient.enabled=false"})
public class MacrosFeignClientPactUnitTest {

    @Configuration
    public static class TestApp {
    }

    @Autowired
    MacrosFeignClient macrosFeignClient;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("atp-macros", "localhost", 8888, this);

    @Test
    @PactVerification()
    public void allPass() {

        UUID projectId = UUID.fromString("7c9dafe9-2cd1-4ffc-ae54-45867f2b9771");
        ResponseEntity<List<MacrosDto>> result =
                macrosFeignClient.findAllByProject(projectId);
        Assert.assertEquals(result.getStatusCode().value(), 200);
        Assert.assertTrue(result.getHeaders().get("Content-Type").contains("application/json"));

        String evaluateReq = "\"CALC()\"";
        ResponseEntity<String> result2 =
                macrosFeignClient.evaluate(evaluateReq);
        Assert.assertEquals(result2.getStatusCode().value(), 200);
        Assert.assertTrue(result2.getHeaders().get("Content-Type").contains("application/json"));


    }

    @Pact(consumer = "atp-macros-core")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        FastDateFormat dateFormat = FastDateFormat.getInstance(ISO_DATE_TIME_1, TimeZone.getDefault());


        DslPart infoForFindAllByProject = PactDslJsonArray.arrayEachLike()
                .uuid("uuid")
                .stringType("name")
                .stringType("description")
                .stringType("content")
                .uuid("createdBy")
                .uuid("modifiedBy")
                .stringType("engine")
                .uuid("projectUuid")
                .or("createdWhen", dateFormat.format(new Date(1)),
                        new DateMatcher(ISO_DATE_TIME_1),
                        new DateMatcher(ISO_DATE_TIME_2))

                .or("modifiedWhen", dateFormat.format(new Date(2)),
                        new DateMatcher(ISO_DATE_TIME_1),
                        new DateMatcher(ISO_DATE_TIME_2))
                .minArrayLike("parameters", 0, new PactDslJsonBody()
                        .uuid("uuid")
                        .stringType("name")
                        .booleanType("optional")
                        .stringType("defaultValue")
                        .stringType("description"))
                .closeObject();


        PactDslResponse response = builder
                .given("all ok")

                .uponReceiving("GET /api/v1/macros/all/project/{uuid} OK")
                .path("/api/v1/macros/all/project/7c9dafe9-2cd1-4ffc-ae54-45867f2b9771")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(infoForFindAllByProject)

                .uponReceiving("POST /api/v1/macros/evaluate OK")
                .path("/api/v1/macros/evaluate")
                .method("POST")
                .headers(headers)
                .body("\"CALC()\"")
                .willRespondWith()
                .status(200)
               .headers(headers)
                .body("\"respons\"");
        return response.toPact();
    }

}
