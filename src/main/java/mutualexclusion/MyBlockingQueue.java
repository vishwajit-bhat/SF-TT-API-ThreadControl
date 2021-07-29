package mutualexclusion;

public class MyBlockingQueue<E> {
  private E [] data = (E[])new Object[10];
  private int count = 0;

  public E take() throws InterruptedException {
    synchronized (this) { // once entered, the ONLY way to resume is to gain the lock
      while (count == 0)
        this.wait();
      E rv = data[0];
      System.arraycopy(data, 1, data, 0, --count);
//      this.notify();
      this.notifyAll();
      return rv;
    }
  }

  public void put(E e) throws InterruptedException {
    synchronized (this) {
      while (count == data.length)
        this.wait();
      data[count++] = e;
//      this.notify();
      this.notifyAll(); // notifies ALL!!!
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
