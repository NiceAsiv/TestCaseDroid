package OSSTest.demo;

import soot.*;
import soot.toolkits.graph.BriefUnitGraph;

import java.util.Map;

/**
 * GetProgramStructure类
 * 这个类是用来获取程序结构的。
 * 使用方法：java GetProgramStructure <classpath> <classfile>
 * 例如：java GetProgramStructure ./target/classes OSSTest.tests.LiveAnalysis
 */
public class GetProgramStructure {

	/**
	 * 主函数
	 * @param args 命令行参数，args[0]是类路径，args[1]是类文件
	 */
	public static void main(String[] args) {

		// 添加一个新的转换到wjtp包
		PackManager.v().getPack("wjtp").add(
				new Transform("wjtp.myanalysis", new SceneTransformer() {
					@Override
					protected void internalTransform(String arg0, Map<String, String> arg1) {
						// 获取主类
						SootClass mainClass = Scene.v().getMainClass();
						System.out.println("Main class is "+ mainClass);
						// 打印主类的所有方法
						for (SootMethod m: mainClass.getMethods()) {
							System.out.println("Method "+ m);
						}
						// 打印主类的所有字段
						for (SootField f: mainClass.getFields()) {
							System.out.println("Field "+f);
						}
						// 获取主方法
						SootMethod mainMethod = Scene.v().getMainMethod();
						System.out.println("Main method is "+ mainMethod);
						// 打印主类的超类
						System.out.println("Super class is "+mainClass.getSuperclass());
						// 打印主方法的所有单元
						for (Unit u: mainMethod.getActiveBody().getUnits()) {
							System.out.println("Unit "+u);
						}
						// 获取主方法的活动体
						Body b = mainMethod.getActiveBody();
						// 创建一个新的单元图
						BriefUnitGraph g = new BriefUnitGraph(b);
						// 创建一个新的活跃性分析
						LivenessAnalysis la = new LivenessAnalysis(g);
						// 打印每个单元的流入和流出集
						for (Unit u : b.getUnits()) {
							System.out.println(u);
							System.out.println(" Before: " + la.getFlowBefore(u));
							System.out.println(" After: " + la.getFlowAfter(u));
						}
					}
				})
		);

		// 获取类路径
		String classpath = args[0];
		System.out.println(classpath);
		// 调用soot.Main的main方法，获取程序结构
		soot.Main.main(new String[] {
				"-w",
				"-f", "J",
				"-p", "jb", "use-original-names:true",
				"-p", "wjtp.myanalysis", "enabled:true",
				"-soot-class-path", classpath,
				"-pp",
				args[1]
		});
	}
}