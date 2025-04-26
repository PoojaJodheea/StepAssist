package com.example.stepassist;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import java.util.HashMap;
import java.util.ArrayList;


public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private final HashMap<String, ArrayList<Review>> markerReviews = new HashMap<>();

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
            inputStream.read(buffer);
            inputStream.close();

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
                Bitmap resizedIcon = resizeMapIcon(icon, 80, 80);

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

    private Bitmap resizeMapIcon(String iconName, int width, int height) {
        int resId = getIconResource(iconName);
        if (resId != 0) {
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
            return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        } else {
            Log.w("MapsActivity", "Icon resource not found for: " + iconName);
            return null;
        }
    }

    private int getIconResource(String iconName) {
        switch (iconName) {
            case "hospital": return R.drawable.hospital;
            case "parking": return R.drawable.parking;
            case "toilet": return R.drawable.toilet;
            case "bus": return R.drawable.bus;
            case "ramp": return R.drawable.ramp_icon;
            case "wheelchair": return R.drawable.wheelchair;
            case "restaurant": return R.drawable.restaurant;
            case "beach": return R.drawable.beach;
            case "supermarket": return R.drawable.supermarket;
            case "hotel": return R.drawable.hotel;
            case "park": return R.drawable.park;
            case "elevator": return R.drawable.elevator;
            case "damaged_road": return R.drawable.road;
            case "wc": return R.drawable.wc;

            default: return R.drawable.default_icon;
        }
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
            facilitiesContainer.removeAllViews(); // Clear previous

            if (location.has("facilities")) {
                JSONArray facilitiesArray = location.getJSONArray("facilities");

                for (int i = 0; i < facilitiesArray.length(); i++) {
                    String facility = facilitiesArray.getString(i);

                    ImageView icon = new ImageView(this);
                    int resId = getIconResource(facility); // You already have this method

                    if (resId != 0) {
                        icon.setImageResource(resId);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
                        params.setMargins(8, 0, 8, 0);
                        icon.setLayoutParams(params);
                        facilitiesContainer.addView(icon);
                    }
                }
            }

            RatingBar ratingBar = sheetView.findViewById(R.id.user_rating);
            EditText reviewInput = sheetView.findViewById(R.id.user_review);
            Button submitButton = sheetView.findViewById(R.id.submit_review);
            LinearLayout reviewsContainer = sheetView.findViewById(R.id.reviews_container);

            String locationKey = location.getDouble("lat") + "," + location.getDouble("lng");

            reviewsContainer.removeAllViews();
            ArrayList<Review> reviews = markerReviews.getOrDefault(locationKey, new ArrayList<>());
            for (Review review : reviews) {
                addReviewToLayout(review, reviewsContainer);
            }

            submitButton.setOnClickListener(v -> {
                String reviewText = reviewInput.getText().toString().trim();
                float rating = ratingBar.getRating();

                if (!reviewText.isEmpty() && rating > 0) {
                    Review newReview = new Review(reviewText, rating);
                    markerReviews.computeIfAbsent(locationKey, k -> new ArrayList<>()).add(newReview);
                    addReviewToLayout(newReview, reviewsContainer);
                    reviewInput.setText("");
                    ratingBar.setRating(0);
                }
            });

        } catch (Exception e) {
            Log.e("MapsActivity", "Error in bottom sheet: " + e.getMessage());
        }

        bottomSheet.show();
    }

    private void addReviewToLayout(Review review, LinearLayout container) {
        TextView textView = new TextView(this);
        textView.setText("⭐ " + review.rating + " - " + review.text);
        textView.setPadding(0, 8, 0, 8);
        container.addView(textView);
    }

    public static class Review {
        String text;
        float rating;

        Review(String text, float rating) {
            this.text = text;
            this.rating = rating;
        }
    }
}
