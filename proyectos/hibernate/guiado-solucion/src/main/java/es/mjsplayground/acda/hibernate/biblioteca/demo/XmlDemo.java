package es.mjsplayground.acda.hibernate.biblioteca.demo;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XmlDemo {
  private static final Logger log = LoggerFactory.getLogger(XmlDemo.class);

  public static SessionFactory configurar(String url, String usuario, String clave, String ddl) {
    return new Configuration()
        .addResource("demo/nota-orm.xml")
        .setProperty("hibernate.connection.url", url)
        .setProperty("hibernate.connection.username", usuario)
        .setProperty("hibernate.connection.password", clave)
        .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
        .setProperty("hibernate.hbm2ddl.auto", ddl)
        .buildSessionFactory();
  }

  public static void main(String[] args) {
    String url =
        System.getenv()
            .getOrDefault(
                "MYSQL_URL", "jdbc:mysql://127.0.0.1:3308/biblioteca_guiado?serverTimezone=UTC");
    try (SessionFactory factory =
        configurar(
            url,
            System.getenv().getOrDefault("MYSQL_USER", "acda"),
            System.getenv().getOrDefault("MYSQL_PASSWORD", "acda_local"),
            "update")) {
      Long id =
          factory.fromTransaction(
              s -> {
                NotaXml nota = new NotaXml("Mapeo XML de demostración");
                s.persist(nota);
                return nota.getId();
              });
      String texto = factory.fromTransaction(s -> s.find(NotaXml.class, id).getTexto());
      log.info("Nota XML {} recuperada: {}. Solo se utiliza la tabla demo_notas_xml.", id, texto);
    }
  }
}
