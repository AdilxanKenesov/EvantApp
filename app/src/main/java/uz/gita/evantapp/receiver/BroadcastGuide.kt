package uz.gita.evantapp.receiver

/*
 * ============================================================================
 *  BROADCAST RECEIVER — QISQA QO'LLANMA
 * ============================================================================
 *
 *  Broadcast — Android ichidagi "e'lon" tizimi. Tizim (yoki app) Intent yuboradi,
 *  shu action'ga obuna bo'lgan barcha receiverlar uni oladi.
 *  Masalan: Wi-Fi yoqildi -> tizim WIFI_STATE_CHANGED_ACTION yuboradi.
 *
 *  ----------------------------------------------------------------------------
 *  1) RO'YXATDAN O'TKAZISH TURLARI
 *  ----------------------------------------------------------------------------
 *  a) MANIFEST (static) receiver:
 *       <receiver android:name=".BootReceiver" android:exported="true">
 *           <intent-filter>
 *               <action android:name="android.intent.action.BOOT_COMPLETED" />
 *           </intent-filter>
 *       </receiver>
 *     + App yopiq bo'lsa ham tizim uni ishga tushiradi.
 *     - Android 8.0 (API 26) dan boshlab IMPLICIT broadcastlarning ko'pchiligi
 *       (Wi-Fi, connectivity, power connected, headset ...) manifest receiverga KELMAYDI.
 *       Istisnolar: BOOT_COMPLETED, LOCALE_CHANGED, TIMEZONE_CHANGED,
 *       PROVIDERS_CHANGED_ACTION, ACTION_PACKAGE_* va boshqa bir nechtasi.
 *
 *  b) CONTEXT-REGISTERED (dynamic) receiver — BU APPDA ISHLATILGAN:
 *       ContextCompat.registerReceiver(context, receiver, filter, RECEIVER_NOT_EXPORTED)
 *       ...
 *       context.unregisterReceiver(receiver)
 *     + Barcha system broadcastlarni oladi.
 *     - Faqat context (Activity/Service) tirik va register qilingan paytda ishlaydi.
 *     ! register/unregister juft bo'lishi shart: onStart <-> onStop, onCreate <-> onDestroy.
 *       Aks holda "Activity has leaked IntentReceiver" xatosi / memory leak.
 *
 *  ----------------------------------------------------------------------------
 *  2) EXPORTED FLAGLARI (Android 13 / 14+)
 *  ----------------------------------------------------------------------------
 *     RECEIVER_EXPORTED     — boshqa app'lar ham broadcast yubora oladi.
 *     RECEIVER_NOT_EXPORTED — faqat tizim va o'z app'imiz. Xavfsizroq.
 *     Android 14 (API 34) da custom broadcast uchun flag ko'rsatmaslik -> SecurityException.
 *     ContextCompat.registerReceiver() eski versiyalarda ham to'g'ri ishlaydi.
 *
 *  ----------------------------------------------------------------------------
 *  3) BROADCAST TURLARI
 *  ----------------------------------------------------------------------------
 *     NORMAL     — sendBroadcast(): hamma receiverga parallel, tartibsiz yetkaziladi.
 *     ORDERED    — sendOrderedBroadcast(): android:priority bo'yicha birma-bir;
 *                  receiver natijani o'zgartirishi (setResultData) yoki
 *                  abortBroadcast() bilan to'xtatishi mumkin.
 *     STICKY     — tizim oxirgi intentni saqlab qo'yadi (ACTION_BATTERY_CHANGED,
 *                  ACTION_HEADSET_PLUG, USB_STATE). registerReceiver() uni darhol qaytaradi.
 *                  isInitialStickyBroadcast() — "bu eski saqlangan intentmi?"
 *                  (sendStickyBroadcast() app'lar uchun deprecated.)
 *     PROTECTED  — faqat tizim yubora oladigan broadcastlar (AIRPLANE_MODE va h.k.).
 *     LOCAL      — LocalBroadcastManager — DEPRECATED. O'rniga Flow/LiveData ishlating.
 *     CUSTOM     — o'z app'imiz yuboradi:
 *                  sendBroadcast(Intent("uz.gita.MY_ACTION").setPackage(packageName))
 *
 *  ----------------------------------------------------------------------------
 *  4) YOZILISH USULLARI (shu loyihada)
 *  ----------------------------------------------------------------------------
 *     1-usul: alohida class         -> WifiReceiver, BluetoothReceiver, AirplaneModeReceiver,
 *                                      InternetReceiver, LocationReceiver
 *     2-usul: anonim object          -> MainActivity: chargingReceiver, batterySaverReceiver
 *     3-usul: callbackFlow          -> BroadcastFlow.kt: Headset, DND, Silent
 *     4-usul: Compose effect        -> ComposeBroadcast.kt: NFC, Data Saver, Battery Low, USB
 *     Bitta receiver + ko'p action  -> chargingReceiver (POWER_CONNECTED + DISCONNECTED)
 *
 *  ----------------------------------------------------------------------------
 *  5) MUHIM QOIDALAR
 *  ----------------------------------------------------------------------------
 *     - onReceive() MAIN thread'da ishlaydi va ~10 soniyada tugashi kerak (aks holda ANR).
 *     - Uzoq ish kerak bo'lsa: goAsync() + coroutine, yoki WorkManager.
 *     - Manifest receiver ichida ishni Activity'ga bog'lamang — u har safar yangi obyekt.
 * ============================================================================
 */
