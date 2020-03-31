
package social_dist;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;

import javax.swing.*;
import java.awt.*;


public class envUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;

    ContinuousPortrayal2D vidPortrayal = new ContinuousPortrayal2D();

    public Object getSimulationInspectedObject() { return state; }
    public Inspector getInspector()
    {
        Inspector i = super.getInspector();
        i.setVolatile(true);
        return i;
    }


    public static void main(String[] args) {
        new envUI().createController();
    }

    public envUI() {
        super(new env(System.currentTimeMillis()));
    }

    public envUI(SimState state) {
        super(state);
    }

    public static String getName() {
        return "Virus Infection";
    }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals() {

        vidPortrayal.setField(((env) state).HumansEnvironment);

        // reschedule the displayer
        display.reset();
        display.setBackdrop(Color.white);

        // redraw the display
        display.repaint();
    }

    public void init(Controller c) {
        super.init(c);

        display = new Display2D(1100, 600, this);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Covid 19 Disease Spread");
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
        display.attach(vidPortrayal, "Agents");
    }

    public void quit() {
        super.quit();

        if (displayFrame != null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

}
