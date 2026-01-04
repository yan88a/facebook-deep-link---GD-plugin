# Facebook Deep Link Plugin for Godot

A Godot Android plugin for handling Facebook deferred deep links.

## Overview

This plugin allows your Godot Android app to fetch and handle Facebook deferred deep links. It integrates with Facebook's App Links API to retrieve deep link data when users install your app through a Facebook ad or link.

## Installation

### Option 1: Using the Output Folder (Recommended)

1. Copy the entire `output` folder to your Godot project:
   ```
   your-godot-project/android/plugins/facebookdeeplink/
   ```

2. The `output` folder contains:
   - `facebookdeeplink.aar` - The compiled Android plugin
   - `FacebookDeepLink.gdip` - Godot plugin configuration file
   - `FacebookDeepLink.gd` - GDScript wrapper class

3. In Godot, go to **Project → Export → Android** and enable the "FacebookDeepLink" plugin

### Option 2: Building from Source

If you need to rebuild the plugin:

1. Ensure you have:
   - Java JDK 8 or higher
   - Android SDK
   - Gradle (or use the included wrapper)

2. Build the AAR:
   ```powershell
   cd android/plugins/facebookdeeplink
   .\gradlew.bat assembleRelease
   ```

3. The AAR will be in `build/outputs/aar/facebookdeeplink-release.aar`

## Usage

### Basic Setup

1. Add the `FacebookDeepLink` node to your scene or create it programmatically:

```gdscript
extends Node

var deep_link: FacebookDeepLink

func _ready():
    deep_link = FacebookDeepLink.new()
    add_child(deep_link)
    
    # Connect to the signal
    deep_link.deep_link_received.connect(_on_deep_link_received)
    
    # Fetch any pending deep links
    deep_link.fetch_deferred_deep_link()

func _on_deep_link_received(target: String):
    print("Received deep link: ", target)
    # Handle the deep link URL
    # Example: navigate to a specific scene based on the link
```

### API Reference

#### Methods

- `fetch_deferred_deep_link()` - Fetches any pending Facebook deferred deep link data
- `on_deep_link_received(target: String)` - Callback method invoked when a deep link is received (can be overridden)

#### Signals

- `deep_link_received(target: String)` - Emitted when a deep link is received

#### Properties

- `last_deep_link: String` - Stores the last received deep link URL

## Requirements

- **Godot Version**: 4.5.1 or higher
- **Android**: Minimum SDK 21 (Android 5.0)
- **Facebook SDK**: Included in the plugin (version 16.0.0)

## Configuration

### Facebook App Setup

1. Create a Facebook App at https://developers.facebook.com/
2. Configure App Links in your Facebook App settings
3. Add your app's package name and deep link URLs
4. Set up deferred deep linking in Facebook Ads Manager (if using ads)

### Android Manifest

The plugin automatically adds the required permissions. Ensure your app has internet permission in your main AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Testing Guide

### Prerequisites

Before testing, ensure you have:

1. **Facebook App Created**: A Facebook App with App Links configured
   - Go to https://developers.facebook.com/
   - Create or select your app
   - Navigate to **Settings → Basic** and note your App ID
   - Go to **Products → App Links** and configure your deep link URLs

2. **App Package Name**: Your Android app's package name (e.g., `com.yourcompany.yourapp`)

3. **Deep Link URL Scheme**: Your app's deep link scheme (e.g., `yourapp://` or `https://yourapp.com/`)

### Step 1: Create a Test Scene

Create a simple test scene in Godot to verify the plugin works:

```gdscript
# TestScene.gd
extends Node

var deep_link: FacebookDeepLink

func _ready():
    # Create and add the deep link handler
    deep_link = FacebookDeepLink.new()
    add_child(deep_link)
    
    # Connect to the signal
    deep_link.deep_link_received.connect(_on_deep_link_received)
    
    # Add a button to manually trigger fetch (for testing)
    var button = Button.new()
    button.text = "Fetch Deep Link"
    button.pressed.connect(_on_button_pressed)
    add_child(button)
    
    # Also fetch automatically on startup
    print("Testing Facebook Deep Link plugin...")
    deep_link.fetch_deferred_deep_link()

func _on_button_pressed():
    print("Manually fetching deep link...")
    deep_link.fetch_deferred_deep_link()

func _on_deep_link_received(target: String):
    print("✓ Deep link received successfully!")
    print("Target URL: ", target)
    
    # Display the result (you can customize this)
    var label = Label.new()
    label.text = "Deep Link: " + target
    label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
    add_child(label)
```

