
Remote widget set compilation service
===
This project contains a client library used to access the on-line widgetset compilation service (CDN).


The service uses Maven to compile the requested widgetset on the fly. Generation creates a unique ID for the widgetset based on the the following configuration:
- Vaadin version 
- all add-on dependencies in the project
- eager loading of widgets (by default deferred loading is used)
- GWT compilation style (e.g. OBF, PRETTY)

The Vaadin Maven plugin can be used to automatically generate 
widget set definition and Java code for the client and access
the widget set compilation service. It uses the project
classpath to resolve the included add-ons. 

