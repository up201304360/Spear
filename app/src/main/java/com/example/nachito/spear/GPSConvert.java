package com.example.nachito.spear;

import android.location.Location;


/**
 * Created by pedro on 2/21/18.
 * LSTS - FEUP
 *
 *
 * Created by Pedro Gonçalves - http://gitub.com/pmfg/acm
 */

public class GPSConvert {


        public String latLonToDMS(double latitude, double longitude) {
            StringBuilder builder = new StringBuilder();

            if (latitude < 0) {
                builder.append("Lat: S ");
            } else {
                builder.append("Lat: N ");
            }

            String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
            String[] latitudeSplit = latitudeDegrees.split(":");
            builder.append(latitudeSplit[0]);
            builder.append("°");
            builder.append(latitudeSplit[1]);
            builder.append("'");
            builder.append(latitudeSplit[2]);
            builder.append("\"");

            builder.append("\nLon: ");

            if (longitude < 0) {
                builder.append("W ");
            } else {
                builder.append("E ");
            }

            String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
            String[] longitudeSplit = longitudeDegrees.split(":");
            builder.append(longitudeSplit[0]);
            builder.append("°");
            builder.append(longitudeSplit[1]);
            builder.append("'");
            builder.append(longitudeSplit[2]);
            builder.append("\"");

            return builder.toString();
        }

        public String latLonToDM(double latitude, double longitude) {

            StringBuilder builder = new StringBuilder();
            try {
                if (latitude < 0) {
                    builder.append("Lat: S ");
                } else {
                    builder.append("Lat: N ");
                }

                String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_MINUTES);
                String[] latitudeSplit = latitudeDegrees.split(":");
                builder.append(latitudeSplit[0]);
                builder.append("°");
                builder.append(latitudeSplit[1]);
                builder.append("\"");

                builder.append("\nLon: ");

                if (longitude < 0) {
                    builder.append("W ");
                } else {
                    builder.append("E ");
                }

                String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_MINUTES);
                String[] longitudeSplit = longitudeDegrees.split(":");
                builder.append(longitudeSplit[0]);
                builder.append("°");
                builder.append(longitudeSplit[1]);
                builder.append("\"");
            }catch (Exception io){
                builder.append("Lat: --- \nLon: --- ");
            }

            return builder.toString();
        }
    }


