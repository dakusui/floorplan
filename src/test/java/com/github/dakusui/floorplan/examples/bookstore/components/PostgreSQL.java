package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Operator;

import static com.github.dakusui.floorplan.UtUtils.printf;
import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.resolver.Resolvers.slotValue;

/**
 * This is just an example.
 * <p>
 * This class models an instance of PostgreSQL DMBS and is intended to illustrate
 * how 'floorplan' library works and is to be used.
 * <p>
 * When you implement your own model for real purpose, you will need to execute
 * actual commands instead of just printing them and learn specification of component
 * for which you are creating model and implement your own class. But still this
 * class gives you a good idea about what you will need to do.
 */
public class PostgreSQL {
  public enum Attr implements Attribute {
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(Integer.class).defaultsTo(slotValue("port")).$()),
    BOOKSTORE_DATABASE(SPEC.property(String.class).defaultsTo(immediate("bookstore_db")).$()),
    BASEDIR(SPEC.property(String.class).defaultsTo(immediate("/usr/local/postgresql")).$()),
    DATADIR(SPEC.property(String.class).defaultsTo(immediate("/var/postgresql/data")).$());
    private final Bean<Attr> bean;

    Attr(Bean<Attr> bean) {
      this.bean = bean;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Bean<Attr> bean() {
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
                printf("ssh -l root@%s yum install postgresql", component.<String>valueOf(Attr.HOSTNAME));
              }),
              $.simple("initdb", () -> {
                printf("ssh -l root@%s postgresql-setup initdb", component.<String>valueOf(Attr.HOSTNAME));
              }),
              $.named("Update postgresql.conf",
                  $.sequential(
                      $.simple("Update port", () -> {
                        printf(
                            "ssh -l root@%s sed -i /etc/postgresql.conf s/PGPORT=.+/PGPORT=%s/g",
                            component.<String>valueOf(Attr.HOSTNAME),
                            component.<Integer>valueOf(Attr.PORTNUMBER)
                        );
                      }),
                      $.simple("Update port", () -> {
                        printf(
                            "ssh -l root@%s sed -i /etc/postgresql.conf s/DATADIR=.+/DATADIR=%s/g",
                            component.<String>valueOf(Attr.HOSTNAME),
                            component.<String>valueOf(Attr.DATADIR));
                      })
                  )
              )
          )
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.START,
          component -> $ -> $.simple(
              "pg_ctl start",
              () -> {
                printf("ssh -l postgres@%s pg_ctl start");
              }
          )
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.STOP,
          component -> $ -> $.simple(
              "pg_ctl stop",
              () -> {
                printf("ssh -l postgres@%s pg_ctl stop");
              }
          )
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.NUKE,
          component -> $ -> $.simple(
              "send kill -9",
              () -> {
                printf("ssh -l root@%s pkill -9 postgres", component.valueOf(Attr.HOSTNAME));
              })
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.UNINSTALL,
          component -> $ -> $.sequential(
              $.simple("remove basedir", () -> {
                printf("rm -fr %s", component.valueOf(Attr.BASEDIR));
              }),
              $.simple("remove datadir", () -> {
                printf("rm -fr %s", component.valueOf(Attr.DATADIR));
              }))
      )
  ).build();
}
