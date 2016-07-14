# Command Log Analysis Tool


## Usage
```
Usage: spark-submit (...) log_parsing.py
	 --in <input-path>
		# Directory containing input files
	 --out <output-path>
		# Directory to output csv files
	 --intervals <intervals>
		# Comma-separated date list
	[--input-s3] [--output-s3]
	[--aws-s3-bucket <bucket-name>]
	[--aws-access-key <key>]
	[--aws-secret-key <key>]
```

## Input

The application expects to receive as input (```--in``` option) the path to a folder containing the following files:
* ```users.command.log``` - the ATAV command log file;
* ```functions.txt``` - a file where each line contains an ATAV function and its type string, separated by a single space;
* ```atav_users.csv``` - a file containing ATAV users and its respectives groups.

If the input comes from S3, then the user must also use the ```--input-s3``` option. Check below for S3 configuration.

## Output

The output will contain the following .csv files (or folders, if outputting to S3):
* ```function_usage_data.csv```
* ```function_time_data.csv```
* ```function_option_data.csv```

If the output is to S3, then the user must also use the ```--output-s3``` option. Check below for S3 configuration.

## Intervals

The date intervals to be analyzed must be given using the ```--intervals``` option.
The dates should be in ```YYYY-MM-DD``` format and comma-separated. Each pair of dates is an interval.

## AWS S3 Configuration

If using S3 to input and/or output files, then the AWS credentials and bucket name must be provided using the respective options.

**Note 1:** When using S3, the paths given are simply paths inside the bucket (e.g.: provide ```/``` to read from the bucket root path).

**Note 2:** Be sure to provide Spark the path to the necessary AWS Hadoop libraries.
This can be done using the ```--jars``` options for ```spark-submit```.


