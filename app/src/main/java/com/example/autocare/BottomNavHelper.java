package com.example.autocare;

import android.app.Activity;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BottomNavHelper {

    public static void setupBottomNav(Activity activity, int activeNavItemId) {
        int[] navIds = {
            R.id.navHome, R.id.navCustomers, R.id.navMechanics, 
            R.id.navServices, R.id.navProfile, R.id.navVehicles, R.id.navWarranty,
            R.id.navGarages
        };

        int activeColor = Color.parseColor("#00A86B"); // Green
        int inactiveColor = Color.parseColor("#64748B"); // Gray

        for (int id : navIds) {
            LinearLayout navLayout = activity.findViewById(id);
            if (navLayout != null) {
                // First child is ImageView, second child is TextView
                ImageView icon = (ImageView) navLayout.getChildAt(0);
                TextView text = (TextView) navLayout.getChildAt(1);

                if (id == activeNavItemId) {
                    icon.setColorFilter(activeColor);
                    text.setTextColor(activeColor);
                } else {
                    icon.setColorFilter(inactiveColor);
                    text.setTextColor(inactiveColor);
                }
            }
        }
    }
}
