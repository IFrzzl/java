import javax.swing.event.DocumentEvent.EventType;

public class Event {
    private EventType type;
    private int time;
    private Passenger passenger;

    public Event(EventType type, int time, Passenger passenger) {
        this.type = type;
        this.time = time;
        this.passenger = passenger;
    }

    public EventType getType() {
        return type;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public int getTime() {
        return time;
    }
}
