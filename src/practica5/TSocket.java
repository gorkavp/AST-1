package practica5;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ast.protocols.tcp.TCPSegment;
import practica1.CircularQ.CircularQueue;

public class TSocket {

    protected static final int RCV_QUEUE_SIZE = 3;

    protected Protocol proto;
    protected Lock lk;
    protected Condition appCV;

    protected int localPort;
    protected int remotePort;

    // Sender variables:
    protected int sndMSS;       // Send maximum segment size
    protected boolean segmentAcknowledged; // segment not yet acknowledged ?
    protected int rcvWindow;

    // Receiver variables:
    protected CircularQueue<TCPSegment> rcvQueue;
    protected int rcvSegConsumedBytes;

    //Other atributes (sender or receiver)
    protected Condition receptor;
    protected Condition emissor;

    /**
     * Create an endpoint bound to the given TCP ports.
     */
    protected TSocket(Protocol p, int localPort, int remotePort) {
        lk = new ReentrantLock();
        appCV = lk.newCondition();
        receptor = lk.newCondition();
        emissor = lk.newCondition();
        proto = p;
        this.localPort = localPort;
        this.remotePort = remotePort;
        // init sender variables
        sndMSS = p.net.getMMS() - TCPSegment.HEADER_SIZE; // IP maximum message size - TCP header size
        segmentAcknowledged = true;
        // init receiver variables
        rcvQueue = new CircularQueue(RCV_QUEUE_SIZE);
        rcvSegConsumedBytes = 0;
        rcvWindow = RCV_QUEUE_SIZE;
        //Other necessary initializations
        //...
    }

    // -------------  SENDER PART  ---------------
    public void sendData(byte[] data, int offset, int length) {

        // for each segment to send
        // wait until the sender is not expecting an acknowledgement
        // create a data segment and send it
        this.lk.lock();
        TCPSegment segment = new TCPSegment();
        segment.setAck(false);
        try {
            for (int i = 0; i < length; i = i + this.sndMSS) {
                segment = this.segmentize(data, offset + i, Math.min(this.sndMSS, length - i));
                this.sendSegment(segment);
                System.out.println("Segment enviat");
                this.segmentAcknowledged = false;
                while (!this.segmentAcknowledged) {
                    this.emissor.awaitUninterruptibly();
                }

            }
        } finally {
            this.lk.unlock();
        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        TCPSegment seg = new TCPSegment();
        byte[] missatge = new byte[length];
        System.arraycopy(data, offset, missatge, 0, length);
        seg.setData(missatge);
        seg.setDestinationPort(this.remotePort);
        seg.setSourcePort(this.localPort);
        return seg;
    }

    protected void sendSegment(TCPSegment segment) {
        proto.net.send(segment);
    }

    // -------------  RECEIVER PART  ---------------
    /**
     * Places received data in buf
     */
    public int receiveData(byte[] buf, int offset, int maxlen) {
        
        this.lk.lock();
        int dades = 0;
        try {

            while (this.rcvQueue.empty()) {
                this.receptor.awaitUninterruptibly();
            }

            while (!this.rcvQueue.empty() && dades < maxlen) {
                dades = dades + consumeSegment(buf, offset + dades, maxlen - dades);
            }
            if (!this.rcvQueue.full()) {
                this.sendAck();
                this.segmentAcknowledged = false;
            }
            return dades;
        } finally {
            this.lk.unlock();
        }
    }

    protected int consumeSegment(byte[] buf, int offset, int length) {
        TCPSegment seg = rcvQueue.peekFirst();
        // get data from seg and copy to receiveData's buffer
        int n = seg.getDataLength() - rcvSegConsumedBytes;
        if (n > length) {
            // receiveData's buffer is small. Consume a fragment of the received segment
            n = length;
        }
        // n == min(length, seg.getDataLength() - rcvSegConsumedBytes)
        System.arraycopy(seg.getData(), seg.getDataOffset() + rcvSegConsumedBytes, buf, offset, n);
        rcvSegConsumedBytes += n;
        if (rcvSegConsumedBytes == seg.getDataLength()) {
            // seg is totally consumed. Remove from rcvQueue
            rcvQueue.get();
            rcvSegConsumedBytes = 0;
        }
        return n;
    }

    protected void sendAck() {

        TCPSegment ack = new TCPSegment();
        ack.setWindow(this.rcvQueue.free());
        ack.setSourcePort(this.localPort);
        ack.setDestinationPort(this.remotePort);
        ack.setAck(true);
        sendSegment(ack);
        System.out.println("ACK enviat");
    }

    // -------------  SEGMENT ARRIVAL  -------------
    /**
     * Segment arrival.
     *
     * @param rseg segment of received packet
     */
    protected void processReceivedSegment(TCPSegment rseg) {
        lk.lock();
        try {
            // Check 
            //System.out.println("<---------" + rseg);

            if (rseg.isAck()) {

                this.rcvWindow = rseg.getWindow();
                this.segmentAcknowledged = true;
                this.emissor.signal();

            } else if (rseg.getDataLength() > 0) {
                // Process segment data
                if (rcvQueue.full()) {
                    System.out.println("La cua està plena");
                    return;
                } else {
                    this.rcvQueue.put(rseg);
                    this.receptor.signal();
                }
            } else {
                this.segmentAcknowledged = false;
            }
        } finally {
            lk.unlock();
        }
    }
}
