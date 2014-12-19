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

package youten.redo.ble.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

/**
 * Util for Bluetooth Low Energy
 */
public class BleUtil {

    private BleUtil() {
        // Util
    }

    /** check if BLE Supported device */
    public static boolean isBLESupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /** get BluetoothManager */
    public static BluetoothManager getManager(Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    /** create AdvertiseSettings */
    public static AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        // ConnectableをtrueにするとFlags AD typeの3byteがManufacturer specific data等の前につくようになります。
        builder.setConnectable(connectable);
        builder.setTimeout(timeoutMillis);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        return builder.build();
    }

    /** create AdvertiseDate for iBeacon */
    public static AdvertiseData createIBeaconAdvertiseData(UUID proximityUuid, short major,
            short minor, byte txPower) {
        if (proximityUuid == null) {
            throw new IllegalArgumentException("proximitiUuid null");
        }
        // UUID to byte[]
        // ref. http://stackoverflow.com/questions/6881659/how-to-convert-two-longs-to-a-byte-array-how-to-convert-uuid-to-byte-array
        byte[] manufacturerData = new byte[23];
        ByteBuffer bb = ByteBuffer.wrap(manufacturerData);
        bb.order(ByteOrder.BIG_ENDIAN);
        // fixed 4bytes
        // ManufacturerIdが正しく入るようになったので先頭2byteの変わりに
        // addManufacturerData時に0x004cとbyte[23]の2引数を指定すると一応iBeaconとして認識される気配がします。
        // （何をもって"iBeacon"とすべきかは某MFiなNDAの話なので分かりませんが！）
        //bb.put((byte) 0x4c);
        //bb.put((byte) 0x00);
        bb.put((byte) 0x02);
        bb.put((byte) 0x15);
        bb.putLong(proximityUuid.getMostSignificantBits());
        bb.putLong(proximityUuid.getLeastSignificantBits());
        bb.putShort(major);
        bb.putShort(minor);
        bb.put(txPower);

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addManufacturerData(0x004c, manufacturerData);
        AdvertiseData adv = builder.build();
        return adv;
    }

    /** create AdvertiseDate for FMP(Find Me Profile, include IAS and DIS) */
    public static AdvertiseData createFMPAdvertiseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();

        builder.addServiceUuid(new ParcelUuid(UUID.fromString(BleUuid.SERVICE_DEVICE_INFORMATION)));
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(BleUuid.SERVICE_IMMEDIATE_ALERT)));
        AdvertiseData adv = builder.build();
        return adv;
    }

}
