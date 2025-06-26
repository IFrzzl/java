public class Seat {
    int row;
    int seat;
    SeatStatus status;

    public Seat(int row, int seat, SeatStatus status) {
        this.row = row;
        this.seat = seat;
        this.status = status;
    }
    public int letterNumber(char letter) {
        return (int) letter - 64;
    }
    
    public char numberLetter(int number) {
        return (char) (number + 64);
    }

    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getSeat() {
        return seat;
    }
    public void setSeat(int seat) {
        this.seat = seat;
    }
    public SeatStatus getStatus() {
        return status;
    }
    public void setStatus(SeatStatus newStatus) {
        if (newStatus == SeatStatus.OCCUPIED) {
            switch (status) {
                case SeatStatus.ECONOMY: status = SeatStatus.ECONOMY_OCCUPIED;
                case SeatStatus.ECONOMY_EXIT: status = SeatStatus.ECONOMY_EXIT_OCCUPIED;
                case SeatStatus.BUSINESS: status = SeatStatus.BUSINESS_OCCUPIED;
                case SeatStatus.BUSINESS_EXIT: status = SeatStatus.BUSINESS_EXIT_OCCUPIED;
                default: status = SeatStatus.OTHER;
            }
        } else {
            status = newStatus;
        }
    }
}