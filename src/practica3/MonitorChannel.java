package practica3;

import ast.protocols.tcp.TCPSegment;

public class MonitorChannel extends practica2.Protocol.MonitorChannel {

   
    private double lossRatio;

   

    public MonitorChannel(int N, double lossRatio) {
        super(N);
        this.lossRatio = lossRatio;
    }

    @Override
    public void send(TCPSegment seg) {
        throw new RuntimeException("Aquest mètode s'ha de completar...");
    }

    @Override
    public int getMMS() {
        return 5;
    }

}
