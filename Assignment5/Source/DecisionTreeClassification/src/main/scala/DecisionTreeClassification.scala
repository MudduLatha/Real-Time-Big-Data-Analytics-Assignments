import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree

/**
  * Created by latha on 9/29/2016.
  */
object DecisionTreeClassification {

  def main(args: Array[String]) {
//    val IMAGE_CATEGORIES = Array("BackStroke", "Stroke", "FreeStyle", "SideStroke", "Butterfly")
    System.setProperty("hadoop.home.dir", "C:\\Users\\latha\\Downloads\\winutils-master\\hadoop-2.6.0")
    //    Logger.getLogger("org").setLevel(Level.ERROR)
    //    Logger.getLogger("akka").setLevel(Level.ERROR)
    val sparkConf = new SparkConf().setAppName("ImageClassification").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)
    val train = sc.textFile("data/Training")
    val test = sc.textFile("data/Testing")
    val parsedData = train.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }
    val testData1 = test.map(line => {
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    })


    val trainingData = parsedData


    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 2
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    val classify1 = testData1.map { line =>
      val prediction = model.predict(line.features)
      (line.label, prediction)
    }

    val prediction1 = classify1.groupBy(_._1).map(f => {
      var fuzzy_Pred = Array(0, 0)
      f._2.foreach(ff => {
        fuzzy_Pred(ff._2.toInt) += 1
      })
      var count = 0.0
      fuzzy_Pred.foreach(f => {
        count += f
      })
      var i = -1
      var maxIndex = 5
      val max = fuzzy_Pred.max
      val pp = fuzzy_Pred.map(f => {
        val p = f * 100 / count
        i = i + 1
        if(f == max)
          maxIndex=i
        (i, p)
      })
      (f._1, pp, maxIndex)
    })
    prediction1.foreach(f => {
      println("\n\n\n" + f._1 + " : " + f._2.mkString(";\n"))
    })
    val y = prediction1.map(f => {
      (f._1, f._3.toDouble)
    })

    y.collect().foreach(println(_))

    val metrics = new MulticlassMetrics(y)

    println("Accuracy:" + metrics.accuracy)

    println("Confusion Matrix:")
    println(metrics.confusionMatrix)
    println("Confusion Matrix:")

    var labels = metrics.labels;


    for(i <- labels){
      println("Precision:")
      println(metrics.precision(i))
      println("Recall:")
      println(metrics.recall(i))
      println("F-Measure:")
      println(metrics.fMeasure(i))

    }
  }
}
