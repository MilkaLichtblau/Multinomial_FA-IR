#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_FAIR_JAR=$GIT_ROOT/algorithm
PATH_TO_CFA_ALGORITHM=$GIT_ROOT/experiments/dataExperiments/cfa/src
PATH_TO_COMPAS_DATA=$GIT_ROOT/experiments/dataExperiments/data/COMPAS
PATH_TO_COMPAS_RESULTS=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings


###################################################################
# AGE (younger than 25 (P1), 25-45 (NP), older than 45 (P2)), RACE (White, Non-White)
#####################################################################
cd $PATH_TO_FAIR_JAR

# p is same percentages as in dataset
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_ageRace_java.csv 500 0.18,0.11,0.1,0.39,0.06,0.16 0.1 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace

# p is all the same
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_ageRace_java.csv 500 0.16,0.16,0.16,0.16,0.16,0.16 0.1 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace

cd $PATH_TO_CFA_ALGORITHM
#baseline
python3 main.py compas_ageRace 0.05 0,0,0,0,0,0 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace_CFA_theta=0.csv
python3 main.py compas_ageRace 0.05 1,1,1,1,1,1 $PATH_TO_COMPAS_RESULTS/ageRace/compas_ageRace_CFA_theta=1.csv

#################################################ü
# SEX (male, female), RACE (White, Non-White)
###################################################
cd $PATH_TO_FAIR_JAR
# p is same percentages as in dataset
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_sexRace_java.csv 500 0.26,0.55,0.08,0.11 0.1 $PATH_TO_COMPAS_RESULTS/sexRace/compas_sexRace

#p is all the same
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_sexRace_java.csv 500 0.25,0.25,0.25,0.25 0.1 $PATH_TO_COMPAS_RESULTS/sexRace/compas_sexRace

cd $PATH_TO_CFA_ALGORITHM
#baseline
python3 main.py compas_sexRace 0.05 0,0,0,0 $PATH_TO_COMPAS_RESULTS/sexRace/compas_sexRace_CFA_theta=0.csv
python3 main.py compas_sexRace 0.05 1,1,1,1 $PATH_TO_COMPAS_RESULTS/sexRace/compas_sexRace_CFA_theta=1.csv

###########################################
# SEX, AGE
###########################################
cd $PATH_TO_FAIR_JAR
# p is same percentages as in dataset
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_sexAge_java.csv 500 0.46,0.17,0.04,0.11,0.18,0.04 0.1 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge

#p is all the same
java -jar Multinomial_FA-IR.jar data $PATH_TO_COMPAS_DATA/compas_sexAge_java.csv 500 0.16,0.16,0.16,0.16,0.16,0.16 0.1 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge

cd $PATH_TO_CFA_ALGORITHM
#baseline
python3 main.py compas_sexAge 0.05 0,0,0,0,0,0 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge_CFA_theta=0.csv
python3 main.py compas_sexAge 0.05 1,1,1,1,1,1 $PATH_TO_COMPAS_RESULTS/sexAge/compas_sexAge_CFA_theta=1.csv


