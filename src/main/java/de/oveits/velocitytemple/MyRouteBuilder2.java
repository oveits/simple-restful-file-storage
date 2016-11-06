package de.oveits.velocitytemple;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder2 extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
    	
    	// in order to activate those routes, you need to add sth. like
    	// <bean id="myRouteBuilder2" class="de.oveits.velocitytemple.MyRouteBuilder2"/>
    	// and
    	// <camelContext xmlns="http://camel.apache.org/schema/spring">
    	//    <routeBuilder ref="myRouteBuilder2"/>
    	// in /velocity-temple/src/main/resources/META-INF/spring/camel-context.xml
    	
    	
//    	from("jetty:http://0.0.0.0:{{CamelPath.inputport}}/CamelPath?continuationTimeout=3600000")
    	from("jetty:http://0.0.0.0:2004/CamelPath2?continuationTimeout=3600000")
    		.routeId("CamelPath2")
    		.description("CamelPath2", "Requires: routingSlip", "en")
    		.log("routingSlip")
    		.routingSlip(header("routingSlip"))
    		;
        	

    }

}
