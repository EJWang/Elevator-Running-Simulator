package Module;

import exceptions.FloorDoesNotExistException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Module.Building which involved in this simulation.
 * It has the basic information of total number of floor level,
 * the total number of elevators and the Module.Elevator Manager System.
 *
 * @author EJWang
 */
public class Building {

    // Module.Elevator Manager System of this building
    private ElevatorManagerSystem ems;

    // the list of all floor levels of this building
    private List<Floor> floorLevels;

    // the highest and lowest floor level
    private int lowestLevel;
    private int highestLevel;

    /* =================================== Constructor Methods ===================================================== */

    /**
     * Construct a Module.Building.
     *
     * @param lowestLevel  The lowest level of this building
     * @param highestLevel The highest level of this building
     */
    public Building(int lowestLevel, int highestLevel) throws Exception {
        this.lowestLevel = lowestLevel;
        this.highestLevel = highestLevel;
        ems = new ElevatorManagerSystem(this);

        // construct Module.Floor based on lowest level and highest level
        floorLevels = new ArrayList<>();
        for (int i = lowestLevel; i <= highestLevel; i++) {
            // skip 0 floor level
            if (i == 0)
                continue;
            floorLevels.add(new Floor(i, ems));
        }
    }


    /* =================================== Public Methods ========================================================== */

    /**
     * Get specified floor level of this building.
     *
     * @param floorLevel The level of requested floor
     * @return The requested floor level
     * @throws FloorDoesNotExistException if requested floor level does not exist
     */
    public Floor getSpecifiedFloor(int floorLevel) throws FloorDoesNotExistException {
        Floor targetFloor = null;

        // search for the target floor
        for (Floor floor : floorLevels)
            if (floor.getFloorLevel() == floorLevel)
                targetFloor = floor;

        // request floor does not exist in this building
        if (targetFloor == null)
            throw new FloorDoesNotExistException("Module.Floor level " + floorLevel + " does not exist.");

        return targetFloor;
    }


    /* =================================== Getters ================================================================= */

    public int getLowestLevel() {
        return lowestLevel;
    }

    public int getHighestLevel() {
        return highestLevel;
    }

    public int getTotalLevels() {
        return Math.abs(this.getLowestLevel()) + this.getHighestLevel();
    }

    public ElevatorManagerSystem getEMS() {
        return ems;
    }
}
