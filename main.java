import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.awt.Color;

// next steps
//family seating - clustering together
// add functionality for multiple queues
// add taking longer time for stepping over occupied seats
// add seatButton to seat class for easier changing
// it seems that more families has a big effect on performance



public class main {
    public static Random rand = new Random();
    public static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static Simulation[] allSimulations;

    public static void main(String[] args) {
        Plane plane = new Plane(16, 4, 12, 6, new int[]{3, 3}, new int[]{5 ,9, 12, 15}, "Boeing 737");
        final int MAX_GROUPS = 6;
        final int NUMBER_SIMULATIONS = 25;

        /*         SimulationWindow simulationWindow = new SimulationWindow(plane);

        simulationWindow.planeView.setBackground(Color.WHITE); 
        simulationWindow.refreshPlaneView(); */
        Passenger[] allPassengers = new Passenger[plane.getCapacity()];
        generatePassengerData(plane, allPassengers);

        allSimulations = new Simulation[NUMBER_SIMULATIONS];

        // Initial batch: parallelize simulateBoardingTime
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < NUMBER_SIMULATIONS; i++) {
            allSimulations[i] = new Simulation(deepCopy(allPassengers), plane, MAX_GROUPS);
            allSimulations[i].generateInitialGroups();
            final int idx = i;
            futures.add(executor.submit(() -> allSimulations[idx].simulateBoardingTime()));
        }
        for (Future<Integer> future: futures){
            try {future.get();} catch (Exception e) { e.printStackTrace(); }
        }

        System.out.println("hello sexy man");
        // Genetic algorithm generations
        final int NUMBER_GENERATIONS = 250;
        for (int i = 0; i < NUMBER_GENERATIONS; i++){
            allSimulations = evolution(allSimulations);

            // Parallelize simulateBoardingTime for new population
            List<Future<Integer>> genFutures = new ArrayList<>();
            for (int j = 0; j < allSimulations.length; j++) {
                final int idx = j;
                genFutures.add(executor.submit(() -> allSimulations[idx].simulateBoardingTime()));
            }
            for (Future<Integer> future: genFutures){
                try {future.get();} catch (Exception e) { e.printStackTrace(); }
            }

            System.out.println("Generation " + i + " complete. Current winning time: " + findQuickest(allSimulations)[0].getDuration());
        }
        executor.shutdown();
    }
    static Passenger[] deepCopy(Passenger[] originals){
        Passenger[] copy = new Passenger[originals.length];
        for (int i = 0; i < originals.length; i++) {
            copy[i] = new Passenger(originals[i]);
        }
        return copy;
    }
    static void generatePassengerData(Plane plane, Passenger[] allPassengers) {
        // we're making a set of variables to control walking, stowing, and sitting speeds
        // these are the probabilities of a passenger being/having

        final double families = 0.20;
        int numberPassengers = plane.getCapacity();
        for (int i = 0; i < allPassengers.length; i++){
            Passenger passenger = new Passenger();
            int bags = 0;
            double factor = 1.0;
            if (rand.nextDouble()<constants.PROBABILITY_DISABLED){
                factor *= 0.6;
            }
            if (rand.nextDouble()<constants.PROBABILITY_OLD | rand.nextDouble()<constants.PROBABILITY_CHILDREN) {
                factor *= 0.8;
            }
            if (rand.nextDouble()<constants.PROBABILITY_BAGS) {
                bags = rand.nextInt(constants.MAX_BAGS + 1);
                factor *= 1 - (0.2 * bags);
            }
            passenger.setWalkingSpeed(rand.nextGaussian() * 2.0 * 1/factor + constants.DEFAULT_WALKING_SPEED); // 2 is the standard deviation
            passenger.setStowingSpeed(bags * (rand.nextGaussian() * 2.0 * 1/factor + constants.DEFAULT_STOWING_SPEED));
            passenger.setSittingSpeed(rand.nextGaussian() * 2.0 * 1/factor + constants.DEFAULT_SITTING_SPEED);
            passenger.setBags(bags);
            allPassengers[i] = passenger;
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
                if (allPassengers[i].getFamily() != null) continue;
                family.add(allPassengers[i]);
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
                Seat seat = seatingChart[i][j];
                if (seat.getStatus() == SeatStatus.OTHER || seat.getStatus() == SeatStatus.AISLE) {continue;}
                Passenger passenger = allPassengers[p];
                passenger.setSeat(seat);
                seat.setStatus(SeatStatus.OCCUPIED);
                seat.setPassenger(passenger);
                p++;
            }
        }
    }

    // --- Genetic algorithm stuffs ---

