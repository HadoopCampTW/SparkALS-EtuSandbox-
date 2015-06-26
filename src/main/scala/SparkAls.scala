

import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.Rating


import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.{RDD => SparkRDD}

import org.apache.spark.util.Utils
import org.apache.hadoop.fs.FileUtil

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.apache.commons.io._

import sys.process._

import java.io.File


object SparkAls {
  
   private val RUN_JAR="/home/eva/SparkAls/target/scala-2.10/sparkals_2.10-0.1-SNAPSHOT.jar"
   private val Out_path = "/user/eva/SparkAls/output"
   private val Out_Hadoop_Path = "hdfs://localhost:8020/user/eva/SparkAls/output"
   private val In_path  = "hdfs://localhost:8020/user/eva/SparkAls/ratings.dat"
   
   
  def setSparkEnv(master:String) : SparkContext = {

    val conf = new SparkConf()
       //.setMaster("spark://craigmbp:7077")
       .setMaster(master)
       .setAppName("SparkAls")
       // runtime Spark Home, set by env SPARK_HOME or explicitly as below
       //.setSparkHome("/opt/spark")

       // be nice or nasty to others (per node)
       //.set("spark.executor.memory", "1g")
       //.set("spark.core.max", "2")

       // find a random port for driver application web-ui
       //.set("spark.ui.port", findAvailablePort.toString)
       //.setJars(findJars)
       //.setJars(Seq("/Users/cray/Documents/workspace-scala/ScalaParseDate/target/scala-2.10/scalaparsedate_2.10-1.0.jar"))
       //.setJars(Seq(RUN_JAR))
    
       // The coarse-grained mode will instead launch only one long-running Spark task on each Mesos machine,
       // and dynamically schedule its own “mini-tasks” within it. The benefit is much lower startup overhead,
       // but at the cost of reserving the Mesos resources for the complete duration of the application.
       // .set("spark.mesos.coarse", "true")

    // for debug purpose
    println("sparkconf: " + conf.toDebugString)

    val sc = new SparkContext(conf)
    sc
  }


  
  def ExecAls(sc:SparkContext) = {


    // Load and parse the data
    println("\nLoad into RDD...\n");
    val data = sc.textFile(In_path)
    //test.data
    //val ratings = data.map(_.concat(",n").split(',') match { 

    //ratings.data of MovieLens
    println("Mapping...\n");
    val ratings = data.map(_.split("::") match { 
        case Array(user, item, rate, _) => {
                printf("--->user: %s, item: %s, rate: %s\n", user, item, rate )
                Rating(user.toInt, item.toInt, rate.toDouble)
            }
        case some => println(some); throw new Exception("-->Match error...")
    })

    System.out.flush()


    // Build the recommendation model using ALS
    val rank = 10  //number of lantent factors
    val numIterations = 20
    val lambda = 0.01 //normalization parameter
    println("Training...\n");
    val model = ALS.train(ratings, rank, numIterations, lambda)

    // Evaluate the model on rating data
    val usersProducts = ratings.map { case Rating(user, product, rate) =>
      (user, product)
    }
    
    println("Predicting...\n");
    val predictions = 
      model.predict(usersProducts).map { case Rating(user, product, rate) => 
        ((user, product), rate)
      }
    
    println("Joining...\n");
    val ratesAndPreds = ratings.map { case Rating(user, product, rate) => 
      ((user, product), rate)
    }.join(predictions).sortByKey()  //ascending or descending 



    println("\n\nTry to delete path: ["+Out_path+"]\n\n\n");
    val delete_out_path = "hadoop fs -rm -r " + Out_path
    delete_out_path.!
    //FileUtil.fullyDelete(new File(Out_path));
    
    val formatedRatesAndPreds = ratesAndPreds.map {
      case ((user, product), (rate, pred)) => user + "\t" + product + "\t" + rate + "\t" + pred
    }
    formatedRatesAndPreds.saveAsTextFile(Out_Hadoop_Path)
    
    val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) => 
      val err = (r1 - r2)
      err * err
    }.mean()
    println("\n\n--->Mean Squared Error = " + MSE + "\n\n\n")

  }
  
  
  
  
  def main(args: Array[String]) {

    val sc = setSparkEnv( args(args.length -1) )

    ExecAls(sc)
    
    sc.stop()
  }
 

  
}
