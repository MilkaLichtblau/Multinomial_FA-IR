#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_LSAT_DATA=$GIT_ROOT/experiments/dataExperiments/data/LSAT
PATH_TO_LSAT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/LSAT/rankings

# SEX (Male, Female), RACE (White, Non-White)
echo "#################################################"
echo "# STARTING LSAT EXPERIMENTS"
echo "#################################################"
cd $PATH_TO_JAR

# p is equal for all
echo "Multi FAIR with p all equal"
java -jar Multinomial_FA-IR.jar data $PATH_TO_LSAT_DATA/LSAT_sexRace_java.csv 300 0.25,0.25,0.25,0.25 0.1 $PATH_TO_LSAT_RESULTS/LSAT_sexRace


