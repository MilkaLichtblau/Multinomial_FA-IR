#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

echo "#################################################"
echo "# STARTING COMPAS AGE RACE EXPERIMENTS"
echo "#################################################"
###################################################################
# AGE (younger than 25 (P1), 25-45 (NP), older than 45 (P2)), RACE (White, Non-White)
#####################################################################
cd $PATH_TO_FAIR_JAR

# p is same percentages as in dataset
echo "Multi FAIR with p as statistical parity"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_ageRace_java.csv 500 0.18,0.11,0.1,0.39,0.06,0.16 0.1 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace


