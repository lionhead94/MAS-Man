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

public class decide_crossroad extends DefaultInternalAction {
    // decide_crossroad(crossRoadPosX, crossRoadPosY, lastAgentPosX, lastAgentPosY, targetPosX, targetPosY, computedPosX, computedPosY)

    MasmanModel model = MasmanModel.get();

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms) throws Exception {
        try {
            Location crossroadPos = new Location((int) ((NumberTerm) terms[0]).solve(), (int) ((NumberTerm) terms[1]).solve());
            Location lastPos = new Location((int) ((NumberTerm) terms[2]).solve(), (int) ((NumberTerm) terms[3]).solve());
            Location targetPos = new Location((int) ((NumberTerm) terms[4]).solve(), (int) ((NumberTerm) terms[5]).solve());
            
            // computes the position of the four potential alternatives
            Location up = new Location(crossroadPos.x, crossroadPos.y - 1);
            Location left = new Location(crossroadPos.x - 1, crossroadPos.y);
            Location down = new Location(crossroadPos.x, crossroadPos.y + 1);
            Location right = new Location(crossroadPos.x + 1, crossroadPos.y);
            List<Location> alternatives = new LinkedList<Location>();
            alternatives.add(up);
            alternatives.add(left);
            alternatives.add(down);
            alternatives.add(right);
            
            // scans in order up -> left -> down -> right the alternatives
            List<Double> distances = new LinkedList<Double>();
            for(int i=0; i<alternatives.size(); ++i) {
                Location loc = alternatives.get(i);
                // if it is a valid and free cell its distance from the agent's target is computed
                if(model.inGrid(loc) && !model.hasObject(model.OBSTACLE, loc) && !loc.equals(lastPos))
                    distances.add(loc.distanceEuclidean(targetPos));
                // otherwise a dummy value is added to the distances list
                else
                    distances.add(Double.MAX_VALUE);
            }
            
            // for(int i=0; i<distances.size(); ++i) 
            //     System.out.println("dist from (" + alternatives.get(i).x + "," + alternatives.get(i).y + ") = " + distances.get(i));
            
            // the choosen alternative is the one that minimizes the flight distance from the target
            switch(distances.indexOf(Collections.min(distances))) {
                case(0):
                    return unifier.unifies(terms[6], new NumberTermImpl(up.x)) && unifier.unifies(terms[7], new NumberTermImpl(up.y));
                case(1):
                    return unifier.unifies(terms[6], new NumberTermImpl(left.x)) && unifier.unifies(terms[7], new NumberTermImpl(left.y));
                case(2):
                    return unifier.unifies(terms[6], new NumberTermImpl(down.x)) && unifier.unifies(terms[7], new NumberTermImpl(down.y));
                default:
                    return unifier.unifies(terms[6], new NumberTermImpl(right.x)) && unifier.unifies(terms[7], new NumberTermImpl(right.y));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}