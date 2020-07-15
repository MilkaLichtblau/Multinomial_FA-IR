#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

echo "#################################################"
echo "# STARTING COMPAS SEX RACE EXPERIMENTS"
echo "#################################################"
#################################################ü
# SEX (male, female), RACE (White, Non-White)
###################################################
cd $PATH_TO_FAIR_JAR

#p is all the same
echo "Multi FAIR with p all equal"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_sexRace_java.csv 500 0.25,0.25,0.25,0.25 0.1 $PATH_TO_COMPAS_RESULTS/sexRace/compas_sexRace


