package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.experience.BuildCallGraph2;
import TestCaseDroid.utils.*;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;

import java.util.Map;
public class BuildCallGraphTest extends SceneTransformer {
    public static String mainClass = "TestCaseDroid.test.CallGraphs";
    public static String targetPackageName = "TestCaseDroid";
    public static void main(String[] args) {

        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);

        //add an inter-procedural analysis phase to Soot
        BuildCallGraphTest analysis = new BuildCallGraphTest();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));

        //判断mainClass是否为应用类
        SootInfoUtils.isApplicationClass(mainClass);
        //输出当前分析环境下的application类和每个类所加载的函数签名
        SootInfoUtils.reportSootApplicationClassInfo();
        //设置入口方法
        SootAnalysisUtils.setEntryPoints(mainClass,"main");

        //新增应用类A2
        SootClass sc2 = Scene.v().loadClassAndSupport("TestCaseDroid.test.A2");
        sc2.setApplicationClass();
        System.out.println("-----------------------------------------");
        System.out.println(Scene.v().getApplicationClasses());

        //运行分析
        PackManager.v().runPacks();

    }
    @Override
    protected void internalTransform(String phaseName, Map options) {
        CallGraph callGraph = Scene.v().getCallGraph();
        DotGraphWrapper dotGraph = new DotGraphWrapper("callgraph");
        QueueReader<Edge> listener = callGraph.listener();
        while (listener.hasNext()){
            Edge next = listener.next();
            SootMethod src = next.src();
            SootMethod tgt = next.tgt();

            String srcString = src.toString();
            String tgtString = tgt.toString();
            //此处的实现方式还比较简单，仅过滤掉了以java.开头的包，并要求起点和终点方法必须有一个为应用类开头，之后可以尝试结合排除包函数以及利用startswith判断起点和终点是否属于同一个项目来对整个项目进行分析，原生成方式的问题是仅会输出应用类方法直接调用的函数，但当调用的函数属于被分析项目时，出现程序不进一步进行分析的问题，或许这个问题也可以用递归的方式解决？或者是soot其实提供了前向后向搜索方法只是我没有找到？暂时不知道，还需要进一步学习
            if((src.getDeclaringClass().isApplicationClass() || tgt.getDeclaringClass().isApplicationClass())&&!src.getDeclaringClass().toString().startsWith("java.")&&!tgt.getDeclaringClass().toString().startsWith("java.")) {
                dotGraph.drawNode(src.toString());
                dotGraph.drawNode(tgt.toString());
                dotGraph.drawEdge(src.toString(), tgt.toString());
                System.out.println("src = " + srcString);
                System.out.println("tgt = " + tgtString);
            }
        }
        dotGraph.plot(mainClass,"cg");
        System.out.println("finish!");

    }
}
