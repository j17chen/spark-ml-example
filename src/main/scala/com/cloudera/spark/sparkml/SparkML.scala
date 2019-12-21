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

import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.SparkSession


object SparkML {
  val rootDir=System.getProperty("user.dir")
  val model_path=s"file:///${rootDir}/src/main/resources/model/lr"
  //训练
  def linearRegression_train(sparkSession:SparkSession): Unit = {
    val trainingDF=TestMLSource.getTrainData(sparkSession)
    trainingDF.show()
    //创建线性回归对象
    val lr = new LinearRegression()
    //设置最大迭代次数
    lr.setMaxIter(50)
    //通过线性回归拟合训练数据，生成模型
    val model = lr.fit(trainingDF)
    model.write.overwrite.save(model_path);
  }
  //测试
  def linearRegression_test(sparkSession:SparkSession):Unit={
    val model = LinearRegressionModel.load(model_path);
    val testDF=TestMLSource.getTestData(sparkSession)
    testDF.show()

    //创建临时视图
    testDF.createOrReplaceTempView("test")

    println("======================")
    //利用model对测试数据进行变化，得到新数据框，查询features", "label", "prediction方面值。
    val tested = model.transform(testDF).select("features", "label", "prediction");
    tested.show();
    //
    println("======================")
    val featureDF = sparkSession.sql("SELECT features FROM test");

    //对特征数据进行模型变换，得到预测结果
    val predictedDF = model.transform(featureDF).select("features", "prediction")
    predictedDF.show()
  }

  def linearRegression(): Unit ={
    val sparkSession=SparkSessionUtil.getSparkSession(LocalSparkDriver,"LinearRegression")
    try{
      linearRegression_train(sparkSession)
      linearRegression_test(sparkSession)
    }finally {
      sparkSession.close()
    }
  }

  def main(args: Array[String]): Unit = {
    linearRegression()
  }
}
