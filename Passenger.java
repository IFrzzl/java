class Passenger {

    //attributes
    Family family;
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
    public void setFamily(Family family) {
        this.family = family;
    }
    public Family getFamily() {
        return family;
    }
    public void setGroupNum(int groupNum) {
        this.groupNum = groupNum;
    }
    public int getGroupNum() {
        return groupNum;
    }

}