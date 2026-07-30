package TestCaseDroid.visualization;

import TestCaseDroid.analysis.report.CallGraphDetailLevel;
import TestCaseDroid.analysis.report.CallGraphGranularity;
import TestCaseDroid.analysis.report.CallGraphReportExporter;
import TestCaseDroid.analysis.report.CallGraphSnapshot;
import TestCaseDroid.analysis.report.GraphView;
import TestCaseDroid.analysis.report.GraphViewBuilder;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Complete three-pane Swing call-graph explorer.
 */
public final class CallGraphViewerPanel extends JPanel {
    private final CallGraphSnapshot snapshot;
    private final CallGraphCanvas canvas = new CallGraphCanvas();
    private final JComboBox<CallGraphGranularity> granularity =
            new JComboBox<>(CallGraphGranularity.values());
    private final JComboBox<CallGraphDetailLevel> detailLevel =
            new JComboBox<>(CallGraphDetailLevel.values());
    private final JSpinner maxDepth;
    private final JCheckBox libraries = new JCheckBox("Show library methods");
    private final JCheckBox constructors = new JCheckBox("Show constructors");
    private final JTextField search = new JTextField();
    private final DefaultListModel<String> nodeModel = new DefaultListModel<>();
    private final JList<String> nodeList = new JList<>(nodeModel);
    private final JTextArea details = textArea();
    private final JTextArea aiContext = textArea();
    private final JLabel status = new JLabel(" ");
    private GraphView currentView;

