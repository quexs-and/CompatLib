package com.quexs.compatlib.wheel.util;

import java.util.Random;

/**
* @date 2017/2/20
* @author Quexs
* @Description 随机数、字母 工具类
*/
public class RandomUtils {

    /**
     * 生成一个0 到 count 之间的随机数
     * @param endNum
     * @return
     */
    public static int getNum(int endNum){
        if(endNum > 0){
            Random random = new Random();
            return random.nextInt(endNum);
        }
        return 0;
    }

    /**
     * 生成一个startNum 到 endNum之间的随机数(不包含endNum的随机数)
     * @param startNum
     * @param endNum
     * @return
     */
    public static int getNum(int startNum,int endNum){
        if(endNum > startNum){
            Random random = new Random();
            return random.nextInt(endNum - startNum) + startNum;
        }
        return 0;
    }

    /**
     * 生成随机大写字母
     * @return
     */
    public static String getLargeLetter(){
        Random random = new Random();
        return String.valueOf ((char) (random.nextInt(25) + 'A'));
    }

    /**
     * 生成随机大写字母字符串
     * @return
     */
    public static String getLargeLetter(int size){
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<size;i++){
            builder.append((char) (random.nextInt(25) + 'A'));
        }
        return builder.toString();
    }

    /**
     * 生成随机小写字母
     * @return
     */
    public static String getSmallLetter(){
        Random random = new Random();
        return String.valueOf ((char) (random.nextInt(25) + 'a'));
    }

    /**
     * 生成随机小写字母字符串
     * @return
     */
    public static String getSmallLetter(int size){
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<size;i++){
            builder.append((char) (random.nextInt(25) + 'a'));
        }
        return builder.toString();
    }

    /**
     * 数字与小写字母混编字符串
     * @param size
     * @return
     */
    public static String getNumSmallLetter(int size){
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<size;i++){
            if(random.nextInt(2) % 2 == 0){//字母
                builder.append((char) (random.nextInt(25) + 'a'));
            }else{//数字
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

    /**
     * 数字与大写字母混编字符串
     * @param size
     * @return
     */
    public static String getNumLargeLetter(int size){
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<size;i++){
            if(random.nextInt(2) % 2 == 0){//字母
                builder.append((char) (random.nextInt(25) + 'A'));
            }else{//数字
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

    /**
     * 数字与大小写字母混编字符串
     * @param size
     * @return
     */
    public static String getNumLargeSmallLetter(int size){
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<size;i++){
            if(random.nextInt(2) % 2 == 0){//字母
                if(random.nextInt(2) % 2 == 0){
                    builder.append((char) (random.nextInt(25) + 'A'));
                }else{
                    builder.append((char) (random.nextInt(25) + 'a'));
                }
            }else{//数字
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

}
