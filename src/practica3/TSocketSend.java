package practica3;

import ast.protocols.tcp.TCPSegment;
import utils.Channel;

public class TSocketSend extends TSocketBase {

    protected int sndMSS;       // Send maximum segment size

    public TSocketSend(Channel channel) {
        super(channel);
        sndMSS = channel.getMMS();
    }

    public void sendData(byte[] data, int offset, int length) {
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        throw new RuntimeException("Aquest mètode s'ha de completar...");

    }

    protected void sendSegment(TCPSegment segment) {
        channel.send(segment);
    }
}
