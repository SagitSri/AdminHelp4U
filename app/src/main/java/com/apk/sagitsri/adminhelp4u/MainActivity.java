package com.apk.sagitsri.adminhelp4u;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Constants;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.ProgressListener;
import com.graphhopper.util.StopWatch;

import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.LocationTextureLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.atlas.TextureAtlas;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.utils.IOUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public TextView le,lge,dte,tme;
    public double lte,lnte;
    public String dt;
    public Date pdt;
    public String tm;
    public String mb;

    MapScaleBar mapScaleBar;



    private MapView mapView;
    private GraphHopper hopper;
    private GeoPoint start;
    private GeoPoint end;

    private volatile boolean prepareInProgress = false;
    private volatile boolean shortestPathRunning = false;
    private String currentArea = "pondy";

    private String downloadURL;
    private File mapsFolder;
    private ItemizedLayer<MarkerItem> itemizedLayer;
    private PathLayer pathLayer;

    private LocationTextureLayer locationLayer;
    private LocationManager locationManager;
    private final MapPosition mapPosition = new MapPosition();


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = new MapView(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SimpleDateFormat input = new SimpleDateFormat("dd-MM-yy");
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy");

//        le = findViewById(R.id.lat);
//        lge = findViewById(R.id.lon);
//        dte = findViewById(R.id.dt);
//        tme = findViewById(R.id.tm);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lte = extras.getDouble("lat");
            lnte = extras.getDouble("ln");
            dt = extras.getString("dt");
            tm = extras.getString("tm");
            mb = extras.getString("mb");
            GeoPoint p = new GeoPoint(lte,lnte);
            updatewhois(p,mb);
//

//            le.setText(Double.toString(lte));
//            lge.setText(Double.toString(lnte));
//            try {
//                pdt = input.parse(dt);
//                dte.setText(output.format(pdt));
//            }catch (ParseException e){
//                e.printStackTrace();
//            }
//            tme.setText(tm);

            // load a Bitmap/SVG from resources
            InputStream is = null;
            Bitmap bmp = null;
            try {
                is = getResources().openRawResource(R.raw.arrow);
                float scale = CanvasAdapter.getScale();
                bmp = CanvasAdapter.decodeSvgBitmap(is, (int) (60 * scale), (int) (60 * scale), 100);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(is);
            }

            TextureRegion textureRegion = new TextureRegion(new TextureItem(bmp), new TextureAtlas.Rect(0, 0, bmp.getWidth(), bmp.getHeight()));
            locationLayer = new LocationTextureLayer(mapView.map(), textureRegion);
            locationLayer.locationRenderer.setAccuracyColor(Color.get(50, 50, 255));
            locationLayer.locationRenderer.setIndicatorColor(Color.MAGENTA);
            locationLayer.locationRenderer.setBillboard(false);

            locationLayer.setEnabled(false);
            mapView.map().layers().add(locationLayer);

        }

        boolean greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19;
//        if (greaterOrEqKitkat) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logUser("GraphHopper is not usable without an external storage!");
            return;
        }
        mapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/graphhopper/maps/");
