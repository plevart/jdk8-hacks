package test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 *
 */

public class ReflectionTest {

    @InheritedAnnotation("A")
    @NoninheritedAnnotation("A")
    public static class ClassA {
        @InheritedAnnotation("A.m1")
        @NoninheritedAnnotation("A.m1")
        public void m1() {}

        @InheritedAnnotation("A.m2")
        @NoninheritedAnnotation("A.m2")
        public void m2() {}
    }

    public static class ClassB extends ClassA {
        @Override
        public void m1() {
            super.m1();
        }

        @Override
        public void m2() {
            super.m2();
        }
    }

    @InheritedAnnotation("C")
    @NoninheritedAnnotation("C")
    public static class ClassC extends ClassB {
        @InheritedAnnotation("C.m1")
        @NoninheritedAnnotation("C.m1")
        @Override
        public void m1() {
            super.m1();
        }

        @Override
        public void m2() {
            super.m2();
        }
    }

    static void dump (Annotation[] annotations, String prefix) {
        for (Annotation ann : annotations)
        {
            String value = ann instanceof NoninheritedAnnotation ? ((NoninheritedAnnotation) ann).value() : ((InheritedAnnotation) ann).value();
            System.out.println(prefix + "@" + ann.annotationType().getName() + "(\"" + value + "\")");
        }
    }

    static void dump(Class<?> clazz) {
        dump(clazz.getAnnotations(), "");
        System.out.println("class " + clazz.getName() + " {");
        for (Method m : clazz.getMethods()) {
           dump(m.getAnnotations(), "  ");
            System.out.println("  " + m.toGenericString() + ";");
        }
        System.out.println("}");
        System.out.println();
    }

    public static void main(String[] args) {
        dump(ClassA.class);
        dump(ClassB.class);
        dump(ClassC.class);
    }
}
