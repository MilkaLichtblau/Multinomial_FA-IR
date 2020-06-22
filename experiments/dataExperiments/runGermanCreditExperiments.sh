#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_GERMAN_CREDIT_DATA=$GIT_ROOT/experiments/dataExperiments/data/GermanCredit
PATH_TO_GERMAN_CREDIT_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/GermanCredit/rankings

# SEX (Male, Female), AGE (100 youngest and 100 oldest are protected)
cd $PATH_TO_JAR

# p matches percentages of groups in dataset (statistical parity)
java -jar MultinomialFair.jar data $PATH_TO_GERMAN_CREDIT_DATA/germanCredit_sexAge_java.csv 500 0.58,0.22,0.06,0.06,0.04,0.03 0.1 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge

# p is equal for all
java -jar MultinomialFair.jar data $PATH_TO_GERMAN_CREDIT_DATA/germanCredit_sexAge_java.csv 500 0.16,0.16,0.16,0.16,0.16,0.16 0.1 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge

# p is something in between
java -jar MultinomialFair.jar data $PATH_TO_GERMAN_CREDIT_DATA/germanCredit_sexAge_java.csv 500 0.37,0.19,0.12,0.12,0.104,0.096 0.1 $PATH_TO_GERMAN_CREDIT_RESULTS/germanCredit_sexAge
