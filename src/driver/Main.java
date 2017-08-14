package driver;

import Module.Building;
import Module.ElevatorManagerSystem;
import Module.EventGenerator;
import view.GUI;

/**
 * The entry-point of this program.
 * Everything will be configured and set up at this class.
 *
 * @author EJWang
 */
public class Main {

    /**
     * The main method.
     *
     * @param args This will not be used
     */
    public static void main(String[] args) throws Exception {
        // create a building with lowest floor -4 and highest floor 43
        Building building = new Building(-4, 43);

        // EMS in building
        ElevatorManagerSystem ems = building.getEMS();

        // construct 4 elevators with capacity of 1300kg and 11 passengers
        ems.constructElevators(4, 1300.0, 11);

        // start GUI
        new Thread(new GUI(building)).start();

        // start ems thread, and ems will start all thread of elevator
        new Thread(ems).start();

        // start generate event
        new Thread(new EventGenerator(building)).start();
    }
}
