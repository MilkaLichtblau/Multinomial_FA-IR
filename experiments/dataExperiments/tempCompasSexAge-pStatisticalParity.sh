#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

###########################################
# SEX, AGE
###########################################
echo "#################################################"
echo "# STARTING COMPAS SEX AGE EXPERIMENTS"
echo "#################################################"
cd $PATH_TO_FAIR_JAR

# p is same percentages as in dataset
echo "Multi FAIR with p as statistical parity"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_sexAge_java.csv 500 0.46,0.17,0.04,0.11,0.18,0.04 0.1 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge
