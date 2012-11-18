package util;

import java.util.*;

/**
 * A {@link MapArrayAccessor} that facilitates dumping a {@link java.util.Map} into a single {@code Object[]} array or a sub-range of the array
 * of twice the size of the map: {@link #toArray}.
 * First half of the array is filled with keys and the second half with corresponding values so that the displacement of a value from it's key
 * is always half the size of the array or sub-range of the array.<p/>
 * Keys (and correspondingly values) are ordered by the key's ascending {@link #hash}. Such array or sub-range of the array can later be queried like the
 * map that was dumped into the array using methods: {@link #get} and {@link #containsKey}
 */
public class BinaryMapArrayAccessor extends AbstractMapArrayAccessor {

    BinaryMapArrayAccessor() {
    }

    private static final Comparator<Object> HASH_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(Object o1, Object o2) {
            return hash(o1) - hash(o2);
        }
    };

    public int getArrayLength(Map<?, ?> map) {
        return map.size() << 1;
    }

    public Object[] toArray(Map<?, ?> map, Object[] array, int offset, int length) {
        int keysLength = halfLength(length);
        if (offset < 0)
            throw new IndexOutOfBoundsException("Offset " + offset + " must be non-negative");
        if (array.length < offset + length)
            throw new IndexOutOfBoundsException("Array length " + array.length + " to small to dump " + length + " keys and values into it beginning with offset " + offset);

        Iterator<?> keys = map.keySet().iterator();
        for (int i = 0; i < keysLength; i++) {
            if (!keys.hasNext())
                throw new IllegalArgumentException("Map has not enough entries for requested length " + length);
            array[offset + i] = Objects.requireNonNull(keys.next(), "Keys in map must be non-null");
        }
        if (keys.hasNext())
            throw new IllegalArgumentException("Map has to much entries for requested length " + length);

        Arrays.sort(array, offset, offset + keysLength, HASH_COMPARATOR);

        int offsetValues = offset + keysLength;
        for (int i = 0; i < keysLength; i++)
            array[offsetValues + i] = map.get(array[offset + i]);

        return array;
    }

    @Override
    protected int indexOf(Object[] array, int offset, int length, Object key) {

        int toIndex = offset + length;
        int hash = hash(key);
        int low = offset;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Object midVal = array[mid];
            int midHash = hash(midVal);
            if (midHash < hash)
                low = mid + 1;
            else if (midHash > hash)
                high = mid - 1;
            else { // found hash at index mid
                // we have a potential match with correct hash -> see if it is a real match
                if (key.equals(midVal)) // found
                    return mid;
                // else search the neighborhood with the same hash
                // from mid-1 backwards
                for (int i = mid - 1; i >= offset; i--) {
                    midVal = array[i];
                    if (hash != hash(midVal))
                        break;
                    if (key.equals(midVal)) // found
                        return i;
                }
                // from mid+1 forwards
                for (int i = mid + 1; i < toIndex; i++) {
                    midVal = array[i];
                    if (hash != hash(midVal))
                        break;
                    if (key.equals(midVal)) // found
                        return i;
                }
                break; // no match
            }
        }
        return -1;  // not found
    }

    /**
     * Retrieve object hash code.
     */
    private static int hash(Object k) {
        return k.hashCode();
    }
}
