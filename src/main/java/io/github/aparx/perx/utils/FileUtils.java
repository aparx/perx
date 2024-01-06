package io.github.aparx.perx.utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 17:05
 * @since 1.0
 */
public final class FileUtils {

  private FileUtils() {
    throw new AssertionError();
  }

  @CanIgnoreReturnValue
  public static boolean createFileIfNotExists(File file) {
    if (file.exists()) return false;
    @Nullable File parentFile = file.getParentFile();
    if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs())
      throw new IllegalStateException("Could not create parent dirs");
    try {
      if (!file.createNewFile())
        throw new IllegalStateException("Could not create file");
      return true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