static Simulation[] evolution(Simulation[] simulations){
    final int ELITE_SIMULATIONS = 5;
    final double SELECTION_RATE = 0.7;
    int selectionCount = (int)(SELECTION_RATE * simulations.length) + 1;
    Simulation[] fittestSimulations = Arrays.copyOfRange(findQuickest(simulations), 0, selectionCount);
    Simulation[] newPopulation = new Simulation[simulations.length];

    // Elitism - keep the very best
    for (int i = 0; i < ELITE_SIMULATIONS; i++) {
        newPopulation[i] = fittestSimulations[i];
    }
    System.out.println("elitism");

    // Parallel crossovers
    List<Future<Simulation>> crossoverFutures = new ArrayList<>();
    int crossoversNeeded = newPopulation.length - ELITE_SIMULATIONS;
    for (int i = 0; i < crossoversNeeded; i++) {
        crossoverFutures.add(executor.submit(() -> crossover(fittestSimulations)));
    }

    int idx = ELITE_SIMULATIONS;
    for (int i = 0; i<crossoversNeeded; i++) {
        Future<Simulation> future = crossoverFutures.get(i);
        try {
            Simulation child = future.get();
            if (child == null) {i--; continue;} // rerun this until you get one with no split families
            newPopulation[idx++] = child;
            if (idx >= newPopulation.length) break;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    System.out.println("crossovers");

    // Mutate (can also be parallelized if desired)
    for (Simulation simulation : newPopulation) {
        if (rand.nextDouble() < 0.05 && simulation != null) {
            mutate(simulation);
        }
    }

    return newPopulation;
}

    static Simulation selection(Simulation[] simulations){
        // using tournament style selection
        Simulation best = null;
        for (int i = 0; i < 4; i++) {
            Simulation candidate = simulations[rand.nextInt(simulations.length)];
            if (best == null || candidate.getDuration() < best.getDuration()) {
                best = candidate;
            }
        }
        return best;
    }

    // this is a problem child, and its handling.
    static Simulation crossover(Simulation[] simulations){
        Simulation s1 = selection(simulations);
        Simulation s2 = selection(simulations);
        int cutoff = rand.nextInt(s1.length);
        Simulation child = new Simulation(deepCopy(s1.getPassengers()), s1.getPlane(), s1.getNumberGroups());
        int[] childGroups = new int[s1.length];
        for (int i = 0; i < s1.length; i++) {
            if (i < cutoff) {
                childGroups[i] = s1.getBoardingInts()[i];
            } else {
                childGroups[i] = s2.getBoardingInts()[i];
            }
        }
        child.setBoardingInts(childGroups);
        if (child.splitFamilies()) {
            return null;
        }
        child.simulateBoardingTime();
        return child;
    }

    static void mutate(Simulation simulation){
        int target = rand.nextInt(simulation.getLength());
        Passenger passenger = simulation.getPassengers()[target];
        ArrayList<Passenger> family = passenger.getFamily();
        if (family != null){
            int group = family.get(rand.nextInt(family.size())).getGroupNum();
            for (Passenger relative: family){
                relative.setGroupNum(group);
            }
        } else {
            passenger.setGroupNum(rand.nextInt(simulation.getNumberGroups()));
        }
        int[] bis = simulation.getBoardingInts();
        bis[target] = passenger.getGroupNum();
        simulation.setBoardingInts(bis);
    }

    static Simulation[] findQuickest(Simulation[] simulations) {
        Simulation[] sorted = Arrays.copyOf(simulations, simulations.length);
        Arrays.sort(sorted, (s1, s2) -> Integer.compare(s1.getDuration(), s2.getDuration()));
        return sorted;
    }
}