package demo;

import soot.*;
import soot.toolkits.graph.BriefUnitGraph;

import java.util.Map;

public class GetProgramStructure {

	public static void main(String[] args) {
		
		PackManager.v().getPack("wjtp").add(
				
				new Transform("wjtp.myanalysis", new SceneTransformer() {
						@Override
						protected void internalTransform(String arg0, 
								Map<String, String> arg1) {
							SootClass mainClass = Scene.v().getMainClass();
							System.out.println("Main class is "+ mainClass);
							for (SootMethod m: mainClass.getMethods()) {
								System.out.println("Method "+ m);
							}
							for (SootField f: mainClass.getFields()) {
								System.out.println("Field "+f);
							}
							SootMethod mainMethod = Scene.v().getMainMethod();
							System.out.println("Main method is "+ mainMethod);
							System.out.println("Super class is "+mainClass.getSuperclass());
							for (Unit u: mainMethod.getActiveBody().getUnits()) {
								System.out.println("Unit "+u);
							}
							Body b = mainMethod.getActiveBody();
							BriefUnitGraph g = new BriefUnitGraph(b);
							LivenessAnalysis la = new LivenessAnalysis(g);
							for (Unit u : b.getUnits()) {
								System.out.println(u);
								System.out.println(" Before: " + la.getFlowBefore(u));
								System.out.println(" After: " + la.getFlowAfter(u));
							}
						}
					}
				)
				
			);
		
        String classpath = args[0]; 
		System.out.println(classpath);
		soot.Main.main(new String[] {
				"-w",
                "-f", "J",
				// "-p", "cg.spark", "enabled:true",
				"-p", "jb", "use-original-names:true",
				"-p", "wjtp.myanalysis", "enabled:true",
                "-soot-class-path", classpath,
                "-pp",
                args[1]
		});
	}
}
