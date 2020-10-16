import pandas as pd
import numpy as np
import sys, os, math
import matplotlib as mpl
import matplotlib.pyplot as plt

from cfa.cfa import ContinuousFairnessAlgorithm
from evaluation.fairnessMeasures import groupPercentageAtK
from evaluation.relevanceMeasures import pak, ndcg_score


def rerank_with_cfa(score_stepsize, thetas, result_filename, pathToData, pathToGroups, qual_attr, group_names):
    data = pd.read_csv(pathToData, sep=',')
    groups = pd.read_csv(pathToGroups, sep=',')

    # check that we have a theta for each group
    if groups.shape[0] != len(thetas):
        raise ValueError(
            "invalid number of thetas, should be {numThetas} Specify one theta per group.".format(numThetas=groups.shape[0]))

    regForOT = 5e-3
    plot_dir = os.path.dirname(result_filename) + "/"
    print(plot_dir)
    cfa = ContinuousFairnessAlgorithm(data,
                                      groups,
                                      group_names,
                                      qual_attr,
                                      score_stepsize,
                                      thetas,
                                      regForOT,
                                      path=plot_dir,
                                      plot=True)
    result = cfa.run()
    result.to_csv(result_filename)


def parseThetas(thetaString):
    thetas = np.array(thetaString.split(","))
    floatThetas = [float(i) for i in thetas]
    return floatThetas


def precisionAtKPrep(origData, fairData, qualAttr):
    origSorting = origData.sort_values(by=[qualAttr, 'uuid'], ascending=[False, True])
    origSorting = origSorting.reset_index(drop=True)

    fairSorting = fairData.sort_values(by=['fairScore', 'uuid'], ascending=[False, True])
    fairSorting = fairSorting.reset_index(drop=True)

    return origSorting['uuid'].values, fairSorting['uuid'].values


def ndcgPrep(fairData):
    ndcgData = fairData.sort_values(by=['fairScore', 'uuid'], ascending=[False, True])
    ndcgData = ndcgData.reset_index(drop=True)
    return ndcgData


def evaluateRelevance(origData, fairData, result_dir, qualAttr, stepsize, calcResult=0):

    ndcgAtK = np.empty(int(math.ceil(fairData.shape[0] / stepsize)))
    precisionAtK = np.empty(int(math.ceil(fairData.shape[0] / stepsize)))
    kAtK = np.empty(int(math.ceil(fairData.shape[0] / stepsize)))
    index = 0

    ndcgData = ndcgPrep(fairData)
    pakOrigData, pakFairData = precisionAtKPrep(origData, fairData, qualAttr)
    for k in range(0, fairData.shape[0], stepsize):
        print(k)
        # relevance measures
        np.put(ndcgAtK,
               index,
               ndcg_score(ndcgData[qualAttr].values, ndcgData['fairScore'].values, k, gains="linear"))
        np.put(precisionAtK,
               index,
               pak(k + 1, pakOrigData, pakFairData))
        np.put(kAtK,
               index,
               k)
        index += 1

    # save result to disk if wanna change plots later
    performanceData = np.stack((kAtK, ndcgAtK, precisionAtK), axis=-1)
    performanceDataframe = pd.DataFrame(performanceData, columns=['pos', 'ndcg', 'P$@$k'])
    performanceDataframe = performanceDataframe.set_index('pos')
    performanceDataframe.to_csv(result_dir + "relevanceEvaluation_stepsize=" + str(stepsize) + ".csv")
    performanceDataframe = pd.read_csv(result_dir + "relevanceEvaluation_stepsize=" + str(stepsize) + ".csv")
    performanceDataframe = performanceDataframe.set_index('pos')

    # plot results
    mpl.rcParams.update({'font.size': 24, 'lines.linewidth': 3,
                         'lines.markersize': 15, 'font.family': 'Times New Roman'})
    mpl.rcParams['ps.useafm'] = True
    mpl.rcParams['pdf.use14corefonts'] = True
    mpl.rcParams['text.usetex'] = True
    ax = performanceDataframe.plot(y=['ndcg', 'P$@$k'],
                                   kind='line',
                                   use_index=True,
                                   yticks=np.arange(0.0, 1.1, 0.2),
                                   rot=45)
    ax.legend(bbox_to_anchor=(1.05, 1),
              loc=2,
              borderaxespad=0.)  # , labels=self.__groupNamesForPlots)
    ax.set_xlabel("ranking position")
    ax.set_ylabel("relevance score")
    plt.savefig(result_dir + "relevanceEvaluation_stepsize=" + str(stepsize) + ".png", dpi=100, bbox_inches='tight')


