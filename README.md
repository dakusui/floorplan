# The FloorPlan library
FloorPlan is a library to model a heterogeneous software system's software syste
for testing.

```
  FloorPlan      ->   DeploymentConfigurator -> Deployment
  (Refs)               (Configurators)         (Components)
```


A component is considered to be a bundle of attributes each of whose value is 
given by a object called "Resolver".

A component is an immutable object and built by its builder, a configurator.
A configurator is created from a spec of a component and an identifier by which
you can specify a unique instance among ones to which belong to the same component
spec.



A component is built by a configurator.

# References
* ActionUnit (A library to write scenarios and programs as objects inside Java)
* Cmd (A library to execute command line tools from Java)