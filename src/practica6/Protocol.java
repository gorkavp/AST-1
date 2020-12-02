package practica6;

import ast.protocols.tcp.TCPSegment;
import ast.logging.Log;
import ast.logging.LogFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import utils.FDuplexChannel;

public class Protocol {

    public static Log log = LogFactory.getLog(Protocol.class);

    protected ArrayList<TSocket> sockets;
    protected Thread task;
    protected Lock lk;
    protected FDuplexChannel.Peer net;

    public Protocol(FDuplexChannel.Peer ch) {
        sockets = new ArrayList<TSocket>();
        task = new Thread(new ReceiverTask());
        task.start();
        lk = new ReentrantLock();
        net = ch;
    }

    public TSocket openWith(int localPort, int remotePort) {
        lk.lock();
        try {

            TSocket socket = new TSocket(Protocol.this, localPort, remotePort);
            System.out.println("TSocketRecv creat amb port local = " + localPort + " i port remot = " + remotePort);
            if (!this.sockets.contains(socket)) {
                this.sockets.add(socket);
            }
            return socket;
        } finally {
            lk.unlock();
        }
    }

    protected void ipInput(TCPSegment segment) {
        TSocket socket = getMatchingTSocket(segment.getDestinationPort(), segment.getSourcePort());
        if (socket != null) {
            socket.processReceivedSegment(segment);
        } else {
            System.out.println("[ProtocolRecv] segment perdut: " + segment);
        }
    }

    protected TSocket getMatchingTSocket(int localPort, int remotePort) {
        lk.lock();
        try {
            for (TSocket s : sockets) {
                if (s.remotePort == remotePort && s.localPort == localPort) {
                    return s;
                }
            }
            return null;
        } finally {
            lk.unlock();
        }
    }

    class ReceiverTask implements Runnable {

        public void run() {
            while (true) {
                TCPSegment rseg = net.receive();
                ipInput(rseg);
            }
        }
    }

}
