package Module;

import exceptions.DockingFailedException;
import exceptions.OverloadException;

import java.util.*;

/**
 * A level of Module.Floor in the Module.Building.
 * It has two queue of passengers for direction going up and going down.
 * Each floor has installed the button to call the Module.Elevator Manager System
 * to schedule a pick up.
 *
 * @author EJWang
 */
public class Floor implements Comparable<Floor> {

    // the level of this floor
    private int floorLevel;

    // the elevator manager system which is managed all elevator
    private ElevatorManagerSystem ems;

    // the queue of waiting residents for both direction
    private List<Passenger> goingUp;
    private List<Passenger> goingDown;

    /**
     * Construct a Module.Floor.
     *
     * @param floorLevel The level of this floor
     */
    public Floor(int floorLevel, ElevatorManagerSystem ems) {
        this.floorLevel = floorLevel;
        this.ems = ems;
        goingUp = new ArrayList<>();
        goingDown = new ArrayList<>();

    }

    /**
     * Higher level is larger than lower level.
     *
     * @param other The other floor
     * @return The number used to indicate the result
     */
    @Override
    public int compareTo(Floor other) {
        if (this.getFloorLevel() > other.getFloorLevel()) {
            return 1;
        } else if (this.getFloorLevel() < other.getFloorLevel()) {
            return -1;
        }
        return 0;
    }

    /**
     * Module.Passenger request to use the elevator, it will send request to Module.Elevator Manager System.
     *
     * @param person The person whom push the button
     */
    public void pushButton(Passenger person) {
        if (person.getTargetDirection() > 0) {
            goingUp.add(person);
        } else {
            goingDown.add(person);
        }
        // push task to Module.Elevator Manager System
        // the system will automatically handle the request and allocate elevator to pick up the passenger
        ems.addTask(this);
    }

    public void notifyPassengerOnboard(Elevator elevator) {
        List<Passenger> queue = (elevator.getDirection() == 1) ? goingUp : goingDown;

        for (Passenger passenger : queue) {
            passenger.walkIn(elevator);
        }
    }

    /**
     * Request for docking.
     * This method have 1/10000 probability to throw exception in order to mock real world event.
     */
    public void requestForDocking() throws DockingFailedException {
        // use random to mock failed docking
        int n = new Random().nextInt(10000);

//        if (n == 5000)
//            throw new DockingFailedException("Failed to docking at floor level " + floorLevel);
    }


    /**
     * Get the level of this floor.
     *
     * @return The level of this floor
     */
    public int getFloorLevel() {
        return floorLevel;
    }

    /**
     * Get specified queue depends on the direction.
     *
     * @param direction The direction of queue
     * @return The direction specified queue
     */
    public List<Passenger> getQueue(int direction) {
        if (direction == -1) {
            return goingDown;
        } else {
            return goingDown;
        }
    }

}
