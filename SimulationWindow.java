
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

    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(e -> System.exit(0));
    fileMenu.add(exitItem);
    menuBar.add(fileMenu);
    frame.setJMenuBar(menuBar);
 
    simulationControls.populate(plane);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, planeView, simulationControls);
    splitPane.setResizeWeight(0.7);
    splitPane.setDividerLocation((int)(frame.getWidth() * 0.5));
    splitPane.setContinuousLayout(true);

    frame.getContentPane().add(splitPane, BorderLayout.CENTER);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    }

    public void setPlaneView(Plane plane, Passenger[] allPassengers) {
        this.plane = plane;
        planeView.removeAll();
        planeView.setPlane(plane);
        planeView.setPassengers(allPassengers);
        planeView.repaint();
    }

public void replacePlane(Plane plane){ // bad naming lol
        this.plane = plane;
        SwingUtilities.invokeLater(() -> { // was having issues with sync
            planeView.removeAll();
            planeView.setPlane(plane);
            planeView.revalidate();
            planeView.repaint();
            simulationControls.populate(plane); 
        }); 
    }

    public void refreshPlaneView(int[] groups) {
        planeView.updateButtons(groups);
        planeView.repaint();
    }

    public void setPlane(Plane plane){
        planeView.setPlane(plane);
        simulationControls.populate(plane);
    }
}

