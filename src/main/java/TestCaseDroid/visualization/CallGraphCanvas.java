package TestCaseDroid.visualization;

import TestCaseDroid.analysis.report.GraphView;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Zoomable Swing canvas with a deterministic depth-based graph layout.
 */
public final class CallGraphCanvas extends JComponent {
    private static final int NODE_WIDTH = 210;
    private static final int NODE_HEIGHT = 62;
    private static final int COLUMN_GAP = 105;
    private static final int ROW_GAP = 38;
    private static final int MARGIN = 55;

    private final Map<String, Rectangle> nodeBounds = new HashMap<>();
    private GraphView view;
    private String selectedNodeId;
    private String searchText = "";
    private double zoom = 1.0;
    private Dimension logicalSize = new Dimension(800, 600);
    private Consumer<String> selectionListener;
    private Point dragStart;
    private Point viewportStart;
    private boolean dragged;

    public CallGraphCanvas() {
        setOpaque(true);
        setBackground(new Color(247, 249, 252));
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setToolTipText("");
        installMouseInteraction();
    }

    public void setGraphView(GraphView view) {
        this.view = view;
        selectedNodeId = null;
        rebuildLayout();
        revalidate();
        repaint();
    }

    public GraphView getGraphView() {
        return view;
    }

    public void setSelectionListener(Consumer<String> selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void setSelectedNode(String nodeId) {
        if (view == null || view.findNode(nodeId) == null) {
            return;
        }
        selectedNodeId = nodeId;
        Rectangle bounds = nodeBounds.get(nodeId);
        if (bounds != null) {
            Rectangle scaled = new Rectangle(
                    (int) (bounds.x * zoom), (int) (bounds.y * zoom),
                    (int) (bounds.width * zoom), (int) (bounds.height * zoom));
            scrollRectToVisible(scaled);
        }
        repaint();
        if (selectionListener != null) {
            selectionListener.accept(nodeId);
        }
    }

    public String getSelectedNodeId() {
        return selectedNodeId;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText == null
                ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        repaint();
    }

    public void resetView() {
        zoom = 1.0;
        updatePreferredSize();
        revalidate();
        repaint();
        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(
                JViewport.class, this);
        if (viewport != null) {
            viewport.setViewPosition(new Point(0, 0));
        }
    }

    public void writePng(Path output) throws IOException {
        double exportScale = Math.min(1.0,
                Math.min(12000.0 / logicalSize.width, 12000.0 / logicalSize.height));
        int width = Math.max(1, (int) Math.ceil(logicalSize.width * exportScale));
        int height = Math.max(1, (int) Math.ceil(logicalSize.height * exportScale));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(getBackground());
            graphics.fillRect(0, 0, width, height);
            graphics.scale(exportScale, exportScale);
            paintGraph(graphics);
        } finally {
            graphics.dispose();
        }
        ImageIO.write(image, "png", output.toFile());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                (int) Math.ceil(logicalSize.width * zoom),
                (int) Math.ceil(logicalSize.height * zoom));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.scale(zoom, zoom);
            paintGraph(g2);
        } finally {
            g2.dispose();
        }
    }

    private void paintGraph(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (view == null || view.getNodes().isEmpty()) {
            graphics.setColor(new Color(91, 103, 120));
            graphics.setFont(getFont().deriveFont(Font.PLAIN, 16f));
            graphics.drawString("No call-graph nodes in this view.", MARGIN, MARGIN);
            return;
        }

        for (GraphView.Edge edge : view.getEdges()) {
            paintEdge(graphics, edge);
        }
        for (GraphView.Node node : view.getNodes()) {
            paintNode(graphics, node);
        }
    }

    private void paintEdge(Graphics2D graphics, GraphView.Edge edge) {
        Rectangle source = nodeBounds.get(edge.getSource());
        Rectangle target = nodeBounds.get(edge.getTarget());
        if (source == null || target == null) {
            return;
        }
        int startX = source.x + source.width;
        int startY = source.y + source.height / 2;
        int endX = target.x;
        int endY = target.y + target.height / 2;
        boolean selected = edge.getSource().equals(selectedNodeId)
                || edge.getTarget().equals(selectedNodeId);
        graphics.setColor(selected ? new Color(35, 108, 178) : new Color(155, 166, 181));
        graphics.setStroke(new BasicStroke(selected ? 2.1f : 1.25f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (endX >= startX + 20) {
            double control = Math.max(45, (endX - startX) * 0.45);
            CubicCurve2D curve = new CubicCurve2D.Double(
                    startX, startY,
                    startX + control, startY,
                    endX - control, endY,
                    endX, endY);
            graphics.draw(curve);
        } else {
            int bend = Math.max(source.y + source.height, target.y + target.height) + 35;
            CubicCurve2D curve = new CubicCurve2D.Double(
                    startX, startY,
                    startX + 50, bend,
                    endX - 50, bend,
                    endX, endY);
            graphics.draw(curve);
        }
        paintArrowHead(graphics, endX, endY);
    }

    private void paintArrowHead(Graphics2D graphics, int x, int y) {
        Polygon arrow = new Polygon();
        arrow.addPoint(x, y);
        arrow.addPoint(x - 9, y - 5);
        arrow.addPoint(x - 9, y + 5);
        graphics.fill(arrow);
    }

    private void paintNode(Graphics2D graphics, GraphView.Node node) {
        Rectangle bounds = nodeBounds.get(node.getId());
        if (bounds == null) {
            return;
        }
        boolean selected = node.getId().equals(selectedNodeId);
        boolean matches = !searchText.isEmpty()
                && (node.getLabel().toLowerCase(Locale.ROOT).contains(searchText)
                || node.getId().toLowerCase(Locale.ROOT).contains(searchText));
        Color fill;
        Color border;
        if ("call-site".equals(node.getKind())) {
            fill = new Color(238, 244, 255);
            border = new Color(93, 128, 180);
        } else if (node.isRecursive()) {
            fill = new Color(255, 239, 236);
            border = new Color(192, 74, 58);
        } else if (node.isLibrary()) {
            fill = new Color(239, 242, 246);
            border = new Color(128, 139, 154);
        } else {
            fill = new Color(231, 246, 241);
            border = new Color(45, 126, 101);
        }
        if (matches) {
            fill = new Color(255, 246, 190);
            border = new Color(182, 130, 20);
        }
        if (selected) {
            border = new Color(25, 91, 166);
        }

        RoundRectangle2D shape = new RoundRectangle2D.Double(
                bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
        graphics.setColor(new Color(30, 42, 56, 24));
        graphics.fill(new RoundRectangle2D.Double(
                bounds.x + 3, bounds.y + 4, bounds.width, bounds.height, 14, 14));
        graphics.setColor(fill);
        graphics.fill(shape);
        graphics.setColor(border);
        graphics.setStroke(new BasicStroke(selected ? 3f : 1.6f));
        graphics.draw(shape);

        graphics.setClip(bounds.x + 9, bounds.y + 5,
                bounds.width - 18, bounds.height - 10);
        graphics.setColor(new Color(31, 41, 55));
        graphics.setFont(getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics titleMetrics = graphics.getFontMetrics();
        graphics.drawString(ellipsize(node.getLabel(), titleMetrics, bounds.width - 18),
                bounds.x + 10, bounds.y + 23);
        graphics.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        graphics.setColor(new Color(82, 94, 111));
        String meta = node.getKind() + "  depth " + node.getDepth()
                + "  in " + node.getFanIn() + " / out " + node.getFanOut();
        graphics.drawString(ellipsize(meta, graphics.getFontMetrics(), bounds.width - 18),
                bounds.x + 10, bounds.y + 45);
        graphics.setClip(null);
    }

    private static String ellipsize(String value, FontMetrics metrics, int maxWidth) {
        if (metrics.stringWidth(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        int length = value.length();
        while (length > 1
                && metrics.stringWidth(value.substring(0, length) + ellipsis) > maxWidth) {
            length--;
        }
        return value.substring(0, length) + ellipsis;
    }

    private void rebuildLayout() {
        nodeBounds.clear();
        if (view == null || view.getNodes().isEmpty()) {
            logicalSize = new Dimension(800, 600);
            updatePreferredSize();
            return;
        }
        Map<Integer, List<GraphView.Node>> layers = new TreeMap<>();
        for (GraphView.Node node : view.getNodes()) {
            layers.computeIfAbsent(node.getDepth(), ignored -> new ArrayList<>()).add(node);
        }
        int maxRows = 1;
        int column = 0;
        for (List<GraphView.Node> nodes : layers.values()) {
            maxRows = Math.max(maxRows, nodes.size());
            int x = MARGIN + column * (NODE_WIDTH + COLUMN_GAP);
            int y = MARGIN;
            for (GraphView.Node node : nodes) {
                nodeBounds.put(node.getId(), new Rectangle(
                        x, y, NODE_WIDTH, NODE_HEIGHT));
                y += NODE_HEIGHT + ROW_GAP;
            }
            column++;
        }
        logicalSize = new Dimension(
                Math.max(800, MARGIN * 2 + column * NODE_WIDTH
                        + Math.max(0, column - 1) * COLUMN_GAP),
                Math.max(600, MARGIN * 2 + maxRows * NODE_HEIGHT
                        + Math.max(0, maxRows - 1) * ROW_GAP));
        updatePreferredSize();
    }

    private void updatePreferredSize() {
        setPreferredSize(getPreferredSize());
    }

    private String nodeAt(Point point) {
        int logicalX = (int) (point.x / zoom);
        int logicalY = (int) (point.y / zoom);
        for (Map.Entry<String, Rectangle> entry : nodeBounds.entrySet()) {
            if (entry.getValue().contains(logicalX, logicalY)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        String nodeId = nodeAt(event.getPoint());
        if (nodeId == null || view == null) {
            return null;
        }
        GraphView.Node node = view.findNode(nodeId);
        return node == null ? null : "<html><b>" + html(node.getLabel())
                + "</b><br>" + html(node.getId()) + "</html>";
    }

    private void installMouseInteraction() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                dragStart = event.getPoint();
                dragged = false;
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(
                        JViewport.class, CallGraphCanvas.this);
                viewportStart = viewport == null ? null : viewport.getViewPosition();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (dragStart == null || viewportStart == null) {
                    return;
                }
                int dx = dragStart.x - event.getX();
                int dy = dragStart.y - event.getY();
                if (Math.abs(dx) + Math.abs(dy) > 4) {
                    dragged = true;
                }
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(
                        JViewport.class, CallGraphCanvas.this);
                if (viewport != null) {
                    int maxX = Math.max(0, getWidth() - viewport.getWidth());
                    int maxY = Math.max(0, getHeight() - viewport.getHeight());
                    viewport.setViewPosition(new Point(
                            Math.max(0, Math.min(maxX, viewportStart.x + dx)),
                            Math.max(0, Math.min(maxY, viewportStart.y + dy))));
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (!dragged) {
                    String nodeId = nodeAt(event.getPoint());
                    if (nodeId != null) {
                        setSelectedNode(nodeId);
                    }
                }
                dragStart = null;
                viewportStart = null;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                if (!event.isControlDown()) {
                    return;
                }
                event.consume();
                double factor = event.getWheelRotation() < 0 ? 1.12 : 1.0 / 1.12;
                zoom = Math.max(0.4, Math.min(2.5, zoom * factor));
                updatePreferredSize();
                revalidate();
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
    }

    private static String html(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
