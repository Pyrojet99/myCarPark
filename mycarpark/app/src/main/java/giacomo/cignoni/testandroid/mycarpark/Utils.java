package giacomo.cignoni.testandroid.mycarpark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    /*
    Returns string representing date in the format passed as an argument of millis from epoch
     */
    public static String getDateFromMillis(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /*
    Transforms int for year, month, day, hour and minute in millis from epoch
     */
    public static long dateToMillis(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        return calendar.getTimeInMillis();
    }

    /*
    Generates bitmap from resource id of drawable vector.
    From https://stackoverflow.com/questions/33696488/getting-bitmap-from-vector-drawable
     */
    public static Bitmap generateBitmapFromVector(Context context, int resourceId) {
        Drawable drawable = AppCompatResources.getDrawable(context, resourceId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
