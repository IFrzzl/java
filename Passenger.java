import java.util.ArrayList;

class Passenger {

    //attributes
    ArrayList<Passenger> family;
    double walkingSpeed;
    double stowingSpeed;
    double sittingSpeed;
    Seat seat;
    int bags;
   public int id = 69;

    int groupNum;
    public int queuePosition = -1; // -1 means not in queue

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
    public double getWalkingSpeed() {
        return walkingSpeed;
    }
    public double getStowingSpeed() {
        return stowingSpeed;
    }
    public double getSittingSpeed() {
        return sittingSpeed;
    }
    public void setSeat(Seat seat) {
        this.seat = seat;
    }
    public Seat getSeat() {
        return seat;
    }
    public void setFamily(ArrayList<Passenger> family) {
        family.remove(this); // i have a feeling i might cause recursion problems lol
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
    public int getBags() {
        return bags;
    }
    public void setBags(int bags) {
        this.bags = bags;
    }
}