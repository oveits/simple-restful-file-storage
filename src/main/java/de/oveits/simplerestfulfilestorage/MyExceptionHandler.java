package de.oveits.simplerestfulfilestorage;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.camel.OutHeaders;
import org.apache.camel.component.http.HttpOperationFailedException;

import com.google.common.collect.Lists;


public class MyExceptionHandler {
	//
	// custom exception handler
	//
	// nor replaced @Headers by @OutHeaders, since the latter allows to rewrite the headers...

	// explicit conversion, if body is not of the type String:
//	public String failMessage(@Body Object body, Exception e, @Headers Map<String, Object> headers,  @OutHeaders Map<String, Object> outHeaders) {
//		String myBody = String.valueOf(body); //body.toString();
//		return failMessage(myBody, e, headers,  outHeaders);
//	}
//	public void setFailResultTextHeader(@Body Result myBody, Exception e, @Headers Map<String, Object> headers,  @OutHeaders Map<String, Object> outHeaders) {		
//		headers.put("ResultText", "ttt");
//	}
//	public String failMessage(@Body String body, Exception e, @Headers Map<String, Object> headers,  @OutHeaders Map<String, Object> outHeaders) {
	public String failMessage(@Body Object myBody, Exception e, @Headers Map<String, Object> headers,  @OutHeaders Map<String, Object> outHeaders) {
//	public String failMessage(String body, Exception e, Map<String, Object> headers,  Map<String, Object> outHeaders) {
//	try {
		String body = String.valueOf(myBody);
		
		String responseBody = null;
		
	try {
		try {
			HttpOperationFailedException httpe = (HttpOperationFailedException) e;
			responseBody = httpe.getResponseBody();
		} catch (Exception kkk) {
			
		}
		

		
//		see comment on convertStreamToString below	
//		body = convertStreamToString(myBody);
		
//		body = myBody.toString();
		
		final int clipAfter = 0;  // 0 means: do not clip
		StringBuilder sb = new StringBuilder();
		
		
		// get error message:
//		if (e.getMessage() != null || e.getCause() != null) {			
			if (e.getMessage() != null && e.getMessage().contains("testMode")) {
				sb.append("TEST MODE: ");
			} else {
				sb.append("ERROR: ");
				sb.append(String.valueOf(e.getClass()).replaceAll("^class ", "") + ": ");
			}		
				
			if (e.getMessage() != null) {
//				sb.append(String.valueOf(e.getClass()).replaceAll("^class ", "") + ": ");
				String sbString = e.getMessage();
				if (headers != null && headers.get("velocityFile") != null) {
					sbString = sbString.replaceFirst("VelocityEndpoint\\[", "VelocityEndpoint " + headers.get("velocityFile") + "[");
					sbString = sbString.replaceFirst("velocity script", "velocity script [" + headers.get("velocityFile") + "]");
				}
				sb.append(sbString);
			}
			
			if (e.getCause() != null)  { //Message() != null && ! e.getCause().getMessage().equals("")) {
				sb.append("\nERROR CAUSE: " + e.getCause());
			} // else 
			
			if (responseBody != null)  { //Message() != null && ! e.getCause().getMessage().equals("")) {
				sb.append("\nRESPONSEBODY: " + responseBody);
			} // else
			
//			if (e.getCause() != null) {
//				sb.append("\nERROR CAUSE: ");
//				sb.append(e.getCause());
//			} 
//		}
		
//		sb.append(e.getClass().toString().replaceAll("^class ", "") + ": ");
//		sb.append(e.getMessage());
//		sb.append(e.getCause());
		
		// get myStack info:
		sb.append("\nSTACK: ");
		
		if (headers == null) {
		  headers = new HashMap<String, Object>();
		}
		if (headers != null && headers.get("myStackList") != null && !headers.get("myStackList").toString().equals("[]")) {
			try {
				sb.append(String.valueOf(Lists.reverse((List<String>) headers.get("myStackList"))));
			} catch (Exception stacklistExeption) {
				// for debugging:
//				Object myTraceListObject = headers.get("myTraceList");
//				String myTraceListClass = headers.get("myTraceList").getClass().getName();
				try {
					sb.append(headers.get("myStackList").toString() + " (not reversed)");			
				} catch (Exception stacklistExeption2) {
					sb.append("(Error retrieving stack)");	
				}		
			}
			
//			finally {
//				sb.append("(Error reading stack)");
//			}
			
			// remove StackList, since it is too big to be transported as HTML header:
			headers.remove("myStackList");
		} else {
		  if (headers != null && headers.get("myStack") != null && !headers.get("myStack").toString().isEmpty()) {
		    sb.append(headers.get("myStack").toString());
		  }
		    
		}
		
		// get Trace info:
		
		if (headers != null && headers.get("myTraceList") != null) {
			sb.append("\nTRACE: ");
			try {
				sb.append(String.valueOf(Lists.reverse((List<String>) headers.get("myTraceList"))));				
			} catch (Exception tracelistExeption) {
				// for debugging:
//				Object myTraceListObject = headers.get("myTraceList");
//				String myTraceListClass = headers.get("myTraceList").getClass().getName();
				try {
					sb.append(headers.get("myTraceList").toString() + " (not reversed)");			
				} catch (Exception tracelistExeption2) {
					sb.append("(Error retrieving trace)");	
				}		
			} 
			
			// remove TraceList, since it is too big to be transported as HTML header:
			headers.remove("myTraceList");
		}
		

				
		// get header info:
		sb.append("\nHEADERS: ");
		//headers.toString().replaceAll("([Pp][Aa][Ss][Ss][a-zA-Z]=)([^, ]{1, }), ", "\1XXXXXX, ");		
//		sb.append(headers.toString().replaceAll("([Pp][Aa][Ss][Ss][a-z, A-Z, 0-9]*=)([^, ^}^\n]*)", "$1(removed)"));
		// now NullPointerSave
		sb.append(String.valueOf(headers).replaceAll("([Pp][Aa][Ss][Ss][a-z, A-Z, 0-9]*=)([^, ^}^\n]*)", "$1(removed)"));

//		sb.append(headers.toString());

		// get body (clipped after staticclipAfter integer)
		sb.append("\nBODY: ");
		if (clipAfter == 0 || body.length() <= clipAfter) {
			sb.append(String.valueOf(body).replaceAll("([Pp][Aa][Ss][Ss][a-z, A-Z, 0-9]*=)([^, ^}^\n]*)", "$1(removed)"));
		} else {
			sb.append(String.valueOf(body).replaceAll("([Pp][Aa][Ss][Ss][a-z, A-Z, 0-9]*=)([^, ^}^\n]*)", "$1(removed)"), 0, clipAfter);
			sb.append("... (clipped after " + clipAfter + " of " + body.length() + " chars)");
		}
		
		// does not seem to be written to the message...
//		if (headers != null) {
		if (headers.get("myExceptionMessages") != null) {
			headers.put("myExceptionMessages", e.getMessage() + ";" + headers.get("myExceptionMessages").toString());
		} else {
			headers.put("myExceptionMessages", e.getMessage());
//		headers.put("Content-Type", "text/html; charset=UTF-8");
//		headers.put("Content-Type", "text; charset=UTF-8");
		}
		
//		headers.put("Content-Type", "text/html; charset=UTF-8");
		
		if (e != null && e.getMessage() != null) {
		headers.put("ResultText", e.getMessage());
		headers.put("ResultCode", 1);
		}
		
//		else {
//			headers = new HashMap<String, Object>();
//			headers.put("myExceptionMessages", e.getMessage());
		// for the case headers is too large to be delivered back, it is more save to remove the header:
		headers.clear();
		headers.put("Content-Type", "text/html; charset=UTF-8");
//		}
		//.setHeader("Content-Type", constant("text/html; charset=UTF-8"))
		
		// does not seem to work:
		//return sb.toString().replaceAll("\n", "\r\n"); // replacement helps IE to display the linebreaks correctly
		// seems to work better and will be displayed correctly in IE and FireFox
		//System.out.println("class MyExceptionHandler: return value = <pre>" + sb.toString() + "</pre>");
		return "<pre>" + sb.toString() + "</pre>";
//		return "mmmmmmmmmmmmmmmm";
		//return sb.toString(); // replacement helps IE to display the linebreaks correctly
	} catch (Exception exeptionHandlerException) {
		String myBodyString =  "Exception in Exeption Handler: " + exeptionHandlerException.getMessage();
		myBody = (Object) myBodyString;
		// for the case headers is too large to be delivered back, it is more save to remove the header:
		headers.clear();
		headers.put("Content-Type", "text/html; charset=UTF-8");
		return myBodyString;
	}
	}
	
//	} catch (Exception exeptionHandlerException) {
//	
//	}
	
	// testing to convert stream to Sting (http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string), but body is of type Object.
	// for now, the conversion needs to be done before the failMessage is called, using 
	// .convertBodyTo(String.class)
//	private static String convertStreamToString(java.io.InputStream is) {
//	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
//	    return s.hasNext() ? s.next() : "";
//	}
		
}
