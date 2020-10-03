package practica2.Protocol;

import ast.protocols.tcp.TCPSegment;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import practica1.CircularQ.CircularQueue;
import utils.Channel;

public class MonitorChannel implements Channel {

    //Completar...

    public MonitorChannel(int N) {
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }

    @Override
    public void send(TCPSegment seg) {
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }

    @Override
    public TCPSegment receive() {
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }

    @Override
    public int getMMS() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

}
