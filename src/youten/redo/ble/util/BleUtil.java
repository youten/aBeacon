/*
 * Copyright (C) 2013 youten
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
        byte[] manufacturerData = new byte[24];
        ByteBuffer bb = ByteBuffer.wrap(manufacturerData);
        bb.order(ByteOrder.BIG_ENDIAN);
        // fixed
        bb.put((byte) 0x4c);
        bb.put((byte) 0x00);
        bb.put((byte) 0x02);
        bb.put((byte) 0x15);
        bb.putLong(proximityUuid.getMostSignificantBits());
        bb.putLong(proximityUuid.getLeastSignificantBits());
        bb.putShort(major);
        bb.putShort(minor);
        //
        // !!!CAUTION!!!
        //
        // 31octetの計算方法にバグがあって、iBeaconに必要な25byte（TxPower）まで埋めると
        // AdvertiseCallback#ADVERTISE_FAILED_DATA_TOO_LARGEが返ってきます。
        // http://tools.oesf.biz/android-5.0.0_r2.0/xref/frameworks/base/core/java/android/bluetooth/le/BluetoothLeAdvertiser.java#totalBytes
        // bb.put(txPower);
        // というわけで現時点ではTxPowerなしというかなり片手落ちなiBeaconのAdvしか吹けません…orz

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        // 1つ目の引数がmanufacturerIdって書いてあるんですがAndroidのscanRecordでは読み取れないため適当値です。
        builder.addManufacturerData(0, manufacturerData);
        AdvertiseData adv = builder.build();
        return adv;
    }
}
