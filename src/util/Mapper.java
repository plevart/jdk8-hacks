package util;

/**
 * Can be replaced with {@code java.util.functions.Mapper} when available
 */
public interface Mapper<To, From> {
    To map(From value);
}
