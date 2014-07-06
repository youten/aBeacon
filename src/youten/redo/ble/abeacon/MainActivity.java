
package youten.redo.ble.abeacon;

import youten.redo.ble.util.BleUtil;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisementData;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "aBeacon";
    private BluetoothAdapter mBTAdapter;
    private BluetoothLeAdvertiser mBTAdvertiser;
    private boolean mIsAdvertising = false;

    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {

        @Override
        public void onSuccess(AdvertiseSettings settingsInEffect) {
            // Advする際に設定した値と実際に動作させることに成功したSettingsが違うとsettingsInEffectに
            // 有効な値が格納される模様です。設定通りに動かすことに成功した際にはnullが返る模様。
            if (settingsInEffect != null) {
                Log.d(TAG, "onSuccess TxPowerLv="
                        + settingsInEffect.getTxPowerLevel()
                        + " mode=" + settingsInEffect.getMode()
                        + " type=" + settingsInEffect.getType());
            } else {
                Log.d(TAG, "onSuccess, settingInEffect is null");
            }
        }

        @Override
        public void onFailure(int errorCode) {
            Log.d(TAG, "onFailure errorCode=" + errorCode);
        }
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
        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

    }

    private void startAdvertise() {
        if ((mBTAdapter != null) && (!mIsAdvertising)) {
            if (mBTAdvertiser == null) {
                mBTAdvertiser = mBTAdapter.getBluetoothLeAdvertiser();
            }
            mBTAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), mAdvCallback);
            setProgressBarIndeterminateVisibility(true);
        }
    }

    private void stopAdvertise() {
        if (mBTAdvertiser != null) {
            mBTAdvertiser.stopAdvertising(mAdvCallback);
        }
        mIsAdvertising = false;
        setProgressBarIndeterminateVisibility(false);
    }

    private static AdvertisementData createAdvData() {
        // 某Beacon
        final byte[] manufacturerData = new byte[] {
                (byte) 0x4c, (byte) 0x00, (byte) 0x02, (byte) 0x15, // fix
                // proximity uuid 01020304-0506-0708-1112-131415161718
                (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, // uuid
                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, // uuid
                (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, // uuid
                (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, // uuid
                (byte) 0x01, (byte) 0x01, // major 257
                (byte) 0x02, (byte) 0x02, // minor 514
                (byte) 0xc5 // Tx Power -59
        };
        AdvertisementData.Builder builder = new AdvertisementData.Builder();
        // TxPowerLevelの設定に応じてTxPowerをAdvに混ぜてくれる設定だと思うのですが
        // いまいち分かっていません。trueにすると最大31オクテットなサイズが減っちゃうので
        // 某BeaconなパケをAdvするためにはfalseにする必要があります。
        builder.setIncludeTxPowerLevel(false);
        // 1つ目の引数がmanufacturerIdって書いてあるんですがAndroidのscanRecordでは読み取れないため適当値です。
        builder.setManufacturerData(0x1234578, manufacturerData);
        return builder.build();
    }

    private static AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        builder.setType(AdvertiseSettings.ADVERTISE_TYPE_SCANNABLE | AdvertiseSettings.ADVERTISE_TYPE_CONNECTABLE);
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        return builder.build();
    }
}
