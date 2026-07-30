import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Deterministically renders the README call-graph screenshots.
 *
 * Run from the repository root:
 *   javac -encoding UTF-8 -d target/readme-tools tools/ReadmeDiagramRenderer.java
 *   java -cp target/readme-tools ReadmeDiagramRenderer
 */
public final class ReadmeDiagramRenderer {
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 820;

    private static final Color BACKGROUND = color("#F5F7FB");
    private static final Color INK = color("#172033");
    private static final Color MUTED = color("#64748B");
    private static final Color LINE = color("#62708A");
    private static final Color BLUE = color("#2563EB");
    private static final Color BLUE_FILL = color("#E8F0FF");
    private static final Color GREEN = color("#0F9D76");
    private static final Color GREEN_FILL = color("#DFF7EF");
    private static final Color PURPLE = color("#7C3AED");
    private static final Color PURPLE_FILL = color("#F0E9FF");
    private static final Color ORANGE = color("#D97706");
    private static final Color ORANGE_FILL = color("#FFF2D8");
    private static final Color GRAY = color("#94A3B8");
    private static final Color GRAY_FILL = color("#EEF2F7");
    private static final Font TITLE = new Font("Segoe UI", Font.BOLD, 38);
    private static final Font SUBTITLE = new Font("Segoe UI", Font.PLAIN, 19);
    private static final Font NODE = new Font("Segoe UI", Font.BOLD, 21);
    private static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font BADGE = new Font("Segoe UI", Font.BOLD, 15);
    private static final File OUTPUT = new File("README.assets");

    public static void main(String[] args) throws Exception {
        if (!OUTPUT.isDirectory() && !OUTPUT.mkdirs()) {
            throw new IOException("Cannot create " + OUTPUT.getAbsolutePath());
        }
        renderDiamond();
        renderRecursion();
        renderOverload();
        renderInterfaceDispatch();
        renderCliScreenshot();
        System.out.println("Rendered README screenshots in " + OUTPUT.getAbsolutePath());
    }

    private static void renderDiamond() throws IOException {
        Canvas canvas = canvas(
                "Diamond call graph · 3 reachable paths",
                "Per-path visited state preserves both branches after they merge at shared().",
                "CHA · MULTI-PATH");

        Node entry = node(70, 335, 230, 82, "diamondEntry()", BLUE_FILL, BLUE);
        Node direct = node(390, 165, 190, 78, "direct()", PURPLE_FILL, PURPLE);
        Node left = node(390, 335, 190, 78, "left()", PURPLE_FILL, PURPLE);
        Node right = node(390, 505, 190, 78, "right()", PURPLE_FILL, PURPLE);
        Node shared = node(750, 420, 200, 82, "shared()", ORANGE_FILL, ORANGE);
        Node sink = node(1080, 300, 250, 92, "diamondSink()", GREEN_FILL, GREEN);

        canvas.edge(entry.right(), direct.left(), "path 1", false);
        canvas.edge(entry.right(), left.left(), "path 2", false);
        canvas.edge(entry.right(), right.left(), "path 3", false);
        canvas.edge(left.right(), shared.left(), null, false);
        canvas.edge(right.right(), shared.left(), null, false);
        canvas.edge(direct.right(), sink.left(), null, false);
        canvas.edge(shared.right(), sink.left(), "merge", false);
        canvas.nodes(entry, direct, left, right, shared, sink);
        canvas.footer("3 witnesses", "direct · left/shared · right/shared", GREEN);
        canvas.save("callgraph-diamond.png");
    }

    private static void renderRecursion() throws IOException {
        Canvas canvas = canvas(
                "Recursive call graph · bounded cycle",
                "Simple-path enumeration terminates at the back edge while retaining the exit to the sink.",
                "RECURSION · CYCLE");

        Node entry = node(65, 345, 235, 82, "recursiveEntry()", BLUE_FILL, BLUE);
        Node a = node(390, 345, 220, 82, "recursiveA(int)", PURPLE_FILL, PURPLE);
        Node b = node(730, 345, 220, 82, "recursiveB(int)", ORANGE_FILL, ORANGE);
        Node sink = node(1080, 345, 245, 82, "recursiveSink()", GREEN_FILL, GREEN);

        canvas.edge(entry.right(), a.left(), null, false);
        canvas.edge(a.right(), b.left(), "call", false);
        canvas.curvedEdge(b.top(), a.top(), 670, 205, "back edge", true);
        canvas.edge(b.right(), sink.left(), "exit branch", false);
        canvas.nodes(entry, a, b, sink);
        canvas.footer("Cycle-safe", "maxDepth + maxPaths bound the search", PURPLE);
        canvas.save("callgraph-recursion.png");
    }

