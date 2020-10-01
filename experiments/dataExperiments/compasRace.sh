#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

K=1500

echo "#################################################"
echo "# STARTING COMPAS RACE EXPERIMENTS"
echo "#################################################"
###################################################################
# RACE (White, Non-White)
#####################################################################
cd $PATH_TO_FAIR_JAR

# p is same percentages as in dataset
echo "Multi FAIR with p as statistical parity"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_race_java.csv $K 0.34,0.66 0.1 $PATH_TO_COMPAS_RESULTS/race/compas_race

# p is all the same
echo "Multi FAIR with p all equal"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_race_java.csv $K 0.5,0.5 0.1 $PATH_TO_COMPAS_RESULTS/race/compas_race

