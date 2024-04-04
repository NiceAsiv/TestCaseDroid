package TestCaseDroid.test;

import java.util.List;

public class CallGraph {
    private String a;
    private static String b;
    private int c = 1 ;
    private static int d = 2;
    public List<String> e;
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