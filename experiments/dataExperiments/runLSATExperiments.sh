#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_LSAT_DATA=$GIT_ROOT/experiments/dataExperiments/data/LSAT
PATH_TO_LSAT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/LSAT/rankings

# SEX (Male, Female), RACE (White, Non-White)
cd $PATH_TO_JAR
# p matches percentages of groups in dataset (statistical parity)
java -jar MultinomialFair.jar data $PATH_TO_LSAT_DATA/LSAT_sexRace_java.csv 2000 0.49,0.35,0.084,0.076 0.1 $PATH_TO_LSAT_RESULTS/LSAT_sexRace

# p is equal for all
java -jar MultinomialFair.jar data $PATH_TO_LSAT_DATA/LSAT_sexRace_java.csv 2000 0.25,0.25,0.25,0.25 0.1 $PATH_TO_LSAT_RESULTS/LSAT_sexRace

cd $PATH_TO_CFA_ALGORITHM
python3 main.py LSAT_sexRace 0.1 0,0,0,0,0,0 $PATH_TO_LSAT_RESULTS/LSAT_sexRace_CFA_theta=0.csv
python3 main.py LSAT_sexRace 0.1 1 1,1,1,1,1,1 $PATH_TO_LSAT_RESULTS/LSAT_sexRace_CFA_theta=1.csv