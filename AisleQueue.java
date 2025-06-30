public class AisleQueue {
    private Passenger[] queue;

    public AisleQueue(int size) {
        this.queue = new Passenger[size];
    }

    public Boolean freeSpace(int index) {
        if (index + 1 >= queue.length) {
            return false;
        }
        return queue[index + 1] == null;
    }

    public int push(Passenger passenger) {

        if (queue[0] != null) {return -1;}
        queue[0] = passenger;
        return 0;
/*         if (passenger.getBags() > 1) {
            if (queue[1] != null) {
                return -1;
            }
            queue[1] = passenger;
            Passenger placeholder = new Passenger();
            placeholder.setSeat(null);
            return 0;
        } else {

        } */
    }

    public Passenger remove(int index) {

        if (queue[index] == null) {
            return null; // nothing to remove
        }

        Passenger passenger = queue[index];

/*         if (passenger.getBags() > 1) {
            queue[index - 1] = null;
        } */
        queue[index] = null;
        return passenger;
    }

    public void advance(int index) { 
        if (index < 0 || index > queue.length - 1|| queue[index] == null) {
            return;
        }
        queue[index + 1] = queue[index];
/*         if (queue[index].getBags()>1){
            queue[index] = queue[index - 1];
            queue[index - 1] = null;
        } else { */
            queue[index] = null;
/*         } */

    }

    public void print(){
        for (Passenger passenger: queue) {
            int i = 0;
            if (passenger != null){
                i = passenger.getIndex();
            } else {
                i = -1;
            }
            System.out.print(i + "   ");

        }
                    System.out.println("");
    }

    public Boolean isEmpty(){
        for (Passenger i:queue){
            if (i != null){
                return false;
            }
        }
        return true;
    }
}
