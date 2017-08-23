package sl.com.app.btaccessory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import sl.com.app.btaccessory.common.Constants;
import sl.com.lib.sharedpreferencesutil.SharedPreferencesUtil;
import sl.com.lib.webapiutil.WebApiUtil;

public class SettingActivity extends AppCompatActivity {
    private EditText etUrlService, etUrlMain;
    private CheckBox chkIsDecode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ntmSave_Clicked(v);
            }
        });
        Button btnUpdateInternet = (Button) findViewById(R.id.btnUpdateInternet);
        btnUpdateInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpdateInternet_Clicked(v);
            }
        });
        Button btnSaveMain = (Button) findViewById(R.id.btnSaveMain);
        btnSaveMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSaveMain_Clicked(v);
            }
        });
        etUrlService = (EditText)findViewById(R.id.etUrlService);
        etUrlService.setRawInputType(InputType.TYPE_NULL);
        etUrlService.setTextIsSelectable(true);

        etUrlMain = (EditText)findViewById(R.id.etUrlMain);
        etUrlMain.setRawInputType(InputType.TYPE_NULL);
        etUrlMain.setTextIsSelectable(true);

        chkIsDecode =  (CheckBox) findViewById(R.id.chkIsDecode);
        boolean isDecode = SharedPreferencesUtil.GetBoolean(this, Constants.KEY_IS_DECODE, true);
        chkIsDecode.setChecked(isDecode);
        loadData();

    }
    private void btnSaveMain_Clicked(View v){
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Warning !!!");
        builder.setMessage("Warning !!! Do you do edit this main url ? You can get data back by set empty");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String urlMain = etUrlMain.getText().toString();
                SharedPreferencesUtil.SetString(activity,Constants.KEY_URL_MAIN, urlMain);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("main url is saved !")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                activity.onBackPressed();

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void ntmSave_Clicked(View v){
        if( etUrlService.getText().toString().trim().equals("")){

        }
        else{
            final Activity activity = this;
            String urlService = etUrlService.getText().toString();
            Boolean isDecode = chkIsDecode.isChecked();
            SharedPreferencesUtil.SetBoolen(this,Constants.KEY_IS_DECODE, isDecode);
            SharedPreferencesUtil.SetString(this,Constants.KEY_URL_SERVICE, urlService);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Setting is saved !")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            activity.onBackPressed();

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
    private void btnUpdateInternet_Clicked(View v){
        updateInternet();
    }
    private void loadData(){
        String urlMain = SharedPreferencesUtil.GetString(this, Constants.KEY_URL_MAIN);
        if(urlMain == null || urlMain.equals(""))
        {
            urlMain = "https://docs.google.com/spreadsheets/d/1Z45EBAQE84iD2je4D1TVFxChzIf2il5kmsXbd_rovCE/pub?gid=0&single=true&output=csv";
            SharedPreferencesUtil.SetString(this,Constants.KEY_URL_MAIN, urlMain);
        }
        etUrlMain.setText(urlMain);

        String urlService = SharedPreferencesUtil.GetString(this, Constants.KEY_URL_SERVICE);
        if(urlService == null || urlService.equals("")){
            updateInternet();
        }
        else{
            etUrlService.setText(urlService);
        }
    }
    private void updateInternet(){
        final Activity __currentActivity = (Activity)this;
        final ProgressDialog callServiceDialog = new ProgressDialog(__currentActivity );
        callServiceDialog.setTitle("IFC Update");
        callServiceDialog.setMessage("Updating setting ...!!!");
        callServiceDialog.setCanceledOnTouchOutside(false);
        callServiceDialog.show();
        String uri = SharedPreferencesUtil.GetString(this, Constants.KEY_URL_MAIN);
        WebApiUtil.GetAsync(uri, null, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String []data = responseString.split("\r\n");
                for (String str :
                        data) {
                    if(str.contains("orion-textfile-new")){
                        String []strs = str.split(",");
                        etUrlService.setText(strs[1]);
                        break;
                    }
                }
                callServiceDialog.dismiss();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callServiceDialog.dismiss();
            }


        });
    }
}
