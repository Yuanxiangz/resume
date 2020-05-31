package test.demo;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Created by yuanxiangz on 5/31/20.
 * //评测题目: //1.启动a、b两个线程，由这两个线程打印出1到100共一百个数字。要求：a线程打印1、3、5、7等奇数， b线程打印2、4、6、8等偶数。依次串 行打印，即打印完1之后，再打印2，然后是3、4、5…直到100，全部打印完成后，进程能正常结束
 */
public class Task1 {
    static int num = 1;
    public static void main(String[] args) {
        Semaphore c1 = new Semaphore(1);
        Semaphore c2 = new Semaphore(1);
        try {
            //block thread 2 first
            c2.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread thread1 = new Thread(new Consumer(1,c1, c2));
        Thread thread2 = new Thread(new Consumer(2,c1, c2));
        thread1.start();
        thread2.start();
    }

    public static class Consumer implements Runnable{
        private int threadId;
        private Semaphore c1;
        private Semaphore c2;

        public Consumer(int threadId, Semaphore c1, Semaphore c2){
            this.threadId = threadId;
            this.c1 = c1;
            this.c2 = c2;
        }

        public void run() {
            try{
                while (num < 100){
                    if(threadId == 1 && num < 100){
                        c1.acquire();
                        System.out.println(String.format("thread 1 print: %d", num));
                        num++;
                        c2.release();
                    }

                    if(threadId == 2 && num < 100){
                        c2.acquire();
                        System.out.println(String.format("thread 2 print: %d", num));
                        num++;
                        c1.release();
                    }
                }
                System.out.println(String.format("thread %d exit", threadId));
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}
