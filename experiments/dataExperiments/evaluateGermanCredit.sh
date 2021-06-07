#!/bin/bash

GIT_ROOT="$(git rev-parse --show-toplevel)"
PATH_TO_EVAL_CODE=$GIT_ROOT/experiments/dataExperiments/src/
RANKINGS_DIR=$GIT_ROOT/experiments/dataExperiments/results/GermanCredit/rankings/
EVAL_DIR=$GIT_ROOT/experiments/dataExperiments/results/GermanCredit/evalAndPlots/
EXPERIMENT_NAMES=""

cd $PATH_TO_EVAL_CODE
echo "Eval German Credit"
python3 evaluateResults.py $RANKINGS_DIR $EVAL_DIR $EXPERIMENT_NAMES
