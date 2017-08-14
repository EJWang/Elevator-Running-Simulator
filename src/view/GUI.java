package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import Module.Building;
import Module.Elevator;

/**
 * The GUI of Elevator Running Simulator.
 *
 * @author EJWang
 */
public class GUI implements Runnable {

    private Building building;
    private List<Elevator> elevatorList;

    /**
     * Construct a GUI.
     *
     * @param building The building will be display
     */
    public GUI(Building building) {
        this.building = building;
        elevatorList = building.getEMS().getAllElevators();
    }


    class StatusDisplayCanvas extends JComponent {

        /**
         * Construct an animation thread and run it.
         */
        public StatusDisplayCanvas() {
            Thread animationThread = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        repaint();
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            animationThread.start();
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            int width = 25;
            int height = 15;

            int x; // horizontal coordinate
            int y; // vertical coordinate

            for (int i = 0; i < elevatorList.size(); i++) {
                x = i * 50 + 20;
                y = 640 - elevatorList.get(i).getCurrFloor().getFloorLevel() * height;

                // Moving elevator represented as Red color block, standing elevator represented as Black color block
                if (elevatorList.get(i).getDirection() == 0)
                    g2d.setColor(Color.BLACK);
                else
                    g2d.setColor(Color.RED);

                g2d.fillRect(x, y, width, height);
            }
        }

    }


    /**
     * This method should be invoke by start(). Do not call it directly.
     * <p>
     * Start display the GUI.
     */
    @Override
    public void run() {
        JFrame frame = new JFrame("Building Elevator Managerment System");

        // exit when close windows
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(800, 400);
        frame.add(new StatusDisplayCanvas());
        frame.setVisible(true);
        // center of the screen
        frame.setLocationRelativeTo(null);

        DEBUG_ELEVATORS_CURRENT_LEVELS();
    }

    private void DEBUG_ELEVATORS_CURRENT_LEVELS() {
        while (true) {
            try {
                int[] currLevels = new int[4];
                int[] task = new int[4];
                int[] directions = new int[4];
                int[] weights = new int[4];
                int[] persons = new int[4];
                int[] status = new int[4];

                for (int i = 0; i < 4; i++) {
                    currLevels[i] = elevatorList.get(i).getCurrFloor().getFloorLevel();
                    task[i] = elevatorList.get(i).getTasks().size();
                    directions[i] = elevatorList.get(i).getDirection();
                    weights[i] = elevatorList.get(i).getCurrWeight();
                    persons[i] = elevatorList.get(i).getCurrNumOfPassenger();
                    status[i] = elevatorList.get(i).getOperationSignal();
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("GUI Sleep failed");
            }
        }
    }
}
