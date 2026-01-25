package planeSimulation;
public class Event {
    private EventTypes type;
    private double time;
    private int passenger;
    private int position; // which row it happens in

    public Event(EventTypes type, double time, int passenger, int position) {
        this.type = type;
        this.time = time;
        this.passenger = passenger;
        this.position = position;
    }

    public EventTypes getType() {
        return type;
    }

    public int getPassenger() {
        return passenger;
    }

    public double getTime() {
        return time;
    }

    public int getPosition() {
        return position;
    }
}
