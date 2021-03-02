'''
Created on May 19, 2020

@author: meike
'''
import pandas as pd
import numpy as np
from src.util import *


def makeWorstThreeGroupsProtected(data, docString):
    groupExposureFrame = pd.DataFrame(columns=["group"])
    groupExposureFrame["group"] = data["group"].unique()
    groupExposureFrame = averageGroupExposure(data, groupExposureFrame)
    groupExposureFrame.sort_values(by=["exposure"], ascending=False, inplace=True)
    nonProtectedGroups = groupExposureFrame.head(len(groupExposureFrame.group) - 3)["group"]
    docString = docString + "\n\nFollowing groups were set to be non-protected: " + str(nonProtectedGroups.values)
    npIndex = data[data["group"].isin(nonProtectedGroups)].index
    data.loc[npIndex, "group"] = 0

    worstThreeGroups = groupExposureFrame.tail(3)["group"]
    newGroupName = 1
    docString = docString + "\n\nRenamed the following groups: "
    for groupName in worstThreeGroups:
        groupIndex = data.loc[data["group"] == groupName].index
        data.loc[groupIndex, "group"] = newGroupName
        docString = docString + "\n" + str(groupName) + " --> " + str(newGroupName)
        newGroupName += 1

    return data, pd.DataFrame({"group": data["group"].unique()}), docString


def main():
    data = pd.read_csv("../data/COMPAS/compas-scores-two-years.csv", header=0)
    # we do the same data cleaning as is done by ProPublica. See link for details
    # https://github.com/propublica/compas-analysis/blob/master/Compas%20Analysis.ipynb
    data = data[data["days_b_screening_arrest"] <= 30]
    data = data[data["days_b_screening_arrest"] >= -30]
    data = data[data["is_recid"] != -1]
    data = data[data["c_charge_degree"] != "O"]
    data = data[data["score_text"] != "N/A"]

    # drop irrelevant columns
    keep_cols = ["sex", "age_cat", "race", "decile_score", "v_decile_score", "priors_count", "two_year_recid"]
    data = data[keep_cols]
    data["sex"] = data["sex"].replace({"Male":1,
                                       "Female":0})
    data["age_cat"] = data["age_cat"].replace({"Less than 25":1,
                                               "25 - 45":2,
                                               "Greater than 45":0})
    data["race"] = data["race"].replace({"Caucasian":0,
                                         "African-American":1,
                                         "Hispanic":1,
                                         "Asian":1,
                                         "Native American":1,
                                         "Other":1})
    # normalize numeric columns to interval [0,1]
    scaledDecile = (data["decile_score"] - np.min(data["decile_score"])) / np.ptp(data["decile_score"])
    scaledVDecile = (data["v_decile_score"] - np.min(data["v_decile_score"])) / np.ptp(data["v_decile_score"])
    scaledpriors = (data["priors_count"] - np.min(data["priors_count"])) / np.ptp(data["priors_count"])

    # calculate score based on recidivism score, violent recidivism score and number of prior arrests
    # violent recidivism weighs highest.
    data["score"] = np.zeros(data.shape[0])
    for idx, _ in data.iterrows():
        recidivism = scaledDecile[idx]
        violentRecidivism = scaledVDecile[idx]
        priorArrests = scaledpriors[idx]

        score = 0.25 * recidivism + 0.5 * violentRecidivism + 0.25 * priorArrests
        data.loc[idx, "score"] = score

    # higher scores should be better scores
    data["score"] = data["score"].max() - data["score"]

    # add some random noise to break ties
    noise = np.random.normal(0, 0.000001, data.shape[0])
    data["score"] = data["score"] + noise

    # drop these columns
    data = data.drop(columns=['decile_score', 'v_decile_score', 'priors_count'])
    data.sort_values(by=["score"], ascending=False, inplace=True)

    data.to_csv("../data/COMPAS/compas_sexAgeRace.csv", header=True, index=False)

    # we need the groups as extra dataframe for the baseline
    resultData, groups, docString = prepareForJavaCode(data, ["sex"])
    resultData.to_csv("../data/COMPAS/compas_sex_java.csv", header=True, index=False)
    groups.to_csv("../data/COMPAS/compas_sex_groups.csv", header=True, index=False)
    with open("../data/COMPAS/compas_sex_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, groups, docString = prepareForJavaCode(data, ["race"])
    resultData.to_csv("../data/COMPAS/compas_race_java.csv", header=True, index=False)
    groups.to_csv("../data/COMPAS/compas_race_groups.csv", header=True, index=False)
    with open("../data/COMPAS/compas_race_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, groups, docString = prepareForJavaCode(data, ["age_cat"])
    resultData.to_csv("../data/COMPAS/compas_age_java.csv", header=True, index=False)
    groups.to_csv("../data/COMPAS/compas_age_groups.csv", header=True, index=False)
    with open("../data/COMPAS/compas_age_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, _, docString = prepareForJavaCode(data, ["age_cat", "race", "sex"])
    resultData, groups, docString = makeWorstThreeGroupsProtected(resultData, docString)
    resultData.to_csv("../data/COMPAS/compas_worstThreeGroups_java.csv", header=True, index=False)
    groups.to_csv("../data/COMPAS/compas_worstThreeGroups_groups.csv", header=True, index=False)
    with open("../data/COMPAS/compas_worstThreeGroups_doc.txt", "w") as text_file:
        text_file.write(docString)


if __name__ == '__main__':
    main()
