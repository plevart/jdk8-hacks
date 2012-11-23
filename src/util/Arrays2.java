package util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A set of static methods that facilitate fast retrieval of elements from arrays by keys that are obtainable from elements
 * by a user specified {@link Mapper} {@code keyExtractor}. The retrieval is based on binary search by hashCode of the keys of elements
 * followed by the linear search by key of the equal-hashCode-neighbourhood of the found candidate.<p/>
 * The arrays to be searched must 1st be sorted using {@link #sortByKeyHash}.<p/>
 * If extracted keys are known to be unique (by the means of {@link #equals} method) or if it doesn't matter which of the
 * key-equivalent elements is found, the {@link #findAnyBinaryByKeyHash} method can be used. Otherwise...todo(findAllBinaryByKeyHash)...
 * <p/>
 * These methods could be added to {@link java.util.Arrays} perhaps.
 */
public class Arrays2 {

    /**
     * A shortcut for calling:
     * <pre>
     *     {@link #sortByKeyHash(Object[], int, int, Mapper) sortByKeyHash(values, 0, values.length, keyExtractor)}
     * </pre>
     *
     * @param values       an array of values
     * @param keyExtractor a mapper from values to corresponding keys
     * @param <V>          the type of values
     */
    public static <V> void sortByKeyHash(V[] values, Mapper<?, ? super V> keyExtractor) {
        sortByKeyHash(values, 0, values.length, keyExtractor);
    }

    /**
     * Sorts the specified range of the specified array of {@code values} according to the hasCode value of the key
     * extracted from each value with the specified {@code keyExtractor}.
     * The range to be sorted extends from index {@code fromIndex}, inclusive, to index {@code toIndex}, exclusive.
     *
     * @param values       an array of values
     * @param fromIndex    the index of the first element (inclusive) to be sorted
     * @param toIndex      the index of the last element (exclusive) to be sorted
     * @param keyExtractor a mapper from values to corresponding keys
     * @param <V>          the type of values
     */
    public static <V> void sortByKeyHash(V[] values, int fromIndex, int toIndex, Mapper<?, ? super V> keyExtractor) {
        Arrays.sort(
            values,
            fromIndex,
            toIndex,
            new KeyHashOrder<V>(keyExtractor)
        );
    }

    /**
     * A shortcut for calling:
     * <pre>
     *     {@link #findAnyBinaryByKeyHash(Object[], int, int, Mapper, Object) findAnyBinaryByKeyHash(values, 0, values.length, keyExtractor, searchKey)}
     * </pre>
     *
     * @param values       an array of values that was previously sorted by
     *                     {@link #sortByKeyHash(Object[], Mapper) sortByKeyHash(values, keyExtractor)}
     * @param keyExtractor a mapper from values to corresponding keys
     * @param searchKey    a key to be searched
     * @param <K>          the type of keys
     * @param <V>          the type of values
     * @return any element for which key matches or null if not found
     */
    public static <K, V> V findAnyBinaryByKeyHash(V[] values, Mapper<? extends K, ? super V> keyExtractor, K searchKey) {
        return findAnyBinaryByKeyHash(values, 0, values.length, keyExtractor, searchKey);
    }

    /**
     * Searches for any element in the specified array of {@code values} between index {@code fromIndex} inclusive and index {@code toIndex} exclusive
     * that has a key (obtained by specified {@code keyExtractor}) equal to the specified {@code searchKey} and returns such element if found
     * or {@code null} if not found.
     *
     * @param values       an array of values that was previously sorted by
     *                     {@link #sortByKeyHash(Object[], int, int, Mapper) sortByKeyHash(values, fromIndex, toIndex, keyExtractor)}
     * @param fromIndex    the index of the first element (inclusive) to be searched for
     * @param toIndex      the index of the last element (exclusive) to be searched for
     * @param keyExtractor a mapper from values to corresponding keys
     * @param searchKey    a key to be searched
     * @param <K>          the type of keys
     * @param <V>          the type of values
     * @return any element for which key matches or null if not found
     */
    public static <K, V> V findAnyBinaryByKeyHash(V[] values, int fromIndex, int toIndex, Mapper<? extends K, ? super V> keyExtractor, K searchKey) {
        int searchKeyHash = searchKey.hashCode();
        int i = binarySearchByHash(values, fromIndex, toIndex, keyExtractor, searchKeyHash);
        if (i >= 0) {
            // we have a potential match with correct hash -> see if it is a real match
            V value = values[i];
            K key = keyExtractor.map(value);
            if (key.equals(searchKey))
                return value;
            // else search the neighborhood with the same hashCode
            // from i-1 backwards
            for (int j = i - 1; j >= fromIndex; j--) {
                value = values[j];
                key = keyExtractor.map(value);
                if (key.hashCode() != searchKeyHash)
                    break;
                if (key.equals(searchKey))
                    return value;
            }
            // from i+1 forwards
            for (int j = i + 1; j < toIndex; j++) {
                value = values[j];
                key = keyExtractor.map(value);
                if (key.hashCode() != searchKeyHash)
                    break;
                if (key.equals(searchKey))
                    return value;
            }
        }
        // not found
        return null;
    }

    public static <K, V> V findAnyLinear(V[] values, int fromIndex, int toIndex, Mapper<? extends K, ? super V> keyExtractor, K searchKey) {
        for (int i = fromIndex; i < toIndex; i++) {
            V value = values[i];
            K key = keyExtractor.map(value);
            if (key.equals(searchKey)) {
                return value;
            }
        }
        // not found
        return null;
    }

    private static <V> int binarySearchByHash(V[] values, int fromIndex, int toIndex, Mapper<?, ? super V> keyExtractor, int keyHash) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            V midVal = values[mid];
            int midHash = keyExtractor.map(midVal).hashCode();
            if (midHash < keyHash)
                low = mid + 1;
            else if (midHash > keyHash)
                high = mid - 1;
            else
                return mid; // found
        }
        return -(low + 1);  // not found.
    }

    private static final class KeyHashOrder<V> implements Comparator<V> {
        private final Mapper<?, ? super V> keyExtractor;

        KeyHashOrder(Mapper<?, ? super V> keyExtractor) {
            this.keyExtractor = keyExtractor;
        }

        @Override
        public int compare(V value1, V value2) {
            return keyExtractor.map(value1).hashCode() - keyExtractor.map(value2).hashCode();
        }
    }
}