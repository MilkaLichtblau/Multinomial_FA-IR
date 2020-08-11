'''
Created on Jul 15, 2020

@author: meike
'''

import pandas as pd
import glob
import sklearn.metrics
from src.util import selectionUtilityLossPerGroup, orderingUtilityLossPerGroup, ndcgLoss, ndcg_score

COMPAS_RANKINGS_DIR = "../results/COMPAS/rankings/"
EXPERIMENT_NAMES = ["race", "ageRace", "sexAge", "sexRace"]


def main():

    for experiment in EXPERIMENT_NAMES:
        allFairRankingFilenames = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_fair.csv")
        allUnfairRankingFilenames = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_unfair.csv")
        allRemainingFilenames = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_remaining.csv")
        CFAHalfFilename = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_theta=05.csv")
        CFAOneFilename = glob.glob(COMPAS_RANKINGS_DIR + experiment + "/" + "*_theta=1.csv")
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

            # load baseline rankings
            thetaHalfRanking = pd.read_csv(CFAHalfFilename, header=0)
            thetaOneRanking = pd.read_csv(CFAOneFilename, header=0)

            # eval for Multinomial FA*IR
            multi_fair_result = pd.DataFrame()
            multi_fair_result["group"] = fairRanking['group'].unique()
            multi_fair_result = selectionUtilityLossPerGroup(remainingRanking, fairRanking, multi_fair_result)
            multi_fair_result = orderingUtilityLossPerGroup(colorblindRanking, fairRanking, multi_fair_result)
            multi_fair_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                fairRanking["score"].to_numpy(),
                                                k=len(fairRanking))
            multi_fair_result["exposureGain"] = 0.0

            # eval for CFA algorithm with theta=0.5
            kay = len(fairRanking)
            n = len(thetaHalfRanking) - kay
            cfaHalf_result = pd.DataFrame()
            cfaHalf_result["group"] = fairRanking['group'].unique()
            cfaHalf_result = selectionUtilityLossPerGroup(thetaHalfRanking.tail(n), thetaHalfRanking.head(kay), cfaHalf_result)
            cfaHalf_result = orderingUtilityLossPerGroup(colorblindRanking, thetaHalfRanking.head(kay), cfaHalf_result)
            cfaHalf_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                thetaHalfRanking.head(kay)["score"].to_numpy(),
                                                k=kay)
            cfaHalf_result["exposureGain"] = 0.0

            # eval for CFA algorithm with theta=1
            n = len(thetaOneRanking) - kay
            cfaOne_result = pd.DataFrame()
            cfaOne_result["group"] = fairRanking['group'].unique()
            cfaOne_result = selectionUtilityLossPerGroup(thetaOneRanking.tail(n), thetaOneRanking.head(kay), cfaOne_result)
            cfaOne_result = orderingUtilityLossPerGroup(colorblindRanking, thetaOneRanking.head(kay), cfaOne_result)
            cfaOne_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                thetaOneRanking.head(kay)["score"].to_numpy(),
                                                k=kay)
            cfaOne_result["exposureGain"] = 0.0


if __name__ == '__main__':
    main()
