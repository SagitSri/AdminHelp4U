package com.apk.sagitsri.adminhelp4u;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class AdminHome extends AppCompatActivity {

    public final static int QRcodeWidth = 250 ;
    private static final int PERMISSION_REQUEST_CODE = 200;
    Bitmap bitmap;
    TextView et;

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private List<DataModel> albumList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        requestPermission();

        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        albumList = new ArrayList<>();
        adapter = new CustomAdapter(this, albumList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

//        prepareAlbums();
        getRequestAdmin();
//
    }

    public void testit(double lat,double lan){
        Toast.makeText(this, "Scanned: " + lat+"&"+lan, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.scanqr) {
            Toast.makeText(this,"Scan QR Code",Toast.LENGTH_LONG).show();
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Scan");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 350, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");
                Toast.makeText(this,"Scan Cancelled!",Toast.LENGTH_LONG).show();

            } else {
                Log.e("Scan", "Scanned");


                //tv_qr_readTxt.setText(result.getContents());
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
//                String[] words = result.getContents().split(",");
//                System.out.println(Arrays.toString(words));
//                et.setText(Arrays.toString(words));
//                Toast.makeText(this,Arrays.toString(words),Toast.LENGTH_LONG).show();



            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            this.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void prepareAlbums() {


        DataModel a = new DataModel(12.0245522,79.8328477,"29-10-2019","23:44","9597654336");
        albumList.add(a);

        a = new DataModel(12.0245522,79.8328477,"29-10-2019","08:44","6374023685");
        albumList.add(a);

        a = new DataModel(12.0245522,79.8328477,"29-10-2019","22:44","2122789451");
        albumList.add(a);

        a = new DataModel(12.0245522,79.8328477,"29-10-2019","08:44","6374023685");
        albumList.add(a);

        a = new DataModel(12.0245522,79.8328477,"29-10-2019","22:44","8056425677");
        albumList.add(a);



        adapter.notifyDataSetChanged();
    }

    private void getRequestAdmin() {
        final String S_URL = "http://pyky.000webhostapp.com/Help4U/notifyadmin.php?";

        RequestQueue queue = Volley.newRequestQueue(AdminHome.this);



        StringRequest postRequest = new StringRequest(Request.Method.GET, S_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

//                        Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                        System.out.println(response.toString());

                        try {

                            JSONObject obj = new JSONObject(response.toString());
                            JSONArray dataArray = obj.getJSONArray("res");

                            for (int i = 0; i < dataArray.length(); i++) {


                                JSONObject dataobj = dataArray.getJSONObject(i);

                                final double dblt = dataobj.getDouble("lat");
                                final double dbln = dataobj.getDouble("lon");
                                String dbmob = dataobj.getString("mob");
                                String dbdt = dataobj.getString("dt");
                                String dbtm = dataobj.getString("tm");

                                DataModel a = new DataModel(dblt,dbln,dbdt,dbtm,dbmob);
                                albumList.add(a);

                                adapter.notifyDataSetChanged();

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("ErrorResponse", error.getMessage());
                        Toast.makeText(AdminHome.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
        ) {

        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CAMERA, WRITE_EXTERNAL_STORAGE, CALL_PHONE}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean wrES = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean pCal = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted && wrES && pCal)
                        Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
                    else {
                        Toast.makeText(this,"Permission Denied, App needs these permissions!",Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, CAMERA, WRITE_EXTERNAL_STORAGE, CALL_PHONE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AdminHome.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
