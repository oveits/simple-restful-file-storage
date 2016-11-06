package de.oveits.velocitytemple;

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
    	
//    	String cached = "cached=false";
    	
    	// Standard Exception Handler
		onException(Exception.class)
				.setHeader("ResultCode", constant(1))
				.setHeader("ResultText", constant("Failure"))
				.convertBodyTo(String.class).bean(MyExceptionHandler.class)
				.log("failed request: ${body}")
//				.setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
				.setHeader("Content-Type", constant("text/html"))
//				.setHeader("Content-Type", constant("text/plain"))
				.handled(false)
				.end();
    	
    	//
		// MAIN ROUTES
		//
		
		// at least a single jetty consumer is needed, so the rest routes can be created based on the jetty component:
		// in our case, we implement a redirect on the root URL:
    	from("jetty:http://0.0.0.0:{{inputport}}/")
		    .setHeader("Location", simple("${headers.CamelHttpUrl}files"))
			  .setHeader("CamelHttpResponseCode", constant("301"))		
			  ;
		

    	// define source interfaces and port:
		restConfiguration().host("0.0.0.0").port("{{inputport}}");
		
		// define REST service:
		rest("/files")
		// List
			.get() 
			.route().pipeline("direct:before", "direct:listFiles", "direct:after").endRest()
		// Read as text:
			.get("/{fileName}") // Read
			.route().pipeline("direct:before", "direct:readFile", "direct:after").endRest()
		// Read as json (not supported)
//			.get("/{fileName}/json")
//			.route().pipeline("direct:before", "direct:readFile", "direct:toJson").endRest()
		// Create
			.post("/{fileName}")
			.route().pipeline("direct:before", "direct:createFile", "direct:after").endRest()
		// Update
			.put("/{fileName}")
			.route().pipeline("direct:before", "direct:updateFile", "direct:after").endRest()
		// Delete
			.delete("/{fileName}")
			.route().pipeline("direct:before", "direct:deleteFile", "direct:after").endRest()
		;
		

		
		from("direct:before")
		// default settings:
			// cached = false
			.choice().when(header("cached").isNull()).setHeader("cached", constant("false")).end()
		;
		
		from("direct:after")
		// set format of the response:
			.to("direct:toText")
			;
				
		
		from("direct:toText")
		.setHeader("Content-Type", constant("text/html; charset=UTF-8"))
//		.setBody(simple("<pre>${body}</pre>"))
//		.throwException(new RuntimeException("lerhoziwrhzehireoihreaooi"))
		;
		
		// not yet implemented:
//		from("direct:toJson")
//		.setHeader("Content-Type", constant("text/html; charset=UTF-8"))
//		;

	    Boolean allowRoutingSlip = false;	    
		if (allowRoutingSlip) {
			// testing of recursive routingSlip; not yet implemented:
		    from("jetty:http://0.0.0.0:{{inputport}}/routingSlip/") //?continuationTimeout=3600000")
				.routeId("routingSlip")
		    	.routingSlip(header("routingSlip"))
			;
		}
//		
//	    from("direct:routingSlip")
//	    .routeId("routingSlip")
//	    	// for tracing, but we need a better solution:
////	    	.log("begin of direct:routingSlip")
//	    	//
//	    	// not implemented: if we allow for nextHop routing, we need to implement a process on how to limit the effect of loops: 
////			.choice().when(header("maxHops"))
////				.setHeader("maxHops", simple("${headers.maxHops} - 1"))
////			.otherwise()
////			    .setHeader("maxHops", simple("1"))
////			.end()
//	    	//
//	    	// as long as loop prevention is not implemented, we cannot allow for nextHop routing: 
////	    	.choice().when(header("nextHop"))
////	    	    .setHeader("routingSlip", simple("${headers.nextHop}"))
////	    	    .removeHeader("nextHop")
////	    	.end()
//	    	.routingSlip(header("routingSlip"))
//	    	//
//	    	// as long as loop prevention is not implemented, we cannot allow for nextHop routing: 
////	    	.choice().when(header("nextHop").isNotNull())
////	    	    .setHeader("routingSlip", simple("${headers.nextHop}"))
////	    	    .removeHeader("nextHop")
////	    	    .to("direct:routingSlip")
////	    	.end()
//	    	// for tracing, but we need a better solution:	    	
////	    	.log("end of direct:routingSlip")
//	    	
////	    	.removeHeaders(".*")
//	    	
//	    	// default response: pure text
//	    	.setHeader("Content-Type", constant("text/plain"))
//		;
//	    
//	    from("seda:routingSlip?concurrentConsumers=10")
//	    	.routingSlip(header("routingSlip"))
//	    ;
//	    
//	    from("vm:routingSlip?concurrentConsumers=10")
//	    	.routingSlip(header("routingSlip"))
//	    ;
	    
	    	
		
		//
		// CRUD ROUTES
		//
		
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
		
		from("direct:listFiles")	
			.routeId("listFiles")			
//			.log("direct:listFiles started")
			.setHeader("folderList",	simple("files, src/main/resources/files"))
			.setHeader("directoryName",	simple("src/main/resources/files"))
			.bean(FileUtilBeans.class, "listFiles")
//			.throwException(new RuntimeException("errhdpgoiwehrohrhwiohrwod"))
//			.log("direct:listFiles ended")
		;
		
		
		from("direct:readFile")
			.routeId("readFile")
//			.log("direct:readFile started with file=${headers.fileName}")
			.setHeader("fileName", simple("${headers.fileName}"))
			.doTry()
				.to("direct:verifyFileName")
				.setHeader("folderList",	simple("files, src/main/resources/files"))
				.bean(FileUtilBeans.class, "readFile")
			    .setHeader("Location", simple("${headers.CamelHttpUrl}"))
				.setHeader("CamelHttpResponseCode", constant("200"))
			.doCatch(Exception.class)
				.setHeader("CamelHttpResponseCode", constant("404"))
				.removeHeader("Location")
				.setBody(simple("404 Not Found: file ${headers.fileName} does not exist"))
			.endDoTry()
//			.log("direct:readFile ended with file=${headers.fileName}")
		;
		
		
		
		from("direct:updateFile")
			.routeId("updateFile")
//			.log("direct:updateFile started with file=${headers.fileName}")
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
//			.log("direct:updateFile ended with file=${headers.fileName}")
		;
		
		from("direct:deleteFile")
			.routeId("deleteFile")
//			.log("direct:deleteFile started with file=${headers.fileName}")			
			.setHeader("fileName", simple("src/main/resources/files/${headers.fileName}"))
			.to("direct:verifyFileName")
			.setHeader("folderList",	simple("files, src/main/resources/files"))
			.bean(FileUtilBeans.class, "deleteFile")
		    .setHeader("Location", simple("${headers.CamelHttpUrl}"))
			.choice()
				.when(body().isEqualTo("true"))
					.setHeader("CamelHttpResponseCode", constant("204"))
					.setBody(constant(null))
				.otherwise()
					.setHeader("CamelHttpResponseCode", constant("404"))
					.setBody(simple("404 Not Found: file ${headers.fileName} does not exist"))
			.end()
//			.log("direct:deleteFile ended with file=${headers.fileName}")
		;

				
		//
		// Helper Routes
		//
		
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
