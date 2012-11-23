package util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class HashMapArrayAccessor4 extends AbstractMapArrayAccessor
{
    HashMapArrayAccessor4()
    {
    }

    @Override
    public int getArrayLength(Map<?, ?> map)
    {
        int length = map.size() * 3;
        while ((length & 3) > 0)
        {
            length++;
        }
        return length;
    }

    @Override
    public Object[] toArray(Map<?, ?> map, Object[] array, int offset, int length)
    {
        if (offset < 0)
        {
            throw new IndexOutOfBoundsException("Offset " + offset + " must be non-negative");
        }
        if (array.length < offset + length)
        {
            throw new IndexOutOfBoundsException("Array length " + array.length + " to small to dump " + length + " keys and values into it beginning with offset " + offset);
        }

        int keysLength = halfLength(length); // this also checks that returned keysLength is even

        class Int
        {
            int value;

            public String toString()
            {
                return String.valueOf(value);
            }
        }

        Map<Integer, Int> collisions = new TreeMap<Integer, Int>()
        {
            @Override
            public Int get(Object key)
            {
                Int val = super.get(key);
                if (val == null && key instanceof Integer)
                {
                    put((Integer) key, val = new Int());
                }
                return val;
            }
        };


        for (Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<?, ?> e = it.next();
            Object key = e.getKey();
            Object value = e.getValue();
            // 1st try exact even index (most keys will be put at exact locations)
            int hash = (hash(key) << 1) & Integer.MAX_VALUE;
            int i = hash % keysLength;
            int keyIndex = offset + i;
            if (array[keyIndex] == null)
            {
                array[keyIndex] = key;
                array[keyIndex + keysLength] = value;
            }
            else
            {
                // 2nd try odd displaced indexes (that do not clash with exact even locations)
                int dh;
                for (dh = 1; dh < keysLength; dh += 2)
                {
                    i = (hash + dh) % keysLength;
                    keyIndex = offset + i;
                    if (array[keyIndex] == null)
                    {
                        array[keyIndex] = key;
                        array[keyIndex + keysLength] = value;
                        collisions.get(dh).value++;
                        break;
                    }
                }
                if (dh >= keysLength)
                {
                    // 3rd try even displaced indexes (they can clash with exact even locations therefore they are tried last)
                    for (dh = 2; dh < keysLength; dh += 2)
                    {
                        i = (hash + dh) % keysLength;
                        keyIndex = offset + i;
                        if (array[keyIndex] == null)
                        {
                            array[keyIndex] = key;
                            array[keyIndex + keysLength] = value;
                            collisions.get(dh).value++;
                            break;
                        }
                    }
                    if (dh >= keysLength)
                    {
                        throw new IllegalArgumentException("Map has " + map.size() + " entries which is to much to be dumped into array of length " + length);
                    }
                }
            }
        }

        if (!collisions.isEmpty())
        {
            System.out.println("WARN4: collisions when dumping " + map.size() + " entries into array of length " + length + ": " + collisions);
        }

        return array;
    }

    @Override
    protected int indexOf(Object[] array, int offset, int length, Object key)
    {

        int h = ((hash(key) << 1) & Integer.MAX_VALUE) % length;
        int i = offset + h;
        // 1st try exact even index (most keys will be put at exact locations)
        Object value = array[i];
        if (value == null)
        {
            return -1; // found gap - no match
        }
        if (key.equals(value))
        {
            return i; // match
        }
        // 2nd try odd displaced indexes (that do not clash with exact even locations)
        for (int dh = 1; dh < length; dh++)
        {
            int hi = h + dh;
            if (hi >= length)
            {
                hi -= length;
            }
            i = offset + hi;
            value = array[i];
            if (value == null)
            {
                break; // found gap - no match
            }
            if (key.equals(value))
            {
                return i; // match
            }
        }
        // 3rd try even displaced indexes (they can clash with exact even locations therefore they are tried last)
        for (int dh = 2; dh < length; dh++)
        {
            int hi = h + dh;
            if (hi >= length)
            {
                hi -= length;
            }
            i = offset + hi;
            value = array[i];
            if (value == null)
            {
                break; // found gap - no match
            }
            if (key.equals(value))
            {
                return i; // match
            }
        }

        return -1;  // no match
    }

    /**
     * Retrieve object hash code and applies a supplemental hash function to the result hash, which defends against poor quality
     * hash functions.
     */
    private static int hash(Object k)
    {
        if (k instanceof String)
        {
            return ((String) k).hash32();
        }

        int h = k.hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    protected static int halfLength(int length)
    {
        if (length < 0)
        {
            throw new IllegalArgumentException("length " + length + " should be non-negative");
        }
        if ((length & 3) > 0)
        {
            throw new IllegalArgumentException("length " + length + " should be a multiple of 4");
        }
        return length >> 1;
    }
}
