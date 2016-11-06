package de.oveits.velocitytemple;
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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test the cahce when reloading .tm files in the classpath
 */
public class SshTests extends CamelTestSupport {

    @Before
    public final void setUp() throws Exception {
        super.setUp();
        
        // the ssh tests are stateless, so there is nothing to do (yet) as setup
        //
        // see below an example what might be needed, if we need to initialize (copied from VelocityTemplateTests):
//
//        // create a tm file in the classpath as this is the tricky reloading stuff
//        template.sendBodyAndHeader("file://target/test-classes/org/apache/camel/component/stringtemplate", "Hello ${headers.name}", Exchange.FILE_NAME, "hello.tm");
//        
//        //
//        // delete template to clean the system (do not evaluate response):
//        //
//        MockEndpoint mock = getMockEndpoint("mock:result");
//
//        Map<String, Object> headers = new HashMap<String, Object>();
//        String body;
//        //
//        headers.put("recipientList", "http://localhost:2005/templates/ttt");
//        headers.put("CamelHttpMethod", "DELETE");
//        headers.put("name", "London");
//        body=null;
//        //
//        template.sendBodyAndHeaders("direct:recipientList", body, headers); 
//        
//        mock.reset();
    }
    
    
    @Override
    public final boolean useJmx() {
        return true;
    }
    
    @Test
    public final void sshNoStderr() throws Exception {
    	//
    	// INIT: 
    	//
        MockEndpoint mock = getMockEndpoint("mock:result");
        Map<String, Object> headers = new HashMap<String, Object>();
        String body;

        //
    	// ssh with commands that will not return a STDERR
    	//
        mock.reset();
        headers = new HashMap<String, Object>();
        
        // expectations need to be defined before sending the message:
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("### OUTPUT ###\nHello World");
        mock.expectedHeaderReceived("CamelHttpResponseCode", "200");
        mock.expectedHeaderReceived("Location", "http://localhost:2005/ssh");

        headers.put("recipientList", 
        		"http://localhost:2005/ssh?throwExceptionOnFailure=false");
        headers.put("CamelHttpMethod", "POST");
        headers.put("username", "srx");
        headers.put("password", "2GwN!gb4");
        headers.put("hostname", "192.168.163.10");
		
        // Optional Headers:
		// port default is 22:
		headers.put("port", "22");
		// default is defined in velocity-template-temple.properties: default.format 
		// (or later to be changed to format.default?)
		// Note: backslash needs to be escaped here, but not in the real header
		headers.put("format", "### OUTPUT ###\\n<STDOUT>");
		// default is defined in velocity-template-temple.properties: default.formatOnError 
		// (or later to be changed to formatOnError.default?)
		headers.put("formatOnError", "### OUTPUT ###\\n<STDOUT>### ERROR ###\\n<STDERR>");
		
        body = "echo Hello World";

        template.sendBodyAndHeaders("direct:recipientList", body, headers); 

        mock.assertIsSatisfied();
        

    }
    
    @Test
    public final void sshWithStderr() throws Exception {
    	//
    	// INIT: 
    	//
        MockEndpoint mock = getMockEndpoint("mock:result");
        Map<String, Object> headers = new HashMap<String, Object>();
        String body;

        //
    	// ssh with commands that will not return a STDERR
    	//
        mock.reset();
        headers = new HashMap<String, Object>();
        
        // expectations need to be defined before sending the message:
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("### OUTPUT ###\nHello World\n### ERROR ###\nksh: this_is_an_unknown_command: not found");
        mock.expectedHeaderReceived("CamelHttpResponseCode", "200");
        mock.expectedHeaderReceived("Location", "http://localhost:2005/ssh");

        headers.put("recipientList", "http://localhost:2005/ssh?throwExceptionOnFailure=false");
        headers.put("CamelHttpMethod", "POST");
        headers.put("username", "srx");
        headers.put("password", "2GwN!gb4");
        headers.put("hostname", "192.168.163.10");
		
        // Optional Headers:
		// port default is 22:
		headers.put("port", "22");
		// default is defined in velocity-template-temple.properties: default.format (or later to be changed to format.default?)
		// Note: backslash needs to be escaped here, but not in the real header
		headers.put("format", "### OUTPUT ###\\n<STDOUT>");
		// default is defined in velocity-template-temple.properties: default.formatOnError (or later to be changed to formatOnError.default?)
		headers.put("formatOnError", "### OUTPUT ###\\n<STDOUT>\\n### ERROR ###\\n<STDERR>");
		
		body = "echo Hello World; this_is_an_unknown_command";

        template.sendBodyAndHeaders("direct:recipientList", body, headers); 

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
//				.setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
				.setHeader("Content-Type", constant("text/html"))
//				.setHeader("Content-Type", constant("text/plain"))				
				.handled(true)
//				.continued(true)
				.end();
            	

              
              from("direct:recipientList")
              	.recipientList(simple("${headers.recipientList}"), ",")
              	// for troubleshooting:
              	//        .throwException(new RuntimeException("dfhoelkhsrfdoeklhgsoeoidhgoeldsrjklgsejlg"))
              	.to("mock:result")
                ;
            	
            }
        };
    }
}