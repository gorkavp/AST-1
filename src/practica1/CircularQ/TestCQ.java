package practica1.CircularQ;

import java.util.*;

public class TestCQ {

    public static void main(String[] args) {
        
        CircularQueue cua = new CircularQueue(10);
        
        System.out.print("La cua està ");
        if (cua.empty()) System.out.println("buida.");
        else System.out.println("plena.");
        for (int i=0; i<8; i++) {
            cua.put(i);
        }
        System.out.println("Hi ha lliure 5 elements? " + cua.hasFree(5));
        System.out.println("Hi ha lliure 2 elements? " + cua.hasFree(2));
        System.out.println("Està buida la cua? " + cua.empty());
        System.out.println("Està plena la cua?: " + cua.full());
        System.out.println("Primer element de la cua " + cua.peekFirst());
        System.out.println("Últim element de la cua " + cua.peekLast());
        
        for (int i=8; i<10; i++){
            cua.put(i);
        }
        System.out.print("La cua ");
        if (cua.full()) System.out.println("està plena.");
        else System.out.println("no està plena.");
        System.out.println("Últim element de la cua " + cua.peekLast());
        
        Iterator it = cua.iterator();
        while (it.hasNext()) System.out.print(it.next() + ", ");
        System.out.println("");
    }
}
