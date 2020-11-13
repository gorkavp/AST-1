package practica4;

import java.util.ArrayList;
import utils.Channel;

public class ProtocolSend extends ProtocolBase {

    protected ArrayList<TSocketSend> sockets;

    public ProtocolSend(Channel ch) {
        super(ch);
        sockets = new ArrayList();
    }

    public TSocketSend openForOutput(int localPort, int remotePort) {
        lk.lock();
        try {

            TSocketSend socket = new TSocketSend(ProtocolSend.this, localPort, remotePort);
            System.out.println("TSocketSend creat amb port local = " + localPort + " i port remot = " + remotePort);
            if (!sockets.contains(socket)) {
                sockets.add(socket);
            }
            return socket;

        } finally {
            lk.unlock();
        }
    }
}
