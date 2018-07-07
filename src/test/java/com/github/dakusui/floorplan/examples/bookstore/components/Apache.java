package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Operator;

import static com.github.dakusui.floorplan.UtUtils.printf;

import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.resolver.Resolvers.slotValue;

public class Apache {
  public enum Attr implements Attribute {
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(Integer.class).defaultsTo(slotValue("port")).$()),
    DATADIR(SPEC.property(String.class).defaultsTo(immediate("/var/apache/www")).$());
    private final Bean<Attr> bean;

    Attr(Bean<Attr> bean) {
      this.bean = bean;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Attribute.Bean<Attr> bean() {
      return this.bean;
    }
  }

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(
      Attr.class
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.INSTALL,
          component -> $ -> $.sequential(
              $.simple("yum install", () -> {
                printf("ssh -l root@%s yum install httpd", component.<String>valueOf(Attr.HOSTNAME));
              }),
              $.simple("update datadir", () -> {
                printf(
                    "ssh -l root@%s sed -i /etc/httpd.conf 's/^<Directory .+/<Directory \"%s\">/g'",
                    component.<String>valueOf(Attr.HOSTNAME),
                    component.<String>valueOf(Attr.DATADIR)
                );
              }),
              $.simple("update port number", () -> {
                printf(
                    "ssh -l root@%s sed -i /etc/httpd.conf 's/^Listen .+/Listen %s/g'",
                    component.<String>valueOf(Attr.HOSTNAME),
                    component.<String>valueOf(Attr.PORTNUMBER)
                );
              })
          )
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.START,
          component -> $ -> $.simple("start", () -> {
            printf("ssh -l httpd@%s apachectl start", component.<String>valueOf(Attr.HOSTNAME));
          })
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.STOP,
          component -> $ -> $.simple("stop", () -> {
            printf("ssh -l httpd@%s apachectl stop", component.<String>valueOf(Attr.HOSTNAME));
          })
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.NUKE,
          component -> $ -> $.simple("", () -> {
            printf("ssh -l root@%s pkill -9 stop", component.<String>valueOf(Attr.HOSTNAME));
          })
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.UNINSTALL,
          component -> $ -> $.simple("", () -> {
            printf("ssh -l root@%s yum remove httpd", component.<String>valueOf(Attr.HOSTNAME));
          })
      )
  ).build();
}
