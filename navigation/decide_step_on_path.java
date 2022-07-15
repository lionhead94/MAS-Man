package navigation;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Atom;
import jason.environment.grid.Location;
import java.util.*;
import masman_env.MasmanModel;

public class decide_step_on_path extends DefaultInternalAction {
    // decide_step_on_path(agent, agentPosX, agentPosY, lastPosX, lastPosY, computedPosX, computedPosY)

    private MasmanModel model = MasmanModel.get();
    private int pacmanWarnDist = 2;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms) throws Exception {
        try {
            int agent = (int) ((NumberTerm) terms[0]).solve();
            Location agentPos = new Location((int) ((NumberTerm) terms[1]).solve(), (int) ((NumberTerm) terms[2]).solve());
            Location lastPos = new Location((int) ((NumberTerm) terms[3]).solve(), (int) ((NumberTerm) terms[4]).solve());
            Location turnPos;
            // recovers the previous step in order to perform the next coherently
            if(agentPos.y < lastPos.y){
                // if the pacman sees a ghost within the warning distance reverses course
                if(agent == 0 && checkGhostApproaching(agentPos, "up"))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y + 1));
                // when possible every agent proceeds straight on the same direction
                if(!model.hasObject(model.OBSTACLE, new Location(agentPos.x, agentPos.y - 1)))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y - 1));
            }
            else if(agentPos.y > lastPos.y) {
                if(agent == 0 && checkGhostApproaching(agentPos, "down"))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y - 1));
                if(!model.hasObject(model.OBSTACLE, new Location(agentPos.x, agentPos.y + 1)))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y + 1));
            }
            else if(agentPos.x < lastPos.x) {
                if(agent == 0 && checkGhostApproaching(agentPos, "left"))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x + 1)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y));
                if(!model.hasObject(model.OBSTACLE, new Location(agentPos.x - 1, agentPos.y)))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x - 1)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y));
            }
            else if(agentPos.x > lastPos.x) {
                if(agent == 0 && checkGhostApproaching(agentPos, "right"))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x - 1)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y));
                if(!model.hasObject(model.OBSTACLE, new Location(agentPos.x + 1, agentPos.y)))
                    return unifier.unifies(terms[5], new NumberTermImpl(agentPos.x + 1)) && unifier.unifies(terms[6], new NumberTermImpl(agentPos.y));
            }
            // if here the agent is at a corner and not possible to continue straight. The right direction wrt to it is computed
            turnPos = solveCorner(agentPos, lastPos);
            return unifier.unifies(terms[5], new NumberTermImpl(turnPos.x)) && unifier.unifies(terms[6], new NumberTermImpl(turnPos.y));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private Location solveCorner(Location agentPos, Location prevPos) {
        List<Location> alternatives = new LinkedList<Location>();
        alternatives.add(new Location(agentPos.x, agentPos.y - 1)); // up
        alternatives.add(new Location(agentPos.x - 1, agentPos.y)); // left
        alternatives.add(new Location(agentPos.x, agentPos.y + 1)); // down
        alternatives.add(new Location(agentPos.x + 1, agentPos.y)); // right
        // picks the first free tile that is not the actual position proceeding in order
        for(int i=0; i<alternatives.size(); ++i) {
            Location alt = alternatives.get(i);
            if(!alt.equals(prevPos) && !model.hasObject(model.OBSTACLE, alt))
                return alt;
        }
        return new Location(-1,-1);
    }

    private boolean checkGhostApproaching(Location pacmanPos, String direction) {
        // scans the ahead tiles to check if a ghost is approaching 
        if(direction.equals("up")) {
            for(int i=1; i<=pacmanWarnDist; ++i) {
                if(model.hasObject(model.AGENT, new Location(pacmanPos.x, pacmanPos.y-i)))
                    return true;
            }
        }
        else if(direction.equals("left")) {
            for(int i=1; i<=pacmanWarnDist; ++i) {
                if(model.hasObject(model.AGENT, new Location(pacmanPos.x-i, pacmanPos.y)))
                    return true;
            }
        }
        else if(direction.equals("down")) {
            for(int i=1; i<=pacmanWarnDist; ++i) {
                if(model.hasObject(model.AGENT, new Location(pacmanPos.x, pacmanPos.y+i)))
                    return true;
            }
        }
        else if(direction.equals("right")) {
            for(int i=1; i<=pacmanWarnDist; ++i) {
                if(model.hasObject(model.AGENT, new Location(pacmanPos.x+i, pacmanPos.y)))
                    return true;
            }
        }
        return false;
    }
}