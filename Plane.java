public class Plane {
        Seat[][] seatingChart;

        int length;
        int width;
        int capacity;

        int businessRows;
        int seatsperBusinessRow;;
        int economyRows;
        int[] exitRows;

        int[] blocks; // widths of each block e.g. [2, 4, 2]
        int aisles;
        int[] aisle_indexes;

        String planeType;

    public Plane(int length, int businessRows, int economyRows, int seatsPerRow, int[] blocks, int[] exitRows, String planeType) {
            this.length = length;
            this.businessRows = businessRows;
            this.economyRows = economyRows;
            this.width = seatsPerRow;
            seatsperBusinessRow = seatsPerRow;
            this.blocks = blocks;
            this.exitRows = exitRows;
            this.aisles = blocks.length - 1;
            this.planeType = planeType;
            this.aisle_indexes = new int[blocks.length - 1];
            createSeatingChart();
            this.capacity = getCapacity();
 
        }

    public void createSeatingChart() {
            seatingChart = new Seat[length][width + aisles];
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width + aisles; j++) {
                    seatingChart[i][j] = new Seat(i, j, SeatStatus.OTHER);
                    seatingChart[i][j].setRow(i);
                    seatingChart[i][j].setSeat(j);
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

                for (int j = 0; j < aisles; j++) {
                    // need the sum of seats before the aisle
                    int seatsBeforeAisle = 0;
                    for (int k = 0; k < j+1; k++) {
                        seatsBeforeAisle += blocks[k];
                    }
                    seatsBeforeAisle += j;
                    // set the aisle seat
                    seatingChart[i][seatsBeforeAisle].setStatus(SeatStatus.AISLE);
                    this.aisle_indexes[j] = seatsBeforeAisle;
                }

            }


            for (int k = 0; k < blocks.length; k++) {

                // for each block, find the index middle seats, adding seats from the previous blocks
                if (blocks[k] > 2) {
                    if (blocks[k] % 2 == 0) {
                        for (int i = 0; i < businessRows; i++) {
                            // even number of seats, set the two middle seats to OTHER
                            // realistically a row won't be wider than 6 seats, so this is fine
                            int middleSeat1 = seatsBeforeIndex(k, blocks[k] / 2 - 1);
                            int middleSeat2 = seatsBeforeIndex(k, blocks[k] / 2);
                            seatingChart[i][middleSeat1].setStatus(SeatStatus.OTHER);
                            seatingChart[i][middleSeat2].setStatus(SeatStatus.OTHER);

                        }
                        seatsperBusinessRow -= 2;
                    } else {
                        for (int i = 0; i < businessRows; i++) {
                            // odd number of seats, set the middle seat to OTHER
                            int middleSeat = seatsBeforeIndex(k, blocks[k] / 2);
                            seatingChart[i][middleSeat].setStatus(SeatStatus.OTHER);
                        }
                        seatsperBusinessRow -= 1;
                    }
                }
            }
        }

        public int seatsBeforeIndex(int block, int index) {
            int seatsBefore = block;
            for (int i = 0; i < block; i++) {
                seatsBefore += blocks[i];
            }
            seatsBefore += index; 
            return seatsBefore; 
        }

        public Seat[][] getSeatingChart() {
            return seatingChart;
        }

        public int getLength() {
            return length;
        }
        public int getCapacity() {
            int res = 0;
            for (int i = 0; i<seatingChart.length; i++){
                for (int j = 0; j<seatingChart[i].length; j++){
                    if (seatingChart[i][j].getStatus() == SeatStatus.AISLE || seatingChart[i][j].getStatus() == SeatStatus.OTHER){
                        continue;
                    }
                    res++;
                }
            }
            return res;
        }
        public int getRows() {
            return length;
        }

        public int getWidth() {
            return width + aisles;
        }

        public int[] getAisles(){
            return aisle_indexes;
        }

        public String getType(){
            return planeType;
        }

        public int getSetsperBusinessRow() {
            return seatsperBusinessRow;
        }

        public int getBusinessRows() {
            return businessRows;
        }
}
