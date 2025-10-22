import java.util.*;
import java.util.stream.*;

public class Simulation {
    int duration = 69;
    int[] boardingInts;
    int length = 0;
    Passenger[]allPassengers;
    Plane plane;
    int numberGroups;

    int randomPenalty = 0;
    int[] adjacentSameGroupSeats;
    double[] avgDistance;
    double[] sdDistance;

    public Simulation(Passenger[]allPassengers, Plane plane, int numberGroups) {
        this.allPassengers = allPassengers;
        this.plane = plane;
        this.numberGroups = numberGroups;
        this.length = allPassengers.length;
        generateInitialGroups();

        adjacentSameGroupSeats = new int[numberGroups];
        avgDistance = new double[numberGroups];
        sdDistance = new double[numberGroups];

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
        AisleQueue aisle = new AisleQueue(plane.getLength() + 2);

        Queue<Integer> boardingQueue = new LinkedList<Integer>();
        for (int i = 0; i<numberGroups; i++){
            for (int j = 0; j<boardingGroups[i].length; j++) {
                boardingQueue.add(boardingGroups[i][j]);
            }
        }
        eventsQueue.add(new Event(EventTypes.WALK, 0, boardingQueue.poll(), 0)); // first passenger starts walking at time 0

        while (!eventsQueue.isEmpty()) {

            Event currentEvent = eventsQueue.poll();
            int passenger = currentEvent.getPassenger();
            double time = currentEvent.getTime();
            int position = currentEvent.getPosition();
            switch (currentEvent.getType()) {
                case EventTypes.SITTING:
                    time += allPassengers[passenger].getSittingSpeed();
                    aisle.remove(position);
                    break;
                case EventTypes.WALK:
                    if (position >= allPassengers[passenger].getSeat().getRow()) {
                        eventsQueue.add(new Event(EventTypes.SITTING, time + allPassengers[passenger].getStowingSpeed(), passenger, position));
                    } else if (aisle.freeSpace(position)) {
                        aisle.advance(position);
                        eventsQueue.add(new Event(EventTypes.WALK, time + allPassengers[passenger].getWalkingSpeed(), passenger, position + 1));
                    } else {
                        eventsQueue.add(new Event(EventTypes.WALK, time + 2, passenger, position)); // wait two ticks and try again
                    }
                    break;
            }
            // always trying to cram another passenger on
            if (!boardingQueue.isEmpty()) {
                Integer firstPassenger = boardingQueue.peek();
                if (firstPassenger != null){
                    if (aisle.push(firstPassenger) != -1){
                        boardingQueue.poll();
                        eventsQueue.add(new Event(EventTypes.WALK, time + 2, firstPassenger, 0));
                    }
                }
            }
            

            if (time>ticksElapsed) {ticksElapsed = time;}
        }

        // we want to encourage orderly looking groups that are feasible to board together
        this.randomPenalty = (int) randomPenalty;
        this.duration = (int) ticksElapsed;
        // default return
        // ha! i refuse to write another loop
        return (int)(parameters.QUICKNESS * ticksElapsed
         + 10*parameters.CLUSTERING*IntStream.of(this.adjacentSameGroupSeats).sum()
         + 10*parameters.ORDERLINESS * (DoubleStream.of(this.avgDistance).sum() + DoubleStream.of(this.sdDistance).sum()));
    }

    public void randomPenalty(int[][] boardingGroups){

        for (int i = 0; i<boardingGroups.length; i++){
            int adjacentSameGroupSeats = 0;
            ArrayList<Double> distances = new ArrayList<>();
            for (int j = 0; j<boardingGroups[i].length; j++){
                int s1 = boardingGroups[i][j];
                int x = s1 % plane.getWidth();
                int y = s1 / plane.getWidth();
                Boolean adjacentSameGroup = false;
                // check if seat in business
                if (y>0){
                    if (s1 < plane.getBusinessRows() * plane.getSetsperBusinessRow()){
                        x = s1 % plane.getSetsperBusinessRow();
                        y = s1 / plane.getSetsperBusinessRow();
                        if (y>0){
                            int frontSeat = s1 - plane.getSetsperBusinessRow();
                            if (boardingInts[frontSeat] == boardingInts[s1]){
                                adjacentSameGroup = true;
                            }
                        }
                    } else {
                        int frontSeat = s1 - plane.getWidth();
                        if (boardingInts[frontSeat] == boardingInts[s1]){
                            adjacentSameGroup = true;
                        }
                    }
                }
                // number of seats with adjacent same group seats
                if (x>1){
                    int leftSeat = s1 -1;
                    if (boardingInts[leftSeat] == boardingInts[s1]){
                        adjacentSameGroup = true;
                    }
                } 
                
                if (adjacentSameGroup){
                    adjacentSameGroupSeats++;
                }
                
                for (int k = j+1; k<boardingGroups[i].length; k++){
                    
                    int s2 = boardingGroups[i][k];
                    int x2, y2;
                    if (s1 < plane.getBusinessRows() * plane.getSetsperBusinessRow()){
                        x2 = s2 % plane.getSetsperBusinessRow();
                        y2 = s2 / plane.getSetsperBusinessRow();
                    }
                    else {
                        x2 = s2 % plane.getWidth();
                        y2 = s2 / plane.getWidth();
                    }
                    double distance = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
                    distances.add(distance);
                    // 
                }
        }

            double avgDistance = 0;
            for (Double d: distances){
                avgDistance += d;
            }

            avgDistance = avgDistance / distances.size();
            // now calculte s.d.
            double differenceTotal = 0;
            for (Double d: distances){
                differenceTotal += Math.pow(d - avgDistance, 2);
            }
            double sd = Math.sqrt(differenceTotal / distances.size());

            this.adjacentSameGroupSeats[i] = adjacentSameGroupSeats;
            this.avgDistance[i] = avgDistance;
            this.sdDistance[i] = sd;
        }
    }

    public int getDuration(){
        return duration;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    public void setNumberGroups(int num){
        this.numberGroups = num;
    }
}