def evaluateFairness(data, groups, groupNames, result_dir, stepsize):
    """
    evaluates fairness of rankings resulting from cfa algorithm
    """

    index = 0
    percAtK = np.empty(shape=(int(math.ceil(data.shape[0] / stepsize)), len(groups)))
    kAtK = np.empty(int(math.ceil(data.shape[0] / stepsize)))
    data = ndcgPrep(data)
    for k in range(0, data.shape[0], stepsize):
        print(k)
        percAtK[index] = groupPercentageAtK(data.head(k + 1), groups)
        kAtK[index] = k
        index += 1

    # save result to disk if wanna change plots later
    fairnessData = np.c_[kAtK.T, percAtK]
    colNames = ['pos'] + groupNames
    fairnessDataframe = pd.DataFrame(fairnessData, columns=colNames)
    fairnessDataframe = fairnessDataframe.set_index('pos')
    fairnessDataframe.to_csv(result_dir + "fairnessEvaluation_stepsize=" + str(stepsize) + ".csv")
    fairnessDataframe = pd.read_csv(result_dir + "fairnessEvaluation_stepsize=" + str(stepsize) + ".csv")
    fairnessDataframe = fairnessDataframe.set_index('pos')

    # plot results
    mpl.rcParams.update({'font.size': 24, 'lines.linewidth': 3,
                         'lines.markersize': 15, 'font.family': 'Times New Roman'})
    mpl.rcParams['ps.useafm'] = True
    mpl.rcParams['pdf.use14corefonts'] = True
    mpl.rcParams['text.usetex'] = True
    ax = fairnessDataframe.plot(y=groupNames,
                                kind='line',
                                use_index=True,
                                rot=45)
    ax.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)
    ax.set_xlabel("ranking position")
    ax.set_ylabel("percentage")
    plt.savefig(result_dir + "fairnessEvaluation_stepsize=" + str(stepsize) + ".png", dpi=100, bbox_inches='tight')


def main():
    score_stepsize = float(sys.argv[2])
    thetas = parseThetas(sys.argv[3])
    result_dir = sys.argv[4]
    if sys.argv[1] == 'compas_age':
        groupNames = {"[0]":"> 45",
                      "[1]":"< 25",
                      "[2]":"25-45"}
        rerank_with_cfa(score_stepsize,
                        thetas,
                        result_dir,
                        '../../data/COMPAS/compas_age_java.csv',
                        '../../data/COMPAS/compas_age_groups.csv',
                        'score',
                        groupNames)
    elif sys.argv[1] == 'compas_race':
        groupNames = {"[0]":"White",
                      "[1]":"Non-White"}
        rerank_with_cfa(score_stepsize,
                        thetas,
                        result_dir,
                        '../../data/COMPAS/compas_race_java.csv',
                        '../../data/COMPAS/compas_race_groups.csv',
                        'score',
                        groupNames)
    elif sys.argv[1] == 'compas_worstThreeGroups':
        groupNames = {"[0]":"Others",
                      "[1]":"PoC male < 25yr",
                      "[2]":"White male < 25yr",
                      "[3]":"PoC female < 25yr"}
        rerank_with_cfa(score_stepsize,
                        thetas,
                        result_dir,
                        '../../data/COMPAS/compas_worstThreeGroups_java.csv',
                        '../../data/COMPAS/compas_worstThreeGroups_groups.csv',
                        'score',
                        groupNames)
    elif sys.argv[1] == 'LSAT_sexRace':
        groupNames = {"[0 0]":"Male, White",
                      "[1 0]":"Female, White",
                      "[0 1]":"Male, Non-White",
                      "[1 1]":"Female, Non-White"}
        rerank_with_cfa(score_stepsize,
                        thetas,
                        result_dir,
                        '../../data/LSAT/LSAT_sexRace_java.csv',
                        '../../data/LSAT/LSAT_sexRace_groups.csv',
                        'score',
                        groupNames)
    elif sys.argv[1] == 'germanCredit_sexAge':
        groupNames = {"[0 0]":"Male, middle age",
                      "[0 1]":"Male, among 100 youngest",
                      "[0 2]":"Male, among 100 oldest",
                      "[1 0]":"Female, middle age",
                      "[1 1]":"Female, among 100 youngest",
                      "[1 2]":"Female, among 100 oldest"}
        rerank_with_cfa(score_stepsize,
                        thetas,
                        result_dir,
                        '../../data/GermanCredit/germanCredit_sexAge_java.csv',
                        '../../data/GermanCredit/germanCredit_sexAge_groups.csv',
                        'score',
                        groupNames)
    else:
        print("CFA: unknown command line option.")


if __name__ == '__main__':
    main()
