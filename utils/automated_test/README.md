#Automated Testing

Automated Testing is aimed to run all major analysis function and compare output results between current beta release ATAV with last stable release version to make sure no differences. 

##Usage:

##1. Set paths

Check the `atav_config_paths.sh` to make sure the tests will read/write files from the desired locations.

##2. Run test case

To run a test case, simply use:

    ./atav_run_<case>.sh
replacing `<case>` for the desired test case, e.g.:

    ./atav_run_anno.sh
    ./atav_fun_all.sh


##3. Compare results

After running the test case, the output might be compared using:

    ./atav_diff_<case>.sh

replacing `<case>` for the respective test case, e.g.:

    ./atav_diff_anno.sh
    ./atav_diff_all.sh


**Note:** it is recommended to use `atav_diff_all.sh`, which may be used even if the test case used was not `atav_run_all.sh`.
This provides a complete list of output files added/removed and guarantees that .csv files are correctly compared in case of attribute changes.