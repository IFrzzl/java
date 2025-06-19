import java.util.ArrayList;

class Passenger {

    //attributes
    ArrayList<Passenger> family;
    double walkingSpeed;
    double stowingSpeed;
    double sittingSpeed;
    Seat seat;
    
    int groupNum;

    public Passenger() {

    }

    public void setWalkingSpeed(double walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }
    public void setStowingSpeed(double stowingSpeed) {
        this.stowingSpeed = stowingSpeed;
    }
    public void setSittingSpeed(double sittingSpeed) {
        this.sittingSpeed = sittingSpeed;
    }
    public void setSeat(Seat seat) {
        this.seat = seat;
    }
    public Seat getSeat() {
        return seat;
    }
    public void setFamily(ArrayList<Passenger> family) {
        this.family = family;
    }
    public ArrayList<Passenger> getFamily() {
        return family;
    }
    public void setGroupNum(int groupNum) {
        this.groupNum = groupNum;
    }
    public int getGroupNum() {
        return groupNum;
    }

}