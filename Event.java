public class Event {
    private EventTypes type;
    private double time;
    private Passenger passenger;
    private int position; // which row it happens in

    public Event(EventTypes type, double time, Passenger passenger, int position) {
        this.type = type;
        this.time = time;
        this.passenger = passenger;
        this.position = position;
    }

    public EventTypes getType() {
        return type;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public double getTime() {
        return time;
    }

    public int getPosition() {
        return position;
    }
}
