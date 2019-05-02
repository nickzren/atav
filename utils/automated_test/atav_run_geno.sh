. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/list_var_geno --list-var-geno --min-coverage 10 --include-qc-missing --qd 5 --qual 50 --mq 40 --gq 20 --snv-sor 3 --indel-sor 10 --snv-fs 60 --indel-fs 200 --rprs -3 --mqrs -10 --filter pass,likely,intermediate --het-percent-alt-read 0.3-1 --ctrl-af 0.01 --ccds-only --evs-maf 0.1

atav_prod.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/list_var_geno --list-var-geno --min-coverage 10 --include-qc-missing --qd 5 --qual 50 --mq 40 --gq 20 --snv-sor 3 --indel-sor 10 --snv-fs 60 --indel-fs 200 --rprs -3 --mqrs -10 --filter pass,likely,intermediate --het-percent-alt-read 0.3-1 --ctrl-af 0.01 --ccds-only --evs-maf 0.1
