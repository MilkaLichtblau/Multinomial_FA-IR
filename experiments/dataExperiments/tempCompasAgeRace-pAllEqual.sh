#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

cd $PATH_TO_FAIR_JAR

# p is all the same
echo "Multi FAIR with p all equal"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_ageRace_java.csv 500 0.16,0.16,0.16,0.16,0.16,0.16 0.1 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace

cd $PATH_TO_CFA_ALGORITHM
#baseline
echo "Baseline with CFA theta = 0"
python3 main.py compas_ageRace 0.05 0,0,0,0,0,0 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace_CFA_theta=0.csv
echo "Baseline with CFA theta = 1"
python3 main.py compas_ageRace 0.05 1,1,1,1,1,1 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace_CFA_theta=1.csv
