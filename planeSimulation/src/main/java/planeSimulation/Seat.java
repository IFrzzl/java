package planeSimulation;
public class Seat {
    int row;
    int seat;
    SeatStatus status;

    public Seat(int row, int seat, SeatStatus status) {
        this.row = row;
        this.seat = seat;
        this.status = status;
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
        this.status = newStatus;
    }
}