# Facebook Deep Link Plugin for Godot

A Godot Android plugin for handling Facebook deferred deep links.

## Overview

This plugin allows your Godot Android app to fetch and handle Facebook deferred deep links. It integrates with Facebook's App Links API to retrieve deep link data when users install your app through a Facebook ad or link.

## How It Works: Technical Flow

### The Deferred Deep Linking Process

The plugin implements Facebook's **deferred deep linking** mechanism, which allows you to retrieve the original deep link URL even when a user installs your app from Google Play Store after clicking a Facebook ad.

#### Step-by-Step Flow:

```
1. User clicks Facebook Ad
   ↓
2. Facebook stores metadata + generates Install Referrer
   ↓
3. User redirected to Google Play Store
   ↓
4. User installs app from Play Store
   ↓
5. App launches → Plugin calls fetchDeferredDeepLink()
   ↓
6. Plugin queries Google Play Install Referrer API
   ↓
7. Plugin sends referrer to Facebook servers
   ↓
8. Facebook matches referrer → Returns original deep link
   ↓
9. Plugin receives deep link URL → Emits signal to GDScript
```

### What the Plugin Does Under the Hood

When you call `fetch_deferred_deep_link()`, the plugin:

1. **Calls Facebook SDK**: Invokes `AppLinkData.fetchDeferredAppLinkData()`
2. **Queries Google Play Install Referrer API**: The Facebook SDK automatically:
   - Uses Android's `InstallReferrerClient` API
   - Retrieves the install referrer from Google Play
   - The referrer contains a unique identifier that Facebook embedded
3. **Sends Referrer to Facebook**: The SDK sends an HTTP request to Facebook's servers:
   ```
   POST https://graph.facebook.com/v{version}/app_links
   Headers:
     - User-Agent: FacebookSDK
     - Content-Type: application/json
   Body:
     {
       "app_id": "your-facebook-app-id",
       "install_referrer": "{referrer-from-google-play}",
       "device_id": "{android-id}",
       "package_name": "com.yourcompany.yourapp"
     }
   ```
4. **Facebook Server Processing**: Facebook:
   - Looks up the referrer in their database
   - Matches it with the original deep link metadata stored when the ad was clicked
   - Returns the original deep link URL (if found)
5. **Returns Result**: The callback receives:
   - `AppLinkData` object with the deep link URL (if metadata exists)
   - `null` if no deferred deep link was found

### What Data is Sent to Google/Facebook?

#### To Google Play (via Install Referrer API):
- **No data is sent** - The plugin only **reads** the install referrer
- The referrer is a string that Google Play provides (e.g., `"utm_source=facebook&utm_campaign=summer_sale"`)
- This happens automatically via Android's `InstallReferrerClient` API

#### To Facebook Servers:
When the Facebook SDK queries for deferred deep links, it sends:

```json
{
  "app_id": "123456789",                    // Your Facebook App ID
  "install_referrer": "fb_ref=abc123...",  // Referrer from Google Play
  "device_id": "android_id_hash",          // Hashed Android ID
  "package_name": "com.yourcompany.app",   // Your app's package name
  "sdk_version": "16.0.0",                 // Facebook SDK version
  "platform": "android"
}
```

**Privacy Note**: 
- The device ID is hashed/anonymized
- No personal user data is sent
- Only technical identifiers needed for matching

### Important Technical Details

1. **Install Referrer**: 
   - Google Play provides this automatically when an app is installed
   - Contains tracking parameters (like `utm_source`, `utm_campaign`)
   - Facebook embeds a unique identifier in the referrer when redirecting to Play Store

2. **Timing**:
   - The deferred deep link is only available **after** the app is installed
   - It persists for a limited time (typically 24-48 hours)
   - Must be fetched on first launch or shortly after installation

3. **Network Requirement**:
   - Requires internet connection to query Facebook servers
   - The plugin will fail silently if offline (no error thrown)

4. **No Direct Google Communication**:
   - The plugin does **not** directly communicate with Google
   - It only reads the install referrer that Android/Google Play provides
   - All Facebook communication happens through Facebook's SDK

### Code Flow Example

```java
// When fetchDeferredDeepLink() is called:

1. AppLinkData.fetchDeferredAppLinkData(activity, callback)
   ↓
2. Facebook SDK internally:
   a. Gets Install Referrer from Google Play
   b. Constructs HTTP request to Facebook
   c. Sends: {app_id, referrer, device_id, package_name}
   ↓
3. Facebook server responds with:
   - Deep link URL (if match found)
   - null (if no match)
   ↓
4. Callback invoked with AppLinkData
   ↓
5. Plugin extracts target URI
   ↓
6. Emits signal to GDScript: "deep_link_received"
```

### Limitations

- **Requires Internet**: Must be online to fetch deferred deep links
- **Time-Sensitive**: Deferred deep links expire after a period
- **First Install Only**: Only works for installations via the Facebook ad flow
- **No Metadata = Silent**: If no deep link exists, the callback is not invoked (no error)

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
