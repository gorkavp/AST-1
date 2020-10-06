package practica1.LinkedQ;

import java.util.Iterator;
import utils.Queue;

public class LinkedQueue<E> implements Queue<E> {

    private final E[] queue;
    private int N = 0;
    Node<E> primer, ultim;

    public LinkedQueue() {
        queue = (E[]) (new Object[0]);
    }


    @Override
    public int size() {
        return N;
    }

    @Override
    public int free() {
        return 0;
    }

    @Override
    public boolean hasFree(int n) {
        if (n<0) throw new IllegalArgumentException("El valor ha de ser més gran o igual a 0.");
        return (n <= 0);
    }

    @Override
    public boolean empty() {
       return (this.size() == 0);
    }

    @Override
    public boolean full() {
        return size() == this.N;
    }

    @Override
    public E peekFirst() {
        
        if (!this.empty()) return primer.getValue();
        return null;
    }

    @Override
    public E peekLast() {
        if (!this.empty()) return ultim.getValue();
        return null;
    }

    @Override
    public E get() {
        if (empty()) throw new IllegalStateException("La cua està buida");
      E e = this.peekFirst();
      this.primer = this.primer.getNext();
      this.N--;
      return e;
    }

    @Override
    public void put(E value) {
      Node<E> n = new Node();
      n.setValue(value);
      if(this.size()==0) {
          this.primer = n;
          this.ultim = n;
      }
      this.ultim.setNext(n);
      this.ultim = n;
      this.N++;
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    class MyIterator implements Iterator {

        private int elem;

        public MyIterator() {
            this.elem = 0;
        }
        
        @Override
        public boolean hasNext() {
            return(this.elem < LinkedQueue.this.N);
        }

        @Override
        public E next() {
            
            Node<E> n = new Node();
            n = LinkedQueue.this.primer;
            for (int i = 0; i < this.elem; i++)
                n = n.getNext();
            this.elem++;
            return n.getValue();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
