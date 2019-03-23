package io.fogcloud.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttActivity : AppCompatActivity(), MqttCallback {
    private val TAG = "MqttActivity"
    private lateinit var client: MqttClient
    private lateinit var textViewUrl : TextView
    private lateinit var textViewTopic : TextView
    private lateinit var editTextPub : EditText
    private lateinit var textViewSub : TextView
    private lateinit var buttonPub : Button
    private lateinit var buttonConnect : Button
    private lateinit var buttonDisconnect : Button

    override fun messageArrived(topic: String?, message: MqttMessage) {
        val msg = message.toString()
        Log.d(TAG,"messageArrived : $msg")
        Toast.makeText(this.applicationContext, msg, Toast.LENGTH_LONG).show()
        textViewSub.text = msg
    }

    override fun connectionLost(cause: Throwable?) {
        Log.i(TAG,"connectionLost")
        buttonConnect.visibility = View.VISIBLE
        buttonDisconnect.visibility = View.GONE
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.i(TAG,"deliveryComplete")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mqtt_client)
        textViewUrl = findViewById(R.id.textViewUrl)
        textViewTopic = findViewById(R.id.textViewTopic)
        editTextPub = findViewById(R.id.editTextPub)
        textViewSub = findViewById(R.id.textViewSub)

        buttonPub = findViewById(R.id.buttonPub)
        buttonPub.setOnClickListener{ view ->
            pubMessage()
        }

        val persistence = MemoryPersistence()
        client = MqttClient(textViewUrl.text as String?, "clientID", persistence)

        buttonConnect = findViewById(R.id.buttonConnect)
        buttonConnect.setOnClickListener{ view ->



            val topic = textViewTopic.text.toString()

            try {
                val msg = "now connecting... " + textViewUrl.text as String?
                Log.d(TAG, msg)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                client.setCallback(this)
                client.connect()
                client.subscribe(topic, 1)

                buttonConnect.visibility = View.GONE
                buttonDisconnect.visibility = View.VISIBLE
            } catch (ex: MqttException) {
                ex.printStackTrace()
                Toast.makeText(this, "$ex", Toast.LENGTH_LONG).show()
            }
        }

        buttonDisconnect = findViewById(R.id.buttonDisconnect)
        buttonDisconnect.setOnClickListener{ view ->
            if (client.isConnected) {
                client.disconnect()
            }
        }
    }

    private fun pubMessage() {
        val msg : String = editTextPub.text.toString()
        Log.i(TAG, "Pub Msg: $msg")

        if (!client.isConnected) {
            Toast.makeText(this, "Not Connected", Toast.LENGTH_LONG).show()
            return
        }

        try {
            client.publish(textViewTopic.text as String?, MqttMessage(msg.toByteArray()))
        } catch (ex: MqttException) {
            ex.printStackTrace()
            Toast.makeText(this, "$ex", Toast.LENGTH_LONG).show()
        }

    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()

        buttonConnect.visibility = View.VISIBLE
        buttonDisconnect.visibility = View.GONE
    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        if (client.isConnected) {
            client.disconnect()
        }
        super.onPause()
    }
}