### Step 2: Configure Facebook App Links

1. **Set Up App Links in Facebook Dashboard**:
   - Go to your Facebook App Dashboard
   - Navigate to **Products → App Links**
   - Click **Create App Link**
   - Add your Android package name
   - Configure your deep link URLs

2. **Create a Test Deep Link URL**:
   ```
   https://your-app-domain.com/deeplink?param1=value1&param2=value2
   ```
   Or use a custom scheme:
   ```
   yourapp://deeplink?param1=value1&param2=value2
   ```

### Step 3: Testing Methods

#### Method 1: Using Facebook App Links Debugger (Recommended)

1. **Install the App Links Debugger**:
   - Download from: https://developers.facebook.com/tools/app-link-debugger/
   - Or use the web version: https://developers.facebook.com/tools/debug/applinks/

2. **Test Your Deep Link**:
   - Enter your deep link URL in the debugger
   - Click "Debug"
   - Verify that your app package name is recognized
   - Check for any errors or warnings

3. **Test on Device**:
   - Open the debugger on your Android device
   - Enter your deep link URL
   - Click "Open in App" (if app is installed) or "Install App" (if not installed)
   - For deferred deep links: Install the app, then open the deep link again

#### Method 2: Manual Testing with ADB

1. **Build and Install Your App**:
   ```bash
   # Export your Godot project as Android APK
   # Install on device via ADB
   adb install your-app.apk
   ```

2. **Test Direct Deep Link** (if app is installed):
   ```bash
   adb shell am start -W -a android.intent.action.VIEW -d "yourapp://deeplink?test=123" com.yourcompany.yourapp
   ```

3. **Test Deferred Deep Link**:
   - Uninstall the app first: `adb uninstall com.yourcompany.yourapp`
   - Create a Facebook post or ad with your deep link
   - Click the link to install the app
   - After installation, open the app
   - The plugin should fetch the deferred deep link automatically

#### Method 3: Testing with Facebook Ads (Production-like)

1. **Create a Test Ad Campaign**:
   - Go to Facebook Ads Manager
   - Create a new campaign
   - Use your deep link URL as the destination
   - Target a small test audience
   - Publish the ad

2. **Test the Flow**:
   - Click the ad on a device where your app is NOT installed
   - Install the app from the Play Store
   - Open the app
   - The deferred deep link should be fetched automatically

### Step 4: Verify Plugin Integration

Add debug logging to verify the plugin is working:

```gdscript
func _ready():
    deep_link = FacebookDeepLink.new()
    add_child(deep_link)
    
    if deep_link.plugin:
        print("✓ Plugin loaded successfully")
    else:
        print("✗ Plugin NOT loaded - check export settings")
        return
    
    deep_link.deep_link_received.connect(_on_deep_link_received)
    deep_link.fetch_deferred_deep_link()
    print("Fetching deferred deep link...")

func _on_deep_link_received(target: String):
    print("✓ Signal received!")
    print("Deep link: ", target)
    print("Last deep link stored: ", deep_link.last_deep_link)
```

### Step 5: Check Logcat Output

Monitor Android logcat to see plugin activity:

```bash
# Filter for your app's logs
adb logcat | grep -i "facebook\|deeplink\|godot"

# Or filter for specific tags
adb logcat -s Godot:V FacebookDeepLink:V
```

Look for:
- `FacebookDeepLink plugin loaded successfully` - Plugin initialized
- `Deep link received: [URL]` - Deep link was fetched
- Any error messages from Facebook SDK

### Common Test Scenarios

#### Scenario 1: App Already Installed
1. App is already installed on device
2. User clicks Facebook deep link
3. App opens immediately with deep link data
4. **Expected**: Deep link should be available immediately (not deferred)

#### Scenario 2: Deferred Deep Link (App Not Installed)
1. App is NOT installed
2. User clicks Facebook ad/link
3. User installs app from Play Store
4. User opens app
5. **Expected**: Plugin fetches deferred deep link on first launch

#### Scenario 3: No Deep Link Available
1. App opens normally (no deep link clicked)
2. Plugin calls `fetch_deferred_deep_link()`
3. **Expected**: No error, no deep link received (normal behavior)

### Debugging Tips

