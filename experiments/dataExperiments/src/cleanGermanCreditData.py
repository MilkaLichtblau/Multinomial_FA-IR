'''
Created on May 19, 2020

@author: meike
'''

import pandas as pd
import numpy as np
import scipy.stats as stats
from util import prepareForJavaCode


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

    prepareForJavaCode(data, ["sex"]).to_csv("../data/GermanCredit/germanCredit_sex_java.csv", header=True, index=False)
    prepareForJavaCode(data, ["age"]).to_csv("../data/GermanCredit/germanCredit_age_java.csv", header=True, index=False)
    prepareForJavaCode(data, ["foreigner"]).to_csv("../data/GermanCredit/germanCredit_foreigner_java.csv", header=True, index=False)
    prepareForJavaCode(data, ["sex", "foreigner"]).to_csv("../data/GermanCredit/germanCredit_sexForeigner_java.csv", header=True, index=False)
    prepareForJavaCode(data, ["sex", "age"]).to_csv("../data/GermanCredit/germanCredit_sexAge_java.csv", header=True, index=False)
    prepareForJavaCode(data, ["age", "foreigner"]).to_csv("../data/GermanCredit/germanCredit_ageForeigner_java.csv", header=True, index=False)


if __name__ == '__main__':
    main()
