package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.MapHandler;
import com.junjunguo.pocketmaps.model.util.Destination;
import com.junjunguo.pocketmaps.model.util.MapHandlerListener;
import com.junjunguo.pocketmaps.model.util.NavigatorListener;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.model.MapViewPosition;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 24, 2015.
 */
public class MapActions implements NavigatorListener, MapHandlerListener {
    private Activity activity;
    private int ZOOM_LEVEL_MAX;
    private int ZOOM_LEVEL_MIN;
    protected FloatingActionButton showPositionBtn, navigationBtn, settingsBtn, controlBtn;
    protected FloatingActionButton zoomInBtn, zoomOutBtn;
    private ViewGroup sideBarVP, sideBarMenuVP, navSettingsVP, navSettingsFromVP, navSettingsToVP, navInstructionVP,
            nvaInstructionListVP;
    private boolean menuVisible, onStartPoint;
    private String travelMode;
    private EditText fromLocalET, toLocalET;

    public MapActions(Activity activity, MapView mapView, int zoom_level_max, int zoom_level_min) {
        this.activity = activity;
        this.showPositionBtn = (FloatingActionButton) activity.findViewById(R.id.map_show_my_position_fab);
        this.navigationBtn = (FloatingActionButton) activity.findViewById(R.id.map_nav_fab);
        this.settingsBtn = (FloatingActionButton) activity.findViewById(R.id.map_settings_fab);
        this.controlBtn = (FloatingActionButton) activity.findViewById(R.id.map_sidebar_control_afb);
        this.zoomInBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_in_fab);
        this.zoomOutBtn = (FloatingActionButton) activity.findViewById(R.id.map_zoom_out_fab);
        this.ZOOM_LEVEL_MAX = zoom_level_max;
        this.ZOOM_LEVEL_MIN = zoom_level_min;
        // view groups managed by separate layout xml file
        this.sideBarVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_layout);
        this.sideBarMenuVP = (ViewGroup) activity.findViewById(R.id.map_sidebar_menu_layout);
        this.navSettingsVP = (ViewGroup) activity.findViewById(R.id.nav_settings_layout);
        this.navSettingsFromVP = (ViewGroup) activity.findViewById(R.id.nav_settings_from_layout);
        this.navSettingsToVP = (ViewGroup) activity.findViewById(R.id.nav_settings_to_layout);
        this.navInstructionVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_layout);
        this.nvaInstructionListVP = (ViewGroup) activity.findViewById(R.id.nav_instruction_list_layout);
        //form location and to location textView
        this.fromLocalET = (EditText) activity.findViewById(R.id.nav_settings_from_local_et);
        this.toLocalET = (EditText) activity.findViewById(R.id.nav_settings_to_local_et);
        this.menuVisible = false;
        this.onStartPoint = true;
        this.travelMode = "foot";
        MapHandler.getMapHandler().setMapHandlerListener(this);
        controlBtnHandler();

        zoomControlHandler(mapView);
        showMyLocation(mapView);
        navBtnHandler();
        navSettingsHandler();
    }

    /**
     * navigation settings implementation
     */
    private void navSettingsHandler() {
        final ImageButton navSettingsClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_clear_btn);
        navSettingsClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.INVISIBLE);
                sideBarVP.setVisibility(View.VISIBLE);
            }
        });
        travelModeSetting();
        settingsFromItemHandler();
        settingsToItemHandler();
    }

    /**
     * settings layout:
     * <p/>
     * to item handler: when to item is clicked
     */
    private void settingsToItemHandler() {
        final ViewGroup toItemVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_item);
        toItemVG.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        toItemVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        toItemVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        navSettingsToVP.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        //        to layout
        //clear button
        ImageButton toLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_to_clear_btn);
        toLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsToVP.setVisibility(View.INVISIBLE);
            }
        });
        //  to layout: items
        toUseCurrentLocationHandler();
        toChooseFromFavoriteHandler();
        toPointOnMapHandler();
    }

    /**
     * from layout : point item view group  (on to layout)
     * <p/>
     * preform actions when point on map item is clicked
     */
    private void toPointOnMapHandler() {
        final ViewGroup pointItem = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_point);
        pointItem.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        onStartPoint = false;
                        navSettingsToVP.setVisibility(View.INVISIBLE);
                        //touch on map
                        Toast.makeText(activity, "Touch on Map to choose your destination!", Toast.LENGTH_SHORT).show();
                        MapHandler.getMapHandler().setNeedLocation(true);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * choose from favorite list handler: preform actions when choose from favorite item is clicked (on to layout)
     */
    private void toChooseFromFavoriteHandler() {
        //create a list view
        //read from Json file inflater to RecyclerView
        final ViewGroup chooseFavorite = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_favorite);
        chooseFavorite.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));

                        return true;
                }
                return false;
            }
        });
    }

    /**
     * current location handler: preform actions when current location item is clicked (on to layout)
     */
    private void toUseCurrentLocationHandler() {
        final ViewGroup useCurrentLocal = (ViewGroup) activity.findViewById(R.id.map_nav_settings_to_current);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        if (MapActivity.getmCurrentLocation() != null) {
                            Destination.getDestination().setStartPoint(
                                    new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                                            MapActivity.getmCurrentLocation().getLongitude()));
                            toLocalET.setText(Destination.getDestination().getStartPointToString());
                            navSettingsToVP.setVisibility(View.INVISIBLE);
                            navSettingsVP.setVisibility(View.VISIBLE);
                            activeNavigator();
                        } else {
                            Toast.makeText(activity, "Current Location not available, Check your GPS signal!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * settings layout:
     * <p/>
     * from item handler: when from item is clicked
     */
    private void settingsFromItemHandler() {
        final ViewGroup fromFieldVG = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_item);
        fromFieldVG.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        fromFieldVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        fromFieldVG.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        navSettingsVP.setVisibility(View.INVISIBLE);
                        navSettingsFromVP.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
        //        from layout
        //clear button
        ImageButton fromLayoutClearBtn = (ImageButton) activity.findViewById(R.id.nav_settings_from_clear_btn);
        fromLayoutClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                navSettingsVP.setVisibility(View.VISIBLE);
                navSettingsFromVP.setVisibility(View.INVISIBLE);
            }
        });
        //  from layout: items
        useCurrentLocationHandler();
        chooseFromFavoriteHandler();
        pointOnMapHandler();
    }

    /**
     * from layout : point item view group
     * <p/>
     * preform actions when point on map item is clicked
     */
    private void pointOnMapHandler() {
        final ViewGroup pointItem = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_point);
        pointItem.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        pointItem.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        onStartPoint = true;
                        navSettingsFromVP.setVisibility(View.INVISIBLE);
                        //touch on map
                        Toast.makeText(activity, "Touch on Map to choose your start Location", Toast.LENGTH_SHORT)
                                .show();
                        MapHandler.getMapHandler().setNeedLocation(true);
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * choose from favorite list handler: preform actions when choose from favorite item is clicked
     */
    private void chooseFromFavoriteHandler() {
        //create a list view
        //read from Json file inflater to RecyclerView
        final ViewGroup chooseFavorite = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_favorite);
        chooseFavorite.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        chooseFavorite.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));

                        return true;
                }
                return false;
            }
        });
    }

    /**
     * current location handler: preform actions when current location item is clicked
     */
    private void useCurrentLocationHandler() {
        final ViewGroup useCurrentLocal = (ViewGroup) activity.findViewById(R.id.map_nav_settings_from_current);
        useCurrentLocal.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary_light));
                        return true;
                    case MotionEvent.ACTION_UP:
                        useCurrentLocal.setBackgroundColor(activity.getResources().getColor(R.color.my_primary));
                        if (MapActivity.getmCurrentLocation() != null) {
                            Destination.getDestination().setStartPoint(
                                    new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                                            MapActivity.getmCurrentLocation().getLongitude()));
                            fromLocalET.setText(Destination.getDestination().getStartPointToString());
                            navSettingsFromVP.setVisibility(View.INVISIBLE);
                            navSettingsVP.setVisibility(View.VISIBLE);
                            activeNavigator();
                        } else {
                            Toast.makeText(activity, "Current Location not available, Check your GPS signal!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * when use press on the screen to get a location form map
     *
     * @param latLong
     */
    @Override public void onPressLocation(LatLong latLong) {
        if (onStartPoint) {
            Destination.getDestination().setStartPoint(latLong);
            fromLocalET.setText(Destination.getDestination().getStartPointToString());
        } else {
            Destination.getDestination().setEndPoint(latLong);
            toLocalET.setText(Destination.getDestination().getEndPointToString());
        }
        navSettingsVP.setVisibility(View.VISIBLE);
        activeNavigator();
    }

    /**
     * active navigator + drawer polyline on map
     */
    private void activeNavigator() {
        LatLong startPoint = Destination.getDestination().getStartPoint();
        LatLong endPoint = Destination.getDestination().getEndPoint();
        if (startPoint != null && endPoint != null) {
            MapHandler mapHandler = MapHandler.getMapHandler();
            mapHandler.setVehicle(getTravelMode());
            mapHandler.calcPath(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude);
        }
        //        else do nothing
    }


    /**
     * set up travel mode
     */
    private void travelModeSetting() {
        final ImageButton footBtn, bikeBtn, carBtn;
        footBtn = (ImageButton) activity.findViewById(R.id.nav_settings_foot_btn);
        bikeBtn = (ImageButton) activity.findViewById(R.id.nav_settings_bike_btn);
        carBtn = (ImageButton) activity.findViewById(R.id.nav_settings_car_btn);
        //default foot
        footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
        //foot
        footBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!getTravelMode().equalsIgnoreCase("foot")) {
                    setTravelMode("foot");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_orange_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                    activeNavigator();
                }
            }
        });
        //bike
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!getTravelMode().equalsIgnoreCase("bike")) {
                    setTravelMode("bike");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_orange_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_white_24dp);
                    activeNavigator();
                }
            }
        });
        // car
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!getTravelMode().equalsIgnoreCase("car")) {
                    setTravelMode("car");
                    footBtn.setImageResource(R.drawable.ic_directions_walk_white_24dp);
                    bikeBtn.setImageResource(R.drawable.ic_directions_bike_white_24dp);
                    carBtn.setImageResource(R.drawable.ic_directions_car_orange_24dp);
                    activeNavigator();
                }
            }
        });
    }


    /**
     * handler clicks on nav button
     */
    private void navBtnHandler() {
        navigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                sideBarVP.setVisibility(View.INVISIBLE);
                navSettingsVP.setVisibility(View.VISIBLE);
            }
        });
    }


    /**
     * start button: control button handler
     */
    private void controlBtnHandler() {
        final ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1);
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(300);
        anim.setInterpolator(new OvershootInterpolator());

        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isMenuVisible()) {
                    setMenuVisible(false);
                    sideBarMenuVP.setVisibility(View.INVISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_keyboard_arrow_up_white_24dp);
                    controlBtn.startAnimation(anim);
                } else {
                    setMenuVisible(true);
                    sideBarMenuVP.setVisibility(View.VISIBLE);
                    controlBtn.setImageResource(R.drawable.ic_clear_white_24dp);
                    controlBtn.startAnimation(anim);
                }
            }
        });
    }

    /**
     * implement zoom btn
     */
    protected void zoomControlHandler(final MapView mapView) {
        zoomInBtn.setImageResource(R.drawable.ic_add_white_24dp);
        zoomOutBtn.setImageResource(R.drawable.ic_remove_white_24dp);

        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            MapViewPosition mvp = mapView.getModel().mapViewPosition;

            @Override public void onClick(View v) {
                if (mvp.getZoomLevel() < ZOOM_LEVEL_MAX) mvp.zoomIn();
            }
        });
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            MapViewPosition mvp = mapView.getModel().mapViewPosition;

            @Override public void onClick(View v) {
                if (mvp.getZoomLevel() > ZOOM_LEVEL_MIN) mvp.zoomOut();
            }
        });
    }

    /**
     * move map to my current location as the center of the screen
     */
    protected void showMyLocation(final MapView mapView) {
        showPositionBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (MapActivity.getmCurrentLocation() != null) {
                    showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
                    mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                            new LatLong(MapActivity.getmCurrentLocation().getLatitude(),
                                    MapActivity.getmCurrentLocation().getLongitude()),
                            mapView.getModel().mapViewPosition.getZoomLevel()));
                } else {
                    showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
                    Toast.makeText(activity, "No Location Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * @return foot, bike, car (default: foot)
     */
    public String getTravelMode() {
        return travelMode;
    }

    /**
     * default foot if not set
     *
     * @param travelMode
     */
    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    /**
     * @return side bar menu visibility status
     */
    public boolean isMenuVisible() {
        return menuVisible;
    }

    /**
     * side bar menu visibility
     *
     * @param menuVisible
     */
    public void setMenuVisible(boolean menuVisible) {
        this.menuVisible = menuVisible;
    }

    /**
     * the change on navigator: navigation is used or not
     *
     * @param on
     */
    @Override public void statusChanged(boolean on) {
        if (on) {
            navigationBtn.setImageResource(R.drawable.ic_directions_white_24dp);
        } else {
            navigationBtn.setImageResource(R.drawable.ic_navigation_white_24dp);
        }
    }

}
