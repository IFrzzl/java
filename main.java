import java.util.ArrayList;
import java.util.Random;
import java.awt.Color;

public class main {
     public static void main(String[] args) {

        Plane plane = new Plane(32, 8, 24, 10, new int[]{3, 4, 3}, new int[]{5 ,9, 12, 15}, "Boeing 737");
        final int MAX_GROUPS = 2;
        final int NUMBER_SIMULATIONS = 100;
         SimulationWindow simulationWindow = new SimulationWindow(plane);

       simulationWindow.planeView.setBackground(Color.WHITE); 
        simulationWindow.refreshPlaneView();
        ArrayList<Passenger> allPassengers = new ArrayList<>();
        generatePassengerData(plane, allPassengers);
        ArrayList<Simulation> simulations = new ArrayList<>();
        for (int i = 0; i<NUMBER_SIMULATIONS; i++){
            Simulation simulation = new Simulation();
            int[] initialGroups = simulation.generateInitialGroups(allPassengers, MAX_GROUPS);
            simulation.simulateBoardingTime(allPassengers, plane, initialGroups, MAX_GROUPS);
            simulations.add(simulation);

        }
        simulations = findQuickest(simulations);
        for (Simulation simulation: simulations){
            System.out.println(simulation.getDuration());
        }
        System.out.println(simulations.get(simulations.size()-1).getDuration() - simulations.get(0).getDuration());
        // so - sBT acts as a temperature fuction for the GA
    }

    public static ArrayList<Simulation> findQuickest(ArrayList<Simulation> simulations) {
        simulations.sort((s1, s2) -> Integer.compare(s1.getDuration(), s2.getDuration()));
        return simulations;
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
            passenger.id = i;
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
            for (int i = index; i < index + familySize && i < numberPassengers; i++) { // fancy double conditional
                if (allPassengers.get(i).getFamily() != null) continue;
                family.add(allPassengers.get(i));
            }
            if (family.size() == familySize) {
                for (Passenger relative : family) {
                    ArrayList<Passenger> familyCopy = new ArrayList<>(family);
                    familyCopy.remove(relative); // no circular references = no commodification errors
                    relative.setFamily(familyCopy);
                }
                familyPassengers -= familySize;
            }
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
}
