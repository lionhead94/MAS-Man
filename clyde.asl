/* Initial beliefs */

/* Initial goal */

/* Plans */
+active : pacman_at(PacX,PacY) & mode(M) <-
    .print("Clyde is alive");
    navigation.decide_target(4, PacX, PacY, M, TX, TY);
    +target(TX,TY);
    //.print("Targeting ", TX, ",", TY); 
    !approach_target.

// clyde at crossroad decides where to go depending on pacman position
+!approach_target : at(X,Y) & prev_at(PrevX,PrevY) & crossroad(X,Y) & pacman_pos(PacX,PacY) & mode(M) <-
    check_pacman_killed(X,Y);
    navigation.decide_target(4, PacX, PacY, M, TX, TY);
    -+target(TX,TY);
    navigation.decide_crossroad(X, Y, PrevX, PrevY, TX, TY, ChoiceX, ChoiceY);
    .print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.
// clyde in corridor advances coherently with the past movement
+!approach_target : at(X,Y) & prev_at(PrevX,PrevY) & not crossroad(X,Y) & pacman_pos(PacX,PacY) & mode(M) <-
    check_pacman_killed(X,Y);
    navigation.decide_target(4, PacX, PacY, M, TX, TY);
    -+target(TX,TY);
    navigation.decide_step_on_path(4, X, Y, PrevX, PrevY, ChoiceX, ChoiceY);
    //.print("Moving to (", ChoiceX, ",", ChoiceY, ")");
    move_to(ChoiceX, ChoiceY);
    check_pacman_killed(ChoiceX,ChoiceY);
    !approach_target.

// stores a local copy of the new pacman pos to avoid problems during 
// the plan selection
+pacman_at(X,Y) <-
    -+pacman_pos(X,Y).

// clyde or another ghost killed the pacman, stops execution
+stop : true <-
    .succeed_goal(approach_target).

// clyde killed the pacman
+killer : true <-
    .print("I killed the Pac-Man");
    +stop.

// the pacman collected all the dots, clyde surrenders
+looser[source(pacman)] : true <-
    .fail_goal(approach_target).