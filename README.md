# The FloorPlan library
<<<<<<< HEAD
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

=======
FloorPlan is a library to model a heterogeneous software system's for testing.

A challenge in system level automated testing is a difficulty for modeling SUT
in a way where you can 
1. deploy your SUT automatically and repeatably
2. test deployment variations themselves
3. deploy your SUT on various physical environments
4. reuse and run your tests by combinating your test scenario, environments, and 
  deployment from your repartoire
  
It gives a programmed way to achieve those.
For more detail, see our wiki.
>>>>>>> fef2649bde295b7e618aee0d18fffe5727c3dd37

## Installation and how to use it
Following is maven coordinate.

```xml
  <dependency>
    <groupId>com.github.dakusui</groupId>
    <artifactId>floorplan</artifactId>
    <version>[2.2.2)</version>
  </dependency>
```

<<<<<<< HEAD
=======

## Building FloorPlan library.
This library is built and tested with following JDK and Maven.
```
// JDK
java version "1.8.0_172"
Java(TM) SE Runtime Environment (build 1.8.0_172-b11)
Java HotSpot(TM) 64-Bit Server VM (build 25.172-b11, mixed mode)
// Maven
Apache Maven 3.5.3 (3383c37e1f9e9b3bc3df5050c29c8aff9f295297; 2018-02-25T04:49:05+09:00)
```
 
To build this, clone this repo and run following maven command.

```
// clone
$ git clone https://github.com/dakusui/floorplan.git
// build and test
$ mvn clean compile test

```

# Basic ideas
```FloorPlan``` can be considered a meta-framework library for testing by which 
you create your own testing framework for your own SUT.

Basic concepts of it are,
1. Separate a physical environment and a logical structure of the SUT, which are
  respectively called 'Profile' and 'FloorPlan'. 

(t.b.d.)
```
  FloorPlan      ->   FixtureConfigurator   ->    Fixture
  (Refs)               (Configurators)         (Components)
```


A component is an immutable object and built by its builder, a configurator.
A configurator is created from a spec of a component and an identifier by which
you can specify a unique instance among ones to which belong to the same component
spec.

A component is built by a configurator.

## Testing process for HDS

* 4-phase testing
  * setUp
  * exercise and verify
  * tearDown



## Defining specification of your SUT
>>>>>>> fef2649bde295b7e618aee0d18fffe5727c3dd37

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
