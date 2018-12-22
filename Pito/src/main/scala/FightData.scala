import breeze.optimize.MaxIterations
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import math.{pow, sqrt}


object FightData {
  class Dice(
              var number: Int,
              var range: Int,
              var bonus: Int,
              var crit: Int
            )

  class Creature(
                var name: String,
                var armure: Int,
                var hp: Int,
                var melee_touch: Array[Int],
                var throws: Array[Dice],
                var melee_range: Int,
                var ranged_touch: Array[Int],
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



  def sendAction(ctx: EdgeContext[FightData.Creature,String,Long]):Unit = {
    var dmg = 0
    var heal = 0
    val r = scala.util.Random
    if (ctx.dstAttr.name == "Solar" && ctx.srcAttr.dead == false && ctx.dstAttr.dead == false) {
      var distance = ctx.srcAttr.distance

      if (distance <= ctx.srcAttr.melee_range) {
        for (i <- 1 to ctx.srcAttr.melee_touch.size) {
          val attack = ctx.srcAttr.melee_touch.apply(i)
          val roll = r.nextInt(20)
          val dstHP = ctx.dstAttr.hp

          if (roll == 20) {
            val diceNumber = ctx.srcAttr.throws.apply(1).number
            val diceRange = ctx.srcAttr.throws.apply(1).range
            val dmgBonus = ctx.srcAttr.throws.apply(1).bonus
            for (i <- 1 to diceNumber) {
              dmg = dmg + r.nextInt(diceRange)
            }
            dmg = dmg + dmgBonus
            dmg = dmg * ctx.srcAttr.throws.apply(1).crit

          } else {
            if (roll + attack >= ctx.dstAttr.armure) {
              val diceNumber = ctx.srcAttr.throws.apply(1).number
              val diceRange = ctx.srcAttr.throws.apply(1).range
              val dmgBonus = ctx.srcAttr.throws.apply(1).bonus
              for (i <- 1 to diceNumber) {
                dmg = dmg + r.nextInt(diceRange)
              }
              dmg = dmg + dmgBonus

            }
          }
        }

      } else {
        if (distance <= ctx.srcAttr.ranged_range) {
          val attack = ctx.srcAttr.ranged_touch.apply(0)
          val roll = r.nextInt(20)
          val dstHP = ctx.dstAttr.hp

          if (roll == 20) {
            val diceNumber = ctx.srcAttr.throws.apply(2).number
            val diceRange = ctx.srcAttr.throws.apply(2).range
            val dmgBonus = ctx.srcAttr.throws.apply(2).bonus
            for (i <- 1 to diceNumber) {
              dmg = dmg + r.nextInt(diceRange)
            }
            dmg = dmg + dmgBonus
            dmg = dmg * ctx.srcAttr.throws.apply(2).crit

          } else {
            if (roll + attack >= ctx.dstAttr.armure) {
              val diceNumber = ctx.srcAttr.throws.apply(2).number
              val diceRange = ctx.srcAttr.throws.apply(2).range
              val dmgBonus = ctx.srcAttr.throws.apply(2).bonus
              for (i <- 1 to diceNumber) {
                dmg = dmg + r.nextInt(diceRange)
              }
              dmg = dmg + dmgBonus

            }
          }
          ctx.srcAttr.distance = distance - ctx.srcAttr.mvmt
        } else {
          ctx.srcAttr.distance = distance - ctx.srcAttr.mvmt * 2
        }
      }


    }
    if (ctx.srcAttr.name == "Solar" && ctx.srcAttr.dead == false && ctx.dstAttr.dead == false) {
      val distance = ctx.dstAttr.distance
      // 2/3 des PVs max du solar
      if (ctx.srcAttr.hp <= 242) {
        // Cure Critical Wounds
        for (i <- 1 to 4) {
          heal = r.nextInt(8) + heal
        }
        heal = heal + 20

      } else {

        if (distance <= ctx.srcAttr.melee_range) {
          for (i <- 1 to ctx.srcAttr.melee_touch.size) {
            val attack = ctx.srcAttr.melee_touch.apply(i)
            val roll = r.nextInt(20)
            val dstHP = ctx.dstAttr.hp

            if (roll == 20) {
              val diceNumber = ctx.srcAttr.throws.apply(1).number
              val diceRange = ctx.srcAttr.throws.apply(1).range
              val dmgBonus = ctx.srcAttr.throws.apply(1).bonus
              for (i <- 1 to diceNumber) {
                dmg = dmg + r.nextInt(diceRange)
              }
              dmg = dmg + dmgBonus
              dmg = dmg * ctx.srcAttr.throws.apply(1).crit

            } else {
              if (roll + attack >= ctx.dstAttr.armure) {
                val diceNumber = ctx.srcAttr.throws.apply(1).number
                val diceRange = ctx.srcAttr.throws.apply(1).range
                val dmgBonus = ctx.srcAttr.throws.apply(1).bonus
                for (i <- 1 to diceNumber) {
                  dmg = dmg + r.nextInt(diceRange)
                }
                dmg = dmg + dmgBonus

              }
            }
          }

        } else {
          if (distance <= ctx.srcAttr.ranged_range) {
            for (i <- 1 to ctx.srcAttr.ranged_touch.size) {
              val attack = ctx.srcAttr.ranged_touch.apply(i)
              val roll = r.nextInt(20)
              val dstHP = ctx.dstAttr.hp

              if (roll == 20) {
                val diceNumber = ctx.srcAttr.throws.apply(2).number
                val diceRange = ctx.srcAttr.throws.apply(2).range
                val dmgBonus = ctx.srcAttr.throws.apply(2).bonus
                for (i <- 1 to diceNumber) {
                  dmg = dmg + r.nextInt(diceRange)
                }
                dmg = dmg + dmgBonus
                dmg = dmg * ctx.srcAttr.throws.apply(2).crit

              } else {
                if (roll + attack >= ctx.dstAttr.armure) {
                  val diceNumber = ctx.srcAttr.throws.apply(2).number
                  val diceRange = ctx.srcAttr.throws.apply(2).range
                  val dmgBonus = ctx.srcAttr.throws.apply(2).bonus
                  for (i <- 1 to diceNumber) {
                    dmg = dmg + r.nextInt(diceRange)
                  }
                  dmg = dmg + dmgBonus
                }
              }
            }
          }
        }
      }
    }
    heal = heal + ctx.srcAttr.regen
    ctx.srcAttr.hp = ctx.srcAttr.hp + heal
  }



  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Fight").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val dice_solar = new Dice(3, 6, 18, 2)
    val dice_solar2 = new Dice(2, 8, 13, 2)
    val solar = new Creature(
      "Solar",
      44,
      363,
      Array(35, 30, 25, 20),
      Array(dice_solar,dice_solar2),
      10,
      Array(31, 26, 21, 16),
      110,
      14,
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
      val dice_rider = new Dice(1, 8, 2,3)
      val dice_rider2 = new Dice(1, 6, 0,3)
      val rider = new Creature(
        "Worg Rider " + i,
        18,
        13,
        Array(6),
        Array(dice_rider,dice_rider2),
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
    val dice_warlord1 = new Dice(1, 8, 10,2)
    val dice_warlord2 = new Dice(1, 6, 5,2)
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
      val dice_orc = new Dice(1, 8, 10,3)
      val dice_orc2 = new Dice(1, 8, 6,3)
      val orc = new Creature(
        "Barbare Orc " + i,
        17,
        142,
        Array(19, 14, 9),
        Array(dice_orc,dice_orc2),
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
