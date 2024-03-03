package demo;

public class GenJimple {
    public static void main(String[] args){
        String classpath = args[0]; 
		System.out.println(classpath);
		soot.Main.main(new String[] {
                "-f", "J",
                "-soot-class-path", classpath,
                "-pp",
                args[1]
		});
        return;
    }
}
