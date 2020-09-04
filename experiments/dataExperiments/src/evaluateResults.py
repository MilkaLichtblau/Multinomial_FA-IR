'''
Created on Jul 15, 2020

@author: meike
'''

import pandas as pd
import glob
from src.util import selectionUtilityLossPerGroup, orderingUtilityLossPerGroup, ndcg_score, \
    averageGroupExposureGain


def main():
    # evaluate compas
    print("Evaluation for COMPAS experiments")
    evaluate("../results/COMPAS/rankings/", "../results/COMPAS/evalAndPlots/", ["race" , "age", "worstThree"])

    # evaluate German credit
    print("Evaluation for GermanCredit experiments")
    evaluate("../results/GermanCredit/rankings/", "../results/GermanCredit/evalAndPlots/", [""])

    # evaluate LSAT
    print("Evaluation for LSAT experiments")
    evaluate("../results/LSAT/rankings/", "../results/LSAT/evalAndPlots/", [""])


def evaluate(rankingsDir, evalDir, experimentNames):
    for experiment in experimentNames:
        print(experiment)
        allFairRankingFilenames = glob.glob(rankingsDir + experiment + "/" + "*_fair.csv")
        allUnfairRankingFilenames = glob.glob(rankingsDir + experiment + "/" + "*_unfair.csv")
        allRemainingFilenames = glob.glob(rankingsDir + experiment + "/" + "*_remaining.csv")
        CFAHalfFilename = glob.glob(rankingsDir + experiment + "/" + "*_theta=05.csv")[0]
        CFAOneFilename = glob.glob(rankingsDir + experiment + "/" + "*_theta=1.csv")[0]
        # get all files with corresponding k
        for fairRankingFilename in allFairRankingFilenames:
            fairRanking = pd.read_csv(fairRankingFilename, header=0, skipinitialspace=True)

            # find corresponding colorblind ranking and remainings
            pString = fairRankingFilename.split(sep="_")[3]
            kString = fairRankingFilename.split(sep="_")[2]
            print(kString, pString)
            colorblindRanking = pd.read_csv([string for string in allUnfairRankingFilenames if ((pString in string) and (kString in string))][0],
                                            header=0,
                                            skipinitialspace=True)
            remainingRanking = pd.read_csv([string for string in allRemainingFilenames if ((pString in string) and (kString in string))][0],
                                           header=0,
                                           skipinitialspace=True)

            # load baseline rankings
            thetaHalfRanking = pd.read_csv(CFAHalfFilename, header=0)
            thetaOneRanking = pd.read_csv(CFAOneFilename, header=0)

            thetaHalfSorted = thetaHalfRanking.sort_values(by=['fairScore', 'uuid'], ascending=[False, True])
            thetaHalfSorted = thetaHalfSorted.reset_index(drop=True)

            thetaOneSorted = thetaOneRanking.sort_values(by=['fairScore', 'uuid'], ascending=[False, True])
            thetaOneSorted = thetaOneSorted.reset_index(drop=True)

            # eval for Multinomial FA*IR
            multi_fair_result = pd.DataFrame()
            multi_fair_result["group"] = fairRanking['group'].unique()
            multi_fair_result = selectionUtilityLossPerGroup(remainingRanking, fairRanking, multi_fair_result)
            multi_fair_result = orderingUtilityLossPerGroup(colorblindRanking, fairRanking, multi_fair_result)
            multi_fair_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                fairRanking["score"].to_numpy(),
                                                k=len(fairRanking))
            multi_fair_result = averageGroupExposureGain(colorblindRanking, fairRanking, multi_fair_result)
            multi_fair_result.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_multiFairResult.csv")

            # eval for CFA algorithm with theta=0.5
            kay = len(fairRanking)
            n = len(thetaHalfSorted) - kay
            cfaHalf_result = pd.DataFrame()
            cfaHalf_result["group"] = fairRanking['group'].unique()
            cfaHalf_result = selectionUtilityLossPerGroup(thetaHalfSorted.tail(n), thetaHalfSorted.head(kay), cfaHalf_result)
            cfaHalf_result = orderingUtilityLossPerGroup(thetaHalfSorted.head(kay), colorblindRanking, cfaHalf_result)
            cfaHalf_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                thetaHalfSorted.head(kay)["score"].to_numpy(),
                                                k=kay)
            cfaHalf_result = averageGroupExposureGain(colorblindRanking, fairRanking, cfaHalf_result)
            cfaHalf_result.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_cfaHalfResult.csv")

            # eval for CFA algorithm with theta=1
            n = len(thetaOneSorted) - kay
            cfaOne_result = pd.DataFrame()
            cfaOne_result["group"] = fairRanking['group'].unique()
            cfaOne_result = selectionUtilityLossPerGroup(thetaOneSorted.tail(n), thetaOneSorted.head(kay), cfaOne_result)
            cfaOne_result = orderingUtilityLossPerGroup(thetaOneSorted.head(kay), colorblindRanking, cfaOne_result)
            cfaOne_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                thetaOneSorted.head(kay)["score"].to_numpy(),
                                                k=kay)
            cfaOne_result = averageGroupExposureGain(colorblindRanking, fairRanking, cfaOne_result)
            cfaOne_result.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_cfaOneResult.csv")


if __name__ == '__main__':
    main()
