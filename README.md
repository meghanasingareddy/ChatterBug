# ChatterBug - Bluetooth Chat Application

ChatterBug is an Android application that enables peer-to-peer communication between nearby devices using Bluetooth technology, without requiring an internet connection.

## Features

- 🔵 **Bluetooth Device Discovery**: Automatically scan and discover nearby Bluetooth-enabled devices
- 💬 **Real-time Messaging**: Send and receive messages instantly through Bluetooth connection
- 🔒 **Secure Communication**: Uses Bluetooth's built-in security protocols
- 📱 **Modern UI**: Clean, intuitive Material Design interface
- 🔄 **Auto-reconnection**: Attempts to reconnect if connection is lost
- 👥 **Paired Device Support**: Works with both paired and unpaired devices

## Prerequisites

Before running the app, ensure you have:

1. **Android Studio** (latest version recommended)
2. **Android SDK** with minimum API level 21 (Android 5.0)
3. **Physical Android device** with Bluetooth capability (emulator won't work for Bluetooth testing)
4. **USB cable** for device connection

## How to Run the Application

### Step 1: Install Android Studio
1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Install and set up Android Studio with the Android SDK

### Step 2: Open the Project
1. Launch Android Studio
2. Click "Open an existing project"
3. Navigate to `c:\meghna\Coding\proj\CascadeProjects\ChatterBug`
4. Click "OK" to open the project

### Step 3: Sync the Project
1. Android Studio will automatically start syncing the project
2. Wait for "Gradle sync finished" message
3. If there are any errors, click "Sync Project with Gradle Files" button

### Step 4: Connect Your Android Device
1. Enable "Developer Options" on your Android device:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
2. Enable "USB Debugging" in Developer Options
3. Connect your device via USB cable
4. Allow USB debugging when prompted on your device

### Step 5: Run the Application
1. In Android Studio, click the green "Run" button (▶️) or press Shift+F10
2. Select your connected device from the deployment target dialog
3. Click "OK" to install and run the app

## How to Use ChatterBug

### Initial Setup
1. **Grant Permissions**: The app will request Bluetooth and location permissions - grant all required permissions
2. **Enable Bluetooth**: If Bluetooth is disabled, the app will prompt you to enable it

### Starting a Chat
1. **Make Device Discoverable**: 
   - Tap "Make Discoverable" to allow other devices to find you
   - Your device will be discoverable for 5 minutes

2. **Find Devices**:
   - Tap "Start Chat" to scan for nearby devices
   - Pull down to refresh the device list
   - Paired devices will show with a connected icon

3. **Connect and Chat**:
   - Tap on a device from the list to connect
   - Once connected, start typing messages
   - Messages appear in real-time on both devices

### Testing with Two Devices
For the best experience, test with two Android devices:
1. Install the app on both devices
2. Make one device discoverable
3. Use the other device to scan and connect
4. Start chatting!

## Project Structure

```
ChatterBug/
├── app/
│   ├── src/main/
│   │   ├── java/com/chatterbug/app/
│   │   │   ├── MainActivity.java          # Main screen
│   │   │   ├── DeviceListActivity.java    # Device discovery
│   │   │   ├── ChatActivity.java          # Chat interface
│   │   │   ├── adapters/                  # RecyclerView adapters
│   │   │   ├── models/                    # Data models
│   │   │   └── services/                  # Bluetooth service
│   │   ├── res/                          # Resources (layouts, drawables, etc.)
│   │   └── AndroidManifest.xml           # App permissions and activities
│   └── build.gradle                      # App-level dependencies
├── build.gradle                          # Project-level build file
└── settings.gradle                       # Project settings
```

## Troubleshooting

### Common Issues:

1. **"Bluetooth not supported"**
   - Ensure you're testing on a physical device, not an emulator
   - Check that your device has Bluetooth capability

2. **"Permission denied" errors**
   - Grant all requested permissions in device settings
   - For Android 12+, ensure you grant the new Bluetooth permissions

3. **Devices not found**
   - Ensure both devices have Bluetooth enabled
   - Make sure the target device is discoverable
   - Try refreshing the device list

4. **Connection failed**
   - Check if devices are within Bluetooth range (typically 10 meters)
   - Try pairing devices through system Bluetooth settings first
   - Restart Bluetooth on both devices

5. **Build errors**
   - Ensure you have the latest Android SDK
   - Try "Clean Project" and "Rebuild Project" in Android Studio
   - Check that all dependencies are properly synced

### Permissions Required:
- `BLUETOOTH` - Basic Bluetooth functionality
- `BLUETOOTH_ADMIN` - Bluetooth discovery and connection
- `ACCESS_FINE_LOCATION` - Required for Bluetooth discovery on Android 6+
- `BLUETOOTH_SCAN` - Android 12+ permission for scanning
- `BLUETOOTH_ADVERTISE` - Android 12+ permission for advertising
- `BLUETOOTH_CONNECT` - Android 12+ permission for connecting

## Technical Details

- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 14 (API 34)
- **Architecture**: MVVM with Repository pattern
- **Bluetooth Protocol**: RFCOMM (Radio Frequency Communication)
- **UUID**: Custom UUID for service discovery
- **Threading**: Background threads for Bluetooth operations

## Future Enhancements

- File sharing capability
- Group chat support
- Message encryption
- Chat history persistence
- Custom themes
- Voice messages

## License

This project is created for educational purposes and demonstrates Bluetooth communication in Android applications.

---

**Note**: This app requires physical Android devices for testing as Bluetooth functionality is not available in Android emulators.
