import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.PriorityQueue;

public class Simulation {

    public static void main(String[] args) {

        // Create a plane
        Plane plane = new Plane(20, 2, 10, 4, new int[]{2, 2}, 180, new int[]{5 ,9, 12, 15}, "Boeing 737");
        // Create a seating chart

        SimulationWindow simulationWindow = new SimulationWindow(plane);

        
        // Refresh the plane view
        simulationWindow.planeView.setBackground(Color.WHITE);
        simulationWindow.refreshPlaneView();

        // Generate passenger data
        ArrayList<Passenger> allPassengers = new ArrayList<>();
        generatePassengerData(plane, allPassengers);
    }

    public static void generatePassengerData(Plane plane, ArrayList<Passenger> allPassengers) {
        Random rand = new Random();

        // we're making a set of variables to control walking, stowing, and sitting speeds
        // these are the probabilities of a passenger being/having
        final double disabled = 0.03;
        final double families = 0.20;
        final double old = 0.10;
        final double children = 0.10;
        final double bags = 0.80;
;
        int numberPassengers = plane.getCapacity();
        for (int i=0;i<numberPassengers;i++){
            Passenger passenger = new Passenger();
            int j = rand.nextInt(100);
            double factor = 1.0;
            if (j<=disabled*100){
                factor *= 0.4;
            }
            if (j<=old*100|| j<=children*100) {
                factor *= 0.8;
            }
            if (j<=bags*100) {
                factor *= 1 - (0.2 * rand.nextInt(0, 2));
            }
            passenger.setWalkingSpeed(rand.nextGaussian() * 2.0 * 1/factor + 2.0);
            passenger.setStowingSpeed(bags * (rand.nextGaussian() * 2.0 * 1/factor + 5.0));
            passenger.setSittingSpeed(rand.nextGaussian() * 2.0 * 1/factor + 4.0);
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
            int i = 0;
            do {
                Passenger passenger = allPassengers.get(rand.nextInt(numberPassengers));
                if (passenger.getFamily() == null){
                    passenger.setFamily(family);
                    family.add(passenger);
                    i++;
                }
            } while (i<familySize);
            familyPassengers -= familySize;
        }
    }

    public static int simulateBoardingTime(ArrayList<Passenger> allPassengers, Plane plane, int[] boardingInts, int groupsNumber) {
        // make a priority queue for boarding events
        // custom comparator using lamba to sort by start time
        PriorityQueue<Event> eventsQueue = new PriorityQueue<>((e1, e2) -> Integer.compare(e1.getTime(), e2.getTime()));
        for (int i = 0; i < allPassengers.size(); i++) {
            Passenger passenger = allPassengers.get(i);
            int boardingGroup = boardingInts[i];
            passenger.setGroupNum(boardingGroup);
        }

        // arrays of passengers by boarding group
        Passenger[][] boardingGroups = new Passenger[groupsNumber][];
        for (int i = 0; i < groupsNumber; i++) {
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

        // make enterPlane events
        for (int i = 0; i<groupsNumber; i++){
            for (int j = 0; j < boardingGroups[i].length; j++){
                int boardingTime = i * 600 + j * 5; // two constants here - time per group, time per passenger.
                Event boardingEvent = new Event(EventTypes.ENTERPLANE, boardingTime, boardingGroups[i][j]);
                eventsQueue.add(boardingEvent);
            }
        }

        // default return
        return 0;
    }
}