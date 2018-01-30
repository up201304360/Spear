package com.example.nachito.spear;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by ines on 4/28/17.
 */


class OsmMapsItemizedOverlay extends ItemizedIconOverlay<OverlayItem> {
    private ArrayList<OverlayItem> mItemList = new ArrayList<>();

    public OsmMapsItemizedOverlay(List<OverlayItem> pList, Drawable pDefaultMarker, OnItemGestureListener<OverlayItem> pOnItemGestureListener, Context pContext) {
        super(pList, pDefaultMarker, pOnItemGestureListener, pContext);
    }


    void addOverlay(OverlayItem aOverlayItem) {
        mItemList.add(aOverlayItem);
        populate();
    }

    public void removeOverlay(OverlayItem aOverlayItem) {
        mItemList.remove(aOverlayItem);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mItemList.get(i);
    }

    @Override
    public int size() {
        if (mItemList != null)
            return mItemList.size();
        else
            return 0;
    }

    @Override
    public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
        return false;
    }


}