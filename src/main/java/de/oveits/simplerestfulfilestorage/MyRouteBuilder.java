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

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;


/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public final void configure() {
        
//        String cached = "cached=false";
        
        // Standard Exception Handler
        
        /**
         * Custom Exception handler, which is setting 
         *   ResultCode = 1
         *   ResultText = Failure
         *   
         *   and it is applying MyExceptionHandler.class to the Error message received from submodultes.
         *   See documentation of MyExceptionHandler.class for details. 
         */
        onException(Exception.class)
                .setHeader("ResultCode", constant(1))
                .setHeader("ResultText", constant("Failure"))
                .convertBodyTo(String.class).bean(MyExceptionHandler.class)
                .log("failed request: ${body}")
//                .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
                .setHeader("Content-Type", constant("text/html"))
//                .setHeader("Content-Type", constant("text/plain"))
                .handled(false)
                .end();
        
        /**
         * MAIN ROUTES
         */
        
        /**
         * Redirect Route from "/" to "/files"
         * 
         * Note: at least a single jetty consumer is needed, so the rest routes can be created based on the jetty component:
         *       In our case, this redirect route is regestering the jetty component for us
         */
        from("jetty:http://0.0.0.0:{{inputport}}/")
            .setHeader("Location", simple("${headers.CamelHttpUrl}files"))
            .setHeader("CamelHttpResponseCode", constant("301"))        
            ;
        

        /** 
         * define source interfaces and port: 
         * - we liste on all source interfaces 
         * - we listen on the port defined as inputport in the this.properties file
         */
        restConfiguration().host("0.0.0.0").port("{{inputport}}");
        
        /** 
         * define REST service with base path "/files"
         */
        rest("/files")
            /** 
             * GET /files -> List files
             * 
             * @params: none
             */
            .get() 
            .route().pipeline("direct:before", "direct:listFiles", "direct:after").endRest()
            
            /**
             * GET /files/fileName -> Read file and return als text
             */
            .get("/{fileName}")
            .route().pipeline("direct:before", "direct:readFile", "direct:after").endRest()
            
            // Read as json (not yet supported)
//            .get("/{fileName}/json")
//            .route().pipeline("direct:before", "direct:readFile", "direct:toJson").endRest()
            
            /**
             * POST /files/fileName -> Create file with name = fileName
             */
            .post("/{fileName}")
            .route().pipeline("direct:before", "direct:createFile", "direct:after").endRest()

            /**
             * PUT /files/fileName -> Update (overwrite) file with name = fileName
             *                        will create the file, if it does not exist
             */
            .put("/{fileName}")
            .route().pipeline("direct:before", "direct:updateFile", "direct:after").endRest()
        
            /**
             * DELETE /files/fileName -> Delete file with name = fileName
             */
            .delete("/{fileName}")
            .route().pipeline("direct:before", "direct:deleteFile", "direct:after").endRest()
        ;
        
        /**
         * Apply this route before each REST command
         * - set caching to false, so we can update the files without re-starting the routes
         */
        from("direct:before")
        // default settings:
            // cached = false
            .choice().when(header("cached").isNull()).setHeader("cached", constant("false")).end()
        ;

        /**
         * Apply this route after each REST command before sending the reply
         * - reply with text format
         */
        from("direct:after")
            .to("direct:toText")
            ;
                
        /**
         * Sub Route to set the format to text
         */
        from("direct:toText")
            .setHeader("Content-Type", constant("text/html; charset=UTF-8"))
    //        .setBody(simple("<pre>${body}</pre>"))
    //        .throwException(new RuntimeException("lerhoziwrhzehireoihreaooi"))
            ;
        
        // not yet implemented:
//        from("direct:toJson")
//        .setHeader("Content-Type", constant("text/html; charset=UTF-8"))
//        ;
            
        
        /**
         * RESTful CRUD ROUTES
         * C = Create
         * R = Read (both, list all files and read single file)
         * U = Update
         * D = Delete
         */

        /**
         * List all Files in "/files" directory
         */
        from("direct:listFiles")    
            .routeId("listFiles")            
//            .log("direct:listFiles started")
            .setHeader("folderList",    simple("files, src/main/resources/files"))
            .setHeader("directoryName",    simple("src/main/resources/files"))
            .bean(FileUtilBeans.class, "listFiles")
//            .throwException(new RuntimeException("errhdpgoiwehrohrhwiohrwod"))
//            .log("direct:listFiles ended")
        ;
        
        /**
         * Create File
         * - creates file, if it does not exist
         * - fails with code 409, if file exists already (will not overwrite file)
         */
        from("direct:createFile")
            .routeId("createFile")
            .log("direct:createFile started with file=${headers.fileName}")
            .setHeader(Exchange.FILE_NAME, simple("${headers.fileName}"))
            .to("direct:verifyFileName")
            .doTry()
                .to("file:src/main/resources/files/?autoCreate=true&fileExist=Fail") // Ignore will not overwrite existing files
                .setHeader("Location", simple("${headers.CamelHttpUrl}"))
                .setHeader("CamelHttpResponseCode", constant("201"))
                .setBody(simple("File ${headers.fileName} created: href=${headers.CamelHttpUrl}"))
            .doCatch(Exception.class)
                .setHeader("CamelHttpResponseCode", constant("409"))
                .setHeader("Location", simple("${headers.CamelHttpUrl}"))
                .setBody(simple("File ${headers.fileName} exists already: href=${headers.CamelHttpUrl}"))
            .endDoTry()            
            .log("direct:createFile ended with file=${headers.fileName}")
        ;        
        
        /**
         * Read File
         * 
         * @params headers.fileName 
         * - returns file content, if file exists,
         * - return error code 404, if the file is not found.
         */
        from("direct:readFile")
            .routeId("readFile")
//            .log("direct:readFile started with file=${headers.fileName}")
            .setHeader("fileName", simple("${headers.fileName}"))
            .doTry()
                .to("direct:verifyFileName")
                .setHeader("folderList",    simple("files, src/main/resources/files"))
                .bean(FileUtilBeans.class, "readFile")
                .setHeader("Location", simple("${headers.CamelHttpUrl}"))
                .setHeader("CamelHttpResponseCode", constant("200"))
            .doCatch(Exception.class)
                .setHeader("CamelHttpResponseCode", constant("404"))
                .removeHeader("Location")
                .setBody(simple("404 Not Found: file ${headers.fileName} does not exist"))
            .endDoTry()
//            .log("direct:readFile ended with file=${headers.fileName}")
        ;    
        
        /**
         * Update File
         * - creates file, if it does not exist
         * - overwrites file, if it exists already
         */
        from("direct:updateFile")
            .routeId("updateFile")
//            .log("direct:updateFile started with file=${headers.fileName}")
            .setHeader(Exchange.FILE_NAME, simple("${headers.fileName}"))
            .to("direct:verifyFileName")
            .removeHeader("CamelHttpResponseCode")
            .doTry()
                .to("file:src/main/resources/files/?autoCreate=true&fileExist=Fail")
                .setHeader("CamelHttpResponseCode", constant("201"))
                .setHeader("Location", simple("${headers.CamelHttpUrl}"))
                .setBody(simple("File ${headers.fileName} created: href=${headers.CamelHttpUrl}"))
            .doCatch(Exception.class)
                .to("file:src/main/resources/files/?autoCreate=true&fileExist=Override") // will always overwrite
                .setHeader("CamelHttpResponseCode", constant("200"))
                .setHeader("Location", simple("${headers.CamelHttpUrl}"))
                .setBody(simple("File ${headers.fileName} updated: href=${headers.CamelHttpUrl}"))
            .endDoTry()
//            .log("direct:updateFile ended with file=${headers.fileName}")
        ;
        
        /**
         * Delete File
         * - deletes file, if it exists
         * - returns error code 404, if file does not exist
         */
        from("direct:deleteFile")
            .routeId("deleteFile")
            .log("direct:deleteFile started with file=${headers.fileName}")        
            .to("direct:verifyFileName")
            .setHeader("folderList",    simple("files, src/main/resources/files"))
            .log("direct:deleteFile started with folderList=${headers.folderList}")
            .log("direct:deleteFile: calling FileUtilBeans deleteFile")    
            .bean(FileUtilBeans.class, "deleteFile")
            .log("direct:deleteFile: finished FileUtilBeans deleteFile")    
            .setHeader("Location", simple("${headers.CamelHttpUrl}"))
            .choice()
                .when(body().isEqualTo("true"))
                    .setHeader("CamelHttpResponseCode", constant("204"))
                    .setBody(constant(null))
                .otherwise()
                    .setHeader("CamelHttpResponseCode", constant("404"))
                    .setBody(simple("404 Not Found: file ${headers.fileName} does not exist"))
            .end()
            .log("direct:deleteFile ended with file=${headers.fileName}")
        ;
            
        /**
         * verifyFileName helper route: verify, that a file name is valid syntactically
         * 
         * Returns an exception, which will be handled with the custom exception handler
         *   - if the fileName is empty, 
         *   - or if it contains the ".." string (as a security measure)
         *   
         * It does not test, whether the file exists; this is done by the FileUtilBeans class
         */        
        from("direct:verifyFileName")
            .routeId("direct:verifyFileName")
            .choice()
                .when(header("fileName").isNull())
                    .throwException(new RuntimeException("direct:verifyFileName called with null fileName"))
                .when(header("fileName").isEqualTo(""))
                    .throwException(new RuntimeException("direct:verifyFileName called with empty fileName"))
                .when(header("fileName").contains(".."))
                    .throwException(new RuntimeException("direct:verifyFileName called with invalid fileName"))
            .end()
        ;
        

            

    }

}
