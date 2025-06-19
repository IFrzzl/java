public class Event {
    private EventTypes type;
    private int time;
    private Passenger passenger;

    public Event(EventTypes type, int time, Passenger passenger) {
        this.type = type;
        this.time = time;
        this.passenger = passenger;
    }

    public EventTypes getType() {
        return type;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public int getTime() {
        return time;
    }
}
