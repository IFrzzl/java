public class Plane {
        Seat[][] seatingChart;

        int length;
        int capacity;

        int businessRows;
        int businessSeatsPerRow;

        int economyRows;
        int defaultSeatsPerRow;

        int[] businessExitRows;
        int[] economyExitRows;

        String planeType;

    public Plane(int length, int businessRows, int businessSeatsPerRow, int economyRows,
     int economySeatsPerRow, int[] businessExitRows, int[] economyExitRows, int capacity, String planeType) {
            this.length = length;
            this.businessRows = businessRows;
            this.businessSeatsPerRow = businessSeatsPerRow;
            this.economyRows = economyRows;
            this.defaultSeatsPerRow = economySeatsPerRow;
            this.businessExitRows = businessExitRows;
            this.economyExitRows = economyExitRows;
            this.capacity = capacity;
    }

    public void createSeatingChart() {
               seatingChart = new Seat[length][];
            for (int i = 0; i < length; i++) {
                if (i < businessRows) {
                    seatingChart[i] = new Seat[businessSeatsPerRow];
                    for (int j = 0; j < businessSeatsPerRow; j++) {
                        seatingChart[i][j] = new Seat(i, j, SeatStatus.BUSINESS);
                    }
                } else {
                    seatingChart[i] = new Seat[defaultSeatsPerRow];
                    for (int j = 0; j < defaultSeatsPerRow; j++) {
                        seatingChart[i][j] = new Seat(i, j, SeatStatus.ECONOMY);
                    }
                }
            }

            // Set exit rows
            for (int row : businessExitRows) {
                for (int j = 0; j < businessSeatsPerRow; j++) {
                    seatingChart[row][j].setStatus(SeatStatus.BUSINESS_EXIT);
                }
            }
            for (int row : economyExitRows) {
                for (int j = 0; j < defaultSeatsPerRow; j++) {
                    seatingChart[row][j].setStatus(SeatStatus.ECONOMY_EXIT);
                }
            }
        }

        public Seat[][] getSeatingChart() {
            return seatingChart;
        }

        public int getLength() {
            return length;
        }
        public int getCapacity() {
            return capacity;
        }
        public int getRows() {
            return defaultSeatsPerRow;
        }

}
