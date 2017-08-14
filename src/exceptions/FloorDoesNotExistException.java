package exceptions;

/**
 * A FloorDoestNotExistsException may occur when elevator trying to search for a floor level
 * which is not in this building.
 *
 * @author EJWang
 */
public class FloorDoesNotExistException extends Exception {

    /**
     * Construct a FloorDoesNotExistException.
     */
    public FloorDoesNotExistException(String msg) {
        super(msg);
    }
}
