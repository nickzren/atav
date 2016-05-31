. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/coverage_summary/coverage_summary_coverage.details.csv $ATAV_OUTPUT_PATH/old/coverage_summary/coverage_summary_coverage.details.csv > $ATAV_DIFF_PATH/coverage.details.csv.diff

diff $ATAV_OUTPUT_PATH/new/coverage_summary/coverage_summary_sample.summary.csv $ATAV_OUTPUT_PATH/old/coverage_summary/coverage_summary_sample.summary.csv > $ATAV_DIFF_PATH/sample.summary.csv.diff
