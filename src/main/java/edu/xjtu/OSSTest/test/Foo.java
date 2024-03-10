package edu.xjtu.OSSTest.test;

public class Foo {
    int a, b;

    public Foo(int x, int y) {
        a = x;
        b = y;
    }

    public int minus() {
        int m = a - b;
        return m;
    }

    public int divide() {
        int d;
        if (b == 0) {
            d = 0;
        } else {
            d = a / b;
        }
        return d;
    }
}
