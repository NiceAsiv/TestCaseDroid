package TestCaseDroid.analysis.info;

import java.util.ArrayList;
import java.util.List;

class MethodInfo {
    public String className;
    public String methodName;
    public int paramNum;
    public List<String> paramters = new ArrayList<>();
    public Boolean isOverRide = false;
    public Boolean isConstructor = false;

    MethodInfo() {
    }

    MethodInfo(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }
    MethodInfo(String className, String methodName, int paramNum, List<String> parameters) {
        this.className = className;
        this.methodName = methodName;
        this.paramNum = paramNum;
        this.paramters = parameters;
    }
}
