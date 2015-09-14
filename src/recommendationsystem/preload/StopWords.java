package recommendationsystem.preload;

/**
 * StopWords is a collection of words that we do not want included in the
 * models. This list was taken from Joey Lawrance's original list in PFIS2's
 * implementation (located in a file called je.txt) and also includes the words
 * "string" and "null." Note that these words have been stemmed according to
 * {@link Stemmer}.
 * <p>
 * This should probably be moved to a separate file or into the preferences so
 * it can be modified by the user.
 * 
 * @author David Piorkowski
 * 
 */
public class StopWords
{
    /*
     * Original list:
     * 
     * "a", "about", "above", "abstract", "across", "after", "afterwards",
     * "again", "against", "all", "almost", "alone", "along", "already", "also",
     * "although", "always", "am", "among", "amongst", "amoungst", "amount",
     * "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway",
     * "anywhere", "are", "around", "as", "assert", "at", "b", "back", "be",
     * "became", "because", "become", "becomes", "becoming", "been", "before",
     * "beforehand", "behind", "being", "below", "beside", "besides", "between",
     * "beyond", "bill", "boolean", "both", "bottom", "break", "but", "by",
     * "byte", "c", "call", "can", "cannot", "cant", "case", "catch", "char",
     * "class", "co", "com", "computer", "con", "const", "continue", "could",
     * "couldnt", "cry", "d", "de", "default", "describe", "detail", "do",
     * "done", "double", "down", "due", "during", "e", "each", "eg", "eight",
     * "either", "eleven", "else", "elsewhere", "empty", "en", "enough", "enum",
     * "etc", "even", "ever", "every", "everyone", "everything", "everywhere",
     * "except", "extends", "f", "false", "few", "fifteen", "fify", "fill",
     * "final", "finally", "find", "fire", "first", "five", "float", "for",
     * "former", "formerly", "forty", "found", "four", "from", "front", "full",
     * "further", "g", "get", "give", "go", "goto", "h", "had", "has", "hasnt",
     * "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein",
     * "hereupon", "hers", "herself", "him", "himself", "his", "how", "however",
     * "hundred", "i", "ibm", "ie", "if", "implements", "import", "in", "inc",
     * "indeed", "instanceof", "int", "interest", "interface", "into", "is",
     * "it", "its", "itself", "j", "k", "keep", "l", "la", "lang", "last",
     * "latter", "latterly", "lcom", "least", "less", "ljava", "long", "ltd",
     * "m", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine",
     * "more", "moreover", "most", "mostly", "move", "much", "must", "my",
     * "myself", "n", "name", "namely", "native", "neither", "never",
     * "nevertheless", "new", "next", "nine", "no", "nobody", "none", "noone",
     * "nor", "not", "nothing", "now", "nowhere", "null", "o", "of", "off",
     * "often", "on", "once", "one", "only", "onto", "or", "org", "other",
     * "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own",
     * "p", "package", "part", "per", "perhaps", "please", "private",
     * "protected", "public", "put", "q", "r", "rather", "re", "return", "s",
     * "same", "see", "seem", "seemed", "seeming", "seems", "serious",
     * "several", "she", "short", "should", "show", "side", "since", "sincere",
     * "six", "sixty", "so", "some", "somehow", "someone", "something",
     * "sometime", "sometimes", "somewhere", "static", "still", "strictfp",
     * "string", "such", "super", "switch", "synchronized", "system", "t",
     * "take", "ten", "than", "that", "the", "their", "them", "themselves",
     * "then", "thence", "there", "thereafter", "thereby", "therefore",
     * "therein", "thereupon", "these", "they", "thick", "thin", "third",
     * "this", "those", "though", "three", "through", "throughout", "throw",
     * "throws", "thru", "thus", "to", "together", "too", "top", "toward",
     * "towards", "transient", "true", "try", "twelve", "twenty", "two", "u",
     * "un", "und", "under", "until", "up", "upon", "us", "very", "v", "via",
     * "void", "volatile", "w", "was", "we", "well", "were", "what", "whatever",
     * "when", "whence", "whenever", "where", "whereafter", "whereas",
     * "whereby", "wherein", "whereupon", "wherever", "whether", "which",
     * "while", "whither", "who", "whoever", "whole", "whom", "whose", "why",
     * "will", "with", "within", "without", "would", "www", "x", "y", "yet",
     * "you", "your", "yours", "yourself", "yourselves", "z"
     */

