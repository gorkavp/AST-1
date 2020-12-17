package practica7;

import ast.protocols.tcp.TCPSegment;
import ast.util.CircularQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Connection oriented Protocol Control Block.
 *
 * Each instance of TSocket maintains all the status of an endpoint.
 *
 * Interface for application layer defines methods for passive/active opening
 * and for closing the connection. Interface lower layer defines methods for
 * processing of received segments and for sending of segments. We assume an
 * ideal lower layer with no losses and no errors in packets.
 *
 * State diagram:
 * <pre>
 * +---------+
 * |  CLOSED |-------------
 * +---------+             \
 * LISTEN  |                   \
 * ------  |                    | CONNECT
 * V                    | -------
 * +---------+               | snd SYN
 * |  LISTEN |               |
 * +---------+          +----------+
 * |               | SYN_SENT |
 * |               +----------+
 * rcv SYN   |                    |
 * -------   |                    | rcv SYN
 * snd SYN   |                    | -------
 * |                    |
 * V                   /
 * +---------+             /
 * |  ESTAB  |<------------
 * +---------+
 * CLOSE    |     |    rcv FIN
 * -------   |     |    -------
 * +---------+          snd FIN  /       \                    +---------+
 * |  FIN    |<-----------------           ------------------>|  CLOSE  |
 * |  WAIT   |------------------           -------------------|  WAIT   |
 * +---------+          rcv FIN  \       /   CLOSE            +---------+
 * -------   |      |  -------
 * |      |  snd FIN
 * V      V
 * +----------+
 * |  CLOSED  |
 * +----------+
 * </pre>
 *
 * @author AST's teachers
 */
public class TSocket {

    protected Protocol proto;
    protected Lock lk;
    protected Condition appCV;

    protected int localPort;
    protected int remotePort;

    protected int state;
    protected CircularQueue<TSocket> acceptQueue;

    // States of FSM:
    protected final static int CLOSED = 0,
            LISTEN = 1,
            SYN_SENT = 2,
            ESTABLISHED = 3,
            FIN_WAIT = 4,
            CLOSE_WAIT = 5;

    /**
     * Create an endpoint bound to the local IP address and the given TCP port.
     * The local IP address is determined by the networking system.
     *
     * @param ch
     */
    protected TSocket(Protocol p, int localPort) {
        lk = new ReentrantLock();
        appCV = lk.newCondition();
        proto = p;
        this.localPort = localPort;
        state = CLOSED;
    }

    /**
     * Passive open
     */
    protected void listen() {
        lk.lock();
        try {
            acceptQueue = new CircularQueue<TSocket>(5);
            state = LISTEN;
            proto.addListenTSocket(this);
        } finally {
            lk.unlock();
        }
    }

    public TSocket accept() {
        lk.lock();
        try {
            while (this.acceptQueue.empty()) {
                this.appCV.awaitUninterruptibly();
            }
            return this.acceptQueue.get();
        } finally {
            lk.unlock();
        }
    }

    /**
     * Active open
     */
    protected void connect(int remPort) {
        lk.lock();
        try {
            remotePort = remPort;
            proto.addActiveTSocket(this);
            TCPSegment SYN = new TCPSegment();
            SYN.setPorts(this.localPort, this.remotePort);
            SYN.setSyn(true);
            this.sendSegment(SYN);
            this.state = SYN_SENT;
            while (this.state != ESTABLISHED) {
                this.appCV.awaitUninterruptibly();
            }
        } finally {
            lk.unlock();
        }
    }

    public void close() {
        lk.lock();
        try {
            switch (state) {
                case ESTABLISHED: {
                    TCPSegment FIN = new TCPSegment();
                    FIN.setPorts(localPort, remotePort);
                    FIN.setFin(true);
                    this.sendSegment(FIN);
                    this.state = FIN_WAIT;
                    break;
                }
                case CLOSE_WAIT: {
                    TCPSegment FIN = new TCPSegment();
                    FIN.setPorts(localPort, remotePort);
                    FIN.setFin(true);
                    this.sendSegment(FIN);
                    this.proto.removeActiveTSocket(this);
                    this.state = CLOSED;
                    break;
                }
                default:
            }
        } finally {
            lk.unlock();
        }
    }

    /**
     * Segment arrival.
     *
     * @param rseg segment of received packet
     */
    protected void processReceivedSegment(TCPSegment rseg) {
        lk.lock();
        try {
            switch (state) {
                case LISTEN: {
                    if (rseg.isSyn()) {
                        // create a new TSocket for new connection and set it to ESTABLISHED state
                        // also set local and remote ports
                        // prepare this TSocket to accept the newly created TSocket
                        // from the new TSocket send SYN segment for new connection                
                        TSocket s = new TSocket(this.proto, this.localPort);
                        s.remotePort = rseg.getSourcePort();
                        s.state = ESTABLISHED;
                        this.proto.addActiveTSocket(s);
                        this.acceptQueue.put(s);
                        this.appCV.signalAll();
                        TCPSegment SYN = new TCPSegment();
                        SYN.setPorts(s.localPort, s.remotePort);
                        SYN.setSyn(true);
                        s.sendSegment(SYN);
                    }
                    break;
                }
                case SYN_SENT: {
                    if (rseg.isSyn()) {
                        this.state = ESTABLISHED;
                        this.appCV.signalAll();
                    }
                    break;
                }
                case ESTABLISHED: {
                    if (rseg.isFin()) {
                        this.state = CLOSE_WAIT;
                    }
                    break;
                }
                case FIN_WAIT: {
                    if (rseg.isFin()) {
                        this.proto.removeActiveTSocket(this);
                        this.state = CLOSED;
                    }
                    break;
                }
                case CLOSE_WAIT: {
                    // Process segment text
                    if (rseg.getDataLength() > 0) {
                        if (state == ESTABLISHED || state == FIN_WAIT) {
                            // Here should go the segment's data processing
                        } else {
                            // This should not occur, since a FIN has been received from the
                            // remote side.  Ignore the segment text.
                        }
                    }
                    // Check FIN bit               
                    if (rseg.isFin()) {
                        this.close();
                    }
                    break;
                }
            }
        } finally {
            lk.unlock();
        }
    }

    protected void sendSegment(TCPSegment segment) {
        proto.channel.send(segment);
    }

    public String stateToString() {
        String sst;
        switch (state) {
            case CLOSED:
                sst = "CLOSED";
                break;
            case LISTEN:
                sst = "LISTEN";
                break;
            case SYN_SENT:
                sst = "SYN_SENT";
                break;
            case ESTABLISHED:
                sst = "ESTABLISHED";
                break;
            case FIN_WAIT:
                sst = "FIN_WAIT";
                break;
            case CLOSE_WAIT:
                sst = "CLOSE_WAIT";
                break;
            default:
                sst = "?";
        }
        return sst;
    }

}
