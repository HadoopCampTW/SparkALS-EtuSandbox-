# sbt "run -m $master "
# Spark Web UI:  http://host:8080
#
#export master="spark://ip-10-166-138-217.ap-southeast--1.compute.internal:7077"
export master="local[*]"
echo "Running on $master ..."
command="spark-submit \
   --executor-memory 1G \
   --num-executors 3 \
   --class SparkAls target/scala-2.10/sparkals_2.10-0.1-SNAPSHOT.jar \
   $master"
echo "Executing... $command"
`time $command`
