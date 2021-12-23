package com.zelgius.mjpg_streamer;

import java.lang.reflect.Array;

public class JavaUtils {

    public static <T> T[] array(Class<T> tClass, int size) {
        return (T[]) Array.newInstance(tClass, size);
    }
}
