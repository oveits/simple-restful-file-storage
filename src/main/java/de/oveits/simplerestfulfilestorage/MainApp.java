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

import java.io.File;

//import org.apache.camel.main.Main;
import org.apache.camel.spring.Main;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;


/**
 * A Camel Application
 */
public class MainApp {

    private static final String LOG_CONF_NAME = "camel.lcf";
    
    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(final String... args) throws Exception {
    	
//    	// OV added: write myHTTPHeaderFilterStrategy into registry for later use for HTTP producers
//    	SimpleRegistry registry = new SimpleRegistry();
//    	CamelContext context = new DefaultCamelContext(registry);
//    	//
//    	HeaderFilterStrategy myHTTPHeaderFilterStrategy = new MyHTTPHeaderFilterStrategy();
//    	//
//    	registry.put("myHTTPHeaderFilterStrategy", myHTTPHeaderFilterStrategy);
    	
        initLogging();
        
//        startCXF();
        startCamel(args);
        
    }
    
//    private static void startCXF() throws Exception {
//        BrokerService broker = new BrokerService();
//        broker.setDataDirectory("data/");
//        broker.addConnector("cxf");
//        //broker.addConnector("tcp://localhost:61616");
//        broker.start();
//    }


    private static void startCamel(final String[] args) throws Exception {
        Main main = new Main();
        main.enableHangupSupport();
        //main.addRouteBuilder(new MyRouteBuilder());
        main.run(args);
    }
    
    private static void initLogging() {
        File logConfFile = new File(LOG_CONF_NAME);

        if (logConfFile.exists()) {
            PropertyConfigurator.configure(LOG_CONF_NAME);
        } else {
            BasicConfigurator.configure();
        }
    }
}

