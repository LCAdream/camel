/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.couchdb;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

public class CouchDbConsumerUpdatesIntegrationTest extends CamelTestSupport {

    @EndpointInject("couchdb:http://localhost:5984/camelcouchdb?deletes=false")
    private Endpoint from;

    @EndpointInject("mock:result")
    private MockEndpoint to;

    private CouchDbClient client;

    @BeforeEach
    public void before() {
        client = new CouchDbClient("camelcouchdb", true, "http", "localhost", 5984, null, null);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(from).to(to);
            }
        };
    }

    @Test
    void testDeletesOnly() throws InterruptedException {
        to.expectedHeaderReceived(CouchDbConstants.HEADER_METHOD, "UPDATE");
        to.expectedMessageCount(1);

        JsonElement obj = new Gson().toJsonTree("{ \"randomString\" : \"" + UUID.randomUUID() + "\" }");
        Response resp = client.save(obj);
        client.remove(resp.getId(), resp.getRev());

        to.assertIsSatisfied();
    }
}
