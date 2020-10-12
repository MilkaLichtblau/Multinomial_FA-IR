#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_GERMAN_CREDIT_DATA=$GIT_ROOT/experiments/dataExperiments/data/GermanCredit
PATH_TO_GERMAN_CREDIT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/GermanCredit/rankings

# SEX (Male, Female), AGE (100 youngest and 100 oldest are protected)
echo "#################################################"
echo "# STARTING GERMAN CREDIT EXPERIMENTS"
echo "#################################################"
cd $PATH_TO_JAR

# p matches percentages of groups in dataset (statistical parity)
echo "Multi FAIR with p as statistical parity"
java -jar Multinomial_FA-IR.jar data $PATH_TO_GERMAN_CREDIT_DATA/germanCredit_sexAge_java.csv 75 0.58,0.22,0.065,0.045,0.03,0.06 0.1 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge
