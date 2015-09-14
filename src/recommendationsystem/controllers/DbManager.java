package recommendationsystem.controllers;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.preload.MethodData;

/**
 * DbManager initializes and maintains a connection to the database. It also
 * provides methods for all the database queries for the IFT MariaDB database.
 * <p>
 * The database "get" methods are synchronized in the even that multiple models
 * are running at the same time.
 * <p>
 * DbManager is a singleton class. Call <code>getInstance()</code> to use it.
 * 
 * @author David Piorkowski
 * 
 */
public class DbManager
{
    private static DbManager instance;

    private Connection conn;
    private ErrorLogger eLog;

    /**
     * The singleton constructor.
     */
    private DbManager()
    {
        // Singleton class, don't put anything here
    }

    /**
     * Get an instance of DbManager.
     * 
     * @return The singleton instance of DbManager
     */
    public static synchronized DbManager getInstance()
    {
        if (instance == null)
        {
            instance = new DbManager();
            instance.init();
        }
        return instance;
    }

    /**
     * DbManager is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
    
    private void init()
    {
        eLog = ErrorLogger.getInstance();
    }

    /**
     * Call this to open a connection to the plugin database and prepare the
     * CallableStatements for the stored procedures. If the connection is
     * already open, this method does nothing.
     * <p>
     * Note that this must be called before any methods that act on the database
     * are called.
     * 
     * @throws SQLException
     *             when the MySQL/MariaDb JDBC driver is not found.
     */
    public void openConnectionAndInit(String connectionString)
            throws SQLException
    {
        try
        {
            closeConnection();
            if (conn == null)
            {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connectionString);
                System.out.println("Database connection open.");
                verifyConnection();
                prepareCallableStatements();
            }
        }
        catch (ClassNotFoundException e)
        {
            eLog.logException(e);
            throw new SQLException("Couldn't load MySQL JDBC driver");
        }
    }

    /**
     * Checks status of the database connection.
     * 
     * @return <ul>
     *         <li>True when the connection is open
     *         <li>False if the connection is unavailable
     *         </ul>
     * @throws SQLException
     *             if a database access error occurs
     */
    private boolean verifyConnection() throws SQLException
    {
        if (conn == null || conn.isClosed())
        {
            return false;
        }
        return true;
    }

    /**
     * Closes a previously opened connection. This method is called when the
     * plugin exits and no longer needs to access the database.
     * 
     * @throws SQLException
     *             if a database access error occurs
     */
    public void closeConnection() throws SQLException
    {
        if (verifyConnection())
        {
            System.out.println("Database connection closed.");
            conn.close();
            conn = null;
        }
    }

    private CallableStatement sp_getAllMethodIdsAndKeys,
            sp_getAllWordIdsAndWords, sp_getIdfDenominator, sp_getIdfNumerator,
            sp_getMethodDataFromMethodKey, sp_getMethodIdFromMethodKey,
            sp_getMethodsFromWordId, sp_getMostCommonWords,
            sp_getMostCommonWordsPFIS, sp_getMostCommonWordsPFIS2,
            sp_getMostCommonWordsTFIDF2, sp_getNumWords,
            sp_getPathFromMethodKey, sp_getTfDenominator,
            sp_getWordCountsFromMethodIdAndWordId, sp_getWordIdFromWord,
            sp_getWordsFromMethodId, sp_insertMethod,
            sp_insertMethodToWordMapping, sp_insertWord,
            sp_insertWordCountForMethodIdAndWordId,
            sp_updateWordCountForMethodIdAndWordId;
    private ResultSet rs;

    /**
     * Prepares the CallableStatments for the IFT database. Since preparing the
     * statements is an expensive operation,we initialize them here once and
     * reuse them as necessary.
     * <p>
     * This method does nothing if a connection has not been opened.
     * 
     * @throws SQLException
     *             if database access error occurs.
     */
    private void prepareCallableStatements() throws SQLException
    {
        if (!verifyConnection())
            return;

        // Preparing these is expensive, we only want to do it once and have
        // them ready to go
        sp_getAllMethodIdsAndKeys = conn
                .prepareCall("{call sp_getAllMethodIdsAndKeys()}");
        sp_getAllWordIdsAndWords = conn
                .prepareCall("{call sp_getAllWordIdsAndWords()}");
        sp_getIdfDenominator = conn
                .prepareCall("{call sp_getIdfDenominator(?)}");
        sp_getIdfNumerator = conn.prepareCall("{call sp_getIdfNumerator()}");
        sp_getMethodDataFromMethodKey = conn
                .prepareCall("{call sp_getMethodDataFromMethodKey(?)}");
        sp_getMethodIdFromMethodKey = conn
                .prepareCall("{call sp_getMethodIdFromMethodKey(?)}");
        sp_getMethodsFromWordId = conn
                .prepareCall("{call sp_getMethodsFromWordId(?)}");
        sp_getMostCommonWords = conn
                .prepareCall("{call sp_getMostCommonWords(?, ?)}");
        sp_getMostCommonWordsPFIS = conn
                .prepareCall("{call sp_getMostCommonWordsPFIS(?)}");
        sp_getMostCommonWordsPFIS2 = conn
                .prepareCall("{call sp_getMostCommonWordsPFIS2(?)}");
        sp_getMostCommonWordsTFIDF2 = conn
                .prepareCall("{call sp_getMostCommonWordsTFIDF2(?, ?)}");
        sp_getNumWords = conn.prepareCall("{call sp_getNumWords()}");
        sp_getPathFromMethodKey = conn
                .prepareCall("{call sp_getPathFromMethodKey(?)}");
        sp_getTfDenominator = conn.prepareCall("{call sp_getTfDenominator(?)}");
        sp_getWordCountsFromMethodIdAndWordId = conn
                .prepareCall("{call sp_getWordCountsFromMethodIdAndWordId(?, ?)}");
        sp_getWordIdFromWord = conn
                .prepareCall("{call sp_getWordIdFromWord(?)}");
        sp_getWordsFromMethodId = conn
                .prepareCall("{call sp_getWordsFromMethodId(?)}");
        sp_insertMethod = conn.prepareCall("{call sp_InsertMethod(?, ?, ?)}");
        sp_insertMethodToWordMapping = conn
                .prepareCall("{call sp_insertMethodToWordMapping(?, ?)}");
        sp_insertWord = conn.prepareCall("{call sp_insertWord(?)}");
        sp_insertWordCountForMethodIdAndWordId = conn
                .prepareCall("{call sp_insertWordCountForMethodIdAndWordId(?, ?, ?)}");
        sp_updateWordCountForMethodIdAndWordId = conn
                .prepareCall("{call sp_updateWordCountForMethodIdAndWordId(?, ?, ?)}");
    }

    /**
     * Returns all the method IDs and keys that exist in the methods table of
     * the database. The caller is responsible for closing the result set. This
     * method is synchronized.
     * 
     * @return <ul>
     *         <li>ResultSet with the following columns: id, methodKey
     *         <li>
     *         null if the query fails
     *         </ul>
     */
    public synchronized ResultSet getAllMethodIdsAndKeys()
    {
        ResultSet rv = null;
        try
        {
            rv = sp_getAllMethodIdsAndKeys.executeQuery();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns all the word IDs and words that exist in the words table of the
     * database. The caller is responsible for closing the result set. This
     * method is synchronized.
     * 
     * @return <ul>
     *         <li>ResultSet with the following columns: id, word
     *         <li>null if the query fails
     *         </ul>
     */
    public synchronized ResultSet getAllWordIdsAndWords()
    {
        ResultSet rv = null;
        try
        {
            rv = sp_getAllWordIdsAndWords.executeQuery();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns the number of methods that a word appears given the word's id.
     * This method is synchronized.
     * 
     * @param wordId
     *            The database ID of the word
     * @return <ul>
     *         <li>The number of methods that contain that word
     *         <li>
     *         -1 if the query fails or the ID is not found
     *         </ul>
     */
    public synchronized int getIdfDenominator(int wordId)
    {
        int rv = -1;
        try
        {
            sp_getIdfDenominator.setInt(1, wordId);
            rs = sp_getIdfDenominator.executeQuery();
            while (rs.next())
                rv = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns the total number of methods. This method is synchronized.
     * 
     * @return <ul>
     *         <li>The count of methods in table methods_words
     *         <li>
     *         -1 if the query fails
     *         </ul>
     */
    public synchronized int getIdfNumerator()
    {
        int rv = -1;
        try
        {
            rs = sp_getIdfNumerator.executeQuery();
            while (rs.next())
                rv = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * 
     * Returns a {@link MethodData} object given method's key. This method is
     * synchronized.
     * <p>
     * The plugin now uses MethodDeclaration as its main data type. See
     * {@link MethodManager} for more details
     * 
     * @param methodKey
     * @return <ul>
     *         <li>A new MethodData object for the given method key <li> null if
     *         the query fails or if the key was not found
     *         </ul>
     * @deprecated
     */
    public synchronized MethodData getMethodDataFromMethodKey(String methodKey)
    {
        MethodData rv = null;
        try
        {
            sp_getMethodDataFromMethodKey.setString(1, methodKey);
            rs = sp_getMethodDataFromMethodKey.executeQuery();
            while (rs.next())
                rv = new MethodData(rs.getString(1), rs.getString(2),
                        rs.getString(3));
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns the method's ID given a method's key. Most of the calls on the
     * database are based on IDs since they are quicker, use this to determine
     * an ID for those calls. This method is synchronized.
     * 
     * @param methodKey
     *            The method key to look for
     * @return <ul>
     *         <li>The method's database id for the given key
     *         <li>
     *         -1 if the query fails or the key was not found
     *         </ul>
     */
    public synchronized int getMethodIdFromMethodKey(String methodKey)
    {
        int rv = -1;
        try
        {
            sp_getMethodIdFromMethodKey.setString(1, methodKey);
            rs = sp_getMethodIdFromMethodKey.executeQuery();
            while (rs.next())
                rv = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns all the method IDs and keys that contain a particular word's id.
     * The caller is responsible for closing the result set. This method is
     * synchronized.
     * 
     * @param wordId
     *            The database id of the word
     * 
     * @return <ul>
     *         <li>ResultSet with the following columns: id, methodKey
     *         <li>null if the query fails or the ID is not found
     *         </ul>
     */
    public synchronized ResultSet getMethodsFromWordId(int wordId)
    {
        ResultSet rv = null;
        try
        {
            sp_getMethodsFromWordId.setInt(1, wordId);
            rv = sp_getMethodsFromWordId.executeQuery();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns up to 5 common shared words between two methods from the TF-IDF
     * calculation from most occurring to least occurring. This method is
     * synchronized. Use getMostCommonWordsTFIDF2(int, int) instead.
     * 
     * @param methodId1
     *            The id of the current method
     * @param methodId2
     *            The id of the method to compare to
     * @return An ordered string array of size 5 that contains one of the
     *         following in each cell
     *         <ul>
     *         <li>a word shared between the methods
     *         <li>
     *         null for empty entries
     *         </ul>
     */
    @Deprecated
    public synchronized String[] getMostCommonWordsTFIDF(int methodId1,
            int methodId2)
    {
        String[] rv = new String[8];
        int i = 0;
        try
        {
            sp_getMostCommonWords.setInt(1, methodId1);
            sp_getMostCommonWords.setInt(2, methodId2);
            rs = sp_getMostCommonWords.executeQuery();
            while (rs.next())
                rv[i++] = rs.getString(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns up to 5 common shared words between two methods from the TF-IDF
     * calculation from most occurring to least occurring. This method is
     * synchronized. This is an updated version of getMostCommonWordsTFIDF(int,
     * int).
     * 
     * @param methodId1
     *            The id of the current method
     * @param methodId2
     *            The id of the method to compare to
     * @return An ordered string array of size 5 that contains one of the
     *         following in each cell
     *         <ul>
     *         <li>a word shared between the methods
     *         <li>
     *         null for empty entries
     *         </ul>
     */
    public synchronized String[] getMostCommonWordsTFIDF2(int methodId1,
            int methodId2)
    {
        String[] rv = new String[8];
        int i = 0;
        try
        {
            sp_getMostCommonWordsTFIDF2.setInt(1, methodId1);
            sp_getMostCommonWordsTFIDF2.setInt(2, methodId2);
            rs = sp_getMostCommonWordsTFIDF2.executeQuery();
            while (rs.next())
                rv[i++] = rs.getString(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns the most common occurring words according to PFIS given a
     * method's id. This method is synchronized. User
     * getMostCommonWordsPFIS2(int) instead.
     * 
     * @param methodId
     *            The id of the current method
     * @return An ordered string array of size 5 that contains one of the
     *         following in each cell
     *         <ul>
     *         <li>a word shared between the methods
     *         <li>
     *         null for empty entries
     *         </ul>
     */
    @Deprecated
    public synchronized String[] getMostCommonWordsPFIS(int methodId)
    {
        String[] rv = new String[8];
        int i = 0;
        try
        {
            sp_getMostCommonWordsPFIS.setInt(1, methodId);
            rs = sp_getMostCommonWordsPFIS.executeQuery();
            while (rs.next())
                rv[i++] = rs.getString(2);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns the most common occurring words according to PFIS given a
     * method's id. This method is synchronized. This is an updated version of
     * getMostCommonWordsPFIS(int).
     * 
     * @param methodId
     *            The id of the current method
     * @return An ordered string array of size 5 that contains one of the
     *         following in each cell
     *         <ul>
     *         <li>a word shared between the methods
     *         <li>
     *         null for empty entries
     *         </ul>
     */
    public synchronized String[] getMostCommonWordsPFIS2(int methodId)
    {
        String[] rv = new String[8];
        int i = 0;
        try
        {
            sp_getMostCommonWordsPFIS2.setInt(1, methodId);
            rs = sp_getMostCommonWordsPFIS2.executeQuery();
            while (rs.next())
                rv[i++] = rs.getString(2);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns the total number of words in the words table of the database.
     * This method is synchronized.
     * 
     * @return The number of unique words in the database.
     */
    public synchronized int getNumWords()
    {
        int rv = 0;

        try
        {
            rs = sp_getNumWords.executeQuery();
            while (rs.next())
                rv = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }

        return rv;
    }

    /**
     * Return the path of the source code file containing the method given a
     * method key. This is mainly used to bring a method into focus in the
     * Eclipe's source code editor. This method is synchronized.
     * 
     * @param methodKey
     *            The method's key
     * @return <ul>
     *         <li>The path to the method's source code file
     *         <li>-1 if the query fails or the key is not found
     *         </ul>
     */
    public synchronized String getPathFromMethodKey(String methodKey)
    {
        // TODO: Check if using method id's is faster
        String rv = null;
        try
        {
            sp_getPathFromMethodKey.setString(1, methodKey);
            rs = sp_getPathFromMethodKey.executeQuery();
            while (rs.next())
                rv = rs.getString(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Return the total number of words in the given method. This method is
     * synchronized.
     * 
     * @param methodId
     *            The database id of the method
     * @return <ul>
     *         <li>The number of words in the given method
     *         <li>-1 if the method's ID is not found
     *         </ul>
     */
    public synchronized int getTfDenominator(int methodId)
    {
        int rv = -1;
        try
        {
            sp_getTfDenominator.setInt(1, methodId);
            rs = sp_getTfDenominator.executeQuery();
            while (rs.next())
                rv = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns the number of times a word occurs for the given method's ID and
     * word's ID. This method is synchronized.
     * 
     * @param methodId
     *            The database id of the method
     * @param wordId
     *            The database's id of the word
     * @return The number of times a word occurs within the method
     */
    public synchronized int getWordCountFromMethodIdAndWordId(int methodId,
            int wordId)
    {
        int rv = 0;

        try
        {
            sp_getWordCountsFromMethodIdAndWordId.setInt(1, methodId);
            sp_getWordCountsFromMethodIdAndWordId.setInt(2, wordId);
            rs = sp_getWordCountsFromMethodIdAndWordId.executeQuery();
            while (rs.next())
                rv = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Return the database ID of the given word. This method is synchronized.
     * 
     * @param word
     *            The word to look for
     * @return <ul>
     *         <li>The database ID of the given word
     *         <li>-1 if the query fails or the word is not found
     *         </ul>
     */
    public synchronized int getWordIdFromWord(String word)
    {
        int rv = -1;
        try
        {
            sp_getWordIdFromWord.setString(1, word);
            rs = sp_getWordIdFromWord.executeQuery();
            while (rs.next())
                rv = rs.getInt(1);
            rs.close();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Returns all the word IDs, words and the number of each word's occurrence
     * for a given method's id. The caller is responsible for closing the result
     * set. This method is synchronized.
     * 
     * @param methodId
     *            The database id of the method key
     * 
     * @return <ul>
     *         <li>ResultSet with the following columns: id, word, wordCount
     *         <li>
     *         null if the query fails or the ID is not found
     *         </ul>
     */
    public synchronized ResultSet getWordsFromMethodId(int methodId)
    {
        ResultSet rv = null;
        try
        {
            sp_getWordsFromMethodId.setInt(1, methodId);
            rv = sp_getWordsFromMethodId.executeQuery();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
        return rv;
    }

    /**
     * Inserts a method's data to the methods table of the database. If the
     * method's key already exists in the database, the new information is
     * ignored and no database rows are added or modified.
     * 
     * @param methodData
     *            The method to insert
     */
    public void insertMethod(MethodData methodData)
    {
        try
        {
            sp_insertMethod.setString(1, methodData.getKey());
            sp_insertMethod.setString(2, methodData.getName());
            sp_insertMethod.setString(3, methodData.getPath());
            sp_insertMethod.executeUpdate();
        }
        catch (MySQLIntegrityConstraintViolationException e)
        {
            // Ignore, this is to handle duplicate entries
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
    }

    /**
     * Inserts a new mapping from method id to word id to the methods_words
     * table of the database. Each row represents one instance of a word
     * existing in the method. Unlike the other inserts, duplicate entries are
     * allowed since each word can exist more than once in a method.
     * 
     * @param methodId
     *            The method's database ID
     * @param wordId
     *            The word's ID
     */
    public void insertMethodToWordMapping(int methodId, int wordId)
    {
        try
        {
            sp_insertMethodToWordMapping.setInt(1, methodId);
            sp_insertMethodToWordMapping.setInt(2, wordId);
            sp_insertMethodToWordMapping.executeUpdate();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
    }

    /**
     * Inserts a new word to the words table of the database. If a word already
     * exists, no rows are added or modified in the database.
     * 
     * @param word
     *            The word to add
     */
    public void insertWord(String word)
    {
        try
        {
            sp_insertWord.setString(1, word.toLowerCase());
            sp_insertWord.executeUpdate();
        }
        catch (MySQLIntegrityConstraintViolationException e)
        {
            // Ignore, this is to handle duplicate entries
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
    }

    /**
     * Inserts a new word and word count for a given method. This method should
     * be only used if the row does not yet exist in the database. Existence can
     * be verified using the getWordCountFromMethodIdAndWordId(int, int) method.
     * If the method exists, updateWordCountForMethodIdAndWordId(int, int)
     * should be used instead.
     * 
     * @param methodId
     *            The method's database id
     * @param wordId
     *            The word's database id
     * @param wordCount
     *            The number of times the word occurs in the given method
     */
    public void insertWordCountForMethodIdAndWordId(int methodId, int wordId,
            int wordCount)
    {
        try
        {
            sp_insertWordCountForMethodIdAndWordId.setInt(1, methodId);
            sp_insertWordCountForMethodIdAndWordId.setInt(2, wordId);
            sp_insertWordCountForMethodIdAndWordId.setInt(3, wordCount);
            sp_insertWordCountForMethodIdAndWordId.executeUpdate();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
    }

    /**
     * Updates an existing word and word count for a given method. This method
     * should be only used if the row already exists in the database. Existence
     * can be verified using the getWordCountFromMethodIdAndWordId(int, int)
     * method. If the method does not exist,
     * insertWordCountForMethodIdAndWordId(int, int) should be used instead.
     * 
     * @param methodId
     *            The method's database id
     * @param wordId
     *            The word's database id
     * @param wordCount
     *            The number of times the word occurs in the given method
     */
    public void updateWordCountForMethodIdAndWordId(int methodId, int wordId,
            int wordCount)
    {
        try
        {
            sp_updateWordCountForMethodIdAndWordId.setInt(1, methodId);
            sp_updateWordCountForMethodIdAndWordId.setInt(2, wordId);
            sp_updateWordCountForMethodIdAndWordId.setInt(3, wordCount);
            sp_updateWordCountForMethodIdAndWordId.executeUpdate();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
    }
}
