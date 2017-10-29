package com.mall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by faithpercious on 2017/10/15.
 */
public class Tokencahe {
    public  static final String TOKEN_PREFIX="token_";
    private static Logger logger= LoggerFactory.getLogger(Tokencahe.class);
    public static LoadingCache<String,String> loadCache=CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).
            build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return"null";
                }
            });
    public static  void setkey(String key,String value){
        loadCache.put(key,value);
    }
    public  static  String getkey(String key){
        String value=null;
        try {
            value=loadCache.get(key);
            if ("null".equals(value)){
                return null;
            }
            else return  value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }
}
