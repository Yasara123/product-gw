/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.gateway.httpcompliance.tests.responses.clienterror;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClient;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClientImpl;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import java.io.File;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

public class HTTP400ComplianceTest {
    private GatewayAdminClient gwClient;
    private HttpServerOperationBuilderContext emulator;
    private String host = "127.0.0.1";
    private int port = 9090;

    @BeforeClass
    public void setup() throws Exception {
        gwClient = new GatewayAdminClientImpl();
        gwClient.startGateway();
        gwClient.deployArtifact("artifacts" + File.separator + "http-compliance-test-camel-context.xml");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(6065).context("/users"))

                .when(request()
                        .withMethod(HttpMethod.valueOf("GETT"))
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.OK)
                        .withBody("User1"))

                .when(request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.OK)
                        .withBody("User2"))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user3")
                        .withBody("name=WSO2&location=Colombo10"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.OK)
                        .withBody("Trace Expert City"))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withPath("/user3").withBody(""))
                .then(response()
                        .withStatusCode(HttpResponseStatus.OK)
                        .withBody("Trace Expert City"))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwClient.stopGateway();
        emulator.stop();
        gwClient.cleanArtifacts();
    }

    @Test
    public void test400GETRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host("127.0.0.1").port(9090))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.valueOf("GETT"))
                        .withPath("/new-route")
                        .withHeader("routeId", "r1"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.BAD_REQUEST,
                "Expected response code not found");
//        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "User1");
    }

//    @Test
//    public void test200POSTRequestWithPayload() throws Exception {
//        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
//                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))
//
//                .when(HttpClientRequestBuilderContext.request()
//                        .withMethod(HttpMethod.POST)
//                        .withPath("/new-route")
//                        .withBody("name=WSO2&location=Colombo10")
//                        .withHeader("routeId", "r3"))
//
//                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();
//
//        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
//                "Expected response code not found");
//        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Trace Expert City");
//    }
//
//    @Test
//    public void test200POSTRequestWithoutPayload() throws Exception {
//        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
//                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))
//
//                .when(HttpClientRequestBuilderContext.request()
//                        .withMethod(HttpMethod.POST)
//                        .withPath("/new-route")
//                        .withHeader("routeId", "r3"))
//
//                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();
//
//        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.OK,
//                "Expected response code not found");
//        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Trace Expert City",
//                "Response body does not match the expected response body");
//    }
}
