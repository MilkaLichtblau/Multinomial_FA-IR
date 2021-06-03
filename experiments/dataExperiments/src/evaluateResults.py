'''
Created on Jul 15, 2020

@author: meike
'''

import pandas as pd
import glob, os
# from sklearn.metrics import ndcg_score
from src.util import *
import sklearn.metrics
import scipy.stats


def main():
    # evaluate compas
    print("Evaluation for COMPAS experiments")
    evaluateDiceRollAverage("../results/COMPAS/rankings/", "../results/COMPAS/evalAndPlots/", ["age", "race" , "worstThree"], numExps=5)
    evaluate("../results/COMPAS/rankings/", "../results/COMPAS/evalAndPlots/", ["age", "race" , "worstThree"])

    # evaluate German credit
    print("Evaluation for GermanCredit experiments")
    evaluate("../results/GermanCredit/rankings/", "../results/GermanCredit/evalAndPlots/", [""])
    evaluateDiceRollAverage("../results/GermanCredit/rankings/", "../results/GermanCredit/evalAndPlots/", [""])

    # evaluate LSAT
    print("Evaluation for LSAT experiments")
    evaluate("../results/LSAT/rankings/", "../results/LSAT/evalAndPlots/", [""])
    evaluateDiceRollAverage("../results/LSAT/rankings/", "../results/LSAT/evalAndPlots/", [""])


def evaluate(rankingsDir, evalDir, experimentNames):
    for experiment in experimentNames:
        print("\nEXPERIMENT: " + experiment)
        allFairRankingFilenames = glob.glob(rankingsDir + experiment + "/" + "*alpha*" + "*_fair.csv")
        allUnfairRankingFilenames = glob.glob(rankingsDir + experiment + "/" + "*_unfair.csv")
        allRemainingFilenames = glob.glob(rankingsDir + experiment + "/" + "*alpha*" + "*_remaining.csv")
        CFAOneFilename = glob.glob(rankingsDir + experiment + "/" + "*_theta=1.csv")[0]
        allDiceRollFairFilenames = glob.glob(rankingsDir + experiment + "/" + "*_dice_" + "*_fair.csv")
        allDiceRollRemainingFilenames = glob.glob(rankingsDir + experiment + "/" + "*_dice_" + "*_remaining.csv")

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
            multi_fair_result["kendallTau"] = scipy.stats.kendalltau(colorblindRanking.head(kay)["score"].to_numpy(),
                                                                             fairRanking["score"].to_numpy())[0]

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
            cfaOne_result["kendallTau"] = scipy.stats.kendalltau(colorblindRanking.head(kay)["score"].to_numpy(),
                                                                         thetaOneSorted.head(kay)["score"].to_numpy())[0]

            # group fairness metrics
            cfaOne_result = averageGroupExposureGain(colorblindRanking.head(kay), thetaOneSorted.head(kay), cfaOne_result)
            cfaOne_result = cfaOne_result.sort_values(by=['group'])
            cfaOne_result.to_csv(evalDir + experiment + "/" + kString + "_cfaOneResult.csv")

            #####################################################
            # eval for dice roll algorithm single trial
            #####################################################

            # do this evaluation only if dice roll experiments exists for given k
            try:
                diceRollFairRanking = pd.read_csv([string for string in allDiceRollFairFilenames if ((pString in string) and (kString in string))][0],
                                               header=0,
                                               skipinitialspace=True)
                diceRollRemainingRanking = pd.read_csv([string for string in allDiceRollRemainingFilenames if ((pString in string) and (kString in string))][0],
                                               header=0,
                                               skipinitialspace=True)
            except:
                # no dice roll experiment results found for current k and p, so we don't want to evaluate
                continue

            print("\ndice roll eval")
            diceRoll_result = pd.DataFrame()
            diceRoll_result["group"] = fairRanking['group'].unique()

            # individual fairness metrics
            diceRoll_result = selectionUtilityLossPerGroup(diceRollRemainingRanking, diceRollFairRanking, diceRoll_result)
            diceRoll_result = orderingUtilityLossPerGroup(colorblindRanking, diceRollFairRanking, diceRoll_result)

            # performance metrics
            diceRoll_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                         diceRollFairRanking["score"].to_numpy(),
                                                         k=kay)
            diceRoll_result["kendallTau"] = scipy.stats.kendalltau(colorblindRanking.head(kay)["score"].to_numpy(),
                                                                         diceRollFairRanking.head(kay)["score"].to_numpy())[0]

            # group fairness metrics
            diceRoll_result = averageGroupExposureGain(colorblindRanking.head(kay), diceRollFairRanking.head(kay), diceRoll_result)
            diceRoll_result = diceRoll_result.sort_values(by=['group'])
            diceRoll_result.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_diceRollResult.csv")


