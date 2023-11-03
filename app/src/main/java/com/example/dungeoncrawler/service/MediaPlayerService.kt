package com.example.dungeoncrawler.service

import android.content.Context
import android.media.MediaPlayer
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings

class MediaPlayerService {

    private lateinit var mediaPlayerDungeon: MediaPlayer
    private lateinit var mediaPlayerBoss: MediaPlayer
    private lateinit var mediaPlayerMenu: MediaPlayer

    fun setupMediaPlayer(context: Context) {
        mediaPlayerDungeon = MediaPlayer.create(context, R.raw.dungeon)
        mediaPlayerDungeon.isLooping = true
        mediaPlayerBoss = MediaPlayer.create(context, R.raw.boss)
        mediaPlayerBoss.isLooping = true
        mediaPlayerMenu = MediaPlayer.create(context, R.raw.menu)
        mediaPlayerMenu.isLooping = true
    }

    private fun startMediaPlayerDungeon() {
        mediaPlayerDungeon.start()
    }

    fun startMediaPlayerMenu() {
        mediaPlayerMenu.start()
    }

    fun startMediaPlayerByLevelCount(levelCount: Int) {
        if (levelCount >= Settings.enemiesPerLevel.size) {
            startMediaPlayerBoss()
        } else {
            startMediaPlayerDungeon()
        }
    }

    fun pauseMediaPlayers() {
        if (mediaPlayerDungeon.isPlaying) {
            mediaPlayerDungeon.pause()
        }
        if (mediaPlayerBoss.isPlaying) {
            mediaPlayerBoss.pause()
        }
        if (mediaPlayerMenu.isPlaying) {
            mediaPlayerMenu.pause()
        }
    }

    fun pauseMediaPlayerDungeon() {
        mediaPlayerDungeon.pause()
    }

    fun startMediaPlayerBoss() {
        mediaPlayerBoss.start()
    }

}