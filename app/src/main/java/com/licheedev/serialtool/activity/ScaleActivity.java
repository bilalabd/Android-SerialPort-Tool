package com.licheedev.serialtool.activity;

import androidx.appcompat.app.AppCompatActivity;
import co.sspp.library.SweetAlertDialog;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.SerialPortFinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.licheedev.serialtool.R;
import com.licheedev.serialtool.activity.base.BaseActivity;
import com.licheedev.serialtool.activity.base.HttpBaseActivity;
import com.licheedev.serialtool.comn.Device;
import com.licheedev.serialtool.comn.SerialPortManager;
import com.licheedev.serialtool.util.PrefHelper;
import com.licheedev.serialtool.util.ToastUtil;
import com.licheedev.serialtool.util.constant.PreferenceKeys;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.licheedev.serialtool.R.array.baudrates;

public class ScaleActivity extends HttpBaseActivity implements IScaleReadCallback {

    private Device mDevice;
    private boolean mOpened = false;
    private EditText mBarcodeTxt;
    private EditText mScaleTxt;
    private boolean mIsScale = false;
    private boolean mSwitching = false;
    private TextView mScaleCounter;
    protected SharedPreferences mPrefs;
    protected SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale);

        Button openScannerBtn = findViewById(R.id.btn_open_scanner);
        Button closeScannerBtn = findViewById(R.id.btn_close_scanner);
        Button startScanBtn = findViewById(R.id.btn_start_scan);
        Button startScaleBtn = findViewById(R.id.btn_start_scale);
        Button saveScaleBtn = findViewById(R.id.btn_save_scale);
        Button resetScale = findViewById(R.id.btn_reset_scale);

        mBarcodeTxt = findViewById(R.id.barcode_edtxt);
        mScaleTxt = findViewById(R.id.weight_edtxt);
        mScaleCounter = findViewById(R.id.scale_counter);

        mPrefs = this.getSharedPreferences("SMARTLIFE_PREFS", MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mScaleCounter.setText(mPrefs.getString("COUNTER", "0"));




        openScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceIO.write("/sys/devices/platform/x6818-scanner/scanner_power", String.valueOf(0));
            }
        });

        closeScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceIO.write("/sys/devices/platform/x6818-scanner/scanner_power", String.valueOf(1));
            }
        });

        startScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice = new Device("/dev/ttySAC3", "19200");
                mIsScale = false;
                switchSerialPort();
            }
        });

        startScaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice = new Device("/dev/ttySAC2", "9600");
                mIsScale = true;
                switchSerialPort();
            }
        });

        saveScaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switchSerialPort();

                String barcode  = mBarcodeTxt.getText().toString().trim();
                String weight  = mScaleTxt.getText().toString().trim();

                if(barcode.isEmpty() ||
                        barcode == "" ||
                        weight.isEmpty() ||
                        weight == "" ||
                        weight.equals("0")) {
                   showErrorAlert("Scale or Barcode is empty!");
                   return;
                }

                Map<String, String>  params = new HashMap<String, String>();
                params.put("barcode", barcode);
                params.put("weight", weight);
                showActivityIndicator(getResources().getString(R.string.saving));
                makeHttpRequest(ApiMethods.SCALE, ApiUrl.SCALE_URL.url(), params);
            }
        });

        resetScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDevice = new Device("/dev/ttySAC2", "9600");
                if(mIsScale) {
                    SerialPortManager.instance().sendCommand("7A");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //DeviceIO.write("/sys/devices/platform/x6818-scanner/scanner_power", String.valueOf(1));
    }



    /*@Override
    protected int getLayoutId() {
        return R.layout.activity_scale;
    }*/

    private void showErrorAlert(String msg) {
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#FF0000"));
        dialog.setTitleText("Error");
        dialog.setContentText(msg);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void switchSerialPort() {
        /*if (mOpened) {
            SerialPortManager.instance().close();
            mOpened = false;
        } else {
            SerialPortManager serialPortManager = SerialPortManager.instance();
            serialPortManager.setScaleReadCallback(this);
            mOpened = serialPortManager.open(mDevice) != null;
        }*/
        if(mOpened) {
            SerialPortManager.instance().close();
        }
        SerialPortManager serialPortManager = SerialPortManager.instance();
        serialPortManager.setScaleReadCallback(this);
        mOpened = serialPortManager.open(mDevice) != null;
    }

    @Override
    public void onScaleReadData(String data) {

        runOnUiThread(new Runnable() {
            public void run() {
                if(mSwitching) {
                    mSwitching = false;
                    return;
                }
                if(!mIsScale) {
                    mBarcodeTxt.setText(data);

                    if(mOpened) {
                        switchSerialPort();
                    }
                    if(!mIsScale) {
                        mDevice = new Device("/dev/ttySAC2", "9600");
                        mIsScale = true;
                        mSwitching = true;
                        switchSerialPort();
                    }

                } else {
                    mScaleTxt.setText(data.replaceAll("\\D+",""));
                    if(mOpened) {
                        // Bilal
                         mIsScale = true;
                        switchSerialPort();
                    }
                }
            }
        });
    }



    @Override
    protected void handleResponse(JSONObject response, ApiMethods method) {
        super.handleResponse(response, method);
        hideActivityIndicator();
        switchSerialPort();

        try {
            String code = response.getString("code");
            if(code.equals("1")) {
                String message = response.getString("msg");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            } else {
                String message = response.getString("msg");

                switchSerialPort();
                mDevice = new Device("/dev/ttySAC3", "19200");

                switchSerialPort();

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                int counter = Integer.parseInt(mPrefs.getString("COUNTER", "0"));
                ++counter;
                mEditor.putString("COUNTER", ""+counter);
                mEditor.apply();
                mScaleCounter.setText(""+counter);

            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_LONG).show();
        } finally {
            mIsScale = false;
            mBarcodeTxt.setText("");
            mScaleTxt.setText("");
        }
    }
}