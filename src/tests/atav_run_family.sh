. atav_config_paths.sh

atav_beta.sh --sample $ATAV_TEST_PATH/alsfamilies_allctrls.txt --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/family --family-analysis --family-id $ATAV_TEST_PATH/families.txt --min-coverage 10 --var-status pass --ccds-only --evs-maf 0.1

atav.sh --sample $ATAV_TEST_PATH/alsfamilies_allctrls.txt --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/family --family-analysis --family-id $ATAV_TEST_PATH/families.txt --min-coverage 10 --var-status pass --ccds-only --evs-maf 0.1
