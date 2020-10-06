package practica1.Protocol;

import ast.protocols.tcp.TCPSegment;
import utils.Channel;

public class TSocketSend {

    private final Channel channel;

    public TSocketSend(Channel channel) {
        this.channel = channel;
    }

    public void sendData(byte[] data, int offset, int length) {
        
        byte[] missatge = new byte[length];
        System.arraycopy(data, offset, missatge, 0, length);
        TCPSegment segment = new TCPSegment();
        segment.setData(missatge);
        channel.send(segment);
    }
}