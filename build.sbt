
name := "SparkAls"

organization := "My organization"

scalaVersion := "2.10.4"


mainClass in (Compile,run) := Some("SparkAls")


libraryDependencies += "org.apache.spark" %% "spark-core" % "1.0.2"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

crossScalaVersions := Seq("2.10.3", "2.11.0-M8")


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.RC1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
)

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"


libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.0.0-cdh4.2.0"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.0.2"

// If using CDH, also add Cloudera repo
resolvers += "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/"



