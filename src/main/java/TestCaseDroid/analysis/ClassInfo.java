package TestCaseDroid.analysis;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
@Setter
@Getter
public class ClassInfo {
    private String packageName;
    private String className;
    private Map<String, String> classFields;
    private Map<String, String> classMethods;
    private Map<String, String> classMethodBodies;

    public ClassInfo(String className) {
        this.className = className;
        this.classFields = new HashMap<>();
        this.classMethods = new HashMap<>();
        this.classMethodBodies = new HashMap<>();
    }
    public void addField(String fieldName, String fieldValue) {
        this.classFields.put(fieldName, fieldValue);
    }

    public void addMethod(String methodName, String methodSignature) {
        this.classMethods.put(methodName, methodSignature);
    }

    public void addMethodBody(String methodName, String methodBody) {
        this.classMethodBodies.put(methodName, methodBody);
    }
}
