package com.cloudera.spark.sparkml

import org.apache.spark.sql.SparkSession

sealed trait SparkDriverType{
  val NAME:String
  def builder():SparkSession.Builder
}

object LocalSparkDriver extends SparkDriverType {
   override val NAME: String  = "local"
   override def builder: SparkSession.Builder = {
     SparkSession.builder().master("local")
   }
}

case object YarnClusterSparkDriver extends SparkDriverType{
  override val NAME: String  = "yarn-cluster"
  override def builder(): SparkSession.Builder = {
    SparkSession.builder().master("yarn").config("spark.deploy.mode","cluster")
  }
}

case object YarnClientSparkDriver extends SparkDriverType{
  override val NAME: String  = "yarn-client"
  override def builder(): SparkSession.Builder = {
    SparkSession.builder().master("yarn").config("spark.deploy.mode","client")
  }
}

object SparkSessionUtil{
  def getSparkSession(driverType: SparkDriverType, appName:String):SparkSession={
    driverType.builder().appName(s"${appName}-${driverType.NAME}").getOrCreate()
  }
}