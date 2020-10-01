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
    ax1.set_xlabel('$p_1$', labelpad=15)
    ax1.set_ylabel('$p_2$', labelpad=15)
    ax1.set_zlabel(zLabel, labelpad=10)
    ax1.invert_xaxis()
# colorbar
    sc = cm.ScalarMappable(cmap=cmap, norm=norm)
    sc.set_array([])
    plt.colorbar(sc, pad=0.2)
    plt.savefig(filename, dpi=100, bbox_inches='tight')


def prepareAndSaveFigures(filenamePattern, kString):
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
        expGainGroupNPVal = evalData.loc[evalData["group"] == 0, "expGain"].iloc[0]
        ndcgLossVal = evalData["ndcgLoss"].min()
        tempDict = dict(minProp1=minProp1Val,
                        minProp2=minProp2Val,
                        expGainGroupNP=expGainGroupNPVal,
                        expGainGroup1=expGainValGroup1,
                        expGainGroup2=expGainValGroup2,
                        ndcgLoss=ndcgLossVal)
        data.append(tempDict)

    dataToPlot = pd.DataFrame(data, columns=["minProp1", "minProp2", "expGainGroupNP", "expGainGroup1", "expGainGroup2", "ndcgLoss"])
    dataToPlot.sort_values(["minProp1", "minProp2"], inplace=True)
    # normalize exposure data
#     minGain = dataToPlot[["expGainGroup1", "expGainGroup2"]].min().min()
    maxGain = dataToPlot[["expGainGroup1", "expGainGroup2", "expGainGroupNP"]].max().max()
#     dataToPlot["expGainGroup1"] = (dataToPlot["expGainGroup1"] - minGain) / (maxGain - minGain)
#     dataToPlot["expGainGroup2"] = (dataToPlot["expGainGroup2"] - minGain) / (maxGain - minGain)
    dataToPlot["expGainGroup1"] = dataToPlot["expGainGroup1"] / maxGain
    dataToPlot["expGainGroup2"] = dataToPlot["expGainGroup2"] / maxGain
    dataToPlot["expGainGroupNP"] = dataToPlot["expGainGroupNP"] / maxGain

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

    plotHeatmap(dataToPlot,
                "expGainGroupNP",
                "Exposure Gain",
                 "../results/COMPAS/evalAndPlots/age/" + kString + "-heatmap-expGainGroupNP.png",
                 valfmt="{x:.3f}")
    plotHeatmap(dataToPlot,
                "expGainGroup1",
                "Exposure Gain",
                 "../results/COMPAS/evalAndPlots/age/" + kString + "-heatmap-expGainGroup1.png",
                 valfmt="{x:.3f}")
    plotHeatmap(dataToPlot,
                "expGainGroup2",
                "Exposure Gain",
                 "../results/COMPAS/evalAndPlots/age/" + kString + "-heatmap-expGainGroup2.png",
                 valfmt="{x:.3f}")
    plotHeatmap(dataToPlot,
                "ndcgLoss",
                "NDCG Loss",
                "../results/COMPAS/evalAndPlots/age/" + kString + "-heatmap-ndcgLoss.png",
                valfmt="{x:.3f}")


def plotHeatmap(dataToPlot, heatmapValueStr, cbarlabel, filename, **annotationKW):
    heatmapFrame = pd.DataFrame(index=dataToPlot["minProp1"].unique(),
                                columns=dataToPlot["minProp2"].unique(),
                                dtype=np.float64)

    for colName, colContent in heatmapFrame.iteritems():
        for rowName in colContent.index:
            val = dataToPlot.loc[((dataToPlot["minProp1"] == colName) & (dataToPlot["minProp2"] == rowName)), heatmapValueStr].values
            if len(val) == 0:
                heatmapFrame.at[rowName, colName] = np.nan
            else:
                heatmapFrame.at[rowName, colName] = val[0]

    fig = plt.figure(figsize=(11, 11))
    ax = fig.add_subplot(111)
    im = heatmap(heatmapFrame, heatmapFrame.index, heatmapFrame.columns, ax=ax,
                    cmap="coolwarm", cbarlabel=cbarlabel, vmin=-1, vmax=1)
    annotate_heatmap(im, thresholds=[-0.8, 0.8], **annotationKW)
    plt.savefig(filename, dpi=100, bbox_inches='tight')


