'''
Created on Aug 20, 2020

@author: meike
'''

from src.util import plotKDEPerGroup
import pandas as pd


def visualizeOrigCompasData():
    data = pd.read_csv("../data/COMPAS/compas_sex_java.csv", header=0, skipinitialspace=True)
    labels = {"0": "females",
              "1": "males"}
    plotKDEPerGroup(data, "Negative Recidivism Risk\n(higher is better)", "../data/COMPAS/compas_sex_kde.png", colNames=labels)

    data = pd.read_csv("../data/COMPAS/compas_race_java.csv", header=0, skipinitialspace=True)
    labels = {"0": "white",
              "1": "non-white"}
    plotKDEPerGroup(data, "Negative Recidivism Risk\n(higher is better)", "../data/COMPAS/compas_race_kde.png", colNames=labels)

    data = pd.read_csv("../data/COMPAS/compas_age_java.csv", header=0, skipinitialspace=True)
    labels = {"0": "older 45",
              "2": "younger 25",
              "1": "25 - 45"}
    plotKDEPerGroup(data, "Negative Recidivism Risk\n(higher is better)", "../data/COMPAS/compas_age_kde.png", colNames=labels)

    data = pd.read_csv("../data/COMPAS/compas_worstThreeGroups_java.csv", header=0, skipinitialspace=True)
    labels = {"0": "all others",
              "1": "young non-white females",
              "2": "young white females",
              "3": "young non-white males"}
    plotKDEPerGroup(data, "Negative Recidivism Risk\n(higher is better)", "../data/COMPAS/compas_worstThreeGroups_kde.png", colNames=labels)


def visualizeOrigGermanCreditData():
    data = pd.read_csv("../data/GermanCredit/germanCredit_sex_java.csv", header=0, skipinitialspace=True)
    labels = {"0": "females",
              "1": "males"}
    plotKDEPerGroup(data, "Credit Score\n(higher is better)", "../data/GermanCredit/germanCredit_sex_kde.png", colNames=labels)

    data = pd.read_csv("../data/GermanCredit/germanCredit_age_java.csv", header=0, skipinitialspace=True)
    labels = {"0": "others",
              "1": "oldest decile",
              "2": "youngest decile"}
    plotKDEPerGroup(data, "Credit Score\n(higher is better)", "../data/GermanCredit/germanCredit_age_kde.png", colNames=labels)


def visualizeOrigLSATData():
    data = pd.read_csv("../data/LSAT/LSAT_sexRace_java.csv", header=0, skipinitialspace=True)
    labels = {"0": "white males",
              "1": "white females",
              "2": "non-white males",
              "3": "non-white females"}
    plotKDEPerGroup(data, "LSAT Score\n(higher is better)", "../data/LSAT/LSAT_sexRace_kde.png", colNames=labels)


def main():
    visualizeOrigCompasData()
    visualizeOrigGermanCreditData()
    visualizeOrigLSATData()


if __name__ == '__main__':
    main()
