package es.mjsplayground.acda.firestore.reservas.config;

import com.google.auth.oauth2.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.*;
import com.google.firebase.cloud.FirestoreClient;
import java.util.Date;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Configuration
public class FirestoreConfig {
  @Bean(destroyMethod = "delete")
  FirebaseApp firebaseApp(Environment env) throws Exception {
    String modo = env.getProperty("reservas.firestore.modo", "emulador");
    String proyecto = env.getProperty("reservas.firestore.proyecto", "demo-acda-reservas");
    FirebaseOptions.Builder options = FirebaseOptions.builder().setProjectId(proyecto);
    if (modo.equals("emulador")) {
      if (!proyecto.startsWith("demo-"))
        throw new IllegalArgumentException("El modo emulador requiere un proyecto demo-.");
      String host = env.getProperty("FIRESTORE_EMULATOR_HOST", "127.0.0.1:8085");
      options.setCredentials(
          GoogleCredentials.create(new AccessToken("solo-emulador", new Date(Long.MAX_VALUE))));
      options.setFirestoreOptions(
          FirestoreOptions.newBuilder().setEmulatorHost(host).setProjectId(proyecto).build());
    } else if (modo.equals("cloud")) {
      if (proyecto.startsWith("demo-"))
        throw new IllegalArgumentException("Configura proyecto real para cloud explícitamente.");
      options.setCredentials(GoogleCredentials.getApplicationDefault());
    } else throw new IllegalArgumentException("Modo permitido: emulador o cloud");
    return FirebaseApp.initializeApp(options.build(), "reservas-" + java.util.UUID.randomUUID());
  }

  @Bean(destroyMethod = "close")
  Firestore firestore(FirebaseApp app) {
    return FirestoreClient.getFirestore(app);
  }
}
