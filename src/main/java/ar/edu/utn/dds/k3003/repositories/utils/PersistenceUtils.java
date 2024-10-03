package ar.edu.utn.dds.k3003.repositories.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceUtils {

  public static EntityManagerFactory createEntityManagerFactory() {
    String[] keys = new String[] {"javax.persistence.jdbc.url", "javax.persistence.jdbc.user",
        "javax.persistence.jdbc.password"};

    Map<String, String> env = System.getenv();
    Map<String, Object> configOverrides = Arrays.stream(keys)
        .filter(env::containsKey)
        .collect(Collectors.toMap(key -> key, env::get));

    return Persistence.createEntityManagerFactory("postgres", configOverrides);
  }

}