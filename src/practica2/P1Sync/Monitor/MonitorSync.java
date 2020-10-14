package practica2.P1Sync.Monitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import practica1.CircularQ.CircularQueue;
import practica2.P1Sync.CounterThreadID;

public class MonitorSync {

    private final int N;
    private CircularQueue<CounterThreadID> p;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition c = lock.newCondition();
    private static int torn = 0;

    public MonitorSync(int N) {
        this.N = N;
    }

    public void waitForTurn(int id) {

        this.lock.lock();
        try {
            while (torn != id) {
                c.awaitUninterruptibly();
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void transferTurn() {

        torn = (1 + torn) % 2;
        this.lock.lock();
        try {
            c.signal();
        } finally {
            this.lock.unlock();
        }
    }
}
