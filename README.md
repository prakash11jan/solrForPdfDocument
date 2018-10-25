# solrForPdfDocument
parsing pdf using tika and indexing in solr and fetching documents

In this sample project, i am trying to solve problems like
1. parsing pdf content and search through a key word
2. indexing those in solr
3. search by keyword which present in pdf document.
4. enabling authenticated and non authenticated docs handling

Steps to run:
1. create afolders mentioned in application.properties
2. put the appropriate pdf documents
3.install solr
4.install maven
5. clone my project
7.run mvn clean and mvn intall in eclipse
else 
go to POM.xml directory and run below
mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

