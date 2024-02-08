# OSGi examples

All the examples were developed for OSGi R8 containers, but most of them should run in containers compliant with older OSGi releases (perhaps after some downgrade of the dependencies). [Karaf](http://karaf.apache.org/) is the recommended container for the demonstration as it provides many features out of the box and the missing features can be installed easily.

Following features are required or recommended:

```
# Install Declarative Services
feature:install scr

# Install Http Whiteboard
feature:install pax-web-http-whiteboard

# Install webconsole
# This requires Http Whiteboard already
feature:install webconsole

# Install Blueprint Container
feature:install aries-blueprint

# Install CDI support
feature:install cdi

# Install JAX-RS Whiteboard and support JSON provided by Jackson
feature:repo-add mvn:org.apache.cxf.karaf/apache-cxf/3.6.2/xml/features
feature:repo-add mvn:org.apache.aries.jax.rs/org.apache.aries.jax.rs.features/2.0.2/xml
feature:install aries-jax-rs-whiteboard
feature:install aries-jax-rs-whiteboard-jackson

# Install war support for deploying WAR artifacts
feature:install war
# Then deploy a WAR by dropping to deploy folder (but it may not work),
# or use following command:
#
# bundle:install webbundle:{URL}?Web-ContextPath={context}
```
