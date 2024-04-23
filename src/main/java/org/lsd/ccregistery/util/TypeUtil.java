package org.lsd.ccregistery.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class TypeUtil {

    public static Object cast(Class<?> origin, Object arg) {

        if (arg == null) {
            return null;
        }

        if (arg.getClass().isAssignableFrom(origin)) {
            return arg;
        }

       if (origin.isArray()) {
           if (arg instanceof List<?> argList) {
               Object array =  Array.newInstance(origin.getComponentType(), argList.size());
               for (int i = 0; i < argList.size(); i++) {
                   Array.set(array, i, argList.get(i));
               }
               return array;
           }
       }

        if (arg instanceof JSONObject jsonObject) {
            return jsonObject.toJavaObject(origin);
        }

       if (arg instanceof Map map) {
           return new JSONObject(map).toJavaObject(origin);
       }

        if (origin.equals(Double.class)  || origin.equals(double.class)) {
            return Double.valueOf(arg.toString());
        }else if (origin.equals(Float.class)  || origin.equals(float.class)) {
            return Float.valueOf(arg.toString());
        }else if (origin.equals(Short.class)  || origin.equals(short.class)) {
            return Short.valueOf(arg.toString());
        }else if (origin.equals(Byte.class)  || origin.equals(byte.class)) {
            return Byte.valueOf(arg.toString());
        }else if (origin.equals(Character.class)  || origin.equals(char.class)) {
            return Character.valueOf(arg.toString().charAt(0));
        }else if (origin.equals(Boolean.class)  || origin.equals(boolean.class)) {
            return Boolean.valueOf(arg.toString());
        }else if (origin.equals(Long.class)  || origin.equals(long.class)) {
            return Long.valueOf(arg.toString());
        }else if (origin.equals(Integer.class)  || origin.equals(int.class)) {
            return Integer.valueOf(arg.toString());
        }
       return arg;
    }
}