1. **Plugin Not Loading**:
   - Check Godot export settings: **Project → Export → Android → Plugins**
   - Verify `FacebookDeepLink.gdip` file exists in plugin folder
   - Check that AAR file is present

2. **No Deep Link Received**:
   - Verify Facebook App Links are configured correctly
   - Check package name matches between app and Facebook settings
   - Ensure internet permission is granted
   - Test with Facebook App Links Debugger first

3. **Deep Link Received but Not Handled**:
   - Check that signal connection is set up: `deep_link.deep_link_received.connect(...)`
   - Verify `_on_deep_link_received()` method exists
   - Add print statements to trace execution

4. **Facebook SDK Errors**:
   - Check logcat for Facebook SDK error messages
   - Verify Facebook App ID is correct (if required)
   - Ensure device has internet connection

### Testing Checklist

- [ ] Plugin loads without errors
- [ ] `fetch_deferred_deep_link()` can be called
- [ ] Signal `deep_link_received` is emitted when deep link is available
- [ ] Deep link URL is correctly parsed and accessible
- [ ] Works when app is already installed
- [ ] Works with deferred deep links (install → open)
- [ ] Handles case when no deep link is available
- [ ] Logs are clear and helpful for debugging

### Example: Complete Test Script

```gdscript
extends Node

class_name DeepLinkTest

var deep_link: FacebookDeepLink
var status_label: Label

func _ready():
    # Create UI for testing
    var vbox = VBoxContainer.new()
    add_child(vbox)
    
    status_label = Label.new()
    status_label.text = "Initializing..."
    vbox.add_child(status_label)
    
    var fetch_btn = Button.new()
    fetch_btn.text = "Fetch Deep Link"
    fetch_btn.pressed.connect(_on_fetch_pressed)
    vbox.add_child(fetch_btn)
    
    # Initialize plugin
    deep_link = FacebookDeepLink.new()
    add_child(deep_link)
    
    if deep_link.plugin:
        status_label.text = "Plugin loaded ✓"
        deep_link.deep_link_received.connect(_on_deep_link_received)
        # Auto-fetch on startup
        deep_link.fetch_deferred_deep_link()
    else:
        status_label.text = "Plugin NOT loaded ✗"

func _on_fetch_pressed():
    status_label.text = "Fetching..."
    deep_link.fetch_deferred_deep_link()

func _on_deep_link_received(target: String):
    status_label.text = "Deep Link: " + target
    print("Test successful! Received: ", target)
```

## Project Structure

```
facebookdeeplink/
├── output/                    # Distribution files (copy this to your Godot project)
│   ├── facebookdeeplink.aar  # Compiled plugin
│   ├── FacebookDeepLink.gdip # Plugin configuration
│   └── FacebookDeepLink.gd   # GDScript wrapper
├── src/                       # Source code
│   └── main/
│       ├── java/             # Java plugin implementation
│       └── AndroidManifest.xml
├── build.gradle              # Build configuration
├── plugin.cfg                # Plugin metadata
└── README.md                 # This file
```

## Building from Source

### Prerequisites

- Java JDK 8+
- Android SDK (API 33)
- Gradle 8.0+

### Build Steps

1. The plugin uses Godot 4.5.1 library from Maven Central
2. Run the build:
   ```powershell
   .\gradlew.bat assembleRelease
   ```
3. The AAR will be in `build/outputs/aar/facebookdeeplink-release.aar`

## Troubleshooting

### Plugin not found
- Ensure the plugin is enabled in Godot's Android export settings
- Check that the AAR file is in the correct location
- Verify the plugin name matches in `plugin.cfg` and `FacebookDeepLink.gdip`

### Deep links not working
- Verify your Facebook App Links configuration
- Check that your app's package name matches Facebook settings
- Ensure internet permission is granted
- Test with Facebook's App Links Debugger tool

### Build errors
- Ensure Android SDK is properly configured
- Check that `ANDROID_HOME` environment variable is set
- Verify Java JDK is installed and `JAVA_HOME` is set

## License

This plugin is provided as-is. Make sure to comply with Facebook's terms of service when using their SDK.

## Support

For issues related to:
- **Godot**: https://godotengine.org/community
- **Facebook SDK**: https://developers.facebook.com/docs/app-links
- **Plugin Issues**: Check the source code in `src/main/java/`

## Version

- **Plugin Version**: 1.0
- **Godot Version**: 4.5.1
- **Facebook SDK**: 16.0.0
