package es.mjsplayground.acda.hibernate.biblioteca.config;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import java.util.function.Supplier;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

public final class HibernateFactory implements Supplier<SessionFactory>, AutoCloseable {
  private SessionFactory factory;

  public static SessionFactory crear(String url, String usuario, String clave, String ddl) {
    if (!java.util.Set.of("update", "create-drop", "validate").contains(ddl))
      throw new IllegalArgumentException("Modo DDL no permitido");
    return new Configuration()
        .addAnnotatedClass(Editorial.class)
        .addAnnotatedClass(Autor.class)
        .addAnnotatedClass(Libro.class)
        .addAnnotatedClass(Detalle.class)
        .setProperty("hibernate.connection.url", url)
        .setProperty("hibernate.connection.username", usuario)
        .setProperty("hibernate.connection.password", clave)
        .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
        .setProperty("hibernate.hbm2ddl.auto", ddl)
        .setProperty("hibernate.show_sql", "false")
        .setProperty("hibernate.format_sql", "true")
        .setProperty("hibernate.generate_statistics", "true")
        .buildSessionFactory();
  }

  public synchronized SessionFactory get() {
    if (factory == null)
      factory =
          crear(
              System.getenv()
                  .getOrDefault(
                      "MYSQL_URL",
                      "jdbc:mysql://127.0.0.1:3308/biblioteca_guiado?serverTimezone=UTC&connectTimeout=3000&socketTimeout=5000"),
              System.getenv().getOrDefault("MYSQL_USER", "acda"),
              System.getenv().getOrDefault("MYSQL_PASSWORD", "acda_local"),
              "update");
    return factory;
  }

  public synchronized void close() {
    if (factory != null) factory.close();
  }
}
