package util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class HashMapArrayAccessor extends AbstractMapArrayAccessor {

    HashMapArrayAccessor() {
    }

    @Override
    public int getArrayLength(Map<?, ?> map) {
        int length = map.size() * 3;
        return (length & 1) == 0 ? length : length + 1;
    }

    @Override
    public Object[] toArray(Map<?, ?> map, Object[] array, int offset, int length) {
        int keysLength = halfLength(length);
        if (offset < 0)
            throw new IndexOutOfBoundsException("Offset " + offset + " must be non-negative");
        if (array.length < offset + length)
            throw new IndexOutOfBoundsException("Array length " + array.length + " to small to dump " + length + " keys and values into it beginning with offset " + offset);

        class Int {
            int value;

            public String toString() { return String.valueOf(value); }
        }

        Map<Integer, Int> collisions = new TreeMap<Integer, Int>() {
            @Override
            public Int get(Object key) {
                Int val = super.get(key);
                if (val == null && key instanceof Integer) put((Integer) key, val = new Int());
                return val;
            }
        };

        for (Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<?, ?> e = it.next();
            Object key = e.getKey();
            Object value = e.getValue();
            int hash = hash(key);
            int dh;
            for (dh = 0; dh < keysLength; dh++) {
                int i = (hash + dh) % keysLength;
                int keyIndex = offset + i;
                if (array[keyIndex] == null) {
                    array[keyIndex] = key;
                    array[keyIndex + keysLength] = value;
                    if (dh > 0) collisions.get(dh).value++;
                    break;
                }
            }
            if (dh >= keysLength)
                throw new IllegalArgumentException("Map has " + map.size() + " entries which is to much to be dumped into array of length " + length);
        }

        if (!collisions.isEmpty())
            System.out.println("WARN: collisions when dumping " + map.size() + " entries into array of length " + length + ": " + collisions);

        return array;
    }

    @Override
    protected int indexOf(Object[] array, int offset, int length, Object key) {

        int h = hash(key) % length;
        int dh;
        for (dh = 0; dh < length; dh++) {
            int hi = h + dh;
            if (hi >= length) hi -= length;
            int i = offset + hi;
            Object value = array[i];
            if (value == null) break; // found gap - no match
            if (key.equals(value)) return i; // match
        }

        return -1;  // no match
    }

    /**
     * Retrieve object hash code and applies a supplemental hash function to the
     * result hash, which defends against poor quality hash functions.
     */
    private static int hash(Object k) {
        if (k instanceof String) {
            return ((String) k).hash32();
        }

        int h = k.hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
}
