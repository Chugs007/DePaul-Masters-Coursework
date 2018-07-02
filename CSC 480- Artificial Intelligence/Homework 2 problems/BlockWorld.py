import sys

#class represents state of the block world program
#blockOrder is a list containing the current blocks and what positions they are in
class State:
  
    def __init__(self,blockOrder):       
        self.blockOrder = blockOrder
        self.heuristicValue = 0
        
    #gets all the successors of a given state. uses a dictionary containing keys representing a given state
    #the value of each key is a list of lists containing the different successor states of the key         
    def successors(self):
        successor_states = [ ]
        dictionary = {
            ('rb','o','') : [['ro','b',''],['b','o','r'],['orb','','']],
            ('br','o','') : [['obr','',''],['bo','r',''],['b','o','r']],
            ('ob','r','') : [['or','b',''],['b','o','r'],['rob','','']],
            ('bo','r','') : [['b','o','r'],['rbo','',''],['br','o','']],
            ('ro','b','') : [['b','o','r'],['bro','',''],['rb','o','']],
            ('or','b','') : [['b','o','r'],['bor','',''],['ob','r','']],
            ('rob','','') : [['ob','r','']],
            ('rbo','','') : [['bo','r','']],
            ('bro','','') : [['ro','b','']],
            ('bor','','') : [['or','b','']],
            ('obr','','') : [['br','o','']],
            ('orb','','') : [['rb','o','']],
            ('b','o','r') : [['rb','o',''],['ro','b',''],['ob','r',''],['or','b',''],['bo','r',''],['br','o','']]
        }
        blockTuple = tuple(self.blockOrder)
        result = dictionary.get(blockTuple,[''])
        for r in result:
            state = State(r)
            successor_states.append(state)       
        return successor_states

    def getBlockOrder(self):
        return self.blockOrder

#class represents the blocks world program    
class BlocksWorld:

    #checks if given state is equal to the goal state
    def isGoalState(self,currentState,goalState):
        if goalState.getBlockOrder() == currentState.getBlockOrder():            
            return True
        else:        
            return False

    #uses simple heuristic of checking each block in the current state, and comparing its index in the list compared
    #to the goal state's list. if not equal, then increment count by one. returns the state with the minimum count.
    def bestFirstHeuristic(self,stack,goalState):    
        bestOption = None
        mininumValue = sys.maxsize
        for state in stack:
            count = 0
            curStateCharArray = self.stateToCharArray(state)          
            goalStateCharArray = self.stateToCharArray(goalState)
            if curStateCharArray.index('b') != goalStateCharArray.index('b'):
                count += 1
            if curStateCharArray.index('r') != goalStateCharArray.index('r'):
                count += 1
            if curStateCharArray.index('o') != goalStateCharArray.index('o'):
                count += 1
            if mininumValue > count:
                bestOption = state
                mininumValue = count             
        return bestOption

    #takes given state and return array representing the state with the blocks in corresponding positions
    def stateToCharArray(self,state):
        chars = []
        currentList = tuple(state.getBlockOrder())
        for line in currentList:
            if len(line) <=2:
                chars.append('')
            if len(line) <=1:
                chars.append('')
            if len(line) ==0:
                chars.append('')
            for c in line:
                
                chars.append(c)
            chars.append('')
        return chars

    #best first search uses list that is continously popped from based on best first heuristic
    #check if goal state is reached, otherwise gets list of successors for given state and repeats the process
    def bestFirstSearch(self,startState,goalState):
        stack = []
        seen = []
        steps = 0
        stack.append(startState)
        seen.append(startState.getBlockOrder())
        while len(stack) > 0:
            currentState = self.bestFirstHeuristic(stack,goalState)
            stack.remove(currentState)
            print(currentState.getBlockOrder())
            if self.isGoalState(currentState,goalState):
                return steps
           
            successors = currentState.successors()
            for successor in successors:
                s = successor.getBlockOrder()            
                if s not in seen:
                    stack.append(successor)
                    seen.append(s)
            steps += 1
        return -1

    #performs depth first seach of the block world program
    def dfs(self,startState,goalState):
        stack = []
        seen = []
        steps = 0
        stack.append(startState)
        seen.append(startState.getBlockOrder())
        while len(stack) > 0:
            currentState = stack.pop()
            print(currentState.getBlockOrder())
            if self.isGoalState(currentState,goalState):
                return steps
            steps += 1
            successors = currentState.successors()
            for successor in successors:
                s = successor.getBlockOrder()               
                if s not in seen:
                    stack.append(successor)
                    seen.append(s)

        return -1

    #performs breadth first search of the block world program
    def bfs(self,startState,goalState):
        stack = []
        seen = []
        steps = 0
        stack.append(startState)
        seen.append(startState.getBlockOrder())
        while len(stack) > 0:
            currentState = stack.pop(0)
            print(currentState.getBlockOrder())
            if self.isGoalState(currentState,goalState):
                return steps
            steps += 1
            successors = currentState.successors()
            for successor in successors:
                s = successor.getBlockOrder()
                if s not in seen:
                    stack.append(successor)
                    seen.append(s)

        return -1
                
#start state set initially to some value, can change
s = State(['bro','',''])
#end state set to some value, can change
endState = State(['rbo','',''])
#instantiates the block world class and performs the different search algorithms on the initial state
blocksWorld = BlocksWorld()
print('Blocks World Program')
print('')
print('Depth First Search')
dfsResult = blocksWorld.dfs(s,endState)
print('Steps taken: ' + str(dfsResult))
print('')
print('Breadth First Search')
bfsResult = blocksWorld.bfs(s,endState)
print('Steps taken: ' + str(bfsResult))
print('')
print('Best First Search')
bestFirstResult = blocksWorld.bestFirstSearch(s,endState)
print('Steps taken: ' + str(bestFirstResult))


