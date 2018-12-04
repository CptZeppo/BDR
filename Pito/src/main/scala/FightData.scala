import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import math.{ sqrt, pow }


object FightData {
  class Dice(
              var number: Int,
              var range: Int,
              var bonus: Int
            )

  class Creature(
                var name: String,
                var armure: Int,
                var hp: Int,
                var melee_dmg: Array[Int],
                var throws: Array[Dice],
                var melee_range: Int,
                var ranged_dmg: Array[Int],
                var ranged_range: Int,
                var regen: Int,
                var distance: Int,
                var mvmt: Int,
                var dead: Boolean
                )
  {
    override def toString: String =
      s"$name"
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Fight").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val dice_solar = new Dice(3, 6, 18)
    val solar = new Creature(
      "Solar",
      44,
      363,
      Array(35, 30, 25, 20),
      Array(dice_solar),
      10,
      Array(31, 26, 21, 16),
      110,
      15,
      0,
      50,
      false
    )

    // Initialize the array for the creatures
    var id: Long = 1
    var creatures = Array((id, solar))

    // Map is 200x200 so we random coordinates accordingly
    val random = scala.util.Random
    val coord_solar = Array(random.nextInt(200), random.nextInt(200))

    // Function to calculate distance to solar
    def distance(coords: Array[Int]): Int =
      sqrt(pow(coord_solar(0) - coords(0), 2) + pow(coord_solar(1) - coords(1), 2)).toInt

    // Worg riders
    for (i <- 1 to 9) {
      val coord_rider = Array(random.nextInt(200), random.nextInt(200))
      val dice_rider = new Dice(1, 8, 2)
      val rider = new Creature(
        "Worg Rider " + i,
        18,
        13,
        Array(6),
        Array(dice_rider),
        5,
        Array(4),
        60,
        0,
        distance(coord_rider),
        20,
        false
      )
      id += 1
      creatures :+= (id, rider)
    }

    // Warlord
    val coord_warlord = Array(random.nextInt(200), random.nextInt(200))
    val dice_warlord1 = new Dice(1, 8, 10)
    val dice_warlord2 = new Dice(2, 6, 0)
    val warlord = new Creature(
      "Warlord",
      27,
      141,
      Array(20, 15, 10),
      Array(dice_warlord1, dice_warlord2),
      5,
      Array(19),
      10,
      0,
      distance(coord_warlord),
      30,
      false
    )
    id += 1
    creatures :+= (id, warlord)

    // Barbares orcs
    for (i <- 1 to 4) {
      val coord_orc = Array(random.nextInt(200), random.nextInt(200))
      val dice_orc = new Dice(1, 8, 10)
      val orc = new Creature(
        "Barbare Orc " + i,
        17,
        142,
        Array(19, 14, 9),
        Array(dice_orc),
        5,
        Array(16, 11, 6),
        110,
        0,
        distance(coord_orc),
        40,
        false
      )
      id += 1
      creatures :+= (id, orc)
    }

    println(creatures.mkString("\n"))

    // Create a RDD for the vertices
    val vertices: RDD[(VertexId, Creature)] =
      sc.parallelize(creatures)

    // Create a RDD for the edges
    var relations = Array(Edge[String](1L, 2L))
    for (i <- 3 to creatures.length){
      relations :+= Edge[String](1L, i.toLong, "string")
    }
    val edges: RDD[Edge[String]] =
      sc.parallelize(relations)
    val graph = Graph(vertices, edges, null)
  }
}
