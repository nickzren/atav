. atav_config_paths.sh

atav_beta.sh --site-coverage-comparison --sample $ATAV_TEST_PATH/ped --gene-boundary $ATAV_TEST_PATH/coverage_region --out $ATAV_OUTPUT_PATH/new/site_coverage_comparison --min-coverage 10

atav.sh --site-coverage-comparison --sample $ATAV_TEST_PATH/ped --gene-boundary $ATAV_TEST_PATH/coverage_region --out $ATAV_OUTPUT_PATH/old/site_coverage_comparison --min-coverage 10