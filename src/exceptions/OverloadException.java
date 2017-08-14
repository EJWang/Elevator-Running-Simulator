package exceptions;

/**
 * The OverloadException occurred while loading passenger but exceed the capacity.
 *
 * @author EJWang
 */
public class OverloadException extends Exception {

    /**
     * Construct an overload exception.
     *
     * @param msg The message of this event
     */
    public OverloadException(String msg) {
        super(msg);
    }
}
