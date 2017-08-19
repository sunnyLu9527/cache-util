package com;

import com.htt.app.cache.utils.JedisUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * 程序主入口
 * Created by sunnyLu on 2017/5/27.
 */
public class Main {
    public static void main(String[] args) {

        try {
            ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:/*.xml");
            String operator = args[0];
            int size = args.length;
            //判断database是否默认
            int database = JedisUtils.DATA_BASE;
            if("-d".equalsIgnoreCase(args[size-2])){
                database = Integer.valueOf(args[size-1]);
            }
            if ("hset".equalsIgnoreCase(operator)){
                String key = args[1];
                String field = args[2];
                String value = args[3];
                JedisUtils.hsetexToJedis(key,field,value,database);
                System.out.println("ok");
            } else if("hget".equalsIgnoreCase(operator)){
                String key = args[1];
                String field = args[2];
                String value = JedisUtils.getFromJedis(key,field,database);
                System.out.println(value);
            } else if("hexists".equalsIgnoreCase(operator)){
                String key = args[1];
                String field = args[2];
                if(JedisUtils.isExists(key,field,database)){
                    System.out.println("true");
                }else{
                    System.out.println("false");
                }
            } else if("del".equalsIgnoreCase(operator)){
                List<String> keyList = new LinkedList<String>();
                for (int i = 1; i < args.length; i++) {
                    if (!"-d".equalsIgnoreCase(args[i])){
                        keyList.add(args[i]);
                    } else {
                        break;
                    }
                }
                JedisUtils.delKeys(database,(String[])keyList.toArray(new String[keyList.size()]));
                System.out.println("ok");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            printHelp();
        }
    }


    private static void printHelp() {
        print("===============================");
        print("应用配置导入程序");
        print("Author:sunnyLu");
        print("Email:980921840@qq.com");
        print("Tel:18758040464");
        print("参数有异常，请详见REDEME.txt");
        print("祝使用愉快！！");
    }

    private static void print(String str) {
        System.out.println(str);
    }
}
