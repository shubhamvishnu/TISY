package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;

import com.projects.shubhamkhandelwal.tisy.R;

/**
 * Created by Shubham Khandelwal on 11/28/2016.
 */
public class InitIcon {
    public int initDestinationIconResourceID(int destinationIconID){
        int iconResourceId = -1;
        switch (destinationIconID) {
            case R.id.destination_walking: {
                iconResourceId = 1;
                break;
            }
            case R.id.destination_swimming: {
                iconResourceId = 2;
                break;
            }
            case R.id.destination_spa: {
                iconResourceId = 3;

                break;
            }
            case R.id.destination_gym: {
                iconResourceId = 4;

                break;
            }
            case R.id.destination_drinks: {
                iconResourceId = 5;

                break;
            }
            case R.id.destination_casino: {
                iconResourceId = 6;

                break;
            }
            case R.id.destination_zoo: {
                iconResourceId = 7;

                break;
            }
            case R.id.destination_amusement_park: {
                iconResourceId = 8;
                break;
            }
            case R.id.destination_bowling_alley: {
                iconResourceId = 9;
                break;
            }
            case R.id.destination_aquarium: {
                iconResourceId = 10;
                break;
            }
            case R.id.destination_night_club: {
                iconResourceId = 11;
                break;
            }
            case R.id.destination_running: {
                iconResourceId = 12;
                break;

            }
            case R.id.destination_football: {
                iconResourceId = 13;
                break;
            }
            case R.id.destination_gaming: {
                iconResourceId = 14;
                break;
            }
            case R.id.destination_bicycler: {
                iconResourceId = 15;
                break;
            }
            case R.id.destination_cafe: {
                iconResourceId = 16;
                break;
            }
            case R.id.destination_restaurant: {
                iconResourceId = 17;
                break;
            }
            case R.id.destination_dinning: {
                iconResourceId = 18;
                break;
            }
            case R.id.destination_pizza: {
                iconResourceId = 19;
                break;
            }

            case R.id.destination_hotel: {
                iconResourceId = 20;
                break;
            }
            case R.id.destination_university: {
                iconResourceId = 21;
                break;
            }
            case R.id.destination_library: {
                iconResourceId = 23;
                break;
            }
            case R.id.destination_museum: {
                iconResourceId = 24;
                break;
            }
            case R.id.destination_beauty_salon: {
                iconResourceId = 25;
                break;
            }
            case R.id.destination_school: {
                iconResourceId = 26;
                break;
            }
            case R.id.destination_home: {
                iconResourceId = 27;
                break;
            }
            case R.id.destination_stadium: {
                iconResourceId = 28;
                break;
            }
            case R.id.destination_park: {
                iconResourceId = 29;
                break;
            }
            case R.id.destination_pharmacy: {
                iconResourceId = 30;
                break;

            }

            case R.id.destination_hospital: {
                iconResourceId = 31;
                break;

            }
            case R.id.destination_worship: {
                iconResourceId = 32;
                break;
            }
            case R.id.destination_mall: {
                iconResourceId = 33;
                break;
            }

            case R.id.destination_book_store: {
                iconResourceId = 34;
                break;
            }
            case R.id.destination_convenience_store: {
                iconResourceId = 35;
                break;
            }
            case R.id.destination_liquor_store: {
                iconResourceId = 36;
                break;
            }
            case R.id.destination_laundry: {
                iconResourceId = 37;
                break;
            }
            case R.id.destination_print_shop: {
                iconResourceId = 38;
                break;
            }
            case R.id.destination_grocery_store: {
                iconResourceId = 39;
                break;
            }

            case R.id.destination_parking: {
                iconResourceId = 40;
                break;
            }

            case R.id.destination_airport: {
                iconResourceId = 41;
                break;
            }

            case R.id.destination_train_station: {
                iconResourceId = 42;
                break;
            }

            case R.id.destination_bus_station: {
                iconResourceId = 43;
                break;
            }

            case R.id.destination_subway_station: {
                iconResourceId = 44;
                break;
            }

            case R.id.destination_tram: {
                iconResourceId = 45;
                break;
            }

        }
        return iconResourceId;
    }
    public Bitmap getDestinationIcon(Context context, int dIconResourceID) {
        Bitmap destinationIconBitmap = null;
        switch (Constants.dIconResourceId) {
            case 1: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_walking);

                break;
            }
            case 2: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_swimming);


                break;
            }
            case 3: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_spa);


                break;
            }
            case 4: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_gym);


                break;
            }
            case 5: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_drinks);


                break;
            }
            case 6: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_casino);


                break;
            }
            case 7: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination__zoo);


                break;
            }
            case 8: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_amusement_park);


                break;
            }
            case 9: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_bowling_alley);


                break;
            }
            case 10: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_aquarium);


                break;
            }
            case 11: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_night_club);


                break;
            }
            case 12: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_running);


                break;

            }
            case 13: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_football);


                break;
            }
            case 14: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_gaming);


                break;
            }
            case 15: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_bicycle);


                break;
            }
            case 16: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_cafe);


                break;
            }
            case 17: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_restaurant);


                break;
            }
            case 18: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_dinning);


                break;
            }
            case 19: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_pizza);


                break;
            }

            case 20: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_hotel);


                break;
            }
            case 21: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_university);


                break;
            }
            case 23: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_library);


                break;
            }
            case 24: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_museum);


                break;
            }
            case 25: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_beauty_salon);


                break;
            }
            case 26: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_school);


                break;
            }
            case 27: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_home);


                break;
            }
            case 28: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_stadium);


                break;
            }
            case 29: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_park);


                break;
            }
            case 30: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_pharmacy);


                break;

            }

            case 31: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_hospital);


                break;

            }
            case 32: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_worship);


                break;
            }
            case 33: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_mall);


                break;
            }

            case 34: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_book_store);


                break;
            }
            case 35: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_convenience_store);


                break;
            }
            case 36: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_liquor_store);


                break;
            }
            case 37: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_laundry);


                break;
            }
            case 38: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_print_shop);


                break;
            }
            case 39: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_grocery_store);


                break;
            }

            case 40: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_parking);


                break;
            }

            case 41: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_airport);


                break;
            }

            case 42: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_train_station);


                break;
            }

            case 43: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_bus_station);


                break;
            }

            case 44: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_subway_station);


                break;
            }

            case 45: {
                destinationIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.destination_icon_tram);


                break;
            }


        }
        return destinationIconBitmap;
    }
    /**
     * converts the vector drawables to bitmap
     *
     * @param context    : to reference to the location of the vector in the res/drawable directory
     * @param drawableId : the id of the vector drawable in the res/drawable directory
     * @return : return the bitmap object of the vector drawable
     */
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(200,
                200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    public static Bitmap getCustomBitmapFromVectorDrawable(Context context, int drawableId, int height, int width) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
