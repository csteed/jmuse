## jMUSE


**jMUSE** is an interactive visualization tool for exploring quantitative multivariate data in a scatterplot matrix.  **jMUSE** is written in Java and runs on Mac OS X, Windows, and Linux operating systems. **jMUSE** is developed and maintained by the [Chad A. Steed](http://csteed.github.com/). 

### Compiling the jMUSE Source Code

Compiling **jMUSE** is straightforward.  The first step is to clone the repository.  We supply a [Maven](http://maven.apache.org/) POM file to deal with the dependencies.  In the Eclipse development environment, import the code as a Maven project and Eclipse will build the class files.  

To compile **jMUSE** on the command line, issue the following commands:

```
$ mvn compile
$ mvn package
```

### Running jMuse

These commands will generate 2 jar files in the target directory.  Copy the jar file with dependencies into the scripts directory and run either the jmuse.bat script (Windows) or the jmuse.sh script (Mac or Linux).  The **jMUSE** GUI should appear after issuing this command.  Example data files are provided in the data directory for trying **jMUSE** out.  

