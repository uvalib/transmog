# Transmog

Transmog is a simple web application to aid in the conversion of Word documents into more meaningfully
structured XML documents.  Originally designed to convert archival finding aids into EAD XML files,
this application now supports arbitrary profiles defining the structure and rules for for the generated XML.
 
## Building
To run locally using Maven, simply type: 

```
 mvn clean compile jetty:run
```

And visit localhost:8080 in your browser.

