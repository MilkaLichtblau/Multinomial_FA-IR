#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_GERMAN_CREDIT_DATA=$GIT_ROOT/experiments/dataExperiments/data/GermanCredit
PATH_TO_GERMAN_CREDIT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/GermanCredit/rankings

#baseline
echo "Baseline with CFA theta = 0"
cd $PATH_TO_CFA_ALGORITHM
python3 main.py germanCredit_sexAge 0.1 0,0,0,0,0,0 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge_CFA_theta=0.csv
echo "Baseline with CFA theta = 1"
python3 main.py germanCredit_sexAge 0.1 1 1,1,1,1,1,1 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge_CFA_theta=1.csv