//        } else
        mapsFolder = new File(Environment.getExternalStorageDirectory(), "/graphhopper/maps/");

        if (!mapsFolder.exists())
            mapsFolder.mkdirs();

        downloadingFiles();


    }


    private void updatewhois(GeoPoint p, String mb) {
        final GeoPoint s = p;
        final String S_URL = "http://pyky.000webhostapp.com/Help4U/whois.php?";
//        final ProgressDialog progressDialog;
//        progressDialog = new ProgressDialog(MainActivity.this);
//        progressDialog.setMessage("Connecting to Server...");
//        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);


        StringRequest postRequest = new StringRequest(Request.Method.POST, S_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        progressDialog.hide();

                        Toast.makeText(MainActivity.this,response.toString(),Toast.LENGTH_LONG).show();
//                        if (Integer.parseInt(response.toString()) == 1) {
//
//                            Toast.makeText(MainActivity.this, "Shared Successfully!", Toast.LENGTH_SHORT).show();
//
//
//                        } else {
//                            Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
//                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("ErrorResponse", error.getMessage());
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();


                params.put("lat", Double.toString(s.getLatitude()));
                params.put("lon", Double.toString(s.getLongitude()));
                params.put("mob", mb);

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                enableAvailableProviders();
        }
    }

    boolean isReady() {
        // only return true if already loaded
        if (hopper != null)
            return true;

        if (prepareInProgress) {
            logUser("Preparation still in progress");
            return false;
        }
        logUser("Prepare finished but hopper not ready. This happens when there was an error while loading the files");
        return false;
    }

    private void log(String str) {
        Log.i("GH", str);
    }

    private void log(String str, Throwable t) {
        Log.i("GH", str, t);
    }

    private void logUser(String str) {
        log(str);
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("deprecation")
    private MarkerItem createMarkerItem(GeoPoint p, int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphics.drawableToBitmap(drawable);
        MarkerSymbol markerSymbol = new MarkerSymbol(bitmap, 0.5f, 1);
        MarkerItem markerItem = new MarkerItem("", "", p);
        markerItem.setMarker(markerSymbol);
        return markerItem;
    }


    @SuppressLint("StaticFieldLeak")
    void downloadingFiles() {
        final File areaFolder = new File(mapsFolder, currentArea + "-gh");
        if (downloadURL == null || areaFolder.exists()) {
            loadMap(areaFolder);
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Downloading and uncompressing " + downloadURL);
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        new GHAsyncTask<Void, Integer, Object>() {
            protected Object saveDoInBackground(Void... _ignore)
                    throws Exception {
                String localFolder = Helper.pruneFileEnd(AndroidHelper.getFileName(downloadURL));
                localFolder = new File(mapsFolder, localFolder + "-gh").getAbsolutePath();
                log("downloading & unzipping " + downloadURL + " to " + localFolder);
                AndroidDownloader downloader = new AndroidDownloader();
                downloader.setTimeout(30000);
                downloader.downloadAndUnzip(downloadURL, localFolder,
                        new ProgressListener() {
                            @Override
                            public void update(long val) {
                                publishProgress((int) val);
                            }
                        });
                return null;
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                dialog.setProgress(values[0]);
            }

            protected void onPostExecute(Object _ignore) {
                dialog.dismiss();
                if (hasError()) {
                    String str = "An error happened while retrieving maps:" + getErrorMessage();
                    log(str, getError());
                    logUser(str);
                } else {
                    loadMap(areaFolder);
                }
            }
        }.execute();
    }

    void loadMap(File areaFolder) {
        logUser("loading map");

        mapScaleBar = new DefaultMapScaleBar(mapView.map());
        MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mapView.map(), mapScaleBar);
        mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
        mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);
        mapView.map().layers().add(mapScaleBarLayer);

        // Map file source
        MapFileTileSource tileSource = new MapFileTileSource();
        tileSource.setMapFile(new File(areaFolder, currentArea + ".map").getAbsolutePath());
        VectorTileLayer l = mapView.map().setBaseMap(tileSource);
        mapView.map().setTheme(VtmThemes.DEFAULT);
        mapView.map().layers().add(new BuildingLayer(mapView.map(), l));
        mapView.map().layers().add(new LabelLayer(mapView.map(), l));

        // Markers layer
        itemizedLayer = new ItemizedLayer<>(mapView.map(), (MarkerSymbol) null);
        mapView.map().layers().add(itemizedLayer);

        // Map position
        GeoPoint mapCenter = tileSource.getMapInfo().boundingBox.getCenterPoint();
        mapView.map().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), 1 << 15);


        setContentView(mapView);
        loadGraphStorage();
    }

    @SuppressLint("StaticFieldLeak")
    void loadGraphStorage() {
        logUser("loading graph (" + Constants.VERSION + ") ... ");
        new GHAsyncTask<Void, Void, Path>() {
            protected Path saveDoInBackground(Void... v) throws Exception {
                GraphHopper tmpHopp = new GraphHopper().forMobile();
                tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath() + "-gh");
                log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
                hopper = tmpHopp;
                hopper.setAllowWrites(false);
                return null;
            }

            protected void onPostExecute(Path o) {
                if (hasError()) {
                    logUser("An error happened while creating graph:"
                            + getErrorMessage());
                } else {
                    logUser("Finished loading graph. Long press to define where to start and end the route.");
                }

                finishPrepare();

            }
        }.execute();
    }

    private void finishPrepare() {
        prepareInProgress = false;


        GeoPoint s = new GeoPoint(12.0218206,79.7842856);
        drawRoute(s);

        GeoPoint e = new GeoPoint(lte,lnte);
        if(drawRoute(e)){
            mapView.map().setMapPosition(e.getLatitude(), e.getLongitude(), 1 << 15);
        }

    }

    private boolean drawRoute(GeoPoint p){
        if (!isReady())
            return false;

        if (shortestPathRunning) {
            logUser("Calculation still in progress");
            return false;
        }

        if (start != null && end == null) {
            end = p;
            shortestPathRunning = true;
            itemizedLayer.addItem(createMarkerItem(p, R.drawable.marker_icon_red));
            mapView.map().updateMap(true);

            calcPath(start.getLatitude(), start.getLongitude(), end.getLatitude(),
                    end.getLongitude());
        } else {
            start = p;
            end = null;
            // remove routing layers
            mapView.map().layers().remove(pathLayer);
            itemizedLayer.removeAllItems();

            itemizedLayer.addItem(createMarkerItem(start, R.drawable.marker_icon_green));
            mapView.map().updateMap(true);
        }
        return true;
    }

    private PathLayer createPathLayer(PathWrapper response) {
        Style style = Style.builder()
                .fixed(true)
                .generalization(Style.GENERALIZATION_SMALL)
                .strokeColor(0x9900cc33)
                .strokeWidth(4 * getResources().getDisplayMetrics().density)
                .build();
        PathLayer pathLayer = new PathLayer(mapView.map(), style);
        List<GeoPoint> geoPoints = new ArrayList<>();
        PointList pointList = response.getPoints();
        for (int i = 0; i < pointList.getSize(); i++)
            geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
        pathLayer.setPoints(geoPoints);
        return pathLayer;
    }

    @SuppressLint("StaticFieldLeak")
    public void calcPath(final double fromLat, final double fromLon,
                         final double toLat, final double toLon) {

        log("calculating path ...");
        new AsyncTask<Void, Void, PathWrapper>() {
            float time;

            protected PathWrapper doInBackground(Void... v) {
                StopWatch sw = new StopWatch().start();
                GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                        setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
                req.getHints().
                        put(Parameters.Routing.INSTRUCTIONS, "false");
                GHResponse resp = hopper.route(req);
                time = sw.stop().getSeconds();
                return resp.getBest();
            }

            protected void onPostExecute(PathWrapper resp) {
                if (!resp.hasErrors()) {
                    log("from:" + fromLat + "," + fromLon + " to:" + toLat + ","
                            + toLon + " found path with distance:" + resp.getDistance()
                            / 1000f + ", nodes:" + resp.getPoints().getSize() + ", time:"
                            + time + " " + resp.getDebugInfo());
                    logUser("the route is " + (int) (resp.getDistance() / 100) / 10f
                            + "km long, time:" + resp.getTime() / 60000f + "min, debug:" + time);
                    Log.d("Node catch: ", String.valueOf(resp.getPoints().getSize()));
                    Log.d("Node name: ", resp.getPoints().toString());
                    //Toast.makeText(getApplicationContext(),"Between Nodes: "+resp.getPoints().toString(),Toast.LENGTH_LONG).show();




                    pathLayer = createPathLayer(resp);
                    mapView.map().layers().add(pathLayer);
                    mapView.map().updateMap(true);
                } else {
                    logUser("Error:" + resp.getErrors());
                }
                shortestPathRunning = false;
            }
        }.execute();
    }

    @Override
    public void onStart() {
        super.onStart();

        enableAvailableProviders();
    }

    @Override
    public void onStop() {
        locationManager.removeUpdates(this);

        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hopper != null)
            hopper.close();

        hopper = null;
        // necessary?
        System.gc();

        // Cleanup VTM
        mapView.map().destroy();
    }

    private void enableAvailableProviders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                return;
            }
        }

        locationManager.removeUpdates(this);

        for (String provider : locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                locationManager.requestLocationUpdates(provider, 0, 0, this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationLayer.setEnabled(true);
        locationLayer.setPosition(location.getLatitude(), location.getLongitude(), location.getBearing(), location.getAccuracy());

        // Follow location
        mapView.map().getMapPosition(mapPosition);
        mapPosition.setPosition(location.getLatitude(), location.getLongitude());
        mapView.map().setMapPosition(mapPosition);
        mapView.map().animator().animateTo(mapPosition);
        NotifyAdmin(new GeoPoint(location.getLatitude(),location.getLongitude()));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void NotifyAdmin(GeoPoint p) {
        final GeoPoint s = p;
        final String S_URL = "http://pyky.000webhostapp.com/Help4U/upstream.php?";
        final ProgressDialog progressDialog;
//        progressDialog = new ProgressDialog(MainActivity.this);
//        progressDialog.setMessage("Connecting to Server...");
//        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);


        StringRequest postRequest = new StringRequest(Request.Method.POST, S_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        progressDialog.hide();

//                        Toast.makeText(MainActivity.this,response.toString(),Toast.LENGTH_LONG).show();
//                        if (Integer.parseInt(response.toString()) == 1) {
//
//                            Toast.makeText(MainActivity.this, "Shared Successfully!", Toast.LENGTH_SHORT).show();
//
//
//                        } else {
//                            Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
//                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("ErrorResponse", error.getMessage());
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();


                params.put("lt", Double.toString(s.getLatitude()));
                params.put("lon", Double.toString(s.getLongitude()));
                params.put("mob", mb);

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

    }

}
