package planeSimulation;

import javax.swing.*;
import java.awt.*;

public class SimulationWindow {

    Plane plane;
    PlaneView planeView = new PlaneView();
    SimulationControls simulationControls = new SimulationControls();

    public SimulationWindow(Plane plane) {
        this.plane = plane;
        // Stops it from trying to render before it's ready lmao
        SwingUtilities.invokeLater(this::GUI);
    }

private void GUI() {
    JFrame frame = new JFrame("Simulation Window");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1500, 900);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

    JFrame helpBox = new JFrame("Help");
    helpBox.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    Color[] groupColours = {
        new Color(255,120,135), new Color(255,165,95),  new Color(255,235,120),
        new Color(195,250,140), new Color(135,235,170), new Color(130,230,215),
        new Color(145,195,255), new Color(165,160,255), new Color(185,140,255),
        new Color(220,150,255), new Color(245,155,235), new Color(255,165,225),
        new Color(255,175,205), new Color(255,190,215), new Color(255,205,225),
        new Color(255,220,235)
    };

    helpBox.setSize(800, 600);
    // Panel of group number boxes to show boarding group colors.
    JPanel demoSeats = new JPanel(new GridLayout(4, 3, 5, 5));
    for (int i = 0; i < 12; i++) {
        JButton seatDemo = new JButton("" + (i + 1));
        seatDemo.setBackground(groupColours[i]);
        demoSeats.add(seatDemo);
    };

    helpBox.getContentPane().add(demoSeats, BorderLayout.SOUTH);
    JLabel helpLabel = new JLabel("<html><body style='padding: 10px;'>" +
            "<h2>Simulation Help</h2>" +
            "<p>This simulation allows you to visualize the boarding process of a plane.</p>" +
            "<ul>" +
            "<li><b>Start Simulation:</b> Click the 'Start' button to begin boarding.</li>" +
            "<li><b>Pause Simulation:</b> Click the 'Pause' button to pause boarding.</li>" +
            "<li><b>Reset Simulation:</b> Click the 'Reset' button to restart boarding.</li>" +
            "</ul>" +
            "<p>Use the controls on the right panel to adjust simulation parameters.</p>" +
            "<p>Definitions:</p>" +
            "<ul>" +
            "<li><b>Generation:</b> A collection of simulations run with the same parameters to evaluate performance.</li>" +
            "<li><b>Simulation:</b> A single instance of the boarding process.</li>" +
            "<li><b>Boarding Group:</b> A subset of passengers assigned to board together.</li>" +
            "<li><b> % elitism:</b> The percentage of top-performing simulations carried over to the next generation without alteration.</li>" +
            "<li><b> Tournament Size:</b> The number of simulations competing in each selection round for breeding.</li>" +
            "<li><b> Selection Pool:</b> The proportion of the current generation eligible for selection as parents.</li>" +
            "<li><b> Mutation:</b> The probability of simulations randomly tweaked to ensure diversity.</li>" +
            "<li><b> New Simulations:</b> The proportion of entirely new simulations introduced each generation.</li>" +
            "<li><b> Clustering </b>: Valuing passengers sitting near each other higher when assigning boarding groups.</li>" +
            "<li><b> Orderliness </b>: Valuing passengers boarding in an easily analyzable order (using standard deviation of seat pair distances) </li>" +
            "</ul>" +
            "<p> The plane view uses panels where different colors represent different boarding groups. </p>" +
            "<p> The following panels correspond to boarding groups: </p>" +
            "</body>" +
            "</body></html>");
    helpBox.getContentPane().add(helpLabel, BorderLayout.CENTER);
    helpBox.setLocationRelativeTo(frame);


    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(e -> System.exit(0));
    fileMenu.add(exitItem);
    JMenuItem helpItem = new JMenuItem("Help");
    helpItem.setBackground(Color.red);
    helpItem.addActionListener(e -> {
        helpBox.setLocationRelativeTo(frame);
        helpBox.setVisible(true);
        helpBox.toFront();
    });
    menuBar.add(fileMenu);
    menuBar.add(helpItem);
    frame.setJMenuBar(menuBar);
 
    JPanel rightframe = new JPanel();
    rightframe.setLayout(new BoxLayout(rightframe, BoxLayout.Y_AXIS));
    rightframe.add(new JLabel("Simulation Controls"));
    simulationControls.populate(parameters.plane);
    rightframe.add(simulationControls);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, planeView, rightframe);
    splitPane.setResizeWeight(0.7);
    splitPane.setDividerLocation((int)(frame.getWidth() * 0.5));
    splitPane.setContinuousLayout(true);

    frame.getContentPane().add(splitPane, BorderLayout.CENTER);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

}
    public void setPlaneView() {
        this.plane = parameters.plane;
        planeView.removeAll();
        planeView.setPlane(null);
        planeView.setPassengers(parameters.allPassengers);
        planeView.repaint();
    }

public void replacePlane(Plane plane){ // bad naming lol
        this.plane = plane;
        SwingUtilities.invokeLater(() -> { // was having issues with sync
            planeView.removeAll();
            planeView.setPlane(null);
            planeView.revalidate();
            planeView.repaint();
            simulationControls.populate(parameters.plane); 
        }); 
    }

    public void refreshPlaneView(int[] groups) {
        planeView.updateButtons(groups);
        planeView.repaint();
    }

    public void setPlane(){
        planeView.setPlane(null);
        simulationControls.populate(parameters.plane);
    }
}

