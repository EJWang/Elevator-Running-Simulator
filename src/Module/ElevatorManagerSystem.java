package Module;

import Module.Building;
import Module.Elevator;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Module.ElevatorManagerSystem is used to receive user-request-to-use-elevator signal then
 * allocate task to specified elevator. It depends on the workload of the elevator and
 * the priority level of the elevator.
 * <p>
 * Define priority and workload:
 * 1. The direction of user-want-to-go and the running direction of elevator:
 * 2. The number of number of floor that the elevator must go to pick up resident.
 * 3. The distance between elevator and user
 *
 * @author EJWang
 */
public class ElevatorManagerSystem implements Runnable {

    /* ---- Running Configuration ---- */
    private static final int DELAY = 100;
    private static final String LOG_FILENAME = "ems_log.txt";
    private static final boolean LOG_VERBOSE = true;

    // the client which use this system
    private Building building;

    // holding tasks of this system
    private Queue<Floor> tasks;

    // all elevators controlled by this system
    private List<Elevator> elevators;

    /* =================================== Constructor Methods ===================================================== */

    /**
     * Construct an Module.Elevator Manager System.
     *
     * @param building The building act as client and use this system
     * @throws IOException if failed to initialize log writer
     */
    public ElevatorManagerSystem(Building building) throws IOException {
        this.building = building;
        tasks = new LinkedList<>();
        elevators = new ArrayList<>();
    }

    /**
     * Construct elevators.
     *
     * @param totalNumberOfElevator The total number of elevators
     * @param weightCapacity        The maximum weight allowance of every elevator
     * @param passengerCapacity     The maximum passenger capacity of every elevator
     */
    public void constructElevators(int totalNumberOfElevator, double weightCapacity, int passengerCapacity) {
        for (int elevatorNumber = 1; elevatorNumber <= totalNumberOfElevator; elevatorNumber++)
            elevators.add(new Elevator(elevatorNumber, weightCapacity, passengerCapacity, this, building));
    }

    /* =================================== Override Methods ======================================================== */

    /**
     * Start all elevators as Thread, and then proceed to standby.
     */
    @Override
    public void run() {
        // put all elevators as a thread then start it
        for (Elevator elevator : elevators)
            new Thread(elevator).start();
        // let this system standby for receiving task from each floor level
        standby();
    }


    /* =================================== Public Methods ========================================================== */

    /**
     * Get the list of all elevators.
     *
     * @return The list of all elevators.
     */
    public List<Elevator> getAllElevators() {
        return elevators;
    }

    /**
     * Add task to the system.
     *
     * @param floor The floor which is a task
     */
    public void addTask(Floor floor) {
        tasks.add(floor);
    }

    /**
     * Turn off the system and notify all elevators move to the ground floor to safely unload the passenger.
     */
    public void turnOff() {
        log("EMS: 准备关机中");

        // notify all elevators to shutting down
        for (Elevator elevator : elevators)
            elevator.turnOff();

        log("EMS: 所有电梯已经成功关闭, 主系统准备Shutdown....");
    }

    /**
     * Display the emergency information.
     *
     * @param elevator The elevator which request for emergency
     */
    public void requestForEmergency(Elevator elevator) {
        log(String.format("EMS: %d号电梯在%d层请求支援!!",
                elevator.getNumber(),
                elevator.getCurrFloor().getFloorLevel()));
    }


    /* =================================== Private: System Running Procedure ======================================= */

    /**
     * Involke LogRecorder inorder to record the message of each stage of event.
     *
     * @param msg The status of event
     */
    private void log(String msg) {
        LogRecorder.getInstance().recordLog(LOG_FILENAME, msg, LOG_VERBOSE);
    }

    /**
     * Search for the most eligible elevator then assign the task to it.
     *
     * @param targetFloor The floor which request for service
     */
    private void searchElevatorAndAssignTask(Floor targetFloor) {
        // buffer elevator list
        int[] info = new int[elevators.size()];

        // find the elevator which has the lowest workload
        for (int i = 0; i < elevators.size(); i++) {
            // skip the malfunction elevator
            if (elevators.get(i).getOperationSignal() != 1) {
                continue;
            }

            // the elevator has the lowest workload
            int currWorkLoad = elevators.get(i).getWorkLoad(targetFloor);
            if (i == 0) {
                info[0] = i;
                info[1] = currWorkLoad;
            } else if (currWorkLoad < info[1]) {
                info[0] = i;
                info[1] = currWorkLoad;
            }
        }

        // send task to selected elevator
        elevators.get(info[0]).addTask(targetFloor);
    }

    /**
     * Start execute task waiting in this system.
     * Allocate task to the most suitable elevator.
     */
    private void standby() {
        while (true) {
            // Thread sleep control
            try {
                Thread.sleep(DELAY);
            } catch (Exception e) {
                log("EMS: Standby()线程休眠失败");
            }

            // find the most eligible elevator then assign the task to it
            if (tasks.size() != 0)
                searchElevatorAndAssignTask(tasks.poll());
        }
    }
}
