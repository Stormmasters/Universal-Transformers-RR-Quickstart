package org.firstinspires.ftc.teamcode.utils.functions;

// Explanation of logger:
// Essentially, it's FTC logs in a format similar to kernel messages. This was mostly vibecoded
// partially because I don't know how to write to storage in an environment as locked down as an
// FTC opmode and partially because I couldn't be bothered to format anything in java.

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Logger {
    private static FileWriter textWriter;
    private static FileWriter csvWriter;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String LOG_DIR = "/sdcard/FIRST";
    private static final int MAX_LOG_FILES = 40;
    private static boolean disabled = false;
    private static boolean initialized = false;

    public static void disable() {
        disabled = true;
    }

    private static void init() {
        if (disabled || initialized) return;

        try {
            manageOldLogs();

            String runTimestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String textFilename = "ftc_log_" + runTimestamp + ".txt";
            String csvFilename  = "ftc_log_" + runTimestamp + ".csv";

            File textFile = new File(LOG_DIR, textFilename);
            File csvFile  = new File(LOG_DIR, csvFilename);

            textWriter = new FileWriter(textFile, true);
            csvWriter  = new FileWriter(csvFile, true);

            // Add CSV header only if the file is new/empty
            if (csvFile.length() == 0) {
                csvWriter.write("Timestamp,Thread,Class,Level,Message\n");
                csvWriter.flush();
            }

            initialized = true;
        } catch (IOException e) {
            // Prevent repeated attempts if it fails
            disabled = true;
            e.printStackTrace();
        }
    }

    private static void manageOldLogs() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) logDir.mkdirs();

        File[] logFiles = logDir.listFiles((dir, name) ->
                name.startsWith("ftc_log") && (name.endsWith(".txt") || name.endsWith(".csv")));

        if (logFiles == null || logFiles.length <= MAX_LOG_FILES) return;

        Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < logFiles.length - MAX_LOG_FILES; i++) {
            logFiles[i].delete();
        }
    }

    private static void log(String level, String message) {
        if (disabled) return;
        if (!initialized) init();
        if (disabled) return; // init might disable if it fails

        String timestamp = sdf.format(new Date());
        String thread = Thread.currentThread().getName();
        String className = "UnknownClass";

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length > 2) {
            className = stackTrace[2].getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
        }

        String textLog = String.format("%s (%s): %s: [%s] %s\n",
                timestamp, thread, className, level.toUpperCase(), message);

        String csvLog = String.format("%s,%s,%s,%s,\"%s\"\n",
                timestamp, thread, className, level.toUpperCase(), message.replace("\"", "\"\""));

        try {
            textWriter.write(textLog);
            textWriter.flush();
            csvWriter.write(csvLog);
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void info(String message)  { log("INFO", message); }
    public static void warn(String message)  { log("WARN", message); }
    public static void error(String message) { log("ERROR", message); }

    public static void close() {
        if (disabled) return;

        try {
            if (textWriter != null) textWriter.close();
            if (csvWriter != null)  csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}