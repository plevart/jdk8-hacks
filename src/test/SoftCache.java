package test;

import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * A compact immutable generic container of mappings {@code K -> V[]} where all {@code V}s are SoftReferenced and individual mapping can
 * either be present or not present.<p/>
 * Implementation note: for compactness reason, this class is a subclass of {@link SoftReference}. Method {@link #get()} returns
 * either {@code null} if this instance is empty (or already cleared) or a defensive copy of the underlying flattened array of all mapped {@code V}s.<p/>
 * Other methods provide more utility.
 */
public class SoftCache<K extends Enum<K>, V> extends SoftReference<V[]> {

    /**
     * Empty instance constructor
     */
    public SoftCache() {
        this((int[]) null, null);
    }

    /**
     * A constructor for instance with a single mapping: {@code key -> values}
     *
     * @param key    the non {@code null} key
     * @param values the non null values array
     */
    public SoftCache(K key, V... values) {
        this(offsetsFor(key), values.clone());
    }

    private final int[] offsets;

    private SoftCache(int[] offsets, V[] values) {
        super(values);
        this.offsets = offsets;
    }

    /**
     * @return either {@code null} if this instance is empty (or already cleared)
     *         or a defensive copy of the underlying referent (flattened array of all mapped {@code V}s)
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
        // check for presence and retrieve offset & allValues
        int offset;
        V[] allValues;
        if (offsets != null && (allValues = super.get()) != null &&
            offsets.length > ki && (offset = offsets[ki]) >= 0) {
            // retrieve length
            int nextKi = ki + 1;
            int length = offsets.length > nextKi
                         ? (offsets[nextKi] & Integer.MAX_VALUE) - offset
                         : allValues.length - offset;

            // copy elements from the range of underlying array into new array of requested type
            @SuppressWarnings("unchecked")
            V2[] values = (V2[]) Array.newInstance(arrayComponentType, length);
            System.arraycopy(allValues, offset, values, 0, length);

            return values;
        } else {
            return null;
        }
    }

    public SoftCache<K, V> put(K key, V... newValues) {
        int ki = key.ordinal();
        // check for presence and retrieve allValues
        V[] allValues;
        if (offsets != null && (allValues = super.get()) != null) { // not (empty or cleared)
            int offset;
            int[] newOffsets;
            if (offsets.length > ki) {
                // old offsets array is long enough - just clone it
                newOffsets = offsets.clone();
                // make sure ki-th element is marked as present in newOffsets
                newOffsets[ki] = offset = offsets[ki] & Integer.MAX_VALUE;
            } else {
                // old offsets array is to small - make newOffsets array big enough for index ki
                newOffsets = Arrays.copyOf(offsets, ki + 1);
                // fill new elements with the offset pointing after the last element in allValues array
                // and with hi-bit set (indicating that no mapping exists yet)...
                for (int i = offsets.length; i < ki; i++) {
                    newOffsets[i] = allValues.length | Integer.MIN_VALUE;
                }
                // ...except ki-th element which is marked as present
                newOffsets[ki] = offset = allValues.length;
            }
            // retrieve oldLength
            int nextKi = ki + 1;
            int oldLength = newOffsets.length > nextKi
                            ? (newOffsets[nextKi] & Integer.MAX_VALUE) - offset
                            : allValues.length - offset;
            // update newOffsets past 'ki' to accommodate for array extension/shrinkage
            int delta = newValues.length - oldLength;
            if (delta != 0) {
                for (int i = ki + 1; i < newOffsets.length; i++)
                    newOffsets[i] += delta;
            }
            // compute newAllLength
            int newAllLength = allValues.length - oldLength + newValues.length;
            // create newAllValues array
            V[] newAllValues = (V[]) Array.newInstance(allValues.getClass().getComponentType(), newAllLength);
            // copy prefix
            if (offset > 0)
                System.arraycopy(allValues, 0, newAllValues, 0, offset);
            // copy newValues
            if (newValues.length > 0)
                System.arraycopy(newValues, 0, newAllValues, offset, newValues.length);
            // copy suffix
            int suffixOffset = offset + oldLength;
            if (suffixOffset < allValues.length)
                System.arraycopy(allValues, suffixOffset, newAllValues, offset + newValues.length, allValues.length - suffixOffset);
            // create a fresh instance with added/replaced mapping
            return new SoftCache<>(newOffsets, newAllValues);

        } else { // empty or cleared
            // just create a fresh instance with single mapping
            return new SoftCache<>(key, newValues);
        }
    }

    public SoftCache<K, V> remove(K key) {
        int ki = key.ordinal();
        // check for presence and retrieve allValues
        V[] allValues;
        if (offsets != null && (allValues = super.get()) != null) { // not (empty or cleared)
            int offset;
            int[] newOffsets;
            if (offsets.length > ki) {
                // old offsets array is long enough but element marked as absent- just return this
                if (offsets[ki] < 0) return this;
                // old offsets array is long enough - just clone it
                newOffsets = offsets.clone();
                // make sure ki-th element is marked as present in newOffsets
                newOffsets[ki] = offset = offsets[ki] & Integer.MAX_VALUE;
            } else {
                // old offsets array is to small - just return this
                return this;
            }
            // TODO...
            // retrieve oldLength
            int nextKi = ki + 1;
            int oldLength = newOffsets.length > nextKi
                            ? (newOffsets[nextKi] & Integer.MAX_VALUE) - offset
                            : allValues.length - offset;
            // update newOffsets past 'ki' to accommodate for array extension/shrinkage
            int delta = newValues.length - oldLength;
            if (delta != 0) {
                for (int i = ki + 1; i < newOffsets.length; i++)
                    newOffsets[i] += delta;
            }
            // compute newAllLength
            int newAllLength = allValues.length - oldLength + newValues.length;
            // create newAllValues array
            V[] newAllValues = (V[]) Array.newInstance(allValues.getClass().getComponentType(), newAllLength);
            // copy prefix
            if (offset > 0)
                System.arraycopy(allValues, 0, newAllValues, 0, offset);
            // copy newValues
            if (newValues.length > 0)
                System.arraycopy(newValues, 0, newAllValues, offset, newValues.length);
            // copy suffix
            int suffixOffset = offset + oldLength;
            if (suffixOffset < allValues.length)
                System.arraycopy(allValues, suffixOffset, newAllValues, offset + newValues.length, allValues.length - suffixOffset);
            // create a fresh instance with added/replaced mapping
            return new SoftCache<>(newOffsets, newAllValues);

        } else { // empty or cleared
            // just create a fresh instance with single mapping
            return new SoftCache<>(key, newValues);
        }
    }

    private static int[] offsetsFor(Enum<?> key) {
        int ki = key.ordinal();
        int[] offsets = new int[ki + 1];
        for (int i = 0; i < ki; i++)
            offsets[i] = Integer.MIN_VALUE;
        offsets[ki] = 0;
        return offsets;
    }


    // testing...

    void dump(K[] keys, Class<? extends V> arrayComponentType, PrintStream out) {
        for (K k : keys) {
            V[] vals = get(k, arrayComponentType);
            System.out.println(k + "->" + (vals == null ? "null" : Arrays.toString(vals)));
        }
        System.out.println();
    }

    enum Key {
        K1, K2, K3, K4
    }

    public static void main(String[] args) {
        SoftCache<Key, String> sc = new SoftCache<>();
        sc.dump(Key.values(), String.class, System.out);
        sc = sc.put(Key.K1, "V1.a", "V1.b");
        sc.dump(Key.values(), String.class, System.out);
        sc = sc.put(Key.K4, "V4.a", "V4.b");
        sc.dump(Key.values(), String.class, System.out);
        sc = sc.put(Key.K2);
        sc.dump(Key.values(), String.class, System.out);
        sc = sc.put(Key.K3);
        sc.dump(Key.values(), String.class, System.out);
        sc = sc.put(Key.K1);
        sc.dump(Key.values(), String.class, System.out);
        sc = sc.put(Key.K2, "V2.a", "V2.b", "V2.c");
        sc.dump(Key.values(), String.class, System.out);
    }
}
