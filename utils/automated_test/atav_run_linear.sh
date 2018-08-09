. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/linear_sample --effect $ATAV_TEST_PATH/function --quantitative $ATAV_TEST_PATH/quantitative --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/linear --linear --min-coverage 10 --filter PASS --ctrl-af 0.01 --ccds-only --evs-maf 0.1

atav_latest.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/linear_sample --effect $ATAV_TEST_PATH/function --quantitative $ATAV_TEST_PATH/quantitative --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/linear --linear --min-coverage 10 --filter PASS --ctrl-af 0.01 --ccds-only --evs-maf 0.1
