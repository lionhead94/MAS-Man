package masman_env;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;

import java.util.Collections;
import java.util.logging.Logger;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MasmanEnv extends Environment {
    private MasmanModel masmanModel;
    private Logger logger;
    private String[] ghosts = {"blinky", "pinky", "inky", "clyde"};
    // [pacman, blinky, pinky, inky, clyde, prevPacman] when filled
    private LinkedList<Location> agentsPositions = new LinkedList<Location>();
    private Timer timer;
    private int chase_time = 20;
    private int scatter_time = 7;

    @Override
    public void init(String[] args) {
        // environment components setup
        logger = Logger.getLogger(MasmanEnv.class.getName());
        masmanModel = MasmanModel.get();
        MasmanView masmanView = new MasmanView(masmanModel);
        masmanModel.setView(masmanView);

        // init pacman percepts
        Location pacmanLoc = masmanModel.getAgPos(agentStrToInt("pacman"));
        agentsPositions.add(pacmanLoc);
        // add actual and previous position
        addPercept("pacman", Literal.parseLiteral("at(" + pacmanLoc.x + "," + pacmanLoc.y + ")"));
        addPercept("pacman", Literal.parseLiteral("prev_at(" + pacmanLoc.x + "," + pacmanLoc.y + ")"));
        // add knowledge about dots positions and crossroad positions
        addDotsPercepts();
        addCrossroadsPercepts("pacman");
        
        // init ghosts percepts
        for(String ghost:ghosts) {
            // add actual and previous position
            Location ghostLoc = masmanModel.getAgPos(agentStrToInt(ghost));
            addPercept(ghost, Literal.parseLiteral("at(" + ghostLoc.x + "," + ghostLoc.y + ")"));
            addPercept(ghost, Literal.parseLiteral("prev_at(" + ghostLoc.x + "," + ghostLoc.y + ")"));
            agentsPositions.add(ghostLoc);
            // add knowledge about the crossroads
            addCrossroadsPercepts(ghost);
            addPercept(ghost, Literal.parseLiteral("mode(0)")); // initially all in scatter mode
        }
        // prev pacman position
        agentsPositions.add(pacmanLoc);
        // activate pacman
        addPercept("pacman", Literal.parseLiteral("active"));
        // activate blinky
        updateGhostPercepts("blinky");
        addPercept("blinky", Literal.parseLiteral("active"));

        // setup scatter/chase timer
        logger.info("[ SCATTER PHASE ]");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int scatter_duration = scatter_time;
            int chase_duration = chase_time;
            public void run() {
                if(scatter_duration > 0) {
                    scatter_duration--;
                    if(scatter_duration == 0) {
                        logger.info("[ CHASE PHASE ]");
                        for(String ghost:ghosts) {
                            removePercept(ghost, Literal.parseLiteral("mode(0)"));
                            addPercept(ghost, Literal.parseLiteral("mode(1)"));
                        }
                    }
                }
                else {
                    chase_duration--;
                    if(chase_duration == 0) {
                        logger.info("[ SCATTER PHASE ]");
                        chase_duration = chase_time;
                        scatter_duration = scatter_time;
                        for(String ghost:ghosts) {
                            removePercept(ghost, Literal.parseLiteral("mode(1)"));
                            addPercept(ghost, Literal.parseLiteral("mode(0)"));
                        }
                    }
                }
            }
        }, 0, 1000);    // timer starts with no delay and executes run() every second
    }

    public void updateGhostPercepts(String ghost) {
        // remove old info about the pacman
        removePerceptsByUnif(ghost, Literal.parseLiteral("pacman_at(_,_)"));
        removePerceptsByUnif(ghost, Literal.parseLiteral("prev_pacman_at(_,_)"));
        // update pacman positions
        Location pacmanPos = agentsPositions.get(agentStrToInt("pacman"));
        addPercept(ghost, Literal.parseLiteral("pacman_at(" + pacmanPos.x + ", " + pacmanPos.y + ")"));
        Location oldPacmanPos = agentsPositions.get(agentsPositions.size()-1);
        addPercept(ghost, Literal.parseLiteral("prev_pacman_at(" + oldPacmanPos.x + ", " + oldPacmanPos.y + ")"));
    }

    @Override
    public boolean executeAction(String agent, Structure action) {
        boolean result = false;
        // callable by all the agents
        if(action.getFunctor().equals("move_to")) {
            String x = action.getTerm(0).toString();
            String y = action.getTerm(1).toString();
            Location dest = null;
            dest = new Location(Integer.parseInt(x), Integer.parseInt(y));
            if(!masmanModel.inGrid(dest.x, dest.y))
                throw new NumberFormatException();
            try {
                result = masmanModel.move_to(agentStrToInt(agent), dest);
                if(result) {
                    // update the percepts of the agent after the motion
                    Location agentLoc = masmanModel.getAgPos(agentStrToInt(agent));
                    Location agentPrevLoc = agentsPositions.get(agentStrToInt(agent));
                    if(agent.equals("pacman"))  // keep track of the old position
                        agentsPositions.set(agentsPositions.size()-1, agentsPositions.get(agentStrToInt("pacman")));
                    removePerceptsByUnif(agent, Literal.parseLiteral("at(_,_)"));
                    removePerceptsByUnif(agent, Literal.parseLiteral("prev_at(_,_)"));
                    addPercept(agent, Literal.parseLiteral("at(" + agentLoc.x + "," + agentLoc.y + ")")); 
                    addPercept(agent, Literal.parseLiteral("prev_at(" + agentPrevLoc.x + "," + agentPrevLoc.y + ")"));
                    agentsPositions.set(agentStrToInt(agent), agentLoc);
                    if(agent.equals("pacman")) {
                        // update ghosts' perceptions about the pacman
                        for(String ghost:ghosts)
                            updateGhostPercepts(ghost);
                    }
                    try {    
                        // just to make the graphical representation observable
                        // lower amounts tend to generate uneven updates
                        Thread.sleep(500);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch(NumberFormatException nfe) {
                logger.info("ERROR: action 'move_to' called passing one or more invalid coordinates");
            }
        }
        // callable by all the agents
        else if(action.getFunctor().equals("check_pacman_killed")){
            String x = action.getTerm(0).toString();
            String y = action.getTerm(1).toString();
            Location pos = null;
            pos = new Location(Integer.parseInt(x), Integer.parseInt(y));
            if(!masmanModel.inGrid(pos.x, pos.y))
                throw new NumberFormatException();
            result = true;
            int output = masmanModel.check_pacman_killed(pos);
            if(output > 0) {
                // stop the pacman
                addPercept("pacman", Literal.parseLiteral("killed"));
                // stop the ghosts informing the killer of its success
                for(String ghost:ghosts) {
                    if(!ghost.equals(agentIntToStr(output)))
                        addPercept(ghost, Literal.parseLiteral("stop"));
                    else
                        addPercept(ghost, Literal.parseLiteral("killer"));
                }
                // stop the chase/scatter timer
                timer.cancel();
            }
        }
        // only pacman can eat dots
        else if(agent.equals("pacman")) {
            if(action.getFunctor().equals("remove_dot")) {
                Location pacmanPos = masmanModel.getAgPos(agentStrToInt("pacman"));
                int output = masmanModel.remove_dot(pacmanPos);
                if(output >= 0) {
                    result = true;
                    // update remaining dots amount
                    removePerceptsByUnif("pacman", Literal.parseLiteral("remaining_dots(_)."));
                    if(masmanModel.getRemainingDots() > 0)
                        addPercept("pacman", Literal.parseLiteral("remaining_dots(" + masmanModel.getRemainingDots() + ")"));
                    // remove the percept of the removed dot
                    removePercept("pacman", Literal.parseLiteral("dot(" + pacmanPos.x + ", " + pacmanPos.y + ")"));
                    if(output > 0)
                        // enough dots have been removed to activate a new ghost
                        activate(output);
                }
            }
        }
        return result;
    }

    // utility to convert agent name to agent id
    private int agentStrToInt(String from) {
        if(from.equals("pacman"))
            return 0;
        else if(from.equals("blinky"))
            return 1;
        else if(from.equals("pinky"))
            return 2;
        else if(from.equals("inky"))
            return 3;
        return 4;
    }

    // utility to convert agent id to agent name
    private String agentIntToStr(int from) {
        if(from == 0)
            return "pacman";
        return ghosts[--from];
    }

    // activates a new ghost
    private void activate(int agent) {
        // informs the agent on the pacman positions
        updateGhostPercepts(agentIntToStr(agent));
        // places the ghost outside the ghost house
        masmanModel.setAgPos(agent, new Location(14,11));
        // updates the ghost percepts on itself and activates it
        removePerceptsByUnif(agentIntToStr(agent), Literal.parseLiteral("at(_,_)"));
        removePerceptsByUnif(agentIntToStr(agent), Literal.parseLiteral("prev_at(_,_)"));
        addPercept(agentIntToStr(agent), Literal.parseLiteral("at(14,11)"));
        addPercept(agentIntToStr(agent), Literal.parseLiteral("prev_at(14,11)"));
        addPercept(agentIntToStr(agent), Literal.parseLiteral("active"));
    }

    // adds one percept to the pacman for each dot on the map
    private void addDotsPercepts() {
        HashSet<Location> dots = masmanModel.getDotSet();
        for(Location dot:dots)
            addPercept("pacman", Literal.parseLiteral("dot(" + dot.x + "," + dot.y + ")"));
    }

    // adds one percept to the agent for each crossroad on the map
    private void addCrossroadsPercepts(String agent) {
        addPercept(agent, Literal.parseLiteral("crossroad( 6, 1)"));
        addPercept(agent, Literal.parseLiteral("crossroad(21, 1)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 1, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 6, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 9, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad(12, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad(15, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad(18, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad(21, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad(26, 5)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 6, 8)"));
        addPercept(agent, Literal.parseLiteral("crossroad(21, 8)"));
        addPercept(agent, Literal.parseLiteral("crossroad(12,11)"));
        addPercept(agent, Literal.parseLiteral("crossroad(15,11)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 6,14)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 9,14)"));
        addPercept(agent, Literal.parseLiteral("crossroad(18,14)"));
        addPercept(agent, Literal.parseLiteral("crossroad(21,14)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 6,20)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 9,20)"));
        addPercept(agent, Literal.parseLiteral("crossroad(18,20)"));
        addPercept(agent, Literal.parseLiteral("crossroad(21,20)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 6,23)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 9,23)"));
        addPercept(agent, Literal.parseLiteral("crossroad(12,23)"));
        addPercept(agent, Literal.parseLiteral("crossroad(15,23)"));
        addPercept(agent, Literal.parseLiteral("crossroad(18,23)"));
        addPercept(agent, Literal.parseLiteral("crossroad(21,23)"));
        addPercept(agent, Literal.parseLiteral("crossroad( 3,26)"));
        addPercept(agent, Literal.parseLiteral("crossroad(24,26)"));
        addPercept(agent, Literal.parseLiteral("crossroad(12,29)"));
        addPercept(agent, Literal.parseLiteral("crossroad(15,29)"));
    }
}