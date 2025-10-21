public class parameters {

    public static int NUMBER_GENERATIONS = 1000;
    public static int NUMBER_SIMULATIONS = 500;
    public static Plane plane = 
    new Plane(10, 4, 6, 4, new int[]{2, 2}, new int[]{0, 7}, "Boeing 737");
    // default plane
    public static final Plane defaultPlane = new Plane(10, 4, 6, 4, new int[]{2, 2}, new int[]{0, 7}, "Boeing 737");

    public static int MAX_GROUPS = 4;
    public static final int GROUP_STAGGER = 600;
    public static final int PASSENGER_STAGGER = 3;
    public static final int DEFAULT_WALKING_SPEED = 2;
    public static final int DEFAULT_STOWING_SPEED = 12;
    public static final int DEFAULT_SITTING_SPEED = 6;
    public static final int MAX_BAGS = 2;

    public static final int BUTTON_SIZE = 20; // Size of each seat button in the PlaneView
    public static final int BUTTON_GAP = 5; // Gap between seats

    // parameters for passenger generation probabilities
    public static double PROBABILITY_DISABLED = 0.03;
    public static double PROBABILITY_FAMILIES = 0.20;
    public static double PROBABILITY_OLD = 0.10;
    public static double PROBABILITY_CHILDREN = 0.10;
    public static double PROBABILITY_BAGS = 0.80;

    // ga parameter parameters
    public static double ELITISM = 0.03;
    public static int TOURNAMENT_SIZE = 3;
    public static double SELECTION_POOL = 0.5;
    public static double MUTATION = 0.2;
    public static double NEW_SIMULATIONS = 0.3;
    public static int SPLIT_PENALTY = 700; //ticks

    // big flags
    public static Boolean PAUSED = false;
    public static Boolean SKIP = false;
    public static Boolean END = false;
    public static Boolean STARTED = false;
    public static Boolean REDRAW = false;
    static {

    }
}
