package io.github.aparx.perx.config.configs;

import io.github.aparx.perx.config.Config;
import io.github.aparx.perx.config.ConfigHandle;
import io.github.aparx.perx.config.ConfigHandleId;
import io.github.aparx.perx.config.ConfigManager;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 06:51
 * @since 1.0
 */
@ConfigHandleId("database")
public class DatabaseConfig extends ConfigHandle {

  public static final ArrayPath DATABASE_URL = ArrayPath.of("database", "url");
  public static final ArrayPath DATABASE_USERNAME = ArrayPath.of("database", "username");
  public static final ArrayPath DATABASE_PASSWORD = ArrayPath.of("database", "password");

  public DatabaseConfig(ConfigManager configManager) {
    super(configManager);
  }

  @Override
  public void init(@NonNull Config config) {
    config.load();
    config.setIfAbsent(DATABASE_URL, "jdbc:<type>://<host>", "The JDBC connectivity string");
    config.setIfAbsent(DATABASE_USERNAME, "<username>", "The target username of the database");
    config.setIfAbsent(DATABASE_PASSWORD, "<password>", "The password of the database");
    config.save();
  }

  public String getURL() {
    return getRequiredString(DATABASE_URL);
  }

  public String getUsername() {
    return getRequiredString(DATABASE_USERNAME);
  }

  public String getPassword() {
    return getRequiredString(DATABASE_PASSWORD);
  }

}
