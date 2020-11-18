package practica4;

import ast.logging.LogFactory;
import ast.logging.Log;

public class Receiver implements Runnable {

    public static Log log = LogFactory.getLog(Receiver.class);

    protected TSocketRecv input;
    protected int recvBuf, recvInterval;

    public Receiver(TSocketRecv pcb, int recvBuf, int recvInterval) {
        this.input = pcb;
        this.recvBuf = recvBuf;
        this.recvInterval = recvInterval;
    }

    public Receiver(TSocketRecv pcb) {
        this(pcb, 1000, 100);
    }

    public void run() {
        try {
            byte n = 0;
            int dades = 0;
            byte[] buf = new byte[recvBuf];
            while (true) {
                Thread.sleep(this.recvInterval);
                int r = input.receiveData(buf, 0, buf.length);
                // check received data stamps
                for (int j = 0; j < r; j++) {
                    if (buf[j] != n) {
                        throw new Exception("ReceiverTask: Recieved data is corrupted");
                    }
                    n = (byte) (n + 1);
                }
                log.info("Receiver: received %d bytes", r);
                dades = dades + r;
                log.info("Dades total rebudes: %d bytes" , dades);
            }
        } catch (Exception e) {
            log.error("Excepcio a Receiver: %s", e);
            e.printStackTrace(System.err);
        }
    }

}
