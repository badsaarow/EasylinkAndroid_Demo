package io.fogcloud.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.fogcloud.sdk.easylink.api.EasyLink
import io.fogcloud.sdk.easylink.api.EasylinkP2P
import io.fogcloud.sdk.easylink.helper.EasyLinkCallBack
import io.fogcloud.sdk.easylink.helper.EasyLinkParams

class MainActivity : AppCompatActivity() {

    private val TAG = "---main---"
    private var mContext: Context? = null
    private var log_view: EditText? = null

    internal var LHandler: Handler = object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            Log.d(TAG, "handleMessage what:" + msg.what)
            if (msg.what == 1) {
                log_view!!.setText(msg.obj.toString().trim { it <= ' ' } + "\r\n")
            }
            if (msg.what == 2) {
                log_view!!.setText("")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this
        val el = EasyLink(this@MainActivity)
        val elp2p = EasylinkP2P(mContext)


        val easylinktest = findViewById(R.id.easylinktest) as TextView?
        val psw = findViewById(R.id.psw) as EditText?
        val ssid = findViewById(R.id.ssid) as EditText?
        ssid?.setText(el.ssid)

        log_view = findViewById(R.id.log) as EditText?
        if (log_view != null) {
            log_view!!.setText("")
        }

        //go to Mqtt Test
        val buttonGotoMqtt = findViewById(R.id.button_goto_mqtt) as Button?
        buttonGotoMqtt!!.setOnClickListener { view ->
            val newIntent = Intent(this, MqttActivity::class.java)
            startActivity(newIntent)
        }

        //go to Skt EasyLink
        val buttonGotoSktEasyLink = findViewById(R.id.button_goto_skt_easylink) as Button?
        buttonGotoSktEasyLink!!.setOnClickListener { view ->
            val newIntent = Intent(this, SktEasyLinkActivity::class.java)
            startActivity(newIntent)
        }


        if ((easylinktest != null) and (ssid != null) and (psw != null)) {
            easylinktest!!.setOnClickListener {
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                val view = currentFocus
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)

                if (easylinktest.text.toString().equals("Start EasyLink", ignoreCase = true)) {
                    easylinktest.text = "Stop EasyLink"
                    Log.d(TAG, easylinktest.text.toString())
                    easylinktest.setBackgroundColor(Color.rgb(255, 0, 0))
                    val elp = EasyLinkParams()
                    elp.ssid = ssid!!.text.toString().trim { it <= ' ' }
                    elp.password = psw!!.text.toString().trim { it <= ' ' }
                    elp.sleeptime = 50
                    elp.runSecond = 50
                    Toast.makeText(mContext, "open easylink", Toast.LENGTH_SHORT).show()

                    elp2p.startEasyLink(elp, object : EasyLinkCallBack {
                        override fun onSuccess(code: Int, message: String) {
                            Log.d(TAG, "onSuccess")
                            Log.d(TAG, message)
                            send2handler(1, message)
                        }

                        override fun onFailure(code: Int, message: String) {
                            Log.d(TAG, message)
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    easylinktest.text = "Start EasyLink"
                    Log.d(TAG, easylinktest.text.toString())
                    easylinktest.setBackgroundColor(Color.rgb(63, 81, 181))
                    Toast.makeText(mContext, "stop easylink", Toast.LENGTH_SHORT).show()
                    elp2p.stopEasyLink(object : EasyLinkCallBack {
                        override fun onSuccess(code: Int, message: String) {

                            Log.d(TAG, message)
                            send2handler(2, message)
                        }

                        override fun onFailure(code: Int, message: String) {
                            Log.d(TAG, message)
                        }
                    })
                }
            }
        }
    }

    private fun send2handler(code: Int, message: String) {
        Log.d(TAG, "send2handler code:$code, message:$message")

        val msg = Message()
        msg.what = code
        msg.obj = message
        LHandler.sendMessage(msg)
    }
}

