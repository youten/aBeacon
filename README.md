# aBeacon

Android LollipopでBLE Peripheralに対応したので
某iBeaconのAdv packetが再現できるか試してみたものです。

が、LRX21LのNexus9では31octetの計算式がおかしいせいか、
TxPowerが入れられないというなんとも片手落ちなiBeaconしか吹けません。
単純に計算式のミスかその先詰めるところにバグがあるか、
あるいは他のFlagがおかしくてiOSでiBeaconとして受信できないので
あまりがんばる意味もないのか、色々気になる点はあって
申し訳ないのですが差し当たりTxPower0x00版としてcommitしておきます。

また時間があれば。

# License

Apache License Version 2.0

