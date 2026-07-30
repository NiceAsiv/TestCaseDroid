package TestCaseDroid.visualization;

import TestCaseDroid.analysis.info.SignatureSearch;
import TestCaseDroid.analysis.report.CallGraphAnalysisOptions;
import TestCaseDroid.analysis.report.CallGraphAnalyzer;
import TestCaseDroid.analysis.report.CallGraphSnapshot;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Standalone configuration window used by the GUI edition.
 */
final class CallGraphLaunchFrame extends JFrame {
    private final JTextField classPath = new JTextField("target/classes");
    private final JTextField entryClass = new JTextField(
            "TestCaseDroid.test.callgraph.CallGraphExamples");
    private final JTextField entryMethod = new JTextField(
            "TestCaseDroid.test.callgraph.CallGraphExamples#diamondEntry()");
    private final JComboBox<String> algorithm =
            new JComboBox<>(new String[]{"CHA", "Spark", "VTA", "RTA"});
    private final JSpinner maxDepth =
            new JSpinner(new SpinnerNumberModel(12, 0, 1000, 1));
    private final JSpinner maxNodes =
            new JSpinner(new SpinnerNumberModel(2000, 1, 100000, 100));
    private final JCheckBox libraries = new JCheckBox("Include library methods");
    private final JCheckBox constructors = new JCheckBox(
            "Include constructors and static initializers");
    private final JButton analyze = new JButton("Analyze and open graph");
    private final JLabel status = new JLabel("Choose compiled classes or a JAR to begin.");
    private final JProgressBar progress = new JProgressBar();

    CallGraphLaunchFrame() {
        super("TestCaseDroid GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 510));
        setSize(780, 560);
        setLocationByPlatform(true);
        setContentPane(buildContent());
        analyze.addActionListener(event -> analyze());
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.setBackground(new Color(247, 249, 252));

        JPanel heading = new JPanel(new BorderLayout(4, 4));
        heading.setOpaque(false);
        JLabel title = new JLabel("Open a Java call graph");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel(
                "Configure the Soot analysis scope; the graph opens in a separate explorer window.");
        subtitle.setForeground(new Color(79, 91, 108));
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 5, 6, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;

        addRow(form, constraints, 0, "Classes/JAR path", pathChooser());
        addRow(form, constraints, 1, "Entry class", entryClass);
        addRow(form, constraints, 2, "Entry method", entryMethod);
        addRow(form, constraints, 3, "Call-graph algorithm", algorithm);
        addRow(form, constraints, 4, "Maximum depth", maxDepth);
        addRow(form, constraints, 5, "Maximum method nodes", maxNodes);

        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.weightx = 1;
        form.add(libraries, constraints);
        constraints.gridy = 7;
        form.add(constructors, constraints);

        JLabel methodHint = new JLabel(
                "Entry method accepts a full Soot signature or Class#method(parameterTypes).");
        methodHint.setForeground(new Color(95, 106, 122));
        methodHint.setFont(methodHint.getFont().deriveFont(Font.PLAIN, 11f));
        constraints.gridy = 8;
        form.add(methodHint, constraints);
        root.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 8));
        footer.setOpaque(false);
        progress.setIndeterminate(true);
        progress.setVisible(false);
        footer.add(progress, BorderLayout.NORTH);
        status.setForeground(new Color(79, 91, 108));
        footer.add(status, BorderLayout.CENTER);
        analyze.setFont(analyze.getFont().deriveFont(Font.BOLD));
        footer.add(analyze, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel pathChooser() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(classPath, BorderLayout.CENTER);
        JButton browse = new JButton("Browse...");
        browse.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setDialogTitle("Select a classes directory or JAR");
            File current = new File(classPath.getText().trim());
            if (current.exists()) {
                chooser.setSelectedFile(current);
            }
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                classPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        panel.add(browse, BorderLayout.EAST);
        return panel;
    }

    private void addRow(JPanel form,
                        GridBagConstraints constraints,
                        int row,
                        String label,
                        java.awt.Component component) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        JLabel text = new JLabel(label);
        text.setPreferredSize(new Dimension(150, 24));
        form.add(text, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(component, constraints);
    }

    private void analyze() {
        String selectedPath = classPath.getText().trim();
        String selectedClass = entryClass.getText().trim();
        String selectedMethod = entryMethod.getText().trim();
        String selectedAlgorithm = String.valueOf(algorithm.getSelectedItem());
        int selectedMaxDepth = (Integer) maxDepth.getValue();
        int selectedMaxNodes = (Integer) maxNodes.getValue();
        boolean selectedLibraries = libraries.isSelected();
        boolean selectedConstructors = constructors.isSelected();
        if (selectedPath.isEmpty() || selectedClass.isEmpty() || selectedMethod.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Classes/JAR path, entry class and entry method are required.",
                    "Missing analysis input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File input = new File(selectedPath);
        if (!input.exists()) {
            JOptionPane.showMessageDialog(this,
                    "The analysis path does not exist:\n" + input.getAbsolutePath(),
                    "Invalid analysis path", JOptionPane.ERROR_MESSAGE);
            return;
        }

        analyze.setEnabled(false);
        progress.setVisible(true);
        status.setText("Building the " + algorithm.getSelectedItem()
                + " call graph. This can take a moment...");

        SwingWorker<CallGraphSnapshot, Void> worker =
                new SwingWorker<CallGraphSnapshot, Void>() {
                    @Override
                    protected CallGraphSnapshot doInBackground() {
                        String signature = selectedMethod;
                        if (signature.contains("#")) {
                            signature = SignatureSearch.getMethodSignatureByIDEARef(
                                    signature, selectedPath);
                            if (signature == null) {
                                throw new IllegalArgumentException(
                                        "The entry method could not be resolved.");
                            }
                        }
                        if (!signature.startsWith("<") || !signature.endsWith(">")) {
                            throw new IllegalArgumentException(
                                    "Use a full Soot signature or an IDEA-style Class#method reference.");
                        }
                        CallGraphAnalysisOptions options =
                                new CallGraphAnalysisOptions()
                                        .setMaxDepth(selectedMaxDepth)
                                        .setMaxNodes(selectedMaxNodes)
                                        .setIncludeLibraries(selectedLibraries)
                                        .setIncludeConstructors(selectedConstructors);
                        return CallGraphAnalyzer.analyze(
                                selectedClass,
                                signature,
                                selectedPath,
                                selectedAlgorithm,
                                options);
                    }

                    @Override
                    protected void done() {
                        try {
                            CallGraphSnapshot snapshot = get();
                            status.setText("Built " + snapshot.getNodes().size()
                                    + " nodes and " + snapshot.getEdges().size() + " edges.");
                            CallGraphVisualizer.open(snapshot);
                            dispose();
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            showFailure(exception);
                        } catch (ExecutionException exception) {
                            Throwable cause = exception.getCause() == null
                                    ? exception : exception.getCause();
                            showFailure(cause);
                        }
                    }
                };
        worker.execute();
    }

    private void showFailure(Throwable throwable) {
        analyze.setEnabled(true);
        progress.setVisible(false);
        status.setText("Analysis failed. Check the classpath and method signature.");
        JOptionPane.showMessageDialog(this,
                "Could not build the call graph:\n" + throwable.getMessage(),
                "Analysis failed", JOptionPane.ERROR_MESSAGE);
    }
}
