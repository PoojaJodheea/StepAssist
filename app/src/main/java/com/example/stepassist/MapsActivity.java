package com.example.stepassist;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import android.widget.LinearLayout;
import android.widget.Toast;


public class MapsActivity extends AppCompatActivity {
    private JSONObject reviewBeingEdited = null;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                loadMapStyle();
                setupMapBounds();
                loadLocations();
            });
        }
    }

    private void loadMapStyle() {
        try (InputStream is = getResources().openRawResource(R.raw.map_style)) {
            byte[] buffer = new byte[is.available()];
            int readBytes = is.read(buffer);
            if (readBytes != -1) {
                mMap.setMapStyle(new com.google.android.gms.maps.model.MapStyleOptions(
                        new String(buffer, StandardCharsets.UTF_8)));
            }
        } catch (Exception ex) {
            Log.e("MapsActivity", "Error loading map style: " + ex.getMessage());
        }
    }

    private void setupMapBounds() {
        LatLngBounds mauritiusBounds = new LatLngBounds(
                new LatLng(-20.5500, 57.2500),
                new LatLng(-19.9000, 57.8500)
        );

        LatLng mauritiusCenter = new LatLng(-20.3484, 57.5522);
        mMap.setLatLngBoundsForCameraTarget(mauritiusBounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mauritiusCenter, 11.5f));
        mMap.setMinZoomPreference(10.5f);
        mMap.setMaxZoomPreference(16.5f);
    }

    private void loadLocations() {
        try {
            InputStream inputStream = getAssets().open("locations.json");
            byte[] buffer = new byte[inputStream.available()];
            int bytesRead = inputStream.read(buffer);
            inputStream.close();

            if (bytesRead != buffer.length) {
                throw new IOException("Could not read the entire file.");
            }


            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("name");
                String description = obj.getString("description");
                double lat = obj.getDouble("latitude");
                double lng = obj.getDouble("longitude");
                String icon = obj.getString("icon");

                LatLng position = new LatLng(lat, lng);
                Bitmap resizedIcon = resizeMapIcon(icon);

                if (resizedIcon != null) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(name)
                            .snippet(description)
                            .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon)));

                    if (marker != null) {
                        obj.put("lat", lat);
                        obj.put("lng", lng);
                        marker.setTag(obj);
                    }
                }
            }

            mMap.setOnMarkerClickListener(marker -> {
                JSONObject location = (JSONObject) marker.getTag();
                if (location != null) {
                    showBottomSheet(location);
                }
                return true;
            });

        } catch (Exception e) {
            Log.e("MapsActivity", "Error loading locations: " + e.getMessage());
        }
    }

    private Bitmap resizeMapIcon(String iconName) {
        int resId = getIconResource(iconName);
        if (resId != 0) {
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
            return Bitmap.createScaledBitmap(imageBitmap, 80, 80, false);
        } else {
            Log.w("MapsActivity", "Icon resource not found for: " + iconName);
            return null;
        }
    }

    private int getIconResource(String iconName) {
        switch (iconName) {
            case "hospital":
                return R.drawable.hospital;
            case "parking":
                return R.drawable.parking;
            case "toilet":
                return R.drawable.toilet;
            case "bus":
                return R.drawable.bus;
            case "ramp":
                return R.drawable.ramp_icon;
            case "wheelchair":
                return R.drawable.wheelchair;
            case "restaurant":
                return R.drawable.restaurant;
            case "beach":
                return R.drawable.beach;
            case "supermarket":
                return R.drawable.supermarket;
            case "hotel":
                return R.drawable.hotel;
            case "park":
                return R.drawable.park;
            case "elevator":
                return R.drawable.elevator;
            case "damaged_road":
                return R.drawable.road;
            case "wc":
                return R.drawable.wc;

            default:
                return R.drawable.pin;
        }
    }
    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    private void showBottomSheet(JSONObject location) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet, findViewById(android.R.id.content), false);
        bottomSheet.setContentView(sheetView);

        try {
            TextView title = sheetView.findViewById(R.id.location_name);
            title.setText(location.getString("name"));

            ImageView locationImage = sheetView.findViewById(R.id.location_image);
            Glide.with(this).load(location.getString("image")).into(locationImage);

            TextView description = sheetView.findViewById(R.id.location_description);
            description.setText(location.getString("description"));

            LinearLayout facilitiesContainer = sheetView.findViewById(R.id.facilities_container);
            facilitiesContainer.removeAllViews();

            if (location.has("facilities")) {
                JSONArray facilitiesArray = location.getJSONArray("facilities");
                for (int i = 0; i < facilitiesArray.length(); i++) {
                    String facility = facilitiesArray.getString(i);
                    ImageView icon = new ImageView(this);
                    int resId = getIconResource(facility);

                    if (resId != 0) {
                        icon.setImageResource(resId);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
                        params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
                        icon.setLayoutParams(params);
                        facilitiesContainer.addView(icon);
                    }
                }
            }

            String locationKey = location.getDouble("lat") + "," + location.getDouble("lng");

            // Setup Reviews UI
            LinearLayout reviewSectionContainer = sheetView.findViewById(R.id.review_section_container);
            EditText reviewInput = sheetView.findViewById(R.id.user_review);
            RatingBar ratingBar = sheetView.findViewById(R.id.user_rating);
            Button submitReview = sheetView.findViewById(R.id.submit_review);

            // Load existing reviews with editing support
            ReviewManager.showReviews(this, reviewSectionContainer, locationKey, (reviewText, ratingValue, originalReview) -> {
                reviewInput.setText(reviewText);
                ratingBar.setRating(ratingValue);
                reviewBeingEdited = originalReview;
            });
            submitReview.setOnClickListener(v -> {
                float rating = ratingBar.getRating();
                String reviewText = reviewInput.getText().toString().trim();
                String username = "Lara Croft";

                if (!reviewText.isEmpty() && rating > 0) {
                    if (reviewBeingEdited != null) {
                        // Edit mode: update existing review and save all reviews
                        try {
                            reviewBeingEdited.put("rating", rating);
                            reviewBeingEdited.put("review", reviewText);

                            // Get all reviews for this location
                            List<JSONObject> reviews = ReviewManager.getSavedReviews(this, locationKey);

                            // No need to add because reviewBeingEdited is already in the list by reference
                            // Just save the updated list again
                            ReviewManager.saveReviews(this, locationKey, reviews);

                        } catch (JSONException e) {
                            Log.e("MapsActivity", "Error updating review", e);
                        }
                    } else {
                        // New review
                        ReviewManager.submitReview(this, locationKey, username, rating, reviewText);
                    }

                    // Clear input and reset edit mode
                    reviewInput.setText("");
                    ratingBar.setRating(0f);
                    reviewBeingEdited = null;

                    // Refresh review list
                    ReviewManager.showReviews(this, reviewSectionContainer, locationKey, (rt, rv, originalReview) -> {
                        reviewInput.setText(rt);
                        ratingBar.setRating(rv);
                        reviewBeingEdited = originalReview;
                    });

                    Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter review and rating", Toast.LENGTH_SHORT).show();
                }
            });


        } catch (Exception e) {
            Log.e("MapsActivity", "Error in bottom sheet: " + e.getMessage());
        }

        bottomSheet.show();
    }
}