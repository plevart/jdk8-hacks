package util;

import java.util.Map;

/**
 */
public interface MapArrayAccessor {

    MapArrayAccessor BINARY = new BinaryMapArrayAccessor();
    MapArrayAccessor HASH = new HashMapArrayAccessor();
    MapArrayAccessor HASH4 = new HashMapArrayAccessor4();

    int getArrayLength(Map<?, ?> map);

    Object[] toArray(Map<?, ?> map);

    Object[] toArray(Map<?, ?> map, Object[] array, int offset, int length);

    <V> V get(Object[] array, Object key);

    <V> V get(Object[] array, int offset, int length, Object key);

    boolean containsKey(Object[] array, Object key);

    boolean containsKey(Object[] array, int offset, int length, Object key);
}
