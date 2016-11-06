RESTful Velocity Template Engine
================================

This is a RESTful Template Engine for Velocity templates based on the new REST DSL of Apache Camel (available as of 2.14.0). It can create, read, update and delete and evaluate velocity templates, i.e. it can apply velocity template semantics on stored templates or on templates that are sent in the body of the request.

To build this project use

    mvn install

To run this project from source with Maven use

    mvn camel:run
    
To run this project from jar use (tested with jdk1.7.0_09):

    java.exe -jar camel-spring2.jar
    
The Server Port can be configured in the file properties/velocity-template-temple.properties:
    
    e.g.
    inputport=80

Functions:
==========

Apply template that is found in the body:
	send a POST to http://<server>:<port>/templates/apply with the template in the body and the variables in the header.
	Response Codes: 200 OK, if the operation was successful.
	-> This operation can be used as a stateless microservice.
	Optional parameter: resolution=forced (default: undefined/null). Will cause an exit code 404 (Header) not found, if one of the variables in the template could not be resolved; i.e. if there is variable in the body that is missing in the header. 
	
Apply template named "mytemplate":
	send a POST to http://<server>:<port>/templates/mytemplate/apply with the variables in the header.
	Response Codes: 200 OK, if the operation was successful; 404 Not Found, if the template does not exist. The resource is not changed.
	
Create a template named "mytemplate": 
	send a POST to http://<server>:<port>/templates/mytemplate with the template content in the HTTP body.
	Response Codes: 201, if successfully created; 409, if it exists already.
	Optional parameter: resolution=forced (default: undefined/null). Will cause an exit code 404 (Header) Not Found, if one of the variables in the template could not be resolved; i.e. if there is variable in the template file that is missing in the header. 

Read template named "mytemplate": 
	send a GET to http://<server>:<port>/templates/mytemplate. The template will be received in the body.
	Response Codes: 200 OK, if it was read successfully; 404, if it was not found.

Update template named "mytemplate": 
	send a PUT to http://<server>:<port>/templates/mytemplate with the new template content in the HTTP body.
	Response Codes: 201 OK, if it was created; 200 OK, if it was updated successfully

Delete template named "mytemplate":
	send a DELETE to http://<server>:<port>/templates/mytemplate
	Response Codes: 204 No Content with empty body, if it was Deleted successfully. 404 Not Found, if the template does not exist.

    
How to perform Performance Monitoring:
======================================

Start the jar with the possibility to attach JConsole:
1) start the VM with remote monitoring enabled:
   java.exe -Dorg.apache.camel.jmx.createRmiConnector=true -jar camel-spring2.jar
   
   (
   example: on Windows with the full path and prompt in my case:
   F:\veits\Kunden\252_CCS_Support (offer accepted)\Alpha3\CloudCdcConverter\micro-services>"C:\Program Files\Java\jdk1.7.0_09\bin\java.exe" -Dorg.apache.camel.jmx.createRmiConnector=true -jar camel-spring2.jar
   )

2) start JConsole or better jvisualvm.exe: 
   C:\Program Files\Java\jdk1.7.0_09\bin\jconsole.exe
   or better
   C:\Program Files\Java\jdk1.7.0_09\bin\jvisualvm.exe
   
3) connect to remote:
   in jconsole, upon startup, select remove and paste the following URI into the input field (replace DE04058W by your server's name):
   service:jmx:rmi:///jndi/rmi://DE04058W:1099/jmxrmi/camel (or the corresponding URI shown during startup in 1)
   in jvisualvm.exe, menu -> File -> Add JMX connection and add the URI
   ervice:jmx:rmi:///jndi/rmi://DE04058W:1099/jmxrmi/camel (or the corresponding URI shown during startup in 1)
     

How to build the project using gradle
=====================================

To create a runnable jar, perform the following command in the project root:

   gradle jar

To run the jar, perform (you might need to replace the version number):

   java -jar build/libs/velocity-temple-0.0.1-SNAPSHOT.jar

To create a runnable jar for the JUnit tests, perform the following command in the project root:

   gradle testJar

To run the test jar:

   1) start the main application, if not already done
   2) java -jar build/libs/velocity-temple-test-0.0.1-SNAPSHOT.jar
