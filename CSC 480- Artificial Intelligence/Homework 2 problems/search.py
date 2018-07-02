from random import shuffle

# CSC 380/480 Fall 2018
# A comparison of different strategies for 8-puzzle game.

from eight_puzzle_state import *
# from pegs_state import *
#from jealous_state import *

# a general search function that is given a start state, a goal state,
# a strategy ('dfs', 'bfs, 'best') and a maximum number of states to
# visit.  Returns the number of states visited in order to find the goal
# state, using the given strategy

# best-first search expects the state objects to have a __lt__ method,
# which will determine how a list of states is sorted.
def search(start, goal, strategy, max_states, states_so_far=0):
    to_visit = [ start ]
    already_visited = set()
    while to_visit != [ ]:
 #       input('anything\n')
        state = to_visit.pop()
 #       print('current state\n{}'.format(state))
        if state == goal:
            return states_so_far           
        elif states_so_far >= max_states:
            return states_so_far
        elif state in already_visited:
            pass
        else:
            already_visited.add(state)
            new_states = state.successors()
            shuffle(new_states)
            if strategy == 'dfs':
                to_visit = to_visit + new_states
            elif strategy == 'bfs':
                to_visit = new_states + to_visit
            elif strategy == 'best':
                to_visit = new_states + to_visit
                bestFirstEval(to_visit)
            elif strategy == 'bestOld':
                to_visit = new_states + to_visit
                sorted(to_visit,reverse =True)
            states_so_far += 1
    if verbose:
        print('defeat')
    return states_so_far

# this compares search strategies.  It runs each
# strategy on start states 1 through n (some measure
# of difficulty of the problem).  Each is run the given
# number of trials.  A maximum number of states determines
# when the search should terminate (and fail)
def compare(strategies, max_states=10000, trials=10, easiest=1, hardest=10):
    for strat in strategies:
        print(',{}'.format(strat),end = '')
    for level in range(easiest, hardest+1):
        print('\n{}'.format(level),end="")
        for strat in strategies:
            total_states = 0
            for trial in range(trials):
                start = start_state(level)
                goal = goal_state(level)
                total_states += search(start, goal, strat, max_states, False)
            print(',{}'.format(total_states//trials), end='')

#compares all 4 strategies, best uses the manhatten distance heuristic while bestOld uses original heuristic given
compare(['dfs', 'bfs', 'best','bestOld'], 2000)

