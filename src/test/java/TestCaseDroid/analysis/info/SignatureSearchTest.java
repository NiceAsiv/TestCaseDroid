package TestCaseDroid.analysis.info;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureSearchTest {
    private static final String CLASSES = Paths.get("target", "classes").toAbsolutePath().toString();

    @Test
    void parsesSupportedIdeaReferenceForms() {
        assertTrue(SignatureSearch.parseIDEARef("TestCaseDroid.test.CFG#method2(int)"));
        assertTrue(SignatureSearch.parseIDEARef(
                "a.b.C#run(java.util.List<java.lang.String>, java.lang.String...)"));
        assertTrue(SignatureSearch.parseIDEARef("TestCaseDroid.test.CFG#method2()"));
        assertTrue(SignatureSearch.parseIDEARef("TestCaseDroid.test.CallGraphs#main"));
    }

    @Test
    void rejectsMalformedReferences() {
        assertFalse(SignatureSearch.parseIDEARef("TestCaseDroid.test.CFG#method2(int"));
        assertFalse(SignatureSearch.parseIDEARef("TestCaseDroid.test.CFG.method2"));
        assertFalse(SignatureSearch.parseIDEARef(null));
    }

    @Test
    void resolvesExactOverloadsWithoutLeakingStateBetweenCalls() {
        assertEquals("<TestCaseDroid.test.CFG: void method2(int)>",
                SignatureSearch.getMethodSignatureByIDEARef(
                        "TestCaseDroid.test.CFG#method2(int)", CLASSES));
        assertEquals("<TestCaseDroid.test.CFG: void method2(java.lang.String)>",
                SignatureSearch.getMethodSignatureByIDEARef(
                        "TestCaseDroid.test.CFG#method2(java.lang.String)", CLASSES));
        assertEquals("<TestCaseDroid.test.CFG: void method2()>",
                SignatureSearch.getMethodSignatureByIDEARef(
                        "TestCaseDroid.test.CFG#method2()", CLASSES));
    }

    @Test
    void returnsNullForInvalidOrAmbiguousReference() {
        assertNull(SignatureSearch.getMethodSignatureByIDEARef("InvalidRef", CLASSES));
        assertNull(SignatureSearch.getMethodSignatureByIDEARef(
                "TestCaseDroid.test.CFG#method2", CLASSES));
    }

    @Test
    void returnsAllSignaturesForNameSearch() {
        SignatureSearch search = new SignatureSearch(
                "TestCaseDroid.test.CFG", "method2", CLASSES);
        List<String> signatures = search.findMethodSignatures();
        assertEquals(4, signatures.size());
        assertTrue(signatures.contains("<TestCaseDroid.test.CFG: void method2(int,int)>"));
    }
}
