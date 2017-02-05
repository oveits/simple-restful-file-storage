/**
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

package de.oveits.simplerestfulfilestorage;
/**
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

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import de.oveits.simplerestfulfilestorage.MyExceptionHandler;

/**
 * Unit test the cache when reloading .tm files in the classpath
 */
public class SimpleRestfulFileStorageTests extends CamelSpringTestSupport {
    

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
    }
        

    @Before
    public final void setUp() throws Exception {
        super.setUp();

        // create a tm file in the classpath as this is the tricky reloading stuff
        template.sendBodyAndHeader("file://target/test-classes/org/apache/camel/component/stringtemplate", "Hello ${headers.name}", Exchange.FILE_NAME, "hello.tm");
        
        //
        // delete file to clean the system (do not evaluate response):
        //
        MockEndpoint mock = getMockEndpoint("mock:result");

        Map<String, Object> headers = new HashMap<String, Object>();
        String body;
        //
        headers.put("recipientList", "http://localhost:2005/files/ttt");
        headers.put("CamelHttpMethod", "DELETE");
        headers.put("name", "London");
        body = null;
        //
        template.sendBodyAndHeaders("direct:recipientList", body, headers); 
        
        mock.reset();
    }
    
    public final void setupCreatedFile() throws Exception {
        
        MockEndpoint mock = getMockEndpoint("mock:result");
        Map<String, Object> headers = new HashMap<String, Object>();
        String body;
        //
        //
        // create file if not already created:
        //
        mock.reset();
        
        headers.put("recipientList", "http://localhost:2005/files/ttt");
        headers.put("CamelHttpMethod", "POST");
        body = "Hello ${headers.name}";

        // no mock expectations for setup, other than that we receive a response:
        //mock.expectedBodiesReceived("File ttt created: href=http://localhost:2005/files/ttt");
        mock.expectedMessageCount(1);

        template.sendBodyAndHeaders("direct:recipientList", body, headers);
        
        mock.reset();
        headers = new HashMap<String, Object>();
    }
    
    @Override
    public final boolean useJmx() {
        return true;
    }
    
    @Test
    public final void createFile() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        Map<String, Object> headers = new HashMap<String, Object>();
        String body;
        
        //
        // create file:
        //
        mock.reset();
        headers = new HashMap<String, Object>();
        
        headers.put("recipientList", "http://localhost:2005/files/ttt");
        headers.put("CamelHttpMethod", "POST");
        headers.put("name", "London");
        body = "Hello ${headers.name}";

        // mock expectations need to be specified before sending the message:
        mock.expectedBodiesReceived("File ttt created: href=http://localhost:2005/files/ttt");
        mock.expectedMessageCount(1);
      

        template.sendBodyAndHeaders("direct:recipientList", body, headers);

        // TODO: replace the complicated asserts below by mock.expected configuration BEFORE  the file is sent:
        assertFalse(mock.getExchanges().get(0).getIn().getHeader("CamelHttpResponseCode") == null); 
        assertTrue(mock.getExchanges().get(0).getIn().getHeader("CamelHttpResponseCode").toString().equals("201"));
        assertFalse(mock.getExchanges().get(0).getIn().getHeader("Location") == null); 
        assertTrue(mock.getExchanges().get(0).getIn().getHeader("Location").toString().equals("http://localhost:2005/files/ttt"));
        // does not work (I do not know, why; always says that the body is null, although it is not, when debugging, using e0 in 
        // Exchange e0 = mock.getExchanges().get(0); // for debugging
        mock.assertIsSatisfied();
        // replaced by:
//        assertTrue(mock.getExchanges().get(0).getIn().getBody(String.class).equals("File ttt created: href=http://localhost:2005/files/ttt"));

        //
        // create file a second time:
        //
        mock.reset();
        headers = new HashMap<String, Object>();
        
        headers.put("recipientList", "http://localhost:2005/files/ttt?throwExceptionOnFailure=false");
        headers.put("CamelHttpMethod", "POST");
        headers.put("name", "London");
        body = "Hello ${headers.name}";

        // mock expectations need to be specified before sending the message:
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("File ttt exists already: href=http://localhost:2005/files/ttt");
        // could be replaced by (AFTER having sent the message!):
//      assertTrue(mock.getExchanges().get(0).getIn().getBody(String.class).equals("File ttt exists already: href=http://localhost:2005/files/ttt"));

        template.sendBodyAndHeaders("direct:recipientList", body, headers);

        // for debugging:
