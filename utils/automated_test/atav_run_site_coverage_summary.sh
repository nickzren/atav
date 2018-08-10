. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --site-coverage-summary --sample $ATAV_TEST_PATH/trio_sample.txt --gene-boundary $ATAV_TEST_PATH/coverage_region --out $ATAV_OUTPUT_PATH/new/site_coverage_summary --min-coverage 10

atav_prod.sh --disable-timestamp-from-out-path --site-coverage-summary --sample $ATAV_TEST_PATH/trio_sample.txt --gene-boundary $ATAV_TEST_PATH/coverage_region --out $ATAV_OUTPUT_PATH/old/site_coverage_summary --min-coverage 10
 
