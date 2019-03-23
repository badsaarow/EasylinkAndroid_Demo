package io.fogcloud.demo

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import kotlinx.android.synthetic.main.activity_demo_list.*

class DemoListActivity : AppCompatActivity() {

    private var buttonEasyLink: Button? = null
    private var buttonSktEasyLink: Button? = null
    private var buttonMqtt: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_list)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Welcome! Edworks!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        buttonEasyLink = findViewById(R.id.buttonEasyLink)
        buttonEasyLink!!.setOnClickListener{ view ->
            val newIntent = Intent(this, MainActivity::class.java)
            startActivity(newIntent)
        }

        buttonSktEasyLink = findViewById(R.id.buttonSktEasyLink)
        buttonSktEasyLink!!.setOnClickListener{ view ->
            val newIntent = Intent(this, SktEasyLinkActivity::class.java)
            startActivity(newIntent)
        }

        buttonMqtt = findViewById(R.id.buttonMqtt)
        buttonMqtt!!.setOnClickListener{ view ->
            val newIntent = Intent(this, MqttActivity::class.java)
            startActivity(newIntent)
        }
    }
}
