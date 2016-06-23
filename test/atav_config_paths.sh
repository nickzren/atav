#!/bin/sh

#project folder
export ATAV_HOME=/nfs/goldstein/software/atav_home

#folder to test files (functions, samples etc)
export ATAV_TEST_PATH=$ATAV_HOME/test

#output folder (subfolders 'new' and 'old')
export ATAV_OUTPUT_PATH=$ATAV_HOME/test/output
#export ATAV_OUTPUT_PATH=~/output

#folder that contains .diff files
export ATAV_DIFF_PATH=~/Desktop

#folder that contains variant data
export ATAV_DATA_PATH=$ATAV_HOME/data

#folder that contains atav_beta_test.py file
#export ATAV_COMP_SCRIPT_PATH=$ATAV_HOME/test/output
export ATAV_COMP_SCRIPT_PATH=.
