package social_natural;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;

import javax.swing.*;
import java.awt.*;


public class infectionUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;

    ContinuousPortrayal2D vidPortrayal = new ContinuousPortrayal2D();

    public static void main(String[] args) {
        new infectionUI().createController();
    }

    public infectionUI() {
        super(new infection(System.currentTimeMillis()));
    }

    public infectionUI(SimState state) {
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
        // tell the portrayals what to portray and how to portray them
        vidPortrayal.setField(((infection) state).HumansEnvironment);

        // reschedule the displayer
        display.reset();
        display.setBackdrop(Color.white);

        // redraw the display
        display.repaint();
    }

    public void init(Controller c) {
        super.init(c);

        // make the displayer
        display = new Display2D(1100, 600, this);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Virus (Dis)Infection Demonstration Display");
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
