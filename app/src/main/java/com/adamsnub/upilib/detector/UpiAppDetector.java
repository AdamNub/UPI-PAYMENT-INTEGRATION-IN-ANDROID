package com.adamsnub.upilib.detector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.graphics.drawable.Drawable;

import com.adamsnub.upilib.models.UpiApp;

import java.util.ArrayList;
import java.util.List;

public class UpiAppDetector {
    private final Context context;
    
    public UpiAppDetector(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Get list of all installed UPI apps on the device
     */
    public List<UpiApp> getInstalledUpiApps() {
        List<UpiApp> upiApps = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        
        // Create intent to find apps that handle UPI payments
        Intent upiIntent = new Intent(Intent.ACTION_VIEW);
        upiIntent.setData(Uri.parse("upi://pay"));
        
        // Query for apps that can handle this intent
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(upiIntent, 0);
        
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            String appName = resolveInfo.loadLabel(packageManager).toString();
            Drawable icon = resolveInfo.loadIcon(packageManager);
            
            UpiApp upiApp = new UpiApp(appName, packageName, icon);
            upiApps.add(upiApp);
        }
        
        return upiApps;
    }
    
    /**
     * Check if any UPI app is installed
     */
    public boolean hasAnyUpiApp() {
        return !getInstalledUpiApps().isEmpty();
    }
    
    /**
     * Check if a specific UPI app is installed
     */
    public boolean isUpiAppInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}