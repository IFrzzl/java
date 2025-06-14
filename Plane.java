public class Plane {
        Seat[][] seatingChart;

        int length;
        int width;
        int capacity;

        int businessRows;
        int economyRows;
        int[] exitRows;

        int[] blocks; // widths of each block e.g. [2, 4, 2]
        int aisles;

        String planeType;

    public Plane(int length, int businessRows, int economyRows, int seatsPerRow, int[] blocks, int capacity, int[] exitRows, String planeType) {
            this.length = length;
            this.businessRows = businessRows;
            this.economyRows = economyRows;
            this.width = seatsPerRow;
            this.blocks = blocks;
            this.exitRows = exitRows;
            this.capacity = capacity;
            this.aisles = blocks.length - 1; // Default aisles
            this.planeType = planeType;
        }

    public void createSeatingChart() {
            seatingChart = new Seat[length][width + aisles];
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width + aisles; j++) {
                    seatingChart[i][j] = new Seat(i, j, SeatStatus.OTHER);
                    if (i < businessRows) {
                        seatingChart[i][j].setStatus(SeatStatus.BUSINESS);
                    } else {
                        seatingChart[i][j].setStatus(SeatStatus.ECONOMY);
                    }

                    for (int exit: exitRows) {
                    if (i == exit) {
                            if (i < businessRows) {
                                seatingChart[i][j].setStatus(SeatStatus.BUSINESS_EXIT);
                            } else {
                                seatingChart[i][j].setStatus(SeatStatus.ECONOMY_EXIT);
                            }
                        }
                    }
                }
                
                for (int j = 1; j <= aisles; j++) {
                    // need the sum of seats before the aisle
                    int seatsBeforeAisle = 0;
                    for (int k = 0; k < j; k++) {
                        seatsBeforeAisle += blocks[k];
                    }
                    seatsBeforeAisle += j-1;
                    // set the aisle seat
                    seatingChart[i][seatsBeforeAisle].setStatus(SeatStatus.AISLE);
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
            return length;
        }

        public int getWidth() {
            return width + aisles;
        }
}
