package net.coding.program.message;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AmrUtils {

    /**
     * Created by Carlos2015 on 2015/8/28.
     * converts 16 bit PCM to AMR
     */
    public static byte[] convertToAmr(InputStream inputStream, boolean isAddAmrFileHead) throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        Class<?> cls = Class.forName("android.media.AmrInputStream");
        Constructor<?> c = cls.getDeclaredConstructor(InputStream.class);
        c.setAccessible(true);
        Object amrInputStream = c.newInstance(inputStream);
        byte[] buf = new byte[1024];
        Log.w("convertToAmr", "byte[]的类型是:" + buf.getClass());
        Method read = cls.getDeclaredMethod("read", buf.getClass());
        read.setAccessible(true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = 0;
        //添加AMR-NB文件头:"#!AMR\n"(0x2321414d520a,引号内的部分)
        if (isAddAmrFileHead) {
            bos.write(0x23);
            bos.write(0x21);
            bos.write(0x41);
            bos.write(0x4D);
            bos.write(0x52);
            bos.write(0x0A);
        }
        while ((len = (Integer) read.invoke(amrInputStream, buf)) > 0) {
            bos.write(buf, 0, len);
        }
        Method close = cls.getMethod("close");
        close.setAccessible(true);
        close.invoke(amrInputStream);
        buf = bos.toByteArray();
        bos.close();
        return buf;
    }
}
