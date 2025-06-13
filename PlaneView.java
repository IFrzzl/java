import javax.swing.*;
import java.awt.*;

class PlaneView extends JPanel {
    Plane plane;
    Seat[][] seatingChart;
    int rows;
    int cols;
    // Basically a custom panel to draw the plane's seating chart
    // Using a grid layout of JButtons to represent seats using the plane's seating chart

    public PlaneView() {}

    public PlaneView(Plane plane) {
        this.plane = plane;
        this.rows = plane.getRows();
        this.cols = plane.getLength();
        this.seatingChart = plane.getSeatingChart();
        setLayout(new GridLayout(rows, cols));
        createSeatButtons();

        setBackground(Color.RED);

        setVisible(true);
    }

    private void createSeatButtons() {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton seatButton = new JButton();
                seatButton.setPreferredSize(new Dimension(8, 8));
                seatButton.setBackground(Color.RED);
                seatButton.setMargin(new Insets(1, 1, 1, 1));
                add(seatButton);
            }
        }
    }
}