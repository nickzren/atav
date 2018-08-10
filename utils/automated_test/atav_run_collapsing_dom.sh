. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/collapsing_dom --collapsing-dom --min-coverage 10 --filter PASS --ccds-only --evs-maf 0.1 --loo-af 0.01

atav_prod.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/collapsing_dom --collapsing-dom --min-coverage 10 --filter PASS --ccds-only --evs-maf 0.1 --loo-af 0.01