    public CallGraphViewerPanel(CallGraphSnapshot snapshot) {
        super(new BorderLayout());
        this.snapshot = snapshot;
        this.maxDepth = new JSpinner(new SpinnerNumberModel(
                snapshot.getMetadata().getMaxDepth(), 0,
                Math.max(1, snapshot.getMetadata().getMaxDepth()), 1));
        setBackground(new Color(244, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildWorkspace(), BorderLayout.CENTER);
        configureInteraction();
        rebuildView();
    }

    public CallGraphCanvas getCanvas() {
        return canvas;
    }

    public GraphView getCurrentView() {
        return currentView;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 4));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(3, 4, 9, 4));
        JLabel title = new JLabel("TestCaseDroid Call Graph Explorer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel entry = new JLabel(snapshot.getMetadata().getEntryMethod());
        entry.setForeground(new Color(80, 91, 107));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(Box.createVerticalStrut(3));
        titles.add(entry);
        header.add(titles, BorderLayout.CENTER);
        status.setHorizontalAlignment(SwingConstants.RIGHT);
        status.setForeground(new Color(72, 84, 100));
        header.add(status, BorderLayout.EAST);
        return header;
    }

    private JComponent buildWorkspace() {
        JScrollPane graphScroll = new JScrollPane(canvas);
        graphScroll.setBorder(BorderFactory.createLineBorder(new Color(211, 219, 229)));
        graphScroll.getVerticalScrollBar().setUnitIncrement(24);
        graphScroll.getHorizontalScrollBar().setUnitIncrement(24);

        JSplitPane graphAndDetails = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, graphScroll, buildDetailsPanel());
        graphAndDetails.setResizeWeight(0.78);
        graphAndDetails.setDividerLocation(820);
        graphAndDetails.setContinuousLayout(true);

        JSplitPane workspace = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, buildControlPanel(), graphAndDetails);
        workspace.setResizeWeight(0.0);
        workspace.setDividerLocation(245);
        workspace.setContinuousLayout(true);
        return workspace;
    }

    private JComponent buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(251, 252, 254));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(211, 219, 229)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(235, 700));

        granularity.setSelectedItem(CallGraphGranularity.METHOD);
        detailLevel.setSelectedItem(CallGraphDetailLevel.STANDARD);
        libraries.setSelected(false);
        constructors.setSelected(false);
        libraries.setEnabled(snapshot.getMetadata().isIncludesLibraries());
        constructors.setEnabled(snapshot.getMetadata().isIncludesConstructors());
        if (!libraries.isEnabled()) {
            libraries.setToolTipText("Re-run with --includeLibraries to make library nodes available.");
        }
        if (!constructors.isEnabled()) {
            constructors.setToolTipText("Re-run with --includeConstructors to make constructors available.");
        }

        panel.add(sectionLabel("VIEW"));
        panel.add(labeled("Granularity", granularity));
        panel.add(Box.createVerticalStrut(7));
        panel.add(labeled("AI detail level", detailLevel));
        panel.add(Box.createVerticalStrut(7));
        panel.add(labeled("Maximum depth", maxDepth));
        panel.add(Box.createVerticalStrut(6));
        panel.add(libraries);
        panel.add(constructors);
        panel.add(Box.createVerticalStrut(12));
        panel.add(sectionLabel("FIND NODE"));
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(search);
        panel.add(Box.createVerticalStrut(8));

        nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nodeList.setCellRenderer(new NodeRenderer());
        JScrollPane nodeScroll = new JScrollPane(nodeList);
        nodeScroll.setAlignmentX(LEFT_ALIGNMENT);
        nodeScroll.setPreferredSize(new Dimension(210, 310));
        nodeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.add(nodeScroll);
        panel.add(Box.createVerticalStrut(8));

        JButton reset = new JButton("Reset view");
        reset.setAlignmentX(LEFT_ALIGNMENT);
        reset.addActionListener(event -> canvas.resetView());
        panel.add(reset);
        panel.add(Box.createVerticalStrut(12));
        panel.add(sectionLabel("EXPORT"));

        JButton png = new JButton("Save graph PNG...");
        png.setAlignmentX(LEFT_ALIGNMENT);
        png.addActionListener(event -> savePng());
        panel.add(png);
        panel.add(Box.createVerticalStrut(5));
        JButton report = new JButton("Export selected report...");
        report.setAlignmentX(LEFT_ALIGNMENT);
        report.addActionListener(event -> exportSelectedReport());
        panel.add(report);
        panel.add(Box.createVerticalStrut(5));
        JButton bundle = new JButton("Export AI bundle...");
        bundle.setAlignmentX(LEFT_ALIGNMENT);
        bundle.addActionListener(event -> exportAiBundle());
        panel.add(bundle);
        return panel;
    }

    private JComponent buildDetailsPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(355, 700));
        tabs.addTab("Node details", new JScrollPane(details));

        JPanel aiPanel = new JPanel(new BorderLayout(4, 4));
        aiPanel.add(new JScrollPane(aiContext), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        JButton copy = new JButton("Copy AI context");
        copy.addActionListener(event -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(aiContext.getText()), null);
        });
        actions.add(copy);
        aiPanel.add(actions, BorderLayout.SOUTH);
        tabs.addTab("AI context", aiPanel);
        return tabs;
    }

    private void configureInteraction() {
        granularity.addActionListener(event -> rebuildView());
        detailLevel.addActionListener(event -> refreshSelection());
        maxDepth.addChangeListener(event -> rebuildView());
        libraries.addActionListener(event -> rebuildView());
        constructors.addActionListener(event -> rebuildView());
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateSearch();
            }
        });
        nodeList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && nodeList.getSelectedValue() != null) {
                canvas.setSelectedNode(nodeList.getSelectedValue());
            }
        });
        canvas.setSelectionListener(nodeId -> {
            nodeList.setSelectedValue(nodeId, true);
            refreshSelection();
        });
    }

    private void rebuildView() {
        if (granularity.getSelectedItem() == null) {
            return;
        }
        currentView = GraphViewBuilder.build(snapshot,
                (CallGraphGranularity) granularity.getSelectedItem(),
                (Integer) maxDepth.getValue(),
                libraries.isSelected(), constructors.isSelected());
        canvas.setGraphView(currentView);
        updateSearch();
        status.setText(currentView.getNodes().size() + " nodes  |  "
                + currentView.getEdges().size() + " edges"
                + (snapshot.isTruncated() ? "  |  bounded snapshot" : ""));
        if (!currentView.getNodes().isEmpty()) {
            canvas.setSelectedNode(currentView.getNodes().get(0).getId());
        } else {
            details.setText("No nodes match the current filters.");
            aiContext.setText("No nodes match the current filters.");
        }
    }

    private void updateSearch() {
        if (currentView == null) {
            return;
        }
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        canvas.setSearchText(query);
        nodeModel.clear();
        for (GraphView.Node node : currentView.getNodes()) {
            if (query.isEmpty()
                    || node.getLabel().toLowerCase(Locale.ROOT).contains(query)
                    || node.getId().toLowerCase(Locale.ROOT).contains(query)) {
                nodeModel.addElement(node.getId());
            }
        }
    }

    private void refreshSelection() {
        if (currentView == null || canvas.getSelectedNodeId() == null) {
            return;
        }
        GraphView.Node node = currentView.findNode(canvas.getSelectedNodeId());
        if (node == null) {
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(node.getLabel()).append("\n");
        text.append(repeat('-', Math.min(72, Math.max(16, node.getLabel().length()))))
                .append("\n\n");
        text.append("ID: ").append(node.getId()).append("\n");
        text.append("Kind: ").append(node.getKind()).append("\n");
        text.append("Group: ").append(node.getGroup()).append("\n");
        text.append("Depth: ").append(node.getDepth()).append("\n");
        text.append("Fan-in: ").append(node.getFanIn()).append("\n");
        text.append("Fan-out: ").append(node.getFanOut()).append("\n");
        text.append("Application: ").append(node.isApplication()).append("\n");
        text.append("Library: ").append(node.isLibrary()).append("\n");
        text.append("Recursive: ").append(node.isRecursive()).append("\n\n");
        text.append("Evidence\n");
        text.append("--------\n");
        for (Map.Entry<String, Object> entry : node.getDetails().entrySet()) {
            text.append(entry.getKey()).append(": ")
                    .append(String.valueOf(entry.getValue())).append("\n");
        }
        details.setText(text.toString());
        details.setCaretPosition(0);

        CallGraphDetailLevel selectedDetail =
                (CallGraphDetailLevel) detailLevel.getSelectedItem();
        aiContext.setText(CallGraphReportExporter.nodeContext(
                currentView, node.getId(), selectedDetail));
        aiContext.setCaretPosition(0);
    }

    private void savePng() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("callgraph.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path output = ensureExtension(chooser.getSelectedFile().toPath(), ".png");
        try {
            canvas.writePng(output);
            showSuccess("PNG saved to:\n" + output.toAbsolutePath());
        } catch (IOException exception) {
            showError("Could not save PNG", exception);
        }
    }

    private void exportSelectedReport() {
        Path directory = chooseDirectory();
        if (directory == null) {
            return;
        }
        try {
            CallGraphReportExporter.ExportResult result =
                    CallGraphReportExporter.writeReport(directory, snapshot,
                            (CallGraphGranularity) granularity.getSelectedItem(),
                            (CallGraphDetailLevel) detailLevel.getSelectedItem());
            showSuccess("Report saved:\n" + result.getJsonPath().toAbsolutePath()
                    + "\n" + result.getMarkdownPath().toAbsolutePath());
        } catch (IOException exception) {
            showError("Could not export report", exception);
        }
    }

    private void exportAiBundle() {
        Path directory = chooseDirectory();
        if (directory == null) {
            return;
        }
        try {
            CallGraphReportExporter.writeAiBundle(directory, snapshot);
            showSuccess("AI report bundle saved to:\n" + directory.toAbsolutePath());
        } catch (IOException exception) {
            showError("Could not export AI bundle", exception);
        }
    }

    private Path chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select report output directory");
        return chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION
                ? chooser.getSelectedFile().toPath() : null;
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message,
                "TestCaseDroid", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(this,
                message + ":\n" + exception.getMessage(),
                "TestCaseDroid", JOptionPane.ERROR_MESSAGE);
    }

    private JComponent labeled(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        JLabel text = new JLabel(label);
        text.setForeground(new Color(75, 87, 103));
        panel.add(text, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JLabel sectionLabel(String value) {
        JLabel label = new JLabel(value);
        label.setForeground(new Color(75, 87, 103));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static JTextArea textArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        return area;
    }

    private static Path ensureExtension(Path path, String extension) {
        return path.toString().toLowerCase(Locale.ROOT).endsWith(extension)
                ? path : path.resolveSibling(path.getFileName() + extension);
    }

    private static String repeat(char value, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            result.append(value);
        }
        return result.toString();
    }

    private final class NodeRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean selected,
                                                      boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, selected, focus);
            GraphView.Node node = currentView == null
                    ? null : currentView.findNode(String.valueOf(value));
            if (node != null) {
                label.setText(node.getLabel());
                label.setToolTipText(node.getId());
                if (node.isRecursive() && !selected) {
                    label.setForeground(new Color(165, 60, 48));
                }
            }
            return label;
        }
    }
}
