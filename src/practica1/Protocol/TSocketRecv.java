package practica1.Protocol;

import ast.protocols.tcp.TCPSegment;
import utils.Channel;

public class TSocketRecv {

    private final Channel channel;

    public TSocketRecv(Channel channel) {
        this.channel = channel;
    }

    public int receiveData(byte[] data, int offset, int length) {
        
        TCPSegment missatge = channel.receive();
        byte[] d = missatge.getData();
        int num = 0;
        for (int i = missatge.getDataOffset(); i < missatge.getDataOffset() + missatge.getDataLength() && num < length; i++) {
            data[offset+num] = d[i];
            num++;
        }
        return num;
        
    }
}
