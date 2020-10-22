package practica2.Protocol;

import ast.protocols.tcp.TCPSegment;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import practica1.CircularQ.CircularQueue;
import utils.Channel;

public class MonitorChannel implements Channel {

    protected CircularQueue<TCPSegment> cua;
    protected final ReentrantLock lock = new ReentrantLock();
    protected final Condition p = lock.newCondition();
    protected final Condition b = lock.newCondition();

    public MonitorChannel(int N) {
        
        this.cua = new CircularQueue(N);
    }

    @Override
    public void send(TCPSegment seg) {
        
        this.lock.lock();
        try{
            while(this.cua.full()){
                this.p.awaitUninterruptibly();
            }
            this.cua.put(seg);
            this.b.signal();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public TCPSegment receive() {
        
        this.lock.lock();
        try{
            while(this.cua.empty()){
                this.b.awaitUninterruptibly();
            }
            this.p.signal();
            return this.cua.get();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getMMS() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

}
