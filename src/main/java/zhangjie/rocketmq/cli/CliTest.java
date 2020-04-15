package zhangjie.rocketmq.cli;

import org.apache.commons.cli.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @autor zhangjie
 * @date 2020/4/15 18:10
 */
public class CliTest {


    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        /**
         * 第二个参数false表示不支持参数值  true表示支持参数值
         */
        options.addOption("t",false,"dispaly current time");
        options.addOption("c",true,"country code");
        options.addOption("f",true,"file path");

        /**
         * Posix类型的Parser
         * Posix命令行的格式为： -参数名  参数值
         */
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options,args);
        if(cmd.hasOption("c")){
            String countryCode = cmd.getOptionValue("c");
            System.out.println(countryCode);
        }
        if(cmd.hasOption("t")){
            String currentTime = cmd.getOptionValue("t");
            System.out.println(currentTime);
        }

        if(cmd.hasOption("f")){
            String filePath = cmd.getOptionValue("f");
            Properties properties = convert(filePath);
            System.out.println(properties.getProperty("ad"));
        }

        Properties all = commandLine2Properties(cmd);

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("CliTest",options,true);
    }

    /**
     * 将命令行的值转换为Properties
     * @param commandValue 文件路径
     */
    protected  static Properties convert(String commandValue){
        Properties properties = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(commandValue));
            properties.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 将命令行的值转换为Properties
     * @param commandLine
     * @return
     */
    public static Properties commandLine2Properties(final CommandLine commandLine) {
        Properties properties = new Properties();
        Option[] opts = commandLine.getOptions();

        if (opts != null) {
            for (Option opt : opts) {
                String name = opt.getOpt();
                String value = commandLine.getOptionValue(name);
                if (value != null) {
                    properties.setProperty(name, value);
                }
            }
        }

        return properties;
    }
}
