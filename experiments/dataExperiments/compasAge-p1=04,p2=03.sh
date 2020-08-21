#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

cd $PATH_TO_FAIR_JAR

# p is all the same
echo "Multi FAIR with pp1=0.4 and p2=0.3"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.3,0.4,0.3 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age


