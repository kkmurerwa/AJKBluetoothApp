package com.example.ajkbluetoothlightapp;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //Global variables start
    private OutputStream outStream = null;//Helps push information to the Arduino Board
    //Bulb status below helps maintain when the bulbs are on or off
    int number_of_bulbs_on = 0;
    boolean bulb_one_status = false;
    boolean bulb_two_status = false;
    boolean bulb_three_status = false;
    boolean bluetoothConnectionState = false;
    private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();//Gets address of default adapter
    private final UUID my_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//Sets UUID
    //Global Variables End
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button)findViewById(R.id.getBtDevices);
        btn.setOnClickListener(new View.OnClickListener() {
            //OnClick Listener for the bluetooth connection button
            @Override
            public void onClick(View v) {
                if (bAdapter.isEnabled()){
                    //This method is called when the Connect Bluetooth Button is pressed
                        if(bAdapter==null){
                            //Displays error if bluetooth adapter is missing
                            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_LONG).show();
                        }
                        else{
                            Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
                            if(pairedDevices.size()>0){
                                for(BluetoothDevice device: pairedDevices){
                                    String devicename = device.getName();
                                    String macAddress = device.getAddress();
                                    if (devicename.equals("HC-06")){//Connects to any Low-energy device called HC-06
                                        final BluetoothDevice connect_device = bAdapter.getRemoteDevice(macAddress);
                                        new CountDownTimer(10000, 10000) {
                                            public void onTick(long millisUntilFinished) {
                                                try {
                                                    BluetoothSocket socket = connect_device.createInsecureRfcommSocketToServiceRecord(my_UUID);//Create connection socket to bluetooth
                                                    socket.connect();//Creates connection
                                                    outStream = socket.getOutputStream();//Creates an output stream for Arduino-bound communication
                                                    bluetoothConnectionState = true;
                                                    onFinish();
                                                } catch (Exception e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();//Catches errors
                                                }
                                            }
                                            public void onFinish() {
                                                if (!bluetoothConnectionState){
                                                    Toast.makeText(MainActivity.this, "Failed to connect to bluetooth device", Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    Toast.makeText(MainActivity.this, "Bluetooth Module Connected", Toast.LENGTH_SHORT).show();//Creates toast on connection
                                                }
                                            }

                                        }.start();
                                    }
                                }

                            }
                            else {
                                Toast.makeText(MainActivity.this, "Your Phone Has No Paired Devices\nGo to Settings>Bluetooth>Add Devices and pair with" +
                                        "the Bluetooth Module HC-06", Toast.LENGTH_LONG).show();
                            }

                        }
                }
                else {
                    //Code below requests the user to turn on bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        //This method  performs actions if menu items on the title bar menu
        //are passed
        switch (item.getItemId()) {
            case R.id.help:
                AlertDialog helpAlertDialog = new AlertDialog.Builder(MainActivity.this).create();
                helpAlertDialog.setTitle("Help");
                helpAlertDialog.setMessage("You are beyond help");
                helpAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CLOSE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                helpAlertDialog.show();
                return true;

            case R.id.about:
                AlertDialog aboutAlertDialog = new AlertDialog.Builder(MainActivity.this).create();
                aboutAlertDialog.setTitle("About");
                aboutAlertDialog.setMessage("We are Anonymous");
                aboutAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CLOSE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                aboutAlertDialog.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    public void main_switch_on(View view) {
        Button main_switch = (Button) findViewById(R.id.master_switch_off);
        if (bluetoothConnectionState){
            if (number_of_bulbs_on == 0 || !bulb_one_status || !bulb_two_status || !bulb_three_status){
                number_of_bulbs_on = 0;
                try {
                    //The code below sends messages to the Arduino Board via the BT module
                    outStream.write("1".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                main_switch.setBackgroundResource(R.drawable.main_switch_buttons);//Sets bg to yellow
                main_switch.setTextColor(Color.WHITE);
                bulb_one_status = false;
                bulb_two_status = false;
                bulb_three_status = false;
                bulb_one(view);
                bulb_two(view);
                bulb_three(view);
            }
            else {
                Toast.makeText(this, "All bulbs are on", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Bluetooth module not connected", Toast.LENGTH_SHORT).show();
        }
    }
    public void main_switch_off(View view) {
        Button main_switch = (Button) findViewById(R.id.master_switch_off);
        if (bluetoothConnectionState){
            if (number_of_bulbs_on == 3 || bulb_one_status || bulb_two_status || bulb_three_status) {
                number_of_bulbs_on = 3;
                try {
                    outStream.write("0".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                main_switch.setBackgroundResource(R.drawable.main_switch_buttons);
                main_switch.setTextColor(Color.WHITE);
                bulb_one_status = true;
                bulb_two_status = true;
                bulb_three_status = true;
                bulb_one(view);
                bulb_two(view);
                bulb_three(view);
            }
            else {
                Toast.makeText(this, "All bulbs are off", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Bluetooth module not connected", Toast.LENGTH_SHORT).show();
        }
    }
    public void bulb_one(View view) {
        Button bulb_one_switch = (Button) findViewById(R.id.bulb_one);
        if (bluetoothConnectionState){
            if (!bulb_one_status){
                try {
                    //The code below sends messages to the Arduino Board via the BT module
                    outStream.write("3".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bulb_one_status = true;
                number_of_bulbs_on += 1;
                bulb_one_switch.setBackgroundResource(R.drawable.rounded_button_on);
                bulb_one_switch.setTextColor(Color.BLACK);
            }
            else {
                try {
                    //The code below sends messages to the Arduino Board via the BT module
                    outStream.write("2".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bulb_one_status = false;
                number_of_bulbs_on -= 1;
                bulb_one_switch.setBackgroundResource(R.drawable.rounded_button_off);
                bulb_one_switch.setTextColor(Color.WHITE);
            }
        }
        else {
            Toast.makeText(this, "Bluetooth module not connected", Toast.LENGTH_SHORT).show();
        }
    }
    public void bulb_two(View view) {
        Button bulb_two_switch = (Button) findViewById(R.id.bulb_two);
        if (bluetoothConnectionState){
            if (!bulb_two_status){
                try {
                    //The code below sends messages to the Arduino Board via the BT module
                    outStream.write("5".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bulb_two_status = true;
                number_of_bulbs_on += 1;
                bulb_two_switch.setBackgroundResource(R.drawable.rounded_button_on);
                bulb_two_switch.setTextColor(Color.BLACK);
            }
            else {
                try {
                    //The code below sends messages to the Arduino Board via the BT module
                    outStream.write("4".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bulb_two_status = false;
                number_of_bulbs_on -= 1;
                bulb_two_switch.setBackgroundResource(R.drawable.rounded_button_off);
                bulb_two_switch.setTextColor(Color.WHITE);
            }
        }
        else {
            Toast.makeText(this, "Bluetooth module not connected", Toast.LENGTH_SHORT).show();
        }
    }
    public void bulb_three(View view) {
        Button bulb_three_switch = (Button) findViewById(R.id.bulb_three);
        if (bluetoothConnectionState){
            if (!bulb_three_status){
                try {
                    //The code below sends messages to the Arduino Board via the BT module
                    outStream.write("7".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bulb_three_status = true;
                number_of_bulbs_on += 1;
                bulb_three_switch.setBackgroundResource(R.drawable.rounded_button_on);
                bulb_three_switch.setTextColor(Color.BLACK);
            }
            else {
                try {
                    //The code below sends messages to the Arduino Board via the BT module
                    outStream.write("6".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bulb_three_status = false;
                number_of_bulbs_on -= 1;
                bulb_three_switch.setBackgroundResource(R.drawable.rounded_button_off);
                bulb_three_switch.setTextColor(Color.WHITE);
            }
        }
        else {
            Toast.makeText(this, "Bluetooth module not connected", Toast.LENGTH_SHORT).show();
        }
    }
}

