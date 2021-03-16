#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/cfa/src
PATH_TO_DICEROLL_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/diceroll
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings/race

k=1500
minpropsParity=0.34,0.66
minpropsEqual=0.5,0.5

cd $PATH_TO_CFA_ALGORITHM
#baseline
echo "Baseline with CFA theta = 1"
python3 main.py compas_race 0.005 1,1 $PATH_TO_COMPAS_RESULTS/compas_race_CFA_theta=1.csv

# Dice Roll baseline
cd $PATH_TO_DICEROLL_ALGORITHM
echo "Baseline dice roll with biased dice"
python3 main.py $PATH_TO_COMPAS_DATA/compas_race_java.csv $minpropsParity $k $PATH_TO_COMPAS_RESULTS/compas_race_dice_k="$k"_p=[$minpropsParity].csv
echo "Baseline dice roll with unbiased dice"
python3 main.py $PATH_TO_COMPAS_DATA/compas_race_java.csv $minpropsEqual $k $PATH_TO_COMPAS_RESULTS/compas_race_dice_k="$k"_p=[$minpropsEqual].csv
