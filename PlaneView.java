import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;

class PlaneView extends JPanel {
    Plane plane;
    Seat[][] seatingChart;
    int rows;
    int cols;
    JPanel buttonGrid; 
    Passenger[] passengers;
    int[] groups;
    public PlaneView() {}

    public void setPassengers( Passenger[] passengers){
        this.passengers = passengers;
    }
    public void setPlane(Plane plane){
        this.plane = plane;
        this.rows = plane.getRows();
        this.cols = plane.getWidth();
        this.seatingChart = plane.getSeatingChart();

        add(new JLabel(plane.getType() +  " - Plane View"));

        final int buttonSize = parameters.BUTTON_SIZE;
        final int gap = parameters.BUTTON_GAP;
        buttonGrid = new JPanel(new GridLayout(rows, cols, gap, gap));
        buttonGrid.setPreferredSize(new Dimension(
            cols * buttonSize + (cols - 1) * gap + 30,
            rows * buttonSize + (rows - 1) * gap + 30
        ));

        createSeatButtons(buttonSize);

        JPanel gridWrapper = new JPanel(new GridBagLayout());
        buttonGrid.setBackground(new Color(17, 0, 102));
        Border coloured = BorderFactory.createLineBorder(Color.DARK_GRAY, 10);
        Border bevel = BorderFactory.createRaisedBevelBorder();
        Border compound = BorderFactory.createCompoundBorder(coloured, bevel);
        buttonGrid.setBorder(compound);

        gridWrapper.add(buttonGrid);

        setLayout(new BorderLayout());
        add(gridWrapper, BorderLayout.CENTER);
        setBackground(Color.WHITE); // smoosh everything together
    }

    private void createSeatButtons(int buttonSize) {

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton seatButton = new JButton();
                seatButton.setPreferredSize(new Dimension(buttonSize, buttonSize)); // Fixed size

                // color schemes

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
                        seatButton.setBackground(Color.DARK_GRAY);
                        break;
                    default:
                        seatButton.setBackground(Color.DARK_GRAY);
                        break;
                }
                buttonGrid.add(seatButton);

            }
        }
    }

    public void updateButtons(int[] groups){
        Color[] groupColours = {
    new Color(255, 105, 120),   // Vivid Soft Red
    new Color(255, 155, 70),    // Vivid Apricot Orange
    new Color(255, 220, 80),    // Bright Pastel Yellow
    new Color(120, 230, 130),   // Saturated Mint Green
    new Color(100, 190, 255),   // Sky Blue
    new Color(155, 130, 255),   // Lilac Purple
    new Color(255, 140, 230),   // Bright Light Pink
    new Color(255, 170, 210),   // Punchy Cotton Candy
    new Color(130, 255, 210),   // Fresh Aqua Teal
    new Color(255, 195, 110),   // Soft Tangerine
    new Color(160, 220, 255),   // Cool Sky
    new Color(100, 180, 255),   // Bright Baby Blue
    new Color(100, 255, 245),   // Cyan Ice
    new Color(185, 145, 255),   // Electric Periwinkle
    new Color(255, 120, 185),   // Bubblegum Pink
    new Color(255, 165, 165)    // Rosy Blush
};
        Component[] allButtons = buttonGrid.getComponents();
        int len = seatingChart.length;
        int width = seatingChart[0].length;

        for (Passenger passenger: passengers){
            Seat seat = passenger.getSeat();
            int seatIndex = seat.getRow()*width + seat.getSeat();
            JButton seatButton = (JButton) allButtons[seatIndex];
            seatButton.setBackground(groupColours[groups[passenger.getIndex()]]);
            seatButton.setText(Integer.toString(groups[passenger.getIndex()]));
        }
    }
}