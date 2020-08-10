'''
Created on May 19, 2020

@author: meike
'''

import pandas as pd
import numpy as np
import scipy.stats as stats
from src.util import prepareForJavaCode


def main():
    data = pd.read_csv("../data/GermanCredit/german.data", sep=" ")
    # keep credit duration (A2), credit amount (A5), status of existing account (A1), employment length (A7)
    # plus protected attributes sex (A9), age (A13) and foreigner (A20)
    data = data.iloc[:, [0, 1, 4, 6, 8, 12, 19]]
    data.columns = ["accountStatus", "creditDuration", "creditAmount", "employmentLength", "sex", "age", "foreigner"]
    # 0 means no account exists
    data["accountStatus"] = data["accountStatus"].replace({"A11":1,
                                                           "A12":2,
                                                           "A13":3,
                                                           "A14":0})
    # 0 means unemployed
    data["employmentLength"] = data["employmentLength"].replace({"A71":0,
                                                                 "A72":1,
                                                                 "A73":2,
                                                                 "A74":3,
                                                                 "A75":4})
    # 0 is male, 1 is female
    data["sex"] = data["sex"].replace({"A91":0,
                                       "A92":1,
                                       "A93":0,
                                       "A94":0,
                                       "A95":1})
    # 0 is resident, 1 is foreigner
    data["foreigner"] = data["foreigner"].replace({"A201":1,
                                                   "A202":0})

    # categorize age data, oldest (group 2) and youngest (group 1) decile are protected
    data["decileRank"] = pd.qcut(data["age"], 10, labels=False)

    data["age"] = np.where(data["decileRank"] == 0, 1, data["age"])
    data["age"] = np.where(data["decileRank"].between(1, 8), 0, data["age"])
    data["age"] = np.where(data["decileRank"] == 9, 2, data["age"])

    data["score"] = np.zeros(data.shape[0])
    for idx, row in data.iterrows():
        accountStatus = row.loc["accountStatus"]
        creditDuration = row.loc["creditDuration"]
        creditAmount = row.loc["creditAmount"]
        employmentLength = row.loc["employmentLength"]

        score = 0.25 * accountStatus + 0.25 * creditDuration + 0.25 * creditAmount + .25 * employmentLength
        data.loc[idx, "score"] = score

    data = data.drop(columns=['accountStatus', 'creditDuration', 'creditAmount', 'employmentLength'])
    data['score'] = stats.zscore(data['score'])
    data.sort_values(by=["score"], ascending=False, inplace=True)

    data.to_csv("../data/GermanCredit/germanCredit_sexAgeForeigner.csv", header=True, index=False)

    resultData, groups, docString = prepareForJavaCode(data, ["sex"])
    resultData.to_csv("../data/GermanCredit/germanCredit_sex_java.csv", header=True, index=False)
    groups.to_csv("../data/GermanCredit/germanCredit_sex_groups.csv", header=True, index=False)
    with open("../data/GermanCredit/germanCredit_sex_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, groups, docString = prepareForJavaCode(data, ["age"])
    resultData.to_csv("../data/GermanCredit/germanCredit_age_java.csv", header=True, index=False)
    groups.to_csv("../data/GermanCredit/germanCredit_age_groups.csv", header=True, index=False)
    with open("../data/GermanCredit/germanCredit_age_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, groups, docString = prepareForJavaCode(data, ["foreigner"])
    resultData.to_csv("../data/GermanCredit/germanCredit_foreigner_java.csv", header=True, index=False)
    groups.to_csv("../data/GermanCredit/germanCredit_foreigner_groups.csv", header=True, index=False)
    with open("../data/GermanCredit/germanCredit_foreigner_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, groups, docString = prepareForJavaCode(data, ["sex", "foreigner"])
    resultData.to_csv("../data/GermanCredit/germanCredit_sexForeigner_java.csv", header=True, index=False)
    groups.to_csv("../data/GermanCredit/germanCredit_sexForeigner_groups.csv", header=True, index=False)
    with open("../data/GermanCredit/germanCredit_sexForeigner_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, groups, docString = prepareForJavaCode(data, ["sex", "age"])
    resultData.to_csv("../data/GermanCredit/germanCredit_sexAge_java.csv", header=True, index=False)
    groups.to_csv("../data/GermanCredit/germanCredit_sexAge_groups.csv", header=True, index=False)
    with open("../data/GermanCredit/germanCredit_sexAge_doc.txt", "w") as text_file:
        text_file.write(docString)

    resultData, groups, docString = prepareForJavaCode(data, ["age", "foreigner"])
    resultData.to_csv("../data/GermanCredit/germanCredit_ageForeigner_java.csv", header=True, index=False)
    groups.to_csv("../data/GermanCredit/germanCredit_ageForeigner_groups.csv", header=True, index=False)
    with open("../data/GermanCredit/germanCredit_ageForeigner_doc.txt", "w") as text_file:
        text_file.write(docString)


if __name__ == '__main__':
    main()
