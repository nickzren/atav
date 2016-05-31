. atav_config_paths.sh

atav_beta.sh --sample $ATAV_TEST_PATH/linear_sample --function $ATAV_TEST_PATH/function --quantitative $ATAV_TEST_PATH/quantitative --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/linear --linear --min-coverage 10 --var-status pass --ctrl-maf 0.3 --ccds-only --evs-maf 0.1

atav.sh --sample $ATAV_TEST_PATH/linear_sample --function $ATAV_TEST_PATH/function --quantitative $ATAV_TEST_PATH/quantitative --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/linear --linear --min-coverage 10 --var-status pass --ctrl-maf 0.3 --ccds-only --evs-maf 0.1
