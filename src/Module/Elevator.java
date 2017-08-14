package Module;

import java.util.*;

import Module.Building;
import exceptions.DockingFailedException;
import exceptions.ElevatorMalfunctionException;
import exceptions.FloorDoesNotExistException;
import exceptions.OverloadException;

/**
 * This is a elevator installed in the building, and controlled by Module.Elevator Manager System.
 *
 * @author EJWang
 */
public class Elevator implements Runnable {
    /* ---- Running configuration ---- */
    private static final int DELAY = 100;
    private static final String LOG_FILENAME = "elevator_log.txt";
    private static final boolean LOG_VERBOSE = true;

    /* ---- Upper level class ---- */
    private ElevatorManagerSystem ems;
    private Building building;

    /* The basic information of this elevator */
    private int number;
    private double capacityOfWeight;
    private int capacityOfPassengers;

    /* The status of this elevator. */
    private int operationSignal;            // -1 = Emergency, 0 = Shutdown, 1 = Running
    private int direction;                  // -1 = moving down, 0 = stop, 1 = moving up
    private int currWeight;              // current loading weight
    private Floor currFloor;                // current stay floor

    /* The task list of this elevator */
    private List<Floor> tasks;

    /* The sedan store passenger */
    private List<Passenger> sedan;

    /* =================================== Constructor Methods ===================================================== */

    /**
     * Construct an elevator.
     *
     * @param number               The number of this elevator
     * @param capacityOfWeight     The maximum capacity of weight of this elevator
     * @param capacityOfPassengers The maximum capacity of onboard passenger of this elevator
     * @param ems                  The elevator manager system which controlled this elevator
     * @param building             The building which this elevator serving for
     */
    public Elevator(int number, double capacityOfWeight, int capacityOfPassengers, ElevatorManagerSystem ems, Building building) {
        this.number = number;
        this.capacityOfWeight = capacityOfWeight;
        this.capacityOfPassengers = capacityOfPassengers;
        this.ems = ems;
        this.building = building;

        operationSignal = 0;
        direction = 0;
        currWeight = 0;

        try {
            currFloor = building.getSpecifiedFloor(1);
        } catch (Exception e) {
            System.err.printf("Failed to initialize elevator %d\n", number);
        }
        tasks = new ArrayList<>();
        sedan = new ArrayList<>();
    }


    /* =================================== Override Methods ======================================================== */

    /**
     * This method belong to Thread, and it will be invoked by Thread method start.
     */
    @Override
    public void run() {
        operationSignal = 1;
        // standby this elevator
        standby();
    }


    /* =================================== Public Methods ========================================================== */

    /**
     * Add task to the task list of this elevator.
     *
     * @param targetFloor The target floor which request for service
     */
    public void addTask(Floor targetFloor) {
        tasks.add(targetFloor);
    }


    /**
     * Return the workload of this elevator.
     * <p>
     * The workload of this elevator is based on this measurement:
     * <p>
     * 1. If this elevator is currently moving
     *
     * @param targetFloor The target floor of the task
     * @return The workload of this elevator
     */
    public int getWorkLoad(Floor targetFloor) {
        if (direction == 0)
            return 0;

        int grade = 0;
        int absDistance = Math.abs(targetFloor.getFloorLevel() - currFloor.getFloorLevel());

        // same direction
        if (currFloor.getFloorLevel() - targetFloor.getFloorLevel() > 0 && direction == -1 ||
                currFloor.getFloorLevel() - targetFloor.getFloorLevel() < 0 && direction == 1) {
            grade += absDistance * (1 + tasks.size());
        } else {
            // otherwise 2 times distance at least
            grade += 2 * absDistance * (1 + tasks.size());
        }

        return grade;
    }

    /**
     * Manually turn on the elevator to start service.
     *
     * @throws ElevatorMalfunctionException if this elevator is not ready to service
     */
    public void turnOn() throws ElevatorMalfunctionException {
        // this elevator cannot be start
        if (operationSignal == -1)
            throw new ElevatorMalfunctionException(number);

        // set operation signal to normal and standby
        operationSignal = 1;
        standby();
    }

