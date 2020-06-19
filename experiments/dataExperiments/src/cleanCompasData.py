'''
Created on May 19, 2020

@author: meike
'''
import pandas as pd
import numpy as np
from util import prepareForJavaCode


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
    keep_cols = ["sex", "age_cat", "race", "decile_score", "v_decile_score", "priors_count"]
    data = data[keep_cols]
    data["sex"] = data["sex"].replace({"Male":0,
                                       "Female":1})
    data["age_cat"] = data["age_cat"].replace({"Less than 25":0,
                                               "25 - 45":1,
                                               "Greater than 45":2})
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
    # drop these columns
    data = data.drop(columns=['decile_score', 'v_decile_score', 'priors_count'])
    data.sort_values(by=["score"], ascending=False, inplace=True)

    data.to_csv("../data/COMPAS/compas_sexAgeRace.csv", header=True, index=False)

    resultData, docString = prepareForJavaCode(data, ["sex"])
    resultData.to_csv("../data/COMPAS/compas_sex_java.csv", header=True, index=False)
    with open("../data/COMPAS/compas_sex_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, docString = prepareForJavaCode(data, ["race"])
    resultData.to_csv("../data/COMPAS/compas_race_java.csv", header=True, index=False)
    with open("../data/COMPAS/compas_race_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, docString = prepareForJavaCode(data, ["age_cat"])
    resultData.to_csv("../data/COMPAS/compas_age_java.csv", header=True, index=False)
    with open("../data/COMPAS/compas_age_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, docString = prepareForJavaCode(data, ["sex", "race"])
    resultData.to_csv("../data/COMPAS/compas_sexRace_java.csv", header=True, index=False)
    with open("../data/COMPAS/compas_sexRace_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, docString = prepareForJavaCode(data, ["sex", "age_cat"])
    resultData.to_csv("../data/COMPAS/compas_sexAge_java.csv", header=True, index=False)
    with open("../data/COMPAS/compas_sexAge_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, docString = prepareForJavaCode(data, ["age_cat", "race"])
    resultData.to_csv("../data/COMPAS/compas_ageRace_java.csv", header=True, index=False)
    with open("../data/COMPAS/compas_ageRace_doc.txt", "w") as text_file:
        text_file.write(docString)


if __name__ == '__main__':
    main()
