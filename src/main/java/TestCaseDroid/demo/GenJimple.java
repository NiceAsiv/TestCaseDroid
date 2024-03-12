package TestCaseDroid.demo;

/**
 * GenJimple类
 * 这个类是用来生成Jimple代码的。
 * 使用方法：java GenJimple <classpath> <classfile>
 * 例如：java GenJimple ./target/classes TestCaseDroid.tests.Main
 */
public class GenJimple {

    /**
     * 主函数
     * @param args 命令行参数，args[0]是类路径，args[1]是类文件
     */
    public static void main(String[] args){
        // 获取类路径
        String classpath = args[0];
        // 打印类路径
        System.out.println(classpath);
        // 调用soot.Main的main方法，生成Jimple代码
        soot.Main.main(new String[] {
                "-f", "J", // 输出格式为Jimple
                "-soot-class-path", classpath, // 设置Soot的类路径
                "-pp", // 使用Soot的默认类路径
                args[1] // 要分析的类文件
        });
        // 结束程序
        return;
    }
}