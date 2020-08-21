#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

echo "#################################################"
echo "# STARTING COMPAS AGE EXPERIMENTS"
echo "#################################################"
###################################################################
# AGE (younger than 25 (P1), 25-45 (P2), older than 45 (NP)), 
#####################################################################
cd $PATH_TO_FAIR_JAR

# p is same percentages as in dataset
echo "Multi FAIR with p as statistical parity"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 500 0.209,0.573,0.218 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age


