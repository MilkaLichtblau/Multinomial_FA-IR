#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_LSAT_DATA=$GIT_ROOT/experiments/dataExperiments/data/LSAT
PATH_TO_LSAT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/LSAT/rankings

#baseline
cd $PATH_TO_CFA_ALGORITHM
echo "Baseline with CFA theta = 0.5"
python3 main.py LSAT_sexRace 0.1 0.5,0.5,0.5,0.5 $PATH_TO_LSAT_RESULTS/LSAT_sexRace_CFA_theta=05.csv
echo "Baseline with CFA theta = 1"
python3 main.py LSAT_sexRace 0.1 1 1,1,1,1 $PATH_TO_LSAT_RESULTS/LSAT_sexRace_CFA_theta=1.csv