def heatmap(data, row_labels, col_labels, ax=None,
            cbar_kw={}, cbarlabel="", **kwargs):
    """
    Create a heatmap from a numpy array and two lists of labels.

    Parameters
    ----------
    data
        A 2D numpy array of shape (N, M).
    row_labels
        A list or array of length N with the labels for the rows.
    col_labels
        A list or array of length M with the labels for the columns.
    ax
        A `matplotlib.axes.Axes` instance to which the heatmap is plotted.  If
        not provided, use current axes or create a new one.  Optional.
    cbar_kw
        A dictionary with arguments to `matplotlib.Figure.colorbar`.  Optional.
    cbarlabel
        The label for the colorbar.  Optional.
    **kwargs
        All other arguments are forwarded to `imshow`.
    """

    mpl.rcParams.update({'font.size':24, 'lines.linewidth':3, 'lines.markersize':15, 'font.family':'Times New Roman'})  # avoid type 3 (i.e. bitmap) fonts in figures
    mpl.rcParams['ps.useafm'] = True
    mpl.rcParams['pdf.use14corefonts'] = True
    mpl.rcParams['text.usetex'] = True

    if not ax:
        ax = plt.gca()

    # handle nans
    current_cmap = mpl.cm.get_cmap()
    current_cmap.set_bad(color='w')

    # Plot the heatmap
    im = ax.imshow(data, **kwargs)

    # Create colorbar
#     cbar = ax.figure.colorbar(im, ax=ax, **cbar_kw)
#     cbar.ax.set_ylabel(cbarlabel, rotation=-90, va="bottom")

    # We want to show all ticks...
    ax.set_xticks(np.arange(data.shape[1]))
    ax.set_yticks(np.arange(data.shape[0]))
    # ... and label them with the respective list entries.
    ax.set_xticklabels(col_labels)
    ax.set_yticklabels(row_labels)

    # Let the horizontal axes labeling appear on top.
    # ax.tick_params(top=True, bottom=False,
    #               labeltop=True, labelbottom=False)

    # Rotate the tick labels and set their alignment.
#     plt.setp(ax.get_xticklabels(), rotation=-30, ha="right",
#              rotation_mode="anchor")

    # Turn spines off and create white grid.
    for _, spine in ax.spines.items():
        spine.set_visible(False)

    ax.set_xticks(np.arange(data.shape[1] + 1) - .5, minor=True)
    ax.set_yticks(np.arange(data.shape[0] + 1) - .5, minor=True)
    ax.grid(which="minor", color="w", linestyle='-', linewidth=3)
    ax.tick_params(which="minor", bottom=False, left=False)

    ax.invert_yaxis()

    ax.set_xlabel('$p_{1}$', labelpad=15)
    ax.set_ylabel('$p_{2}$', labelpad=15)

    return im  # , cbar


def annotate_heatmap(im, data=None, valfmt="{x:.2f}",
                     textcolors=["black", "white"],
                     thresholds=[0, 1],
                     ** textkw):
    """
    A function to annotate a heatmap.

    Parameters
    ----------
    im
        The AxesImage to be labeled.
    data
        Data used to annotate.  If None, the image's data is used.  Optional.
    valfmt
        The format of the annotations inside the heatmap.  This should either
        use the string format method, e.g. "$ {x:.2f}", or be a
        `matplotlib.ticker.Formatter`.  Optional.
    textcolors
        A list or array of two color specifications.  The first is used for
        values below a threshold, the second for those above.  Optional.
    threshold
        Value in data units according to which the colors from textcolors are
        applied.  If None (the default) uses the middle of the colormap as
        separation.  Optional.
    **kwargs
        All other arguments are forwarded to each call to `text` used to create
        the text labels.
    """

    if not isinstance(data, (list, np.ndarray)):
        data = im.get_array()

    # Set default alignment to center, but allow it to be
    # overwritten by textkw.
    kw = dict(horizontalalignment="center",
              verticalalignment="center")
    kw.update(textkw)

    # Get the formatter in case a string is supplied
    if isinstance(valfmt, str):
        valfmt = mpl.ticker.StrMethodFormatter(valfmt)

    # Loop over the data and create a `Text` for each "pixel".
    # Change the text's color depending on the data.
    texts = []

    # have white text color in masked fields
    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            if data[i, j] in data[data.mask]:
                continue
            kw.update(color=textcolors[int((thresholds[0] > data[i, j]) or (data[i, j] > thresholds[1]))])
            text = im.axes.text(j, i, valfmt(data[i, j], None), **kw)
            texts.append(text)

    return texts


def main():
#     visualizeOrigCompasData()
#     visualizeOrigGermanCreditData()
#     visualizeOrigLSATData()

    kString = "k=200"
    prepareAndSaveFigures("../results/COMPAS/evalAndPlots/age/" + kString + "*_multiFairResult.csv", kString)


if __name__ == '__main__':
    main()
