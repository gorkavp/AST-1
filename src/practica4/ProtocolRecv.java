package practica4;

import ast.protocols.tcp.TCPSegment;
import java.util.ArrayList;
import utils.Channel;

public class ProtocolRecv extends ProtocolBase {

    protected Thread task;
    protected ArrayList<TSocketRecv> sockets;

    public ProtocolRecv(Channel ch) {
        super(ch);
        sockets = new ArrayList<TSocketRecv>();
        task = new Thread(new ReceiverTask());
        task.start();
    }

    public TSocketRecv openForInput(int localPort, int remotePort) {
        lk.lock();
        try {

            TSocketRecv socket = new TSocketRecv(ProtocolRecv.this, localPort, remotePort);
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

        TSocketRecv socket = getMatchingTSocket(segment.getDestinationPort(),segment.getSourcePort());
        if (socket != null) {
            socket.processReceivedSegment(segment);
        } else {
            System.out.println("[ProtocolRecv] segment perdut: " + segment);
        }
    }

    protected TSocketRecv getMatchingTSocket(int localPort, int remotePort) {
        
        lk.lock();
        try {
            for (TSocketRecv s: sockets) {
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

        @Override
        public void run() {
            while (true) {
                TCPSegment rseg = channel.receive();
                ipInput(rseg);
            }
        }
    }

}
