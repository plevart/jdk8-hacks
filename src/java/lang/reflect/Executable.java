/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.reflect;

import java.lang.annotation.*;
import java.util.Map;
import sun.reflect.annotation.AnnotationParser;
import sun.reflect.generics.repository.ConstructorRepository;

/**
 * A shared superclass for the common functionality of {@link Method}
 * and {@link Constructor}.
 *
 * @since 1.8
 */
public abstract class Executable extends AccessibleObject
    implements Member, GenericDeclaration {
    /*
     * Only grant package-visibility to the constructor.
     */
    Executable() {}

    /**
     * Accessor method to allow code sharing
     */
    abstract byte[] getAnnotationBytes();

    /**
     * Does the Executable have generic information.
     */
    abstract boolean hasGenericInformation();

    abstract ConstructorRepository getGenericInfo();

    boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
        /* Avoid unnecessary cloning */
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }

    Annotation[][] parseParameterAnnotations(byte[] parameterAnnotations) {
        return AnnotationParser.parseParameterAnnotations(
               parameterAnnotations,
               sun.misc.SharedSecrets.getJavaLangAccess().
               getConstantPool(getDeclaringClass()),
               getDeclaringClass());
    }

    void separateWithCommas(Class<?>[] types, StringBuilder sb) {
        for (int j = 0; j < types.length; j++) {
            sb.append(Field.getTypeName(types[j]));
            if (j < (types.length - 1))
                sb.append(",");
        }

    }

    void printModifiersIfNonzero(StringBuilder sb, int mask) {
        int mod = getModifiers() & mask;
        if (mod != 0) {
            sb.append(Modifier.toString(mod)).append(' ');
        }
    }

    String sharedToString(int modifierMask,
                          Class<?>[] parameterTypes,
                          Class<?>[] exceptionTypes) {
        try {
            StringBuilder sb = new StringBuilder();

            printModifiersIfNonzero(sb, modifierMask);
            specificToStringHeader(sb);

            sb.append('(');
            separateWithCommas(parameterTypes, sb);
            sb.append(')');
            if (exceptionTypes.length > 0) {
                sb.append(" throws ");
                separateWithCommas(exceptionTypes, sb);
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * Generate toString header information specific to a method or
     * constructor.
     */
    abstract void specificToStringHeader(StringBuilder sb);

    String sharedToGenericString(int modifierMask) {
        try {
            StringBuilder sb = new StringBuilder();

            printModifiersIfNonzero(sb, modifierMask);

            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for(TypeVariable<?> typeparm: typeparms) {
                    if (!first)
                        sb.append(',');
                    // Class objects can't occur here; no need to test
                    // and call Class.getName().
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }

            specificToGenericStringHeader(sb);

            sb.append('(');
            Type[] params = getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String param = (params[j] instanceof Class)?
                    Field.getTypeName((Class)params[j]):
                    (params[j].toString());
                if (isVarArgs() && (j == params.length - 1)) // replace T[] with T...
                    param = param.replaceFirst("\\[\\]$", "...");
                sb.append(param);
                if (j < (params.length - 1))
                    sb.append(',');
            }
            sb.append(')');
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class)?
                              ((Class)exceptions[k]).getName():
                              exceptions[k].toString());
                    if (k < (exceptions.length - 1))
                        sb.append(',');
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * Generate toGenericString header information specific to a
     * method or constructor.
     */
    abstract void specificToGenericStringHeader(StringBuilder sb);

    /**
     * Returns the {@code Class} object representing the class or interface
     * that declares the executable represented by this object.
     */
    public abstract Class<?> getDeclaringClass();

    /**
     * Returns the name of the executable represented by this object.
     */
    public abstract String getName();

    /**
     * Returns the Java language {@linkplain Modifier modifiers} for
     * the executable represented by this object.
     */
    public abstract int getModifiers();

    /**
     * Returns an array of {@code TypeVariable} objects that represent the
     * type variables declared by the generic declaration represented by this
     * {@code GenericDeclaration} object, in declaration order.  Returns an
     * array of length 0 if the underlying generic declaration declares no type
     * variables.
     *
     * @return an array of {@code TypeVariable} objects that represent
     *     the type variables declared by this generic declaration
     * @throws GenericSignatureFormatError if the generic
     *     signature of this generic declaration does not conform to
     *     the format specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     */
    public abstract TypeVariable<?>[] getTypeParameters();

    /**
     * Returns an array of {@code Class} objects that represent the formal
     * parameter types, in declaration order, of the executable
     * represented by this object.  Returns an array of length
     * 0 if the underlying executable takes no parameters.
     *
     * @return the parameter types for the executable this object
     * represents
     */
    public abstract Class<?>[] getParameterTypes();

    /**
     * Returns an array of {@code Type} objects that represent the formal
     * parameter types, in declaration order, of the executable represented by
     * this object. Returns an array of length 0 if the
     * underlying executable takes no parameters.
     *
     * <p>If a formal parameter type is a parameterized type,
     * the {@code Type} object returned for it must accurately reflect
     * the actual type parameters used in the source code.
     *
     * <p>If a formal parameter type is a type variable or a parameterized
     * type, it is created. Otherwise, it is resolved.
     *
     * @return an array of {@code Type}s that represent the formal
     *     parameter types of the underlying executable, in declaration order
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if any of the parameter
     *     types of the underlying executable refers to a non-existent type
     *     declaration
     * @throws MalformedParameterizedTypeException if any of
     *     the underlying executable's parameter types refer to a parameterized
     *     type that cannot be instantiated for any reason
     */
    public Type[] getGenericParameterTypes() {
        if (hasGenericInformation())
            return getGenericInfo().getParameterTypes();
        else
            return getParameterTypes();
    }

    /**
     * Returns an array of {@code Class} objects that represent the
     * types of exceptions declared to be thrown by the underlying
     * executable represented by this object.  Returns an array of
     * length 0 if the executable declares no exceptions in its {@code
     * throws} clause.
     *
     * @return the exception types declared as being thrown by the
     * executable this object represents
     */
    public abstract Class<?>[] getExceptionTypes();

    /**
     * Returns an array of {@code Type} objects that represent the
     * exceptions declared to be thrown by this executable object.
     * Returns an array of length 0 if the underlying executable declares
     * no exceptions in its {@code throws} clause.
     *
     * <p>If an exception type is a type variable or a parameterized
     * type, it is created. Otherwise, it is resolved.
     *
     * @return an array of Types that represent the exception types
     *     thrown by the underlying executable
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if the underlying executable's
     *     {@code throws} clause refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if
     *     the underlying executable's {@code throws} clause refers to a
     *     parameterized type that cannot be instantiated for any reason
     */
    public Type[] getGenericExceptionTypes() {
        Type[] result;
        if (hasGenericInformation() &&
            ((result = getGenericInfo().getExceptionTypes()).length > 0))
            return result;
        else
            return getExceptionTypes();
    }

    /**
     * Returns a string describing this {@code Executable}, including
     * any type parameters.
     */
    public abstract String toGenericString();

    /**
     * Returns {@code true} if this executable was declared to take a
     * variable number of arguments; returns {@code false} otherwise.
     *
     * @return {@code true} if an only if this executable was declared
     * to take a variable number of arguments.
     */
    public boolean isVarArgs()  {
        return (getModifiers() & Modifier.VARARGS) != 0;
    }

    /**
     * Returns {@code true} if this executable is a synthetic
     * construct; returns {@code false} otherwise.
     *
     * @return true if and only if this executable is a synthetic
     * construct as defined by
     * <cite>The Java&trade; Language Specification</cite>.
     */
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    /**
     * Returns an array of arrays that represent the annotations on
     * the formal parameters, in declaration order, of the executable
     * represented by this object. (Returns an array of length zero if
     * the underlying executable is parameterless.  If the executable has
     * one or more parameters, a nested array of length zero is
     * returned for each parameter with no annotations.) The
     * annotation objects contained in the returned arrays are
     * serializable.  The caller of this method is free to modify the
     * returned arrays; it will have no effect on the arrays returned
     * to other callers.
     *
     * @return an array of arrays that represent the annotations on the formal
     *    parameters, in declaration order, of the executable represented by this
     *    object
     */
    public abstract Annotation[][] getParameterAnnotations();

    Annotation[][] sharedGetParameterAnnotations(Class<?>[] parameterTypes,
                                                 byte[] parameterAnnotations) {
        int numParameters = parameterTypes.length;
        if (parameterAnnotations == null)
            return new Annotation[numParameters][0];

        Annotation[][] result = parseParameterAnnotations(parameterAnnotations);

        if (result.length != numParameters)
            handleParameterNumberMismatch(result.length, numParameters);
        return result;
    }

    abstract void handleParameterNumberMismatch(int resultLength, int numParameters);

    /**
     * {@inheritDoc}
     * @throws NullPointerException  {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == null)
            throw new NullPointerException();

        return (T) declaredAnnotations().get(annotationClass);
    }

    /**
     * {@inheritDoc}
     */
    public Annotation[] getDeclaredAnnotations()  {
        return AnnotationParser.toArray(declaredAnnotations());
    }

    private volatile transient Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

    private Map<Class<? extends Annotation>, Annotation> declaredAnnotations() {
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations = this.declaredAnnotations;
        if (declaredAnnotations == null) {
            this.declaredAnnotations = declaredAnnotations = AnnotationParser.parseAnnotations(
                getAnnotationBytes(),
                sun.misc.SharedSecrets.getJavaLangAccess().
                getConstantPool(getDeclaringClass()),
                getDeclaringClass());
        }
        return declaredAnnotations;
    }
}
