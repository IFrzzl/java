package planeSimulation;
import java.util.*;
import java.util.stream.*;

public class Simulation {
    int duration = 0;
    int[] boardingInts;
    int length = 0;
    int numberGroups;

    int randomPenalty = 0;
    int[] adjacentSameGroupSeats;
    double[] avgDistance;
    double[] sdDistance;

    int fitnessScore = 0;

    public Simulation(int numberGroups) {
        this.numberGroups = numberGroups;
        length = parameters.allPassengers.length;
        
        generateInitialGroups();

        adjacentSameGroupSeats = new int[numberGroups];
        avgDistance = new double[numberGroups];
        sdDistance = new double[numberGroups];

    }

    // helper: find the compact passenger index for a seat at (r,c) by scanning pos arrays
    private int findIndex(int[] posRow, int[] posCol, int len, int r, int c){
        for (int i = 0; i < len; i++){
            if (posRow[i] == r && posCol[i] == c) return i;
        }
        return -1;
    }

    public void joinFamilies(){
        for (Passenger passenger: parameters.allPassengers){
            if (passenger.getFamily() != null) {
                int[] family = passenger.getFamily();
                int group = boardingInts[family[parameters.random.nextInt(family.length)]];
                for (int relative: family){
                    boardingInts[relative] = group;
                }
            }
        }
    }

    public void generateInitialGroups(){
        int[] groups = new int[length];

        for (int i = 0; i<length; i++){
            groups[i] = parameters.random.nextInt(numberGroups);
        }
        this.boardingInts = groups;
    }

    public int splitFamilies(){
        int sFs = 0;
        for (Passenger passenger:parameters.allPassengers){
            if (passenger.getFamily() != null) {
                int[] family = passenger.getFamily();
                for (int relative: family){
                    if (boardingInts[relative] != boardingInts[passenger.getIndex()]) {
                        sFs ++;
                    }
                }
            }
        }
        return sFs;
    }

    void mutate(){
        int target1 = parameters.random.nextInt(parameters.allPassengers.length);
        int target2 = parameters.random.nextInt(parameters.allPassengers.length);
        
        Passenger passenger1 = parameters.allPassengers[target1];
        int[] family1 = passenger1.getFamily();
        int newGroup1 = boardingInts[target2];
        if (family1 != null){
            for (int relative: family1){
                boardingInts[relative] = newGroup1;
            }
        }
        boardingInts[passenger1.getIndex()] = newGroup1;

        Passenger passenger2 = parameters.allPassengers[target2];
        int[] family2 = passenger2.getFamily();
        int newGroup2 = boardingInts[target1];
        if (family2 != null){
            for (int relative: family2){
                boardingInts[relative] = newGroup2;
            }
        }
        boardingInts[passenger2.getIndex()] = newGroup2;

        // ensure any mutated values still fall within the valid range
        sanitizeGroups();
    }

