package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodContextTest {

        @Test
        void testMethodContext() {
                // MethodContext methodContext = new MethodContext(
                // "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>");
                // assertEquals("TestCaseDroid.test.CallGraphs", methodContext.getClassName());
                // assertEquals("main", methodContext.getMethodName());
                // assertEquals("void", methodContext.getReturnType());
                // assertEquals("<TestCaseDroid.test.CallGraphs: void
                // main(java.lang.String[])>",
                // methodContext.getMethodSignature());
                // assertEquals(1, methodContext.getParamTypes().size());
                // assertEquals("java.lang.String[]", methodContext.getParamTypes().get(0));
                // <TestCaseDroid.test.ICFG: void <clinit>()>
                MethodContext methodContext1 = new MethodContext("<TestCaseDroid.test.ICFG: void <clinit>()>");
                assertEquals("TestCaseDroid.test.ICFG", methodContext1.getClassName());
                assertEquals("<clinit>", methodContext1.getMethodName());
                assertEquals("void", methodContext1.getReturnType());
                assertEquals("<TestCaseDroid.test.ICFG: void <clinit>()>", methodContext1.getMethodSignature());
                // <TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>
                MethodContext methodContext2 = new MethodContext(
                                "<TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>");
                assertEquals("TestCaseDroid.test.Vulnerable", methodContext2.getClassName());
                assertEquals("main", methodContext2.getMethodName());
                assertEquals("void", methodContext2.getReturnType());
                assertEquals("<TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>",
                                methodContext2.getMethodSignature());
                // <TestCaseDroid.test.CFG: void method1(int,int)>
                MethodContext methodContext3 = new MethodContext("<TestCaseDroid.test.CFG: void method1(int,int)>");
                assertEquals("TestCaseDroid.test.CFG", methodContext3.getClassName());
                assertEquals("method1", methodContext3.getMethodName());
                assertEquals("void", methodContext3.getReturnType());
                assertEquals(2, methodContext3.getParamTypes().size());
                assertEquals("<TestCaseDroid.test.CFG: void method1(int,int)>", methodContext3.getMethodSignature());
                // <org.apache.commons.beanutils2.BeanUtilsBean:<init>(org.apache.commons.beanutils2.ConvertUtilsBean)>;
                MethodContext methodContext4 = new MethodContext(
                                "<org.apache.commons.beanutils2.BeanUtilsBean:<init>(org.apache.commons.beanutils2.ConvertUtilsBean)>");
                assertEquals("org.apache.commons.beanutils2.BeanUtilsBean", methodContext4.getClassName());
                assertEquals("<init>", methodContext4.getMethodName());
                assertNull(methodContext4.getReturnType());
                assertEquals("<org.apache.commons.beanutils2.BeanUtilsBean:<init>(org.apache.commons.beanutils2.ConvertUtilsBean)>",
                                methodContext4.getMethodSignature());
                // <org.apache.commons.beanutils2.BeanUtilsBean:<init>(org.apache.commons.beanutils2.ConvertUtilsBean,int)>;
                MethodContext methodContext5 = new MethodContext(
                                "<org.apache.commons.beanutils2.BeanUtilsBean:<init>(org.apache.commons.beanutils2.ConvertUtilsBean,int)>");
                assertEquals("org.apache.commons.beanutils2.BeanUtilsBean", methodContext5.getClassName());
                assertEquals("<init>", methodContext5.getMethodName());
                assertNull(methodContext5.getReturnType());
                assertEquals("<org.apache.commons.beanutils2.BeanUtilsBean:<init>(org.apache.commons.beanutils2.ConvertUtilsBean,int)>",
                                methodContext5.getMethodSignature());
        }
}