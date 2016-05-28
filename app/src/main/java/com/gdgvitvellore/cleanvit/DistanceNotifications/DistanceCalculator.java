package com.gdgvitvellore.cleanvit.DistanceNotifications;

import android.util.Log;

import java.text.DecimalFormat;

public class DistanceCalculator {
    public double CalculationByDistance(double latt1,double latt2,double lonn1,double lonn2) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = latt1;
        double lat2 = latt2;
        double lon1 = lonn1;
        double lon2 = lonn2;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
}
