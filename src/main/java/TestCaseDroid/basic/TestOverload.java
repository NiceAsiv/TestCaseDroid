package TestCaseDroid.basic;

/**
 * 这个程序的作用是测试JVM 方法签名和方法描述
 */
public class TestOverload {
    public String method1(String str) {
        String mtdName = Thread.currentThread().getStackTrace()[1].getMethodName();//获取当前方法名称，具体使用数组的那个元素和JVM的实现有关，具体说明可以查看Thread.getStackTrace方法的javadoc
        System.out.println("invoke " + mtdName + " return String");
        return "";
    }

    public int method2(String str) {
        String mtdName = Thread.currentThread().getStackTrace()[1].getMethodName();
        System.out.println("invoke " + mtdName + " return int");
        return 1;
    }

    public static void main(String[] args) {
        TestOverload javacTestOverload = new TestOverload();
        String str = javacTestOverload.method1("Test");
        int i = javacTestOverload.method2("Test");
    }
}
