package filter;

public class FilterException extends RuntimeException {
    /** An exception whose getMessage() value is MSG. */
    FilterException(String msg) {
        super(msg);
    }

    /** A utility method that returns a new exception with a message
     *  formed from MSGFORMAT and ARGS, interpreted as for the
     *  String.format method or the standard printf methods.
     *
     *  The use is thus 'throw error(...)', which tells the compiler that
     *  execution will terminate at that point, and avoid insistance on
     *  an explicit return in a value-returning function.)  */
    static FilterException error(String msgFormat, Object... args) {
        return new FilterException(String.format(msgFormat, args));
    }
}
