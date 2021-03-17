'''
Created on Jul 15, 2020

@author: meike
'''

import pandas as pd
import glob
# from sklearn.metrics import ndcg_score
from src.util import *
import sklearn.metrics
import scipy.stats


def main():
    # evaluate compas
    print("Evaluation for COMPAS experiments")
    evaluate("../results/COMPAS/rankings/", "../results/COMPAS/evalAndPlots/", ["age", "race" , "worstThree"])

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
        CFAOneFilename = glob.glob(rankingsDir + experiment + "/" + "*_theta=1.csv")[0]
        allDiceRollFilenames = glob.glob(rankingsDir + experiment + "/" + "*_dice_*")[0]

        # get all files with corresponding k
        for fairRankingFilename in allFairRankingFilenames:
            fairRanking = pd.read_csv(fairRankingFilename, header=0, skipinitialspace=True)

            # find corresponding colorblind ranking and remaining rankings that have not been
            # included into the fair ranking
            pString = fairRankingFilename.split(sep="_")[3]
            kString = fairRankingFilename.split(sep="_")[2]
            print("\n", kString, pString)
            colorblindRanking = pd.read_csv([string for string in allUnfairRankingFilenames if ((pString in string) and (kString in string))][0],
                                             header=0,
                                             skipinitialspace=True)
            remainingRanking = pd.read_csv([string for string in allRemainingFilenames if ((pString in string) and (kString in string))][0],
                                            header=0,
                                            skipinitialspace=True)

            # load baseline rankings
            diceRollRanking = pd.read_csv([string for string in allDiceRollFilenames if ((pString in string) and (kString in string))][0],
                                           header=0,
                                           skipinitialspace=True)

            thetaOneRanking = pd.read_csv(CFAOneFilename, header=0)
            thetaOneSorted = thetaOneRanking.sort_values(by=['fairScore', 'uuid'], ascending=[False, True])
            thetaOneSorted = thetaOneSorted.reset_index(drop=True)

            #####################################################
            # eval for Multinomial FA*IR
            #####################################################
            print("\nFA*IR Eval")
            kay = len(fairRanking)
            multi_fair_result = pd.DataFrame()
            multi_fair_result["group"] = fairRanking['group'].unique()

            # individual fairness metrics
            multi_fair_result = selectionUtilityLossPerGroup(remainingRanking, fairRanking, multi_fair_result)
            multi_fair_result = orderingUtilityLossPerGroup(colorblindRanking, fairRanking, multi_fair_result)

            # performance metrics
            multi_fair_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                           fairRanking["score"].to_numpy(),
                                                           k=kay)
            multi_fair_result["kendallTauLoss"] = 1 - scipy.stats.kendalltau(colorblindRanking.head(kay)["score"].to_numpy(),
                                                                             fairRanking["score"].to_numpy())[0]

            if "compas" in fairRankingFilename:
                # calculate AUC loss for compas experiments
                inputData = pd.read_csv("", header=0, skipinitialspace=True)
                multi_fair_result["aucLoss"] = aucLossWRTColorblind(colorblindRanking["score"].to_numpy(),
                                                                    fairRanking["score"].to_numpy(),
                                                                    k=kay,
                                                                    inputData)

            # group fairness metrics
            multi_fair_result = averageGroupExposureGain(colorblindRanking.head(kay), fairRanking, multi_fair_result)
            multi_fair_result = multi_fair_result.sort_values(by=['group'])
            multi_fair_result.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_multiFairResult.csv")

            #####################################################
            # eval for CFA algorithm with theta=1
            #####################################################
            print("\nCFA 1.0 eval")
            tailLength = len(thetaOneSorted) - kay
            cfaOne_result = pd.DataFrame()
            cfaOne_result["group"] = fairRanking['group'].unique()

            # individual fairness metrics
            cfaOne_result = selectionUtilityLossPerGroup(thetaOneSorted.tail(tailLength), thetaOneSorted.head(kay), cfaOne_result)
            cfaOne_result = orderingUtilityLossPerGroup(colorblindRanking, thetaOneSorted.head(kay), cfaOne_result)

            # performance metrics
            cfaOne_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                       thetaOneSorted.head(kay)["score"].to_numpy(),
                                                       k=kay)
            cfaOne_result["kendallTauLoss"] = 1 - scipy.stats.kendalltau(colorblindRanking.head(kay)["score"].to_numpy(),
                                                                         thetaOneSorted.head(kay)["score"].to_numpy())[0]

            # group fairness metrics
            cfaOne_result = averageGroupExposureGain(colorblindRanking.head(kay), thetaOneSorted.head(kay), cfaOne_result)
            cfaOne_result = cfaOne_result.sort_values(by=['group'])
            cfaOne_result.to_csv(evalDir + experiment + "/" + kString + "_cfaOneResult.csv")

            #####################################################
            # eval for dice roll algorithm
            #####################################################
            print("\ndice roll eval")
            diceRoll_result = pd.DataFrame()
            diceRoll_result["group"] = fairRanking['group'].unique()

            # individual fairness metrics
            diceRoll_result = selectionUtilityLossPerGroup(thetaOneSorted.tail(tailLength), thetaOneSorted.head(kay), diceRoll_result)
            diceRoll_result = orderingUtilityLossPerGroup(colorblindRanking, thetaOneSorted.head(kay), diceRoll_result)

            # performance metrics
            diceRoll_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                       thetaOneSorted.head(kay)["score"].to_numpy(),
                                                       k=kay)
            diceRoll_result["kendallTauLoss"] = 1 - scipy.stats.kendalltau(colorblindRanking.head(kay)["score"].to_numpy(),
                                                                         thetaOneSorted.head(kay)["score"].to_numpy())[0]

            # group fairness metrics
            diceRoll_result = averageGroupExposureGain(colorblindRanking.head(kay), thetaOneSorted.head(kay), diceRoll_result)
            diceRoll_result = diceRoll_result.sort_values(by=['group'])
            diceRoll_result.to_csv(evalDir + experiment + "/" + kString + "_diceRollResult.csv")


if __name__ == '__main__':
    main()
