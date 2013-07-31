package com.opower.persistence.jpile.reflection;

import java.lang.reflect.Method;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

import com.opower.persistence.jpile.sample.Contact;
import com.opower.persistence.jpile.sample.Customer;
import com.opower.persistence.jpile.sample.Product;
import org.junit.Test;

import static com.google.common.collect.ImmutableList.copyOf;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests all caching logic for correctness
 *
 * @author amir.raminfar
 */
public class PersistenceAnnotationInspectorTest {
    private PersistenceAnnotationInspector annotationInspector = new PersistenceAnnotationInspector();

    @Test
    public void testHasTableAnnotation() throws Exception {
        assertTrue(annotationInspector.hasTableAnnotation(Customer.class));
        assertTrue(annotationInspector.hasTableAnnotation(Product.class));
    }

    @Test
    public void testTableName() throws Exception {
        assertEquals("customer", annotationInspector.tableName(Customer.class));
        assertEquals("product", annotationInspector.tableName(Product.class));
    }

    @Test
    public void testSecondaryTable() throws Exception {
        assertNull(annotationInspector.secondaryTable(Customer.class));
    }

    @Test
    public void testIdGetter() throws Exception {
        assertEquals("getId", annotationInspector.idGetter(Customer.class).getName());
    }

    @Test
    public void testSetterFromGetter() throws Exception {
        assertEquals(
                Customer.class.getMethod("setId", Long.class),
                annotationInspector.setterFromGetter(Customer.class.getMethod("getId"))
        );
    }

    @Test
    public void testGetterFromSetter() throws Exception {
        assertEquals(
                Customer.class.getMethod("getId"),
                annotationInspector.getterFromSetter(Customer.class.getMethod("setId", Long.class))
        );
    }

    @Test
    public void testFieldFromGetter() throws Exception {
        assertEquals("id", annotationInspector.fieldFromGetter(
                annotationInspector.idGetter(Customer.class)).getName()
        );
    }

    @Test
    public void testMethodsAnnotatedWith() {
        List<PersistenceAnnotationInspector.AnnotatedMethod<Column>> methods =
                annotationInspector.annotatedMethodsWith(Customer.class, Column.class);
        for (PersistenceAnnotationInspector.AnnotatedMethod<Column> methodWithAnnotations : methods) {
            assertNotNull("Must have @Column", methodWithAnnotations.getMethod().getAnnotation(Column.class));
        }
    }

    /** A test interface. */
    interface TestBaseClass {
        Number getValue();
    }

    /**
     * Verify that the actual method that has the {@link Column} annotation is used, and not some other method signature.
     */
    @Test
    public void testMethodsAnnotatedWithInheritance() throws Exception {
        /** A test class. */
        class TestClass implements TestBaseClass {
            @Column
            @Override
            public Integer getValue() {
                return null;
            }
        }

        List<PersistenceAnnotationInspector.AnnotatedMethod<Column>> methods =
                annotationInspector.annotatedMethodsWith(TestClass.class, Column.class);
        assertEquals("Number of found methods", 1, methods.size());
        assertEquals("Found method", TestClass.class.getMethod("getValue"), methods.get(0).getMethod());
        assertNotNull("Column annotation on method", methods.get(0).getMethod().getAnnotation(Column.class));
    }

    @Test
    public void testMethodsWithMultipleAnnotations() {
        List<Method> methods = annotationInspector.methodsAnnotatedWith(
                Customer.class, OneToOne.class, PrimaryKeyJoinColumn.class
        );
        assertEquals(1, methods.size());

    }

    @Test
    public void testFindAnnotation() {
        assertNotNull(annotationInspector.findAnnotation(Customer.class, Table.class));
    }

    @Test
    public void testHasAnnotation() {
        assertTrue(annotationInspector.hasAnnotation(Customer.class, Entity.class));
    }

    @Test
    public void testFindSecondaryTableAnnotations() throws Exception {
        assertEquals(
                copyOf(Contact.class.getAnnotation(SecondaryTables.class).value()),
                annotationInspector.findSecondaryTables(Contact.class)
        );
    }

    @Test
    public void testGetterFromSetterWithGetBoolean() throws Exception {
        assertEquals(
                JavaBeanBoolean.class.getMethod("getA"),
                annotationInspector.getterFromSetter(JavaBeanBoolean.class.getMethod("setA", boolean.class))
        );
    }

    @Test
    public void testGetterFromSetterWithIsBoolean() throws Exception {
        assertEquals(
                JavaBeanBoolean.class.getMethod("isB"),
                annotationInspector.getterFromSetter(JavaBeanBoolean.class.getMethod("setB", Boolean.class))
        );
    }

    @Test
    public void testSetterFromGetterWithGetBoolean() throws Exception {
        assertEquals(
                JavaBeanBoolean.class.getMethod("setA", boolean.class),
                annotationInspector.setterFromGetter(JavaBeanBoolean.class.getMethod("getA"))
        );
    }

    @Test
    public void testSetterFromGetterWithIsBoolean() throws Exception {
        assertEquals(
                JavaBeanBoolean.class.getMethod("setB", Boolean.class),
                annotationInspector.setterFromGetter(JavaBeanBoolean.class.getMethod("isB"))
        );
    }

    @Test
    public void testFieldFromGetterWithGetBoolean() throws Exception {
        assertEquals(
                "isA",
                annotationInspector.fieldFromGetter(JavaBeanBoolean.class.getMethod("getA")).getName()
        );
    }

    @Test
    public void testFieldFromGetterWithIsBoolean() throws Exception {
        assertEquals(
                "isB",
                annotationInspector.fieldFromGetter(JavaBeanBoolean.class.getMethod("isB")).getName()
        );
    }

    /**
     * A class used to test Java Beans with boolean properties.
     */
    private static class JavaBeanBoolean {
        private boolean isA;
        private Boolean isB;

        public boolean getA() {
            return isA;
        }

        public void setA(boolean isA) {
            this.isA = isA;
        }

        public Boolean isB() {
            return isB;
        }

        public void setB(Boolean isB) {
            this.isB = isB;
        }
    }
}
