class Passenger {

    //attributes
    Family famlily;
    Boolean disabled;
    Seat seat;
    
    Group groupNum;

    public Passenger() {

    }
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
    public Boolean getDisabled() {
        return disabled;
    }
    public void setSeat(Seat seat) {
        this.seat = seat;
    }
    public Seat getSeat() {
        return seat;
    }
    public void setFamily(Family family) {
        this.famlily = family;
    }
    public Family getFamily() {
        return famlily;
    }
    public void setGroupNum(Group groupNum) {
        this.groupNum = groupNum;
    }
    public Group getGroupNum() {
        return groupNum;
    }

}