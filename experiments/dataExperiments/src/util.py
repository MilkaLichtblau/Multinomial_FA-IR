'''
Created on Jun 12, 2020

@author: meike
'''

import pandas as pd
import numpy as np
import uuid
import math
import matplotlib as mpl
import matplotlib.pyplot as plt


################### EVALUATION ####################################
def dcg_score(y_score, gains="exponential"):
    """Discounted cumulative gain (DCG) at rank k
    Parameters
    ----------
    y_true : array-like, shape = [n_samples]
        Ground truth (true relevance labels).
    y_score : array-like, shape = [n_samples]
        Predicted scores.
    k : int
        Rank.
    gains : str
        Whether gains should be "exponential" (default) or "linear".
    Returns
    -------
    DCG @k : float
    """
    if gains == "exponential":
        gains = 2 ** y_score - 1
    elif gains == "linear":
        gains = y_score
    else:
        raise ValueError("Invalid gains option.")

    # highest rank is 1 so +2 instead of +1
    discounts = np.log2(np.arange(len(y_score)) + 2)
    return np.sum(gains / discounts)


def ndcg_score(y_true, y_score, k=10, gains="linear"):
    """Normalized discounted cumulative gain (NDCG) at rank k
    Parameters
    ----------
    y_true : array-like, shape = [n_samples]
        Ground truth (true relevance labels).
    y_score : array-like, shape = [n_samples]
        Predicted scores.
    k : int
        Rank.
    gains : str
        Whether gains should be "exponential" or "linear" (default).
    Returns
    -------
    NDCG @k : float
    """
    best = dcg_score(y_true[:k], gains)
    actual = dcg_score(y_score[:k], gains)
    return actual / best


def averageGroupExposure(ranking, result):
    result["exposure"] = 0.0
    for groupName in result["group"]:
        allCandidatesInGroup = ranking.loc[ranking["group"] == groupName]
        # normalize avg group exposure by group size, such that they are comparable
        groupBias = positionBias(allCandidatesInGroup) / allCandidatesInGroup.shape[0]
        result.at[result[result["group"] == groupName].index[0], "exposure"] = groupBias
    return result


def averageGroupExposureGain(colorblindRanking, fairRanking, result):
    result["expGain"] = 0.0
    for groupName in result["group"]:
        allCandidatesInGroup_fairRanking = fairRanking.loc[fairRanking["group"] == groupName]
        allCandidatesInGroup_colorblindRanking = colorblindRanking.loc[colorblindRanking["group"] == groupName]
        groupBias_fairRanking = positionBias(allCandidatesInGroup_fairRanking)
        groupBias_colorblind = positionBias(allCandidatesInGroup_colorblindRanking)
        if groupBias_colorblind == 0 and groupBias_fairRanking == 0:
            print("group " + str(groupName) + " did not appear in the top-k in both rankings")
        elif groupBias_fairRanking == 0:
            print("group " + str(groupName) + " did not appear in the top-k in fair ranking")
            # expGain = -math.inf
        elif groupBias_colorblind == 0:
            print("group " + str(groupName) + " did not appear in the top-k in colorblind ranking")
            # expGain = math.inf
        expGain = groupBias_fairRanking - groupBias_colorblind
        result.at[result[result["group"] == groupName].index[0], "expGain"] = expGain
    return result


def positionBias(ranking):
    if ranking.empty:
        # this case can happen if a group does not appear in the top-k at all
        # we assign zero then
        return 0
    totalPositionBias = 0.0
    for position, _ in ranking.iterrows():
        if math.log2(position + 2) == 0.0:
            print(position)
        totalPositionBias = totalPositionBias + (1 / (math.log2(position + 2)))

    return totalPositionBias


def aucLossWRTColorblind(colorblindRanking, fairRanking, inputData):

    result = 1

    return result


def selectionUtilityLossPerGroup(remainingRanking, fairRanking, result):
    # add column to result frame
    result["selectUtilLoss"] = 0.0
    # do evaluation for each group separately
    for groupName in result["group"]:
        allExcludedCandidatesInGroup = remainingRanking.loc[remainingRanking["group"] == groupName]
        allIncludedCandidatesFromOtherGroups = fairRanking.loc[fairRanking["group"] != groupName]
        firstExcludedInGroup = allExcludedCandidatesInGroup.score.max()
        worstAbove = allIncludedCandidatesFromOtherGroups.score.min()
        selectUtilLoss = max(0, firstExcludedInGroup - worstAbove)
        result.at[result[result["group"] == groupName].index[0], "selectUtilLoss"] = selectUtilLoss
    return result


