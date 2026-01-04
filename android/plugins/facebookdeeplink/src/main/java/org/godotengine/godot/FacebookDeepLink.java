package org.godotengine.godot;

import android.app.Activity;
import com.facebook.applinks.AppLinkData;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;

public class FacebookDeepLink extends GodotPlugin {

    private Activity activity;

    public FacebookDeepLink(Godot godot) {
        super(godot);
        this.activity = godot.getActivity();
    }

    @Override
    public String getPluginName() {
        return "FacebookDeepLink";
    }

    @UsedByGodot
    public void fetchDeferredDeepLink() {
        AppLinkData.fetchDeferredAppLinkData(activity, appLinkData -> {
            if (appLinkData != null) {
                String target = appLinkData.getTargetUri().toString();
                // Call back to Godot on the UI thread
                activity.runOnUiThread(() -> {
                    // Emit signal to notify GDScript
                    emitSignal("deep_link_received", target);
                });
            }
        });
    }
}

