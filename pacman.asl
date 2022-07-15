/* Initial beliefs */

/* Initial goal */

/* Plans */
+active : true <- 
    .print("Pacman is alive");
    .findall(dot(A,B), dot(A,B), List);
    navigation.decide_target(0, List, X, Y);
    +target(X, Y);
    .print("Picked point: (", X, ", ", Y, ")");
    !approach_target.

// all dots have been picked up, stop the ghosts
+!collect_all_dots : not remaining_dots(_) <-
    .print("Pacman collected all the dots");
    .send(blinky, tell, looser);
    .send(pinky, tell, looser);
    .send(inky, tell, looser);
    .send(clyde, tell, looser).
// not all dots have been picked up, a new one is selected
+!collect_all_dots : remaining_dots(N) <-
    .print("Still dots on the map, continue collection");
    .findall(dot(A,B), dot(A,B), List);
    navigation.decide_target(0, List, X, Y);
    -+target(X,Y);
    .print("Picked point: (", X, ", ", Y, ")");
    !approach_target.

// pacman arrived to target
+!approach_target : at(X,Y) & target(X,Y) <-
    .print("Target (", X, ",", Y, ") achieved");
    remove_dot(X,Y);
    !collect_all_dots.
// pacman at crossroad containing a dot
+!approach_target : at(X,Y) & prev_at(PX,PY) & crossroad(X,Y) & target(TX,TY) & dot(X,Y) <-
    check_pacman_killed(X,Y);
    remove_dot(X,Y);
    navigation.decide_crossroad(X, Y, PX, PY, TX, TY, ChoiceX, ChoiceY);
    .print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.
// pacman at empty crossroad
+!approach_target : at(X,Y) & prev_at(PX,PY) & crossroad(X,Y) & target(TX,TY) <-
    check_pacman_killed(X,Y);
    navigation.decide_crossroad(X, Y, PX, PY, TX, TY, ChoiceX, ChoiceY);
    .print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.
// pacman on corridor tile containing dot
+!approach_target : at(X,Y) & prev_at(PX,PY) & not crossroad(X,Y) & dot(X,Y) <-
    check_pacman_killed(X,Y);
    remove_dot(X,Y);
    navigation.decide_step_on_path(0, X, Y, PX, PY, ChoiceX, ChoiceY);
    //.print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.
// pacman on empty corridor tile
+!approach_target : at(X,Y) & prev_at(PX,PY) & not crossroad(X,Y) <-
    check_pacman_killed(X,Y);
    navigation.decide_step_on_path(0, X, Y, PX, PY, ChoiceX, ChoiceY);
    //.print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.

// when killed by a ghost all the goals are failed and the pacmann stops
+killed : true <-
    .print("Got killed");
    .fail_goal(approach_target);
    .fail_goal(collect_all_dots).