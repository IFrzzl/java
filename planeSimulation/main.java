package planeSimulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class main {
    private static final int WORKER_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    public static ExecutorService executor = Executors.newFixedThreadPool(WORKER_THREADS, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        // stop my desktop env crashing D: :D
        try { t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1)); } catch (Exception e) {}
        t.setName("plane simulation :3" + THREAD_COUNTER.getAndIncrement());
        return t;
    });
    private static Simulation[] allSimulations;
    public static SimulationWindow simulationWindow;

    public static long startTime = 0;

    public static void main(String[] args) {
        Plane plane = parameters.plane;
        simulationWindow = new SimulationWindow(parameters.plane); // using default plane
        parameters.allPassengers = new Passenger[plane.getCapacity()];
        generatePassengerData();
        simulationWindow.setPlaneView();

        while (true) {
            while (!parameters.STARTED) {
                if (parameters.REDRAW) {
                    plane = parameters.plane;
                    simulationWindow.replacePlane(plane);
                    parameters.REDRAW = false;
                } else if (parameters.RESET) {
                    // bring whole program back to start state using the existing controls instance
                    parameters.RESET = false;
                    simulationWindow.simulationControls.reset();           // reset parameter values and UI
                    simulationWindow.simulationControls.refreshGAControls(); // update GA sliders
                }
                try {Thread.sleep(10);} catch (InterruptedException e){}
            }
            startTime = System.nanoTime();
            parameters.END = false;
            plane = parameters.plane;
            
            parameters.allPassengers = new Passenger[plane.getCapacity()];
            generatePassengerData();
            simulationWindow.setPlaneView();

            System.out.println(plane.getCapacity());
            run();

        }
    }

    public static void run(){
        int NUMBER_SIMULATIONS = parameters.NUMBER_SIMULATIONS;
        int NUMBER_GENERATIONS = parameters.NUMBER_GENERATIONS;
        parameters.pool = 1;

        allSimulations = new Simulation[NUMBER_SIMULATIONS];
        ArrayList<Simulation> goodSimulations = new ArrayList<>();

        List<Future<Integer>> futures = new ArrayList<>();
        int currentDuration = 0;
        int staticGenerations = 0;
        for (int i = 0; i < NUMBER_SIMULATIONS; i++) {
             //(int) Math.abs(parameters.random.nextGaussian()) * 3 + 3
            allSimulations[i] = new Simulation(parameters.MAX_GROUPS);
            final int idx = i;
            futures.add(executor.submit(() -> allSimulations[idx].simulateBoardingTime()));
        }
        for (Future<Integer> future: futures){
            try {future.get();
            } catch (Exception e) { e.printStackTrace(); }
        } 

        // Highest level simulation control
        for (int i = 0; i < NUMBER_GENERATIONS; i++){
            if (parameters.END || parameters.RESET){return;}

            while (parameters.PAUSED) {try {
                if (parameters.END || parameters.RESET){return;}
                Thread.sleep(20);} catch (InterruptedException e) {}}
            parameters.PAUSED = false;
            allSimulations = evolution(allSimulations);

            try {Thread.sleep((int) parameters.delay * 1000);} catch (Exception e){/*lol */}

            Simulation w = findQuickest(allSimulations)[0];
            if (w.getDuration() == currentDuration){
                    staticGenerations++;
                } else{
                    currentDuration = w.getDuration();
                    staticGenerations = 0;

                }
            parameters.NEW_SIMULATIONS = Math.min(Math.max(0.1, staticGenerations / 8) * 1, 0.3);
            parameters.MUTATION = Math.min(0.15, Math.max(0.1, 0.05*staticGenerations));
            if (i>10 && staticGenerations == 0){parameters.ELITISM += 0.01;} else {parameters.ELITISM = 0.03;}

            simulationWindow.refreshPlaneView(w.getBoardingInts());
/*             try {Thread.sleep(0);} catch (Exception e){} */
            System.out.println("Generation " + i + " complete. Current winning time: " + w.getDuration() + ". Static gens: " + staticGenerations + ". New sims: " + parameters.NEW_SIMULATIONS + ". Mutation rate: " + parameters.MUTATION);
            System.out.println("Simulation random penalty: " + w.randomPenalty);
            System.out.println(parameters.MAX_GROUPS + "");
            Simulation worst = findQuickest(allSimulations)[allSimulations.length-1];
            simulationWindow.simulationControls.updateGeneration(i, w, worst, staticGenerations, startTime);
            simulationWindow.simulationControls.refreshGAControls(); // sliders go brrr

            if (staticGenerations >= 50 || parameters.SKIP){
                parameters.SKIP = false;
                goodSimulations.add(w);
                parameters.pool = goodSimulations.size() + 1;
                // if stuff really isn't changing, keep the winner and generate a whole new batch
                allSimulations = new Simulation[NUMBER_SIMULATIONS];
                staticGenerations = 0;
                currentDuration = 0;
                futures.clear();
                for (int j = 0; j < NUMBER_SIMULATIONS; j++) {
                    allSimulations[j] = new Simulation(parameters.MAX_GROUPS);
                    final int k = j;
                    futures.add(executor.submit(() -> allSimulations[k].simulateBoardingTime()));
                }
                // need to make sure these are retreived or there's a weird "violates contract" error
                for (Future<Integer> future: futures){
                    try {future.get();
                    } catch (Exception e) { e.printStackTrace(); }
                } 
            }  

            if (i == parameters.NUMBER_GENERATIONS -1){
                goodSimulations.add(w);
            }
        }

        Simulation winner = findQuickest(goodSimulations.toArray(new Simulation[0]))[0];
        simulationWindow.refreshPlaneView(winner.getBoardingInts());  

        parameters.END = true;
        parameters.STARTED = false;
        simulationWindow.simulationControls.repaint();
        
    }

    static void generatePassengerData() {
        
        // RANDOM ATTRIBUTES FOR EACH PASSENGER

        int numberPassengers = parameters.plane.getCapacity();
        for (int i = 0; i < parameters.allPassengers.length; i++){
            int bags = 0;
            double factor = 1.0;
            if (parameters.random.nextDouble()<parameters.PROBABILITY_DISABLED){
                factor *= 0.6;
            }
            if (parameters.random.nextDouble()<parameters.PROBABILITY_OLD | parameters.random.nextDouble()<parameters.PROBABILITY_CHILDREN) {
                factor *= 0.8;
            }
            if (parameters.random.nextDouble()<parameters.PROBABILITY_BAGS) {
                bags = parameters.random.nextInt(parameters.MAX_BAGS + 1);
                factor *= 1 - (0.2 * bags);
            }
            double walkingSpeed = Math.min(3, Math.abs(parameters.random.nextGaussian())) / factor + parameters.DEFAULT_WALKING_SPEED; // 2 is the standard deviation
            double stowingSpeed = bags * Math.min(3, Math.abs(parameters.random.nextGaussian())) / factor + parameters.DEFAULT_STOWING_SPEED;
            double SittingSpeed = Math.min(3, Math.abs(parameters.random.nextGaussian()))/ factor + parameters.DEFAULT_SITTING_SPEED;
            parameters.allPassengers[i] = new Passenger(i, walkingSpeed, stowingSpeed, SittingSpeed, null, bags);
        }

        // GENERATE FAMILIES

        int familyPassengers = (int) (parameters.PROBABILITY_FAMILIES*numberPassengers);
        while (familyPassengers>0){
            int size = 0;
            if (familyPassengers > 7){
                size = parameters.random.nextInt(1, 5) + 2;
            } else { size = familyPassengers;}

            int[] family = new int[size];
            int startIndex = parameters.random.nextInt(parameters.allPassengers.length);
            for (int i = 0; i < size; i++){
                if (startIndex + size - 1 >= parameters.allPassengers.length || parameters.allPassengers[startIndex+i].getFamily() != null ){break;}
                family[i] = startIndex+i;
            }
            for (int passenger: family){
                parameters.allPassengers[passenger].setFamily(family);
            }
            familyPassengers -= family.length;
        }

        // assign seats
        Seat[][] seatingChart = parameters.plane.getSeatingChart();
        int rows = parameters.plane.getLength();
        int cols = parameters.plane.getWidth();
        int p = 0; // this requires the plane capacity to be correct
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Seat seat = seatingChart[i][j];
                if (seat.getStatus() == SeatStatus.OTHER || seat.getStatus() == SeatStatus.AISLE) {continue;}
                Passenger passenger = parameters.allPassengers[p];
                passenger.setSeat(seat);
                // get distance from aisle
                int distance = Math.abs(parameters.plane.getAisles()[0] - j); // only set up to deal with one aisle for the foreseeable
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

        for (int i = 0; i < newSimulations; i++) {      
            Simulation newSim = new Simulation(parameters.random.nextInt(1, parameters.MAX_GROUPS + 1));
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
                // crossover() should always return a non-null, sanitized child
                if (child == null) {
                    Simulation parent = fittestSimulations[parameters.random.nextInt(Math.max(1, fittestSimulations.length))];
                    child = new Simulation(parent.getNumberGroups());
                    int[] ints = parent.getBoardingInts();
                    if (ints != null) child.setBoardingInts(Arrays.copyOf(ints, ints.length));
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
            if (parameters.random.nextDouble() < parameters.MUTATION && newPopulation[i] != null) {
                newPopulation[i].mutate();
            }
        }

        // simulating boarding times
        // ensure newPopulation contains no nulls — fill with clones of the selection pool (fittestSimulations) if needed
        for (int i = 0; i < newPopulation.length; i++) {
            if (newPopulation[i] == null) {
                if (fittestSimulations.length > 0) {
                    Simulation src = fittestSimulations[parameters.random.nextInt(fittestSimulations.length)];
                    Simulation c = new Simulation(src.getNumberGroups());
                    int[] ints = src.getBoardingInts();
                    if (ints != null) c.setBoardingInts(Arrays.copyOf(ints, ints.length));
                    newPopulation[i] = c;
                } else {
                    newPopulation[i] = new Simulation(parameters.random.nextInt(2, parameters.MAX_GROUPS + 1));
                }
            }
        }

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < newPopulation.length; i++) {
            final int taskIndex = i;
            Simulation simulation = newPopulation[i];
            // double-check and sanitize before run
            simulation.sanitizeGroups();
            if (simulation.splitFamilies()) simulation.joinFamilies();
            futures.add(executor.submit(() -> {
                try {
                    return simulation.simulateBoardingTime();
                } catch (Throwable t) {
                    System.out.println("[EXC] Simulation task " + taskIndex + " threw: " + t);
                    t.printStackTrace();
                    throw t;
                }
            }));
        }

        for (Future<Integer> future : futures) {
            try { future.get(); } catch (Exception e) { e.printStackTrace(); }
        }

        return newPopulation;
    }

    static Simulation selection(Simulation[] simulations){
        // using tournament style selection
        Simulation best = null;
        for (int i = 0; i < parameters.TOURNAMENT_SIZE; i++) {
            Simulation candidate = simulations[parameters.random.nextInt(simulations.length)];
            if (best == null || candidate.getFitness() < best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }

    static Simulation crossover(Simulation[] simulations){
        Simulation s1 = selection(simulations);
        Simulation s2 = selection(simulations);
        int length = parameters.allPassengers.length;

        int groupsNum = s1.getNumberGroups();
        if (parameters.random.nextDouble()>0.5){
            groupsNum = s2.getNumberGroups();
        }
        Simulation child = new Simulation(groupsNum);
        int[] childGroups = new int[length];

        int index = 0;
        while(length-index >0 ){
            int blockSize = 0;
            if (length-index <= 4){
                blockSize = length-index;
            } else {
            blockSize = parameters.random.nextInt(4, Math.min(8, length-index));
            }
            Simulation parent = s1;
            if (parameters.random.nextDouble()>0.5) {
                parent = s2;
            }
            for (int i = index; i<index+blockSize; i++){
                childGroups[i] = parent.getBoardingInts()[i];
            }
            index += blockSize;

        }
        child.setBoardingInts(childGroups);
        // make sure group labels are valid for this child (parent labels might be out of range)
        child.sanitizeGroups();
        // repair family splits rather than returning null — keeps evolution running smoothly
        if (child.splitFamilies()) {
            child.joinFamilies();
        }

        return child;
    }

    // cloneSimulation removed — cloning now inlined where used (simpler flow)

    static Simulation[] findQuickest(Simulation[] simulations) {
        Simulation[] sorted = Arrays.copyOf(simulations, simulations.length);
        if (parameters.WORSTFIND){
            Arrays.sort(sorted, (s1, s2) -> Integer.compare(s2.getFitness(), s1.getFitness()));
        } else {
            Arrays.sort(sorted, (s1, s2) -> Integer.compare(s1.getFitness(), s2.getFitness()));
        }
        return sorted;
    }
}
