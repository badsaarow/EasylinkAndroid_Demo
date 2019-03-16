package io.fogcloud.demo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.w3c.dom.Text

class MqttActivity : AppCompatActivity() {
    private val TAG = "MqttActivity"
    private var client: MqttClient? = null

    private var textViewUrl : TextView? = null
    private var textViewTopic : TextView? = null
    private var editTextPub : EditText? = null
    private var editTextSub : EditText? = null
    private var buttonPub : Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mqtt_client)

        textViewUrl = findViewById(R.id.textViewUrl) as TextView?
        textViewTopic = findViewById(R.id.textViewTopic) as TextView?
        editTextPub = findViewById(R.id.editTextPub) as EditText?
        editTextSub = findViewById(R.id.editTextSub) as EditText?

        buttonPub = findViewById(R.id.buttonPub) as Button?

        buttonPub!!.setOnClickListener{ view ->
            pubMessage()
        }
    }

    fun pubMessage() {
        var msg : String = editTextPub!!.text.toString()
        Log.i(TAG, "Pub Msg: " + msg)

        try {
            client?.publish(textViewTopic!!.text as String?, MqttMessage(msg!!.toByteArray()))
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }

    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()

        val persistence = MemoryPersistence()
        client = MqttClient(textViewUrl!!.text as String?, "clientID", persistence)

        try {
            Log.d(TAG, "now connecting... " + textViewUrl!!.text as String?)
            client?.connect()
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }

    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        client?.disconnect()

        super.onPause()
    }
}
