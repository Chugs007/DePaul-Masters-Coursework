

#class represents state of jealous agents problem with agents and actors on left side and right side of river
#leftSide represents list of players on the left side on the river, rightSide represents list of players on the right side of the river
#boatSide is integer with 1 representing boat is on the left side, while 0 represents boat is on the right side
class jealous_state:

    #initialize values, set parent to none
    def __init__(self,leftSide,boatSide,rightSide):
        self.leftSide = leftSide
        self.boatSide = boatSide
        self.rightSide = rightSide
        self.parent  = None
        
    #returns list of agents and actors on left side of river
    def getLeftSide(self):
        return self.leftSide

    #returns integer representing what side the boat is on
    def getBoatSide(self):
        return self.boatSide

    #returns list of agents and actors on the right side
    def getRightSide(self):
        return self.rightSide

    #checks for equality by comparing both lists
    def __eq__(self,other):
        return self.leftSide == other.leftSide and self.rightSide == other.rightSide

    #uses tuple of leftSide and rightSide to hash into value
    def __hash__(self):
        return hash((tuple(self.leftSide),tuple(self.rightSide)))
    
    #prints the current state 
    def printState(self):
        print('State')
        print('left side: ' + str(self.leftSide))
        print('right side: ' + str(self.rightSide))
        print('boat is on left side: ' + str(bool(self.boatSide)))
        print('')

#checks if given state is valid
def isValidState(state):
    leftState = state.getLeftSide()
    rightState = state.getRightSide()
    if poachingEncountered(leftState) or poachingEncountered(rightState):
        return False
    return True

#checks if given list representing one side of the river is in a state of poaching
def poachingEncountered(stateList):
    
    indexA1 = stateList.index('a1') if 'a1' in stateList else -1
    indexA2 = stateList.index('a2') if 'a2' in stateList else -1
    indexA3 = stateList.index('a3') if 'a3' in stateList else -1

    if indexA1 != -1:
        if any('ag' in occ for occ in stateList):
            if 'ag1' not in stateList:
                return True
    if indexA2 != -1:
        if any('ag' in occ for occ in stateList):
            if 'ag2' not in stateList:
                return True
    if indexA3 != -1:
        if any('ag' in occ for occ in stateList):
            if 'ag3' not in stateList:
                return True
    return False

#gets all the successor of a given state. checks for current side by examining boatSide variable
#creates all possible states and checks if state is valid before attempting to add to the successors list
def getSuccessors(state):
    successorStates = []
    seen = []
    currentSide = state.getLeftSide() if state.getBoatSide() == 1 else state.getRightSide()
    otherSide = state.getLeftSide() if state.getBoatSide() == 0 else state.getRightSide()
    boatSide = 1 if state.getBoatSide() == 0 else 0
    for x in range(0,len(currentSide)):
        currentOccupants = currentSide.copy()
        otherSideOccupants = otherSide.copy()
        passenger = currentOccupants.pop(x)
        otherSideOccupants.append(passenger)
        sorted(otherSideOccupants)
        sorted(currentOccupants)
        newState = None
        if boatSide == 0:
            newState = jealous_state(currentOccupants,boatSide,otherSideOccupants)
        else:
            newState  = jealous_state(otherSideOccupants,boatSide,currentOccupants)
        if isValidState(newState):
            newState.parent = state            
            successorStates.append(newState)
        for y in range(0,len(currentSide)):
            if y == x:
                continue
            else:
                newState = None
                currentOccupants = currentSide.copy()
                otherSideOccupants = otherSide.copy()
                passengerA = currentOccupants[x]
                passengerB = currentOccupants[y]
                currentOccupants.remove(passengerA)
                currentOccupants.remove(passengerB)
                otherSideOccupants.append(passengerA)
                otherSideOccupants.append(passengerB)
                sorted(otherSideOccupants)
                sorted(currentOccupants)
                combined = currentOccupants + otherSideOccupants
                if boatSide == 0:
                    newState = jealous_state(currentOccupants,boatSide,otherSideOccupants)
                else:
                    newState  = jealous_state(otherSideOccupants,boatSide,currentOccupants)
                if isValidState(newState) and currentOccupants not in seen:
                    seen.append(currentOccupants)
                    newState.parent = state
                    successorStates.append(newState)
    return successorStates                

#performs depth first search using initial state. uses a list to continously pop last value from and
#check if goal state has been reached. returns state when goal is reached.
def dfs(initialState):
    stack = []
    seen = set()
    steps = 0
    stack.append(initialState)
    while len(stack) > 0:
        state = stack.pop()
        if goalStateReached(state):
             print('Solution found in ' + str(steps) + ' steps')
             return state;
        steps += 1
        seen.add(state)
        successors = getSuccessors(state)
        for successor in successors:
            if (successor not in seen):
                stack.append(successor)
        
#performs breadth first search using initial state. uses a list to continously pop the first element
#from the list.  check if goal state has been reached, returns state when goal is reached.                
def bfs(initialState):
    queue = []
    seen = set()
    queue.append(initialState)
    steps = 0
    while len(queue) > 0:
        state = queue.pop(0)
        if goalStateReached(state):
            print('Solution found in ' + str(steps) + ' steps')
            return state
            
        steps += 1
        seen.add(state)
        successors = getSuccessors(state)
        for successor in successors:
            if (successor not in seen): 
                queue.append(successor)

#checks if goal state is reached by examing both sides of given state. left side should be empty while right side
#should have all actors and agents(6)                
def goalStateReached(state):
    if len(state.getLeftSide()) == 0 and len(state.getRightSide()) == 6:
        return True
    else:
        return False
#prints the solution found by using the parent attribute of each state to iterate through
def printSolution(result):
    path = []
    path.append(result)
    while result.parent:
        path.append(result.parent)
        result = result.parent
    path.reverse()
    for p in path:
        p.printState()

#start state set here to state with all agents and actors on the left side
startState = jealous_state(['a1','a2','a3','ag1','ag2','ag3'],1,[])

#performs dfs and bfs search and prints the solution path for each
print('Jealous Agents Problem')
print('Path taken for search method')
print('DFS search')
result = dfs(startState)
printSolution(result)
print('')
print('***********************************')
print('')
print('BFS search')
result = bfs(startState)
printSolution(result)


