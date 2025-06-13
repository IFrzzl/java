
import javax.swing.*;
import java.awt.*;

public class SimulationWindow {

    public static void main(String[] args) {
        // Stops the GUI trying to render before it's actually created lmao
        SwingUtilities.invokeLater(SimulationWindow::GUI);
    }

    private static void GUI() {
        // Create the main frame
        // TODO: COMPLETE THIS PER DRAWINGS
        JFrame frame = new JFrame("Simulation Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 800);

        // Set up the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");

        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        // Create the two panels
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE); 
        leftPanel.add(new JLabel("Plane View"));

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.add(new JLabel("Simulation Controls"));

        // Split pane with 3:4 ratio
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation((int)(frame.getWidth() * 0.7));
        splitPane.setContinuousLayout(true);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null); // Centers the thing
        frame.setVisible(true);
    }
}