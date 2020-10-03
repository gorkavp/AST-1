package practica1.Protocol;

import ast.protocols.tcp.TCPSegment;
import utils.Channel;

public class QueueChannel implements Channel {

    //Completar...

    public QueueChannel(int N) {
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }

    @Override
    public void send(TCPSegment s) {
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
