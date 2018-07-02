# CSC 380/480 Winter 2018
# A comparison of eight-puzzle using various forms of search.
# In this version, the evaluator for best-first is a count
# of the number of tiles that are in the correct position
# Built on a generalized state finder in search.py
#
# To run this, DO NOT load eight_puzzle_state.py -- instead, open
# search.py and press the F5 key to load it.
import copy
from random import shuffle    # to randomize the order in which successors are visited

class eight_puzzle_state:
    def __init__(self, tiles):
        self.tiles = copy.copy(tiles)

    def __str__(self):
        answer = ''
        for i in range(9):
            answer += '{} '.format(self.tiles[i])
            if (i+1)%3 == 0:
                answer += '\n'
        return answer
    
    def __repr__(self):
        return 'eight_puzzle_state({})'.format(self.tiles)

    def __eq__(self, other):
        return self.tiles == other.tiles

    def __hash__(self):
        return hash(self.tiles[0])

    def getArray(self):
        return self.tiles
    
    def successors(self):
        successor_states = [ ]
        neighbors = {0:[1,3],1:[0,2,4],2:[1,5],3:[0,4,6],4:[1,3,5,7],5:[2,4,8],6:[3,7],7:[4,6,8],8:[5,7]}
        zero_loc = self.tiles.index(' ')
        for loc in neighbors[zero_loc]:
            state = eight_puzzle_state(self.tiles)
            state.tiles[zero_loc] = state.tiles[loc]
            state.tiles[loc] = ' '
            successor_states.append(state)
        return successor_states

    # returns an int between 0 (no tiles in place) to 8 (all in place)
    def evaluation(self):
        wrong = 0
        for i in range(8):
            if self.tiles[i] != goal_state().tiles[i]:
                wrong += 1
        return wrong
        
    def __lt__(self, other):
        return self.evaluation() < other.evaluation()
    
def goal_state(ignore=None):
    return eight_puzzle_state(['1', '2', '3', '8', ' ', '4', '7', '6', '5'])
        

# from random import shuffle   random puzzle is too many moves from goal state
from random import randint

# make a start state which is n moves from goal state
def start_state(n=5):
    already_visited = [ goal_state() ]
    state = goal_state()
    # max number of moves from start state to goal state
    for i in range(n):
        successors = state.successors()
        for s in successors:
            if s in already_visited:
                successors.remove(s)
        shuffle(successors)
        state = successors[0]
        already_visited.append(state)
    return state

#calculates the manhatten distance between the given state and the goal state
#creates two dimensional array of each state, and then iterates through given state finding value at each index in 2d array.
#searches for that value in goal array and then performs the manhatten distance calculation to get result and append to distance value.
#distance value is returned which represents total distance of all tiles of given state compared to goal state
def manhattenDistanceHeuristic(s):
        distance = 0
        state = s.getArray()
        goalState = goal_state().getArray()   
        index =0
        goal = []
        currentState = []
        for i in range(0,3):
            columnG = []
            columnS = []
            for j in range (0,3):
                columnG.append(goalState[index])
                columnS.append(state[index])
                index += 1
            goal.append(columnG)
            currentState.append(columnS)              
        for i in range(0,3):
            for j in range(0,3):
                val = currentState[i][j]
                gIndex =  goalStateQueryItemIndex(goal,val)               
                gX = gIndex[0]                              
                gY = gIndex[1]               
                distance += abs(j - gY) + abs(i - gX)
        return distance

#uses the manhatten distance heuristic to sort list of states according to minimun distance.
#uses insertion sort algorithm, and reverses list after sorting is done.
def bestFirstEval(states):
     for i in range(1,len(states)):
        value = manhattenDistanceHeuristic(states[i])
        iState = states[i]
        j = i-1
        while j>=0 and value < manhattenDistanceHeuristic(states[j]):
            states[j+1] = states[j]
            j -= 1
        states[j+1] = iState
     states.reverse()

#queries the goal state 2d array for given search item representing a tile value.
def goalStateQueryItemIndex(arr, searchItem):
      #for i, e in enumerate(arr):
        #return i,e.index(searchItem)
    for i,j in enumerate(arr):
        for k,l in enumerate(j):
                if l == searchItem:
                    return i,k

def random_eight_puzzle_state():
    tiles = ['1', '2', '3', '4', '5', '6', '7', '8', ' ']
    shuffle(tiles)
    return eight_puzzle_state(tiles)
