package com.peapod.etsysearch;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.squareup.picasso.Picasso;

/**
 * Adapter to hold individual listings for display in the StaggeredGridView
 */
public class ListingAdapter extends ArrayAdapter<Listing> implements View.OnTouchListener {

    private static final String TAG = "ListingAdapter";
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    static class ViewHolder {
        ImageView imageLine;
        TextView textLine;
        int position;
    }

    public ListingAdapter(final Context context, final int textResourceID) {
        super(context, textResourceID);
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    //Touch listener to dark text on touch down
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ViewHolder holder = (ViewHolder) v.getTag();
        TextView tv = holder.textLine;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tv.setTextColor(mContext.getResources().getColor(R.color.light_gray));
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            tv.setTextColor(mContext.getResources().getColor(R.color.white));
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            tv.setTextColor(mContext.getResources().getColor(R.color.white));

            //Interpret a touch up as a click and launch a dialog
            Listing clickedListing = getItem(holder.position);
            showDialog(clickedListing);
        }

        return true;
    }

    private void showDialog(Listing clickedListing) {
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(mContext);
        final Dialog dialog = dialogBuilder
                .withTitle(clickedListing.toString())
                .withTitleColor("#FFFFFFFF")
                .withDuration(300)
                .withDialogColor("#ff903632")
                .withEffect(Effectstype.Fadein)
                .setCustomView(R.layout.listing_dialog, mContext);

        dialog.show();

        //Add image and stylize
        Picasso.with(mContext).load(clickedListing.MainImage.url_570xN).placeholder(R.drawable.placeholder_image).into((ImageView) dialog.findViewById(R.id.modal_image));
        TextView dialogTitle = (TextView) dialog.findViewById(com.gitonway.lee.niftymodaldialogeffects.lib.R.id.alertTitle);
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/OpenSans-Light.ttf");
        dialogTitle.setTypeface(typeface);
        dialogTitle.setTextSize(17);
    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.listing_item, parent,false);
            holder = new ViewHolder();
            holder.textLine = (TextView) convertView.findViewById(R.id.text_line);
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/OpenSans-Regular.ttf");
            holder.textLine.setTypeface(typeface);
            holder.imageLine = (ImageView) convertView.findViewById(R.id.image_line);
            holder.position = position;
            convertView.setTag(holder);
            convertView.setOnTouchListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Populate item
        holder.position = position;
        Listing currentListing = getItem(position);
        holder.textLine.setText(currentListing.toString());
        if (currentListing.MainImage != null) {
            Picasso.with(mContext).load(currentListing.MainImage.url_170x135).fit().centerCrop().into(holder.imageLine);
        } else {
            holder.imageLine.setImageDrawable(null);
        }
        return convertView;
    }

}
