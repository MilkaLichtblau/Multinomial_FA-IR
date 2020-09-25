'''
Created on Aug 20, 2020

@author: meike
'''

from src.util import plotKDEPerGroup
import matplotlib.pyplot as plt
import matplotlib as mpl
from matplotlib import cm
from matplotlib.colors import Normalize
import numpy as np
import pandas as pd
import glob


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


def plot3DFigure(xPos, yPos, zHeight, zLabel, filename):
    mpl.rcParams.update({'font.size':24, 'lines.linewidth':3, 'lines.markersize':15, 'font.family':'Times New Roman'})  # avoid type 3 (i.e. bitmap) fonts in figures
    mpl.rcParams['ps.useafm'] = True
    mpl.rcParams['pdf.use14corefonts'] = True
    mpl.rcParams['text.usetex'] = True
    fig = plt.figure()
    ax1 = fig.add_subplot(111, projection='3d')
    xpos = xPos
    ypos = yPos
    num_elements = len(xpos)
    zpos = np.zeros(num_elements)  # np.full((1, num_elements), dataToPlot["expGainGroup2"].min())[0]
    dx = np.full((1, num_elements), 0.1)[0]
    dy = np.full((1, num_elements), 0.1)[0]
    dz = zHeight
    cmap = cm.get_cmap('coolwarm')
    norm = Normalize(vmin=min(dz), vmax=max(dz))
    colors = cmap(norm(dz))
    ax1.bar3d(xpos, ypos, zpos, dx, dy, dz, color=colors)
# axis labels
    ax1.set_xlabel('p\_1', labelpad=15)
    ax1.set_ylabel('p\_2', labelpad=15)
    ax1.set_zlabel(zLabel, labelpad=10)
    ax1.invert_xaxis()
# colorbar
    sc = cm.ScalarMappable(cmap=cmap, norm=norm)
    sc.set_array([])
    plt.colorbar(sc, pad=0.2)
    plt.savefig(filename, dpi=100, bbox_inches='tight')


def prepare3DFigure(filenamePattern, kString):
    allEvalFilenames = glob.glob(filenamePattern)
    data = []
    for filename in allEvalFilenames:
        evalData = pd.read_csv(filename, header=0, skipinitialspace=True)
        pString = filename.split(sep="_")[1]
        minProp1Val = float(pString.split(sep=",")[1])
        minProp2String = pString.split(sep=",")[2]
        minProp2Val = float(minProp2String[:-1])
        expGainValGroup1 = evalData.loc[evalData["group"] == 1, "expGain"].iloc[0]
        expGainValGroup2 = evalData.loc[evalData["group"] == 2, "expGain"].iloc[0]
        ndcgLossVal = evalData["ndcgLoss"].min()
        tempDict = dict(minProp1=minProp1Val, minProp2=minProp2Val, expGainGroup1=expGainValGroup1, expGainGroup2=expGainValGroup2, ndcgLoss=ndcgLossVal)
        data.append(tempDict)

    dataToPlot = pd.DataFrame(data, columns=["minProp1", "minProp2", "expGainGroup1", "expGainGroup2", "ndcgLoss"])
    dataToPlot.sort_values(["minProp1", "minProp2"], inplace=True)
    print(dataToPlot)

    plot3DFigure(dataToPlot["minProp1"].values,
                 dataToPlot["minProp2"].values,
                 dataToPlot["expGainGroup1"].values,
                 "Exposure Gain",
                 "../results/COMPAS/evalAndPlots/age/" + kString + "-barplot-expGainGroup1.png")

    plot3DFigure(dataToPlot["minProp1"].values,
                 dataToPlot["minProp2"].values,
                 dataToPlot["expGainGroup2"].values,
                 "Exposure Gain",
                 "../results/COMPAS/evalAndPlots/age/" + kString + "-barplot-expGainGroup2.png")

    plot3DFigure(dataToPlot["minProp1"].values,
                 dataToPlot["minProp2"].values,
                 dataToPlot["ndcgLoss"].values,
                 "NDCG loss",
                 "../results/COMPAS/evalAndPlots/age/" + kString + "-barplot-ndcgLoss.png")


def main():
#     visualizeOrigCompasData()
#     visualizeOrigGermanCreditData()
#     visualizeOrigLSATData()
    kString = "k=100"
    prepare3DFigure("../results/COMPAS/evalAndPlots/age/" + kString + "*_multiFairResult.csv", kString)

    kString = "k=200"
    prepare3DFigure("../results/COMPAS/evalAndPlots/age/" + kString + "*_multiFairResult.csv", kString)


if __name__ == '__main__':
    main()
