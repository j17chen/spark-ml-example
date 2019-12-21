/*-
 *
 * Cloudera
 * ==
 * Copyright (C) 2019 - 2023
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Author:jchen
 */
package com.cloudera.spark.sparkml

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ArrayBuffer

trait SparkMLSource {
  def getTrainData(ss:SparkSession,conf: Map[String,String]=Map[String,String]()):DataFrame
  def getTestData(ss:SparkSession,conf: Map[String,String]=Map[String,String]()):DataFrame
  var NAME:String
}

private case class Wine(FixedAcidity: Double, VolatileAcidity: Double,
                        CitricAcid: Double, ResidualSugar: Double, Chlorides: Double,
                        FreeSulfurDioxide: Double, TotalSulfurDioxide: Double, Density: Double, PH:
                        Double, Sulphates: Double, Alcohol: Double, Quality: Double)


object TestMLSource extends SparkMLSource{
  override def getTrainData(ss: SparkSession, conf: Map[String,String]): DataFrame = {
    import ss.implicits._
    var confDir=System.getProperty("user.dir")
    var dataDir=s"${confDir}/src/main/resources/mldata/winequality-white.csv"
    val wineSample=Wine(0,0,0,0,0,0,0,0,0,0,0,0)
    val schema=ss.sparkContext.parallelize(Seq(wineSample)).toDF().schema
    val df=ss.read
          .schema(schema)
          .option("inferSchema", "false")
          .option("delimiter",";")
          .option("header", true)
          .csv(dataDir)
    val wineDataRDD=df.map(w => Wine(
      w.getAs[Double](0),w.getAs[Double](1),w.getAs[Double](2),w.getAs[Double](3),
      w.getAs[Double](4),w.getAs[Double](5),w.getAs[Double](6),w.getAs[Double](7),
      w.getAs[Double](8),w.getAs[Double](9),w.getAs[Double](10),w.getAs[Double](11)))
    //转换RDD成DataFrame
    val trainingDF = wineDataRDD.map(w => (w.Quality,
          Vectors.dense(w.FixedAcidity, w.VolatileAcidity, w.CitricAcid,
            w.ResidualSugar, w.Chlorides, w.FreeSulfurDioxide, w.TotalSulfurDioxide,
            w.Density, w.PH, w.Sulphates, w.Alcohol))).toDF("label", "features")
    trainingDF
  }
  override def getTestData(ss: SparkSession, conf: Map[String,String]): DataFrame = {
    import ss.implicits._
    //创建内存测试数据数据框
    val testDF = ss.createDataFrame(Seq((6.0, Vectors.dense(7, 0.27, 0.36, 20.7, 0.045, 45, 170, 1.001, 3, 0.45, 8.8)),
      (6.0,Vectors.dense(6.3, 0.3, 0.34, 1.6, 0.049, 14, 132, 0.994, 3.3, 0.49, 9.5)),
      (6.0, Vectors.dense(8.1, 0.28, 0.4, 6.9, 0.05, 30, 97, 0.9951, 3.26, 0.44, 10.1)))).toDF("label", "features")
    testDF
  }

  override var NAME: String = "testSource"
}
