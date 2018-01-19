# Match Up

A REST services find out the free time of two people's google calendars.

## Getting Started


### Prerequisites

Before we begin, open a terminal to check that you have valid versions of Java and Maven installed.

```
$ java -version
$ mvn -v
```

### Installing

Installation is quite easy, first you will have to install some front-end dependencies using Bower:


```
bower install
```

Then you can run Maven to package the application:

```
mvn clean package
```

Now you can run the Java application quite easily:

```
cd target
java -jar ng-spring-boot-1.0.0.jar
```

### Run the application

###### Open user interface

```
open calendar-ui/app/index.html 
```

###### Run service
```
mvn spring-boot:run
```

###### Input two calendars' id and time interval, for example,


First Calendar
```
u9udoeo3tukabp92990sbe3c2s@group.calendar.google.com
```
Second Calendar:
```
v2qv94guse9j2f79iq5tgiqm0s@group.calendar.google.com
```
Start Time:
```
2018-01-08 06:00:00
```
End Time:
```
2018-01-08 10:00:00
```
Time Zone:
```
EST
```

## Frameworks

This is a REST services with Spring Boot and Angular.

### Front-end
AngularJS

### Back-end
Spring Boot

Spring Data JPA

Google Calendar API

## Built With

* [AngularJS](https://angularjs.org/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - The back-end used
* [Google Calendar API](https://developers.google.com/google-apps/calendar/) - Google API

## Authors

* **Yuqi (Eugene) Zhu** - [Yuqi (Eugene) Zhu](https://github.com/YuqiZhu29)

## Acknowledgments

* Inspired by [Bhavani Shekhawat](https://github.com/bytekoder)

