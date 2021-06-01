#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/cfa/src
PATH_TO_DICEROLL_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/diceroll
PATH_TO_GERMAN_CREDIT_DATA=$GIT_ROOT/experiments/dataExperiments/data/GermanCredit
PATH_TO_GERMAN_CREDIT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/GermanCredit/rankings/diceroll

#create path if not exists
mkdir -p $PATH_TO_GERMAN_CREDIT_RESULTS

k=50
minpropsParity=0.58,0.22,0.065,0.045,0.03,0.06
minpropsEqual=0.16665,0.16667,0.16667,0.16667,0.16667,0.16667

#baseline
cd $PATH_TO_CFA_ALGORITHM
echo "Baseline with CFA theta = 1"
python3 main.py germanCredit_sexAge 0.1 1,1,1,1,1,1 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge_CFA_theta=1.csv

# Dice Roll baseline
cd $PATH_TO_DICEROLL_ALGORITHM
echo "Baseline dice roll with biased dice"
python3 main.py $PATH_TO_GERMAN_CREDIT_DATA/germanCredit_sexAge_java.csv $minpropsParity $k $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge_dice_k="$k"_p=[$minpropsParity].csv
echo "Baseline dice roll with unbiased dice"
python3 main.py $PATH_TO_GERMAN_CREDIT_DATA/germanCredit_sexAge_java.csv $minpropsEqual $k $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge_dice_k="$k"_p=[$minpropsEqual].csv