    /**
     * Module.Elevator is about to shutdown, move to ground floor
     * and safely release all on board passenger before turn off.
     */
    public void turnOff() {
        try {
            // try to move to ground floor
            log("电梯#" + number + ": 准备关机, 准备移动到1层释放乘客");

            move(building.getSpecifiedFloor(1));
            operationSignal = 0;

            log("电梯#" + number + ": 已经顺利关机");

        } catch (DockingFailedException d) {
            log("电梯#" + number + ": " + d.getMessage());

            turnToEmergencyMode(LOG_VERBOSE);
        } catch (Exception e) {
            log("电梯#" + number + ": " + e.getMessage());
            turnToEmergencyMode(false);
        }
    }

    /**
     * If malfunction occurred, the elevator will turn to emergency Mode.
     * It need to be repaired and fixed before go back to service.
     * Once it has been fixed, this function can be used to reset the operationSignal back to 0.
     */
    public void resetAlert() {
        operationSignal = 0;
    }

    public void receivePassenger(Passenger passenger) throws OverloadException {
        // try to receive passenger
        if (getCurrWeight() + passenger.getWeight() > getCapacityOfWeight() ||
                getCurrNumOfPassenger() + 1 > getCapacityOfPassengers()) {
            throw new OverloadException("Too many people!");
        }
        sedan.add(passenger);
        currWeight += passenger.getWeight();
        tasks.add(passenger.getTargetFloor());
    }


    /* =================================== Private: Module.Elevator Running Procedure ===================================== */

    /**
     * Involke LogRecorder inorder to record the message of each stage of event.
     *
     * @param msg The status of event
     */
    private void log(String msg) {
        LogRecorder.getInstance().recordLog(LOG_FILENAME, msg, LOG_VERBOSE);
    }

    /**
     * Move the elevator from currFloor to targetFloor.
     *
     * @throws InterruptedException       if failed to sleep thread during move(use to mock real world emergency)
     * @throws FloorDoesNotExistException if requested floor level does not exist
     * @throws DockingFailedException     if failed to dock with the floor
     */
    private void move(Floor targetFloor) throws InterruptedException, FloorDoesNotExistException,
            DockingFailedException {

        log(String.format("电梯#%d: 当前楼层为%d层, 准备移动至%d层", number, currFloor.getFloorLevel(), targetFloor.getFloorLevel()));

        while (currFloor != targetFloor) {
            // current floor has passenger to pick up
            if (tasks.contains(currFloor) && !currFloor.getQueue(direction).isEmpty()) {
                log(String.format("电梯#%d: 在当前楼层%d层发现乘客, 暂停移动先接人", number, currFloor.getFloorLevel()));
                docking();
                break;
            }

            // keep moving to the target floor
            int nextLevel;
            int currLevel = currFloor.getFloorLevel();
            int targetLevel = targetFloor.getFloorLevel();
            boolean isGoingDown = (currLevel > targetLevel);

            if (currLevel > targetLevel) {
                // is going down
                nextLevel = (currLevel - 1 == 0) ? currLevel - 2 : currLevel - 1;
            } else {
                nextLevel = (currLevel + 1 == 0) ? currLevel + 2 : currLevel + 1;
            }

            direction = (isGoingDown) ? -1 : 1;

            // need 1 second to move
            try {
                Thread.sleep(700);
            } catch (InterruptedException i) {
                throw new InterruptedException("Failed to sleep Thread during moving.");
            }
            currFloor = building.getSpecifiedFloor(nextLevel);
        }

        // arrive, about to docking with the door installed at the floor
        docking();
    }

    private void docking() throws DockingFailedException {
        log(String.format("电梯#%d: 正在%d层进行docking....%n", number, currFloor.getFloorLevel()));

        try {
            Thread.sleep(2500);
        } catch (Exception e) {

        }

        // elevator stop at current floor
        currFloor.requestForDocking();
        openDoor();
    }

