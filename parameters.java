public class parameters {

    public static final int MAX_GROUPS = 4;
    public static final int GROUP_STAGGER = 600;
    public static final int PASSENGER_STAGGER = 3;
    public static final int DEFAULT_WALKING_SPEED = 2;
    public static final int DEFAULT_STOWING_SPEED = 12;
    public static final int DEFAULT_SITTING_SPEED = 6;
    public static final int MAX_BAGS = 2;

    public static final int BUTTON_SIZE = 20; // Size of each seat button in the PlaneView
    public static final int BUTTON_GAP = 5; // Gap between seats

    // parameters for passenger generation probabilities
    public static final double PROBABILITY_DISABLED = 0.03;
    public static final double PROBABILITY_FAMILIES = 0.20;
    public static final double PROBABILITY_OLD = 0.10;
    public static final double PROBABILITY_CHILDREN = 0.10;
    public static final double PROBABILITY_BAGS = 0.80;

    // ga parameter parameters
    public static double ELITISM = 0.03;
    public static int TOURNAMENT_SIZE = 3;
    public static double SELECTION_POOL = 0.5;
    public static double MUTATION = 0.2;
    public static double NEW_SIMULATIONS = 0.3;
    public static int SPLIT_PENALTY = 700; //ticks
    static {

    }
}
