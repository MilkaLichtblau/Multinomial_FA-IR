'''
Created on Jul 15, 2020

@author: meike
'''

import pandas as pd
import glob, sys, os
# from sklearn.metrics import ndcg_score
from util import *
import scipy.stats
from multiprocessing import Pool
import functools


def evaluate(rankingsDir, evalDir, experiment):
    print("\nFAIR AND CFA EXPERIMENT: " + experiment)
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


def parallelDiceRollEvalFunc(fairDiceRankingFileName, allRemainingFilenames, pStr, kStr, colblindRank, resultPath):

    fairDiceRanking = pd.read_csv(fairDiceRankingFileName, header=0, skipinitialspace=True)
    iString = fairDiceRankingFileName.split(sep="_")[7]
    print("\n", kStr, pStr, iString)
    diceRollRemainingRanking = pd.read_csv(
        [filename for filename in allRemainingFilenames if ((pStr in filename) and (kStr in filename) and (iString in filename))][0],
        header=0,
        skipinitialspace=True)
    diceRoll_result = pd.DataFrame()
    diceRoll_result["group"] = colblindRank['group'].unique()
    # individual fairness metrics
    diceRoll_result = selectionUtilityLossPerGroup(diceRollRemainingRanking, fairDiceRanking, diceRoll_result)
    diceRoll_result = orderingUtilityLossPerGroup(colblindRank, fairDiceRanking, diceRoll_result)
    # performance metrics
    kay = len(fairDiceRanking)
    diceRoll_result["ndcgLoss"] = 1 - ndcg_score(colblindRank["score"].to_numpy(), fairDiceRanking["score"].to_numpy(),
        k=kay)
    diceRoll_result["kendallTau"] = scipy.stats.kendalltau(colblindRank.head(kay)["score"].to_numpy(), fairDiceRanking.head(kay)["score"].to_numpy())[0]
    # group fairness metrics
    diceRoll_result = averageGroupExposureGain(colblindRank.head(kay), fairDiceRanking.head(kay), diceRoll_result)
    diceRoll_result = diceRoll_result.sort_values(by=['group'])
    diceRoll_result.to_csv(resultPath + "diceRollResult_" + kStr + "_" + pStr + "_" + iString)


def evaluateDiceRollPerExperiment(rankingsDir, evalDir, experiment, numExps=10000):
    # eval for dice roll algorithm
    print("\nDICE ROLL AVERAGE EXPERIMENT EVALUATION: " + experiment)
    allUnfairRankingFilenames = glob.glob(rankingsDir + experiment + "/" + "*_unfair.csv")
    allDiceRollFairFilenames = glob.glob(rankingsDir + experiment + "/diceroll/" + "*_fair_*.csv")
    allDiceRollRemainingFilenames = glob.glob(rankingsDir + experiment + "/diceroll/" + "*_remaining_*.csv")

    # get all files with corresponding k, p and i
    while len(allDiceRollFairFilenames) != 0:
        fairDiceRollFilename = allDiceRollFairFilenames[0]

        pString = fairDiceRollFilename.split(sep="_")[5]
        kString = fairDiceRollFilename.split(sep="_")[4]
        # find corresponding colorblind ranking
        colorblindRanking = pd.read_csv([string for string in allUnfairRankingFilenames
                                                if ((pString in string) and (kString in string))][0],
                                         header=0,
                                         skipinitialspace=True)

        globPathName = rankingsDir + experiment + "/diceroll/*" + kString + "_" + glob.escape(pString) + "*_fair_*.csv"
        allFairDiceRankingsOfSameExperimentFilenames = glob.glob(globPathName)

        resultPatth = evalDir + experiment + "/diceroll/"
        if not os.path.exists(resultPatth):
            os.makedirs(resultPatth)

        parallelFunc = functools.partial(parallelDiceRollEvalFunc,
                                         allRemainingFilenames=allDiceRollRemainingFilenames,
                                         pStr=pString,
                                         kStr=kString,
                                         colblindRank=colorblindRanking,
                                         resultPath=resultPatth)
        pooll = Pool(processes=48)
        pooll.map(parallelFunc, allFairDiceRankingsOfSameExperimentFilenames)
        pooll.close()
        pooll.join()
        allDiceRollFairFilenames = [item for item in allDiceRollFairFilenames if item not in allFairDiceRankingsOfSameExperimentFilenames]


def readAndPrepareDiceRollEvalFiles(filename):
    return pd.read_csv(filename)


def evaluateDiceRollStatistics(evalDir, experiment):
    allEvalDiceRollFileNames = glob.glob(evalDir + experiment + "/diceroll/*.csv")
    while len(allEvalDiceRollFileNames) != 0:
        evalFilename = allEvalDiceRollFileNames[0]
        pString = evalFilename.split(sep="_")[3]
        kString = evalFilename.split(sep="_")[2]
        globPathName = evalDir + experiment + "/diceroll/*" + kString + "_" + glob.escape(pString) + "*.csv"
        allEvalFilenamesOfSameExperiment = glob.glob(globPathName)

        with Pool(processes=48) as pool:
            scoresPerGroup = pd.concat(pool.map(readAndPrepareDiceRollEvalFiles, allEvalFilenamesOfSameExperiment))

        print(scoresPerGroup)
        for colName in scoresPerGroup.columns:
            if 'group' in colName or 'Unnamed' in colName:
                continue
            newMeanCol = colName + "_mean"
            newStdCol = colName + "_std"
            scoresPerGroup[newMeanCol] = 0.0
            scoresPerGroup[newStdCol] = 0.0
            for groupName in scoresPerGroup["group"].unique():
                groupRows = scoresPerGroup.loc[scoresPerGroup["group"] == groupName]
                groupMean = groupRows[colName].mean()
                groupStd = groupRows[colName].std()
                scoresPerGroup.at[scoresPerGroup[scoresPerGroup["group"] == groupName].index[0], newMeanCol] = groupMean
                scoresPerGroup.at[scoresPerGroup[scoresPerGroup["group"] == groupName].index[0], newStdCol] = groupStd

        # only keep means and std
        keepCols = [c for c in scoresPerGroup.columns if (c[-4:] == 'mean') or (c[-3:] == 'std') or (c == 'group')]
        scoresPerGroup = scoresPerGroup[keepCols]
        scoresPerGroup = scoresPerGroup.drop_duplicates()

        scoresPerGroup.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_diceRollResultWithMeanAndStd.csv")
        allEvalDiceRollFileNames = [item for item in allEvalDiceRollFileNames if item not in allEvalFilenamesOfSameExperiment]


if __name__ == '__main__':
    rankingsDir = sys.argv[1]
    evalDir = sys.argv[2]
    experimentName = sys.argv[3]

    evaluateDiceRollPerExperiment(rankingsDir, evalDir, experimentName)
    evaluateDiceRollStatistics(evalDir, experimentName)
    evaluate(rankingsDir, evalDir, experimentName)
