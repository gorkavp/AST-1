package practica4;

import ast.logging.Log;
import ast.logging.LogFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import utils.Channel;

public class ProtocolBase {

    public static Log log = LogFactory.getLog(ProtocolBase.class);

    protected Lock lk;
    protected Channel channel;

    protected ProtocolBase(Channel ch) {
        lk = new ReentrantLock();
        channel = ch;
    }

}
