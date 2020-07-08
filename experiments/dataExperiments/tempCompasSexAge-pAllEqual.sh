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

#p is all the same
echo "Multi FAIR with p all equal"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_sexAge_java.csv 500 1.0/6.0,1.0/6.0,1.0/6.0,1.0/6.0,1.0/6.0,1.0/6.0 0.1 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge

cd $PATH_TO_CFA_ALGORITHM
#baseline
echo "Baseline with CFA theta = 0"
python3 main.py compas_sexAge 0.05 0,0,0,0,0,0 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge_CFA_theta=0.csv
echo "Baseline with CFA theta = 1"
python3 main.py compas_sexAge 0.05 1,1,1,1,1,1 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge_CFA_theta=1.csv