    private static void renderOverload() throws IOException {
        Canvas canvas = canvas(
                "Overload-sensitive reachability",
                "The complete Soot signature selects overloaded(int); the String overload remains unreachable.",
                "SIGNATURE · OVERLOAD");

        Node entry = node(65, 230, 235, 82, "overloadEntry()", BLUE_FILL, BLUE);
        Node intCall = node(430, 220, 245, 92, "overloaded(int)", PURPLE_FILL, PURPLE);
        Node intSink = node(1015, 220, 290, 92, "intOverloadSink()", GREEN_FILL, GREEN);
        Node stringCall = node(430, 490, 275, 92, "overloaded(String)", GRAY_FILL, GRAY);
        Node stringSink = node(1015, 490, 290, 92, "stringOverloadSink()", GRAY_FILL, GRAY);

        canvas.edge(entry.right(), intCall.left(), "resolved", false);
        canvas.edge(intCall.right(), intSink.left(), "reachable", false);
        canvas.edge(stringCall.right(), stringSink.left(), "not selected", true);
        canvas.nodes(entry, intCall, intSink, stringCall, stringSink);
        canvas.label(72, 535, "No incoming edge from overloadEntry()", MUTED, SMALL);
        canvas.footer("Precise match", "<class: return method(parameter-types)>", BLUE);
        canvas.save("callgraph-overload.png");
    }

    private static void renderInterfaceDispatch() throws IOException {
        Canvas canvas = canvas(
                "Interface dispatch under CHA",
                "CHA conservatively connects the interface call to every compatible implementation.",
                "CHA · VIRTUAL DISPATCH");

        Node entry = node(55, 345, 220, 82, "interfaceEntry()", BLUE_FILL, BLUE);
        Node api = node(355, 335, 250, 102, "Service.execute()", ORANGE_FILL, ORANGE);
        Node primary = node(720, 195, 280, 92, "PrimaryService.execute()", PURPLE_FILL, PURPLE);
        Node secondary = node(720, 500, 300, 92, "SecondaryService.execute()", GRAY_FILL, GRAY);
        Node primarySink = node(1090, 195, 250, 92, "interfaceSink()", GREEN_FILL, GREEN);
        Node secondarySink = node(1090, 500, 250, 92, "secondarySink()", GRAY_FILL, GRAY);

        canvas.edge(entry.right(), api.left(), null, false);
        canvas.edge(api.right(), primary.left(), "possible target", true);
        canvas.edge(api.right(), secondary.left(), "possible target", true);
        canvas.edge(primary.right(), primarySink.left(), null, false);
        canvas.edge(secondary.right(), secondarySink.left(), null, false);
        canvas.nodes(entry, api, primary, secondary, primarySink, secondarySink);
        canvas.footer("Conservative result", "Spark / VTA / RTA may remove impossible targets", ORANGE);
        canvas.save("callgraph-interface-dispatch.png");
    }

