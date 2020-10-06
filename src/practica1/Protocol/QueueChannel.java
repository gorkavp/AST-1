package practica1.Protocol;

import ast.protocols.tcp.TCPSegment;
import utils.Channel;
import practica1.CircularQ.CircularQueue;

public class QueueChannel implements Channel {

    private CircularQueue<TCPSegment> dades;

    public QueueChannel(int N) {
        this.dades = new CircularQueue(N);
    }

    @Override
    public void send(TCPSegment s) {
        this.dades.put(s);
    }

    @Override
    public TCPSegment receive() {
        return this.dades.get();
    }

    @Override
    public int getMMS() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
