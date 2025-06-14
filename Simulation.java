import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

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
        final double disabled = 0.03;
        final double families = 0.10;
        int numberPassengers = plane.getCapacity();
        for (int i=0;i<numberPassengers;i++){
            Passenger passenger = new Passenger();
            if (rand.nextInt(100)<=disabled*100){
                passenger.setDisabled(true);
            }
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

            Family family = new Family();
            int i = 0;
            do {
                Passenger passenger = allPassengers.get(rand.nextInt(numberPassengers));
                if (passenger.famlily != null){
                    family.add(passenger);
                    i++;
                }
            } while (i<familySize);
            familyPassengers -= familySize;
        }
    }
}

class Group {
    int groupNumber;

    public Group(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public int getGroupNumber() {
        return groupNumber;
    }
}

class Family {
    ArrayList<Passenger> members = new ArrayList<Passenger>();

    public Family() {
        
    }

    public void add(Passenger passenger) {
        if (passenger.getFamily() == null) {
            passenger.setFamily(this);
            members.add(passenger);
        }
    }

    public int size() {
        return members.size();
    }

    public ArrayList<Passenger> getMembers() {
        return members;
    }
}