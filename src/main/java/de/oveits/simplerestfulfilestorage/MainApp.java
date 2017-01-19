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
    /**
     * Main Class: Simple RESTful File Store based on Apache Camel
     * 
     * Run with
     * java -jar -Dlog4j.configuration=file:full_path_to_log4j.properties full_path_to_jarfile.jar
     * 
     * Stop with Ctrl-C in the command line terminal running the program or kill from outside.
     * 
     * @param args: none
     */

    private static final String LOG_CONF_NAME = "camel.lcf";
    
    public static void main(final String... args) throws Exception {
        /**
         * main() procedure for starting logging and starting Camel routes
         * 
         */        
        
        // is it really needed? Try without!
        initLogging();
       
        startCamel(args);
        
    }

    private static void startCamel(final String[] args) throws Exception {
        /**
         * startCamel() procedure for starting Camel routes
         * 
         */ 
        Main main = new Main();
        main.enableHangupSupport();

        main.run(args);
    }
    
    // Is it really needed? Try without!
    private static void initLogging() {
        /**
         * initLogging() procedure for starting logging with LOG_CONF_NAME = "camel.lcf" (hardcoded).
         * 
         */
        File logConfFile = new File(LOG_CONF_NAME);

        if (logConfFile.exists()) {
            PropertyConfigurator.configure(LOG_CONF_NAME);
        } else {
            BasicConfigurator.configure();
        }
    }
}

