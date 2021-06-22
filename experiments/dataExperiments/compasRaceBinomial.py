import fairsearchcore as fsc
from fairsearchcore.models import FairScoreDoc
from fairsearchcore.re_ranker import fair_top_k
import pandas as pd
import numpy as np
from src import *
import scipy
import math

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

##########################Change values here##############################
k = 1500
alpha = 0.1

#p = 0.659 #sum of all protected proportions (sum(pstat))
#filename="CompasRace_oldFAIR_algo_sum_of_pstat_results.csv"

p = 0.5 #sum of all protected proportions (p=0.5) & (sum(peq))
filename = "CompasRace_oldFAIR_algo_sum_of_peq_results.csv"

#########################################################################

colorblind_candidates = pd.read_csv('./data/COMPAS/compas_race_java.csv')
colorblind_candidates["group"].unique()

fair = fsc.Fair(k, p, alpha)
mtable = fair.create_adjusted_mtable()

protected_candidates = []
unprotected_candidates = []
for i in range(0,len(colorblind_candidates.index)):
    score = colorblind_candidates.iloc[i][0]
    if colorblind_candidates.iloc[i][1] >0:
        protected = True
    else:
        protected = False
    cid = colorblind_candidates.iloc[i][2]
    if protected:
        protected_candidates.append(FairScoreDoc(cid, score, protected))
    else:
        unprotected_candidates.append(FairScoreDoc(cid, score, protected))

fair_ranking = fair_top_k(k,protected_candidates, unprotected_candidates, mtable)
fair_candidates = pd.DataFrame(columns=["score","group","uuid"])
ranked_list = []
for cand in fair_ranking:
    group = colorblind_candidates[(colorblind_candidates["uuid"] == cand.id)].iloc[0][1]
    fair_candidates = fair_candidates.append({'score': cand.score , 'group':group, 'uuid':cand.id }, ignore_index=True)
    ranked_list.append(cand.id)

remaining_ranking = pd.DataFrame(columns=["score","group","uuid"])
for i in range(0,len(colorblind_candidates.index)):
    if colorblind_candidates["uuid"][i] not in ranked_list:
        remaining_ranking = remaining_ranking.append({'score': colorblind_candidates.iloc[i][0] , 'group':colorblind_candidates.iloc[i][1], 'uuid':colorblind_candidates.iloc[i][2] }, ignore_index=True)

fair_ranking = fair_candidates
colorblind_ranking = colorblind_candidates

fair_result = pd.DataFrame()
fair_result["group"] = fair_ranking['group'].unique()
kay = len(fair_ranking)

# individual fairness metrics
fair_result = selectionUtilityLossPerGroup(remaining_ranking, fair_ranking, fair_result)
fair_result = orderingUtilityLossPerGroup(colorblind_ranking, fair_ranking, fair_result)

# performance metrics
fair_result["ndcgLoss"] = 1 - ndcg_score(colorblind_ranking["score"].to_numpy(),
                                                       fair_ranking["score"].to_numpy(),
                                                       k=kay)
fair_result["kendallTau"] = scipy.stats.kendalltau(colorblind_ranking.head(kay)["score"].to_numpy(),
                                                                         fair_ranking["score"].to_numpy())[0]

# group fairness metrics
fair_result = averageGroupExposureGain(colorblind_ranking.head(kay), fair_ranking, fair_result)
#fair_result = multi_fair_result.sort_values(by=['group'])
#fair_result.to_csv(evalDir + experiment + "/" + kString + "_" + pString + "_multiFairResult.csv")

fair_result.to_csv(filename)