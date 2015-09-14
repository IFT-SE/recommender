package recommendationsystem.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ErrorLogger
{
    public static ErrorLogger instance;
    private Logger log;
    private FileHandler fileHandler;
    private SimpleFormatter formatter;
    private String fileName;

    private ErrorLogger()
    {

    }

    public synchronized static ErrorLogger getInstance()
    {
        if (instance == null)
        {
            instance = new ErrorLogger();
        }
        return instance;
    }

    private void init()
    {
        try
        {
            log = Logger.getLogger(ErrorLogger.class.getName());
            log.setLevel(Level.SEVERE);
            fileHandler = new FileHandler(fileName);
            formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            log.addHandler(fileHandler);
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set the file to log to. This call is required before the log is
     * initialized.
     * 
     * @param fileName
     *            The path to the file to log to.
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
        init();
    }

    public synchronized void logException(Exception e)
    {
        StringBuilder sb = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        sb.append(e.getMessage());
        sb.append(newLine);
        sb.append(e.getStackTrace());
        log.severe(sb.toString());
    }
}
