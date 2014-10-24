# sbt "run -m $master "
spark-submit \
   --executor-memory 512M \
   --num-executors 2 \
   --class SparkAls target/scala-2.10/sparkals_2.10-0.1-SNAPSHOT.jar \
   $master
