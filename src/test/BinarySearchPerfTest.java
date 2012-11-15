package test;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;

import util.Arrays2;
import util.Mapper;

/**
 * A test to determine the size of array at which linear search by annotationType becomes slower that binary search by hash of annotationType
 */

public class BinarySearchPerfTest {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann1 {}

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann2 {}

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann3 {}

    @Ann1
    @Ann2
    @Ann3
    static class Cls {}

    static final Mapper<Class<?>, Annotation> ANNOTATION_TYPE = new Mapper<Class<?>, Annotation>() {
        @Override
        public Class<?> map(Annotation value) {
            return value.annotationType();
        }
    };

    static final int LOOP_SIZE = 100000;

    private static void testSearch(int arraySize) {
        Annotation[] annotations = Cls.class.getAnnotations();
        Arrays2.sortByKeyHash(annotations, ANNOTATION_TYPE);

        // make test arrays - each array has an element to be found (annotations[1]) at a particular index (each index is tried),
        // preceded by elements that are smaller (annotations[0]) and followed by elements that are larger (annotations[2]).
        Annotation[][] testArrays = new Annotation[arraySize][arraySize];
        for (int i = 0; i < arraySize; i++) {
            for (int j = 0; j < arraySize; j++) {
                testArrays[i][j] = j < i ? annotations[0] : (j > i ? annotations[2] : annotations[1]);
            }
        }

        // make a comparison test map with 3 entries
        Map<Class<?>, Annotation> testMap = new HashMap<>();
        for (Annotation a : annotations)
            testMap.put(a.annotationType(), a);

        Class<?> searchAnnotationType = annotations[1].annotationType();

        System.gc();
        System.gc();
        System.gc();
        try {
            Thread.sleep(500L);
        }
        catch (InterruptedException e) {}

        long t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++) {
            for (int i = 0; i < arraySize; i++) {
                Annotation foundAnn = Arrays2.findAnyLinear(testArrays[i], 0, arraySize, ANNOTATION_TYPE, searchAnnotationType);
                assert foundAnn != null;
            }
        }
        // the work to be done is arraySize^2 so to be fair we divide - take the mean
        long tl = (System.nanoTime() - t0) / (long) (arraySize == 0 ? 1 : arraySize);
        System.out.println(
            " Linear search, arraySize=" + arraySize +
            ": " + tl + " ns (" + ((double) tl / (double) LOOP_SIZE) + " ns/lookup)"
        );

        t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++) {
            for (int i = 0; i < arraySize; i++) {
                Annotation foundAnn = Arrays2.findAnyBinaryByKeyHash(testArrays[i], 0, arraySize, ANNOTATION_TYPE, searchAnnotationType);
                assert foundAnn != null;
            }
        }
        // the work to be done is arraySize^2 so to be fair we divide - take the mean
        long tb = (System.nanoTime() - t0) / (long) (arraySize == 0 ? 1 : arraySize);
        System.out.println(
            " Binary search, arraySize=" + arraySize +
            ": " + tb + " ns (" + ((double) tb / (double) LOOP_SIZE) + " ns/lookup)" +
            " - " + (tl < tb ? "LINEAR" : "BINARY") + " wins");

        t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++) {
            Annotation foundAnn = testMap.get(searchAnnotationType);
            assert foundAnn != null;
        }
        long th = System.nanoTime() - t0;
        System.out.println(
            "HashMap lookup, arraySize=" + arraySize +
            ": " + th + " ns (" + ((double) th / (double) LOOP_SIZE) + " ns/lookup)\n"
        );

    }

    public static void main(String[] args) {
        System.out.println("warm-up:");
        testSearch(30);
        testSearch(30);
        testSearch(30);
        testSearch(100);
        System.out.println("measure:");
        for (int n = 0; n < 50; n++)
            testSearch(n);
    }
}
