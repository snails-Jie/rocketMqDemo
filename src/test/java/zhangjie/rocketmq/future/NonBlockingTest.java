package zhangjie.rocketmq.future;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NonBlockingTest {
    /**
     * 对四个商店查询是顺序进行的，并且一个查询操作会阻塞另一个
     */
    @Test
    public void testBlocking(){
        long start = System.nanoTime();
        System.out.println(findPrices("BestPriice"));
        long duration =(System.nanoTime() - start) /1_000_000;
        System.out.println("Done in "+duration +" msecs"); //Done in 4077 msecs
    }

    /**
     * 使用并行流来避免顺序计算
     *  机器是否足以以并行方式运行四个线程
     */
    @Test
    public void testNonBlockingByParalleStream(){
        long start = System.nanoTime();
        System.out.println(findPricesByParalleStream("BestPriice"));
        long duration =(System.nanoTime() - start) /1_000_000;
        System.out.println("Done in "+duration +" msecs");//Done in 1079 msecs
    }

    /**
     * 1.并行流和CompeletableFuture内部采用的是同样的通用线程池
     *  --> 具体线程数取决于Runtime.getRuntime().availableProcessors()的返回值
     * 2. CompeletableFuture 允许你对执行器（Executor）进行配置
     */
    @Test
    public void testNonBlockingByCompeletableFuture(){
        long start = System.nanoTime();
        System.out.println(findPricesByCompletableFuture("BestPriice"));
        long duration =(System.nanoTime() - start) /1_000_000;
        System.out.println("Done in "+duration +" msecs");//Done in 2086 msecs
    }

    public List<String> findPrices(String product){
        List<Shop> shops = Arrays.asList(new Shop("BestPriice"),
                new Shop("LetsSaveBig"),
                new Shop("MyFavoriteShop"),
                new Shop("BuyItAll"));
        return shops.stream()
                .map(shop -> String.format("%s price is %.2f",shop.getName(),shop.getPrice(product)))
                .collect(Collectors.toList());
    }

    public List<String> findPricesByParalleStream(String product){
        List<Shop> shops = Arrays.asList(new Shop("BestPriice"),
                new Shop("LetsSaveBig"),
                new Shop("MyFavoriteShop"),
                new Shop("BuyItAll"));
        return shops.parallelStream()
                .map(shop -> String.format("%s price is %.2f",shop.getName(),shop.getPrice(product)))
                .collect(Collectors.toList());
    }

    /**
     * 1.对List中的所有future对象执行join操作，一个接一个地等待它们运行结束
     *   --> CompletableFuture类中的join方法和Future接口中的get由相同的含义
     *      --> join不会抛出任何检测到的异常
     * 2. 使用了两个不同的Stream流水线，而不是在同一个处理流的流水线上一个接一个地放置两个map操作
     *    --> 考虑流操作之间的延迟特性，如果你在单一流水线中处理流，只能以同步、顺序执行方式才会成功
     */
    public List<String> findPricesByCompletableFuture(String product){
        List<Shop> shops = Arrays.asList(new Shop("BestPriice"),
                new Shop("LetsSaveBig"),
                new Shop("MyFavoriteShop"),
                new Shop("BuyItAll"));
        List<CompletableFuture<String>> priceFutures =  shops.stream()
                                                    .map(shop -> CompletableFuture.supplyAsync(
                                                            ()-> String.format("%s price is %.2f",
                                                                    shop.getName(),shop.getPrice(product))
                                                                )
                                                        )
                                                    .collect(Collectors.toList());
        return priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
}
