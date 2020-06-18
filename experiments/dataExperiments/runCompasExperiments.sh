#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_JAR=$GIT_ROOT/algorithm
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

cd $PATH_TO_JAR
java -jar MultinomialFair.jar data $PATH_TO_COMPAS_DATA/compas_race_java.csv 500 0.5,0.5 0.1 $PATH_TO_COMPAS_RESULTS/compas_race
