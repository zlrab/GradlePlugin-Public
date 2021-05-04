package com.zlrab.tool;

import com.zlrab.plugin.MainPlugin;
import com.zlrab.plugin.work.LogManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

/**
 * @author zlrab
 * @date 2020/12/24 10:47
 */
public class LogTool {
    public static final String TAG = "ZTag";

    public static boolean debug = true;

    public static void e(String str) {
        e(str, null);
    }

    public static void e(String str, Throwable throwable) {
        if (debug) {
            MainPlugin.logger.error("[" + TAG + "]E:\t" + str + "\t throwable = " + getStackTraceString(throwable));
        }
        if (LogManager.ready())
            LogManager.getInstance().logWrite(str + "\t throwable = " + getStackTraceString(throwable));
    }

    public static void w(String str) {
        if (debug)
            MainPlugin.logger.warn("[" + TAG + "]W:\t" + str);
        if (LogManager.ready()) LogManager.getInstance().logWrite(str);
    }

    public static void d(String str) {
        if (debug)
            MainPlugin.logger.error("[" + TAG + "]W:\t" + str);
        if (LogManager.ready()) LogManager.getInstance().logWrite(str);
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 256);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

}
