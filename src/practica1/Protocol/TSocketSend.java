package practica1.Protocol;

import ast.protocols.tcp.TCPSegment;
import utils.Channel;

public class TSocketSend {

    private final Channel channel;

    public TSocketSend(Channel channel) {
        this.channel = channel;
    }

    public void sendData(byte[] data, int offset, int length) {
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }
}
