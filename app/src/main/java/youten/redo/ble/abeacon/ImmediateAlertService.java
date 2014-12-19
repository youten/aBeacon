
package youten.redo.ble.abeacon;

import java.util.UUID;

import youten.redo.ble.util.BleUuid;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

public class ImmediateAlertService extends BluetoothGattServerCallback {
    private static final String TAG = "IAS";
    private byte[] mAlertLevel = new byte[] {
            (byte) 0x00
    };

    private BluetoothGattServer mGattServer;

    public void setupServices(BluetoothGattServer gattServer) {
        if (gattServer == null) {
            throw new IllegalArgumentException("gattServer is null");
        }
        mGattServer = gattServer;

        // setup services
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
                    UUID.fromString(BleUuid.CHAR_SERIAL_NUMBER_STRING),
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            dis.addCharacteristic(mansc);
            dis.addCharacteristic(monsc);
            dis.addCharacteristic(snsc);
            mGattServer.addService(dis);
        }
    }

    public void onServiceAdded(int status, BluetoothGattService service) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onServiceAdded status=GATT_SUCCESS service="
                    + service.getUuid().toString());
        } else {
            Log.d(TAG, "onServiceAdded status!=GATT_SUCCESS");
        }
    }

    public void onConnectionStateChange(android.bluetooth.BluetoothDevice device, int status,
            int newState) {
        // Log.d(TAG, "onConnectionStateChange status=" + status + "->" + newState);
    }

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
                UUID.fromString(BleUuid.CHAR_SERIAL_NUMBER_STRING))) {
            Log.d(TAG, "CHAR_SERIAL_NUMBER_STRING");
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
    }

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
    }

}
