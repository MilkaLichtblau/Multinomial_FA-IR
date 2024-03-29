'''
Created on Mar 16, 2021

@author: meike
'''
import sys, os
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
    if not os.path.exists(outputFilename):
        os.makedirs(outputFilename)

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

    # save remaining candidates for later evaluation in colorblind ranking
    remainings = pd.concat(groupArrays.values(), ignore_index=True)
    remainings = remainings.sort_values(by=['score', 'uuid'], ascending=[False, True])
    fairRanking.to_csv(outputFilename[:-4] + "_fair_" + str(i) + outputFilename[-4:], header=True, index=False)
    remainings.to_csv(outputFilename[:-4] + "_remaining_" + str(i) + outputFilename[-4:], header=True, index=False)


if __name__ == '__main__':
    # repeat each experiment 10000 and average results later
    for i in range(10000):
        print("Current run: " + str(i))
        main()
