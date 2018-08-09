. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/list_var_geno --list-var-geno --min-coverage 10 --filter PASS --ctrl-af 0.01 --ccds-only --evs-maf 0.1

atav_latest.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/list_var_geno --list-var-geno --min-coverage 10 --filter PASS --ctrl-af 0.01 --ccds-only --evs-maf 0.1
