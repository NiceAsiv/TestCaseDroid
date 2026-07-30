package TestCaseDroid.analysis.report;

import TestCaseDroid.visualization.CallGraphViewerPanel;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Renders the real Swing viewer into the README screenshot without opening a
 * window. Compile the project first, then run:
 *
 * javac -cp target/classes -d target/readme-tools tools/VisualizerScreenshotRenderer.java
 * java -Djava.awt.headless=true -cp "target/classes;target/readme-tools" TestCaseDroid.analysis.report.VisualizerScreenshotRenderer
 *
 * On Linux/macOS replace the classpath semicolon with a colon.
 */
public final class VisualizerScreenshotRenderer {
    private VisualizerScreenshotRenderer() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                render();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private static void render() throws Exception {
        CallGraphViewerPanel panel = new CallGraphViewerPanel(snapshot());
        panel.setSize(1480, 900);
        layout(panel);

        BufferedImage image = new BufferedImage(1480, 900, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        panel.printAll(graphics);
        graphics.dispose();

        File output = new File("README.assets/callgraph-explorer.png");
        ImageIO.write(image, "png", output);
        System.out.println("Rendered " + output.getAbsolutePath());
    }

    private static void layout(Component component) {
        if (component instanceof Container) {
            Container container = (Container) component;
            container.doLayout();
            for (Component child : container.getComponents()) {
                layout(child);
            }
        }
    }

    private static CallGraphSnapshot snapshot() {
        String owner = "demo.checkout.OrderService";
        List<CallGraphSnapshot.MethodNode> nodes = new ArrayList<>();
        nodes.add(node(owner, "checkout", 0, false));
        nodes.add(node(owner, "validate", 1, false));
        nodes.add(node(owner, "reserveInventory", 1, false));
        nodes.add(node(owner, "charge", 1, false));
        nodes.add(node(owner, "audit", 2, false));
        CallGraphSnapshot.MethodNode retry = node(owner, "retryPayment", 2, true);
        retry.setRecursive(true);
        nodes.add(retry);

        List<CallGraphSnapshot.CallEdge> edges = Arrays.asList(
                edge("checkout-validate", nodes.get(0), nodes.get(1), 42, "validate(order)"),
                edge("checkout-reserve", nodes.get(0), nodes.get(2), 45, "reserveInventory(order)"),
                edge("checkout-charge", nodes.get(0), nodes.get(3), 48, "charge(order)"),
                edge("validate-audit", nodes.get(1), nodes.get(4), 71, "audit(order)"),
                edge("charge-retry", nodes.get(3), nodes.get(5), 109, "retryPayment(order)"),
                edge("retry-retry", nodes.get(5), nodes.get(5), 128, "retryPayment(order)"));

        return new CallGraphSnapshot(
                new CallGraphSnapshot.Metadata(
                        owner,
                        nodes.get(0).getId(),
                        "CHA",
                        "target/classes",
                        12,
                        2000,
                        true,
                        true),
                nodes,
                edges,
                Collections.singletonList(Collections.singletonList(retry.getId())),
                Collections.singletonList(
                        "<demo.checkout.LegacyPayment: void unused()>"),
                Collections.singletonList(
                        "Static call graphs may over-approximate runtime dispatch."),
                false);
    }

    private static CallGraphSnapshot.MethodNode node(
            String owner, String name, int depth, boolean recursive) {
        return new CallGraphSnapshot.MethodNode(
                "<" + owner + ": void " + name + "()>",
                "demo.checkout",
                owner,
                name,
                "void " + name + "()",
                "void",
                Collections.emptyList(),
                "public",
                true,
                false,
                false,
                true,
                depth,
                40 + depth * 30);
    }

    private static CallGraphSnapshot.CallEdge edge(
            String id,
            CallGraphSnapshot.MethodNode source,
            CallGraphSnapshot.MethodNode target,
            int line,
            String statement) {
        return new CallGraphSnapshot.CallEdge(
                id,
                source.getId(),
                target.getId(),
                "STATIC",
                statement,
                line,
                true,
                true);
    }
}
