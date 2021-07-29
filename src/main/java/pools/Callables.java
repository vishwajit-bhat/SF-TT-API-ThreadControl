package pools;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

class MyCallable implements Callable<String> {
  private static int nextTaskId = 0;
  private int myTaskId = nextTaskId++;
  @Override
  public String call() throws Exception {
    System.out.println(Thread.currentThread().getName() + " starting, task ID " + myTaskId);
    try {
      Thread.sleep((int)(Math.random() * 2000) + 1000);
      if (Math.random() > 0.7) throw new SQLException("DB Broke!!!");
    } catch (InterruptedException e) {
      System.out.println("Shutdown requested...");;
    }
    System.out.println(Thread.currentThread().getName() + " ending, task ID " + myTaskId);
    return "Task, ID " + myTaskId + " has finished";
  }
}

public class Callables {
  public static void main(String[] args) throws Throwable {
    ExecutorService ex = Executors.newFixedThreadPool(2);
    List<Future<String>> handles = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      handles.add(ex.submit(new MyCallable()));
    }
    System.out.println("All tasks submitted");
    ex.shutdown();
    System.out.println("shutdown requested");

    while (handles.size() > 0) {
      Iterator<Future<String>> ifs = handles.iterator();
      while (ifs.hasNext()) {
        Future<String> handle = ifs.next();
//        handle.cancel...
        if (handle.isDone()) {
          String rv = handle.get();
          System.out.println("task returned " + rv);
          ifs.remove();
        }
      }
    }

    ex.awaitTermination(1, TimeUnit.DAYS);
    System.out.println("terminated...");
  }
}
