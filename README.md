# doc-share-server
## Description
The users of the application can edit and remove documents. In addition they can share their documents with others and allow them to make changes. The application supports multiple users changing documents simultaneously. 

**In the link below you can find all the features and design documents of the application:**

https://drive.google.com/drive/u/1/folders/1b68dFfrmoLxDh0SyZWbXZgOHyMQtf36Q
## Requirements

For building and running the application you need:

- [JDK 9](https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html)
- [Maven 4](https://maven.apache.org/ref/4-LATEST/)

### Run the application
In the SpringApp.java file, click on the green play button next to the public class definition and select the Run option as seen below:

<img width="364" alt="image" src="https://user-images.githubusercontent.com/58644583/204894989-57f09fab-7673-49c6-9c6f-ad7862f4b615.png">

### Dependencies
There are a number of third-party dependencies used in the project. Browse the Maven pom.xml file for details of libraries and versions used.

## application.properties
The **application.properties** file is located in the src/main/resources directory. The code for sample application.properties file is given below :
```
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/docs
spring.datasource.username=root
spring.datasource.password=S263safa
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
server.error.include-message=always
server.port=8081
email= email for sending the activation messages
password= password for the email
host=localhost
client=localhost
client.port=9000
page.activation=activation.html
```

### client repository
https://github.com/otabeksherman/doc-share-client.git

# Authors
Otabek Sherman - otabek.sherman@gmail.com

Gideon Jaffe - gideon.jaffe@gmail.com

Safaa Azbarqa - safaa8721@gmail.com