def evaluateDiceRollAverage(rankingsDir, evalDir, experimentNames, numExps=10000):
    # eval for dice roll algorithm
    for experiment in experimentNames:
        print("\nEXPERIMENT: " + experiment)
        allUnfairRankingFilenames = glob.glob(rankingsDir + experiment + "/" + "*_unfair.csv")
        allDiceRollFairFilenames = glob.glob(rankingsDir + experiment + "/diceroll/" + "*_fair_*.csv")
        allDiceRollRemainingFilenames = glob.glob(rankingsDir + experiment + "/diceroll/" + "*_remaining_*.csv")

        diceRoll_result_total = pd.DataFrame()
        # get all files with corresponding k, p and i
        for fairDiceRollFilename in allDiceRollFairFilenames:

            # find corresponding colorblind ranking and remaining rankings that have not been
            # included into the fair ranking
            pString = fairDiceRollFilename.split(sep="_")[4]
            kString = fairDiceRollFilename.split(sep="_")[3]
            globPathName = rankingsDir + experiment + "/diceroll/*" + kString + "_" + glob.escape(pString) + "*_fair_*.csv"
            allFairDiceRankingsOfSameExperimentFilenames = glob.glob(globPathName)

            for fairDiceRankingFileName in allFairDiceRankingsOfSameExperimentFilenames:
                fairDiceRanking = pd.read_csv(fairDiceRankingFileName, header=0, skipinitialspace=True)
                iString = fairDiceRankingFileName.split(sep="_")[6]

                print("\n", kString, pString, iString)
                colorblindRanking = pd.read_csv([string for string in allUnfairRankingFilenames
                                                        if ((pString in string) and (kString in string))][0],
                                                 header=0,
                                                 skipinitialspace=True)

                diceRollRemainingRanking = pd.read_csv([string for string in allDiceRollRemainingFilenames
                                                               if ((pString in string) and (kString in string)
                                                                   and (iString in string))][0],
                                                       header=0,
                                                       skipinitialspace=True)

                diceRoll_result = pd.DataFrame()
                diceRoll_result["group"] = colorblindRanking['group'].unique()

                # individual fairness metrics
                diceRoll_result = selectionUtilityLossPerGroup(diceRollRemainingRanking, fairDiceRanking, diceRoll_result)
                diceRoll_result = orderingUtilityLossPerGroup(colorblindRanking, fairDiceRanking, diceRoll_result)

                # performance metrics
                kay = len(fairDiceRanking)
                diceRoll_result["ndcgLoss"] = 1 - ndcg_score(colorblindRanking["score"].to_numpy(),
                                                             fairDiceRanking["score"].to_numpy(),
                                                             k=kay)
                diceRoll_result["kendallTau"] = scipy.stats.kendalltau(colorblindRanking.head(kay)["score"].to_numpy(),
                                                                       fairDiceRanking.head(kay)["score"].to_numpy())[0]

                # group fairness metrics
                diceRoll_result = averageGroupExposureGain(colorblindRanking.head(kay), fairDiceRanking.head(kay), diceRoll_result)
                diceRoll_result = diceRoll_result.sort_values(by=['group'])

                if diceRoll_result_total.empty:
                    diceRoll_result_total = diceRoll_result
                else:
                    diceRoll_result_total += diceRoll_result
            # calculate average
            diceRoll_result_total = diceRoll_result_total / numExps
            diceRoll_result_total.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_diceRollResultAveraged.csv")

            # remove all files from todo list that have already been processed
            allDiceRollFairFilenames = [item for item in allDiceRollFairFilenames if item not in allFairDiceRankingsOfSameExperimentFilenames]


if __name__ == '__main__':
    main()
