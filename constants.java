public class constants {

    public static final int MAX_GROUPS = 12;
    public static final int GROUP_STAGGER = 600;
    public static final int PASSENGER_STAGGER = 3;
    public static final int DEFAULT_WALKING_SPEED = 2;
    public static final int DEFAULT_STOWING_SPEED = 12;
    public static final int DEFAULT_SITTING_SPEED = 6;
    public static final int MAX_BAGS = 2;

    public static final int BUTTON_SIZE = 20; // Size of each seat button in the PlaneView
    public static final int BUTTON_GAP = 5; // Gap between seats

    // constants for passenger generation probabilities
    public static final double PROBABILITY_DISABLED = 0.03;
    public static final double PROBABILITY_FAMILIES = 0.20;
    public static final double PROBABILITY_OLD = 0.10;
    public static final double PROBABILITY_CHILDREN = 0.10;
    public static final double PROBABILITY_BAGS = 0.80;

    // ga parameter constants
    public static final double ELITISM = 0.05;
    public static final int TOURNAMENT_SIZE = 4;
    public static final double SELECTION_POOL = 0.4;
    public static final double MUTATION = 0.12;
    public static final double NEW_SIMULATIONS = 0.20;
    static {

    }
}
