package io.fogcloud.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttActivity : AppCompatActivity() {
    private val TAG = "MqttActivity"
    private var client: MqttClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mqtt_client)
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()

        val persistence = MemoryPersistence()
        client = MqttClient("tcp://test.mosquitto.org:1883", "helele", persistence)

        try {
            Log.d(TAG, "now connecting...")
            client?.connect()
            Log.d(TAG, "Now publishing...")
            client?.publish("my/room", MqttMessage("Hello MQTT !".toByteArray()))
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
