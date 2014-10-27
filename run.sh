# sbt "run -m $master "
time spark-submit \
   --executor-memory 1G \
   --num-executors 6 \
   --class SparkAls target/scala-2.10/sparkals_2.10-0.1-SNAPSHOT.jar \
   $master
