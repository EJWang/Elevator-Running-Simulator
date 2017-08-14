package Module;

import sun.rmi.runtime.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * The LogRecorder is followed Singleton design and used to record the status for each component of the simulation.
 */
public class LogRecorder {

    private static LogRecorder logRecorder = null;

    /**
     * Construct the LogRecorder.
     */
    private LogRecorder() {
    }

    /**
     * Get an instance of LogRecorder.
     *
     * @return An instance of LogRecorder.
     */
    public static LogRecorder getInstance() {
        if (logRecorder == null)
            logRecorder = new LogRecorder();
        return logRecorder;
    }

    /**
     * Record the log to the specified file.
     *
     * @param filename The filename of the file used to record log
     * @param msg The description of an event
     * @param isRequiredToPrint print on the console if true
     */
    public void recordLog(String filename, String msg, boolean isRequiredToPrint) {
        String log;
        String timeStamp;
        FileWriter fileWriter;

        try {
            fileWriter = new FileWriter(filename, true);

            // give a timeStamp on the msg
            timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
            log = "[" + timeStamp + "] " + msg + "\n";

            // print out the log on the console immediately
            if (isRequiredToPrint)
                System.err.print(log);

            // write log into the file and flush the buffer to store the data to the hard disk immediately
            fileWriter.write(log);
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException i) {
            System.err.println("WARNING: Failed to construct file writer while recording log!");
        }

    }
}
