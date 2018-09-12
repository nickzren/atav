. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/trio --list-trio --min-coverage 10 --filter PASS --ctrl-af 0.01 --ccds-only --evs-maf 0.1 --run-tier

atav_prod.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/trio --list-trio --min-coverage 10 --filter PASS --ctrl-af 0.01 --ccds-only --evs-maf 0.1 --run-tier