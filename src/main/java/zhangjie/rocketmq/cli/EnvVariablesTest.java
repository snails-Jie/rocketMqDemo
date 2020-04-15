package zhangjie.rocketmq.cli;

/**
 * @autor zhangjie
 * @date 2020/4/15 19:03
 */
public class EnvVariablesTest {
    public static void main(String[] args) {
        String rocketmqHome = System.getenv("ROCKETMQ_HOME");
        System.out.println(rocketmqHome);
    }
}
