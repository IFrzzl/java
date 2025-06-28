import java.util.ArrayList;

class Passenger {

    //attributes
    int[] family; // index in allPassengers
    double walkingSpeed;
    double stowingSpeed;
    double sittingSpeed;
    Seat seat;
    int bags;
    int index;

    public Passenger(){}
    public Passenger(int index, double walkingSpeed, double stowingSpeed, double sittingSpeed, Seat seat, int bags) {
        this.index = index;
        this.walkingSpeed = walkingSpeed;
        this.stowingSpeed = stowingSpeed;
        this.sittingSpeed = sittingSpeed;
        this.seat = seat;
        this.bags = bags;
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
    public void setFamily(int[] family) {
/*         int[] safe = new int[family.length-1];
        int j = 0;
        for (int i = 0; i<family.length-1; i++){
            if (family[i] != this.index){safe[j++] = family[i];}
        }
        this.family = safe; */
        this.family = family;
    }
    public int[] getFamily() {
        return family;
    }
    public int getBags() {
        return bags;
    }
    public void setBags(int bags) {
        this.bags = bags;
    }
    public int getIndex(){return index;}
}