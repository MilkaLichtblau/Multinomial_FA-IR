'''
Created on Jul 15, 2020

@author: meike
'''

import pandas as pd
import glob
import sklearn.metrics
from src.util import selectionUtilityLossPerGroup, orderingUtilityLossPerGroup

COMPAS_RANKINGS_DIR = "../results/COMPAS/rankings/"
EXPERIMENT_NAMES = ["race", "ageRace", "sexAge", "sexRace"]


def main():

    for experiment in EXPERIMENT_NAMES:
        allFairRankingFilenames = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_fair.csv")
        allUnfairRankingFilenames = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_unfair.csv")
        allRemainingFilenames = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_remaining.csv")
        # get all files with corresponding k
        for fairRankingFilename in allFairRankingFilenames:
            fairRanking = pd.read_csv(fairRankingFilename, header=0, skipinitialspace=True)

            # find corresponding colorblind ranking and remainings
            pString = fairRankingFilename.split(sep="_")[3]
            kString = fairRankingFilename.split(sep="_")[2]
            colorblindRanking = pd.read_csv([string for string in allUnfairRankingFilenames if ((pString in string) and (kString in string))][0],
                                            header=0,
                                            skipinitialspace=True)
            remainingRanking = pd.read_csv([string for string in allRemainingFilenames if ((pString in string) and (kString in string))][0],
                                           header=0,
                                           skipinitialspace=True)

            result = pd.DataFrame()
            print(fairRanking.columns)
            result["group"] = fairRanking['group'].unique()
            result = selectionUtilityLossPerGroup(remainingRanking, fairRanking, result)
            result = orderingUtilityLossPerGroup(colorblindRanking, fairRanking, result)
            result["ndcgLoss"] = 1 - sklearn.metrics.ndcg_score(colorblindRanking.head(len(fairRanking)), fairRanking)

    thetaZeroRanking = pd.read_csv(COMPAS_RANKINGS_DIR + "race/compas_race_CFA_theta=0.csv", header=0)
    thetaOneRanking = pd.read_csv(COMPAS_RANKINGS_DIR + "race/compas_race_CFA_theta=1.csv", header=0)


if __name__ == '__main__':
    main()
