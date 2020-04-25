package zhangjie.rocketmq.future;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CurrentFutureTest {

    @Test
    public void demo(){
        Shop shop = new Shop("BestShop");
        long start = System.nanoTime();
        Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
        long invocationTime =(System.nanoTime() - start) /1_000_000;
        System.out.println("Invocation returned after "+invocationTime +" msecs");

        //执行更多任务，比如查询其他商店
        doSomethingElse();
        //在计算商品价格的同时
        try{
            double price = futurePrice.get();
            System.out.printf("Price is %.2f%n",price);
        }catch (Exception e){
            throw new RuntimeException();
        }

        long retrievalTime = (System.nanoTime() -start) /1_000_000;
        System.out.println("Price returned after "+retrievalTime+" msecs");

    }

    @Test
    public void testExcetion(){
        Shop shop = new Shop("BestShop");
        Future<Double> futurePrice = shop.getPriceAsyncDealException("my favorite product");
        try {
            futurePrice.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {//捕获到异常
            e.printStackTrace();
        }

    }

    @Test
    public void testFactoryMethod(){
        Shop shop = new Shop("BestShop");
        long start = System.nanoTime();
        Future<Double> futurePrice = shop.getPriceAsyncByFactoryMethod("my favorite product");
        long invocationTime =(System.nanoTime() - start) /1_000_000;
        System.out.println("Invocation returned after "+invocationTime +" msecs");

        //执行更多任务，比如查询其他商店
        doSomethingElse();
        //在计算商品价格的同时
        try{
            double price = futurePrice.get();
            System.out.printf("Price is %.2f%n",price);
        }catch (Exception e){
            throw new RuntimeException();
        }

        long retrievalTime = (System.nanoTime() -start) /1_000_000;
        System.out.println("Price returned after "+retrievalTime+" msecs");

    }


    private void doSomethingElse(){
        System.out.println("做其他的事情");
    }
}
