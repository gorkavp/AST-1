package practica6;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ast.protocols.tcp.TCPSegment;
import ast.util.Timer;
import java.util.concurrent.TimeUnit;
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
    protected int rcvWindow;
    protected static final int SND_RTO = 500;
    protected Timer timerService;
    protected Timer.Task sndRtTimer;
    protected int sndNxt;
    protected TCPSegment sndUnackedSegment;

    // Receiver variables:
    protected CircularQueue<TCPSegment> rcvQueue;
    protected int rcvSegConsumedBytes;
    protected int rcvNxt;

    //Other atributes (sender or receiver)
    //...
    /**
     * Create an endpoint bound to the given TCP ports.
     */
    protected TSocket(Protocol p, int localPort, int remotePort) {
        lk = new ReentrantLock();
        appCV = lk.newCondition();
        proto = p;
        this.localPort = localPort;
        this.remotePort = remotePort;
        // init sender variables
        sndMSS = p.net.getMMS() - TCPSegment.HEADER_SIZE; // IP maximum message size - TCP header size
        // init receiver variables
        rcvQueue = new CircularQueue<TCPSegment>(RCV_QUEUE_SIZE);
        rcvWindow = RCV_QUEUE_SIZE;
        timerService = new Timer();

        //Other necessary initializations
        //...
    }

    // -------------  SENDER PART  ---------------
    public void sendData(byte[] data, int offset, int length) {
        lk.lock();
        try {
            // A completar per l'estudiant:
            //...
            // for each segment to send
            // wait until the sent segment is acknowledged
            // create a data segment 
            // Taking into account if you are at the zero window case or not
            // and send it
            // Remember to start the timer

        } finally {
            lk.unlock();
        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        // A completar per l'estudiant (veieu practica 5):
        //...
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }

    protected void sendSegment(TCPSegment segment) {
        // A completar per l'estudiant (veieu practica 5):
    }

    protected void timeout() {
        lk.lock();
        try {
            if (sndUnackedSegment != null) {
                System.out.println("torno a enviar segment DADES: " + sndUnackedSegment);
                sendSegment(sndUnackedSegment);
                startRTO();
            }
        } finally {
            lk.unlock();
        }
    }

    protected void startRTO() {
        if (sndRtTimer != null) {
            sndRtTimer.cancel();
        }
        sndRtTimer = timerService.startAfter(
                new Runnable() {
            @Override
            public void run() {
                timeout();
            }
        },
                SND_RTO, TimeUnit.MILLISECONDS);
    }

    protected void stopRTO() {
        if (sndRtTimer != null) {
            sndRtTimer.cancel();
        }
        sndRtTimer = null;
    }

    // -------------  RECEIVER PART  ---------------
    /**
     * Places received data in buf
     */
    public int receiveData(byte[] buf, int offset, int maxlen) {
        lk.lock();
        try {
            // A completar per l'estudiant:
            //...
            // wait until there is a received segment
            // get data from the received segment
            throw new RuntimeException("Aquest mètode s'ha de completar...");
        } finally {
            lk.unlock();
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
        // A completar per l'estudiant:
        //...
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
            // Check ACK
            if (rseg.isAck()) {
                // A completar per l'estudiant:
                //...

            } else if (rseg.getDataLength() > 0) {
                // Process segment data
                if (rcvQueue.full()) {
                    //A completar per l'estudiant:
                    //...
                }
                // A completar per l'estudiant:
                //...
            }
        } finally {
            lk.unlock();
        }
    }
}
