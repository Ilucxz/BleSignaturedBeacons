SpringBoot Webserver:

Run on command line:

$ mvn clean package
$ java -jar target/spring-boot-demo-0.0.1.jar


Test:

$ curl http://localhost:8080/



Bachelorarbeit:

pom.xml = Maven Setup / Log4J2
\spring-boot-demo\target\spring-boot-demo-0.0.1.jar = executable Jar-File
\spring-boot-demo\src\main\java\io\moxd\springdemo\Controller.java = SeverMappings für Requests und Verifizierung
\spring-boot-demo\src\main\resources\application.properties = Anwendungseinstellungen
\spring-boot-demo\src\main\resources\log4j.properties = Konfiguration Log4J2
\spring-boot-demo\logs\spring-boot-logging.log = Log4J2 Log-Datei