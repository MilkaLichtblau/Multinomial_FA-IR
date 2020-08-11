'''
Created on Jun 12, 2020

@author: meike
'''

import pandas as pd
import sklearn


def ndcgLoss(colorblindRanking, fairRanking):
    return 1 - sklearn.metrics.ndcg_score(colorblindRanking, fairRanking)


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


def orderingUtilityLossPerGroup(fairRanking, result):
    result["orderUtilLoss"] = 0.0
    for groupName in result["group"]:
        allCandidatesInGroup = fairRanking.loc[fairRanking["group"] == groupName]
        allOthers = fairRanking.loc[fairRanking["group"] != groupName]
        for position, candidate in allCandidatesInGroup.iterrows():
            allOthersAbove = allOthers.loc[0:position]
            worstScoreAbove = allOthersAbove.score.min()
            orderUtilLoss = max(0, candidate.score - worstScoreAbove)
            currentMaxLossPerGroup = result.at[result[result["group"] == groupName].index[0], "selectUtilLoss"]
            if orderUtilLoss > currentMaxLossPerGroup:
                result.at[result[result["group"] == groupName].index[0], "selectUtilLoss"] = orderUtilLoss
    return result


def prepareForJavaCode(data, headersToFormAGroup):

    numberOfGroups = 1
    for header in headersToFormAGroup:
        numberOfGroups = numberOfGroups * len(data[header].unique())

    result = data.filter(headersToFormAGroup, axis=1)
    result["score"] = data["score"]
    result["group"] = 0

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
    result.sort_values(by=["score"], ascending=False, inplace=True)

    docString = docString + "\n\n" + str(result["group"].value_counts(normalize=True))

    return result, pd.DataFrame({"group": result["group"].unique()}), docString
