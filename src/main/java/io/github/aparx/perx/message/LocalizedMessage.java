package io.github.aparx.perx.message;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Map;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 07:22
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class LocalizedMessage {

  public static final String ARGUMENT_PREFIX = "{";
  public static final String ARGUMENT_SUFFIX = "}";
  public static final char ARGUMENT_ESCAPE = '\\';

  private final String message;
  private final String formatted;

  private final @Nullable StringLookup defaultLookup;
  private final @Nullable StringSubstitutor defaultSubstitutor;

  protected LocalizedMessage(String message) {
    this(message, null);
  }

  protected LocalizedMessage(String message, @Nullable StringLookup defaultLookup) {
    Preconditions.checkNotNull(message, "Message must not be null");
    this.message = message;
    this.defaultLookup = defaultLookup;
    this.formatted = ChatColor.translateAlternateColorCodes('&', message);
    this.defaultSubstitutor = (defaultLookup != null ? createSubstitutor(defaultLookup) : null);
  }

  public static LocalizedMessage of(String message) {
    return new LocalizedMessage(message);
  }

  public static LocalizedMessage of(String message, @Nullable StringLookup defaultLookup) {
    return new LocalizedMessage(message, defaultLookup);
  }

  private static StringSubstitutor createSubstitutor(StringLookup lookup) {
    Preconditions.checkNotNull(lookup, "String lookup must not be null");
    return new StringSubstitutor(lookup, ARGUMENT_PREFIX, ARGUMENT_SUFFIX, ARGUMENT_ESCAPE);
  }

  public @Nullable StringLookup getDefaultLookup() {
    return defaultLookup;
  }

  public @Nullable StringSubstitutor getDefaultSubstitutor() {
    return defaultSubstitutor;
  }

  /** Returns the message in which alternate color codes are translated */
  public String getMessage() {
    return formatted;
  }

  public String getRawMessage() {
    return message;
  }

  public LocalizedMessage withDefaultLookup(@Nullable StringLookup defaultLookup) {
    return new LocalizedMessage(getRawMessage(), defaultLookup);
  }

  public StringSubstitutor extend(StringSubstitutor substitutor) {
    return extend(substitutor.getStringLookup());
  }

  public StringSubstitutor extend(StringLookup lookup) {
    if (defaultLookup == null) return createSubstitutor(lookup);
    return createSubstitutor((key) -> {
      @Nullable String value = lookup.lookup(key);
      if (value != null) return value;
      return defaultLookup.lookup(key);
    });
  }

  public String substitute() {
    return (defaultSubstitutor != null
        ? defaultSubstitutor.replace(getMessage())
        : getMessage());
  }

  public String substituteArgs(Object... args) {
    if (ArrayUtils.isEmpty(args))
      return substitute();
    return substitute((variableName) -> {
      @Nullable Integer value = Ints.tryParse(variableName);
      return (value != null && value >= 0 && value < args.length
          ? Objects.toString(args[value]) : null);
    });
  }

  public String substitute(Map<String, ?> variableMap) {
    return substitute(StringLookupFactory.INSTANCE.mapStringLookup(variableMap));
  }

  public String substitute(StringLookup lookup) {
    return extend(lookup).replace(getMessage());
  }

  public String substitute(StringSubstitutor substitutor) {
    return extend(substitutor).replace(getMessage());
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    LocalizedMessage that = (LocalizedMessage) object;
    return Objects.equals(message, that.message)
        && Objects.equals(defaultLookup, that.defaultLookup);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, defaultLookup);
  }
}
