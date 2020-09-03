package cn.hlh.rpc.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SingletonFactory {
    private static volatile Map<Class,Object> objectMap = new HashMap<>();
    public static <T> T getInstance(Class<T> clazz){
        T object = (T) objectMap.get(clazz);
        if(object==null){
            synchronized (clazz){
                if(object==null){
                    try {
                        Constructor<T> constructor = clazz.getDeclaredConstructor(null);
                        constructor.setAccessible(true);
                        object = constructor.newInstance();
                        objectMap.put(clazz,object);
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
        return object;
    }
}
