#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/cfa/src
PATH_TO_DICEROLL_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/diceroll
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings/worstThree/diceroll

#create path if not exists
mkdir -p $PATH_TO_COMPAS_RESULTS

k=300
minpropsParity=0.826,0.028,0.012,0.134
minpropsEqual=0.25,0.25,0.25,0.25

cd $PATH_TO_CFA_ALGORITHM
#baseline
echo "Baseline with CFA theta = 1"
python3 main.py compas_worstThreeGroups 0.005 1,1,1,1 $PATH_TO_COMPAS_RESULTS/compas_worstThreeGroups_CFA_theta=1.csv

# Dice Roll baseline
cd $PATH_TO_DICEROLL_ALGORITHM
echo "Baseline dice roll with biased dice"
#python3 main.py $PATH_TO_COMPAS_DATA/compas_worstThreeGroups_java.csv $minpropsParity $k $PATH_TO_COMPAS_RESULTS/compas_worstThreeGroups_dice_k="$k"_p=[$minpropsParity].csv
echo "Baseline dice roll with unbiased dice"
python3 main.py $PATH_TO_COMPAS_DATA/compas_worstThreeGroups_java.csv $minpropsEqual $k $PATH_TO_COMPAS_RESULTS/compas_worstThreeGroups_dice_k="$k"_p=[$minpropsEqual].csv
