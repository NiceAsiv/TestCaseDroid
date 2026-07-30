package TestCaseDroid.visualization;

import TestCaseDroid.analysis.report.CallGraphSnapshot;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;

/**
 * Desktop launcher for the Swing call-graph explorer.
 */
public final class CallGraphVisualizer {
    private CallGraphVisualizer() {
    }

    public static void open(CallGraphSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            JFrame frame = new JFrame("TestCaseDroid Call Graph Explorer");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new CallGraphViewerPanel(snapshot));
            frame.setMinimumSize(new Dimension(1050, 680));
            frame.setSize(1480, 900);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The cross-platform Swing look and feel remains a safe fallback.
        }
    }
}
