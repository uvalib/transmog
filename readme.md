# Transmog

Transmog is a simple web application to aid in the conversion of Word documents into more meaningfully
structured XML documents.  Originally designed to convert archival finding aids into EAD XML files,
this application now supports arbitrary profiles defining the structure and rules for for the generated XML.
 
## Running

To more easily evaluate Transmog, download the latest [release](http://github.com/uvalib/transmog/releases/ "latest release") and launch it either by double clicking on it or typing the following on the command line.

```
 java -jar transmog-0.1.jar
```

This will launch a window from which you can start and stop the transmog server.  Click "Start", then visit http://localhost:8080/ in your browser.


## Building
To run locally using Maven, simply type: 

```
 mvn clean compile jetty:run
```

And visit localhost:8080 in your browser.

