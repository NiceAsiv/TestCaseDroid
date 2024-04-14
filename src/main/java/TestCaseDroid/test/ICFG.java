package TestCaseDroid.test;

import lombok.Setter;

@Setter
public class ICFG {
    String inputFilePath;
    private static int graphType = 1;
    public void test1() {
        System.out.println("now in test1");
        test2();
    }
    public void test2() {
        System.out.println("now in test2");
        switch (graphType) {
            case 1:
                test3();
                break;
            case 2:
                System.out.println("graphType is 2");
                break;
            default:
                System.out.println("graphType is not 1 or 2");
        }
    }
    public void test3() {
        System.out.println("now in test3");
        if (graphType == 1) {
            System.out.println("graphType is 1");
        } else {
            System.out.println("graphType is not 1");
        }
    }
}
class Test4 {
    public void test4() {
        System.out.println("now in test4");
        ICFG icfg = new ICFG();
        icfg.test3();
        System.out.println("test4");
        icfg.test1();
    }
}
class Vulnerable {
    public void vulnerable() {
        System.out.println("--------------------");
        System.out.println("now in vulnerable");
        System.out.println("Start running the first test case");
        Test4 test4 = new Test4();
        test4.test4();
        System.out.println("End running the first test case");
        System.out.println("--------------------");
        System.out.println("Start running the second test case");
        ICFG icfg = new ICFG();
        icfg.test1();
        System.out.println("End running the second test case");
        System.out.println("--------------------");
    }
    public static void main(String[] args) {
        Vulnerable vulnerable = new Vulnerable();
        vulnerable.vulnerable();
    }
}
