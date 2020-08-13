'''
Created on Jun 12, 2020

@author: meike
'''

import pandas as pd
import numpy as np
import uuid


def dcg_score(y_true, y_score, k=10, gains="exponential"):
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
    order = np.argsort(y_score)[::-1]
    y_true = np.take(y_true, order[:k])

    if gains == "exponential":
        gains = 2 ** y_true - 1
    elif gains == "linear":
        gains = y_true
    else:
        raise ValueError("Invalid gains option.")

    # highest rank is 1 so +2 instead of +1
    discounts = np.log2(np.arange(len(y_true)) + 2)
    return np.sum(gains / discounts)


def ndcg_score(y_true, y_score, k=10, gains="exponential"):
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
        Whether gains should be "exponential" (default) or "linear".
    Returns
    -------
    NDCG @k : float
    """
    best = dcg_score(y_true, y_true, k, gains)
    actual = dcg_score(y_true, y_score, k, gains)
    return actual / best


def averageGroupExposureGain(colorblindRanking, fairRanking, result):
    result["expGain"] = 0.0
    for groupName in result["group"]:
        allCandidatesInGroup_fairRanking = fairRanking.loc[fairRanking["group"] == groupName]
        allCandidatesInGroup_colorblindRanking = colorblindRanking.loc[colorblindRanking["group"] == groupName]
        groupBias_fairRanking = positionBias(allCandidatesInGroup_fairRanking)
        groupBias_colorblind = positionBias(allCandidatesInGroup_colorblindRanking)
        expGain = groupBias_fairRanking - groupBias_colorblind
        result.at[result[result["group"] == groupName].index[0], "expGain"] = expGain
    return result


def positionBias(ranking):
    totalPositionBias = 0.0
    for position, _ in ranking.iterrows():
        totalPositionBias = totalPositionBias + 0.5 ** position

    # normalize by ranking size
    return totalPositionBias / len(ranking)


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
            allOthersAbove = allOthers.loc[0:position]
            worstScoreAbove = allOthersAbove.score.min()
            orderUtilLoss = max(0.0, candidate.score - worstScoreAbove)
            currentMaxLossPerGroup = result.at[result[result["group"] == groupName].index[0], "orderUtilLoss"]
            if orderUtilLoss > currentMaxLossPerGroup:
                originalPosition = colorblindRanking.loc[colorblindRanking['uuid'] == candidate.uuid].index[0]
                result.at[result[result["group"] == groupName].index[0], "maxRankDrop"] = position - originalPosition
                result.at[result[result["group"] == groupName].index[0], "orderUtilLoss"] = orderUtilLoss
    return result


def prepareForJavaCode(data, headersToFormAGroup):

    numberOfGroups = 1
    for header in headersToFormAGroup:
        numberOfGroups = numberOfGroups * len(data[header].unique())

    result = data.filter(headersToFormAGroup, axis=1)
    result["score"] = data["score"]
    result["group"] = 0
    result["uuid"] = [uuid.uuid4() for _ in range(len(data.index))]

    def setGroupID(x, groupId):
        x["group"] = groupId
        return x

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

    docString = docString + "\n\n" + str(result["group"].value_counts(normalize=True))

    return result, pd.DataFrame({"group": result["group"].unique()}), docString
