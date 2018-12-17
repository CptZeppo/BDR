import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import scala.math.{pow, sqrt}


object FightData2 {
  class Dice(
              var number: Int,
              var range: Int,
              var bonus: Int
            )

  class Coordinates(
                   var x: Int,
                   var y: Int,
                   var z: Int
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
                var coords: Coordinates,
                var mvmt: Array[Int],
                var dead: Boolean,
                var team: Int
                )
  {
    override def toString: String =
      s"$name"
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Fight").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val random = scala.util.Random

    // Solar
    val dice_solar = new Dice(3, 6, 18)
    val coords_solar = new Coordinates(random.nextInt(200), random.nextInt(200), 0)
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
      coords_solar,
      Array(50, 150),
      false,
      1
    )

    // Initialize the array for the creatures
    var id: Long = 1
    var creatures = Array((id, solar))

    // Planetars
    for (i <- 1 to 2) {
      val coords_planetar = new Coordinates(random.nextInt(200), random.nextInt(200), 0)
      val dice_planetar = new Dice(3, 6, 15)
      val planetar = new Creature(
        "Planetar",
        32,
        229,
        Array(27, 22, 17),
        Array(dice_planetar),
        10,
        Array(0),
        0,
        10,
        coords_planetar,
        Array(30, 90),
        false,
        1
      )
      id += 1
      creatures :+= (id, planetar)
    }

    // Movanic Devas
    for (i <- 1 to 2) {
      val coords_mv = new Coordinates(random.nextInt(200), random.nextInt(200), 0)
      val dice_mv = new Dice(2, 6, 7)
      val mv = new Creature(
        "Movanic Deva",
        24,
        126,
        Array(17, 12, 7),
        Array(dice_mv),
        10,
        Array(0),
        0,
        0,
        coords_mv,
        Array(40, 60),
        false,
        1
      )
      id += 1
      creatures :+= (id, mv)
    }

    // Astral Devas
    for (i <- 1 to 4) {
      val coords_av = new Coordinates(random.nextInt(200), random.nextInt(200), 0)
      val dice_av = new Dice(1, 8, 14)
      val av = new Creature(
        "Astral Deva",
        29,
        172,
        Array(26, 21, 16),
        Array(dice_av),
        10,
        Array(0),
        0,
        0,
        coords_av,
        Array(50, 100),
        false,
        1
      )
      id += 1
      creatures :+= (id, av)
    }

    // Red Dragon
    val dice_dragon = new Dice(4, 8, 24)
    val coords_dragon = new Coordinates(random.nextInt(200), random.nextInt(200), 0)
    val dragon = new Creature(
      "Red Dragon",
      39,
      449,
      Array(37),
      Array(dice_dragon),
      30,
      Array(0),
      0,
      0,
      coords_dragon,
      Array(40, 250),
      false,
      2
    )
    id += 1
    creatures :+= (id, dragon)

    // Orc Barbarians
    for (i <- 1 to 200) {
      val coords_ob = new Coordinates(random.nextInt(200), random.nextInt(200), 0)
      val dice_ob = new Dice(1, 12, 10)
      val ob = new Creature(
        "Orc Barbarian",
        15,
        42,
        Array(11),
        Array(dice_ob),
        10,
        Array(5),
        10,
        0,
        coords_ob,
        Array(30, 0),
        false,
        2
      )
      id += 1
      creatures :+= (id, ob)
    }

    // Angel Slayer
    for (i <- 1 to 10) {
      val coords_as = new Coordinates(random.nextInt(200), random.nextInt(200), 0)
      val dice_as = new Dice(1, 8, 7)
      val as = new Creature(
        "Angel Slayer",
        26,
        112,
        Array(21, 16, 11),
        Array(dice_as),
        10,
        Array(19, 14, 9),
        110,
        0,
        coords_as,
        Array(40, 0),
        false,
        2
      )
      id += 1
      creatures :+= (id, as)
    }

    println(creatures.mkString("\n"))

    // Create a RDD for the vertices
    val vertices: RDD[(VertexId, Creature)] =
      sc.parallelize(creatures)

    // Create a RDD for the edges
    var relations = Array(Edge[String](1L, 2L))
    for (i <- 3 to creatures.length) {
      relations :+= Edge[String](1L, i.toLong, "string")
    }
    for (i <- 2 to creatures.length){
      for (j <- i+1 to creatures.length) {
        relations :+= Edge[String](i.toLong, j.toLong, "string")
      }
    }
    val edges: RDD[Edge[String]] =
      sc.parallelize(relations)
    val graph = Graph(vertices, edges, null)
  }
}
