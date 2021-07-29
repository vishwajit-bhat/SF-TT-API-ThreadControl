package pools;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MyJob implements Runnable {
  private static int nextTaskId = 0;
  private int myTaskId = nextTaskId++;
  @Override
  public void run() {
    System.out.println(Thread.currentThread().getName() + " starting, task ID " + myTaskId);
    try {
      Thread.sleep((int)(Math.random() * 2000) + 1000);
    } catch (InterruptedException e) {
      System.out.println("Shutdown requested...");;
    }
    System.out.println(Thread.currentThread().getName() + " ending, task ID " + myTaskId);
  }
}

public class Ex1 {
  public static void main(String[] args) throws Throwable {
    ExecutorService ex = Executors.newFixedThreadPool(2);
    for (int i = 0; i < 8; i++) {
      ex.execute(new MyJob());
    }
    System.out.println("All tasks submitted");
    ex.shutdown();
    System.out.println("shutdown requested");
//    ex.execute(new MyJob());
//    ex.awaitTermination(1, TimeUnit.DAYS);
    System.out.println("terminated...");
  }
}
