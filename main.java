import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// next steps
//family seating - clustering together
// add functionality for multiple queues // fuck no lol
// working on ga at high level control - dynamic parameters


public class main {
    public static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static Simulation[] allSimulations;
    public static Passenger[] globalPassengers;
    public static Plane globalPlane;

    public static void main(String[] args) {
        Plane plane = new Plane(20, 6, 14, 6, new int[]{3, 3}, new int[]{0, 7}, "Boeing 737");
        globalPlane = plane;
        System.out.println(plane.getCapacity());


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
             //(int) Math.abs(RandomProvider.rand.nextGaussian()) * 3 + 3
            allSimulations[i] = new Simulation(allPassengers, plane, parameters.MAX_GROUPS);
             final int idx = i;
            futures.add(executor.submit(() -> allSimulations[idx].simulateBoardingTime()));
        }
        for (Future<Integer> future: futures){
            try {future.get();} catch (Exception e) { e.printStackTrace(); }
        } 

        // Highest level simulation control
        for (int i = 0; i < NUMBER_GENERATIONS; i++){
            allSimulations = evolution(allSimulations);

            Simulation w = findQuickest(allSimulations)[0];
            simulationWindow.refreshPlaneView(w.getBoardingInts());
/*             try {Thread.sleep(0);} catch (Exception e){} */
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
            if (RandomProvider.rand.nextDouble()<parameters.PROBABILITY_DISABLED){
                factor *= 0.6;
            }
            if (RandomProvider.rand.nextDouble()<parameters.PROBABILITY_OLD | RandomProvider.rand.nextDouble()<parameters.PROBABILITY_CHILDREN) {
                factor *= 0.8;
            }
            if (RandomProvider.rand.nextDouble()<parameters.PROBABILITY_BAGS) {
                bags = RandomProvider.rand.nextInt(parameters.MAX_BAGS + 1);
                factor *= 1 - (0.2 * bags);
            }
            double walkingSpeed = Math.min(3, Math.abs(RandomProvider.rand.nextGaussian())) / factor + parameters.DEFAULT_WALKING_SPEED; // 2 is the standard deviation
            double stowingSpeed = bags * Math.min(3, Math.abs(RandomProvider.rand.nextGaussian())) / factor + parameters.DEFAULT_STOWING_SPEED;
            double SittingSpeed = Math.min(3, Math.abs(RandomProvider.rand.nextGaussian()))/ factor + parameters.DEFAULT_SITTING_SPEED;
            allPassengers[i] = new Passenger(i, walkingSpeed, stowingSpeed, SittingSpeed, null, bags);
        }

        // GENERATE FAMILIES

        int familyPassengers = (int) (parameters.PROBABILITY_FAMILIES*numberPassengers);
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
                // get distance from aisle
                int distance = Math.abs(plane.getAisles()[0] - j); // only set up to deal with one aisle for the foreseeable
                passenger.setSittingSpeed(passenger.getSittingSpeed() * distance);
                p++;
            }
        }
    }



    //Genetic algorithm stuff

    static Simulation[] evolution(Simulation[] simulations){
        int ELITE_SIMULATIONS = (int) ((double) simulations.length * parameters.ELITISM);
        int selectionCount = (int)(parameters.SELECTION_POOL * simulations.length);
        Simulation[] fittestSimulations = Arrays.copyOfRange(findQuickest(simulations), 0, selectionCount);
        Simulation[] newPopulation = new Simulation[simulations.length];

        // new random ones for diversity
        int newSimulations = (int)((double) newPopulation.length * parameters.NEW_SIMULATIONS);
        int crossoversNeeded = newPopulation.length - newSimulations - ELITE_SIMULATIONS;
        if (crossoversNeeded + newSimulations + ELITE_SIMULATIONS != newPopulation.length) {
            System.out.println("feck");
        }

        for (int i = 0; i < newSimulations; i++) {      
            Simulation newSim = new Simulation(globalPassengers, globalPlane, RandomProvider.rand.nextInt(3, parameters.MAX_GROUPS + 1));
            newPopulation[i] = newSim;
        }

        // crossovers
        List<Future<Simulation>> crossoverFutures = new ArrayList<>();
        for (int i = 0; i < crossoversNeeded; i++) {
            crossoverFutures.add(executor.submit(() -> crossover(fittestSimulations)));
        }
        int idx = newSimulations;
        for (Future<Simulation> future : crossoverFutures) {
            try {
                Simulation child = future.get();
                if (child == null) {
                    // If crossover failed, try again (redundant)
                    crossoverFutures.add(executor.submit(() -> crossover(fittestSimulations)));
                    continue;
                }
                newPopulation[idx++] = child;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // elistism
        for (int i = 0; i < ELITE_SIMULATIONS; i++) {
            newPopulation[newPopulation.length - ELITE_SIMULATIONS + i] = fittestSimulations[i];
        }

        // mutation (but not elites)
        for (int i = 0; i < newPopulation.length - ELITE_SIMULATIONS; i++) {
            if (RandomProvider.rand.nextDouble() < parameters.MUTATION && newPopulation[i] != null) {
                newPopulation[i].mutate();
            }
        }

        // sBT() 
        List<Future<Integer>> futures = new ArrayList<>();
        for (Simulation simulation : newPopulation) {
            futures.add(executor.submit(simulation::simulateBoardingTime)); // call 
        }
        for (Future<Integer> future: futures) {
            try {
                int duration = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return newPopulation;
    }

    static Simulation selection(Simulation[] simulations){
        // using tournament style selection
        Simulation best = null;
        for (int i = 0; i < parameters.TOURNAMENT_SIZE; i++) {
            Simulation candidate = simulations[RandomProvider.rand.nextInt(simulations.length)];
            if (best == null || candidate.getDuration() < best.getDuration()) {
                best = candidate;
            }
        }
        return best;
    }

    static Simulation crossover(Simulation[] simulations){
        Simulation s1 = selection(simulations);
        Simulation s2 = selection(simulations);
        int length = s1.getPassengers().length;
/*         int groupsNum = s1.getNumberGroups();
        if (RandomProvider.rand.nextDouble()>0.5){
            groupsNum = s2.getNumberGroups();
        }
        Simulation child = new Simulation(globalPassengers, globalPlane, groupsNum);
        child.setNumberGroups(groupsNum); */
        Simulation child = new Simulation(globalPassengers, globalPlane, parameters.MAX_GROUPS);
        int[] childGroups = new int[length];

        int index = 0;
        while(length-index >0 ){
            int blockSize = 0;
            if (length-index <= 4){
                blockSize = length-index;
            } else {
            blockSize = RandomProvider.rand.nextInt(4, Math.min(8, length-index));
            }
            Simulation parent = s1;
            if (RandomProvider.rand.nextDouble()>0.5) {
                parent = s2;
            }
            for (int i = index; i<index+blockSize; i++){
                childGroups[i] = parent.getBoardingInts()[i];
            }
            index += blockSize;

        }
        child.setBoardingInts(childGroups);
        if (child.splitFamilies()) {
            // two choices - return null, or fix it for them.
/*             child.joinFamilies(); */
        }

        return child;
    }

    static Simulation[] findQuickest(Simulation[] simulations) {
        Simulation[] sorted = Arrays.copyOf(simulations, simulations.length);
        Arrays.sort(sorted, (s1, s2) -> Integer.compare(s1.getDuration(), s2.getDuration()));
        return sorted;
    }
}
