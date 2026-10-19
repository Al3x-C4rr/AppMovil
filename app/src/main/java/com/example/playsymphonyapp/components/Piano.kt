//Fase de prueba con funcionalidad del piano

package com.example.playsymphonyapp.components
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.SeekBar
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import com.example.playsymphonyapp.R
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.foundation.layout.Box

private val whiteKeyIds = arrayOf(
    R.id.keyA1, R.id.keyB1, R.id.keyC1, R.id.keyD1, R.id.keyE1, R.id.keyF1, R.id.keyG1,
    R.id.keyA2, R.id.keyB2, R.id.keyC2, R.id.keyD2, R.id.keyE2, R.id.keyF2, R.id.keyG2,
    R.id.keyA3, R.id.keyB3, R.id.keyC3, R.id.keyD3, R.id.keyE3, R.id.keyF3, R.id.keyG3,
    R.id.keyA4, R.id.keyB4, R.id.keyC4, R.id.keyD4, R.id.keyE4, R.id.keyF4, R.id.keyG4,
    R.id.keyA5, R.id.keyB5, R.id.keyC5, R.id.keyD5, R.id.keyE5, R.id.keyF5, R.id.keyG5,
    R.id.keyA6, R.id.keyB6, R.id.keyC6, R.id.keyD6, R.id.keyE6, R.id.keyF6, R.id.keyG6,
    R.id.keyA7, R.id.keyB7, R.id.keyC7, R.id.keyD7, R.id.keyE7, R.id.keyF7, R.id.keyG7
)

private val blackKeyIds = arrayOf(
    R.id.keyA1Sharp, R.id.keyC1Sharp, R.id.keyD1Sharp, R.id.keyF1Sharp, R.id.keyG1Sharp,
    R.id.keyA2Sharp, R.id.keyC2Sharp, R.id.keyD2Sharp, R.id.keyF2Sharp, R.id.keyG2Sharp,
    R.id.keyA3Sharp, R.id.keyC3Sharp, R.id.keyD3Sharp, R.id.keyF3Sharp, R.id.keyG3Sharp,
    R.id.keyA4Sharp, R.id.keyC4Sharp, R.id.keyD4Sharp, R.id.keyF4Sharp, R.id.keyG4Sharp,
    R.id.keyA5Sharp, R.id.keyC5Sharp, R.id.keyD5Sharp, R.id.keyF5Sharp, R.id.keyG5Sharp,
    R.id.keyA6Sharp, R.id.keyC6Sharp, R.id.keyD6Sharp, R.id.keyF6Sharp, R.id.keyG6Sharp,
    R.id.keyA7Sharp, R.id.keyC7Sharp, R.id.keyD7Sharp, R.id.keyF7Sharp, R.id.keyG7Sharp
)

private val whiteKeySounds = arrayOf(
    R.raw.a1, R.raw.b1, R.raw.c1, R.raw.d1, R.raw.e1, R.raw.f1, R.raw.g1,
    R.raw.a2, R.raw.b2, R.raw.c2, R.raw.d2, R.raw.e2, R.raw.f2, R.raw.g2,
    R.raw.a3, R.raw.b3, R.raw.c3, R.raw.d3, R.raw.e3, R.raw.f3, R.raw.g3,
    R.raw.a4, R.raw.b4, R.raw.c4, R.raw.d4, R.raw.e4, R.raw.f4, R.raw.g4,
    R.raw.a5, R.raw.b5, R.raw.c5, R.raw.d5, R.raw.e5, R.raw.f5, R.raw.g5,
    R.raw.a6, R.raw.b6, R.raw.c6, R.raw.d6, R.raw.e6, R.raw.f6, R.raw.g6,
    R.raw.a7, R.raw.b7, R.raw.c7, R.raw.d7, R.raw.e7, R.raw.f7, R.raw.g7
)

private val blackKeySounds = arrayOf(
    R.raw.a11, R.raw.c11, R.raw.d11, R.raw.f11, R.raw.g11,
    R.raw.a22, R.raw.c22, R.raw.d22, R.raw.f22, R.raw.g22,
    R.raw.a33, R.raw.c33, R.raw.d33, R.raw.f33, R.raw.g33,
    R.raw.a44, R.raw.c44, R.raw.d44, R.raw.f44, R.raw.g44,
    R.raw.a55, R.raw.c55, R.raw.d55, R.raw.f55, R.raw.g55,
    R.raw.a66, R.raw.c66, R.raw.d66, R.raw.f66, R.raw.g66,
    R.raw.a77, R.raw.c77, R.raw.d77, R.raw.f77, R.raw.g77
)


