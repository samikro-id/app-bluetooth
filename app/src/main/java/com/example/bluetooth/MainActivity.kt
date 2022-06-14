package com.example.bluetooth

import android.Manifest
import android.Manifest.permission
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(javaClass.simpleName, "Android ${Build.VERSION.SDK_INT}");

        if(bluetoothManager == null){
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if(bluetoothManager == null){
                Log.e(javaClass.simpleName, "unable to init BluetoothManager");
                return
            }
        }

        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d(javaClass.simpleName,"Device doesn't support bluetooth")
            Toast.makeText(applicationContext, "Your Device doesn't support bluetooth !!!", Toast.LENGTH_SHORT).show()
            finish();
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                val permissionCheck = ContextCompat.checkSelfPermission(this, permission.BLUETOOTH)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.i(javaClass.simpleName, "Bluetooth Permission not granted");

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.BLUETOOTH)) {
                        showExplanation(
                            "Permission Needed",
                            "Rationale",
                            arrayOf(
                                permission.BLUETOOTH,
                                permission.BLUETOOTH_CONNECT,
                                permission.BLUETOOTH_ADMIN
                            ),
                            REQUEST_ENABLE_BT
                        )
                    } else {
                        requestPermissionMulti(arrayOf(
                                                    permission.BLUETOOTH,
                                                    permission.BLUETOOTH_CONNECT,
                                                    permission.BLUETOOTH_ADMIN
                                                ), REQUEST_ENABLE_BT)
                    }
                    return
                }
                else{
                    Log.i(javaClass.simpleName, "Bluetooth Granted")
                    if (!bluetoothAdapter!!.isEnabled) {
                        Log.i(javaClass.simpleName, "Bluetooth is off")
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        resultEnableBt.launch(enableBtIntent)
                    }
                }
            }
            else{
                if (!bluetoothAdapter!!.isEnabled) {
                    Log.i(javaClass.simpleName, "Bluetooth is off")
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    resultEnableBt.launch(enableBtIntent)
                }
                else{
                    try{
                        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices

                        pairedDevices?.forEach{ device->
                            Log.i(javaClass.simpleName, "${device.name} : ${device.address}")
                        }
                    }
                    catch (err: Exception){
                        Log.e(javaClass.simpleName, err.message!!)
                    }
                }
            }
        }
    }

    private val resultEnableBt = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode == Activity.RESULT_OK){
            try{
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices

                pairedDevices?.forEach{ device->
                    Log.i(javaClass.simpleName, "${device.name} : ${device.address}")
                }
            }
            catch (err: Exception){
                Log.e(javaClass.simpleName, err.message!!)
            }

        }
        else{
            Toast.makeText(this, "Deny Enable BT", Toast.LENGTH_SHORT).show()
            appExit();
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,  grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.BLUETOOTH) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun showExplanation(
        title: String,
        message: String,
        permission: Array<String>,
        permissionRequestCode: Int
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    requestPermissionMulti(
                        permission, permissionRequestCode
                    )
                })
        builder.create().show()
    }

    fun requestPermissionMulti(permissionArray: Array<String>, permissionRequestCode: Int){
        ActivityCompat.requestPermissions(this, permissionArray, permissionRequestCode)
    }
    fun appExit(){
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2500)
    }
}