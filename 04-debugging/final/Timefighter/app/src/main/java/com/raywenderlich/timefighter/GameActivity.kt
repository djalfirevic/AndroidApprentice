 /*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.timefighter

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class GameActivity : AppCompatActivity() {

  internal val TAG = GameActivity::class.java.simpleName

  internal lateinit var gameScoreTextView: TextView

  internal lateinit var timeLeftTextView: TextView

  internal lateinit var tapMeButton: Button

  internal var score = 0

  internal var gameStarted = false

  internal lateinit var countDownTimer: CountDownTimer
  internal var initialCountDown: Long = 60000
  internal var countDownInterval: Long = 1000
  internal var timeLeft = 60

  companion object {

    private val SCORE_KEY = "SCORE_KEY"

    private val TIME_LEFT_KEY = "TIME_LEFT_KEY"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)

    Log.d(TAG, "onCreate called. Score is: $score")

    gameScoreTextView = findViewById<TextView>(R.id.game_score_text_view)

    timeLeftTextView = findViewById<TextView>(R.id.time_left_text_view)

    tapMeButton = findViewById<Button>(R.id.tap_me_button)

    tapMeButton.setOnClickListener { v -> incrementScore() }

    if (savedInstanceState != null) {
      score = savedInstanceState.getInt(SCORE_KEY)
      timeLeft = savedInstanceState.getInt(TIME_LEFT_KEY)
      restoreGame()
    } else {
      resetGame()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {

    super.onSaveInstanceState(outState)

    outState.putInt(SCORE_KEY, score)
    outState.putInt(TIME_LEFT_KEY, timeLeft)
    countDownTimer.cancel()

    Log.d(TAG, "onSaveInstanceState: Saving Score: $score & Time Left: $timeLeft")
 }

  override fun onDestroy() {
    super.onDestroy()

    Log.d(TAG, "onDestroy called.")
  }

  private fun incrementScore() {

    if (!gameStarted) {
      startGame()
    }

    score++

    val newScore = getString(R.string.your_score, Integer.toString(score))

    gameScoreTextView.text = newScore
}

private fun resetGame() {

    score = 0

    val initialScore = getString(R.string.your_score, Integer.toString(score))
    gameScoreTextView.text = initialScore

    val initialTimeLeft = getString(R.string.time_left, Integer.toString(60))
    timeLeftTextView.text = initialTimeLeft

    countDownTimer = object : CountDownTimer(initialCountDown, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
          timeLeft = millisUntilFinished.toInt() / 1000

          val timeLeftString = getString(R.string.time_left, Integer.toString(timeLeft))
          timeLeftTextView.text = timeLeftString
        }

        override fun onFinish() {
          endGame()
        }
    }

    gameStarted = false
  }

  private fun restoreGame() {

    val restoredScore = getString(R.string.your_score, Integer.toString(score))
    gameScoreTextView.text = restoredScore

    val restoredTime = getString(R.string.time_left, Integer.toString(timeLeft))
    timeLeftTextView.text = restoredTime

    countDownTimer = object : CountDownTimer((timeLeft * 1000).toLong(), countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {

          timeLeft = millisUntilFinished.toInt() / 1000

          val timeLeftString = getString(R.string.time_left, Integer.toString(timeLeft))
          timeLeftTextView.text = timeLeftString
        }

        override fun onFinish() {
          endGame()
        }
    }

    countDownTimer.start()
    gameStarted = true
  }

  private fun startGame() {

    countDownTimer.start()
    gameStarted = true
  }

  private fun endGame() {

    Toast.makeText(this, getString(R.string.game_over_message, Integer.toString(score)), Toast.LENGTH_LONG).show()
    resetGame()

  }
}
