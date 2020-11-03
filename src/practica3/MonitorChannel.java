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

        this.lock.lock();
        try {
            double m = Math.random();
            if (m < this.lossRatio) {
                System.out.println("Paquet perdut");
                return;
            } else {
                super.send(seg);
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getMMS() {
        return 5;
    }

}
