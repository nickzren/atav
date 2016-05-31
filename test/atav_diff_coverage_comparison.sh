. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/coverage_comparison/coverage_comparison_coverage.summary.by.exon.csv $ATAV_OUTPUT_PATH/old/coverage_comparison/coverage_comparison_coverage.summary.by.exon.csv > $ATAV_DIFF_PATH/coverage.summary.by.exon.csv.diff

diff $ATAV_OUTPUT_PATH/new/coverage_comparison/coverage_comparison_coverage.summary.clean.csv $ATAV_OUTPUT_PATH/old/coverage_comparison/coverage_comparison_coverage.summary.clean.csv > $ATAV_DIFF_PATH/coverage.summary.clean.csv.diff

diff $ATAV_OUTPUT_PATH/new/coverage_comparison/coverage_comparison_coverage.summary.csv $ATAV_OUTPUT_PATH/old/coverage_comparison/coverage_comparison_coverage.summary.csv > $ATAV_DIFF_PATH/coverage.summary.csv.diff

diff $ATAV_OUTPUT_PATH/new/coverage_comparison/coverage_comparison_exon.clean.txt $ATAV_OUTPUT_PATH/old/coverage_comparison/coverage_comparison_exon.clean.txt > $ATAV_DIFF_PATH/exon.clean.txt.diff

diff $ATAV_OUTPUT_PATH/new/coverage_comparison/coverage_comparison_sample.summary.csv $ATAV_OUTPUT_PATH/old/coverage_comparison/coverage_comparison_sample.summary.csv > $ATAV_DIFF_PATH/sample.summary.csv.diff

