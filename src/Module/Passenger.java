package Module;

import Module.Floor;
import exceptions.OverloadException;

/**
 * A Module.Passenger whom in the Module.Building and request to use the elevator.
 *
 * @author EJWang
 */
public class Passenger {

    // TODO: 给passenger一个开始等待的时间，和想去的楼层，想按的方向

    private Floor currFloor;
    private Floor targetFloor;
    private int weight;

    private int targetDirection;

    /**
     * Construct a Module.Passenger.
     *
     * @param currFloor   The current floor that this passenger is staying
     * @param targetFloor The target floor that this passenger want to visit
     */
    public Passenger(Floor currFloor, Floor targetFloor, int weight) {
        this.currFloor = currFloor;
        this.targetFloor = targetFloor;
        this.weight = weight;
        this.targetDirection = currFloor.compareTo(targetFloor) == 1 ? -1 : 1;
    }

    /**
     * Run, run, little bird!
     */
    public void escape() {
        System.err.println("乘客: 卧槽电梯差点夹死我了!");
    }

    /**
     * Walk in the elevator.
     */
    public void walkIn(Elevator elevator) {
        try {
            elevator.receivePassenger(this);
        } catch (OverloadException o) {
            walkOut(false);
        }
    }

    /**
     * Walk out the elevator.
     */
    public void walkOut(boolean isArrived) {
        if (!isArrived) {
            // Walk back to the queue and wait for next elevator
            currFloor.getQueue(getTargetDirection()).add(this);
        } else {
            // TODO: Calculating waiting time
        }
    }

    /**
     * Get currFloor of this passenger.
     *
     * @return The currFloor.
     */
    public Floor getCurrFloor() {
        return currFloor;
    }

    /**
     * Get targetFloor of this passenger.
     *
     * @return The targetFloor.
     */
    public Floor getTargetFloor() {
        return targetFloor;
    }

    /**
     * Get target direction of this passenger.
     *
     * @return The target direction of this passenger
     */
    public int getTargetDirection() {
        return targetDirection;
    }

    public int getWeight() {
        return weight;
    }
}
