package com.peapod.etsysearch;

import android.text.Html;

/**
 * Created by piyushpoddar on 4/5/15.
 */
public class Listing {

    String title;
    String description;
    Image MainImage;

    public String toString() {
        if (title == null) return "No name item";
        if (title.length() < 50) {
            return Html.fromHtml(title).toString();
        }
        return Html.fromHtml((title.substring(0,47))) + "...";
    }

}
