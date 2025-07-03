public class AisleQueue {
    private int[] queue;

    public AisleQueue(int size) {
        this.queue = new int[size];
        for (int i = 0; i<size; i++){
            queue[i] = -1;  
        }
    }

    public Boolean freeSpace(int index) {
        if (index + 1 >= queue.length) {
            return false;
        }
        return queue[index + 1] == -1;
    }

    public int push(int passenger) {

        if (queue[0] != -1) {return -1;}
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

    public int remove(int index) {

        if (queue[index] == -1) {
            return -1; // nothing to remove
        }

        int passenger = queue[index];

/*         if (passenger.getBags() > 1) {
            queue[index - 1] = null;
        } */
        queue[index] = -1;
        return passenger;
    }

    public void advance(int index) { 
        if (index < 0 || index > queue.length - 1|| queue[index] == -1) {
            return;
        }
        queue[index + 1] = queue[index];
/*         if (queue[index].getBags()>1){
            queue[index] = queue[index - 1];
            queue[index - 1] = null;
        } else { */
            queue[index] = -1;
/*         } */

    }

    public void print(){
        for (int passenger: queue) {
            System.out.print(passenger + "   ");
        }
        System.out.println("");
    }

    public Boolean isEmpty(){
        for (int i:queue){
            if (i != -1){
                return false;
            }
        }
        return true;
    }
}
