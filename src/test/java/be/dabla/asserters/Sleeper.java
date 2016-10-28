package be.dabla.asserters;

public class Sleeper {

    private static final int DEFAULT = 5000;
    
    private Sleeper() {}

    public static void sleep() {
        sleep(DEFAULT);
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.err.println(e);
            throw new RuntimeException(e);
        }
    }
}
