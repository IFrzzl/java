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
/*         boardingInts = new int[]{12, 12, 12, 12, 
12, 12, 12, 12, 
12, 12, 12, 12, 
11, 11, 11, 11, 
11, 11, 11, 11, 
11, 11, 11, 11,
7, 8, 9, 9, 8, 7,
2, 4, 6, 6, 4, 2,
0, 3, 5, 5, 3, 0,
7, 8, 9, 9, 8, 7,
2, 4, 6, 6, 4, 2,
0, 3, 5, 5, 3, 0,
7, 8, 9, 9, 8, 7,
2, 4, 6, 6, 4, 2,
0, 3, 5, 5, 3, 0,
7, 8, 9, 9, 8, 7,
0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; */
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
        int target1 = RandomProvider.rand.nextInt(allPassengers.length);
        int target2 = RandomProvider.rand.nextInt(allPassengers.length);
        
        Passenger passenger1 = allPassengers[target1];
        int[] family1 = passenger1.getFamily();
        int newGroup1 = boardingInts[target2];
        if (family1 != null){
            for (int relative: family1){
                boardingInts[relative] = newGroup1;
            }
        }
        boardingInts[passenger1.getIndex()] = newGroup1;

        Passenger passenger2 = allPassengers[target2];
        int[] family2 = passenger1.getFamily();
        int newGroup2 = boardingInts[target1];
        if (family2 != null){
            for (int relative: family2){
                boardingInts[relative] = newGroup2;
            }
        }
        boardingInts[passenger2.getIndex()] = newGroup2;

        simulateBoardingTime();
    }

    public int simulateBoardingTime() {
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
            ticksElapsed = parameters.SPLIT_PENALTY;
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
                if (boardingQueue.peek() == null){
                    System.out.println("WHAT THE FUCK"); // flip out
                } else {
                    if (aisle.push(boardingQueue.peek()) != -1){
                        int queuePosition = 0;
    /*                     if (boardingQueue.peek().getBags()>1) {
                            queuePosition = 1;
                        } */
                        Event boardingEvent = new Event(EventTypes.WALK, time + 2, boardingQueue.poll(), queuePosition);
                        eventsQueue.add(boardingEvent);
                    }
                }
            }
            

            if (time>ticksElapsed) {ticksElapsed = time;}
        }

        // default return
        this.duration = (int) ticksElapsed;
        return duration;
    }

    public int getDuration(){
        return duration;
    }
}