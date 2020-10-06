package practica1.CircularQ;

import java.util.*;
import utils.Queue;

public class CircularQueue<E> implements Queue<E> {

    private final E[] queue;
    private final int N;
    private int numElem, ini;

    public CircularQueue(int N) {
        this.N = N;
        queue = (E[]) (new Object[N]);
        this.numElem = 0;
        this.ini = 0;
    }

    @Override
    public int size() {

        return(this.numElem);
    }

    @Override
    public int free() {
        
        return ((this.N-this.numElem));
    }

    @Override
    public boolean hasFree(int n) throws IllegalArgumentException {
        if (n<0) throw new IllegalArgumentException("El valor ha de ser més gran o igual a 0.");
        return (n <= (this.N-this.numElem));
    }

    @Override
    public boolean empty() {
        return (this.size() == 0);
    }

    @Override
    public boolean full() {
        return (this.numElem == this.N);
    }

    @Override
    public E peekFirst() {
        
        if (!this.empty()) return this.queue[this.ini];
        return null;
    }

    @Override
    public E peekLast() {
        
        if (!empty()) return this.queue[(this.ini + this.numElem - 1) % N];
        return null;
    }

    @Override
    public E get() throws IllegalStateException{
        
      if (empty()) throw new IllegalStateException("La cua està buida");
      E e = this.queue[this.ini];
      this.ini = (this.ini + 1) % N;
      this.numElem--;
      return e;
    }

    @Override
    public void put(E e) {
        
        if (this.full()) throw new IllegalStateException("La cua està plena");
        this.queue[(this.ini + this.numElem) % N] = e;
        this.numElem++;
    }

    @Override
    public Iterator<E> iterator() {
        
        return new MyIterator();

    }

    class MyIterator implements Iterator {
        
        private int elem;
        private final int P;

        
        public MyIterator() {
            this.elem = ini;
            this.P = CircularQueue.this.ini + CircularQueue.this.numElem;
        }
        
        @Override
        public boolean hasNext() {
            
            return elem < P && P != 0;
        }

        @Override
        public E next() {
            elem++;
            return queue[elem-1];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
