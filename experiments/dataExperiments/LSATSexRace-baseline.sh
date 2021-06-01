#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/cfa/src
PATH_TO_DICEROLL_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/baselines/diceroll
PATH_TO_LSAT_DATA=$GIT_ROOT/experiments/dataExperiments/data/LSAT
PATH_TO_LSAT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/LSAT/rankings/diceroll

#create path if not exists
mkdir -p $PATH_TO_LSAT_RESULTS

k=300
minpropsParity=0.49,0.35,0.084,0.076
minpropsEqual=0.25,0.25,0.25,0.25

#baseline
cd $PATH_TO_CFA_ALGORITHM
echo "Baseline with CFA theta = 1"
python3 main.py LSAT_sexRace 1 1,1,1,1 $PATH_TO_LSAT_RESULTS/LSAT_sexRace_CFA_theta=1.csv

# Dice Roll baseline
cd $PATH_TO_DICEROLL_ALGORITHM
echo "Baseline dice roll with biased dice"
python3 main.py $PATH_TO_LSAT_DATA/LSAT_sexRace_java.csv $minpropsParity $k $PATH_TO_LSAT_RESULTS/LSAT_sexRace_dice_k="$k"_p=[$minpropsParity].csv
echo "Baseline dice roll with unbiased dice"
python3 main.py $PATH_TO_LSAT_DATA/LSAT_sexRace_java.csv $minpropsEqual $k $PATH_TO_LSAT_RESULTS/LSAT_sexRace_dice_k="$k"_p=[$minpropsEqual].csv
