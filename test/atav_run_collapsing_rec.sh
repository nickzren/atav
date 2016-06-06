. atav_config_paths.sh

atav_beta.sh --sample $ATAV_TEST_PATH/ped --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/collapsing_rec --collapsing-rec --min-coverage 10 --var-status pass --ccds-only --evs-maf 0.1

atav.sh --sample $ATAV_TEST_PATH/ped --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/collapsing_rec --collapsing-rec --min-coverage 10 --var-status pass --ccds-only --evs-maf 0.10
