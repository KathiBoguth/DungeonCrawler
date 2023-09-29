package com.example.dungeoncrawler.service

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.GroundType
import java.util.LinkedList
import java.util.Queue

internal class Pair(var item1: Int, var item2: Int)

class PathFindingService {

    companion object {

        // To find the path from
        // top left to bottom right
        fun isPath(arr: Array<IntArray>, start: Coordinates, goal: Coordinates): Boolean {
            val rowGoal = goal.x
            val columnGoal = goal.y

            // Directions
            val directions =
                arrayOf(intArrayOf(0, 1), intArrayOf(0, -1), intArrayOf(1, 0), intArrayOf(-1, 0))

            // Queue
            val queue: Queue<Pair> = LinkedList()

            // Insert the start point.
            queue.add(Pair(start.x, start.y))

            // Until queue is empty
            while (queue.size > 0) {
                val pair = queue.peek()
                queue.remove()

                // Mark as visited
                pair?.let {
                    arr[pair.item1][pair.item2] = -1
                    // Destination is reached.
                    if (pair.item1 == rowGoal - 1 && pair.item2 == columnGoal - 1) return true

                    // Check all four directions
                    for (dir in directions) {

                        // Using the direction array
                        val fieldA = pair.item1 + dir[0]
                        val fieldB = pair.item2 + dir[1]

                        // Not blocked and valid
                        if (fieldA >= 0 && fieldB >= 0 && fieldA < rowGoal && fieldB < columnGoal && arr[fieldA][fieldB] != -1) {
                            if (fieldA == rowGoal - 1 && fieldB == columnGoal - 1) return true
                            queue.add(Pair(fieldA, fieldB))
                        }
                    }
                }
            }
            return false
        }

        fun fieldToIntArray(field: List<List<GroundType>>): Array<IntArray> {
            val intArray = Array(field.size) { IntArray(field[0].size) { 0 } }
            field.forEachIndexed { indexRow, row ->
                row.forEachIndexed { indexColumn, groundType ->
                    if (groundType == GroundType.STONE) {
                        intArray[indexRow][indexColumn] = -1
                    }

                }
            }
            return intArray
        }
    }

}