    private void openDoor() {
        try {
            log(String.format("电梯#%d: 已经完成与%d层对接, 正在开门准备下客....%n", number, currFloor.getFloorLevel()));

            // Open door need 2.5 seconds to complete
            Thread.sleep(2500);

            // unload all onboard passenger whom has arrived
            unload();

            // do not load passenger when in emergency mode
            if (operationSignal != -1)
                load();

            closeDoor();

        } catch (Exception e) {
            log(String.format("电梯#%d: 开门失败, 遇到问题, 请求EMS帮助"));
            turnToEmergencyMode(false);
        }
    }

    /**
     * Close door to be standby to perform the rest task.
     */
    private void closeDoor() {
        try {
            // Close door need 2.5 seconds to complete
            Thread.sleep(2500);

            log(String.format("电梯#%d: 在%d层准备关门....%n", number, currFloor.getFloorLevel()));

        } catch (Exception e) {
            log(String.format("电梯#%d: 关门失败, 可能遇到危险, 请求帮助", number));
            turnToEmergencyMode(false);
        }
        // accepted task
        tasks.remove(currFloor);
    }

    /**
     * Unload passenger depends on the situation.
     * In normal mode, unload passenger whom is arrive to target floor.
     * In emergency mode, unload all passengers.
     */
    private void unload() {
        int num = 0;
        int kg = 0;

        for (int i = 0; i < sedan.size(); i++) {
            Passenger person = sedan.get(i);

            // normal situation that passenger arrive at target floor
            // or in emergency situation all passenger need to escape immediately
            if (operationSignal == -1) {
                num++;
                kg += person.getWeight();
                person.escape();
                currWeight -= person.getWeight();
                sedan.remove(person);

            } else if (person.getTargetFloor() == currFloor) {
                num++;
                kg += person.getWeight();
                person.walkOut(true);
                currWeight -= person.getWeight();
                sedan.remove(person);
            }
        }

        if (num != 0)
            log(String.format("电梯#%d: 乘客已抵达%d层, 共释放%d名乘客, 载重减轻%dkg",
                    number,
                    currFloor.getFloorLevel(),
                    num,
                    kg));
    }

    /**
     * Load passenger on board.
     */
    private void load() {
        currFloor.notifyPassengerOnboard(this);
    }

    /**
     * Standby the elevator, keep checking the tasks list and perform every task.
     */
    private void standby() {
        Floor target;

        while (operationSignal == 1) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException i) {
                log("电梯#" + number + ": " + i.getMessage());
            }

            if (!tasks.isEmpty()) {
                target = tasks.get(0);

                try {
                    // this function will control remained procedure
                    move(target);

                } catch (DockingFailedException d) {
                    log("电梯#" + number + ": " + d.getMessage());
                    turnToEmergencyMode(LOG_VERBOSE);
                } catch (Exception e) {
                    log("电梯#" + number + ": " + e.getMessage());
                    turnToEmergencyMode(false);
                }
            } else {
                direction = 0;
            }
        }
    }

    /**
     * This is the last measure to keep passenger safe.
     * Open door at current floor to unload all onboard passenger.
     */
    private void turnToEmergencyMode(boolean isDockingFailed) {
        // set the operation mode in emergency
        operationSignal = -1;

        // notify the ems
        ems.requestForEmergency(this);

        // passenger can safely leave now
        if (!isDockingFailed) {
            openDoor();
        }
    }


    /* =================================== Getters ================================================================= */
    public int getOperationSignal() {
        return operationSignal;
    }

    public int getNumber() {
        return number;
    }

    public double getCapacityOfWeight() {
        return capacityOfWeight;
    }

    public int getCapacityOfPassengers() {
        return capacityOfPassengers;
    }

    public Floor getCurrFloor() {
        return currFloor;
    }

    public List<Floor> getTasks() {
        return tasks;
    }

    public int getDirection() {
        return direction;
    }

    public int getCurrNumOfPassenger() {
        return sedan.size();
    }

    public int getCurrWeight() {
        return currWeight;
    }

    public List<Passenger> getSedan() {
        return sedan;
    }

}
