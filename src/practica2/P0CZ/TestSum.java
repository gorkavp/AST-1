package practica2.P0CZ;

public class TestSum {

    public static void main(String[] args) throws InterruptedException {
        
        CounterThread fil1 = new CounterThread();
        CounterThread fil2 = new CounterThread();
        
        fil1.start();
        fil2.start();
        
        fil1.join();
        fil2.join();
        
        System.out.println("Final value of x: " + CounterThread.x);
    }
}

