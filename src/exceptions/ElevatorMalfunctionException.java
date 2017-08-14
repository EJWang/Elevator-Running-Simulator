package exceptions;

/**
 * A exceptions.ElevatorMalfunctionException is used to alert the system that this elevator need
 * maintenance before start to service.
 *
 * @author EJWang
 */
public class ElevatorMalfunctionException extends Exception {

    /**
     * Construct an ElevatorMalfunctionException.
     *
     * @param number The number of elevator that is in malfunction
     */
    public ElevatorMalfunctionException(int number) {
        super(String.format("Module.Elevator #%d is malfunction, cannot be start.", number));
    }
}
