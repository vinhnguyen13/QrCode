package com.ttgvn.qrcode;

import android.content.ContentValues;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.d("TTGD", "----SDK_INT----:" + android.os.Build.VERSION.SDK_INT);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public void authentication(View v) {
        /*JWT parsedJWT = new JWT("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
        Claim subscriptionMetaData = parsedJWT.getClaim("sub");
        String parsedValue = subscriptionMetaData.asString();*/

        EditText txtPhone = (EditText) findViewById(R.id.txtPhone);
        EditText txtPass = (EditText) findViewById(R.id.txtPass);
        Log.d("TTGD", "Login >> phone:" + txtPhone.getText().toString() + " pass:" + txtPass.getText().toString());
        try {
            // CALL GetText method to make post method call
            String Response = postLogin(txtPhone, txtPass);
            Log.d("TTGD", "Response:" + Response);
            setContentView(R.layout.activity_main);
            TextView tv = (TextView)findViewById(R.id.textUser);
            tv.setText("Hello, "+txtPhone.getText().toString());
            Toast.makeText(this, "Login Success" + Response, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "URL Exception", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Post data login to service
     */
    public String postLogin(EditText txtPhone, EditText txtPass) throws UnsupportedEncodingException {
        HttpURLConnection connection = null;
        String Phone = txtPhone.getText().toString();
        String Pass = txtPass.getText().toString();
        String data = URLEncoder.encode("phone", "UTF-8") + "=" + URLEncoder.encode(Phone, "UTF-8");
        data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(Pass, "UTF-8");
        Log.d("TTGD", "Data:" + data);
        try {
            URL url = new URL("http://dev.metvuong.com/api/login");
            // Send POST data request
            connection = (HttpURLConnection) url.openConnection();
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
            Log.d("TTGD", "connection:" + connection);
            //send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            Log.d("TTGD", "DataOutputStream:" + wr);
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            // Get the server response
            InputStream is = connection.getInputStream();
            Log.d("TTGD", "InputStream:" + is);
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            Log.d("TTGD", "BufferedReader:" + rd);
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
                Log.d("TTGD", "-------intent:" + intent);
                //get the extras that are returned from the intent
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public String sendDataAfterScan(){
        return "";
    }
}
