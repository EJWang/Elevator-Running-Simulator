package exceptions;

/**
 * A DockingFailedException use to tell the Module.Elevator Manager System that an accident happened and need help now.
 */
public class DockingFailedException extends Exception {

    /**
     * Construct a DockingFailedException.
     */
    public DockingFailedException(String msg) {
        super(msg);
    }
}
