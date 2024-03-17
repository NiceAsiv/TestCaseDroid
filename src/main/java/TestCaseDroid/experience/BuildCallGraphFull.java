package TestCaseDroid.experience;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.dot.DotGraph;
import soot.util.queue.QueueReader;


import java.io.File;
import java.security.SecureRandom;
import java.util.*;

public class BuildCallGraphFull {
    public static  void serializeCallGraph(CallGraph graph, String fileName){
        if(fileName==null){
            fileName=soot.SourceLocator.v().getOutputDir();
            if (fileName.length()>0){
                fileName = fileName+ File.separator;
            }
            fileName = fileName+"call-graph"+DotGraph.DOT_EXTENSION;
        }
        System.out.println("writing to file"+fileName);
        DotGraph canvas = new DotGraph("call-graph");
        QueueReader<Edge> listener = graph.listener();
        while (listener.hasNext()){
            Edge next = listener.next();

            MethodOrMethodContext src = next.getSrc();
            MethodOrMethodContext tgt = next.getTgt();

            String srcString = src.toString();
            String tgtString = tgt.toString();
            if(!srcString.startsWith("<java.")&&!srcString.startsWith("<sun.")&&!srcString.startsWith("<com.")&&!srcString.startsWith("jdk.")&&!srcString.startsWith("<org.")&&!srcString.startsWith("<javax.")||(!tgtString.startsWith("<java.")&&!tgtString.startsWith("<sun.")&&!tgtString.startsWith("<com.")&&!tgtString.startsWith("jdk.")&&!tgtString.startsWith("<org.")&&!tgtString.startsWith("<java.")&&!tgtString.startsWith("<javax."))){
                canvas.drawNode(src.toString());
                canvas.drawNode(tgt.toString());
                canvas.drawEdge(src.toString(),tgt.toString());
                System.out.println("src = "+srcString);
                System.out.println("tgt = "+tgtString);
            }
        }
        canvas.plot(fileName);
        return;
    }


    public static void main(String[] args) {
        // From
        // https://github.com/pcpratts/soot-rb/blob/master/tutorial/guide/examples/call_graph/src/dk/brics/soot/callgraphs/CallGraphExample.java
        List<String> argsList = new ArrayList<String>(Arrays.asList(args));
        argsList.addAll(Arrays.asList(new String[] { "-w", "-main-class", "testers.CallGraphs", // main-class
                "testers.CallGraphs", // argument classes
                "testers.A" //
        }));
        String[] args2 = new String[argsList.size()];
        args2 = argsList.toArray(args2);
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map options) {
                CHATransformer.v().transform();
                SootClass a = Scene.v().getSootClass("testers.A");
                SootMethod src = Scene.v().getMainClass().getMethodByName("doStuff");
                CallGraph cg = Scene.v().getCallGraph();

                serializeCallGraph(cg, "output" + DotGraph.DOT_EXTENSION);
                System.out.println("serializeCallGraph completed.");

                Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
                while (targets.hasNext()) {
                    SootMethod tgt = (SootMethod) targets.next();
                    System.out.println(src + " may call " + tgt);
                }
            }
        }));
        args = argsList.toArray(new String[0]);
        soot.Main.main(args2);
    }
}
