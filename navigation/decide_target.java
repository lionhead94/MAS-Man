package navigation;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Structure;
import jason.environment.grid.Location;
import java.util.*;
import masman_env.MasmanModel;

public class decide_target extends DefaultInternalAction {
    // decide_target(agent, dotList, newTargetX, newTargetY) for pacman
    // decide_target(agent, pacmanPosX, pacmanPosY, actingMode, newTargetX, newTargetY) for blinky and clyde
    // decide_target(agent, pacmanPosX, pacmanPosY, prevPacmanPosX, prevPacmanPosY, actingMode, newTargetX, newTargetY) for pinky and inky

    MasmanModel model = MasmanModel.get();
    private LinkedList<Location> ghostOutsideTargets = new LinkedList<Location>();
    Location pacmanPos;
    int actingMode;

    public decide_target() {
        super();
        ghostOutsideTargets.add(new Location(27, 0));   // blinky
        ghostOutsideTargets.add(new Location( 0, 0));   // pinky
        ghostOutsideTargets.add(new Location(27,30));   // inky
        ghostOutsideTargets.add(new Location( 0,30));   // clyde
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms) throws Exception {
        try {
            int agent = (int) ((NumberTerm) terms[0]).solve();
            switch(agent) {
                case 1: // blinky
                    actingMode = (int)((NumberTerm) terms[3]).solve();
                    // in scatter mode the target is the default outside of the map
                    if(actingMode == 0) {
                        Location l = ghostOutsideTargets.get(agent-1);
                        return unifier.unifies(terms[4], new NumberTermImpl(l.x)) && unifier.unifies(terms[5], new NumberTermImpl(l.y));
                    }

                    // otherwise the target is directly the pacman
                    pacmanPos = new Location((int) ((NumberTerm) terms[1]).solve(), (int) ((NumberTerm) terms[2]).solve());
                    // System.out.println("Blinky's target updated to " + pacmanPos.toString());
                    return unifier.unifies(terms[4], new NumberTermImpl(pacmanPos.x)) && unifier.unifies(terms[5], new NumberTermImpl(pacmanPos.y));

                case 2: // pinky
                    actingMode = (int)((NumberTerm) terms[5]).solve();
                    // in scatter mode the target is the default outside of the map
                    if(actingMode == 0) {
                        Location l = ghostOutsideTargets.get(agent-1);
                        return unifier.unifies(terms[6], new NumberTermImpl(l.x)) && unifier.unifies(terms[7], new NumberTermImpl(l.y));
                    }
                    
                    pacmanPos = new Location((int) ((NumberTerm) terms[1]).solve(), (int) ((NumberTerm) terms[2]).solve());
                    Location prevPacmanPos = new Location((int) ((NumberTerm) terms[3]).solve(), (int) ((NumberTerm) terms[4]).solve());
                    // otherwise tries to understand the pacman direction ad the target is four cells ahead of it
                    if(pacmanPos.y < prevPacmanPos.y) {
                        // System.out.println("Pacman going up, Pinky tries to anticipate it at " + pacmanPos.x + "," + Integer.toString(pacmanPos.y-4));
                        return unifier.unifies(terms[6], new NumberTermImpl(pacmanPos.x)) && unifier.unifies(terms[7], new NumberTermImpl(pacmanPos.y - 4));
                    }
                    else if(pacmanPos.y > prevPacmanPos.y) {
                        // System.out.println("Pacman going down, Pinky tries to anticipate it at " + pacmanPos.x + "," + Integer.toString(pacmanPos.y+4));
                        return unifier.unifies(terms[6], new NumberTermImpl(pacmanPos.x)) && unifier.unifies(terms[7], new NumberTermImpl(pacmanPos.y + 4));
                    }
                    else if(pacmanPos.x < prevPacmanPos.x) {
                        // System.out.println("Pacman going left, Pinky tries to anticipate it at " + Integer.toString(pacmanPos.x-4) + "," + pacmanPos.y);
                        return unifier.unifies(terms[6], new NumberTermImpl(pacmanPos.x - 4)) && unifier.unifies(terms[7], new NumberTermImpl(pacmanPos.y));
                    }
                    else if(pacmanPos.x > prevPacmanPos.x) {
                        // System.out.println("Pacman going right, Pinky tries to anticipate it at " + Integer.toString(pacmanPos.x+4) + "," + pacmanPos.y);
                        return unifier.unifies(terms[6], new NumberTermImpl(pacmanPos.x + 4)) && unifier.unifies(terms[7], new NumberTermImpl(pacmanPos.y));
                    }
                    else {   // still (beginning)
                        // System.out.println("Pacman is still, Pinky's target updated to " + pacmanPos.x + "," + pacmanPos.y);
                        return unifier.unifies(terms[6], new NumberTermImpl(pacmanPos.x)) && unifier.unifies(terms[7], new NumberTermImpl(pacmanPos.y));
                    }

                case 3: // inky
                    actingMode = (int)((NumberTerm) terms[5]).solve();
                    // in scatter mode the target is the default outside of the map
                    if(actingMode == 0) {
                        Location l = ghostOutsideTargets.get(agent-1);
                        return unifier.unifies(terms[6], new NumberTermImpl(l.x)) && unifier.unifies(terms[7], new NumberTermImpl(l.y));
                    }
                    
                    Location blinkyPos = model.getAgPos(1);
                    pacmanPos = new Location((int) ((NumberTerm) terms[1]).solve(), (int) ((NumberTerm) terms[2]).solve());
                    prevPacmanPos = new Location((int) ((NumberTerm) terms[3]).solve(), (int) ((NumberTerm) terms[4]).solve());
                    Location midpoint;
                    // otherwise it tries to understand the pacman direction and considers a point two cells ahead of it
                    if(pacmanPos.y < prevPacmanPos.y)
                        midpoint = new Location(pacmanPos.x, pacmanPos.y - 2);
                    else if(pacmanPos.y > prevPacmanPos.y)
                        midpoint = new Location(pacmanPos.x, pacmanPos.y + 2);
                    else if(pacmanPos.x < prevPacmanPos.x)
                        midpoint = new Location(pacmanPos.x - 2, pacmanPos.y);
                    else if(pacmanPos.x > prevPacmanPos.x)
                        midpoint = new Location(pacmanPos.x + 2, pacmanPos.y);
                    else {   // still (beginning)
                        // System.out.println("Pacman is still, Inky's target updated to " + pacmanPos.x + "," + pacmanPos.y);
                        return unifier.unifies(terms[6], new NumberTermImpl(pacmanPos.x)) && unifier.unifies(terms[7], new NumberTermImpl(pacmanPos.y));
                    }
                    
                    // computes the components of the vector from blinky to midpoint
                    double vecBlinkyMidpointX = midpoint.x - blinkyPos.x;
                    double vecBlinkyMidpointY = midpoint.y - blinkyPos.y;
                    
                    // computes the target applying the vector to midpoint
                    Location target = new Location((int) Math.round(vecBlinkyMidpointX + midpoint.x), (int) Math.round(vecBlinkyMidpointY + midpoint.y));
                    return unifier.unifies(terms[6], new NumberTermImpl(target.x)) && unifier.unifies(terms[7], new NumberTermImpl(target.y));
                
                case 4:
                    pacmanPos = new Location((int) ((NumberTerm) terms[1]).solve(), (int) ((NumberTerm) terms[2]).solve());
                    int distFromPacman = model.getAgPos(4).distance(pacmanPos);
                    actingMode = (int)((NumberTerm) terms[3]).solve();
                    // if far enough and in seek mode approaches the pacman
                    if(distFromPacman > 8 && actingMode == 1) {
                        // System.out.println("Clyde hunts the pacman, target updated to " + pacmanPos.toString());
                        return unifier.unifies(terms[4], new NumberTermImpl(pacmanPos.x)) && unifier.unifies(terms[5], new NumberTermImpl(pacmanPos.y));
                    }
                    // otherwise the target is the default outside of the map
                    // System.out.println("Clyde is too afraid and runs away, target updated to " + ghostOutsideTargets.get(3).toString());    
                    return unifier.unifies(terms[4], new NumberTermImpl(ghostOutsideTargets.get(3).x)) && unifier.unifies(terms[5], new NumberTermImpl(ghostOutsideTargets.get(3).y));
                
                default:    // pacman
                    // get the list of remaining dots terms
                    List<Term> list = ((ListTerm)(terms[1])).getAsList();
                    Random random = new Random();
                    // pick a random one and return its coords unpacking the term
                    Structure pick = (Structure)(list.get(random.nextInt(list.size())));
                    return unifier.unifies(terms[2], pick.getTerm(0)) && unifier.unifies(terms[3], pick.getTerm(1));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}