    public int simulateBoardingTime() {
        this.duration = 0;
        this.fitnessScore = 0;
        double ticksElapsed = 0;
        // make a priority queue for boarding events
        // custom comparator using lamba to sort by start time
        PriorityQueue<Event> eventsQueue = new PriorityQueue<>((e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));

        // sorting allPassengers by boarding group
        int[][] boardingGroups = new int[numberGroups][];
        for (int i = 0; i < numberGroups; i++) {
            ArrayList<Integer> members = new ArrayList<>();
            for (int j = 0; j<parameters.allPassengers.length; j++){
                if (boardingInts[j] == i){
                    members.add(j);
                }
            }
            boardingGroups[i] = new int[members.size()];
            for (int j = 0; j<members.size(); j++){
                boardingGroups[i][j] = members.get(j);
            }
        }

        randomPenalty(boardingGroups);
 
        //make the plane aisle
        AisleQueue aisle = new AisleQueue(parameters.plane.getLength() + 2);

        Queue<Integer> boardingQueue = new LinkedList<Integer>();
        for (int i = 0; i<numberGroups; i++){
            for (int j = 0; j<boardingGroups[i].length; j++) {
                boardingQueue.add(boardingGroups[i][j]);
            }
        }
        Integer first = boardingQueue.poll();
        if (first == null) {
            // no passengers to board — set duration/fitness to 0 and return
            this.duration = 0;
            this.fitnessScore = (int)(parameters.QUICKNESS * 0
                + 10*parameters.CLUSTERING*IntStream.of(this.adjacentSameGroupSeats).sum()
                + 10*parameters.ORDERLINESS * (DoubleStream.of(this.avgDistance).sum() + DoubleStream.of(this.sdDistance).sum()));
            return this.fitnessScore;
        }
        eventsQueue.add(new Event(EventTypes.WALK, 0, first, 0)); // first passenger starts walking at time 0

        while (!eventsQueue.isEmpty()) {

            Event currentEvent = eventsQueue.poll();
            int passenger = currentEvent.getPassenger();
            double time = currentEvent.getTime();
            int position = currentEvent.getPosition();
            SeatStatus status = parameters.allPassengers[passenger].getSeat().getStatus();
            switch (currentEvent.getType()) {
                case EventTypes.SITTING:
                    time += parameters.allPassengers[passenger].getSittingSpeed();
                    aisle.remove(position);
                    break;
                case EventTypes.WALK:
                    if (position >= parameters.allPassengers[passenger].getSeat().getRow()) {
                        eventsQueue.add(new Event(EventTypes.SITTING, time + parameters.allPassengers[passenger].getStowingSpeed(), passenger, position));
                    } else if (aisle.freeSpace(position)) {
                        aisle.advance(position);
                        eventsQueue.add(new Event(EventTypes.WALK, time + parameters.allPassengers[passenger].getWalkingSpeed(), passenger, position + 1));
                    } else {
                        if (status == SeatStatus.BUSINESS || status == SeatStatus.BUSINESS_EXIT) {
                            this.fitnessScore += 50; // we love capitalism
                        }
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
        this.duration = (int) ticksElapsed;
        // default return
        // ha! i refuse to write another loop
        this.fitnessScore = (int)(parameters.QUICKNESS * ticksElapsed
         + 10*(parameters.CLUSTERING)*IntStream.of(this.adjacentSameGroupSeats).sum()
         + 10*parameters.ORDERLINESS * (DoubleStream.of(this.avgDistance).sum() + DoubleStream.of(this.sdDistance).sum()));
        this.fitnessScore += parameters.SPLIT_PENALTY * splitFamilies();
        return this.fitnessScore;
    }

    public void randomPenalty(int[][] boardingGroups){
        Seat[][] seatingChart = parameters.plane.getSeatingChart();
        int rows = seatingChart.length;
        int cols = seatingChart[0].length;

        int[] posRow = new int[parameters.plane.getCapacity()];
        int[] posCol = new int[parameters.plane.getCapacity()];
        int counter = 0;
        for (int r = 0; r < rows; r++){
            for (int c = 0; c < cols; c++){
                Seat s = seatingChart[r][c];
                if (s.getStatus() == SeatStatus.OTHER || s.getStatus() == SeatStatus.AISLE) continue;
                posRow[counter] = r;
                posCol[counter] = c;
                counter++;
            }
        }

        for (int i = 0; i < numberGroups; i++){
            int adjacentSameGroupSeats = 0;
            ArrayList<Double> distances = new ArrayList<>();
            for (int j = 0; j < boardingGroups[i].length; j++){
                int s1 = boardingGroups[i][j];
                if (s1 < 0 || s1 >= counter) continue;
                int r1 = posRow[s1];
                int c1 = posCol[s1];
                boolean adjacentSameGroup = false;

                // check neighbors: front, right, back, left
                if (r1 > 0){
                    int nr = r1 - 1, nc = c1;
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols){
                        Seat s = seatingChart[nr][nc];
                        if (s.getStatus() != SeatStatus.OTHER && s.getStatus() != SeatStatus.AISLE){
                            int idx = findIndex(posRow, posCol, counter, nr, nc);
                            if (idx != -1 && boardingInts[idx] == boardingInts[s1]) adjacentSameGroup = true;
                        }
                    }
                }
                if (!adjacentSameGroup && c1 < cols - 1){
                    int nr = r1, nc = c1 + 1;
                    Seat s = seatingChart[nr][nc];
                    if (s.getStatus() != SeatStatus.OTHER && s.getStatus() != SeatStatus.AISLE){
                        int idx = findIndex(posRow, posCol, counter, nr, nc);
                        if (idx != -1 && boardingInts[idx] == boardingInts[s1]) adjacentSameGroup = true;
                    }
                }
                if (!adjacentSameGroup && r1 < rows - 1){
                    int nr = r1 + 1, nc = c1;
                    Seat s = seatingChart[nr][nc];
                    if (s.getStatus() != SeatStatus.OTHER && s.getStatus() != SeatStatus.AISLE){
                        int idx = findIndex(posRow, posCol, counter, nr, nc);
                        if (idx != -1 && boardingInts[idx] == boardingInts[s1]) adjacentSameGroup = true;
                    }
                }
                if (!adjacentSameGroup && c1 > 0){
                    int nr = r1, nc = c1 - 1;
                    Seat s = seatingChart[nr][nc];
                    if (s.getStatus() != SeatStatus.OTHER && s.getStatus() != SeatStatus.AISLE){
                        int idx = findIndex(posRow, posCol, counter, nr, nc);
                        if (idx != -1 && boardingInts[idx] == boardingInts[s1]) adjacentSameGroup = true;
                    }
                }

                if (adjacentSameGroup) adjacentSameGroupSeats++;

                for (int k = j + 1; k < boardingGroups[i].length; k++){
                    int s2 = boardingGroups[i][k];
                    if (s2 < 0 || s2 >= counter) continue;
                    int r2 = posRow[s2];
                    int c2 = posCol[s2];
                    double distance = Math.sqrt(Math.pow(c2 - c1, 2) + Math.pow(r2 - r1, 2));
                    distances.add(distance);
                }
            }

            double avgDistance = 0;
            for (Double d: distances) avgDistance += d;
            if (distances.size() == 0) avgDistance = 0.0; else avgDistance = avgDistance / distances.size();

            double differenceTotal = 0;
            for (Double d: distances) differenceTotal += Math.pow(d - avgDistance, 2);
            double sd = 0.0;
            if (distances.size() == 0) sd = 0.0; else sd = Math.sqrt(differenceTotal / distances.size());

            this.adjacentSameGroupSeats[i] = adjacentSameGroupSeats;
            this.avgDistance[i] = avgDistance;
            this.sdDistance[i] = sd;
        }
    }

    public int getDuration(){
        return duration;
    }
    public int getFitness(){
        return fitnessScore;
    }
    public void setNumberGroups(int num){
        this.numberGroups = num;
    }
    public int[] getBoardingInts() {
        return boardingInts;
    }
    public int getNumberGroups() {
        return numberGroups;
    }
    public void setBoardingInts(int[] bi){
        boardingInts = bi;
        sanitizeGroups();
    }

    public void sanitizeGroups() {
        if (boardingInts == null) return;
        if (numberGroups <= 0) return;
        for (int i = 0; i < boardingInts.length; i++) {
            int v = boardingInts[i];
            if (v < 0) {
                boardingInts[i] = 0;
            } else if (v >= numberGroups) {
                boardingInts[i] = v % numberGroups;
            }
        }
    }
}