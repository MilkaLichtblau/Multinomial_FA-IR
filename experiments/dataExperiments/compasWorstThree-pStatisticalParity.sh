#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

###########################################
# THREE WORST OFF 
###########################################
echo "#################################################"
echo "# STARTING COMPAS WORST OFF EXPERIMENTS"
echo "#################################################"
cd $PATH_TO_FAIR_JAR

# p is same percentages as in dataset
echo "Multi FAIR with p as statistical parity"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_worstThreeGroups_java.csv 100 0.826,0.028,0.012,0.134 0.1 $PATH_TO_COMPAS_RESULTS/worstThree/compas_worstThreeGroups
