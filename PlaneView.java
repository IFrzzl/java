import javax.swing.*;
import java.awt.*;

class PlaneView extends JPanel {
    Plane plane;
    Seat[][] seatingChart;
    int rows;
    int cols;
    JLayeredPane buttonGrid;
    // Basically a custom panel to draw the plane's seating chart
    // Using a grid layout of JButtons to represent seats using the plane's seating chart

    public PlaneView() {}

    public PlaneView(Plane plane) {
        this.plane = plane;
        this.rows = plane.getRows();
        this.cols = plane.getWidth();
        this.seatingChart = plane.getSeatingChart();
        buttonGrid = new JLayeredPane();
        buttonGrid.setLayout(new GridLayout(rows, cols, 5, 5)); // 5px gap between elements
        createSeatButtons();
        buttonGrid.setPreferredSize(new Dimension(200, 600)); // Set preferred size for the grid
        buttonGrid.setBackground(Color.lightGray);
        buttonGrid.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Add a border for visibility
        add(buttonGrid);
        setVisible(true);
    }

    private void createSeatButtons() {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                JButton seatButton = new JButton();
                switch (seatingChart[i][j].getStatus()) {
                    case BUSINESS:
                        seatButton.setBackground(Color.BLUE);
                        break;
                    case ECONOMY:
                        seatButton.setBackground(Color.GREEN);
                        break;
                    case BUSINESS_EXIT:
                        seatButton.setBackground(Color.CYAN);
                        break;
                    case ECONOMY_EXIT:
                        seatButton.setBackground(Color.YELLOW);
                        break;
                    case BUSINESS_OCCUPIED:
                        seatButton.setBackground(Color.DARK_GRAY);
                        break;
                    case ECONOMY_OCCUPIED:
                        seatButton.setBackground(Color.ORANGE);
                        break;
                    case BUSINESS_EXIT_OCCUPIED:
                        seatButton.setBackground(Color.MAGENTA);
                        break;
                    case ECONOMY_EXIT_OCCUPIED:
                        seatButton.setBackground(Color.PINK);
                        break;
                    default:
                        seatButton.setBackground(Color.GRAY);
                        break;
                }
                seatButton.setMargin(new Insets(3, 3, 3, 3));
                buttonGrid.add(seatButton);

            }
        }
    }
}