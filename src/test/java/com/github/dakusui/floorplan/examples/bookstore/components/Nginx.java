package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.resolver.Resolvers;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.floorplan.UtUtils.printf;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;

public class Nginx {
  public enum Attr implements Attribute {
    @SuppressWarnings("unchecked")
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(Integer.class).defaultsTo(slotValue("port")).$()),
    BOOKSTORE_APPNAME(SPEC.property(String.class).defaultsTo(immediate("bookstore")).$()),
    UPSTREAM(SPEC.property(List.class).defaultsTo(listOf(Ref.class)).$()),
    @SuppressWarnings("unchecked")
    ENDPOINT(SPEC.property(String.class).defaultsTo(Resolvers.transform(
        listOf(
            Object.class,
            referenceTo(HOSTNAME),
            referenceTo(PORTNUMBER),
            referenceTo(BOOKSTORE_APPNAME)
        ),
        args -> String.format("https://%s:%s/%s", args.get(0), args.get(1), args.get(2))
    )).$());
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
              $.simple(
                  "yum install",
                  () -> printf("ssh -l root@%s 'yum install nginx'", component.<String>valueOf(Attr.HOSTNAME))),
              $.simple(
                  "configure",
                  () -> printf(
                      "ssh -l root@%s, echo \"upstream dynamic {%n" +
                          "%s",
                      "}\" > /etc/nginx.conf%n",
                      component.<String>valueOf(Attr.HOSTNAME),
                      new LinkedList<String>() {{
                        IntStream.range(0, component.sizeOf(Attr.UPSTREAM)).mapToObj(
                            i -> component.<Component<BookstoreApp.Attr>>valueOf(Attr.UPSTREAM, i)
                        ).forEach(
                            (app) -> add(
                                String.format(
                                    "  server:%s:%s",
                                    app.<String>valueOf(BookstoreApp.Attr.WEBSERVER_HOST),
                                    app.<String>valueOf(BookstoreApp.Attr.WEBSERVER_PORT)
                                )
                            ));
                      }}
                  )
              )
          )
      )
  ).build();
}
