# csc 480 Spring 2018
# edited by Omar Chughtai


# this code creates a training set in the form of an ARFF
# file.  It can be used by any of ML algorithms from the
# Weka repository to be trained to identify the 
# author of a paragraph of text.  My training set is
# created from the text of a total of 8 novels.  I chose
# 2 novels written by each of 4 authors:  Jane Austen,
# H.G. Wells, Charles Dickens, and Mark Twain.  For
# convenience in writing the code below, I placed the
# text from both books by each author into 1 text file
# (for example, the file dickens.txt contains the text
# from "A Tale of Two Cities" and "A Christmas Carol").
# Single paragraphs from the 8 books are to be
# used as the training set

import math
import os
import random
from collections import Counter

dictionary = dict() # keys are words, values are counts
authors    = dict() # keys are file names, values are author objects
stopwords = open('stopwords.txt', 'r').read().split()

# an author object stores the name of the input file
# (e..g, 'dickens.txt'), and a list of paragraphs appearing
# in the txt file.  Each paragraph is tokenized into a list
# of the words it contains, in case you want to use this
# information.
class author:
    def __init__(self, title):
        self.title = title
        # you may have to change the path used, depending on
        # where you store the txt files on your own computer
        self.contents = open('literature\\' + title).read()
        # strip the text of punctuation
        tr = str.maketrans(':;.,?!-\'"', '         ')
        self.contents = self.contents.translate(tr)
        self.paras = [ ]
        # split the text into paragaphs
        part = str(self.contents).split('\n\n')
        # this loop examines one word at a time and
        # updates the entry for that word in the global
        # variable "dictionary".  Keys in that dictionary
        # are words, and values are counts for the word
        # (i.e., how many times the word appears in the
        # entire corpus)

        #Extract 7 paragraphs to put into paras list instead of one paragraph
        count = 0
        paras = [];
        for p in part:
            
            # split a paragraph into words
            p = p.split()
            # ignore paragraphs that are not more than 10 words long
            if len(p) > 10:
                for word in p:
                    self.add_to_vocab(word)
                paras.append(p)
                count += 1
                #if count is 7, means two paragraphs found. add both
                if count == 7:
                    #flatten lists of lists into just list.
                    flattenedParas = [y for x in paras for y in x]
                    self.paras.append(flattenedParas)
                    #reset count and paras to start over again
                    paras = []
                    count = 0

    # if a word is already in the dictionary, then increment
    # its count.  If this is the first time a word has
    # been encountered, then set the count to 1.
    def add_to_vocab(self, word):
        global dictionary
        # exclude stop words, words that are less than 5 characters,
        # and capitalized words (they might be proper names)
        if word in stopwords or len(word) < 5 or word[0].isupper():
            return
        if word in dictionary:
            dictionary[word] += 1
        else:
            dictionary[word] = 1

# pick the most frequently occurring words, as specified
# by the "size" parameter
def pick_vocab(size):
    global dictionary
    print('size of dictionary is ' + str(len(dictionary)))
    # I'm making a list of words, ordered by their
    # counts.  Keeping a separate list for the counts
    words = list()
    counts = list()
    # the alternative would be to sort the entire dictionary,
    # but I think this is more efficient
    for word in dictionary:
        count = dictionary[word]
        if len(words) != len(counts): # this should not happen
            raise Exception
        if len(words) == 0:
            words.append(word)
            counts.append(count)
        else:
            for i in range(len(words)):
                if count > counts[i]:
                    counts.insert(i, count)
                    words.insert(i, word)
                    break
                
            if word not in words:
                counts.append(count)
                words.append(word)
            
        # only keep the number of words specified by "size"
        if len(words) > size:
            words = words[:-1]
            counts = counts[:-1]
    for i in range(len(words)):
        print('{} {}'.format(words[i], counts[i]))
    return words       
    
for file in os.listdir('literature\\'):
    authors[file] = author(file)

#change from 500 to 700
keywords = pick_vocab(400)

def make_arff():
    f = open(r'C:\Users\ochug\Desktop\CSC 480\hw6\books.arff', 'w')
    f.write('@relation bookauthor\n\n')
    print(len(keywords))
    for word in keywords:
        f.write('@attribute {}'.format(word) + ' integer\n')
    f.write('@attribute bookauthor {austen,dickens,twain,wells}\n')
    auths = [ ]
    for author in authors:
        print(author[:author.find('.')])
        auths.append(author[:author.find('.')])
    print(auths)
    i=0
    f.write('\n@data\n')
    for file in authors:
        author = authors[file]
        for p in author.paras:
            # if the arff file gets to big, or if you run
            # out of heap space while running Weka
            # you can uncomment the code below in order
            # to take a sampling of paragraphs, rather
            # than include them all in the training set.
            #if random.random() < .75 :
            #    continue

            #Create counter object on each of the seven paragraphs.
            c = Counter(p)
            line = ''
            # create the vector for a paragraph, used frequency count as value for each word entry
            for word in keywords:
                line += str(c[word]) + ','
            line += auths[i]
            f.write(line + '\n')
        i += 1
    f.close()

make_arff()
