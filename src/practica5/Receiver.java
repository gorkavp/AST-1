package practica5;

import ast.logging.LogFactory;
import ast.logging.Log;

public class Receiver implements Runnable {

    public static Log log = LogFactory.getLog(Receiver.class);

    protected TSocket input;
    protected int recvBuf, recvInterval;

    public Receiver(TSocket pcb, int recvBuf, int recvInterval) {
        this.input = pcb;
        this.recvBuf = recvBuf;
        this.recvInterval = recvInterval;
    }

    public Receiver(TSocket pcb) {
        this(pcb, 1000, 100);
    }

    public void run() {
        try {
            byte n = 0;
            int b = 0;
            byte[] buf = new byte[recvBuf];
            while (true) {
                int r = input.receiveData(buf, 0, buf.length);
                // check received data stamps
                for (int j = 0; j < r; j++) {
                    if (buf[j] != n) {
                        throw new Exception("ReceiverTask: Recieved data is corrupted");
                    }
                    n = (byte) (n + 1);
                }
                b += r;
                log.info("Receiver: received %d bytes", r);
                log.info("Dades total rebudes: " + b + " bytes");
                Thread.sleep(recvInterval);
            }

        } catch (Exception e) {
            log.error("Receiver exception: %s", e);
            e.printStackTrace(System.err);
        }
    }

}
