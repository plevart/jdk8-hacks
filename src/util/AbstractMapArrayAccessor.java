package util;

import java.util.Map;

/**
 */
public abstract class AbstractMapArrayAccessor implements MapArrayAccessor {

    public Object[] toArray(Map<?, ?> map) {
        int length = getArrayLength(map);
        return toArray(map, new Object[length], 0, length);
    }

    @Override
    public <V> V get(Object[] array, Object key) {
        return get(array, 0, array.length, key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(Object[] array, int offset, int length, Object key) {
        int keysLength = halfLength(length);
        int keyIndex = indexOf(array, offset, keysLength, key);
        return keyIndex < 0 ? null : (V) array[keyIndex + keysLength];
    }

    @Override
    public boolean containsKey(Object[] array, Object key) {
        return containsKey(array, 0, array.length, key);
    }

    @Override
    public boolean containsKey(Object[] array, int offset, int length, Object key) {
        return indexOf(array, offset, halfLength(length), key) >= 0;
    }

    /**
     * Perform a search in the given {@code array} sub-range starting at {@code offset} with length {@code length}
     * for the location of an element that is equal to the given {@code key}. If such element is found, return index of it; otherwise -1
     *
     * @param array  the array to be searched
     * @param offset the start offset of the array sub-range to be searched
     * @param length the length of array sub-range to be searched
     * @param key    the value to be searched for
     * @return index of the found key or -1 if not found
     */
    protected abstract int indexOf(Object[] array, int offset, int length, Object key);

    protected static int halfLength(int length) {
        if (length < 0) throw new IllegalArgumentException("length " + length + " should be non-negative");
        if ((length & 1) > 0) throw new IllegalArgumentException("length " + length + " should be even number");
        return length >> 1;
    }
}
