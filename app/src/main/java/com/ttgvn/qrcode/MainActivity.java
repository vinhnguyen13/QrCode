package com.ttgvn.qrcode;

import android.content.ContentValues;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.RemoteException;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    static final String LOG_D = "TTGD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.d(LOG_D, "----SDK_INT----:" + android.os.Build.VERSION.SDK_INT);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /*TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String identifier = getDeviceID(telephonyManager);
        */
    }


    public void authentication(View v) {
        /*JWT parsedJWT = new JWT("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
        Claim subscriptionMetaData = parsedJWT.getClaim("sub");
        String parsedValue = subscriptionMetaData.asString();*/

        EditText txtPhone = (EditText) findViewById(R.id.txtPhone);
        EditText txtPass = (EditText) findViewById(R.id.txtPass);
        Log.d(LOG_D, "Login >> phone:" + txtPhone.getText().toString() + " pass:" + txtPass.getText().toString());
        try {
            // CALL GetText method to make post method call
            String Response = postLogin(txtPhone, txtPass);
            Log.d(LOG_D, "Response:" + Response);
            if (Response != null){
                JSONObject jObject = new JSONObject(Response);
                Log.d(LOG_D, "jObject:" + jObject);
                setContentView(R.layout.activity_main);

                TextView tv = (TextView) findViewById(R.id.textUser);
                tv.setText("Hello, " + txtPhone.getText().toString());

                double[] locations = getLocation();
//                String Lat = Double.toString(locations[0]);
//                String Long = Double.toString(locations[1]);
//                System.out.println("Double is " + locations);
//                TextView tv2 = (TextView) findViewById(R.id.textView2);
//                tv2.setText("Location: Long-" + Long +"Lat-"+Lat);

                Toast.makeText(this, "Login Success" + jObject.getString("access_token"), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Login Fail, please check data input again", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Post data login to service
     */
    public String postLogin(EditText txtPhone, EditText txtPass) {
        HttpURLConnection connection = null;
        String Phone = txtPhone.getText().toString();
        String Pass = txtPass.getText().toString();
        try {
            String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(Phone, "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(Pass, "UTF-8");
            data += "&" + URLEncoder.encode("grant_type", "UTF-8") + "=" + "password";
            data += "&" + URLEncoder.encode("client_id", "UTF-8") + "=" + "1";
            data += "&" + URLEncoder.encode("client_secret", "UTF-8") + "=" + "t6uDNcVtNYY0kj4Z5PuNWthRrwyMKmlF5SUIXzxG";
            data += "&" + URLEncoder.encode("scope", "UTF-8") + "=" + "*";
            Log.d(LOG_D, "Data:" + data);
//            URL url = new URL("http://dev.metvuong.com/api/login");
            URL url = new URL("http://dev.metvuong.com/oauth/token");
            // Send POST data request
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            Log.d(LOG_D, "Connection:" + connection);
            //send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();
            String  response = null;
            try {
                InputStream in = new BufferedInputStream(connection.getInputStream());
                response = convertStreamToString(in);
            } catch (MalformedURLException e) {
                Log.e(LOG_D, "MalformedURLException: " + e.getMessage());
                return null;
            } catch (ProtocolException e) {
                Log.e(LOG_D, "ProtocolException: " + e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e(LOG_D, "IOException: " + e.getMessage());
                return null;
            } catch (Exception e) {
                Log.e(LOG_D, "Exception: " + e.getMessage());
                return null;
            }
            return response;
        }catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    //product barcode mode
    public void scanBar(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //product qr code mode
    public void scanQR(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Log.d(LOG_D, "-------intent:" + intent);
                //get the extras that are returned from the intent
                String qrData = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                String textResult = "Content:" + qrData + " Format:" + format;
                String Response = sendDataAfterScan(qrData);
                Toast toast = Toast.makeText(this, textResult, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }



    public String sendDataAfterScan(String qrData){
        double[] locations = getLocation();
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjBkNjUwYmZmMzg4NTRjZDZiYWIwNGUzZTQ4MmE3MThiMTVmYjU5OWRiYTU3ZWZmNGJmZmZhOWVjMzM5MDU5YWU4ODRlOTI2MmE3YThjZjhiIn0.eyJhdWQiOiIxIiwianRpIjoiMGQ2NTBiZmYzODg1NGNkNmJhYjA0ZTNlNDgyYTcxOGIxNWZiNTk5ZGJhNTdlZmY0YmZmZmE5ZWMzMzkwNTlhZTg4NGU5MjYyYTdhOGNmOGIiLCJpYXQiOjE0OTE4MDAyMjYsIm5iZiI6MTQ5MTgwMDIyNiwiZXhwIjoxNTIzMzM2MjI2LCJzdWIiOiIxIiwic2NvcGVzIjpbIioiXX0.mlaEpHEZiPEavN74yQmBZ0wQyka3FnzqpCg9sxfLGVHpbI1Uf2yR-5ABkZq5bvgDUAB7lkTvVnmbRY0LJspnNBNRNsLSm_4q3TOkxKtTu3hLI5L0fJ8r2yDR3xr3Zv8W6iQ25jEYyVnVrWVRikndGsjjMP6hPmkMUjC3jpYRBSlIz8xEcJo0282-srDE_AgmWI58pIlLalTfjJoVGrSMRmjOfNSi5LRRPRKMrrnMGlExwpupIuuTroixAaC11JwkZwibXGzKjruG19RswbYzOCkHOovX19JU2K0ZamY6JEKRMRKnOP_8wTZmISUubxKFrixq4DDdu4x6NNuBu23HAN8xGfUaPo8OjFLBcCCHYkTZDfy7xMk0jGTnC79NYziIHLYMJ1ETh9bG29G3IkMGKpmDAM3YNina9jrQ1iSJOCamKWIDwaudAWDiz4aP4B4Nxi6kip_iO959Gc-d8tll00bvTMLw9KpgZxnOv8UZV7-MaX53_jGkYtiXMSRpl64TREkD-hquojDtUkdylZnuNHx_ahIFx3qcECrX4RY0SgtZXNd0vPZX9lt-yBrOpXBIWrw71AKMFqA8H7G8HqfQneHbyKD6gwqfr-jgcRmKD25S09v4JZ3M1d5XCiPXSxjo9Tp9_cehF-ZWaGT6XmxvuAOr_OMqHKIt6ImQRz3mick";
        String imei = getDeviceID();
        try {
            HttpURLConnection connection = null;
            String data = URLEncoder.encode("lat", "UTF-8") + "=" + locations[0];
            data += "&" + URLEncoder.encode("long", "UTF-8") + "=" + locations[1];
            data += "&" + URLEncoder.encode("imei", "UTF-8") + "=" + URLEncoder.encode(imei, "UTF-8");
            data += "&" + URLEncoder.encode("qrData", "UTF-8") + "=" + URLEncoder.encode(qrData, "UTF-8");
            Log.d(LOG_D, "Data:" + data);
            URL url = new URL("http://dev.metvuong.com/api/login");
            String encodedAuth="Bearer "+token;
            // Send POST data request
            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestProperty("Authorization", encodedAuth);
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(data.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            Log.d(LOG_D, "connection:" + connection);
            //send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            Log.d(LOG_D, "DataOutputStream:" + wr);
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            // Get the server response
            InputStream is = connection.getInputStream();
            Log.d(LOG_D, "InputStream:" + is);
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            Log.d(LOG_D, "BufferedReader:" + rd);
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return rd.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get Imei phone
     * @return
     */
    String getDeviceID(){
        TelephonyManager phonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String id = phonyManager.getDeviceId();
        if (id == null){
            id = "not available";
        }
        int phoneType = phonyManager.getPhoneType();
        String phoneTypeText;
        switch(phoneType){
            case TelephonyManager.PHONE_TYPE_NONE:
                phoneTypeText = "NONE: " + id;
            case TelephonyManager.PHONE_TYPE_GSM:
                phoneTypeText = "GSM: IMEI=" + id;
            case TelephonyManager.PHONE_TYPE_CDMA:
                phoneTypeText = "CDMA: MEID/ESN=" + id;
            default:
                phoneTypeText = "UNKNOWN: ID=" + id;
        }
        return id;
    }

    public double[] getLocation(){
        try {
            // Get LocationManager object
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Create a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Get the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Get Current Location
            Location myLocation = locationManager.getLastKnownLocation(provider);
            Log.d(LOG_D, "myLocation:" + myLocation);
            //latitude of location
            double myLatitude = myLocation.getLatitude();

            //longitude og location
            double myLongitude = myLocation.getLongitude();
            double[] locations = {myLatitude, myLongitude};
            return locations;
        }catch (SecurityException e){
            e.printStackTrace();
            return null;
        }
    }

}
