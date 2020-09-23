'''
Created on Aug 20, 2020

@author: meike
'''

from src.util import plotKDEPerGroup
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import glob
from reportlab.lib.normalDate import ND


def visualizeOrigCompasData():
    data = pd.read_csv("../data/COMPAS/compas_sex_java.csv", header=0, skipinitialspace=True)
    # invert scores for plots
    data["score"] = 1 - data["score"]
    labels = {"0": "females",
              "1": "males"}
    plotKDEPerGroup(data, "Recidivism Risk", "../data/COMPAS/compas_sex_kde.png", colNames=labels)

    data = pd.read_csv("../data/COMPAS/compas_race_java.csv", header=0, skipinitialspace=True)
    # invert scores for plots
    data["score"] = 1 - data["score"]
    labels = {"0": "white",
              "1": "non-white"}
    plotKDEPerGroup(data, "Recidivism Risk", "../data/COMPAS/compas_race_kde.png", colNames=labels)

    data = pd.read_csv("../data/COMPAS/compas_age_java.csv", header=0, skipinitialspace=True)
    # invert scores for plots
    data["score"] = 1 - data["score"]
    labels = {"0": "older 45",
              "2": "younger 25",
              "1": "25 - 45"}
    plotKDEPerGroup(data, "Recidivism Risk", "../data/COMPAS/compas_age_kde.png", colNames=labels)

    data = pd.read_csv("../data/COMPAS/compas_worstThreeGroups_java.csv", header=0, skipinitialspace=True)
    # invert scores for plots
    data["score"] = 1 - data["score"]
    labels = {"0": "others",
              "1": "Y NW F",
              "2": "Y W F",
              "3": "Y NW M"}
    plotKDEPerGroup(data, "Recidivism Risk", "../data/COMPAS/compas_worstThreeGroups_kde.png", colNames=labels)


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


def plotThreeDFigure():
    allEvalFilenames = glob.glob("../results/COMPAS/evalAndPlots/age/k=100" + "*_multiFairResult.csv")
    data = []
    for filename in allEvalFilenames:
        evalData = pd.read_csv(filename, header=0, skipinitialspace=True)
        pString = filename.split(sep="_")[1]
        minProp1Val = float(pString.split(sep=",")[1])
        minProp2String = pString.split(sep=",")[2]
        minProp2Val = float(minProp2String[:-1])
        minExpGainVal = evalData["expGain"].min()
        ndcgLossVal = evalData["ndcgLoss"].min()
        tempDict = dict(minProp1=minProp1Val, minProp2=minProp2Val, minExpGain=minExpGainVal, ndcgLoss=ndcgLossVal)
        data.append(tempDict)

    dataToPlot = pd.DataFrame(data, columns=["minProp1", "minProp2", "minExpGain", "ndcgLoss"])
    dataToPlot.sort_values("minProp1", inplace=True)
    print(dataToPlot)

    fig = plt.figure()
    ax1 = fig.add_subplot(111, projection='3d')

    xpos = dataToPlot["minProp1"].values
    ypos = dataToPlot["minProp1"].values
    num_elements = len(xpos)
    zpos = [0, 0, 0, 0, 0, 0]
    dx = [0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
    dy = [0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
    dz = np.ones(6)  # [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

    ax1.bar3d(xpos, ypos, zpos, dx, dy, dz, color='#00ceaa')
    plt.show()
    # wait for evaluation to be done


def main():
#     visualizeOrigCompasData()
#     visualizeOrigGermanCreditData()
#     visualizeOrigLSATData()

    plotThreeDFigure()


if __name__ == '__main__':
    main()
