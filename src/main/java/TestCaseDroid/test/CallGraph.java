package TestCaseDroid.test;

public class CallGraph {
    public static void main(String[] args) {
        doStuff();
    }

    public static void doStuff() {
        new D().foo();
    }
}

class D
{
    public void foo() {
        bar();
    }

    public void bar() {
    }
}