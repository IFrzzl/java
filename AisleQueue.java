public class AisleQueue {
    // so this is pretty much an array with a function to shift the elements, and push and pop operations
    // also functionality to let one Passenger object occupy two positions in the queue

    private Passenger[] queue;

    public AisleQueue(int size) {
        this.queue = new Passenger[size];
    }

    public Boolean freeSpace(int index) { 
        if (queue[index+1] == null) {
            return true;
        } return false;
    } 

    //refactor
    public int push(Passenger passenger) {
        if (passenger.getBags() > 0) {
            if (queue[0] != null || queue[1] != null) {
                return -1;
            } 
            queue[1] = passenger;
            Passenger placeholder = new Passenger();
            placeholder.setGroupNum(-1);
            queue[0] = placeholder;
            return 0;
        } else {
            if (queue[0] != null) {
                return -1;
            }
            queue[0] = passenger;
            return 0;
        }
    }


    public Passenger remove(int index) {
        Passenger passenger = queue[index];
        if (queue[index] == null) {
            return null; // nothing to remove
        }

        if (queue[index].getBags() > 0){
            //then we also need to remove the placeholder
            remove(index - 1); // cheeky bit of recusrion
        }
        queue[index] = null;
        shift(index);
        return passenger;
    }


    public void shift(int index) {
        if (index == queue.length && queue[index - 1] != null){
            return;
        }
        for (int i = 0; i < index - 1; i++) {
            queue[i] = queue[i + 1];
        }
        queue[queue.length - 1] = null; // clear the last position
    }
}
