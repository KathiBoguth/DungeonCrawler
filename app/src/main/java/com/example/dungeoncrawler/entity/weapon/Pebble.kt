package com.example.dungeoncrawler.entity.weapon

import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction

class Pebble(pebbleDirection: Direction, coordinates: Coordinates) :
    Arrow("pebble_throwable", pebbleDirection, coordinates) {
    val attackValue = 10
}