#author felipe

from pyspark import SparkContext
from pyspark.sql import SQLContext

import sys
import os
from subprocess import Popen
from dateutil import parser

sc = SparkContext()
sqlContext = SQLContext(sc)

def help():
  print("Usage: spark-submit (...) log_parsing.py"+\
      "\n\t --in <input-path>\n\t\t# Directory containing input files"+\
      "\n\t --out <output-path>\n\t\t# Directory to output csv files"+\
      "\n\t --intervals <intervals>\n\t\t# Comma-separated date list"+\
      "\n\t[--input-s3] [--output-s3]"+\
      "\n\t[--aws-s3-bucket <bucket-name>]"+\
      "\n\t[--aws-access-key <key>]"+\
      "\n\t[--aws-secret-key <key>]" , file=sys.stderr)
  exit(-1)

inputDir = None
outputDir = None
intervals = None
s3Bucket = None
useS3 = False
inputS3 = False
outputS3 = False
awsParamCount = 0
i=1;
while i < len(sys.argv):
  if sys.argv[i] == "--in":
    inputDir = sys.argv[i+1]
    i=i+1
  elif sys.argv[i] == "--out":
    outputDir = sys.argv[i+1]
    i=i+1
  elif sys.argv[i] == "--intervals":
    intervalList = sys.argv[i+1].split(',')
    intervals=[]
    for j in range(0,len(intervalList),2):
      intervals.append([intervalList[j],intervalList[j+1]])
    i=i+1
  elif sys.argv[i] == "--aws-s3-bucket":
    s3Bucket = sys.argv[i+1]
    awsParamCount=awsParamCount+1
    i=i+1
  elif sys.argv[i] == "--aws-access-key":
    awsParamCount=awsParamCount+1
    sc._jsc.hadoopConfiguration().set("fs.s3a.access.key", sys.argv[i+1])
    i=i+1
  elif sys.argv[i] == "--aws-secret-key":
    awsParamCount=awsParamCount+1
    sc._jsc.hadoopConfiguration().set("fs.s3a.secret.key", sys.argv[i+1])
    i=i+1
  elif sys.argv[i] == "--input-s3":
    inputS3=useS3=True
  elif sys.argv[i] == "--output-s3":
    outputS3=useS3=True
  else:
    print("Invalid argument: "+sys.argv[i],file=sys.stderr)
    help()
  i=i+1

if inputDir == None or outputDir == None or intervals == None:
  help()

if useS3 and awsParamCount != 3:
  print("Missing some AWS S3 parameter!",file=sys.stderr)
  help()

if useS3:
  awsBucket = 's3a://'+s3Bucket

if inputS3:
  if inputDir[0] == '/':
    inputDir=inputDir[1:]
  inputDir = os.path.join(awsBucket,inputDir)  
else:
  inputDir = 'file://'+inputDir

if outputS3:
  if outputDir[0] == '/':
    outputDir=outputDir[1:]
  outputDir = os.path.join(awsBucket,outputDir)
else:
  outputDir = 'file://'+outputDir
  

def save(data, name):
  if isinstance(data, list):
    df = sqlContext.createDataFrame(data)
  else:
    df = data

  print(">>> Saving "+name)
  
  outputFile = os.path.join(outputDir,name)
  df.write \
    .format('csv').mode('overwrite') \
    .options(header='true') \
    .save(outputFile)

  if not outputS3:
    Popen('hadoop fs -getmerge '+outputFile+' '+outputFile+'.csv && hadoop fs -rm -f -r '+outputFile,shell=True)


from pyspark.sql.types import *
logSchema = StructType([ \
  StructField("user", StringType(), False), \
  StructField("datetime", StringType(), False), \
  StructField("db", StringType(), False), \
  StructField("server", StringType(), False), \
  StructField("command", StringType(), False), \
  StructField("time", StringType(), False), \
  StructField("size", StringType(), False) ])

functionsSchema = StructType([ \
  StructField("function", StringType(), False), \
  StructField("type", StringType(), False) ])


# Load command log file
awsLogFilepath = os.path.join(inputDir,'users.command.log')
df = sqlContext.read \
    .format('csv') \
    .options(header='false',delimiter='\t',mode='DROPMALFORMED') \
    .load(awsLogFilepath, schema = logSchema).cache()
df.registerTempTable('aws_command_log')

# Load functions
awsLogFilepath = os.path.join(inputDir,'functions.txt')
df = sqlContext.read \
    .format('csv') \
    .options(header='false',delimiter=' ') \
    .load(awsLogFilepath, schema = functionsSchema).cache()
df.registerTempTable('aws_function_types')

# Load user groups
awsLogFilepath = os.path.join(inputDir,'atav_users.csv')
df = sqlContext.read \
    .format('csv') \
    .options(header='true') \
    .load(awsLogFilepath,inferSchema='true').cache()
df.registerTempTable('aws_user_groups')


# COMMAND ----------

# --- Auxiliary function declarations ---                                              

from pyspark.sql import Row

# get functions to build dict() (python equivalent HashMap)
functionType = dict()
functions = sqlContext.sql("SELECT * FROM aws_function_types").collect()
for f in functions:
  functionType[f.function] = f.type

# get user groups to build dict() (python equivalent HashMap)
userGroup = dict()
users = sqlContext.sql("SELECT * FROM aws_user_groups").collect()
for u in users:
  userGroup[str(u.uni)] = str(u.group)
  
def get_user_group(user):
  if user in userGroup:
    return userGroup[user]
  else:
    return "n/a"
  
# extracts the function string from the whole command string  
def extractFunction(x):
  try:
    words = str(x).split(" ")
    for word in words:
      if word in functionType:
        return word
    return "notfound"
  except:
    return "invalid"

