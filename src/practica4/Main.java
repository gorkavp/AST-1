package practica4;

import practica3.MonitorChannel;
import utils.Channel;

public class Main {

    public static void main(String[] args) {
        Channel c = new MonitorChannel(10, 0);

        ProtocolRecv proto1 = new ProtocolRecv(c);
        new Thread(new Host1(proto1)).start();

        ProtocolSend proto2 = new ProtocolSend(c);
        new Thread(new Host2(proto2)).start();
    }
}

class Host1 implements Runnable {

    public static final int PORT = 10;

    protected ProtocolRecv proto;

    public Host1(ProtocolRecv proto) {
        this.proto = proto;
    }

    public void run() {
        
        //arranca dos fils receptors, cadascun amb el seu socket de recepcio
        //fes servir els ports apropiats
        TSocketRecv socket1 = proto.openForInput(Host2.PORT1, Host2.PORT1);
        TSocketRecv socket2 = proto.openForInput(Host2.PORT2, Host2.PORT2);
        new Thread(new Receiver(socket1)).start();
        new Thread(new Receiver(socket2)).start();
    }
}

class Host2 implements Runnable {

    public static final int PORT1 = 10;
    public static final int PORT2 = 50;

    protected ProtocolSend proto;

    public Host2(ProtocolSend proto) {
        this.proto = proto;
    }

    public void run() {

        //arranca dos fils emissors, cadascun amb el seu socket de transmissio
        //fes servir els ports apropiats
        TSocketSend socket1 = proto.openForOutput(Host2.PORT1, Host2.PORT1);
        TSocketSend socket2 = proto.openForOutput(Host2.PORT2, Host2.PORT2);
        new Thread(new Sender(socket1)).start();
        new Thread(new Sender(socket2)).start();
    }

}
