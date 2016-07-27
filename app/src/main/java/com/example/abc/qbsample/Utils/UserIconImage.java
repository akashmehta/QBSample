package com.example.abc.qbsample.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by Aakash on 20-02-2016.
 */
public class UserIconImage extends ImageView {
    public UserIconImage(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();
        /*if (drawable instanceof ShapeDrawable) {
            // cast to 'ShapeDrawable'
            ShapeDrawable shapeDrawable = (ShapeDrawable)drawable;
            shapeDrawable.getPaint().setColor(Color.parseColor("#ffffff"));
        } else if (drawable instanceof GradientDrawable) {
            // cast to 'GradientDrawable'
            GradientDrawable gradientDrawable = (GradientDrawable)drawable;
            gradientDrawable.setColor(Color.parseColor("#ffffff"));
        }*/

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

            Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

            int w = getWidth(), h = getHeight();

            Bitmap roundBitmap = getRoundedCroppedBitmap(bitmap, w);
            canvas.drawBitmap(roundBitmap, 0, 0, null);
    }

    public static Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int radius) {
        Bitmap finalBitmap;
        if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
            finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
                    false);
        else
            finalBitmap = bitmap;
        Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
                finalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
                finalBitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawCircle(finalBitmap.getWidth() / 2 ,
                finalBitmap.getHeight() / 2 ,
                finalBitmap.getWidth() / 2 - 1.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(finalBitmap, rect, rect, paint);

        return output;
    }


    // TODO Read more: http://www.androidhub4you.com/2014/10/android-custom-shape-imageview-rounded.html#ixzz40hPAjDQ7
}
