# The FloorPlan library
FloorPlan is a library to model a heterogeneous software system's software syste
for testing.

```
  FloorPlan      ->   FixtureConfigurator   ->    Fixture
  (Refs)               (Configurators)         (Components)
```


A component is an immutable object and built by its builder, a configurator.
A configurator is created from a spec of a component and an identifier by which
you can specify a unique instance among ones to which belong to the same component
spec.

A component is built by a configurator.

## Installation and how to use it
Following is maven coordinate.

```xml
  <dependency>
    <groupId>com.github.dakusui</groupId>
    <artifactId>floorplan</artifactId>
    <version>[2.0.0)</version>
  </dependency>
```

# Basic ideas
```FloorPlan``` can be considered a meta-framework library by which you can create
your own testing framework for your SUT.

A challenge in testing a heterogeneous distributed system (HDS) its complexity.

## Testing process for HDS

* 4-phase testing
  * setUp
  * exercise and verify
  * tearDown



## Defining specification of your SUT

```

    +---------+        +-------------------+        +-------+
    |FloorPlan| - - - >|FixtureConfigurator| - - - >|Fixture|
    +---------+        +-------------------+        +-------+
         |1                       |1                    |1
         |                        |                     |
         |                        |                     |
         V*                       V*                    V*
       +---+               +------------+          +---------+
       |Ref|               |Configurator|          |Component|
       +---+               +------------+          +---------+
                                  |1                    |1
                                  |                     |
                                  |                     |
                                  V*                    V*
                             +---------+            +---------+
                             |Attribute|            |Attribute|
                             +---------+            +---------+

````

### Ref(Reference)
(t.b.d.)

### Component
A component is considered to be a bundle of attributes each of whose value is 
given by an object called "Resolver".

### (Component)Configurator
(t.b.d.)
### Attribute
(t.b.d.)
### Fixture
(t.b.d.)
### FixtureConfigurator
(t.b.d.)
### FloorPlan



# References
* ActionUnit (A library to write scenarios and programs as objects inside Java)
* Cmd (A library to execute command line tools from Java)
* 'HDS' is a word coined by the author and it does not have anything to do with
  Hitachi Data Systems(tm) nor Hinomaru Driving School(tm).