//        Exchange e0 = mock.getExchanges().get(0);  

        // TODO: replace the complicated asserts below by mock.expected configuration BEFORE  the file is sent:
        assertFalse(mock.getExchanges().get(0).getIn().getHeader("CamelHttpResponseCode") == null); 
        assertTrue(mock.getExchanges().get(0).getIn().getHeader("CamelHttpResponseCode").toString().equals("409"));
        assertFalse(mock.getExchanges().get(0).getIn().getHeader("Location") == null); 
        assertTrue(mock.getExchanges().get(0).getIn().getHeader("Location").toString().equals("http://localhost:2005/files/ttt"));
        mock.assertIsSatisfied();

    }
    
    @Test
    public final void updateFile() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        Map<String, Object> headers = new HashMap<String, Object>();
        String body;       
        
        //
        // update file ttt to "Hello ${headers.name}":
        //
        mock.reset();
        headers = new HashMap<String, Object>();
        
        // specify input parameters:
        headers.put("recipientList", "http://localhost:2005/files/ttt");
        headers.put("name", "London");
        headers.put("CamelHttpMethod", "PUT");
        body = "Hello ${headers.name}";

        // expectations need to be defined before sending the message:
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("File ttt created: href=http://localhost:2005/files/ttt");
        mock.expectedHeaderReceived("CamelHttpResponseCode", "201");
        mock.expectedHeaderReceived("Location", "http://localhost:2005/files/ttt");

        template.sendBodyAndHeaders("direct:recipientList", body, headers);
        
        // assert:
        mock.assertIsSatisfied();
 
        
        //
        // update file ttt to "Hello ${headers.name}" a second time:
        //
        mock.reset();
        headers = new HashMap<String, Object>();
        
        // specify input parameters:
        headers.put("recipientList", "http://localhost:2005/files/ttt");
        headers.put("name", "London");
        headers.put("CamelHttpMethod", "PUT");
        body = "Hello ${headers.name}";

        // expectations need to be defined before sending the message:
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("File ttt updated: href=http://localhost:2005/files/ttt");  
        mock.expectedHeaderReceived("CamelHttpResponseCode", "200");
        mock.expectedHeaderReceived("Location", "http://localhost:2005/files/ttt");

        template.sendBodyAndHeaders("direct:recipientList", body, headers);
        
        mock.assertIsSatisfied();

    }
    
    @Test
    public final void readFile() throws Exception {
        //
        // INIT: 
        //
        MockEndpoint mock = getMockEndpoint("mock:result");
        Map<String, Object> headers = new HashMap<String, Object>();
//        String body;

        //
        // read file, when it does not exist:
        //
        mock.reset();
        headers = new HashMap<String, Object>();
        
        // expectations need to be defined before sending the message:
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("404 Not Found: file ttt does not exist");
        mock.expectedHeaderReceived("CamelHttpResponseCode", "404");
        mock.expectedHeaderReceived("Location", null);
        
        headers.put("recipientList", "http://localhost:2005/files/ttt?throwExceptionOnFailure=false");
        headers.put("CamelHttpMethod", "GET");

        template.sendBodyAndHeaders("direct:recipientList", "Hello ${headers.name}", headers);

        mock.assertIsSatisfied();
        
        // make sure the file is created:
        setupCreatedFile();       
       
        //
        // read file, when it exists:
        //
        mock.reset();
        headers = new HashMap<String, Object>();
     
        // expectations need to be defined before sending the message:
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Hello ${headers.name}");
        mock.expectedHeaderReceived("CamelHttpResponseCode", "200");
        mock.expectedHeaderReceived("Location", "http://localhost:2005/files/ttt");
        
        headers.put("recipientList", "http://localhost:2005/files/ttt");
        headers.put("CamelHttpMethod", "GET");

        template.sendBodyAndHeaders("direct:recipientList", "Hello ${headers.name}", headers);

        mock.assertIsSatisfied();

    }
    
    @Test
    public final void deleteFile() throws Exception {  
        //
        // INIT: 
        //
        MockEndpoint mock = getMockEndpoint("mock:result");
        Map<String, Object> headers = new HashMap<String, Object>();
//        String body;
        
        // make sure the resource exists:
        setupCreatedFile();   
        
        //
        // delete file that exists: expect 204 No Content:
        //
        mock.reset();
        headers = new HashMap<String, Object>();
        
        mock.expectedMessageCount(1);
//        mock.expectedBodiesReceived("");
        mock.expectedHeaderReceived("CamelHttpResponseCode", "204");
        mock.expectedHeaderReceived("Location", "http://localhost:2005/files/ttt");

        headers.put("recipientList", "http://localhost:2005/files/ttt");
        headers.put("CamelHttpMethod", "DELETE");

        template.sendBodyAndHeaders("direct:recipientList", "Body", headers);  
        
        mock.assertIsSatisfied();
        
        //
        // delete file that does not exist: expect 404 Not Found:
        //
        mock.reset();
        headers = new HashMap<String, Object>();
        
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("404 Not Found: file ttt does not exist");
        mock.expectedHeaderReceived("CamelHttpResponseCode", "404");
        mock.expectedHeaderReceived("Location", "http://localhost:2005/files/ttt");

        headers.put("recipientList", "http://localhost:2005/files/ttt?throwExceptionOnFailure=false");
        headers.put("CamelHttpMethod", "DELETE");
        headers.put("name", "London");

        template.sendBodyAndHeaders("direct:recipientList", "Body", headers);  

        mock.assertIsSatisfied();
        

    }
    
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public final void configure() throws Exception {
                
                onException(Exception.class)
                .setHeader("ResultCode", constant(1))
                .setHeader("ResultText", constant("Failure"))
                .convertBodyTo(String.class).bean(MyExceptionHandler.class)
                .log("failed request: ${body}")
//                .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
                .setHeader("Content-Type", constant("text/html"))
//                .setHeader("Content-Type", constant("text/plain"))                
                .handled(true)
//                .continued(true)
                    .end();
                

              
                from("direct:recipientList")
                    .recipientList(simple("${headers.recipientList}"), ",")
                  // for troubleshooting:
                  //        .throwException(new RuntimeException("dfhölkhsrfdöklhgsöoidhgöldsrjklgsejlg"))
                    .to("mock:result")
                  ;
              
                
            }
        };
    }
}