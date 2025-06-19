import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;

class PlaneView extends JPanel {
    Plane plane;
    Seat[][] seatingChart;
    int rows;
    int cols;
    JPanel buttonGrid; 

    public PlaneView() {}

    public PlaneView(Plane plane) {
        this.plane = plane;
        this.rows = plane.getRows();
        this.cols = plane.getWidth();
        this.seatingChart = plane.getSeatingChart();

        final int buttonSize = 20; // compared to using a grid layout, this loses flexibility but is easier to manage
        final int gap = 5;
        buttonGrid = new JPanel(new GridLayout(rows, cols, gap, gap));
        buttonGrid.setPreferredSize(new Dimension(
            cols * buttonSize + (cols - 1) * gap,
            rows * buttonSize + (rows - 1) * gap
        ));

        createSeatButtons(buttonSize);

        JPanel gridWrapper = new JPanel(new GridBagLayout());
        buttonGrid.setBackground(new Color(17, 0, 102)); // Centers the seat grid inside the PlaneView panel
        gridWrapper.add(buttonGrid);

        setLayout(new BorderLayout());
        add(gridWrapper, BorderLayout.CENTER);
        setBackground(Color.WHITE); // smoosh everything together
    }
    
    // Not my code - from https://stackoverflow.com/questions/423950/rounded-swing-jbutton-using-java
    private static class RoundedBorder implements Border {

        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }

    private void createSeatButtons(int buttonSize) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton seatButton = new JButton();
                seatButton.setPreferredSize(new Dimension(buttonSize, buttonSize)); // Fixed size

                switch (seatingChart[i][j].getStatus()) {
                    case BUSINESS:
                        seatButton.setBackground(new Color(251, 109, 112));
                        break;
                    case ECONOMY:
                        seatButton.setBackground(new Color(219, 53, 47));
                        break;
                    case BUSINESS_EXIT:
                      seatButton.setBackground(new Color(235, 82, 35));
                        break;
                    case ECONOMY_EXIT:
                        seatButton.setBackground(new Color(235, 82, 35));
                        break;
                    case BUSINESS_OCCUPIED:
                        seatButton.setBackground(new Color(56, 214, 93));
                        break;
                    case ECONOMY_OCCUPIED:
                        seatButton.setBackground(new Color(57, 211, 19));
                        break;
                    case BUSINESS_EXIT_OCCUPIED:
                        seatButton.setBackground(new Color(235, 115, 35));
                        break;
                    case ECONOMY_EXIT_OCCUPIED:
                        seatButton.setBackground(new Color(235, 115, 35));
                        break;
                    case AISLE:
                        seatButton.setBackground(new Color(217, 211, 205));
                        System.out.println(seatButton.getHeight());
                        seatButton.setBorder(new RoundedBorder(40 ));
                        break;
                    default:
                        seatButton.setBackground(Color.GRAY);
                        break;
                }

                buttonGrid.add(seatButton);

            }
        }
    }
}