# register function to be used in SparkSQL
from pyspark.sql.types import StringType
sqlContext.registerFunction("extract_function",extractFunction,StringType())

# checks if a given datetime string is inside a given interval
def date_in_interval(datetimestr,start,end):
  try:
    dateObj = parser.parse(str(datetimestr))
    date = str(dateObj.date())
    return (start <= date and date <= end)
  except:
    return False #probably an invalid format, so just return false

# register function to be used in SparkSQL
from pyspark.sql.types import BooleanType
sqlContext.registerFunction("date_in_interval",date_in_interval,BooleanType())

# extracts time in seconds from time string
def get_time(timestr):
  try:
    return int(timestr.split(" ")[0])
  except:
    return 0

# register function to be used in SparkSQL
from pyspark.sql.types import IntegerType
sqlContext.registerFunction("get_time",get_time,IntegerType())

timeframes = ["[0,10min[","[10min,1h[","[1h,6h[","[6h,12h[","[12h,1d[","[1d,2d[","[2d+"]

def get_timeframe(time):
  if time < 10*60:
    return 0
  elif time < 3600:
    return 1
  elif time < 6*3600:
    return 2
  elif time < 12*3600:
    return 3
  elif time < 24*3600:
    return 4
  elif time < 2*24*3600:
    return 5
  else:
    return 6

# Workaround to allow enum usage
def enum(l):
  return zip(range(len(l)),l)
  


### Usage and time analysis
print(">>> Running function usage/runtime analysis")

# textFile = sc.textFile("/FileStore/tables/x27jm5321465339702253/users_command-ae26f.log")

functionUsageData = []
functionTimeData = sc.emptyRDD().cache()

functionCountData = [] # Used at further analysis
  # functionCountData[<interval-id>][<function-name>] has the number of times such function was used in such interval
  
for inx,interval in enum(intervals):
  #get DataFrame for the interval being analyzed
  df = sqlContext.sql("SELECT user, extract_function(command) fun, get_time(time) time FROM aws_command_log") \
    .where("date_in_interval(datetime,'"+interval[0]+"','"+interval[1]+"')") \
    .drop("datetime")
  
  # Workaround for Spark 2.0 preview
  # Switch back to udf ASAP
  # df = sqlContext.sql("SELECT * FROM aws_command_log")
  
  # r3 = df.rdd.filter(lambda r: r.user not in userGroup)
  # r = df.rdd.filter(lambda r: date_in_interval(r.datetime,interval[0],interval[1]))\
            # .map(lambda r: Row(user=r.user,fun=extractFunction(r.command),time=get_time(r.time))).cache()
  r = df.rdd

  intervalstr = interval[0]+" to "+interval[1]
    
  # Function Usage Analysis
  functionUsagePairs = r.map(lambda l: (l.fun,1)).reduceByKey(lambda x,y : x+y)
  
  functionCountData.append(functionUsagePairs.collectAsMap())

  newRows = functionUsagePairs.map(lambda t: Row( series=intervalstr, function=t[0], value=t[1])).collect()
  functionUsageData.extend(newRows)
  
  # Timeframe Analysis
  funTimeframePairs = r.map(lambda l: ((l.fun,get_timeframe(l.time),get_user_group(l.user)),(1,l.time))).reduceByKey(lambda x,y: (x[0]+y[0],x[1]+y[1]) )
  
  newRows = funTimeframePairs.map(lambda t: \
            Row(interval=intervalstr,interval_id=inx,timeframe_id=t[0][1],timeframe=timeframes[t[0][1]], \
                function=t[0][0], count=t[1][0],cpu_time=t[1][1],group=t[0][2]))
  functionTimeData = functionTimeData.union(newRows)
  
  # CPU Time Analysis
  cpuTimePairs = r.map(lambda l: (get_user_group(l.user),l.time)).reduceByKey(lambda x,y : x+y)

# Create DataFrame for easy ad-hoc analysis
functionTimeDF = functionTimeData.toDF()



save(functionUsageData,'function_usage_data')


# Check function timeframes for a specific interval
save(functionTimeDF.orderBy("timeframe_id"),'function_time_data')


l=cpuTimePairs.map(lambda t: (t[0],t[1]/3600)).collect()
l.sort()
l



### Options analysis
print(">>> Running options analysis")

def createFunOptPairs(command):
  try:
    fun = extractFunction(command)
    words = str(command).split(" ")
    l = []
    for word in words:
      if word[:2] == "--" and word != fun:
        l.append((fun,word))
    return l
  except:
    return []

# for each interval ...
funOptData = sc.emptyRDD().cache()
for inx,interval in enum(intervals):
  
  # ... map-reduce function-options pairs
  df = sqlContext.sql("SELECT * FROM aws_command_log")
  r = df.rdd.filter(lambda r: date_in_interval(r.datetime,interval[0],interval[1])) \
       .flatMap(lambda r : createFunOptPairs(r.command)) \
       .map(lambda t : (t,1)).reduceByKey(lambda x,y : x+y) \
       .sortBy(lambda tt : (tt[0][0],-tt[1])).cache()
 
  intervalstr = interval[0]+" to "+interval[1]
  
  # ... then generate rows for DF
  r1 = r.map(lambda t: \
             Row(interval=intervalstr, interval_id=inx, function_name=t[0][0], function_type=functionType[t[0][0]], \
                 option=t[0][1], value=(t[1]*100.0)/functionCountData[inx][t[0][0]]))
  funOptData = funOptData.union(r1)

# Create DataFrame for easy ad-hoc analysis
funOptDF = funOptData.toDF()
  

save(funOptDF,'function_option_data')




