package tests;
/*
    This is a simple test case to test the correctness of the call graph.
 */
public class CallGraphTest {

    public static void main(String[] args) {
        doSomething();
    }

    public static void doSomething() {
        AC a = new AC();
        a.MethodA("Hello");
    }

    class B{
        public String MethodB(String param){
            return new AC().MethodA(param);
        }
    }
}


class AC{
    public String MethodA(String param){
        return param;
    }
    public void foo() {
        bar();
    }
    public void bar() {
        System.out.println("bar");
    }

}
