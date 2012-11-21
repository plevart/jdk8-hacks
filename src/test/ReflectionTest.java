package test;

import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class ReflectionTest
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target(
    {
        ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR
    })
    @Inherited
    public @interface InheritedAnnotation
    {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(
    {
        ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR
    })
    public @interface NoninheritedAnnotation
    {
        String value();
    }

    @InheritedAnnotation("A")
    @NoninheritedAnnotation("A")
    public static class ClassA
    {
        @InheritedAnnotation("A.f1")
        @NoninheritedAnnotation("A.f1")
        public String f1;

        @InheritedAnnotation("A.m1")
        @NoninheritedAnnotation("A.m1")
        public void m1()
        {
        }

        @InheritedAnnotation("A.m2")
        @NoninheritedAnnotation("A.m2")
        public void m2()
        {
        }
    }

    public static class ClassB extends ClassA
    {
        @InheritedAnnotation("B.f1")
        @NoninheritedAnnotation("B.f1")
        public String f1;

        @Override
        public void m1()
        {
            super.m1();
        }

        @Override
        public void m2()
        {
            super.m2();
        }
    }

    @InheritedAnnotation("C")
    @NoninheritedAnnotation("C")
    public static class ClassC extends ClassB
    {
        @InheritedAnnotation("C.m1")
        @NoninheritedAnnotation("C.m1")
        @Override
        public void m1()
        {
            super.m1();
        }

        @Override
        public void m2()
        {
            super.m2();
        }
    }

    static void dump(Annotation[] annotations, String prefix, Appendable sb) throws IOException
    {
        for (Annotation ann : annotations)
        {
            String value = ann instanceof NoninheritedAnnotation ? ((NoninheritedAnnotation) ann).value() : ((InheritedAnnotation) ann).value();
            sb.append(prefix).append("@").append(ann.annotationType().getName()).append("(\"").append(value).append("\")\n");
        }
    }

    static void dump(Class<?> clazz, Field[] fields, Method[] methods, Appendable sb) throws IOException
    {

        dump(clazz.getAnnotations(), "", sb);
        sb.append("class ").append(clazz.getName()).append(" {\n\n");

        if (fields != null)
        {
            for (Field f : fields)
            {
                dump(f.getAnnotations(), "  ", sb);
                sb.append("  ").append(f.toGenericString()).append(";\n\n");
            }
        }

        if (methods != null)
        {
            for (Method m : methods)
            {
                dump(m.getAnnotations(), "  ", sb);
                sb.append("  ").append(m.toGenericString()).append(";\n\n");
            }
        }

        sb.append("}\n\n");
    }
    static final Appendable NOOP_APPENDABLE = new Appendable()
    {
        @Override
        public Appendable append(CharSequence csq)
        {
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end)
        {
            return this;
        }

        @Override
        public Appendable append(char c)
        {
            return this;
        }
    };

    static class Test1 extends Thread
    {
        final int loops;

        Test1(int loops)
        {
            this.loops = loops;
        }

        @Override
        public void run()
        {
            try
            {
                for (int i = 0; i < loops; i++)
                {
                    dump(ClassA.class, ClassA.class.getFields(), ClassA.class.getMethods(), NOOP_APPENDABLE);
                    dump(ClassB.class, ClassB.class.getFields(), ClassB.class.getMethods(), NOOP_APPENDABLE);
                    dump(ClassC.class, ClassC.class.getFields(), ClassC.class.getMethods(), NOOP_APPENDABLE);
                }
            }
            catch (IOException e)
            {
            }
        }
    }

    static class Test2 extends Thread
    {
        final int loops;

        Test2(int loops)
        {
            this.loops = loops;
        }

        @Override
        public void run()
        {
            try
            {
                Field[] classAfields = ClassA.class.getFields();
                Method[] classAmethods = ClassA.class.getMethods();
                Field[] classBfields = ClassB.class.getFields();
                Method[] classBmethods = ClassB.class.getMethods();
                Field[] classCfields = ClassC.class.getFields();
                Method[] classCmethods = ClassC.class.getMethods();

                for (int i = 0; i < loops; i++)
                {
                    dump(ClassA.class, classAfields, classAmethods, NOOP_APPENDABLE);
                    dump(ClassB.class, classBfields, classBmethods, NOOP_APPENDABLE);
                    dump(ClassC.class, classCfields, classCmethods, NOOP_APPENDABLE);
                }
            }
            catch (IOException e)
            {
            }
        }
    }

    static class Test3 extends Thread
    {
        final int loops;

        Test3(int loops)
        {
            this.loops = loops;
        }

        @Override
        public void run()
        {
            for (int i = 0; i < loops; i++)
            {
                Objects.requireNonNull(ClassC.class.getAnnotation(InheritedAnnotation.class));
                Objects.requireNonNull(ClassA.class.getAnnotation(InheritedAnnotation.class));
                Objects.requireNonNull(ClassB.class.getAnnotation(InheritedAnnotation.class));
            }
        }
    }

    static void testCorrectness()
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            dump(ClassA.class, ClassA.class.getFields(), ClassA.class.getMethods(), sb);
            dump(ClassB.class, ClassB.class.getFields(), ClassB.class.getMethods(), sb);
            dump(ClassC.class, ClassC.class.getFields(), ClassC.class.getMethods(), sb);
        }
        catch (IOException e)
        {
        }
        System.out.println(sb);
    }

    static long test1(int threads, int loops, long prevT)
    {

        Thread[] workers = new Thread[threads];
        for (int i = 0; i < workers.length; i++)
        {
            workers[i] = new Test1(loops);
        }

        return runWorkers(workers, loops, prevT);
    }

    static long test2(int threads, int loops, long prevT)
    {

        Thread[] workers = new Thread[threads];
        for (int i = 0; i < workers.length; i++)
        {
            workers[i] = new Test2(loops);
        }

        return runWorkers(workers, loops, prevT);
    }

    static long test3(int threads, int loops, long prevT)
    {

        Thread[] workers = new Thread[threads];
        for (int i = 0; i < workers.length; i++)
        {
            workers[i] = new Test3(loops);
        }

        return runWorkers(workers, loops, prevT);
    }

    static long runWorkers(Thread[] workers, int loops, long prevT)
    {

        try
        {
            System.gc();
            Thread.sleep(500L);
            System.gc();
            Thread.sleep(500L);
            System.gc();
            Thread.sleep(500L);
        }
        catch (InterruptedException e)
        {
        }

        long t0 = System.nanoTime();

        for (int i = 0; i < workers.length; i++)
        {
            workers[i].start();
        }

        for (int i = 0; i < workers.length; i++)
        {
            try
            {
                workers[i].join();
            }
            catch (InterruptedException e)
            {
            }
        }

        long t = System.nanoTime() - t0;

        System.out.println(
                workers[0].getClass().getSimpleName() + ": "
                + String.format("%3d", workers.length) + " concurrent threads * "
                + String.format("%9d", loops) + " loops each: "
                + String.format("%,15.3f", (double) t / 1000000d) + " ms"
                + (prevT == 0L ? "" : String.format(" (x %6.2f)", (double) t / (double) prevT)));

        return t;
    }

    public static void main(String[] args) throws IOException
    {
        long t;
        System.out.println("warm-up:");
        t = test1(1, 20000, 0L);
        test1(1, 20000, t);
        test1(1, 20000, t);
        System.out.println();
        t = test2(1, 100000, 0);
        test2(1, 100000, t);
        test2(1, 100000, t);
        System.out.println();
        t = test3(1, 10000000, 0);
        test3(1, 10000000, t);
        test3(1, 10000000, t);
        System.out.println();

        System.out.println("measure:");
        t = test1(1, 20000, 0);
        test1(2, 20000, t);
        test1(4, 20000, t);
        test1(8, 20000, t);
        test1(32, 20000, t);
        test1(128, 20000, t);
        System.out.println();
        t = test2(1, 100000, 0);
        test2(2, 100000, t);
        test2(4, 100000, t);
        test2(8, 100000, t);
        test2(32, 100000, t);
        test2(128, 100000, t);
        System.out.println();
        t = test3(1, 10000000, 0);
        test3(2, 10000000, t);
        test3(4, 10000000, t);
        test3(8, 10000000, t);
        test3(32, 10000000, t);
        test3(128, 10000000, t);
        System.out.println();
    }
}
