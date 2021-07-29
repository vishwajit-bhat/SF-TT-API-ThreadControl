package mutualexclusion;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<E> {
  private E [] data = (E[])new Object[10];
  private int count = 0;
  ReentrantLock lock = new ReentrantLock();
  Condition notEmpty = lock.newCondition();
  Condition notFull = lock.newCondition();

  public E take() throws InterruptedException {
    lock.lock();
    try {
      while (count == 0)
        notEmpty.await();
      E rv = data[0];
      System.arraycopy(data, 1, data, 0, --count);
      notFull.signal();
      return rv;
    } finally {
      lock.unlock();
    }
  }

  public void put(E e) throws InterruptedException {
    lock.lock();
    try {
      while (count == data.length)
        notFull.await();
      data[count++] = e;
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    MyBlockingQueue<int[]> queue = new MyBlockingQueue<>();
    Thread prod = new Thread(() -> {
      try {
        for (int i = 0; i < 10_000; i++) {
          int[] data = {i, 0};
          if (i < 500) {
            Thread.sleep(1);
          }
          data[1] = i;
          if (i == 5_000) data[0] = -1;
          queue.put(data);
          data = null;
        }
      } catch (InterruptedException ie) {
        System.out.println("Premature shutdown requested...");
      }
      System.out.println("Producer finished");
    });
    Thread cons = new Thread(() -> {
      try {
        for (int i = 0; i < 10_000; i++) {
          int[] data = queue.take();
          if (data[0] != data[1] || data[0] != i) {
            System.out.println("***** ERROR at count " + i);
          }
          if (i > 9_500) {
            Thread.sleep(1);
          }
        }
      } catch (InterruptedException ie) {
        System.out.println("Premature shutdown requested...");
      }
      System.out.println("Consumer finished");
    });
    prod.start();
    cons.start();
    System.out.println("main started producer and consumer");
    prod.join();
    cons.join();
    System.out.println("All threads completed");
  }
}
