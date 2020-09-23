#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings

cd $PATH_TO_FAIR_JAR

echo "Multi FAIR with p1=0.1 and all possible p2"
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.8,0.1,0.1 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.7,0.1,0.2 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.6,0.1,0.3 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.5,0.1,0.4 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.4,0.1,0.5 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.3,0.1,0.6 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.2,0.1,0.7 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_age_java.csv 100 0.1,0.1,0.8 0.1 $PATH_TO_COMPAS_RESULTS/age/compas_age


