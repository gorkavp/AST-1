package practica4;

import ast.protocols.tcp.TCPSegment;
import practica1.CircularQ.CircularQueue;

public class TSocketRecv extends TSocketBase {

    protected CircularQueue<TCPSegment> rcvQueue;
    protected int rcvSegUnc;

    /**
     * Create an endpoint bound to the local IP address and the given TCP port.
     * The local IP address is determined by the networking system.
     *
     * @param ch
     */
    protected TSocketRecv(ProtocolRecv p, int localPort, int remotePort) {
        super(p, localPort, remotePort);
        rcvQueue = new CircularQueue<TCPSegment>(20);
        rcvSegUnc = 0;
    }

    /**
     * Places received data in buf
     */
    public int receiveData(byte[] buf, int offset, int length) {

        this.lk.lock();
        int dades = 0;
        try {

            while (rcvQueue.empty()) {
                this.appCV.awaitUninterruptibly();
            }

            while (!rcvQueue.empty() && dades < length) {
                dades = dades + consumeSegment(buf, offset + dades, length - dades);
            }
            return dades;
        } finally {
            this.lk.unlock();
        }

    }

    protected int consumeSegment(byte[] buf, int offset, int length) {

        TCPSegment seg = rcvQueue.peekFirst();
        // getCnd data from seg and copy to receiveData's buffer
        int n = seg.getDataLength() - this.rcvSegUnc;
        if (n > length) {
            // receiveData's buffer is small. Consume a fragment of the received segment
            n = length;
        }
        // n == min(length, seg.getDataLength() - rcvSegConsumedBytes)
        System.arraycopy(seg.getData(), seg.getDataOffset() + this.rcvSegUnc, buf, offset, n);
        this.rcvSegUnc += n;
        if (this.rcvSegUnc == seg.getDataLength()) {
            // seg is totally consumed. Remove from rcvQueue
            rcvQueue.get();
            this.rcvSegUnc = 0;
        }
        return n;
    }

    /**
     * Segment arrival.
     *
     * @param rseg segment of received packet
     */
    protected void processReceivedSegment(TCPSegment rseg) {

        this.lk.lock();
        try {
            if (!this.rcvQueue.full()) {
                this.rcvQueue.put(rseg);
                this.appCV.signalAll();
            }
        } finally {
            this.lk.unlock();
        }
    }
}
