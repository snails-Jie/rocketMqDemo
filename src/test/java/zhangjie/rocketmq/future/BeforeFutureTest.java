package zhangjie.rocketmq.future;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * jdk8之前使用例子
 */
public class BeforeFutureTest {

    @Test
    public void demo(){
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return doSomeLongComputation();
            }
        });
        doSomethingElse();

        try {
            System.out.println("耗时操作是否执行完："+future.isDone());
            Thread.sleep(11000);
            System.out.println("耗时操作是否执行完："+future.isDone());
            Boolean result =  future.get();
//           Boolean result =  future.get(15,TimeUnit.SECONDS);
            System.out.println("耗时操作的结果为："+result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//        catch (TimeoutException e) {//在指定的时间没有获取到结果会报超时
//            e.printStackTrace();
//        }
    }

    public boolean doSomeLongComputation() {
        try {
            System.out.println("做一些耗时的操作");
            Thread.sleep(10000);
            System.out.println("耗时操作完成");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void doSomethingElse(){
        System.out.println("做其他的事情");
    }
}
