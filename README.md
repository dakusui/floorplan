# The FloorPlan library
FloorPlan is a library to model a heterogeneous software system's specification
for testing.

A challenge in system level automated testing is in modeling SUT in a way where
you can

1. deploy your SUT automatically and repeatably
2. verify deployment variations.
3. deploy your SUT in various physical environments (dev, stg, prod, local) while
  a test scenario can work transparently.
4. reuse and run your tests by combinating your test scenario, environments, and 
  deployment from your repertoire.

at the same time.
  
This library gives a programmable way to achieve those. For more detail,
see our [our wiki][1].


## Installation and how to use it
Following is maven coordinate.

```xml
  <dependency>
    <groupId>com.github.dakusui</groupId>
    <artifactId>floorplan</artifactId>
    <version>[2.2.2)</version>
  </dependency>
```


## Building FloorPlan library.
This library is built and tested with following JDK and Maven.
```
$ mvn --version
Apache Maven 3.3.9
Maven home: /usr/share/maven
Java version: 1.8.0_171, vendor: Oracle Corporation
Java home: /usr/lib/jvm/java-8-oracle/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "4.4.0-130-generic", arch: "amd64", family: "unix"
```
 
To build it, clone this repo and run following maven command.

```
$ mvn clean compile test

```

# References
* [1]: FloorPlan Wiki

[1]: https://github.com/dakusui/floorplan/wiki
