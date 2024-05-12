package TestCaseDroid.analysis.info;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SignatureSearchTest {
    @Test
    void shouldParseValidIDEARef() {
        // TestCaseDroid.test.CFG#method2(int)
        // TestCaseDroid.test.CFG#method2(java.lang.String)
        // TestCaseDroid.test.CFG#method2()
        // TestCaseDroid.test.CFG#method2(int, int)
        String ideaRef = "TestCaseDroid.test.CFG#method2(int)";
        assertTrue(SignatureSearch.parseIDEARef(ideaRef));
        String ideaRef2 = "TestCaseDroid.test.CFG#method2(java.lang.String)";
        assertTrue(SignatureSearch.parseIDEARef(ideaRef2));
        String ideaRef3 = "TestCaseDroid.test.CFG#method2()";
        assertTrue(SignatureSearch.parseIDEARef(ideaRef3));
        String ideaRef4 = "TestCaseDroid.test.CFG#method2(int, int)";
        assertTrue(SignatureSearch.parseIDEARef(ideaRef4));
        String ideaRef5 = "TestCaseDroid.test.CFG#main";
        assertTrue(SignatureSearch.parseIDEARef(ideaRef5));
    }

    @Test
    void shouldNotParseInvalidIDEARef() {
        String ideaRef = "TestCaseDroid.test.CFG#method2(int";
        assertFalse(SignatureSearch.parseIDEARef(ideaRef));
    }

    @Test
    void shouldGetMethodSignatureByValidIDEARef() {
        String ideaRef = "TestCaseDroid.test.CFG#method2(int)";
        String expectedSignature = "<TestCaseDroid.test.CFG: void method2(int)>";
        assertEquals(expectedSignature, SignatureSearch.getMethodSignatureByIDEARef(ideaRef, "E:\\Tutorial\\TestCaseDroid\\target\\classes"));
        String ideaRef2 = "TestCaseDroid.test.CallGraphs#main";
        String expectedSignature2 = "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>";
        assertEquals(expectedSignature2, SignatureSearch.getMethodSignatureByIDEARef(ideaRef2, "E:\\Tutorial\\TestCaseDroid\\target\\classes"));
    }

    @Test
    void shouldReturnNullForInvalidIDEARef() {
        String ideaRef = "InvalidRef";
        assertNull(SignatureSearch.getMethodSignatureByIDEARef(ideaRef, "E:\\Tutorial\\TestCaseDroid\\target\\classes"));
    }

    @Test
    void shouldGetMethodSignature() {
        SignatureSearch signatureSearch = new SignatureSearch("TestCaseDroid.test.CFG", "method2", "E:\\Tutorial\\TestCaseDroid\\target\\classes");
        // TODO: Add assertions for the expected output
        signatureSearch.getMethodSignature();
    }
}