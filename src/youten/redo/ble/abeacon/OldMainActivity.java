
package youten.redo.ble.abeacon;

import java.util.UUID;

import youten.redo.ble.util.BleUtil;
import youten.redo.ble.util.BleUuid;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class OldMainActivity extends Activity {
    private static final String TAG = "aBeacon";
    private BluetoothManager mBTManager;
    private BluetoothAdapter mBTAdapter;
    private BluetoothGattServer mGattServer;
    private BluetoothLeAdvertiser mBTAdvertiser;
    private boolean mIsAdvertising = false;
    private byte[] mAlertLevel = new byte[] {
            (byte) 0x00
    };

    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {

//        @Override
//        public void onSuccess(AdvertiseSettings settingsInEffect) {
//            // Advする際に設定した値と実際に動作させることに成功したSettingsが違うとsettingsInEffectに
//            // 有効な値が格納される模様です。設定通りに動かすことに成功した際にはnullが返る模様。
//            if (settingsInEffect != null) {
//                Log.d(TAG, "onSuccess TxPowerLv="
//                        + settingsInEffect.getTxPowerLevel()
//                        + " mode=" + settingsInEffect.getMode()
//                        + " type=" + settingsInEffect.getType());
//            } else {
//                Log.d(TAG, "onSuccess, settingInEffect is null");
//            }
//        }
//
//        @Override
//        public void onFailure(int errorCode) {
//            Log.d(TAG, "onFailure errorCode=" + errorCode);
//        }
    };

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        public void onServiceAdded(int status, BluetoothGattService service) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServiceAdded status=GATT_SUCCESS service="
                        + service.getUuid().toString());
            } else {
                Log.d(TAG, "onServiceAdded status!=GATT_SUCCESS");
            }
        };

        public void onConnectionStateChange(android.bluetooth.BluetoothDevice device, int status,
                int newState) {
            // Log.d(TAG, "onConnectionStateChange status=" + status + "->" + newState);
        };

        // とにかくなんでもReadRequestとWriteRequestを通るっぽいので
        public void onCharacteristicReadRequest(android.bluetooth.BluetoothDevice device,
                int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
            if (characteristic.getUuid().equals(
                    UUID.fromString(BleUuid.CHAR_MANUFACTURER_NAME_STRING))) {
                Log.d(TAG, "CHAR_MANUFACTURER_NAME_STRING");
                characteristic.setValue("Name:Hoge");
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            } else if (characteristic.getUuid().equals(
                    UUID.fromString(BleUuid.CHAR_MODEL_NUMBER_STRING))) {
                Log.d(TAG, "CHAR_MODEL_NUMBER_STRING");
                characteristic.setValue("Model:Redo");
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            } else if (characteristic.getUuid().equals(
                    UUID.fromString(BleUuid.CHAR_SERIAL_NUMBEAR_STRING))) {
                Log.d(TAG, "CHAR_SERIAL_NUMBEAR_STRING");
                characteristic.setValue("Serial:777");
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            } else if (characteristic.getUuid().equals(
                    UUID.fromString(BleUuid.CHAR_ALERT_LEVEL))) {
                Log.d(TAG, "CHAR_ALERT_LEVEL");
                characteristic.setValue(mAlertLevel);
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }
        };

        public void onCharacteristicWriteRequest(android.bluetooth.BluetoothDevice device,
                int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                boolean responseNeeded, int offset, byte[] value) {
            Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                    + Boolean.toString(preparedWrite) + " responseNeeded="
                    + Boolean.toString(responseNeeded) + " offset=" + offset);
            if (characteristic.getUuid().equals(
                    UUID.fromString(BleUuid.CHAR_ALERT_LEVEL))) {
                Log.d(TAG, "CHAR_ALERT_LEVEL");
                if (value != null && value.length > 0) {
                    Log.d(TAG, "value.length=" + value.length);
                    mAlertLevel[0] = value[0];
                } else {
                    Log.d(TAG, "invalid value written");
                }
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        null);
            }
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
    protected void onStart() {
        super.onStart();

        startAdvertise();
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
        mBTManager = BleUtil.getManager(this);
        if (mBTManager != null) {
            mBTAdapter = mBTManager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void startAdvertise() {
        if ((mBTAdapter != null) && (!mIsAdvertising)) {
            mGattServer = mBTManager.openGattServer(this, mGattServerCallback);
            { // immediate alert
                BluetoothGattService ias = new BluetoothGattService(
                        UUID.fromString(BleUuid.SERVICE_IMMEDIATE_ALERT),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);
                // alert level char.
                BluetoothGattCharacteristic alc = new BluetoothGattCharacteristic(
                        UUID.fromString(BleUuid.CHAR_ALERT_LEVEL),
                        BluetoothGattCharacteristic.PROPERTY_READ |
                                BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                                BluetoothGattCharacteristic.PERMISSION_WRITE);
                ias.addCharacteristic(alc);
                mGattServer.addService(ias);
            }

            { // device information
                BluetoothGattService dis = new BluetoothGattService(
                        UUID.fromString(BleUuid.SERVICE_DEVICE_INFORMATION),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);
                // manufacturer name string char.
                BluetoothGattCharacteristic mansc = new BluetoothGattCharacteristic(
                        UUID.fromString(BleUuid.CHAR_MANUFACTURER_NAME_STRING),
                        BluetoothGattCharacteristic.PROPERTY_READ,
                        BluetoothGattCharacteristic.PERMISSION_READ);
                // model number string char.
                BluetoothGattCharacteristic monsc = new BluetoothGattCharacteristic(
                        UUID.fromString(BleUuid.CHAR_MODEL_NUMBER_STRING),
                        BluetoothGattCharacteristic.PROPERTY_READ,
                        BluetoothGattCharacteristic.PERMISSION_READ);
                // serial number string char.
                BluetoothGattCharacteristic snsc = new BluetoothGattCharacteristic(
                        UUID.fromString(BleUuid.CHAR_SERIAL_NUMBEAR_STRING),
                        BluetoothGattCharacteristic.PROPERTY_READ,
                        BluetoothGattCharacteristic.PERMISSION_READ);
                dis.addCharacteristic(mansc);
                dis.addCharacteristic(monsc);
                dis.addCharacteristic(snsc);
                mGattServer.addService(dis);
            }

            if (mBTAdvertiser == null) {
                mBTAdvertiser = mBTAdapter.getBluetoothLeAdvertiser();
            }
            //mBTAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), mAdvCallback);
            setProgressBarIndeterminateVisibility(true);
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
        }
        mIsAdvertising = false;
        setProgressBarIndeterminateVisibility(false);
    }

    //    private static AdvertisementData createAdvData() {
    //        // 某Beacon
    //        final byte[] manufacturerData = new byte[] {
    //                (byte) 0x4c, (byte) 0x00, (byte) 0x02, (byte) 0x15, // fix
    //                // proximity uuid 01020304-0506-0708-1112-131415161718
    //                (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, // uuid
    //                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, // uuid
    //                (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, // uuid
    //                (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, // uuid
    //                (byte) 0x01, (byte) 0x01, // major 257
    //                (byte) 0x02, (byte) 0x02, // minor 514
    //                (byte) 0xc5
    //                // Tx Power -59
    //        };
    //        AdvertisementData.Builder builder = new AdvertisementData.Builder();
    // TxPowerLevelの設定に応じてTxPowerをAdvに混ぜてくれる設定だと思うのですが
    // いまいち分かっていません。trueにすると最大31オクテットなサイズが減っちゃうので
    // 某BeaconなパケをAdvするためにはfalseにする必要があります。
    //        builder.setIncludeTxPowerLevel(false);
    // 1つ目の引数がmanufacturerIdって書いてあるんですがAndroidのscanRecordでは読み取れないため適当値です。
    // builder.setManufacturerData(0x1234578, manufacturerData);

    // Device InformationとImmediate AlertをAdvで喋らせます
    //        List<ParcelUuid> uuidList = new ArrayList<ParcelUuid>();
    //        uuidList.add(ParcelUuid.fromString(BleUuid.SERVICE_DEVICE_INFORMATION));
    //        uuidList.add(ParcelUuid.fromString(BleUuid.SERVICE_IMMEDIATE_ALERT));
    //        builder.setServiceUuids(uuidList);

    //        return builder.build();
    //    }

    private static AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        // Read/WriteさせるのでConnectableを指定します。
        //        builder.setType(AdvertiseSettings.ADVERTISE_TYPE_CONNECTABLE);
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        return builder.build();
    }
}
