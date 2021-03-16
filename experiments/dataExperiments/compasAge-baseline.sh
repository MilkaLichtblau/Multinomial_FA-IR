#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/cfa/src
PATH_TO_DICEROLL_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/diceroll
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings/age

k=500
minpropsParity=0.209,0.573,0.218
minpropsEqual=0.33333,0.33333,0.33334

cd $PATH_TO_CFA_ALGORITHM
#CFA baseline
echo "Baseline with CFA theta = 1"
python3 main.py compas_age 0.005 1,1,1 $PATH_TO_COMPAS_RESULTS/compas_age_CFA_theta=1.csv

# Dice Roll baseline
cd $PATH_TO_DICEROLL_ALGORITHM
echo "Baseline dice roll with biased dice"
python3 main.py $PATH_TO_COMPAS_DATA/compas_age_java.csv $minpropsParity $k $PATH_TO_COMPAS_RESULTS/compas_age_dice_k="$k"_p=[$minpropsParity].csv
echo "Baseline dice roll with unbiased dice"
python3 main.py $PATH_TO_COMPAS_DATA/compas_age_java.csv $minpropsEqual $k $PATH_TO_COMPAS_RESULTS/compas_age_dice_k="$k"_p=[$minpropsEqual].csv
