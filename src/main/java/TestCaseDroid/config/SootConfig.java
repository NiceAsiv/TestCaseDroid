package TestCaseDroid.config;

import TestCaseDroid.utils.SootUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Creates an isolated Soot scene for one analysis.
 *
 * <p>Soot stores most analysis state in global singletons. Every setup therefore
 * starts with {@link G#reset()} and the setup operation is synchronized so that
 * two analyses cannot corrupt one another in the same JVM.</p>
 */
@Setter
@Getter
@Slf4j
public class SootConfig {
    private static final String JVM_CLASS_PATH = System.getProperty("java.class.path", "");

    private String callGraphAlgorithm = "CHA";

    public void setupSoot(String className, Boolean constructCallGraph) {
        setupSoot(className, constructCallGraph, null, Collections.<String>emptyList());
    }

    public void setupSoot(String className, Boolean constructCallGraph, String classesPath) {
        setupSoot(className, constructCallGraph, classesPath, Collections.<String>emptyList());
    }

    /**
     * Configures Soot and, when requested, builds a call graph rooted at the
     * supplied entry method signatures. If no signatures are supplied, the
     * class's {@code main} method is preferred; classes without a main method
     * use all of their concrete methods as analysis roots.
     */
    public void setupSoot(String className, Boolean constructCallGraph, String classesPath,
                          Collection<String> entryMethodSignatures) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("The entry class must not be blank");
        }

        synchronized (SootConfig.class) {
            G.reset();
            configureOptions(classesPath);

            SootClass appClass = Scene.v().loadClassAndSupport(className.trim());
            appClass.setApplicationClass();
            Scene.v().loadNecessaryClasses();

            List<SootMethod> entryPoints = resolveEntryPoints(appClass, entryMethodSignatures);
            if (!entryPoints.isEmpty()) {
                Scene.v().setEntryPoints(entryPoints);
            }

            if (constructCallGraph) {
                configureCallGraphAlgorithm();
                PackManager.v().runPacks();
            }
        }
    }

    private void configureOptions(String classesPath) {
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().set_exclude(SootUtils.excludeClassesList);
        Options.v().set_no_bodies_for_excluded(true);

        List<String> inputEntries = splitClassPath(classesPath);
        if (!inputEntries.isEmpty()) {
            for (String entry : inputEntries) {
                if (!new File(entry).exists()) {
                    throw new IllegalArgumentException("Class path entry does not exist: " + entry);
                }
            }
            Options.v().set_process_dir(inputEntries);
        }

        List<String> completeClassPath = new ArrayList<>(inputEntries);
        completeClassPath.addAll(splitClassPath(JVM_CLASS_PATH));
        if (!completeClassPath.isEmpty()) {
            Options.v().set_soot_classpath(String.join(File.pathSeparator, completeClassPath));
        }
    }

    private List<SootMethod> resolveEntryPoints(SootClass appClass, Collection<String> signatures) {
        List<SootMethod> result = new ArrayList<>();
        if (signatures != null) {
            for (String signature : signatures) {
                if (signature != null && !signature.trim().isEmpty()) {
                    result.add(Scene.v().getMethod(signature.trim()));
                }
            }
        }
        if (!result.isEmpty()) {
            return result;
        }

        for (SootMethod method : appClass.getMethods()) {
            if (method.isMain()) {
                Scene.v().setMainClass(appClass);
                return Collections.singletonList(method);
            }
        }
        for (SootMethod method : appClass.getMethods()) {
            if (method.isConcrete()) {
                result.add(method);
            }
        }
        return result;
    }

    private void configureCallGraphAlgorithm() {
        String algorithm = callGraphAlgorithm == null
                ? "CHA"
                : callGraphAlgorithm.trim().toUpperCase(Locale.ROOT);
        switch (algorithm) {
            case "CHA":
                Options.v().setPhaseOption("cg.cha", "on");
                break;
            case "SPARK":
                Options.v().setPhaseOption("cg.spark", "on");
                Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
                break;
            case "VTA":
                Options.v().setPhaseOption("cg.spark", "on");
                Options.v().setPhaseOption("cg.spark", "vta:true");
                break;
            case "RTA":
                Options.v().setPhaseOption("cg.spark", "on");
                Options.v().setPhaseOption("cg.spark", "rta:true");
                Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown call graph algorithm '" + callGraphAlgorithm
                                + "'. Supported values: CHA, Spark, VTA, RTA");
        }
    }

    private static List<String> splitClassPath(String classPath) {
        if (classPath == null || classPath.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> entries = new ArrayList<>();
        for (String item : classPath.split(Pattern.quote(File.pathSeparator))) {
            String value = item.trim();
            if (!value.isEmpty() && !entries.contains(value)) {
                entries.add(new File(value).getAbsolutePath());
            }
        }
        return entries;
    }
}
