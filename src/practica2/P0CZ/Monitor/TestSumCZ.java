package practica2.P0CZ.Monitor;

public class TestSumCZ {

    public static void main(String[] args) throws InterruptedException {
        
        MonitorCZ m = new MonitorCZ();
        CounterThreadCZ fil1 = new CounterThreadCZ(m);
        CounterThreadCZ fil2 = new CounterThreadCZ(m);
        CounterThreadCZ fil3 = new CounterThreadCZ(m);
        CounterThreadCZ fil4 = new CounterThreadCZ(m);
        fil1.start();
        fil2.start();
        fil3.start();
        fil4.start();
        fil1.join();
        fil2.join();
        fil3.join();
        fil4.join();
        System.out.println("La x és: " + m.getX());
        
    }
}
