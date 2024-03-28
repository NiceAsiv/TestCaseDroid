package TestCaseDroid;

public class TestCaseDroidApplication {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("USAGE: java -jar TestCaseDroid.jar <path to apk file>");
        } else {
            String apkPath = args[0];
            System.out.println("Analyzing " + apkPath);
        }
    }
}
