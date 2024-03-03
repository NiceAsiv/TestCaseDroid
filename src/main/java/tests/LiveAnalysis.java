package tests;

public class LiveAnalysis {
    // private static int x=0,y=0,z=0,t=0,a=0,b=0,c=0;
    private static void bar(int a, int b, int c){
        return;
    }
    public static void main(String[] args) {
        // int x=0,y=0,z=0,t=0,a=0,b=0,c=0;
        int x = Integer.valueOf(args[0]);
        int y = Integer.valueOf(args[1]); 
        int z = Integer.valueOf(args[2]);
        int t = Integer.valueOf(args[3]);
        int a = Integer.valueOf(args[4]);
        int b = Integer.valueOf(args[5]);
        int c = Integer.valueOf(args[6]);
        a = x + y;
        t = a *5;
        c = a + x;
        if (x == 0) {
            b = t + z;
        }
        c = y + 1;
        bar(a,b,c);
        return;
    }
}
