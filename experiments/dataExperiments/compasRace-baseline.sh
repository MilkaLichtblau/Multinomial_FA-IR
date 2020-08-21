#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings


cd $PATH_TO_CFA_ALGORITHM
#baseline
echo "Baseline with CFA theta = 0.5"
python3 main.py compas_race 0.1 0.5,0.5 $PATH_TO_COMPAS_RESULTS/race/compas_race_CFA_theta=05.csv
echo "Baseline with CFA theta = 1"
python3 main.py compas_race 0.1 1,1 $PATH_TO_COMPAS_RESULTS/race/compas_race_CFA_theta=1.csv
