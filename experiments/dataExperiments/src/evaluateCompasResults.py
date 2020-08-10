'''
Created on Jul 15, 2020

@author: meike
'''

import pandas as pd
import re, glob
import util

COMPAS_RANKINGS_DIR = "../results/COMPAS/rankings/"


def main():
    experimentNames = ["race", "ageRace", "sexAge", "sexRace"]

    for experiment in experimentNames:
        allFairRankingFilenames = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_fair.csv")
        for fairRankingFilename in allFairRankingFilenames:
            fairRanking = pd.read_csv(fairRankingFilename, header=0)
            # find corresponding colorblind ranking
            k, pString = getKAndPFromFilename(fairRankingFilename)
            unfairRankingFilename = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/*" + pString + "*_unfair.csv")
            colorblindRanking = pd.read_csv(unfairRankingFilename, header=0)

            # split colorblind into those who are in the ranking (top-k) and those who are not
            colorblindIncluded = colorblindRanking.head(k)
            colorblindExcluded = colorblindRanking.tail(len(colorblindRanking) - k)

    thetaZeroRanking = pd.read_csv(COMPAS_RANKINGS_DIR + "race/compas_race_CFA_theta=0.csv", header=0)
    thetaOneRanking = pd.read_csv(COMPAS_RANKINGS_DIR + "race/compas_race_CFA_theta=1.csv", header=0)


def getKAndPFromFilename(filename):
    splitted = filename.split(sep="_")
    kString = splitted[2]
    pString = splitted[3]
    return int(re.search(r'\d+', kString).group()), pString


if __name__ == '__main__':
    main()