    private static void renderCliScreenshot() throws IOException {
        BufferedImage image = new BufferedImage(1400, 920, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        configure(graphics);
        graphics.setColor(color("#F2F5FA"));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        graphics.setColor(INK);
        graphics.setFont(TITLE);
        graphics.drawString("End-to-end CLI result", 60, 68);
        graphics.setColor(MUTED);
        graphics.setFont(SUBTITLE);
        graphics.drawString("The packaged JAR reports all three diamond paths.", 60, 104);

        int x = 55;
        int y = 145;
        int w = 1290;
        int h = 710;
        graphics.setColor(color("#111827"));
        graphics.fill(new RoundRectangle2D.Double(x, y, w, h, 22, 22));
        graphics.setColor(color("#273244"));
        graphics.fill(new RoundRectangle2D.Double(x, y, w, 54, 22, 22));
        graphics.fillRect(x, y + 28, w, 26);
        graphics.setColor(color("#FF5F57"));
        graphics.fillOval(x + 24, y + 19, 16, 16);
        graphics.setColor(color("#FEBC2E"));
        graphics.fillOval(x + 50, y + 19, 16, 16);
        graphics.setColor(color("#28C840"));
        graphics.fillOval(x + 76, y + 19, 16, 16);
        graphics.setFont(new Font("Consolas", Font.PLAIN, 15));
        graphics.setColor(color("#A8B3C7"));
        graphics.drawString("TestCaseDroid — PowerShell", x + 515, y + 35);

        Font mono = new Font("Consolas", Font.PLAIN, 17);
        Font monoBold = new Font("Consolas", Font.BOLD, 17);
        int textX = x + 28;
        int lineY = y + 92;
        int step = 29;
        drawTerminalLine(graphics, textX, lineY, mono, color("#8BE9FD"),
                "> java -jar target/TestCaseDroid-1.4.0-cli-all.jar ...");
        lineY += step * 2;
        drawTerminalLine(graphics, textX, lineY, monoBold, color("#50FA7B"),
                "Found 3 paths from diamondEntry() to diamondSink()");
        lineY += step * 2;
        drawTerminalLine(graphics, textX, lineY, monoBold, color("#F8F8F2"), "Path 1:");
        lineY += step;
        drawTerminalLine(graphics, textX, lineY, mono, color("#CBD5E1"),
                "diamondEntry() -> direct() -> diamondSink()");
        lineY += step * 2;
        drawTerminalLine(graphics, textX, lineY, monoBold, color("#F8F8F2"), "Path 2:");
        lineY += step;
        drawTerminalLine(graphics, textX, lineY, mono, color("#CBD5E1"),
                "diamondEntry() -> left() -> shared() -> diamondSink()");
        lineY += step * 2;
        drawTerminalLine(graphics, textX, lineY, monoBold, color("#F8F8F2"), "Path 3:");
        lineY += step;
        drawTerminalLine(graphics, textX, lineY, mono, color("#CBD5E1"),
                "diamondEntry() -> right() -> shared() -> diamondSink()");
        lineY += step * 2;
        drawTerminalLine(graphics, textX, lineY, mono, color("#F1C40F"),
                "WARN  Graphviz not found; PNG conversion skipped. DOT files were generated.");
        lineY += step * 2;
        drawTerminalLine(graphics, textX, lineY, mono, color("#8BE9FD"),
                "> exit code: 0");

        graphics.dispose();
        ImageIO.write(image, "png", new File(OUTPUT, "cli-three-paths.png"));
    }

    private static void drawTerminalLine(Graphics2D graphics, int x, int y, Font font,
                                         Color color, String text) {
        graphics.setFont(font);
        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }

    private static Canvas canvas(String title, String subtitle, String badge) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        configure(graphics);
        graphics.setPaint(new GradientPaint(0, 0, color("#FFFFFF"), WIDTH, HEIGHT, BACKGROUND));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setColor(color("#DDE4F0"));
        graphics.drawLine(55, 133, WIDTH - 55, 133);
        graphics.setColor(INK);
        graphics.setFont(TITLE);
        graphics.drawString(title, 60, 67);
        graphics.setColor(MUTED);
        graphics.setFont(SUBTITLE);
        graphics.drawString(subtitle, 60, 105);
        drawBadge(graphics, WIDTH - 315, 48, 255, 36, badge);
        return new Canvas(image, graphics);
    }

