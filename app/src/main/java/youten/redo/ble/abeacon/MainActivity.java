/*
 * Copyright (C) 2014 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package youten.redo.ble.abeacon;

import java.util.UUID;

import youten.redo.ble.util.BleUtil;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "aBeacon";
    private static final int REQUEST_ENABLE_BT = 1;
    // BT
    private BluetoothAdapter mBTAdapter;
    private BluetoothLeAdvertiser mBTAdvertiser;
    private BluetoothGattServer mGattServer;
    // View
    private Button mIBeaconButton;
    private Button mIASButton;
    private Button mStopButton;

    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        public void onStartSuccess(android.bluetooth.le.AdvertiseSettings settingsInEffect) {
            // Advする際に設定した値と実際に動作させることに成功したSettingsが違うとsettingsInEffectに
            // 有効な値が格納される模様です。設定通りに動かすことに成功した際にはnullが返る模様。
            if (settingsInEffect != null) {
                Log.d(TAG, "onStartSuccess TxPowerLv="
                        + settingsInEffect.getTxPowerLevel()
                        + " mode=" + settingsInEffect.getMode()
                        + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Log.d(TAG, "onStartSuccess, settingInEffect is null");
            }
            mIBeaconButton.setEnabled(false);
            mIASButton.setEnabled(false);
            mStopButton.setEnabled(true);
            setProgressBarIndeterminateVisibility(false);
        }

        public void onStartFailure(int errorCode) {
            Log.d(TAG, "onStartFailure errorCode=" + errorCode);
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAdvertise();
    }

    private void init() {
        // BLE check
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // BT check
        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if ((mBTAdapter == null) || (!mBTAdapter.isEnabled())) {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        mIBeaconButton = (Button) findViewById(R.id.ibeacon_button);
        mIBeaconButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startIBeaconAdvertise();
            }
        });
        mIASButton = (Button) findViewById(R.id.ias_button);
        mIASButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startIASAdvertise();
            }
        });
        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAdvertise();
            }
        });
    }

    // start Advertise as iBeacon
    private void startIBeaconAdvertise() {
        if (mBTAdapter == null) {
            return;
        }
        if (mBTAdvertiser == null) {
            mBTAdvertiser = mBTAdapter.getBluetoothLeAdvertiser();
        }
        if (mBTAdvertiser != null) {
            mBTAdvertiser.startAdvertising(
                    BleUtil.createAdvSettings(true, 0),
                    BleUtil.createIBeaconAdvertiseData(
                            UUID.fromString("01020304-0506-0708-1112-131415161718"),
                            (short) 257, (short) 514, (byte) 0xc5),
                    mAdvCallback);
        }
    }

    // start Advertise as Immediate Alert Service
    private void startIASAdvertise() {
        if (mBTAdapter == null) {
            return;
        }
        if (mBTAdvertiser == null) {
            mBTAdvertiser = mBTAdapter.getBluetoothLeAdvertiser();
        }
        if (mBTAdvertiser != null) {
            ImmediateAlertService ias = new ImmediateAlertService();
            mGattServer = BleUtil.getManager(this).openGattServer(this, ias);
            ias.setupServices(mGattServer);
            
            mBTAdvertiser.startAdvertising(
                    BleUtil.createAdvSettings(true, 0),
                    BleUtil.createFMPAdvertiseData(),
                    mAdvCallback);
        }
    }

    private void stopAdvertise() {
        if (mGattServer != null) {
            mGattServer.clearServices();
            mGattServer.close();
            mGattServer = null;
        }
        if (mBTAdvertiser != null) {
            mBTAdvertiser.stopAdvertising(mAdvCallback);
            mBTAdvertiser = null;
        }
        mIBeaconButton.setEnabled(true);
        mIASButton.setEnabled(true);
        mStopButton.setEnabled(false);
        setProgressBarIndeterminateVisibility(false);
    }
}
