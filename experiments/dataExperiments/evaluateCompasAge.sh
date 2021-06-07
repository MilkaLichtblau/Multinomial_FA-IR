#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_EVAL_CODE=$GIT_ROOT/experiments/dataExperiments/src/
RANKINGS_DIR=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/rankings/
EVAL_DIR=$GIT_ROOT/experiments/dataExperiments/results/COMPAS/evalAndPlots/
EXPERIMENT_NAMES="age"

cd $PATH_TO_EVAL_CODE
echo "Eval Compas Age"
python3 evaluateResults.py $RANKINGS_DIR $EVAL_DIR $EXPERIMENT_NAMES
