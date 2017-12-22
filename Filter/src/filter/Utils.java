package filter;

import java.io.*;
import java.nio.file.Files;

public class Utils {
    public static BufferedReader makeReader(String fileName) {

        // The name of the file to open.

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            return new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName), "UTF-16"));


        } catch(FileNotFoundException ex) {
            throw FilterException.error("Could not find file");

        } catch (Exception exp) {
            throw FilterException.error("Could not read file ");
        }
    }

    /**
     * Return the entire contents of FILE as a byte array.  FILE must
     * be a normal file.  Throws IllegalArgumentException
     * in case of problems.
     */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * Return the entire contents of File of FILENAME as a byte array.
     * FILE must be a normal file.  Throws IllegalArgumentException
     * in case of problems.
     */
    static byte[] readContents(String fileName) {
        return readContents(new File(fileName));
    }
}
