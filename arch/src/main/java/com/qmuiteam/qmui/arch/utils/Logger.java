package com.qmuiteam.qmui.arch.utils;

import android.util.Log;

/**
 * Log wrapper
 */

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class Logger {

    private static final String TAG = "Logger";

    private static int mLevel = Log.INFO;


    public static void setLogLevel(int level) {
        mLevel = level;
    }

    public static int getLogLevel() {
        return mLevel;
    }

    public static boolean debug() {
        return mLevel <= Log.DEBUG;
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void v(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.VERBOSE)) {
            sLogWriter.logV(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (msg == null && tr == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.VERBOSE)) {
            sLogWriter.logV(tag, msg, tr);
        }
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.DEBUG)) {
            sLogWriter.logD(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (msg == null && tr == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.DEBUG)) {
            sLogWriter.logD(tag, msg, tr);
        }
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.INFO)) {
            sLogWriter.logI(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (msg == null && tr == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.INFO)) {
            sLogWriter.logI(tag, msg, tr);
        }
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.WARN)) {
            sLogWriter.logW(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (msg == null && tr == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.WARN)) {
            sLogWriter.logW(tag, msg, tr);
        }
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.ERROR)) {
            sLogWriter.logE(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (msg == null && tr == null) {
            return;
        }
        if (sLogWriter.isLoggable(Log.ERROR)) {
            sLogWriter.logE(tag, msg, tr);
        }
    }

    @Deprecated
    public static void k(String msg) {
        k(TAG, msg);
    }

    @Deprecated
    public static void k(String tag, String msg) {
        if (sLogWriter.isLoggable(Log.DEBUG)) {
            sLogWriter.logK(tag, msg);
        }

    }

    public static void st(String tag, int depth) {
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] elems = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < Math.min(depth, elems.length); i++) {
                if (i > 1) {
                    sb.append("\n");
                }
                sb.append(getSimpleClassName(elems[i].getClassName()));
                sb.append(".");
                sb.append(elems[i].getMethodName());
            }
            v(tag, sb.toString());
        }
    }

    private static String getSimpleClassName(String fullClassName) {
        int index = fullClassName.lastIndexOf('.');
        if (index < 0) {
            return fullClassName;
        }
        return fullClassName.substring(index + 1);
    }

    public static void throwException(Throwable exception) {
        if (exception == null) {
            return;
        }
        exception.printStackTrace();
        if (Logger.debug()) {
            throw new RuntimeException("Error! Now in debug, we alert to you to correct it !", exception);
        }
    }

    public static void alertErrorInfo(String errorMsg) {
        if (Logger.debug()) {
            throw new IllegalStateException(errorMsg);
        }
    }

    private static ILogWritter sLogWriter = DefaultLogHandler.getInstance();

    public static void registerLogHandler(ILogWritter wrapper) {
        sLogWriter = wrapper;
    }

    public abstract static class ILogWritter {

        /*是否可打印或者写入文件*/
        public boolean isLoggable(int level) {
            return getLogLevel() <= level;
        }

        public void logV(String tag, String msg) {

        }

        public void logV(String tag, String msg, Throwable throwable) {

        }

        public void logD(String tag, String msg) {

        }

        public void logD(String tag, String msg, Throwable throwable) {

        }

        public void logI(String tag, String message) {

        }

        public void logI(String tag, String msg, Throwable throwable) {

        }

        public void logE(String tag, String message, Throwable e) {

        }

        public void logE(String tag, String msg) {

        }

        public void logW(String tag, String msg) {

        }

        public void logW(String tag, String msg, Throwable throwable) {

        }

        public void logK(String tag, String message) {

        }

    }

    private static final class DefaultLogHandler extends ILogWritter {

        private DefaultLogHandler() {
        }

        static DefaultLogHandler getInstance() {
            return SingletonHolder.INSTANCE;
        }

        @Override
        public void logV(String tag, String msg) {
            Log.v(tag, msg);
        }

        @Override
        public void logV(String tag, String msg, Throwable throwable) {
            Log.v(tag, msg, throwable);
        }

        @Override
        public void logD(String tag, String msg) {
            Log.d(tag, msg);
        }

        @Override
        public void logD(String tag, String msg, Throwable throwable) {
            Log.d(tag, msg, throwable);
        }

        @Override
        public void logI(String tag, String msg) {
            Log.i(tag, msg);
        }

        @Override
        public void logI(String tag, String msg, Throwable throwable) {
            Log.i(tag, msg, throwable);
        }

        @Override
        public void logE(String tag, String msg, Throwable throwable) {
            Log.e(tag, msg, throwable);
        }

        @Override
        public void logE(String tag, String msg) {
            Log.e(tag, msg);
        }

        @Override
        public void logW(String tag, String msg) {
            Log.w(tag, msg);
        }

        @Override
        public void logW(String tag, String msg, Throwable throwable) {
            Log.w(tag, msg, throwable);
        }

        private static class SingletonHolder {
            private static final DefaultLogHandler INSTANCE = new DefaultLogHandler();
        }
    }
}
