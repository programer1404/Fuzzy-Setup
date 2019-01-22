package ir.sinapp.fuzzy

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var currentRed = 7
    private var currentBlue = 7
    private var currentGreen = 7
    private val fuzzy = Fuzzy(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showColorPickerDialog()

//        sample_text.text = String.format("%.3f",fuzzy.result(1700.0, 50.0))
    }

    private fun showColorPickerDialog() {

        tv_red.text = "$currentRed"
        tv_green.text = "$currentGreen"
        tv_blue.text = "$currentBlue"

        seekbar_red.progress = currentRed
        seekbar_green.progress = currentGreen
        seekbar_blue.progress = currentBlue

        updateView()

        view_result.setBackgroundColor(
            rgb(
                seekbar_red.progress.toFloat() / 15,
                seekbar_green.progress.toFloat() / 15,
                seekbar_blue.progress.toFloat() / 15
            )
        )

        seekbar_red.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tv_red.text = "$progress"
                currentRed = progress
                view_result.setBackgroundColor(
                    rgb(
                        seekbar_red.progress.toFloat() / 15,
                        seekbar_green.progress.toFloat() / 15,
                        seekbar_blue.progress.toFloat() / 15
                    )
                )
                updateView()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekbar_green.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tv_green.text = "$progress"
                currentGreen = progress
                view_result.setBackgroundColor(
                    rgb(
                        seekbar_red.progress.toFloat() / 15,
                        seekbar_green.progress.toFloat() / 15,
                        seekbar_blue.progress.toFloat() / 15
                    )
                )
                updateView()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekbar_blue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tv_blue.text = "$progress"
                currentBlue = progress
                view_result.setBackgroundColor(
                    rgb(
                        seekbar_red.progress.toFloat() / 15,
                        seekbar_green.progress.toFloat() / 15,
                        seekbar_blue.progress.toFloat() / 15
                    )
                )
                updateView()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

    }

    private fun updateView() {
        fuzzy.setColor(currentRed.toDouble(), currentGreen.toDouble(), currentBlue.toDouble())
        lbl_color.text = fuzzy.lblColor
//        lbl_degree.text = fuzzy.lblDegree
        lbl_lum.text = fuzzy.lblLux
    }

    fun rgb(red: Float, green: Float, blue: Float): Int {
        return -0x1000000 or
                ((red * 255.0f + 0.5f).toInt() shl 16) or
                ((green * 255.0f + 0.5f).toInt() shl 8) or
                (blue * 255.0f + 0.5f).toInt()
    }

/*    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }

    }
*/

}
