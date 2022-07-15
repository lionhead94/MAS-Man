/* Initial beliefs */

/* Initial goal */

/* Plans */
+active : pacman_at(PacX,PacY) & prev_pacman_at(PrevPacX,PrevPacY) & mode(M) <-
    .print("Inky is alive");
    navigation.decide_target(3, PacX, PacY, PrevPacX, PrevPacY, M, TX, TY);
    +target(TX,TY);
    //.print("Targeting ", TX, ",", TY); 
    !approach_target.

// inky at crossroad decides where to go depending on pacman position
+!approach_target : at(X,Y) & prev_at(PrevX,PrevY) & crossroad(X,Y) & pacman_pos(PacX,PacY) & prev_pacman_pos(PrevPacX,PrevPacY) & mode(M) <-
    check_pacman_killed(X,Y);
    navigation.decide_target(3, PacX, PacY, PrevPacX, PrevPacY, M, TX, TY);
    -+target(TX,TY);
    navigation.decide_crossroad(X, Y, PrevX, PrevY, TX, TY, ChoiceX, ChoiceY);
    .print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.
// blinky in corridor advances coherently with the past movement
+!approach_target : at(X,Y) & prev_at(PrevX,PrevY) & not crossroad(X,Y) & pacman_pos(PacX,PacY) & prev_pacman_pos(PrevPacX,PrevPacY) & mode(M) <-
    check_pacman_killed(X,Y);
    navigation.decide_target(3, PacX, PacY, PrevPacX, PrevPacY, M, TX, TY);
    -+target(TX,TY);
    navigation.decide_step_on_path(2, X, Y, PrevX, PrevY, ChoiceX, ChoiceY);
    //.print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.

// stores a local copy of the new and last pacman pos to avoid problems during 
// the plan selection
+pacman_at(X,Y) <-
    -+pacman_pos(X,Y).

+prev_pacman_at(X,Y) <-
    -+prev_pacman_pos(X,Y).

// inky or another ghost killed the pacman, stops execution
+stop : true <-
    .succeed_goal(approach_target).

// inky killed the pacman
+killer : true <-
    .print("I killed the Pac-Man");
    +stop.

// the pacman collected all the dots, inky surrenders
+looser[source(pacman)] : true <-
    .fail_goal(approach_target).