class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer



    private lateinit var keys: Array<Button>

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setContent {
            PianoView(
                whiteKeyIds = whiteKeyIds,
                blackKeyIds = blackKeyIds,
                whiteKeySounds = whiteKeySounds,
                blackKeySounds = blackKeySounds,
                onPlaySound = { playSound(it)
                }
            )
        }
    }
    private fun playSound(soundResource: Int) {

        mediaPlayer = MediaPlayer.create(this, soundResource)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                it.release()
            }, 1000) // Stop playback after 1 second
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
@Composable
fun PianoAdaptable(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        PianoView()
    }
}
@Composable
fun PianoView() {
    val context = LocalContext.current
    val players = remember { mutableSetOf<MediaPlayer>() }

    DisposableEffect(Unit) {
        onDispose {
            players.toList().forEach { it.release() }
            players.clear()
        }
    }

    PianoView(
        whiteKeyIds = whiteKeyIds,
        blackKeyIds = blackKeyIds,
        whiteKeySounds = whiteKeySounds,
        blackKeySounds = blackKeySounds,
        onPlaySound = { soundResource ->
            val player = MediaPlayer.create(context, soundResource)

            if (player != null) {
                players.add(player)

                player.setOnCompletionListener { finished ->
                    if (players.remove(finished)) {
                        finished.release()
                    }
                }

                player.setOnErrorListener { failed, _, _ ->
                    if (players.remove(failed)) {
                        failed.release()
                    }
                    true
                }

                player.start()
            }
        }
    )
}
@SuppressLint("ClickableViewAccessibility")
@Composable
fun PianoView(
    whiteKeyIds: Array<Int>,
    blackKeyIds: Array<Int>,
    whiteKeySounds: Array<Int>,
    blackKeySounds: Array<Int>,
    onPlaySound: (Int) -> Unit
) {
    val currentOnPlaySound by rememberUpdatedState(onPlaySound)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->

            // Carga el diseño original del teclado.
            val view = LayoutInflater.from(context)
                .inflate(R.layout.activity_main, null, false)

            val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
            val scrollView =
                view.findViewById<HorizontalScrollView>(R.id.scrollView)

            seekBar.max = 100

            // Evita desplazar el contenedor arrastrándolo.
            scrollView.setOnTouchListener { _, _ -> true }

            seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        val maxScroll = (
                                scrollView.getChildAt(0).width -
                                        scrollView.width
                                ).coerceAtLeast(0)

                        scrollView.scrollTo(
                            maxScroll * progress / 100,
                            0
                        )
                    }

                    override fun onStartTrackingTouch(
                        seekBar: SeekBar?
                    ) {}

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {}
                }
            )

            // Posición inicial: 55 % del recorrido.
            scrollView.post {
                seekBar.progress = 55
            }

            // Conecta cada ID con su sonido.
            val keySounds =
                whiteKeyIds.zip(whiteKeySounds) +
                        blackKeyIds.zip(blackKeySounds)

            keySounds.forEach { (keyId, soundResource) ->
                val button = view.findViewById<Button>(keyId)
                val originalTint = button.backgroundTintList

                button.setOnClickListener {
                    button.backgroundTintList =
                        ColorStateList.valueOf(
                            Color.parseColor("#80ffe5")
                        )

                    currentOnPlaySound(soundResource)

                    button.postDelayed({
                        button.backgroundTintList = originalTint
                    }, 100)
                }
            }

            // Devuelve el teclado XML para mostrarlo en Compose.
            RotatingPianoContainer(context).apply {
                addView(view)
            }
        }
    )
}
@Preview(
    showBackground = true,
    widthDp = 700,
    heightDp = 400
)
@Composable
fun PianoViewPreview() {
    PianoView(
        whiteKeyIds = emptyArray(),
        blackKeyIds = emptyArray(),
        whiteKeySounds = emptyArray(),
        blackKeySounds = emptyArray(),
        onPlaySound = {}
    )
}
private class RotatingPianoContainer(
    context: android.content.Context
) : android.widget.FrameLayout(context) {

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        val rotate = height > width

        getChildAt(0)?.measure(
            View.MeasureSpec.makeMeasureSpec(
                if (rotate) height else width,
                View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                if (rotate) width else height,
                View.MeasureSpec.EXACTLY
            )
        )

        setMeasuredDimension(width, height)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val child = getChildAt(0) ?: return
        val width = right - left
        val height = bottom - top

        val x = (width - child.measuredWidth) / 2
        val y = (height - child.measuredHeight) / 2

        child.layout(
            x,
            y,
            x + child.measuredWidth,
            y + child.measuredHeight
        )

        child.pivotX = child.measuredWidth / 2f
        child.pivotY = child.measuredHeight / 2f
        child.rotation = if (height > width) 90f else 0f
    }
}