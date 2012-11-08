package test;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * A compact immutable generic container of mappings {@code K -> V[]} where all {@code V}s are SoftReferenced and individual mapping can:
 * <ul>
 * <li>either be present and the number of mapped {@code V}s for that mapping is between 0 and {@link Short#MAX_VALUE} or</ln>
 * <li>not present</li>
 * </ul>
 * Implementation note: for compactness reason, this class is a subclass of {@link SoftReference}. Method {@link #get()} returns
 * either {@code null} if this instance is empty (or already cleared) or a defensive copy of the underlying flattened array of all mapped {@code V}s.<p/>
 * Other methods provide more utility.
 */
public class SoftCache<K extends Enum<K>, V> extends SoftReference<V[]> {

    /**
     * Empty instance constructor
     */
    public SoftCache() {
        this((short[]) null, null);
    }

    /**
     * A constructor for instance with a single mapping: {@code key -> values}
     *
     * @param key    the non {@code null} key
     * @param values the non null values array with a maximum length of {@link Short#MAX_VALUE}
     */
    public SoftCache(K key, V... values) {
        this(offsetsFor(key), values.clone());
    }

    private final short[] offsets;

    private SoftCache(short[] offsets, V[] values) {
        super(values);
        this.offsets = offsets;
    }

    /**
     * @return either {@code null} if this instance is empty (or already cleared) or a defensive copy of the underlying referent (flattened array of all mapped {@code V}s)
     */
    @Override
    public V[] get() {
        V[] allValues = super.get();
        return allValues == null ? null : allValues.clone();
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(K key) {
        int ki = key.ordinal();
        return offsets != null && offsets.length > ki && offsets[ki] >= 0 && super.get() != null;
    }

    /**
     * @return {@code true} if this instance contains no mappings (or has been cleared)
     */
    public boolean isEmpty() {
        return super.get() == null;
    }

    /**
     * @param key
     * @return
     */
    public <V2 extends V> V2[] get(K key, Class<V2> arrayComponentType) {
        int ki = key.ordinal();
        // check for presence and retrieve offset
        int offset;
        V[] allValues;
        if (offsets == null || offsets.length <= ki || (offset = offsets[ki]) < 0 || (allValues = super.get()) == null) {
            return null; // no mapping
        }
        // retrieve length
        int length;
        int nextKi = ki + 1;
        if (offsets.length > nextKi) {
            int nextOffset = offsets[nextKi];
            length = nextOffset >= 0 ? nextOffset - offset : -nextOffset - 1 - offset;
        }
        else {
            length = allValues.length - offset;
        }

        // copy elements from the range of underlying array into new array of same type
        @SuppressWarnings("unchecked")
        V2[] values = (V2[]) Array.newInstance(arrayComponentType, length);
        System.arraycopy(allValues, offset, values, 0, length);

        return values;
    }

    private static short[] offsetsFor(Enum<?> key) {
        int ord = key.ordinal();
        short[] offsets = new short[ord + 1];
        for (int i = 0; i < ord; i++)
            offsets[i] = -1;
        offsets[ord] = 0;
        return offsets;
    }
}
