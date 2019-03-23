package io.fogcloud.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
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
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.fogcloud.sdk.easylink.api.EasyLink
import io.fogcloud.sdk.easylink.api.EasylinkP2P
import io.fogcloud.sdk.easylink.helper.EasyLinkCallBack
import io.fogcloud.sdk.easylink.helper.EasyLinkParams
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket

class SktEasyLinkActivity : AppCompatActivity() {

    private val TAG = "SktEasyLinkActivity"
    private var mContext: Context? = null
    private var editTextLog: EditText? = null
    private var textViewEasyLink: TextView? = null
    private var elp2p: EasylinkP2P? = null
    private var psw : EditText? = null
    private var ssid : EditText? = null

    internal var LHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            Log.d(TAG, "handleMessage what:" + msg.what)
            if (msg.what == 1) {
                //폰 정보 수신 완료
                val jsonStr = msg.obj.toString()
                editTextLog!!.setText(jsonStr.trim { it <= ' ' } + "\r\n")
                stopEasyLink(elp2p!!)

                try {
                    val easyLinkResponse: EasyLinkResponse = Gson().fromJson(jsonStr.replace("[^\\x0A\\x0D\\x20-\\x7E]", ""), EasyLinkResponse::class.java)
                    SocketTask().execute(easyLinkResponse)
                } catch (e: JsonSyntaxException) {
                    Log.e("invalid json", jsonStr)
                    Toast.makeText(this@SktEasyLinkActivity, "invalid Json string", Toast.LENGTH_LONG).show()
                }
            }
            if (msg.what == 2) {
                //do nothing
            }
        }
    }

    class SocketTask : AsyncTask<EasyLinkResponse, Void, String>() {
        private var socket : Socket? = null
        private var networkReader: BufferedReader? = null

        override fun doInBackground(vararg params: EasyLinkResponse?): String {
            val p0 : EasyLinkResponse = params[0]!!
            Log.d("SocketTask", p0.IP)
            try {
                socket = Socket(p0.IP, 5000)
            } catch (e: Exception) {
                Log.e("SocketTask", "Exception:$e")
            }

            networkReader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            val dos = DataOutputStream(socket!!.getOutputStream())

            val json = """
                {"type":"REQ","code":"DP1200","key":"e7aa7f5e-15df-4ea1-9e7b-e4f05d2ac288","ip":"223.39.117.11","port":"31147","serviceid":"WINIXRND1D"}
            """.trimIndent()
            val jsonByte = json.toByteArray()
            val header: ByteArray = byteArrayOf(0x76,0x31,0x30,0x30,0x0,0x0,0x0,jsonByte.size.toByte())
            val payload: ByteArray = header + jsonByte

            Log.d("SocketTask", "write to socket")
            try {
                dos.write(payload, 0, payload.size)
            } catch (e: Exception) {
                Log.e("SocketTask", "Exception:" + e.toString())
            }
            return ""
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skt_easylink)

        mContext = this
        val el = EasyLink(this@SktEasyLinkActivity)
        elp2p = EasylinkP2P(mContext)


        textViewEasyLink = findViewById<TextView>(R.id.easylinktest)
        psw = findViewById<EditText>(R.id.psw)
        ssid = findViewById<EditText>(R.id.ssid)
        ssid?.setText(el.ssid)

        editTextLog = findViewById<EditText>(R.id.log)
        if (editTextLog != null) {
            editTextLog!!.setText("")
        }

        //go to Mqtt Test
        val buttonGotoMqtt = findViewById<Button>(R.id.button_goto_mqtt)
        buttonGotoMqtt!!.setOnClickListener { view ->
            val newIntent = Intent(this, MqttActivity::class.java)
            startActivity(newIntent)
        }

        if ((textViewEasyLink == null) or (ssid == null) or (psw == null)) {
            return
        }

        textViewEasyLink!!.setOnClickListener {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = currentFocus
            imm.hideSoftInputFromWindow(view!!.windowToken, 0)

            if (textViewEasyLink!!.text.toString().equals("Start EasyLink", ignoreCase = true)) {
                startEasyLink(ssid, psw, elp2p!!)
            } else {
                stopEasyLink(elp2p!!)
            }
        }
    }

    private fun stopEasyLink(elp2p: EasylinkP2P) {
        Log.d(TAG,"stopEasyLink()")
        textViewEasyLink!!.text = "Start EasyLink"
        textViewEasyLink!!.setBackgroundColor(Color.rgb(63, 81, 181))
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

    private fun startEasyLink(ssid: EditText?, psw: EditText?, elp2p: EasylinkP2P) {
        Log.d(TAG,"startEasyLink()")

        textViewEasyLink!!.text = "Stop EasyLink"
        textViewEasyLink!!.setBackgroundColor(Color.rgb(255, 0, 0))
        val elp = EasyLinkParams()
        elp.ssid = ssid!!.text.toString().trim { it <= ' ' }
        elp.password = psw!!.text.toString().trim { it <= ' ' }
        elp.sleeptime = 50
        elp.runSecond = 50
        Toast.makeText(mContext, "open easylink", Toast.LENGTH_SHORT).show()

        editTextLog!!.setText("")

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
    }

    private fun send2handler(code: Int, message: String) {
        Log.d(TAG, "send2handler code:" + code + ", message:" + message)

        val msg = Message()
        msg.what = code
        msg.obj = message
        LHandler.sendMessage(msg)
    }
}

