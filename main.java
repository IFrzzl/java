import java.util.ArrayList;
import java.util.Random;

class Main {
    ArrayList<Passenger> allPassengers = new ArrayList<Passenger>();
    int numberPassengers;
    Plane plane;

    public static void main(String[] args) {
        numberPassengers = plane.capacity;
        generatePassengerData();
     }

     
    public void generatePassengerData() {
        Random rand = new Random();
        final double disabled = 0.03;
        final double families = 0.10;

        for (i=0;i<numberPassengers;i++){
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
}