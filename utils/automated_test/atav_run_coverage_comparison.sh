. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --coverage-comparison --sample $ATAV_TEST_PATH/trio_sample.txt --gene-boundary $ATAV_TEST_PATH/gene_boundaries --min-coverage 10 --out $ATAV_OUTPUT_PATH/new/coverage_comparison

atav_prod.sh --disable-timestamp-from-out-path --coverage-comparison --sample $ATAV_TEST_PATH/trio_sample.txt --gene-boundary $ATAV_TEST_PATH/gene_boundaries --min-coverage 10 --out $ATAV_OUTPUT_PATH/old/coverage_comparison
