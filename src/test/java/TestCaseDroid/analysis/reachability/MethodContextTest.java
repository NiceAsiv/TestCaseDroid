package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodContextTest {
    @Test
    void parsesRegularAndInitializerSignatures() {
        MethodContext regular = new MethodContext(
                "<TestCaseDroid.test.Example: java.lang.String load(int,java.lang.String[])>");
        assertEquals("TestCaseDroid.test.Example", regular.getClassName());
        assertEquals("load", regular.getMethodName());
        assertEquals("java.lang.String", regular.getReturnType());
        assertEquals(Arrays.asList("int", "java.lang.String[]"), regular.getParamTypes());

        MethodContext classInitializer = new MethodContext(
                "<TestCaseDroid.test.Example: void <clinit>()>");
        assertEquals("<clinit>", classInitializer.getMethodName());
        assertEquals("void", classInitializer.getReturnType());

        MethodContext legacyConstructor = new MethodContext(
                "<TestCaseDroid.test.Example:<init>(java.lang.String)>");
        assertEquals("<init>", legacyConstructor.getMethodName());
        assertNull(legacyConstructor.getReturnType());
    }

    @Test
    void trimsParametersAndRejectsMalformedSignatures() {
        MethodContext context = new MethodContext(
                "<a.b.C: void run(int, java.util.List<java.lang.String>)>");
        assertEquals(Arrays.asList("int", "java.util.List<java.lang.String>"),
                context.getParamTypes());

        assertThrows(IllegalArgumentException.class, () -> new MethodContext(null));
        assertThrows(IllegalArgumentException.class, () -> new MethodContext("a.b.C#run"));
        assertThrows(IllegalArgumentException.class,
                () -> new MethodContext("<a.b.C: run()>"));
    }

    @Test
    void componentConstructorBuildsCanonicalSignature() {
        MethodContext context = new MethodContext(
                "a.b.C", "run", "void", Arrays.asList("int", "long"));
        assertEquals("<a.b.C: void run(int,long)>", context.getMethodSignature());
    }
}
