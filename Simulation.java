import java.awt.Color;
import java.util.*;

public class Simulation {

    public static void main(String[] args) {

        Plane plane = new Plane(20, 2, 10, 4, new int[]{2, 2}, 80, new int[]{5 ,9, 12, 15}, "Boeing 737");
        final int MAX_GROUPS = 6;
 /*        SimulationWindow simulationWindow = new SimulationWindow(plane);

        simulationWindow.planeView.setBackground(Color.WHITE);
        simulationWindow.refreshPlaneView(); */

        ArrayList<Passenger> allPassengers = new ArrayList<>();
        generatePassengerData(plane, allPassengers);
        int[] initialGroups = generateInitialGroups(allPassengers, MAX_GROUPS);
        int duration = simulateBoardingTime(allPassengers, plane, initialGroups, MAX_GROUPS);
        System.out.println(duration);
    }

    public static void generatePassengerData(Plane plane, ArrayList<Passenger> allPassengers) {
        Random rand = new Random();

        // we're making a set of variables to control walking, stowing, and sitting speeds
        // these are the probabilities of a passenger being/having

        final double families = 0.20;
        int numberPassengers = plane.getCapacity();

        for (int i=0;i<numberPassengers;i++){
            Passenger passenger = new Passenger();
            int bags = 0;
            double factor = 1.0;
            if (rand.nextInt(100)<=constants.PROBABILITY_DISABLED*100){
                factor *= 0.4;
            }
            if (rand.nextInt(100)<=constants.PROBABILITY_OLD*100|| rand.nextInt(100)<=constants.PROBABILITY_CHILDREN*100) {
                factor *= 0.8;
            }
            if (rand.nextInt(100)<=constants.PROBABILITY_BAGS*100) {
                factor *= 1 - (0.2 * rand.nextInt(0, 2));
            }
            if (rand.nextInt(100)<=constants.PROBABILITY_BAGS*100) {
                bags = rand.nextInt(0, constants.MAX_BAGS + 1);
            }
            passenger.setWalkingSpeed(rand.nextGaussian() * 2.0 * 1/factor + constants.DEFAULT_WALKING_SPEED); // 2 is the standard deviation
            passenger.setStowingSpeed(bags * (rand.nextGaussian() * 2.0 * 1/factor + constants.DEFAULT_STOWING_SPEED));
            passenger.setSittingSpeed(rand.nextGaussian() * 2.0 * 1/factor + constants.DEFAULT_SITTING_SPEED);
            passenger.setBags(bags);
            allPassengers.add(passenger);
        }

        //generate families
        int familyPassengers = (int) (families*numberPassengers);
        while (familyPassengers>0){

            int familySize = 0;
            if (familyPassengers > 8){
                familySize = rand.nextInt(4) + 2;
            } else {
                familySize = familyPassengers;
            }

            ArrayList<Passenger> family = new ArrayList<>();
            int index = rand.nextInt(0, numberPassengers);
            for (int i = index; i <= index + familySize; i++){
                if (i >= numberPassengers || allPassengers.get(i).getFamily() != null) {continue;}
                family.add(allPassengers.get(i));
                allPassengers.get(i).setFamily(family);
            }
            familyPassengers -= familySize;
        }

        // assign seats
        Seat[][] seatingChart = plane.getSeatingChart();
        int rows = plane.getRows();
        int cols = plane.getWidth();
        int p = 0; // this requires the plane capacity to be correct
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (seatingChart[i][j] == null || seatingChart[i][j].getStatus() == SeatStatus.OTHER) {continue;}
                Passenger passenger = new Passenger();
                if (p < allPassengers.size()) {passenger = allPassengers.get(p);}
                if (passenger.getSeat() == null) {
                    passenger.setSeat(seatingChart[i][j]);
                    seatingChart[i][j].setStatus(SeatStatus.OCCUPIED);
                    passenger.setSeat(seatingChart[i][j]);
                    p++;
                }
            }
        }
    }
    public static int[] generateInitialGroups(ArrayList<Passenger> allPassengers, int numberGroups){
        Random rand = new Random();
        int[] groups = new int[allPassengers.size()];
        for (Passenger passenger: allPassengers){
            passenger.setGroupNum(rand.nextInt(1, numberGroups + 1));
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
        return groups;
    }
    public static int simulateBoardingTime(ArrayList<Passenger> allPassengers, Plane plane, int[] boardingInts, int numberGroups) {
        
        double ticksElapsed = 0;
        System.out.println("Starting boarding simulation...");
        // make a priority queue for boarding events
        // custom comparator using lamba to sort by start time
        PriorityQueue<Event> eventsQueue = new PriorityQueue<>((e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));
        eventsQueue.add(new Event(EventTypes.WALK, 0, allPassengers.get(0), 0)); // first passenger starts walking at time 0

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

        while (!eventsQueue.isEmpty()) {
            Event currentEvent = eventsQueue.poll();
            Passenger passenger = currentEvent.getPassenger();
            double time = currentEvent.getTime();
            int position = currentEvent.getPosition();
            switch (currentEvent.getType()) {
                case EventTypes.SITTING:
                    time += passenger.getSittingSpeed();
                    aisle.remove(position);
                    passenger.getSeat().setStatus(SeatStatus.OCCUPIED);
                    // so this is the only time that the queue can shift, so we should also consi
                case EventTypes.WALK:
                    System.out.println("Passenger " + passenger.getGroupNum() + " at position " + position + " at time " + time);
                    if (position >= passenger.getSeat().getRow()) {
                        eventsQueue.add(new Event(EventTypes.SITTING, time + passenger.getStowingSpeed(), passenger, position));
                    } else if (aisle.freeSpace(position)) {
                        aisle.shift(position + 1);
                        System.out.println(aisle);
                        eventsQueue.add(new Event(EventTypes.WALK, time + passenger.getWalkingSpeed(), passenger, position + 1));
                    } else {
                        eventsQueue.add(new Event(EventTypes.WALK, time + 2, passenger, position)); // wait two ticks and try again
                    }
            }

            // always trying to cram another passenger on
            if (!boardingQueue.isEmpty()) {
                if (aisle.push(boardingQueue.peek()) != -1){
                    Event boardingEvent = new Event(EventTypes.WALK, ticksElapsed + 1, boardingQueue.poll(), 0);
                    eventsQueue.add(boardingEvent);
                }
            }

            if (time>ticksElapsed) {ticksElapsed = time;}
        }

        // default return
        return (int) ticksElapsed;
    }
}