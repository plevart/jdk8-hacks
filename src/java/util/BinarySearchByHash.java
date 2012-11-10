package java.util;

/**
 * A set of static methods that facilitate fast retrieval of elements from arrays by keys that are obtainable from elements
 * by a user specified {@link Mapper} {@code keyExtractor}. The retrieval is based on binary search by hashCode of the keys of elements
 * followed by the linear search of the equal-hashCode-neighbourhood of the found candidate.<p/>
 * The arrays to be searched must 1st be sorted using {@link #sortByKeyHash}.<p/>
 * If extracted keys are known to be unique (by the means of {@link #equals} method) or if it doesn't matter which of the
 * key-equivalent elements is found, the {@link #findAny} method can be used. Otherwise...
 */
public class BinarySearchByHash {

    private static final int LINEAR_SEARCH_MAX_LENGTH = 10;

    public interface Mapper<K, V> {
        K map(V value);
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
        if (values.length > LINEAR_SEARCH_MAX_LENGTH) {
            Arrays.sort(
                values,
                fromIndex,
                toIndex,
                new KeyHashOrder<V>(keyExtractor)
            );
        }
    }

    /**
     * Searches for any element in the specified array of {@code values} between index {@code fromIndex} inclusive and index {@code toIndex} exclusive
     * that has a key (obtained by specified {@code keyExtractor}) equal to the specified {@code searchKey} and returns such element if found
     * or {@code null} if not found.
     *
     * @param values       an array of values
     * @param fromIndex    the index of the first element (inclusive) to be searched for
     * @param toIndex      the index of the last element (exclusive) to be searched for
     * @param keyExtractor a mapper from values to corresponding keys
     * @param searchKey    a key to be searched
     * @param <K>          the type of keys
     * @param <V>          the type of values
     * @return any element for which key matches or null if not found
     */
    public static <K, V> V findAny(V[] values, int fromIndex, int toIndex, Mapper<? extends K, ? super V> keyExtractor, K searchKey) {
        if (values.length <= LINEAR_SEARCH_MAX_LENGTH) {
            for (int i = 0; i < values.length; i++) {
                V value = values[i];
                K key = keyExtractor.map(value);
                if (key.equals(searchKey)) {
                    return value;
                }
            }
        }
        else {
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

    private static class KeyHashOrder<V> implements Comparator<V> {
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
