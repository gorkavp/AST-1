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
    protected Condition emissor;
    protected Condition receptor;

    /**
     * Create an endpoint bound to the given TCP ports.
     */
    protected TSocket(Protocol p, int localPort, int remotePort) {
        lk = new ReentrantLock();
        appCV = lk.newCondition();
        this.emissor = lk.newCondition();
        this.receptor = lk.newCondition();
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

        // for each segment to send
        // wait until the sent segment is acknowledged
        // create a data segment 
        // Taking into account if you are at the zero window case or not
        // and send it
        // Remember to start the timer
        this.lk.lock();
        TCPSegment segment = new TCPSegment();
        try {
            for (int i = 0; i < length; i = i + this.sndMSS) {
                while (this.sndUnackedSegment != null) {
                    this.emissor.awaitUninterruptibly();
                }
                segment = this.segmentize(data, offset + i, Math.min(this.sndMSS, length - i));
                while (this.rcvWindow == 0) {
                    TCPSegment segmentsondeig = new TCPSegment();
                    segmentsondeig = segment;
                    segmentsondeig.setData(data, 0, 1);
                    this.sndUnackedSegment = segmentsondeig;
                    System.out.println("Emissor: finestra = " + this.rcvWindow + ", envio segment de sondeig");
                    this.startRTO();
                    this.emissor.awaitUninterruptibly();
                }
                this.sndUnackedSegment = segment;
                segment.setSeqNum(this.sndNxt);
                System.out.println("Emissor: segment " + this.sndNxt++ + " enviat");
                this.sendSegment(segment);
                this.startRTO();
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
        seg.setAck(false);
        return seg;
    }

    protected void sendSegment(TCPSegment segment) {
        proto.net.send(segment);
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

        // wait until there is a received segment
        // get data from the received segment
        this.lk.lock();
        int dades = 0;
        try {

            while (this.rcvQueue.empty()) {
                this.receptor.awaitUninterruptibly();
            }

            while (!this.rcvQueue.empty() && dades < maxlen) {
                dades = dades + consumeSegment(buf, offset + dades, maxlen - dades);
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
        ack.setAckNum(this.rcvNxt);
        ack.setWindow(this.rcvQueue.free());
        ack.setSourcePort(this.localPort);
        ack.setDestinationPort(this.remotePort);
        ack.setAck(true);
        ack.setData(null, 0, 0);
        System.out.println("Receptor: segment " + this.sndNxt++ + " rebut, ack " + ack.getAckNum() + " enviat i el receptor espera rebre el segment " + this.rcvNxt);
        sendSegment(ack);
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

            if (rseg.isAck()) {

                this.stopRTO();
                this.rcvWindow = rseg.getWindow();
                this.sndUnackedSegment = null;
                System.out.println("Emissor: ack " + rseg.getAckNum() + " rebut i el tamany de la finestra és " + this.rcvWindow);
                this.emissor.signal();

            } else if (rseg.getDataLength() > 0) {
                // Process segment data
                if (rcvQueue.full()) {
                    System.out.println("La cua està plena");
                    return;
                } else if (rseg.getSeqNum() == this.rcvNxt) {
                    this.rcvQueue.put(rseg);
                    this.rcvNxt++;
                }
                this.sendAck();
                this.receptor.signal();
            }
        } finally {
            lk.unlock();
        }
    }
}