    private static void drawBadge(Graphics2D graphics, int x, int y, int width, int height, String text) {
        graphics.setColor(color("#E9EEF8"));
        graphics.fill(new RoundRectangle2D.Double(x, y, width, height, height, height));
        graphics.setColor(color("#50617D"));
        graphics.setFont(BADGE);
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, x + (width - metrics.stringWidth(text)) / 2,
                y + (height + metrics.getAscent() - metrics.getDescent()) / 2);
    }

    private static Node node(int x, int y, int width, int height,
                             String label, Color fill, Color border) {
        return new Node(x, y, width, height, Arrays.asList(label), fill, border);
    }

    private static void configure(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    private static Color color(String hex) {
        return Color.decode(hex);
    }

    private static final class Canvas {
        private final BufferedImage image;
        private final Graphics2D graphics;

        private Canvas(BufferedImage image, Graphics2D graphics) {
            this.image = image;
            this.graphics = graphics;
        }

        private void nodes(Node... nodes) {
            for (Node node : nodes) {
                node.draw(graphics);
            }
        }

        private void edge(Point start, Point end, String label, boolean dashed) {
            graphics.setColor(dashed ? GRAY : LINE);
            graphics.setStroke(stroke(dashed));
            graphics.drawLine(start.x, start.y, end.x, end.y);
            arrow(graphics, start.x, start.y, end.x, end.y, dashed ? GRAY : LINE);
            if (label != null) {
                edgeLabel(graphics, (start.x + end.x) / 2, (start.y + end.y) / 2 - 9, label);
            }
        }

        private void curvedEdge(Point start, Point end, int controlX, int controlY,
                                String label, boolean dashed) {
            graphics.setColor(dashed ? GRAY : LINE);
            graphics.setStroke(stroke(dashed));
            Shape curve = new QuadCurve2D.Double(
                    start.x, start.y, controlX, controlY, end.x, end.y);
            graphics.draw(curve);
            arrow(graphics, controlX, controlY, end.x, end.y, dashed ? GRAY : LINE);
            if (label != null) {
                edgeLabel(graphics, controlX, controlY - 10, label);
            }
        }

        private void label(int x, int y, String text, Color color, Font font) {
            graphics.setFont(font);
            graphics.setColor(color);
            graphics.drawString(text, x, y);
        }

        private void footer(String heading, String text, Color accent) {
            int y = HEIGHT - 82;
            graphics.setColor(color("#E3E8F1"));
            graphics.drawLine(60, y - 28, WIDTH - 60, y - 28);
            graphics.setColor(accent);
            graphics.fillRoundRect(60, y - 2, 9, 30, 9, 9);
            graphics.setColor(INK);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 17));
            graphics.drawString(heading, 84, y + 18);
            graphics.setColor(MUTED);
            graphics.setFont(SMALL);
            graphics.drawString(text, 250, y + 18);
        }

        private void save(String name) throws IOException {
            graphics.dispose();
            ImageIO.write(image, "png", new File(OUTPUT, name));
        }
    }

    private static final class Node {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final List<String> lines;
        private final Color fill;
        private final Color border;

        private Node(int x, int y, int width, int height, List<String> lines,
                     Color fill, Color border) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.lines = lines;
            this.fill = fill;
            this.border = border;
        }

        private void draw(Graphics2D graphics) {
            graphics.setColor(new Color(24, 39, 75, 18));
            graphics.fill(new RoundRectangle2D.Double(x + 4, y + 6, width, height, 22, 22));
            graphics.setColor(fill);
            graphics.fill(new RoundRectangle2D.Double(x, y, width, height, 22, 22));
            graphics.setColor(border);
            graphics.setStroke(new BasicStroke(2.4f));
            graphics.draw(new RoundRectangle2D.Double(x, y, width, height, 22, 22));
            graphics.setColor(INK);
            graphics.setFont(NODE);
            FontMetrics metrics = graphics.getFontMetrics();
            int totalHeight = lines.size() * metrics.getHeight();
            int baseline = y + (height - totalHeight) / 2 + metrics.getAscent();
            for (String line : lines) {
                graphics.drawString(line, x + (width - metrics.stringWidth(line)) / 2, baseline);
                baseline += metrics.getHeight();
            }
        }

        private Point left() {
            return new Point(x, y + height / 2);
        }

        private Point right() {
            return new Point(x + width, y + height / 2);
        }

        private Point top() {
            return new Point(x + width / 2, y);
        }
    }

    private static final class Point {
        private final int x;
        private final int y;

        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static BasicStroke stroke(boolean dashed) {
        return dashed
                ? new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10.0f, new float[]{9.0f, 8.0f}, 0.0f)
                : new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static void edgeLabel(Graphics2D graphics, int centerX, int centerY, String text) {
        graphics.setFont(SMALL);
        FontMetrics metrics = graphics.getFontMetrics();
        int width = metrics.stringWidth(text) + 18;
        int height = 25;
        graphics.setColor(new Color(255, 255, 255, 235));
        graphics.fillRoundRect(centerX - width / 2, centerY - height / 2, width, height, 12, 12);
        graphics.setColor(MUTED);
        graphics.drawString(text, centerX - metrics.stringWidth(text) / 2,
                centerY + (metrics.getAscent() - metrics.getDescent()) / 2);
    }

    private static void arrow(Graphics2D graphics, int fromX, int fromY,
                              int toX, int toY, Color color) {
        double angle = Math.atan2(toY - fromY, toX - fromX);
        int size = 13;
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(toX, toY);
        arrow.lineTo(toX - size * Math.cos(angle - Math.PI / 6),
                toY - size * Math.sin(angle - Math.PI / 6));
        arrow.lineTo(toX - size * Math.cos(angle + Math.PI / 6),
                toY - size * Math.sin(angle + Math.PI / 6));
        arrow.closePath();
        graphics.setColor(color);
        graphics.fill(arrow);
    }
}
