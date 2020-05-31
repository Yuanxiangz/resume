package test.demo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yuanxiangz on 5/31/20.
 * //2.1000个IP，每个IP有个属性表示有没有被VM占用，创建VM的时候需要选择一个空闲的IP（即没有被占用的），10个并发请求来创建VM，如何提高分配IP的并发
 */
public class Task2 {
    static AtomicInteger count = new AtomicInteger(0);
    static Random random = new Random();
    static Random random2 = new Random();
    public static void main(String[] args) {
        int ipCount = 1000;
        int threadCount = 10;
        //床架你1000个IP
        List<IPItem> ips = mockRandomIp(ipCount);
        //分配到IP池中
        List<LinkedList<IPItem>> ipPool = allocateIpsInIPPool(ips, threadCount);

        List<Thread> consumers = new ArrayList<Thread>();
        for(int i=0;i<threadCount;i++){
            Thread thread = new Thread(new Consumer(i, ipPool, threadCount));
            consumers.add(thread);
            thread.start();
        }
    }

    //todo: has dup ips
    public static List<IPItem> mockRandomIp(int count) {
        List<IPItem> idleIps = new ArrayList<IPItem>();
        for(int i=0;i<count;i++){
            String part1 = String.valueOf(random.nextInt(255));
            String part2 = String.valueOf(random.nextInt(255));
            String part3 = String.valueOf(random.nextInt(255));
            String part4 = String.valueOf(random.nextInt(255));

            idleIps.add(new IPItem(part1+"."+part2+"."+part3+"."+part4, random2.nextBoolean()));
        }

        return idleIps;
    }

    public static List<LinkedList<IPItem>> allocateIpsInIPPool(List<IPItem> idleIps, int threadCount){
        List<LinkedList<IPItem>> ipPool = new ArrayList<LinkedList<IPItem>>();
        if(idleIps == null || idleIps.size() == 0){
            return ipPool;
        }

        int averageCount = idleIps.size() / threadCount;
        for(int i=0;i<threadCount;i++){
            LinkedList<IPItem> linkedList = new LinkedList<IPItem>();
            if(averageCount> 0 && i<(threadCount-1)){
                List<IPItem> list = idleIps.subList(i*averageCount, (i+1)*averageCount);
                linkedList.addAll(list);
            }

            if(i == (threadCount -1)){
                //剩余的放最后一个链表里
                List<IPItem> list = idleIps.subList(i*averageCount, idleIps.size());
                linkedList.addAll(list);
            }

            ipPool.add(linkedList);
        }

        return ipPool;
    }

    public static class IPItem{
        private String ip;
        private boolean idle;

        public IPItem(String ip, boolean idle){
            this.ip = ip;
            this.idle = idle;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public boolean isIdle() {
            return idle;
        }

        public void setIdle(boolean idle) {
            this.idle = idle;
        }

        @Override
        public String toString() {
            return String.format("ip: %s, idle: %b", ip, idle).toString();
        }
    }

    public static class Consumer implements Runnable {
        private int threadId;
        private List<LinkedList<IPItem>> ipPool;
        private int threadCount;

        public Consumer(int threadId, List<LinkedList<IPItem>> ipPool, int threadCount){
            this.threadId = threadId;
            this.ipPool = ipPool;
            this.threadCount = threadCount;
        }

        public void run() {
            if(ipPool == null || ipPool.size() == 0){
                System.out.println("ippool is empty!");
            }

            int mod = threadId%threadCount;
            LinkedList<IPItem> linkedList = ipPool.get(mod);
            if(linkedList == null || linkedList.size()==0){
                System.out.println("current mod ip is empty");
                return;
            }

            while (linkedList.size()>0){
                IPItem ipItem = linkedList.removeFirst();
                System.out.println(String.format("thread %d consume count %d", threadId, count.addAndGet(1)));
                if(ipItem.isIdle()) {
                    System.out.println(String.format("thread %d consume ip: %s", threadId, ipItem.toString()));
                } else {
                    System.out.println(String.format("thread %d drop ip: %s", threadId, ipItem.toString()));
                }

            }
        }
    }
}

