'''
Created on Mar 16, 2021

@author: meike
'''
import sys
import numpy as np
import pandas as pd
import random


def parseMinprops(minpropString):
    minprops = np.array(minpropString.split(","))
    floatMinProps = [float(i) for i in minprops]
    return floatMinProps


def rollDice(massDist):
    randRoll = random.random()  # in [0,1]
    summ = 0
    result = 0
    for mass in massDist:
        summ += mass
        if randRoll < summ:
            return result
        result += 1


def main():
    origRanking = pd.read_csv(sys.argv[1], header=0, skipinitialspace=True)
    minProps = parseMinprops(sys.argv[2])
    k = int(sys.argv[3])
    outputFilename = sys.argv[4]

    fairRanking = pd.DataFrame(columns=["score", "group", "uuid"])

    # separate groups into dict of group arrays
    groupArrays = {}
    for groupName, _ in enumerate(minProps):
        groupArrays[groupName] = origRanking.loc[origRanking['group'] == groupName]

    for _ in range(k):
        groupToPut = rollDice(minProps)
        # get best candidate from group and pop candidate
        candidate = groupArrays.get(groupToPut).head(n=1)
        groupArrays[groupToPut] = groupArrays.get(groupToPut)[1:]
        fairRanking = fairRanking.append(candidate)

    fairRanking.to_csv(outputFilename)


if __name__ == '__main__':
    main()
