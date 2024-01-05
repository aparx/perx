package io.github.aparx.perx.message;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 07:29
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class MessageMap implements MessageRegister {

  private final Map<ArrayPath, LocalizedMessage> messageMap;

  public MessageMap() {
    this(new HashMap<>());
  }

  public MessageMap(Map<ArrayPath, LocalizedMessage> messageMap) {
    Preconditions.checkNotNull(messageMap, "Map must not be null");
    this.messageMap = messageMap;
  }

  @Override
  public LocalizedMessage get(ArrayPath path) {
    @Nullable LocalizedMessage message = messageMap.get(path);
    return (message == null ? LocalizedMessage.of(path.toString()) : message);
  }

  @Override
  public LocalizedMessage get(String path) {
    return get(ArrayPath.parse(path));
  }

  @Override
  public @Nullable LocalizedMessage set(ArrayPath path, LocalizedMessage message) {
    Preconditions.checkNotNull(path, "Path must not be null");
    Preconditions.checkNotNull(message, "Message must not be null");
    return messageMap.put(path, message);
  }

  @Override
  public boolean contains(ArrayPath path) {
    return messageMap.containsKey(path);
  }

  @Override
  public void forEach(BiConsumer<ArrayPath, LocalizedMessage> action) {
    messageMap.forEach(action);
  }

}
