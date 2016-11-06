package de.oveits.velocitytemple;



import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.CamelContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;


public class ClearContentCache {
	//
	// will clear the contentCache of velocity Templates and alike using JMX
	//

	public final String clearContentCache(final Exchange exchange, final CamelContext context, @Body final String body, @Headers final Map<String, Object> headers) throws Exception {

//		Iterator<String> headerIterator = headers.keySet().iterator();
//		while (headerIterator.hasNext()) {
//		    String headerName = headerIterator.next();
//		    System.out.println(headerName + "=" + headers.get(headerName));
//		    
//		    if (headerName.split(".").length >= 2 && headerName.split(".")[headerName.split(".").length - 1].toString().equals("regex")){
//		    	String headerToBeChecked = headerName.substring(0, headerName.split(".")[headerName.split(".").length - 1].length());
//		    	if (headers.get(headerToBeChecked) == null || !headers.get(headerToBeChecked).toString().matches(headers.get(headerName).toString())){
//		    		throw (new Exception(""));
//		    	}
//		    }
//
//		}
		
        // clear the cache using jmx
        MBeanServer mbeanServer = context.getManagementStrategy().getManagementAgent().getMBeanServer();
        Set<ObjectName> objNameSet = mbeanServer.queryNames(new ObjectName("org.apache.camel:type=endpoints,name=\"velocity:*\",*"), null);
        ObjectName managedObjName = new ArrayList<ObjectName>(objNameSet).get(0);        
        mbeanServer.invoke(managedObjName, "clearContentCache", null, null);
        
//        ArrayList<ObjectName> myArray = new ArrayList<ObjectName>(objNameSet);
//        String listLen = String.valueOf(myArray.size());
//        throw (new Exception(listLen));
		return "cleared cache"; 
	}
	
			
}