    private static final String[] words =
        { "a", "about", "abov", "abstract", "across", "after", "afterward",
                "again", "against", "all", "almost", "alon", "along",
                "alreadi", "also", "although", "alwai", "am", "among",
                "amongst", "amoungst", "amount", "an", "and", "anoth", "ani",
                "anyhow", "anyon", "anyth", "anywai", "anywher", "ar",
                "around", "as", "assert", "at", "b", "back", "be", "becam",
                "becaus", "becom", "becom", "becom", "been", "befor",
                "beforehand", "behind", "be", "below", "besid", "besid",
                "between", "beyond", "bill", "boolean", "both", "bottom",
                "break", "but", "by", "byte", "c", "call", "can", "cannot",
                "cant", "case", "catch", "char", "class", "co", "com",
                "comput", "con", "const", "continu", "could", "couldnt", "cry",
                "d", "de", "default", "describ", "detail", "do", "done",
                "doubl", "down", "due", "dure", "e", "each", "eg", "eight",
                "either", "eleven", "els", "elsewher", "empti", "en", "enough",
                "enum", "etc", "even", "ever", "everi", "everyon", "everyth",
                "everywher", "except", "extend", "f", "fals", "few", "fifteen",
                "fifi", "fill", "final", "final", "find", "fire", "first",
                "five", "float", "for", "former", "formerli", "forti", "found",
                "four", "from", "front", "full", "further", "g", "get", "give",
                "go", "goto", "h", "had", "ha", "hasnt", "have", "he", "henc",
                "her", "here", "hereaft", "herebi", "herein", "hereupon",
                "her", "herself", "him", "himself", "hi", "how", "howev",
                "hundr", "i", "ibm", "ie", "if", "implement", "import", "in",
                "inc", "inde", "instanceof", "int", "interest", "interfac",
                "into", "is", "it", "it", "itself", "j", "k", "keep", "l",
                "la", "lang", "last", "latter", "latterli", "lcom", "least",
                "less", "ljava", "long", "ltd", "m", "made", "mani", "mai",
                "me", "meanwhil", "might", "mill", "mine", "more", "moreov",
                "most", "mostli", "move", "much", "must", "my", "myself", "n",
                "name", "name", "nativ", "neither", "never", "nevertheless",
                "new", "next", "nine", "no", "nobodi", "none", "noon", "nor",
                "not", "noth", "now", "nowher", "null", "o", "of", "off",
                "often", "on", "onc", "on", "onli", "onto", "or", "org",
                "other", "other", "otherwis", "our", "our", "ourselv", "out",
                "over", "own", "p", "packag", "part", "per", "perhap", "pleas",
                "privat", "protect", "public", "put", "q", "r", "rather", "re",
                "return", "s", "same", "see", "seem", "seem", "seem", "seem",
                "seriou", "sever", "she", "short", "should", "show", "side",
                "sinc", "sincer", "six", "sixti", "so", "some", "somehow",
                "someon", "someth", "sometim", "sometim", "somewher", "static",
                "still", "strictfp", "string", "such", "super", "switch",
                "synchron", "system", "t", "take", "ten", "than", "that",
                "the", "their", "them", "themselv", "then", "thenc", "there",
                "thereaft", "therebi", "therefor", "therein", "thereupon",
                "these", "thei", "thick", "thin", "third", "thi", "those",
                "though", "three", "through", "throughout", "throw", "throw",
                "thru", "thu", "to", "togeth", "too", "top", "toward",
                "toward", "transient", "true", "try", "twelv", "twenti", "two",
                "u", "un", "und", "under", "until", "up", "upon", "us", "veri",
                "v", "via", "void", "volatil", "w", "wa", "we", "well", "were",
                "what", "whatev", "when", "whenc", "whenev", "where",
                "whereaft", "wherea", "wherebi", "wherein", "whereupon",
                "wherev", "whether", "which", "while", "whither", "who",
                "whoever", "whole", "whom", "whose", "why", "will", "with",
                "within", "without", "would", "www", "x", "y", "yet", "you",
                "your", "your", "yourself", "yourselv", "z" };

    /**
     * Returns True if the word is in the list of stop words. The search is
     * not-case sensitive. This method assumes that the passed in word has
     * already been stemmed.
     * 
     * @param stemmedWord
     *            The stemmed word to seek
     * @return <ul>
     *         <li>True if the word matches one in the list
     *         <li>False otherwise</li>
     */
    public static boolean isStopWord(String stemmedWord)
    {
        stemmedWord = stemmedWord.trim();

        for (String w : words)
        {
            if (w.equalsIgnoreCase(stemmedWord))
                return true;
        }
        return false;
    }

    /**
     * Returns the list of stop words.
     * 
     * @return A string array containing the stop words
     */
    public static String[] getStopWords()
    {
        return words;
    }

}
