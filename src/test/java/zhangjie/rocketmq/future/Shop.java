package zhangjie.rocketmq.future;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 异步api
 */
public class Shop {

    private String name;

    public Shop(String name) {
        this.name = name;
    }
     
    public String getPriceAndDiscountCode(String product){
        double price = caculatePrice(product);
        Discount.Code code =Discount.Code.values()[new Random().nextInt(Discount.Code.values().length)];
        return String.format("%s:%.2f:%s",name,price,code);
    }

    /**
     * 同步api
     *  完成需要等待1秒
     */
    public double getPrice(String product){
        return caculatePrice(product);
    }

    /**
     * 异步api
     *  调用立刻返回一个Future对象
     * 用于提示错误的异常会被限制在当前线程范围内，最终会杀死线程
     *  -->导致等待get方法返回结果的客户端永久地被阻塞
     */
    public Future<Double> getPriceAsync(String product){
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(()->{
            double price = caculatePrice(product);
            futurePrice.complete(price);
        }).start();
        return futurePrice;
    }

    /**
     * 工厂方法创建，不用担心实现的细节
     *  supplyAsync方法接受一个生产者(Supplier)作为参数，返回一个CompletetableFuture对象（异步执行后会读取调用产生着方法的返回值）
     *  生产者方法会交由ForkJoinPool池中的某个执行线程（Executor）运行
     */
    public Future<Double> getPriceAsyncByFactoryMethod(String product){
        return CompletableFuture.supplyAsync(() -> caculatePrice(product));
    }

    /**
     * 异步API -错误处理
     * 客户端会收到ExecutionException异常，该异常接收一个包含失败原因的Exception参数
     */
    public Future<Double> getPriceAsyncDealException(String product){
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(()->{
            try{
                double price = caculatePriceException(product);
                futurePrice.complete(price);
            }catch (Exception ex){
                futurePrice.completeExceptionally(ex);
            }
        }).start();
        return futurePrice;
    }



    private double caculatePriceException(String product){
        throw new RuntimeException("计算商品价格异常");
    }

    private double caculatePrice(String product){
        deplay();
        return new Random().nextDouble() * product.charAt(0) + product.charAt(1);
    }

    public static void deplay(){
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
