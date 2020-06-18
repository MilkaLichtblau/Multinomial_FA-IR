#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_GERMAN_CREDIT_DATA=$GIT_ROOT/experiments/dataExperiments/data/GermanCredit
PATH_TO_GERMAN_CREDIT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/GermanCredit/rankings

cd $PATH_TO_JAR
java -jar MultinomialFair.jar data $PATH_TO_GERMAN_CREDIT_DATA/germanCredit_age_java.csv 500 0.5,0.5 0.1 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_age
