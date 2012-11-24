package test;

import util.MapArrayAccessor;
import util.Mapper;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A test to determine the size of array at which linear search by annotationType becomes slower that binary search by hash of annotationType
 */

public class SearchPerfTest {

    @Retention(RUNTIME) @interface A00 {}
    @Retention(RUNTIME) @interface A01 {}
    @Retention(RUNTIME) @interface A02 {}
    @Retention(RUNTIME) @interface A03 {}
    @Retention(RUNTIME) @interface A04 {}
    @Retention(RUNTIME) @interface A05 {}
    @Retention(RUNTIME) @interface A06 {}
    @Retention(RUNTIME) @interface A07 {}
    @Retention(RUNTIME) @interface A08 {}
    @Retention(RUNTIME) @interface A09 {}
    @Retention(RUNTIME) @interface A0A {}
    @Retention(RUNTIME) @interface A0B {}
    @Retention(RUNTIME) @interface A0C {}
    @Retention(RUNTIME) @interface A0D {}
    @Retention(RUNTIME) @interface A0E {}
    @Retention(RUNTIME) @interface A0F {}
    @Retention(RUNTIME) @interface A10 {}
    @Retention(RUNTIME) @interface A11 {}
    @Retention(RUNTIME) @interface A12 {}
    @Retention(RUNTIME) @interface A13 {}
    @Retention(RUNTIME) @interface A14 {}
    @Retention(RUNTIME) @interface A15 {}
    @Retention(RUNTIME) @interface A16 {}
    @Retention(RUNTIME) @interface A17 {}
    @Retention(RUNTIME) @interface A18 {}
    @Retention(RUNTIME) @interface A19 {}
    @Retention(RUNTIME) @interface A1A {}
    @Retention(RUNTIME) @interface A1B {}
    @Retention(RUNTIME) @interface A1C {}
    @Retention(RUNTIME) @interface A1D {}
    @Retention(RUNTIME) @interface A1E {}
    @Retention(RUNTIME) @interface A1F {}
    @Retention(RUNTIME) @interface A20 {}
    @Retention(RUNTIME) @interface A21 {}
    @Retention(RUNTIME) @interface A22 {}
    @Retention(RUNTIME) @interface A23 {}
    @Retention(RUNTIME) @interface A24 {}
    @Retention(RUNTIME) @interface A25 {}
    @Retention(RUNTIME) @interface A26 {}
    @Retention(RUNTIME) @interface A27 {}
    @Retention(RUNTIME) @interface A28 {}
    @Retention(RUNTIME) @interface A29 {}
    @Retention(RUNTIME) @interface A2A {}
    @Retention(RUNTIME) @interface A2B {}
    @Retention(RUNTIME) @interface A2C {}
    @Retention(RUNTIME) @interface A2D {}
    @Retention(RUNTIME) @interface A2E {}
    @Retention(RUNTIME) @interface A2F {}
    @Retention(RUNTIME) @interface A30 {}
    @Retention(RUNTIME) @interface A31 {}
    @Retention(RUNTIME) @interface A32 {}
    @Retention(RUNTIME) @interface A33 {}
    @Retention(RUNTIME) @interface A34 {}
    @Retention(RUNTIME) @interface A35 {}
    @Retention(RUNTIME) @interface A36 {}
    @Retention(RUNTIME) @interface A37 {}
    @Retention(RUNTIME) @interface A38 {}
    @Retention(RUNTIME) @interface A39 {}
    @Retention(RUNTIME) @interface A3A {}
    @Retention(RUNTIME) @interface A3B {}
    @Retention(RUNTIME) @interface A3C {}
    @Retention(RUNTIME) @interface A3D {}
    @Retention(RUNTIME) @interface A3E {}
    @Retention(RUNTIME) @interface A3F {}

    @A00 @A01 @A02 @A03
    static class C4 {}

    @A00 @A01 @A02 @A03 @A04 @A05 @A06 @A07 @A08 @A09 @A0A @A0B @A0C @A0D @A0E @A0F
    static class C16 {}

    @A00 @A01 @A02 @A03 @A04 @A05 @A06 @A07 @A08 @A09 @A0A @A0B @A0C @A0D @A0E @A0F
    @A10 @A11 @A12 @A13 @A14 @A15 @A16 @A17 @A18 @A19 @A1A @A1B @A1C @A1D @A1E @A1F
    @A20 @A21 @A22 @A23 @A24 @A25 @A26 @A27 @A28 @A29 @A2A @A2B @A2C @A2D @A2E @A2F
    @A30 @A31 @A32 @A33 @A34 @A35 @A36 @A37 @A38 @A39 @A3A @A3B @A3C @A3D @A3E @A3F
    static class C64 {}

    static final Mapper<Class<?>, Annotation> ANNOTATION_TYPE = new Mapper<Class<?>, Annotation>() {
        @Override
        public Class<?> map(Annotation value) {
            return value.annotationType();
        }
    };

    static final int LOOP_SIZE = 5000000;

    private static final A00 FAKE_ANNOTATION = new A00() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return A00.class;
        }
    };

    private static Annotation getFakeAnnotation(Class<?> clazz) {
        return FAKE_ANNOTATION;
    }

    private static void testSearch(Class<?> clazz) {

        Annotation[] annotations = clazz.getAnnotations();

        Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();
        for (Annotation a : annotations)
            map.put(a.annotationType(), a);

        Class<?>[] annotationTypes = map.keySet().toArray(new Class[map.size()]);

        Object[] binaryArray = MapArrayAccessor.BINARY.toArray(map);
        Object[] hashArray = MapArrayAccessor.HASH.toArray(map);
        Object[] hashArray4 = MapArrayAccessor.HASH4.toArray(map);


        System.gc();
        System.gc();
        System.gc();
        try {
            Thread.sleep(500L);
        }
        catch (InterruptedException e) {}

        long t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++) {
            for (Class<?> annotationType : annotationTypes) {
                Annotation foundAnn = getFakeAnnotation(annotationType);
                Objects.requireNonNull(foundAnn);
            }
        }
        long tf = System.nanoTime() - t0;
        System.out.println(
            "       FakeEmptyLookup" +
            ": " + tf + " ns (" + ((double) tf / (double) LOOP_SIZE / (double) annotationTypes.length) + " ns/lookup)"
        );

        t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++) {
            for (Class<?> annotationType : annotationTypes) {
                Annotation foundAnn = map.get(annotationType);
                Objects.requireNonNull(foundAnn);
            }
        }
        long th = System.nanoTime() - t0;
        System.out.println(
            "               HashMap, map.size=" + map.size() +
                ": " + th + " ns (" + ((double) th / (double) LOOP_SIZE / (double) annotationTypes.length) + " ns/lookup)"
        );

        t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++) {
            for (Class<?> annotationType : annotationTypes) {
                Annotation foundAnn = MapArrayAccessor.BINARY.get(binaryArray, annotationType);
                Objects.requireNonNull(foundAnn);
            }
        }
        long tba = System.nanoTime() - t0;
        System.out.println(
            "BinaryMapArrayAccessor, array.length=" + binaryArray.length +
                ": " + tba + " ns (" + ((double) tba / (double) LOOP_SIZE / (double) annotationTypes.length) + " ns/lookup)"
        );

        t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++)
        {
            for (Class<?> annotationType : annotationTypes)
            {
                Annotation foundAnn = MapArrayAccessor.HASH.get(hashArray, annotationType);
                Objects.requireNonNull(foundAnn);
            }
        }
        long tha = System.nanoTime() - t0;
        System.out.println(
                "  HashMapArrayAccessor, array.length=" + hashArray.length
                + ": " + tha + " ns (" + ((double) tha / (double) LOOP_SIZE / (double) annotationTypes.length) + " ns/lookup)");

        t0 = System.nanoTime();
        for (int n = 0; n < LOOP_SIZE; n++)
        {
            for (Class<?> annotationType : annotationTypes)
            {
                Annotation foundAnn = MapArrayAccessor.HASH4.get(hashArray4, annotationType);
                if (foundAnn == null)
                {
                    System.out.println("Can't find " + annotationType.getName() + " in: " + Arrays.toString(hashArray4));
                }
                Objects.requireNonNull(foundAnn);
            }
        }
        long tha4 = System.nanoTime() - t0;
        System.out.println(
                "  HashMapArrayAccessor4, array.length=" + hashArray4.length
                + ": " + tha4 + " ns (" + ((double) tha4 / (double) LOOP_SIZE / (double) annotationTypes.length) + " ns/lookup)");

        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("warm-up:");
        testSearch(C4.class);
        testSearch(C16.class);
        testSearch(C64.class);
        testSearch(C4.class);
        testSearch(C16.class);
        testSearch(C64.class);
        System.out.println("measure:");
        testSearch(C4.class);
        testSearch(C16.class);
        testSearch(C64.class);
    }
}
