package Module;

import exceptions.FloorDoesNotExistException;
import java.util.Random;

/**
 * The Module.EventGenerator is responsible to randomly generate event to the Module.Building in order to
 * test the Module.ElevatorManagerSystem in the building.
 * The purpose of this program is for entertainment only, it does not has any actual science meaning
 * in real life. Please be caution of that.
 *
 * @author EJWang
 */
public class EventGenerator implements Runnable {
    /* ---- Running configuration ---- */
    private static final int NUMBER_OF_EVENTS = 5;
    private static final int MAX_TIME = 500;
    private static final int MIN_TIME = 500;

    private static final String LOG_FILENAME = "event_log.txt";
    private static final boolean LOG_VERBOSE = true;

    private Building building;

    /* =================================== Constructor Methods ===================================================== */

    /**
     * Construct an Module.EventGenerator.
     *
     * @param building The building which involved in this experiment
     */
    public EventGenerator(Building building) {
        this.building = building;
    }

    /* =================================== Private Methods ======================================================== */

    /**
     * Involke LogRecorder inorder to record the message of each stage of event.
     *
     * @param msg The status of event
     */
    private void log(String msg) {
        LogRecorder.getInstance().recordLog(LOG_FILENAME, msg, LOG_VERBOSE);
    }

    /* =================================== Override Methods ======================================================== */

    /**
     * This method will be invoked by start().
     */
    @Override
    public void run() {
        int currLevel;
        int targetLevel;
        int weight;

        int high = building.getHighestLevel();
        int low = building.getLowestLevel();
        int times = 0;
        while (times < NUMBER_OF_EVENTS) {
            times++;
            try {
                Random random = new Random();

                do {
                    currLevel = random.nextInt(high - low) + low;
                    targetLevel = random.nextInt(high - low) + low;
                    weight = random.nextInt(80) + 50;
                } while (targetLevel == currLevel || targetLevel == 0 || currLevel == 0);

                // generate current floor and target floor of this event
                Floor currFloor = building.getSpecifiedFloor(currLevel);
                Floor targetFloor = building.getSpecifiedFloor(targetLevel);
                Passenger person = new Passenger(currFloor, targetFloor, weight);

                // Record event
                log(String.format("事件: 在%d层有乘客准备前往%d层, 他/她的体重为: %dkg", currLevel, targetLevel, weight));

                // this will add person to the waiting queue
                currFloor.pushButton(person);

                // time interval of generate event
                Thread.sleep(random.nextInt(MAX_TIME) + MIN_TIME);

            } catch (InterruptedException i) {
                log("事件: " + i.getMessage());
                break;
            } catch (FloorDoesNotExistException f) {
                log("事件: " + f.getMessage());
            }
        }
        while (true) {
            int i = 0;
            for (Elevator elevator : building.getEMS().getAllElevators()) {
                if (elevator.getTasks().size() == 0)
                    i++;
            }
            if (i == 4) {
                building.getEMS().turnOff();
                while (true) {
                    int j = 0;
                    for (Elevator elevator : building.getEMS().getAllElevators()) {
                        try {
                            if (elevator.getCurrFloor() == building.getSpecifiedFloor(1)) {
                                j++;
                            }
                        } catch (Exception e) {

                        }
                    }
                    if (j == 4) {
                        log("所有的模拟运算已经完成!");
                        System.exit(0);
                    }
                }
            }
        }
    }

}
