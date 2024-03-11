package OSSTest.demo;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

public class LivenessAnalysis extends BackwardFlowAnalysis<Unit, Set<Local> >{

	public LivenessAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		doAnalysis();
	}

	@Override
	protected void flowThrough(Set<Local> in, Unit unit, Set<Local> out) {
		out.clear(); out.addAll(in);
		// kill
		for (ValueBox vb : unit.getDefBoxes()) {
			Value v = vb.getValue();
			if (v instanceof Local)
				out.remove((Local)v);
		}
		// gen
		for (ValueBox vb : unit.getUseBoxes()) {
			Value v = vb.getValue();
			if (v instanceof Local)
				out.add((Local)v);
		}
	}

	@Override
	protected void copy(Set<Local> source, Set<Local> dest) {
		dest.clear(); dest.addAll(source);
	}

	@Override
	protected void merge(Set<Local> in0, Set<Local> in1, Set<Local> out) {
		out.clear(); out.addAll(in0); out.addAll(in1);
	}

	@Override
	protected Set<Local> newInitialFlow() {
		return new HashSet<Local>();
	}

	@Override
	protected Set<Local> entryInitialFlow() {
		return newInitialFlow();
	}

}
