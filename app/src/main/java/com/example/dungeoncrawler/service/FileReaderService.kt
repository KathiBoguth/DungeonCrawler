package com.example.dungeoncrawler.service

import android.content.Context
import com.example.dungeoncrawler.entity.GroundType
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.concurrent.ThreadLocalRandom


class FileReaderService {

    private fun parseRoomCsvToList(filename: URI, context: Context): List<List<GroundType>> {
        var fileContent = ""
        try {
            val inputStream: InputStream = context.assets.open(filename.path)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            fileContent = String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val room = mutableListOf<List<GroundType>>()
        fileContent.split("\n").forEach {
            val rowString = it.split(",")
            val rowEnum = rowString.map { identifier ->
                when (identifier) {
                    "r" -> randomGroundType()
                    "s" -> GroundType.STONE
                    else -> GroundType.STONE
                }
            }
            room.add(rowEnum)
        }

        return transpose(room)
    }

    fun parseFieldSchemeToField(
        fieldScheme: List<List<URI>>,
        context: Context
    ): List<List<GroundType>> {
        val resolvedField = fieldScheme.flatMap {
            it.map { fileName ->
                parseRoomCsvToList(fileName, context)
            }.reduce { acc, next ->
                acc.zip(next) { a, b -> a + b }
            }
        }.map {
            val row = it.toMutableList()
            row.add(0, GroundType.STONE)
            row.add(GroundType.STONE)
            return@map row.toList()
        }
        val stoneWallRow = List(resolvedField[0].size) { GroundType.STONE }
        val resolvedFieldMutable = resolvedField.toMutableList()
        resolvedFieldMutable.add(0, stoneWallRow)
        resolvedFieldMutable.add(stoneWallRow)
        return resolvedFieldMutable.toList()
    }

    private fun randomGroundType(): GroundType {

        return when (ThreadLocalRandom.current().nextInt(4)) {
            0 -> GroundType.GROUND1
            1 -> GroundType.GROUND2
            2 -> GroundType.PEBBLES
            3 -> GroundType.WATER
            else -> GroundType.GROUND1
        }
    }

    private fun transpose(matrix: List<List<GroundType>>): List<List<GroundType>> {
        val column = matrix[0].size
        val row = matrix.size
        val transpose = List(column) { MutableList(row) { GroundType.STONE } }
        for (i in 0 until row) {
            for (j in 0 until column) {
                transpose[j][i] = matrix[i][j]
            }
        }
        return transpose
    }
}