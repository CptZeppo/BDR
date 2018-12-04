import org.apache.spark.sql.SparkSession


object LoadData {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Inverted Index").master("local[*]").getOrCreate()
    import spark.implicits._

    val df = spark.read.json("src\\main\\resources\\monsters_data.txt").as[(String, scala.List[String])]
    val fantasticBeasts = df.filter(""" size(spellList) != 0 """)
    fantasticBeasts.printSchema()
    fantasticBeasts.show()

    val inverted = fantasticBeasts.flatMap { r => r._2.map { l => (l, r._1)}}
    val grouped = inverted.groupByKey(_._1)
    // print KeyValueGroupedDataSet
    grouped.mapGroups{case(k, iter) => (k, iter.map(x => x._2).toArray)}.show
  }
}