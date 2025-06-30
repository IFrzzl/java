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
    public static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static Simulation[] allSimulations;
    public static Passenger[] globalPassengers;
    public static Plane globalPlane;

    public static void main(String[] args) {
        Plane plane = new Plane(20, 6, 14, 6, new int[]{3, 3}, new int[]{0, 7}, "Boeing 737");
        globalPlane = plane;


        final int NUMBER_SIMULATIONS = 1000;
        final int NUMBER_GENERATIONS = 10000;
        SimulationWindow simulationWindow = new SimulationWindow(plane);
        
        Passenger[] allPassengers = new Passenger[plane.getCapacity()];
        generatePassengerData(plane, allPassengers);
        globalPassengers = allPassengers;
        simulationWindow.setPlaneView(plane, allPassengers);

        allSimulations = new Simulation[NUMBER_SIMULATIONS];

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < NUMBER_SIMULATIONS; i++) {
            allSimulations[i] = new Simulation(allPassengers, plane, RandomProvider.rand.nextInt(3, constants.MAX_GROUPS+1));
             final int idx = i;
            futures.add(executor.submit(() -> allSimulations[idx].simulateBoardingTime()));
        }
        for (Future<Integer> future: futures){
            try {future.get();} catch (Exception e) { e.printStackTrace(); }
        } 

        // Genetic algorithm generations
        for (int i = 0; i < NUMBER_GENERATIONS; i++){
            allSimulations = evolution(allSimulations);

            Simulation w = findQuickest(allSimulations)[0];
            simulationWindow.refreshPlaneView(w.getBoardingInts());
            try {Thread.sleep(0);} catch (Exception e){}
            System.out.println("Generation " + i + " complete. Current winning time: " + findQuickest(allSimulations)[0].getDuration());
        }
        executor.shutdown();

        Simulation winner = findQuickest(allSimulations)[0];
        simulationWindow.refreshPlaneView(winner.getBoardingInts());  
        System.out.println(winner.getBoardingInts());

    }
    static void generatePassengerData(Plane plane, Passenger[] allPassengers) {
        
        // RANDOM ATTRIBUTES FOR EACH PASSENGER

        int numberPassengers = plane.getCapacity();
        for (int i = 0; i < allPassengers.length; i++){
            int bags = 0;
            double factor = 1.0;
            if (RandomProvider.rand.nextDouble()<constants.PROBABILITY_DISABLED){
                factor *= 0.6;
            }
            if (RandomProvider.rand.nextDouble()<constants.PROBABILITY_OLD | RandomProvider.rand.nextDouble()<constants.PROBABILITY_CHILDREN) {
                factor *= 0.8;
            }
            if (RandomProvider.rand.nextDouble()<constants.PROBABILITY_BAGS) {
                bags = RandomProvider.rand.nextInt(constants.MAX_BAGS + 1);
                factor *= 1 - (0.2 * bags);
            }
            double walkingSpeed = Math.abs(RandomProvider.rand.nextGaussian()) * 2.0 * 1/factor + constants.DEFAULT_WALKING_SPEED; // 2 is the standard deviation
            double towingSpeed = bags * (Math.abs(RandomProvider.rand.nextGaussian()) * 2.0 * 1/factor + constants.DEFAULT_STOWING_SPEED);
            double SittingSpeed = Math.abs(RandomProvider.rand.nextGaussian()) * 2.0 * 1/factor + constants.DEFAULT_SITTING_SPEED;
            allPassengers[i] = new Passenger(i, walkingSpeed, towingSpeed, SittingSpeed, null, bags);
        }

        // GENERATE FAMILIES

        int familyPassengers = (int) (constants.PROBABILITY_FAMILIES*numberPassengers);
        while (familyPassengers>0){
            int size = 0;
            if (familyPassengers > 7){
                size = RandomProvider.rand.nextInt(1, 5) + 2;
            } else { size = familyPassengers;}

            int[] family = new int[size];
            int startIndex = RandomProvider.rand.nextInt(allPassengers.length);
            for (int i = 0; i < size; i++){
                if (startIndex + size - 1 >= allPassengers.length || allPassengers[startIndex+i].getFamily() != null ){continue;}
                family[i] = startIndex+i;
            }
            for (int passenger: family){
                allPassengers[passenger].setFamily(family);
            }
            familyPassengers -= size;
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
                p++;
            }
        }
    }



    // --- Genetic algorithm stuffs ---

    static Simulation[] evolution(Simulation[] simulations){
        int ELITE_SIMULATIONS = (int) (constants.ELITISM*simulations.length);
        int selectionCount = (int)(constants.SELECTION_POOL * simulations.length) + 1;
        Simulation[] fittestSimulations = Arrays.copyOfRange(findQuickest(simulations), 0, selectionCount);
        Simulation[] newPopulation = new Simulation[simulations.length];

        // Parallel crossovers
        List<Future<Simulation>> crossoverFutures = new ArrayList<>();
        int newSimulations = (int)((double) newPopulation.length * (1-constants.NEW_SIMULATIONS));
        int crossoversNeeded = newPopulation.length - newSimulations - ELITE_SIMULATIONS;

        for (int i = 0; i<newSimulations; i++){
            
            Simulation newSim = new Simulation(globalPassengers, globalPlane, RandomProvider.rand.nextInt(3, constants.MAX_GROUPS+1));
            newPopulation[i] = newSim;
            crossoverFutures.add(executor.submit(()-> newSim.simulateBoardingTime))
        }

        for (int i = 0; i < crossoversNeeded; i++) {
            crossoverFutures.add(executor.submit(() -> crossover(fittestSimulations)));
        }
        int index = ELITE_SIMULATIONS + newSimulations - 1;
        while (crossoversNeeded > 0) {
                Future<Simulation> future = crossoverFutures.getFirst();
                try {
                    Simulation child = future.get();
                    if (child == null) {
                        crossoverFutures.add(executor.submit(() -> crossover(fittestSimulations)));
                        continue;
                    }
                    newPopulation[index++] = child;
                    crossoversNeeded--;
                    if (index >= newPopulation.length) break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        

        for (Simulation simulation : newPopulation) {
            if (RandomProvider.rand.nextDouble() < constants.MUTATION && simulation != null) {
                simulation.mutate();
            }

        // Elitism - keep the very best
        for (int i = 0; i < ELITE_SIMULATIONS; i++) {
            newPopulation[i] = fittestSimulations[i];
        }
        }

        return newPopulation;
    }

        static Simulation selection(Simulation[] simulations){
            // using tournament style selection
            Simulation best = null;
            for (int i = 0; i < constants.TOURNAMENT_SIZE; i++) {
                Simulation candidate = simulations[RandomProvider.rand.nextInt(simulations.length)];
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
            int length = s1.getPassengers().length;
            int groupsNum = s1.getNumberGroups();
            if (RandomProvider.rand.nextDouble()>0.5){
                groupsNum = s2.getNumberGroups();
            }
            Simulation child = new Simulation(globalPassengers, globalPlane, groupsNum);
            int[] childGroups = new int[length];
            for (int i = 0; i < length; i++) {
                if (RandomProvider.rand.nextDouble()>0.5) {
                    childGroups[i] = s1.getBoardingInts()[i];
                } else {
                    childGroups[i] = s2.getBoardingInts()[i];
                }
            }
            child.setBoardingInts(childGroups);
            if (child.splitFamilies()) {
                // two choices - return null, or fix it for them.
                child.joinFamilies();
            }
            child.simulateBoardingTime();
            return child;
    }

    static Simulation[] findQuickest(Simulation[] simulations) {
        Simulation[] sorted = Arrays.copyOf(simulations, simulations.length);
        Arrays.sort(sorted, (s1, s2) -> Integer.compare(s1.getDuration(), s2.getDuration()));
        return sorted;
    }
}