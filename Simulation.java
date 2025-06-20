import java.awt.Color;
import java.util.*;

public class Simulation {
    int duration = 0;
    int[] initialGroups;
    public Simulation() {
    }

    public int[] generateInitialGroups(ArrayList<Passenger> allPassengers, int numberGroups){
        Random rand = new Random();
        int[] groups = new int[allPassengers.size()];
        for (Passenger passenger: allPassengers){
            passenger.setGroupNum(rand.nextInt(numberGroups));
        }
        // check families and make array
        int i = 0;
        for (Passenger passenger: allPassengers){
            if (passenger.getFamily() != null){
                for (Passenger relative: passenger.getFamily()){
                    relative.setGroupNum(passenger.getGroupNum());
                }
            }
            groups[i] = passenger.getGroupNum();
            i++;
        }

        initialGroups = groups;
        return initialGroups;
    }
    public int simulateBoardingTime(ArrayList<Passenger> allPassengers, Plane plane, int[] boardingInts, int numberGroups) {
        
        double ticksElapsed = 0;
        System.out.println("Starting boarding simulation...");
        // make a priority queue for boarding events
        // custom comparator using lamba to sort by start time
        PriorityQueue<Event> eventsQueue = new PriorityQueue<>((e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));

        for (int i = 0; i < allPassengers.size(); i++) {
            Passenger passenger = allPassengers.get(i);
            int boardingGroup = boardingInts[i];
            passenger.setGroupNum(boardingGroup);
        }

        // sorting allPassengers by boarding group
        Passenger[][] boardingGroups = new Passenger[numberGroups][];
        for (int i = 0; i < numberGroups; i++) {
            int numberPassengers = 0;
            for (Passenger passenger : allPassengers) {
                if (passenger.getGroupNum() == i) {
                    numberPassengers++;
                }
            }

            boardingGroups[i] = new Passenger[numberPassengers];
            int index = 0;
            for (Passenger passenger : allPassengers) {
                if (passenger.getGroupNum() == i) {
                    boardingGroups[i][index] = passenger;
                    index++;
                }
            }
        }

        

        // check if families are split up
        for (Passenger passenger:allPassengers){
            if (passenger.getFamily() != null) {
                for (Passenger relative: passenger.getFamily()){
                    if (relative.getGroupNum() != passenger.getGroupNum()) {
                        return 999999;
                    }
                }
            }
        }

        //make the plane aisle
        AisleQueue aisle = new AisleQueue(plane.length + 2);

        Queue<Passenger> boardingQueue = new LinkedList<Passenger>();
        for (int i = 0; i<numberGroups; i++){
            for (int j = 0; j<boardingGroups[i].length; j++) {
                boardingQueue.add(boardingGroups[i][j]);
            }
        }

        eventsQueue.add(new Event(EventTypes.WALK, 0, boardingQueue.poll(), 0)); // first passenger starts walking at time 0

        while (!eventsQueue.isEmpty()) {
            Event currentEvent = eventsQueue.poll();
            Passenger passenger = currentEvent.getPassenger();
            double time = currentEvent.getTime();
            int position = currentEvent.getPosition();
            passenger.queuePosition = position;
            switch (currentEvent.getType()) {
                case EventTypes.SITTING:
                    time += passenger.getSittingSpeed();
                    aisle.remove(position);
                    passenger.getSeat().setStatus(SeatStatus.OCCUPIED);
                    break;
                case EventTypes.WALK:
                    if (position >= passenger.getSeat().getRow()) {
                        eventsQueue.add(new Event(EventTypes.SITTING, time + passenger.getStowingSpeed(), passenger, position));
                    } else if (aisle.freeSpace(position)) {
                        aisle.advance(position);
                        eventsQueue.add(new Event(EventTypes.WALK, time + passenger.getWalkingSpeed(), passenger, position + 1));
                    } else {
                        eventsQueue.add(new Event(EventTypes.WALK, time + 2, passenger, position)); // wait two ticks and try again
                    }
                    break;
            }
            // always trying to cram another passenger on
            if (!boardingQueue.isEmpty()) {
                if (aisle.push(boardingQueue.peek()) != -1){
                    int queuePosition = 0;
                    if (boardingQueue.peek().getBags()>1) {
                        queuePosition = 1;
                    }
                    Event boardingEvent = new Event(EventTypes.WALK, time + 1, boardingQueue.poll(), queuePosition);
                    eventsQueue.add(boardingEvent);
                }
                }
            

            if (time>ticksElapsed) {ticksElapsed = time;}
        }

        // default return
        duration = (int) ticksElapsed;
        return duration;
    }

    public int getDuration(){
        return duration;
    }
}