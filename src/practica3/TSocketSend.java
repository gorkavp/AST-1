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

        this.lock.lock();
        TCPSegment segment = new TCPSegment();
        try {
            for (int i = 0; i < length; i = i + this.sndMSS) {
                if (length - i < this.sndMSS) {
                    segment = this.segmentize(data, offset + i, length - i);
                } else {
                    segment = this.segmentize(data, offset + i, this.sndMSS);
                }
                this.sendSegment(segment);
            }
        } finally {
            this.lock.unlock();
        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {

        byte[] missatge = new byte[length];
        System.arraycopy(data, offset, missatge, 0, length);
        TCPSegment segment = new TCPSegment();
        segment.setData(missatge);
        return segment;
    }

    protected void sendSegment(TCPSegment segment) {
        channel.send(segment);
    }
}
