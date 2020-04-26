package zhangjie.rocketmq.future;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.SocketHandler;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AsynPipelineTest {

    /**
     * 顺序查4个shop的价格 + Discount服务未4个商店的折扣
     */
    @Test
    public void demo(){
        long start = System.nanoTime();
        List<String> list = findPrices("BestPriice");
        list.forEach(str -> System.out.println(str));
        long duration =(System.nanoTime() - start) /1_000_000;
        System.out.println("Done in "+duration +" msecs");//Done in 8096 msecs
    }

    @Test
    public void testCompletableFuture(){
        long start = System.nanoTime();
        List<String> list = findPricesByCompletableFuture("BestPriice");
        list.forEach(str -> System.out.println(str));
        long duration =(System.nanoTime() - start) /1_000_000;
        System.out.println("Done in "+duration +" msecs");//Done in 2068 msecs
    }


    /**
     * 将两个完全不相干的CompletableFuture对象结果整合起来,但不希望等到第一个任务完成结束后开始第二个任务
     * ThenCombine方法
     *  1. 接受名为BigFunction的第二个参数
     *     --> 定义当两个CompletableFuture对象完成计算后，结果如何合并
     *  2. thenCombine提供一个Async的版本
     *    -->导致BigFunction定义的合并操作被提交到线程池中
     */
    @Test
    public void testThenCombine() throws InterruptedException, ExecutionException, TimeoutException {
        Shop shop = new Shop("11111");
        Future<Double> futurePrice = CompletableFuture.supplyAsync(() -> shop.getPrice("test"))
                                    .thenCombine(
                                            CompletableFuture.supplyAsync(() -> getRate()),
                                            (price,rate) -> price *rate
                                    );
        Double a = futurePrice.get(10, TimeUnit.SECONDS);
        System.out.println(a);
    }

    /**
     * 响应CompletableFuture的completion事件
     *  thenAccept：接收CompletableFuture执行完毕后的返回值做参数
     *   1. 一旦CompletableFuture计算得到结果，它就会返回一个CompletableFuture<Void>
     *       -->只能等待其结束
     *   2. CompletableFuture#allOf 接收一个
     */
    @Test
    public void testReative(){
        long start = System.nanoTime();
        CompletableFuture[] futures = findPricesStream("BestPriice")
                                        .map(f -> f.thenAccept(
                                                s -> System.out.println(s + "( done in " + (System.nanoTime() -start)/1_000_000 +" msecs)")
                                        ))
                                        .toArray(size -> new CompletableFuture[size]);
        CompletableFuture.allOf(futures).join();
        System.out.println("All shops have now responded in " + (System.nanoTime() -start)/1_000_000 +" msecs");

        long duration =(System.nanoTime() - start) /1_000_000;
        System.out.println("Done in "+duration +" msecs");//Done in 2068 msecs
    }

    /**
     *  1.执行三次map
     *   1.1 将每个shop对象转换成一个字符串
     *   1.2 对字符串进行解析，转换成Quote对象(同步操作不会带来太多的延迟)
     *   1.3 联系远程的Discount服务，计算最终的折扣价格
     */
    public List<String> findPrices(String product){
        List<Shop> shops = Arrays.asList(new Shop("BestPriice"),
                new Shop("LetsSaveBig"),
                new Shop("MyFavoriteShop"),
                new Shop("BuyItAll"));
        return shops.stream()
                .map(shop -> shop.getPriceAndDiscountCode(product))
                .map(Quote::parse)
                .map(Discount::applyDiscount)
                .collect(Collectors.toList());
    }

    /**
     * 以CompletableFuture来重构
     * 1.以异步方式取得每个shop指定产品的原始价格
     * 2. Quote对象存在时，对其返回的值进行转换
     * 3. 使用另一个异步任务构造期望的Future,申请折扣
     * ===========================================
     * 1. CompletableFuture的thenCompose方法允许你对两个异步操作进行流水线
     *    --> 前一个结果作为参数执行第二个异步函数
     *    1.1 名字中不带Async的方法和它前一个任务一样，在同一个线程中运行
     *    1.2 名字带Async的方法将后续的任务提交到一个线程池
     *   -->本例中第二个CompletableFuture对象的结果取决于第一个CompletableFuture，选择thenCompose更高效，减少线程切换的开销
     */
    public List<String> findPricesByCompletableFuture(String product){
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Shop> shops = Arrays.asList(new Shop("BestPriice"),
                new Shop("LetsSaveBig"),
                new Shop("MyFavoriteShop"),
                new Shop("BuyItAll"));
        List<CompletableFuture<String>> priceFutures = shops.stream()
                                                        .map(shop -> CompletableFuture.supplyAsync(()->shop.getPriceAndDiscountCode(product),executor))
                                                        .map(futrue -> futrue.thenApply(Quote::parse))
                                                        .map(future -> future.thenCompose(quote ->
                                                                        CompletableFuture.supplyAsync(()->Discount.applyDiscount(quote),executor)))
                                                        .collect(Collectors.toList());
        return priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public Stream<CompletableFuture<String>> findPricesStream(String product){
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Shop> shops = Arrays.asList(new Shop("BestPriice"),
                new Shop("LetsSaveBig"),
                new Shop("MyFavoriteShop"),
                new Shop("BuyItAll"));
        return shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPriceAndDiscountCode(product),executor))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> (future.thenCompose(quote ->
                            CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote),executor))));
    }


    public Integer getRate(){
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 2;
    }


}
