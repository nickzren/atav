. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/site_coverage_comparison/site_coverage_comparison_coverage.summary.clean.csv $ATAV_OUTPUT_PATH/old/site_coverage_comparison/site_coverage_comparison_coverage.summary.clean.csv > $ATAV_DIFF_PATH/coverage.summary.clean.csv.diff

diff $ATAV_OUTPUT_PATH/new/site_coverage_comparison/site_coverage_comparison_coverage.summary.csv $ATAV_OUTPUT_PATH/old/site_coverage_comparison/site_coverage_comparison_coverage.summary.csv > $ATAV_DIFF_PATH/coverage.summary.csv.diff

diff $ATAV_OUTPUT_PATH/new/site_coverage_comparison/site_coverage_comparison_site.clean.txt $ATAV_OUTPUT_PATH/old/site_coverage_comparison/site_coverage_comparison_site.clean.txt > $ATAV_DIFF_PATH/site.clean.txt.diff

diff $ATAV_OUTPUT_PATH/new/site_coverage_comparison/site_coverage_comparison_sample.summary.csv $ATAV_OUTPUT_PATH/old/site_coverage_comparison/site_coverage_comparison_sample.summary.csv > $ATAV_DIFF_PATH/sample.summary.csv.diff