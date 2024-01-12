package com.example.dungeoncrawler.service

import android.content.Context
import android.media.MediaPlayer
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings

class MediaPlayerService {

    private lateinit var mediaPlayerDungeon: MediaPlayer
    private lateinit var mediaPlayerBoss: MediaPlayer
    private lateinit var mediaPlayerMenu: MediaPlayer
    private lateinit var mediaPlayerExplosionSound: MediaPlayer
    private lateinit var mediaPlayerAttackSound: MediaPlayer
    private lateinit var mediaPlayerPunchSound: MediaPlayer
    private lateinit var mediaPlayerCoinSound: MediaPlayer
    private lateinit var mediaPlayerSwordSound: MediaPlayer
    private lateinit var mediaPlayerHealSound: MediaPlayer

    fun setupMediaPlayer(context: Context) {
        mediaPlayerDungeon = MediaPlayer.create(context, R.raw.dungeon)
        mediaPlayerDungeon.isLooping = true
        mediaPlayerBoss = MediaPlayer.create(context, R.raw.boss)
        mediaPlayerBoss.isLooping = true
        mediaPlayerMenu = MediaPlayer.create(context, R.raw.menu)
        mediaPlayerMenu.isLooping = true
        mediaPlayerExplosionSound = MediaPlayer.create(context, R.raw.explosion)
        mediaPlayerAttackSound = MediaPlayer.create(context, R.raw.slash)
        mediaPlayerPunchSound = MediaPlayer.create(context, R.raw.punch)
        mediaPlayerCoinSound = MediaPlayer.create(context, R.raw.coin)
        mediaPlayerSwordSound = MediaPlayer.create(context, R.raw.swordhit)
        mediaPlayerHealSound = MediaPlayer.create(context, R.raw.heal)
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

    fun playMediaPlayerExplosion() {
        if (mediaPlayerExplosionSound.isPlaying) {
            mediaPlayerExplosionSound.pause()
        }
        mediaPlayerExplosionSound.start()
    }

    fun playMediaPlayerSlash() {
        if (mediaPlayerAttackSound.isPlaying) {
            mediaPlayerAttackSound.pause()
        }
        mediaPlayerAttackSound.start()
    }

    fun playMediaPlayerPunch() {
        if (mediaPlayerPunchSound.isPlaying) {
            mediaPlayerPunchSound.pause()
        }
        mediaPlayerPunchSound.start()
    }

    fun playMediaPlayerCoin() {
        if (mediaPlayerCoinSound.isPlaying) {
            mediaPlayerCoinSound.pause()
        }
        mediaPlayerCoinSound.start()
    }

    fun playMediaPlayerSword() {
        if (mediaPlayerSwordSound.isPlaying) {
            mediaPlayerSwordSound.pause()
        }
        mediaPlayerSwordSound.start()
    }

    fun playMediaPlayerHeal() {
        if (mediaPlayerHealSound.isPlaying) {
            mediaPlayerHealSound.pause()
        }
        mediaPlayerHealSound.start()
    }

}