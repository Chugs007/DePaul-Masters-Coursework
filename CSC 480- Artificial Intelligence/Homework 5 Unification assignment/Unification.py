

def unify(x,y,s):
    """Unification algorithm, checks to see if there exists a substitution
    that allows the terms x and y to be identical.  Returns none if a substitution
    can't be found"""
    if s is None:
        return None
    elif x==y:
        return s
    elif isVariable(x):
        return unify_var(x,y,s)
    elif isVariable(y):
      return unify_var(y,x,s)
    elif type(x) == list and type(y) == list:
        return unify(x[1:],y[1:], unify(x[0],y[0],s))
    else:
        return None

def isVariable(x):
    """Checks if term is a variable by examining to see if ? is at beginning
    of terms, returns true if so otherwise false"""
    #return x[0:1] == "?"
    return "?" in x

def unify_var(var,x,s):
    """Attempts to unify the two terms.  Checks if either x or var are lists, and converts to tuple if so.
    Checks to see if var or x are in the substitution object, if so calls unify with corresponding value for var or x.
    If not in substitution object, calls occur_check to see if var occurs anywhere in x.  if it doesn't then adds var to the subsitution object with x being its value.
    """
    if (type(var) == list):
        var = tuple(var)
    elif (type(x) == list):
        x = tuple(x)
    if var in s:
        return unify(s[var],x,s)
    elif x in s:
        return unify(var,s[x],s)
    elif occur_check(var,x,s):
        return None
    else:
        return addVar(s,var,x)
    
def occur_check(var, x, s):
    """Checks to see if var occurs in x, first checks if both are equal.
    Then checks if x is variable and is in the substitution object, and recursively
    calls again to check.  If x is a list, then checks if some value in list is equal to
    var. Returns true if any of the above scenarios matches, otherwise returns false."""
    if var == x:
        return True
    elif isVariable(x) and x in s:
        return occur_check(var, s[x], s)
    elif isinstance(x,list):
        return some(lambda element: occur_check(var, element, s), x)
    else:
        return False
    

def addVar(s, var, val):
    """Copies the current substitution object, and then adds val
    to newly copied object with var used as the key
    """
    s2 = s.copy()
    s2[var] = val
    for k,v in s2.items():
        if v == var:
            s2[k] = val 
    return s2

def dictToString(dict):
    result = str(dict)
    return result.replace(":","/")

def printResult(result):
    print(False) if result == None else print(dictToString(result))
    
ex1 = unify(['human','?x'],['human','?y'],{})
ex2 = unify(['likes','?x','?y'],['likes','pat0','chris2'],{})
ex3 = unify(['likes','?x','?x'],['likes','pat0','chris2'],{})
ex4 = unify(['likes','?x','?x'],['likes','?y','pat0'],{})
ex5 = unify(['likes','pat0','?x'],['likes','?y','pat0'],{})
ex6= unify(['likes','?x','?y'],['likes',['friend','pat0'],'pat0'],{})
ex7 = unify(['likes',['friend','?x'],'?x'],['likes',['friend','?y'],'?y'],{})
ex8 = unify(['suburb',['sk1','?x'],'?x'],['suburb','?y','Naperville'],{})
ex9 = unify(['suburb',['sk2','?x'],'?x'],['suburb',['sk3','?y'],'Naperville'],{})
ex10= unify(['suburb',['sk3,?x'],'?x'],['suburb','[sk3,?y]','Naperville'],{})
ex11 = unify(['married',['spouse-of','?f']],['married',['spouse-of','socrates']],{})
printResult(ex1)
printResult(ex2)
printResult(ex3)
printResult(ex4)
printResult(ex5)
printResult(ex6)
printResult(ex7)
printResult(ex8)
printResult(ex9)
printResult(ex10)
printResult(ex11)
