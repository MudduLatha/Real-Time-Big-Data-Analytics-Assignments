import org.apache.spark.{SparkConf, SparkContext}


object TransaformationsAndActions {

  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir","E:\\MS\\PrinciplesofBigDataManagement\\hadoop-common-2.2.0-bin-master")
    // initialise spark context
    val conf = new SparkConf().setAppName("CountSpark").setMaster("local[2]").set("spark.executor.memory","8g")
    val sc = new SparkContext(conf)

    val textFile = sc.textFile("D:\\RealTime\\InputTextFile.txt")

    val counts = textFile.map(word => (word, 1))
      .reduceByKey(_ + _)
      .sortByKey(true)

    //var SmallArr = new Array[String](5)
    var KeyArr = new Array[String](5)
    SmallArr = textFile.takeOrdered(2)(
     Ordering[String].reverse)

    KeyArr.foreach((o: String) => println(o))

    counts.saveAsTextFile("D:\\RealTime\\RealTimeBigDataAnalyticsAssignment2")

  }

}
