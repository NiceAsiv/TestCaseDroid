package TestCaseDroid.test;

public class CFG {
    private String CFGname;
    private String CFGtype;
    private String CFGpath;
    private String CFGalgorithm;
    int field1;


    public CFG(String CFGname, String CFGtype, String CFGpath, String algorithm, int field1) {
        this.CFGname = CFGname;
        this.CFGtype = CFGtype;
        this.CFGpath = CFGpath;
        this.CFGalgorithm = algorithm;
        this.field1 = field1;
    }

    public void method1(int a,int b) {
//        int a = 10;
        CFGname = "Useless assignment";  // Useless statement
        if (a > 5 && this.field1 < 4) {
            b = a + this.field1;
            method2();
        } else {
            method3();
        }

        if (b > 4) {
            method2();
        } else {
            method3();
        }
        CFGtype = "Another useless assignment";  // Useless statement
    }


    public void method2() {
        int b = 20;
        CFGpath = "Yet another useless assignment";  // Useless statement
        while (b > 0) {
            b--;
        }
        CFGalgorithm = "Final useless assignment";  // Useless statement
        if (b == 0) {
            method3();
        }
    }
    public void method2(int a) {
        int b = 20;
        CFGpath = "Yet another useless assignment";  // Useless statement
        while (b > 0) {
            b--;
        }
        CFGalgorithm = "Final useless assignment";  // Useless statement
        if (b == 0) {
            method3();
        }
    }
    public void method2(String a) {
        int b = 20;
        CFGpath = "Yet another useless assignment";  // Useless statement
        while (b > 0) {
            b--;
        }
        CFGalgorithm = "Final useless assignment";  // Useless statement
        if (b == 0) {
            method3();
        }
    }
    public void method2(int c,int d) {
        int b = 20;
        CFGpath = "Yet another useless assignment";  // Useless statement
        while (b > 0) {
            b--;
        }
        CFGalgorithm = "Final useless assignment";  // Useless statement
        if (b == 0) {
            method3();
        }
    }

    public void method3() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
        }
        CFGname = CFGtype = CFGpath = CFGalgorithm = "Assigning all fields at once";  // Useless statement
    }
}
