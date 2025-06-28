import java.util.*;

public class Simulation {
    int duration = 0;
    int[] boardingInts;
    int length = 0;
    Passenger[]allPassengers;
    Plane plane;
    int numberGroups;
    public Simulation(Passenger[]allPassengers, Plane plane, int numberGroups) {
        this.allPassengers = allPassengers;
        this.plane = plane;
        this.numberGroups = numberGroups;
        this.length = allPassengers.length;
        generateInitialGroups();
    }

    public int[] getBoardingInts() {
        return boardingInts;
    }
    public int getLength() {
        return length;
    }
    public int getNumberGroups() {
        return numberGroups;
    }
    public Passenger[]getPassengers(){
        return allPassengers;
    }
    public Plane getPlane(){
        return plane;
    }
    public void setBoardingInts(int[] bi){
        boardingInts = bi;
    }

    public void joinFamilies(){
        for (Passenger passenger: allPassengers){
            if (passenger.getFamily() != null) {
                int[] family = passenger.getFamily();
                int group = boardingInts[family[RandomProvider.rand.nextInt(family.length)]];
                for (int relative: family){
                    boardingInts[relative] = group;
                }
            }
        }
    }

    public void generateInitialGroups(){
        int[] groups = new int[length];

        for (int i = 0; i<length; i++){
            groups[i] = RandomProvider.rand.nextInt(numberGroups);
        }
        boardingInts = groups;
        joinFamilies();
    }

    public Boolean splitFamilies(){
        for (Passenger passenger:allPassengers){
            if (passenger.getFamily() != null) {
                int[] family = passenger.getFamily();
                for (int relative: family){
                    if (boardingInts[relative] != boardingInts[passenger.getIndex()]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void mutate(){
        int target = RandomProvider.rand.nextInt(allPassengers.length);
        Passenger passenger = allPassengers[target];
        int[] family = passenger.getFamily();
        int newGroup = RandomProvider.rand.nextInt(numberGroups);
        if (family != null){
            for (int relative: family){
                boardingInts[relative] = newGroup;
            }
        }
        boardingInts[passenger.getIndex()] = newGroup;
    }

    public int simulateBoardingTime() {
        long startTime = System.nanoTime();
        double ticksElapsed = 0;
        // make a priority queue for boarding events
        // custom comparator using lamba to sort by start time
        PriorityQueue<Event> eventsQueue = new PriorityQueue<>((e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));

        // sorting allPassengers by boarding group
        int[][] boardingGroups = new int[numberGroups][];
        for (int i = 0; i < numberGroups; i++) {
            ArrayList<Integer> members = new ArrayList<>();
            for (int j = 0; j<allPassengers.length; j++){
                if (boardingInts[j] == i){
                    members.add(j);
                }
            }
            boardingGroups[i] = new int[members.size()];
            for (int j = 0; j<members.size(); j++){
                boardingGroups[i][j] = members.get(j);
            }
        }

        // check if families are split up
        if (splitFamilies()){
            this.duration = 999999999;
            return 999999999;
        }

        //make the plane aisle
        AisleQueue aisle = new AisleQueue(plane.length + 2);

        Queue<Passenger> boardingQueue = new LinkedList<Passenger>();
        for (int i = 0; i<numberGroups; i++){
            for (int j = 0; j<boardingGroups[i].length; j++) {
                boardingQueue.add(allPassengers[boardingGroups[i][j]]);
            }
        }

        eventsQueue.add(new Event(EventTypes.WALK, 0, boardingQueue.poll(), 0)); // first passenger starts walking at time 0

        while (!eventsQueue.isEmpty()) {
            Event currentEvent = eventsQueue.poll();
            Passenger passenger = currentEvent.getPassenger();
            double time = currentEvent.getTime();
            int position = currentEvent.getPosition();
            switch (currentEvent.getType()) {
                case EventTypes.SITTING:
                    time += passenger.getSittingSpeed();
                    aisle.remove(position);
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
                    Event boardingEvent = new Event(EventTypes.WALK, time + 2, boardingQueue.poll(), queuePosition);
                    eventsQueue.add(boardingEvent);
                }
            }
            
/*             aisle.print(); */
/*             try {Thread.sleep(3000);} catch (Exception e){} */
            if (time>ticksElapsed) {ticksElapsed = time;}
        }

        // default return
        this.duration = (int) ticksElapsed;
/*         long endTime = System.nanoTime();
        long elapsed = (endTime - startTime);
        System.out.println(elapsed/1000000); */
        return duration;
    }

    public int getDuration(){
        return duration;
    }
}