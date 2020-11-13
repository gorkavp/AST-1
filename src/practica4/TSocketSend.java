package practica4;

import ast.protocols.tcp.TCPSegment;

public class TSocketSend extends TSocketBase {

    protected int sndMSS;       // Send maximum segment size

    /**
     * Create an endpoint bound to the local IP address and the given TCP port.
     * The local IP address is determined by the networking system.
     *
     * @param ch
     */
    protected TSocketSend(ProtocolSend p, int localPort, int remotePort) {
        super(p, localPort, remotePort);
        sndMSS = p.channel.getMMS() - TCPSegment.HEADER_SIZE; // IP maximum message size - TCP header size
    }

    public void sendData(byte[] data, int offset, int length) {

        this.lk.lock();
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
        return seg;
    }

    protected void sendSegment(TCPSegment segment) {
        proto.channel.send(segment);
    }
}
