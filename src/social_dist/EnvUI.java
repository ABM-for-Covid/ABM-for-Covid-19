
package social_dist;

import sim.display.ChartUtilities;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.media.chart.TimeSeriesChartGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class EnvUI extends GUIState {
    public ArrayList<Display2D> displays_abm = new ArrayList<>();
    public Display2D display;
    public Display2D display2;
    public Display2D display3;
    public Display2D display4;
    public JFrame displayFrame;
    public JFrame displayFrame2;
    public JFrame displayFrame3;
    public JFrame displayFrame4;

    ContinuousPortrayal2D glassBox = new ContinuousPortrayal2D();
    ContinuousPortrayal2D blackBox = new ContinuousPortrayal2D();
    ContinuousPortrayal2D quarantinedBox = new ContinuousPortrayal2D();
    ContinuousPortrayal2D testingdBox = new ContinuousPortrayal2D();


    public sim.util.media.chart.TimeSeriesAttributes infectiousAgents;
    public sim.util.media.chart.TimeSeriesAttributes exposedAgents;
    public sim.util.media.chart.TimeSeriesAttributes recoveredAgents;
    public sim.util.media.chart.TimeSeriesAttributes deathCount;
    public TimeSeriesChartGenerator myChart;

    public Object getSimulationInspectedObject() {
        return state;
    }

    public Inspector getInspector() {
        Inspector i = super.getInspector();
        i.setVolatile(true);
        return i;
    }


    public static void main(String[] args) {
        new EnvUI().createController();
    }

    public EnvUI() {
        super(new Env(System.currentTimeMillis()));
    }

    public EnvUI(SimState state) {
        super(state);
    }

    public static String getName() {
        return "Virus Infection";
    }

    public void start() {
        super.start();
        setupPortrayals();
        myChart.clearAllSeries();

        //chart
        ChartUtilities.scheduleSeries(this, infectiousAgents, new sim.util.Valuable() {
            public double doubleValue() { return ((Env) state).getInfected_Agents(); }});

        ChartUtilities.scheduleSeries(this, exposedAgents, new sim.util.Valuable() {
            public double doubleValue() { return ((Env) state).getExposed_Agents(); }});

        ChartUtilities.scheduleSeries(this, deathCount, new sim.util.Valuable() {
            public double doubleValue() { return ((Env) state).getDeath_Count(); }});

        ChartUtilities.scheduleSeries(this, recoveredAgents, new sim.util.Valuable() {
            public double doubleValue() { return ((Env) state).getRecovered_Agents(); }});
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();

        //chart
        ChartUtilities.scheduleSeries(this, infectiousAgents, new sim.util.Valuable() {
            public double doubleValue() {
                return ((Env) state).getInfected_Agents();
            }
        });
    }

    public void paint_displays(Display2D display) {
        Color backdrop_color = new Color(57, 78, 96);
        display.reset();
        display.setBackdrop(backdrop_color);
        display.repaint();
    }


    protected Color humanColor = new Color(15, 165, 189);
    protected Color exposedColor = new Color(192, 183, 76);
    protected Color infectedColorI0 = new Color(255, 122, 47);
    protected Color infectedColorI1 = new Color(230, 23, 255);
    protected Color infectedColorI2 = new Color(255, 27, 59);
    protected Color infectedColorI3 = new Color(255, 27, 59);
    protected Color deadColor = new Color(15, 19, 17);
    protected Color recoveredColor = new Color(9, 255, 42);


    public void add_glassView_colors(Human human, Graphics2D graphics) {
        if (human.isExposed()) {
            exposedColor = new Color(192, 156, 30, human.getOpacity());
            graphics.setColor(exposedColor);
        } else if (human.getInfectionState() == 0) {
            infectedColorI0 = new Color(255, 122, 47, human.getOpacity());
            graphics.setColor(infectedColorI0);
        } else if (human.getInfectionState() == 1) {
            infectedColorI1 = new Color(230, 23, 255, human.getOpacity());
            graphics.setColor(infectedColorI1);
        } else if (human.getInfectionState() == 2 || human.getInfectionState() == 3) {
            graphics.setColor(infectedColorI3);
        } else if (human.dead) graphics.setColor(deadColor);
        else if (human.recovered) graphics.setColor(recoveredColor);
        else {
            humanColor = new Color(11, 172, 189, human.getOpacity());
            graphics.setColor(humanColor);
        }
    }

    public void add_blackBoxView_colors(Human human, Graphics2D graphics) {
        if (human.getInfectionState() == 1 || human.getInfectionState() == 2 || human.getInfectionState() == 3) {
            graphics.setColor(infectedColorI3);
        } else if (human.dead) graphics.setColor(deadColor);

        else {
            humanColor = new Color(11, 172, 189, human.getOpacity());
            graphics.setColor(humanColor);
        }
    }

    public void setupPortrayals() {
        // tell the portrays what to portray and how to portray them
        glassBox.setField(Env.HumansEnvironment);
        blackBox.setField(Env.BlackBoxEnvironment);
        quarantinedBox.setField(Env.QuarantinedEnvironment);
        testingdBox.setField(Env.TestEnvironment);

        glassBox.setPortrayalForAll(new SimplePortrayal2D() {
                                        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                            Human human = (Human) object;
                                            double diamx = info.draw.width * Env.DIAMETER;
                                            double diamy = info.draw.height * Env.DIAMETER;

                                            if (Env.glassView) {
                                                add_glassView_colors(human, graphics);

                                            } else {
                                                add_blackBoxView_colors(human, graphics);
                                            }
                                            if (!(human.isolated || human.quarantined))
                                                graphics.fillOval((int) (info.draw.x - diamx / 2), (int) (info.draw.y - diamy / 2), (int) (diamx), (int) (diamy));
                                            super.draw(human, graphics, info);
                                        }
                                    }
        );

        blackBox.setPortrayalForAll(new SimplePortrayal2D() {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Human human = (Human) object;
                double diamx = info.draw.width * Env.DIAMETER;
                double diamy = info.draw.height * Env.DIAMETER;
                add_blackBoxView_colors(human, graphics);

                if (!(human.isolated || human.quarantined))
                    graphics.fillOval((int) (info.draw.x - diamx / 2), (int) (info.draw.y - diamy / 2), (int) (diamx), (int) (diamy));
                //graphics.drawOval((int) (info.draw.x - 2.5 - diamx / 2), (int) (info.draw.y - 2.5 - diamy / 2), (int) (diamx + 5), (int) (diamy + 5));

                super.draw(human, graphics, info);
            }
        });

        quarantinedBox.setPortrayalForAll(new SimplePortrayal2D() {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Human human = (Human) object;
                double diamx = info.draw.width * Env.DIAMETER;
                double diamy = info.draw.height * Env.DIAMETER;

                add_glassView_colors(human, graphics);

                if (human.isolated || human.quarantined) {
                    graphics.fillOval((int) (info.draw.x - 10 - diamx / 2), (int) (info.draw.y - 10 - diamy / 2), (int) (diamx + 20), (int) (diamy + 20));
                    if (human.prime)
                        graphics.drawOval((int) (info.draw.x - 12.5 - diamx / 2), (int) (info.draw.y - 12.5 - diamy / 2), (int) (diamx + 25), (int) (diamy + 25));
                    super.draw(human, graphics, info);
                    graphics.setColor(Color.BLACK);
                    graphics.drawString("" + human.count_iso, (int) (info.draw.x - (diamx / 2) - 4), (int) (info.draw.y + 4));
                }
                super.draw(human, graphics, info);
            }
        });

        testingdBox.setPortrayalForAll(new SimplePortrayal2D() {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Human human = (Human) object;
                double diamx = info.draw.width * Env.DIAMETER;
                double diamy = info.draw.height * Env.DIAMETER;

                add_glassView_colors(human, graphics);

                graphics.fillOval((int) (info.draw.x - 10 - diamx / 2), (int) (info.draw.y - 10 - diamy / 2), (int) (diamx + 20), (int) (diamy + 20));
                super.draw(human, graphics, info);
                graphics.setColor(Color.WHITE);
                String result_s;
                if (human.test_result_positive)
                    result_s = "Agent " + human.aindex + " Tested Positive (+ve)";
                else result_s = "Agent " + human.aindex + " Tested Negative (-ve)";
                graphics.drawString(result_s, (int) (info.draw.x + 50), (int) (info.draw.y + 4));

                super.draw(human, graphics, info);
            }
        });

        // reschedule the displayer
        paint_displays(display);
        paint_displays(display2);
        paint_displays(display3);
        paint_displays(display4);
    }

    public void init(Controller c) {
        super.init(c);

        display = new Display2D(Env.ENV_XMAX, Env.ENV_YMAX, this);
        display.setScale(0.65);
        displayFrame = display.createFrame();
        displayFrame.setTitle("Glass View of Infection Spread ( SEI3R )");
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
        display.attach(glassBox, "Agents");


        display2 = new Display2D(Env.ENV_XMAX, Env.ENV_YMAX, this);
        display2.setScale(0.65);
        displayFrame2 = display2.createFrame();
        displayFrame2.setTitle("Black Box View of Infection Spread ( SEI3R ) ");
        c.registerFrame(displayFrame2);   // register the frame so it appears in the "Display" list
        displayFrame2.setVisible(false);
        display2.attach(blackBox, "Black Box View");


        display3 = new Display2D(Env.ENV_XMAX, Env.ENV_YMAX, this);
        display3.setScale(0.9);
        displayFrame3 = display3.createFrame();
        displayFrame3.setTitle("Quarantined Box View");
        c.registerFrame(displayFrame3);   // register the frame so it appears in the "Display" list
        displayFrame3.setVisible(false);
        display3.attach(quarantinedBox, "Quarantined Agents");


        display4 = new Display2D(Env.ENV_XMAX, Env.ENV_YMAX, this);
        display4.setScale(0.9);
        displayFrame4 = display4.createFrame();
        displayFrame4.setTitle("Testing Results");
        c.registerFrame(displayFrame4);   // register the frame so it appears in the "Display" list
        displayFrame4.setVisible(false);
        display4.attach(testingdBox, "Testing Results");


        // infection curve chart
        myChart = ChartUtilities.buildTimeSeriesChartGenerator(this, "Infection Curve", "Simulation Steps (1 Day = 500 steps");
        myChart.setYAxisLabel("Count");
        infectiousAgents = ChartUtilities.addSeries(myChart, "Infectious Agents");
        exposedAgents = ChartUtilities.addSeries(myChart, "Exposed Agents");
        recoveredAgents = ChartUtilities.addSeries(myChart, "Recovered Agents");
        deathCount = ChartUtilities.addSeries(myChart, "Death Count");
        infectiousAgents.setStrokeColor(infectedColorI3);
        exposedAgents.setStrokeColor(exposedColor);
        recoveredAgents.setStrokeColor(recoveredColor);
        deathCount.setStrokeColor(deadColor);
    }

    public void quit() {
        super.quit();

        if (displayFrame != null) displayFrame.dispose();
        displayFrame = null;
        display = null;

        if (displayFrame2 != null) displayFrame2.dispose();
        displayFrame2 = null;
        display2 = null;

        if (displayFrame3 != null) displayFrame3.dispose();
        displayFrame3 = null;
        display3 = null;

        if (displayFrame4 != null) displayFrame4.dispose();
        displayFrame4 = null;
        display4 = null;
        myChart.createImage(1600, 1200);
    }

}
