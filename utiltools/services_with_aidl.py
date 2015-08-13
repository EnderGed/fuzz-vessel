import re

services = [('ACCESSIBILITY_SERVICE', 'AccessibilityManager', 'accessibility', 'frameworks/base/core/java/android/view/accessibility/IAccessibilityManager.aidl'),
 ('ACCOUNT_SERVICE', 'AccountManager', 'account', 'frameworks/base/core/java/android/accounts/IAccountManager.aidl'),
 ('ACTIVITY_SERVICE', 'ActivityManager', 'activity', None),
 ('ALARM_SERVICE', 'AlarmManager', 'alarm', 'frameworks/base/core/java/android/app/IAlarmManager.aidl'),
 ('APPWIDGET_SERVICE', 'AppWidgetManager', 'appwidget', 'frameworks/base/core/java/com/android/internal/appwidget/IAppWidgetService.aidl'),
 ('APP_OPS_SERVICE', 'AppOpsManager', 'appops', None),
 ('AUDIO_SERVICE', 'AudioManager', 'audio', 'frameworks/base/media/java/android/media/IAudioService.aidl'),
 ('BATTERY_SERVICE', 'BatteryManager', 'batterymanager', None),
 ('BLUETOOTH_SERVICE', 'BluetoothAdapter', 'bluetooth', 'frameworks/base/core/java/android/bluetooth/IBluetoothManager.aidl'),
 ('CAMERA_SERVICE', 'CameraManager', 'camera', None),
 ('CAPTIONING_SERVICE', 'CaptioningManager', 'captioning', None),
 ('CLIPBOARD_SERVICE', 'ClipboardManager', 'clipboard', 'frameworks/base/core/java/android/content/IClipboard.aidl'),
 ('CONNECTIVITY_SERVICE', 'ConnectivityManager', 'connectivity', 'frameworks/base/core/java/android/net/IConnectivityManager.aidl'),
 ('CONSUMER_IR_SERVICE', 'ConsumerIrManager', 'consumer_ir', None),
 ('DEVICE_POLICY_SERVICE', 'DevicePolicyManager', 'device_policy', 'frameworks/base/core/java/android/app/admin/IDevicePolicyManager.aidl'),
 ('DISPLAY_SERVICE', 'DisplayManager', 'display', 'frameworks/base/core/java/android/hardware/display/IDisplayManager.aidl'),
 ('DOWNLOAD_SERVICE', 'DownloadManager', 'download', None),
 ('DROPBOX_SERVICE', 'DropBoxManager', 'dropbox', 'frameworks/base/core/java/com/android/internal/os/IDropBoxManagerService.aidl'),
 ('INPUT_METHOD_SERVICE', 'InputMethodManager', 'input_method', 'frameworks/base/core/java/com/android/internal/view/IInputMethodManager.aidl'),
 ('INPUT_SERVICE', 'InputManager', 'input', 'frameworks/base/core/java/android/hardware/input/IInputManager.aidl'),
 ('JOB_SCHEDULER_SERVICE', 'JobScheduler', 'jobscheduler', None),
 ('KEYGUARD_SERVICE', 'NotificationManager', 'keyguard', 'frameworks/base/core/java/android/app/INotificationManager.aidl'),
 ('LAUNCHER_APPS_SERVICE', 'LauncherApps', 'launcherapps', None),
 ('LAYOUT_INFLATER_SERVICE', 'LayoutInflater', 'layout_inflater', None),
 ('LOCATION_SERVICE', 'LocationManager', 'location', 'frameworks/base/location/java/android/location/ILocationManager.aidl'),
 ('MEDIA_PROJECTION_SERVICE', 'MediaProjectionManager', 'media_projection', None),
 ('MEDIA_ROUTER_SERVICE', 'MediaRouter', 'media_router', None),
 ('MEDIA_SESSION_SERVICE', 'MediaSessionManager', 'media_session', None),
 ('NFC_SERVICE', 'NfcManager', 'nfc', None),
 ('NOTIFICATION_SERVICE', 'NotificationManager', 'notification', 'frameworks/base/core/java/android/app/INotificationManager.aidl'),
 ('NSD_SERVICE', 'NsdManager', 'servicediscovery', 'frameworks/base/core/java/android/net/nsd/INsdManager.aidl'),
 ('POWER_SERVICE', 'PowerManager', 'power', 'frameworks/base/core/java/android/os/IPowerManager.aidl'),
 ('PRINT_SERVICE', 'PrintManager', 'print', None),
 ('RESTRICTIONS_SERVICE', 'RestrictionsManager', 'restrictions', None),
 ('SEARCH_SERVICE', 'SearchManager', 'search', 'frameworks/base/core/java/android/app/ISearchManager.aidl'),
 ('SENSOR_SERVICE', 'SensorManager', 'sensor', None),
 ('STORAGE_SERVICE', 'StorageManager', 'storage', None),
 ('TELECOM_SERVICE', 'TelecomManager', 'telecom', None),
 ('TELEPHONY_SERVICE', 'TelephonyManager', 'phone', 'frameworks/base/telephony/java/com/android/internal/telephony/ITelephony.aidl'),
 ('TELEPHONY_SUBSCRIPTION_SERVICE',
  'SubscriptionManager',
  'telephony_subscription_service', None),
 ('TEXT_SERVICES_MANAGER_SERVICE', 'TextServicesManager', 'textservices', 'frameworks/base/core/java/com/android/internal/textservice/ITextServicesManager.aidl'),
 ('TV_INPUT_SERVICE', 'TvInputManager', 'tv_input', None),
 ('UI_MODE_SERVICE', 'UiModeManager', 'uimode', 'frameworks/base/core/java/android/app/IUiModeManager.aidl'),
 ('USAGE_STATS_SERVICE', 'UsageStatsManager', 'usagestats', 'frameworks/base/core/java/com/android/internal/app/IUsageStats.aidl'),
 ('USB_SERVICE', 'UsbManager', 'usb', 'frameworks/base/core/java/android/hardware/usb/IUsbManager.aidl'),
 ('USER_SERVICE', 'UserManager', 'user', 'frameworks/base/core/java/android/os/IUserManager.aidl'),
 ('VIBRATOR_SERVICE', 'Vibrator', 'vibrator', 'frameworks/base/core/java/android/os/IVibratorService.aidl'),
 ('WALLPAPER_SERVICE', 'com.android.server.WallpaperService', 'wallpaper', 'frameworks/base/core/java/android/app/IWallpaperManager.aidl'),
 ('WIFI_P2P_SERVICE', 'WifiP2pManager', 'wifip2p', 'frameworks/base/wifi/java/android/net/wifi/p2p/IWifiP2pManager.aidl'),
 ('WIFI_SERVICE', 'WifiManager', 'wifi', 'frameworks/base/wifi/java/android/net/wifi/IWifiManager.aidl')]

types = set()
prim_or_trash = set()

arg_types = re.compile("(\\(|, )(in |out |inout ){0,1}([^ \\[]*)", re.M)
packages = re.compile("import (([^\\.;]*\\.)*([^;]*))", re.M) #- imports 1-package, 2-trash, 3-type
signatures = re.compile("\\{([^\0]*)\\}", re.M)

for _, _, _, path in services:
    if path:
        with open('/usr/local/home/smartseclab/android-4.3_r3.1/' + path, 'r') as fil:
            data = fil.read()
            pack = {name: full_name for full_name, _, name in packages.findall(data)}
            signs = signatures.search(data).group(0)
            args = [arg for _, _, arg in arg_types.findall(signs)]
            for arg in args:
                if arg in pack:
                    types.add(pack[arg])
                else:
                    prim_or_trash.add(arg)