def orderingUtilityLossPerGroup(colorblindRanking, fairRanking, result):
    result["orderUtilLoss"] = 0.0
    result["maxRankDrop"] = 0
    for groupName in result["group"]:
        allCandidatesInGroup = fairRanking.loc[fairRanking["group"] == groupName]
        allOthers = fairRanking.loc[fairRanking["group"] != groupName]
        for position, candidate in allCandidatesInGroup.iterrows():
            allOthersAbove = allOthers.loc[0:position - 1]
            worstScoreAbove = allOthersAbove.score.min()

            # calculate ordering utility loss, should be maximum of all
            orderUtilLoss = max(0.0, candidate.score - worstScoreAbove)
            currentMaxLossPerGroup = result.at[result[result["group"] == groupName].index[0], "orderUtilLoss"]
            if orderUtilLoss > currentMaxLossPerGroup:
                result.at[result[result["group"] == groupName].index[0], "orderUtilLoss"] = orderUtilLoss

            # calculate max rank drop for groups
            originalPosition = colorblindRanking.loc[colorblindRanking['uuid'] == candidate.uuid].index[0]
            rankdrop = position - originalPosition
            currentMaxRankDrop = result.at[result[result["group"] == groupName].index[0], "maxRankDrop"]
            if rankdrop > currentMaxRankDrop:
                result.at[result[result["group"] == groupName].index[0], "maxRankDrop"] = rankdrop
    return result

############################# VISUALISATION #############################################


def plotKDEPerGroup(data, score_attr, filename, colNames=None):

    mpl.rcParams.update({'font.size': 24, 'lines.linewidth': 3, 'lines.markersize': 15, 'font.family':'Times New Roman'})
    # avoid type 3 (i.e. bitmap) fonts in figures
    mpl.rcParams['ps.useafm'] = True
    mpl.rcParams['pdf.use14corefonts'] = True
    mpl.rcParams['text.usetex'] = True

    scoresPerGroup = getScoresByGroup(data)
    if colNames is not None:
        scoresPerGroup = scoresPerGroup.rename(colNames, axis='columns')
    scoresPerGroup.plot.kde()
    score_attr = score_attr.replace('_', '\_')

    plt.xlabel(score_attr)
#     plt.legend(bbox_to_anchor=(1.1, 1.05), prop={'size': 25})
    plt.legend(bbox_to_anchor=(0, 0, 1, 1), prop={'size': 20})
    plt.savefig(filename, dpi=100, bbox_inches='tight')


def getScoresByGroup(data):
    """
    takes a dataset with one data point per row
    each data point has a qualifying as well as >= 1 sensitive attribute column
    takes all values from column qual_attr and resorts data such that result contains all scores from
    qual_attr in one column per group of sensitive attributes.

    Arguments:
        dataset {[dataframe]} -- raw data with one data point per row
        scoreColName {[string]} -- name of column that contains scores

    Returns:
        [dataframe] -- group labels as column names and scores as column values,
                       columns can contain NaNs if group sizes are not equal
    """

    result = pd.DataFrame(dtype=float)
    # select all rows that belong to one group
    for group in data["group"].unique():
        colName = str(group)
        copy = data.copy()
        copy = copy.loc[(copy["group"] == group)]
        resultCol = pd.DataFrame(data=copy["score"].values, columns=[colName])
        # needs concat to avoid data loss in case new resultCol is longer than already existing result
        # dataframe
        result = pd.concat([result, resultCol], axis=1)
    return result


############################# DATA CLEANING #############################################
def prepareForJavaCode(data, headersToFormAGroup):
    numberOfGroups = 1
    for header in headersToFormAGroup:
        numberOfGroups = numberOfGroups * len(data[header].unique())

    result = data.filter(headersToFormAGroup, axis=1)
    result["score"] = data["score"]
    result["group"] = 0
    result["uuid"] = [uuid.uuid4() for _ in range(len(data.index))]

#     def setGroupID(x, groupId):
#         x["group"] = groupId
#         return x

    grouped = result.groupby(list(headersToFormAGroup), as_index=False, sort=False)
    groupID = 1
    docString = str(headersToFormAGroup) + "\n"
    for name, group in grouped:
        index = group.index
        if (type(name) is int and name == 0) or (type(name) is tuple and name.count(0) == len(name)):
            # we have found the non-protected group
            result.loc[index, "group"] = 0
            docString = docString + str(name) + " --> 0\n"
        else:
            result.loc[index, "group"] = groupID
            docString = docString + str(name) + " --> " + str(groupID) + "\n"
            groupID += 1

    result = result.drop(columns=headersToFormAGroup)
    result.sort_values(by=["score", "uuid"], ascending=[False, True], inplace=True)
    result = result.reset_index(drop=True)
    docString = docString + "\n\nPercentages of Groups\n" + str(result["group"].value_counts(normalize=True))

    # write exposure per group in docString to decide who should be protected
    groupExposureFrame = pd.DataFrame(columns=["group"])
    groupExposureFrame["group"] = result["group"].unique()
    groupExposureFrame = averageGroupExposure(result, groupExposureFrame)
    groupExposureFrame.sort_values(by=["exposure"], ascending=False, inplace=True)
    docString = docString + "\n\nExposure Per Groups\n" + str(groupExposureFrame)

    return result, pd.DataFrame({"group": result["group"].unique()}), docString
