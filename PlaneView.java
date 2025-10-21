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

    // was using a JButton but rendering was soooo slow so basically using tiny little JPanels now.
    static class SeatCell extends JPanel {
        public SeatCell(int size) {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(size, size));
            setOpaque(true);
        }
        void setCellColor(Color c){ setBackground(c); }
    }

    public PlaneView() { setDoubleBuffered(true); }

    public void setPassengers(Passenger[] passengers){
        this.passengers = passengers;
    }

    public void setPlane(Plane plane){
        this.plane = plane;
        this.rows = plane.getRows();
        this.seatingChart = plane.getSeatingChart();
        // cols must match seatingChart columns (includes aisles)
        this.cols = (seatingChart != null && seatingChart.length > 0) ? seatingChart[0].length : plane.getWidth();

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
        removeAll();                 
        add(gridWrapper, BorderLayout.CENTER);
        revalidate();                
        repaint();
        setVisible(true);
    }

    private void createSeatButtons(int buttonSize) {
        // create lightweight cells once
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                SeatCell cell = new SeatCell(buttonSize);
                // apply base color once based on seat status (if seatingChart present)
                if (seatingChart != null) {
                    SeatStatus status = seatingChart[i][j].getStatus();
                    switch (status) {
                        case BUSINESS: cell.setCellColor(new Color(251, 109, 112)); break;
                        case ECONOMY: cell.setCellColor(new Color(219, 53, 47)); break;
                        case BUSINESS_EXIT: case ECONOMY_EXIT: cell.setCellColor(new Color(235, 82, 35)); break;
                        case AISLE: default: cell.setCellColor(Color.DARK_GRAY); break;
                    }
                } else {
                    cell.setCellColor(Color.DARK_GRAY);
                }
                buttonGrid.add(cell);
            }
        }
    }

    public void updateButtons(int[] groups){
        if (buttonGrid == null || seatingChart == null || passengers == null || groups == null) return;

        Component[] allButtons = buttonGrid.getComponents();
        int width = (seatingChart.length > 0) ? seatingChart[0].length : cols;
        if (allButtons == null || allButtons.length < rows * width) return;

        Color[] groupColours = {
            new Color(255, 105, 120), new Color(255, 155, 70), new Color(255, 220, 80),
            new Color(100, 255, 245), new Color(120, 230, 130), new Color(100, 190, 255),
            new Color(155, 130, 255), new Color(255, 140, 230), new Color(255, 170, 210),
            new Color(130, 255, 210), new Color(255, 195, 110), new Color(160, 220, 255),
            new Color(100, 180, 255), new Color(255, 165, 165), new Color(185, 145, 255),
            new Color(255, 120, 185),
        };

        for (Passenger passenger: passengers){
            Seat seat = passenger.getSeat();
            if (seat == null) continue;
            int seatIndex = seat.getRow() * width + seat.getSeat();
            if (seatIndex < 0 || seatIndex >= allButtons.length) continue;
            SeatCell cell = (SeatCell) allButtons[seatIndex];
            int g = groups[passenger.getIndex()];
            if (g >= 0 && g < groupColours.length) {
                cell.setCellColor(groupColours[g]);
            }
        }

        buttonGrid.repaint();
    }
}