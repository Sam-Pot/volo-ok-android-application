package com.example.volook

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.volook.databinding.ActivityMainBinding
import com.example.volook.login.LoginActivity
import com.example.volook.scanner.ScannerQR
import com.example.volook.shared.configs.Constants
import com.example.volook.shared.helpers.LocationManager
import com.example.volook.shared.services.JwtService
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), SensorEventListener {

private lateinit var binding: ActivityMainBinding
    //LOGIN
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var intent: Intent
    //LIGHT
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    lateinit var scanFloatingButton: FloatingActionButton
    //INSTANCE
    companion object {
        lateinit var instance: MainActivity
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        sharedPreferences = this@MainActivity.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        intent = getIntent()
        val firstStartUp:Boolean = intent.getBooleanExtra(Constants.FIRST_STARTUP, true)
        val keepAccess: Boolean = sharedPreferences.getString(Constants.KEEP_ACCESS, "false").toBoolean()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(JwtService.readJwt(instance).isBlank() || firstStartUp){
            //setContentView(R.layout.activity_login)
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
            finish()
        }else{
            //NAVIGATION
            setContentView(binding.root)
            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_flights, R.id.navigation_bookings, R.id.navigation_tickets, R.id.navigation_account))
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            //QR SCANNER
            scanFloatingButton = binding.scan
            scanFloatingButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(MainActivity.instance, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.instance, arrayOf(Manifest.permission.CAMERA), 1)
                } else {
                    val scanActivity = Intent(MainActivity.instance, ScannerQR::class.java)
                    startActivity(scanActivity)
                }
            }
        }
        //LIGHT SENSOR MANAGEMENT
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    //ACTION BAR NAVIGATION
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() || super.onSupportNavigateUp()
    }

    //LIGHT SENSOR
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightLevel = event.values[0]
            if (lightLevel < 10) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    //LOCATION SENSOR
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationManager.REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "PERMESSO NEGATO", Toast.LENGTH_SHORT).show()
            }
        }
    }
}