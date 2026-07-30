package TestCaseDroid.visualization;

import TestCaseDroid.TestCaseDroidApplication;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dedicated entry point for the GUI artifacts.
 *
 * <p>With no arguments it opens a configuration window. When arguments are
 * supplied, they use the normal CLI syntax and visualization is enabled
 * automatically. This keeps the GUI artifact convenient for both double-click
 * users and scripted launches.</p>
 */
public final class CallGraphGuiApplication {
    private CallGraphGuiApplication() {
    }

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            TestCaseDroidApplication.main(withVisualization(args));
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Error: The GUI edition requires a desktop display. "
                    + "Use the CLI edition for headless environments.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            new CallGraphLaunchFrame().setVisible(true);
        });
    }

    static String[] withVisualization(String[] args) {
        List<String> values = new ArrayList<>(Arrays.asList(args));
        if (!values.contains("--visualize") && !values.contains("-viz")
                && !values.contains("--help") && !values.contains("-h")) {
            values.add("--visualize");
        }
        return values.toArray(new String[0]);
    }

    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing's cross-platform look and feel remains available.
        }
    }
}
