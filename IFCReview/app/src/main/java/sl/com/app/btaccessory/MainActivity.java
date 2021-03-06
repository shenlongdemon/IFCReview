package sl.com.app.btaccessory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sl.commonutil.zip.LZString;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

import sl.com.app.btaccessory.common.Constants;
import sl.com.lib.sharedpreferencesutil.SharedPreferencesUtil;
import sl.com.lib.webapiutil.WebApiUtil;
import sl.com.lib.wirelessdevicecommunication.ISLDevice;
import sl.com.lib.wirelessdevicecommunication.SLDeviceManager;
import sl.com.lib.wirelessdevicecommunication.device.SLBluetoothDevice;
import sl.com.lib.wirelessdevicecommunication.interfaces.ISLDeviceChanged;


public class MainActivity extends AppCompatActivity implements ISLDeviceChanged {
    // xu ly them phan cho nut RESET
    private enum HANDLE{
        NONE,
        OPEN_PORT,
        RESET,
        RUN,
        SET
    }
    public com.sl.commonutil.zip.Constants.COMPRESS_TYPE COMPRESS_TYPE = com.sl.commonutil.zip.Constants.COMPRESS_TYPE.NONE;
    private HANDLE currentHanle = HANDLE.NONE;
    private Spinner spDevice, spSetting, spVersion;
    private Button btnSet, btnRefresh, btnOpenPort, btnRunApp, btnReset;
    private IFCActionAdapter _actionAdapter;
    private DeviceAdapter _deviceAdapter;
    private VersionAdapter _versionAdapter;

    private TextView tvResult; // man hinh tren
    private TextView tvAction; // man hinh duoi
    Handler _handler;
    private List<Byte> _data = null;
    Toast _toast =null;
    private static final String SEPERATE = "@";
    private static final int REQUEST_ENABLE_BT = 1;
    private static int DELAY_BETWEEN_2_SEND_DATA = 300;
    //private static final String DATA_KEY = "data_key";
    private static int indexHtml = 0;
    private String[] colors = new String[] {"red", "blue","black"};
    private int _display = 1;
    private  static String TAG = "shenlong";
    private int _isReceive = 0;
    private boolean _isDecode = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        final Activity __currentActivity = (Activity) this;
        _toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        SLDeviceManager.getInstance().setContext(this, this);
        SLDeviceManager.getInstance().discoverBluetooth(3);
        spDevice = (Spinner)findViewById(R.id.spDevice);
        spSetting = (Spinner)findViewById(R.id.spSetting);
        spVersion  = (Spinner)findViewById(R.id.spVersion);
        spVersion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadSettings();
                loadDelay();
                loadButtonText();
                __currentActivity.invalidateOptionsMenu();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        btnSet = (Button)findViewById(R.id.btnSet);
        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        btnOpenPort = (Button)findViewById(R.id.btnOpenPort);
        btnOpenPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    _isReceive = 1;
                    JSONObject data = SharedPreferencesUtil.GetJSONObject(__currentActivity, getVersion());
                    JSONArray op = data.getJSONArray("OpenPort");
                    String[] openPortCode = new String[op.length()];
                    String openPortName = data.get("OpenPortName").toString();
                    for(int i = 0 ; i < op.length();i++)
                    {
                        openPortCode[i] = openPortName + SEPERATE + op.getString(i);
                    }
                    currentHanle = HANDLE.OPEN_PORT;
                    SendAsyncTask(openPortCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btnRunApp  = (Button)findViewById(R.id.btnRun);
        btnRunApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    JSONObject data = SharedPreferencesUtil.GetJSONObject(__currentActivity,getVersion());
                    JSONArray runAppCodes = data.getJSONArray("RunApp");
                    String runAppName = data.get("RunAppName").toString();
                    int count = runAppCodes.length();
                    String[] cs = new String[count];
                    for(int i = 0 ; i < count;i++){
                        cs[i] = runAppName + SEPERATE + runAppCodes.getString(i);
                    }


                    currentHanle = HANDLE.RUN;

                    SendAsyncTask(cs);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        btnReset  = (Button)findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    JSONObject data = SharedPreferencesUtil.GetJSONObject(__currentActivity, getVersion());
                    JSONArray op = data.getJSONArray("ResetCPU");
                    String[] resetCode = new String[op.length()];
                    String resetName = data.get("ResetCPUName").toString();
                    for(int i = 0 ; i < op.length();i++)
                    {
                        resetCode[i] = resetName + SEPERATE + op.getString(i);
                    }
                    currentHanle = HANDLE.RESET;
                    SendAsyncTask(resetCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        tvAction = (TextView)findViewById(R.id.tvAction);
        tvAction.setMovementMethod(new ScrollingMovementMethod());
        tvResult = (TextView)findViewById(R.id.tvResult);
        tvResult.setMovementMethod(new ScrollingMovementMethod());
        final MainActivity mainActivity = this;
        _handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                try {
                    if (msg.what == 1) {
                        byte[] sdata = (byte[]) msg.obj;
                        String decoded = new String(sdata, "UTF-8");


                        Log.i("shenlong", decoded);
                        if (_data == null || _data.size() == 0) {
                            _data = new ArrayList<Byte>();
                        }
                        for (int i = 0; i < msg.arg1; i++) {
                            _data.add(sdata[i]);
                        }
                        byte[] bytes = new byte[_data.size()];
                        for (int i = 0; i < _data.size(); i++) {
                            bytes[i] = _data.get(i);
                        }

                        String str = new String(bytes);
                        Boolean isDecode = mainActivity.isDecode();
                        if(currentHanle == HANDLE.OPEN_PORT && isDecode) {

                            String ipaddress = getIPAdress(_data);
                            if (_isReceive == 1 && ipaddress != "") {
                                setTextForResult(str + "\n\n" + ipaddress);
                                _isReceive = 0;
                            }
                        }
                        else {
                            String text = getTextResultFromDevice();
                            setTextForResult(text + "\n\n" + str );
                        }

                    }
                }catch (Exception ex)
                {}
                return true;
            }
        }) ;


        loadDevice();
        loadSettings();
        loadVersion();
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    tvResult.setText("");
                    tvAction.setText("");
                    clear();
                    loadDevice();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    currentHanle = HANDLE.SET;
                    doSetting();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public Boolean isDecode(){
        return this._isDecode;
    }
    @Override
    protected void onResume() {
        super.onResume();
        this._isDecode = SharedPreferencesUtil.GetBoolean(this, Constants.KEY_IS_DECODE);
    }

    private void setTextForResult(String text)
    {
        String now = "<b><u>" + getNow() + "</u></b>";
        text = now + "\n" + text;
        text = text.replace("\n","<br/>");
        String color = colors[indexHtml];
        String content =
                "<font color='" + color + "'>"
                        + text
                        +  "<br/></font>";

        tvResult.append(
                Html.fromHtml(content)
        );
    }
    private void handleWhenGetData()
    {
        final Activity __currentActivity = (Activity)this;
        JSONObject jsonObj = SharedPreferencesUtil.GetJSONObject(__currentActivity, getVersion());
    }
    private String getVersion()
    {

        String version = "";
        try
        {
            version = spVersion.getSelectedItem().toString();
            version = version.replace(" ", "_");
        }
        catch (Exception ex){}
        return  version;
    }
    private String getTextResultFromDevice(){
        String res = "";
        int count = _data.size();
        for(int i = 0 ; i < count;i++){
            int so = (int) _data.get(i);
            so = so < 0 ? so + 256 : so;
            String hex = Integer.toHexString(so).toUpperCase();
            if(hex.length() == 1){
                hex += "0";
            }
            res += hex + " ";
        }
        return res;
    }
    private String getIPAdress(List<Byte> data)
    {
        String port = "";
        String ip1 = "";
        String ip2 = "";
        String imei = "";
        String sim = "";
        String fw = "";
        String res = "";
        try
        {

            if (_data.size() > 250) {

                // imei
                for(int i = 32; i < 47;i++)
                {
                    int c = (int)_data.get(i) - 48;
                    imei += c + "";
                }
                // sim
                for(int i = 230; i < 249;i++)
                {
                    int c = (int)_data.get(i) - 48;
                    sim += c + "";
                }
                // ip1
                for(int i = 148; i < 152;i++ )
                {
                    int sopi = (int) _data.get(i);
                    sopi = sopi < 0 ? sopi + 256 : sopi;
                    ip1 += sopi + ".";
                }
                // ip2
                for(int i = 187; i < 191;i++ )
                {
                    int sopi = (int) _data.get(i);
                    sopi = sopi < 0 ? sopi + 256 : sopi;
                    ip2 += sopi + ".";
                }
                // port
                int p2 = (int) _data.get(185);
                int p1 = (int) _data.get(186);

                String s1 = (Integer.toHexString(p1).toUpperCase());
                if(s1.length() > 2)
                {
                    s1 = s1.substring(s1.length() - 2,2);
                }
                String s2 = (Integer.toHexString(p2).toUpperCase());
                if(s2.length() > 2)
                {
                    s2 = s2.substring(s2.length() - 2, s2.length());
                }
                String hex = (s1 + "" + s2) + "";
                port = Integer.parseInt(hex,16) + "";

                // fw
                for(int i = 68; i < 83;i++ )
                {
                    char c = (char)((byte)_data.get(i));
                    fw += c + "";
                }
                res = "Ip1 = " + ip1 + " : " + port
                        + "\r\n" + "IP2 = " + ip2 + " : " + port
                        + "\r\n" + "IMEI = " + imei
                        + "\r\n" + "SIM = " + sim
                        + "\r\n" + "Firmware = " + fw;
            }
        }
        catch(Exception ex)
        {
            res = "";
        }

        return res;
    }
    private void clear()
    {
        _data = null;
       //tvAction.setText("");
       //tvResult.setText("");
    }
    private String getNow()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }
    public void SendAsyncTask(String... action_data)
    {
        SendAsyncTask(1, action_data);
    }
    private void increaseIndexColor(){
        indexHtml++;
        if(indexHtml >= colors.length)
        {
            indexHtml = 0;
        }
    }
    public void SendAsyncTask(int display, String... action_data)
    {
        _display = display;
        increaseIndexColor();
        new SendTask().execute(action_data);
    }

    public int Send(String actionName, String data)
    {

        int res = 1;
        try {
            clear();
            ISLDevice device = (ISLDevice) spDevice.getSelectedItem();

            if (data != "") {
                data = data.trim().toUpperCase();
                byte[] bytes = IFCBusiness.getData(data);
                if (device != null) {
                    try {
                        boolean isConnected = SLDeviceManager.getInstance().isConnected(device.getSignature());
                        if (isConnected == true) {
                            SLDeviceManager.getInstance().doAction(device.getSignature(), SLDeviceManager.Action.SEND, bytes);
                            res = 1;
                        } else {
                            Log.i("shenlong", "Device is not connect !!!");
                        }
                    } catch (Exception e) {
                        Log.i("shenlong", "Error when SEND data");
                    }
                } else {
                    Log.i("shenlong", "Device is null !!!");
                }
            }
        }catch (Exception ex){
            Log.i("shenlong", "Send error !!!");
        }
        return res;
    }
    public  void doFactoryReset()
    {
        try {
            JSONObject data = SharedPreferencesUtil.GetJSONObject(this, getVersion());
            JSONArray fr = data.getJSONArray("FactoryReset");
            String[] updateFactoryResetStrs = new String[fr.length()];

            for (int i = 0; i < fr.length(); i++) {
                updateFactoryResetStrs[i] = "Factory Reset" + SEPERATE + fr.getString(i);
            }
            SendAsyncTask(updateFactoryResetStrs);
        }
        catch (Exception ex)
        {
            tvAction.setText("Cannot do Factory Reset \r\n" + tvAction.getText());
        }
    }
    private void doUpdateInternetWithNONECompress(String url, final ProgressDialog callServiceDialog) {
        WebApiUtil.GetAsync(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //tvAction.setText("Analizing data ...!!!"+ "\n" + tvAction.getText());
                    JSONArray arrayData = response.getJSONArray("Data");
                    handleForDataInternet(arrayData);
                    loadUI();
                    setTextForResult("Updating from internet DONE!!!");
                } catch (Exception e) {
                    setTextForResult("Error when Analyzing data!!!");
                }
                callServiceDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                setTextForResult("Error when connect server!!!");
                callServiceDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                setTextForResult("Error when connect server!!!");
                callServiceDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                setTextForResult("Error when connect server!!!");
                callServiceDialog.dismiss();
            }
        });
    }
    private void doUpdateInternetWithLZStringCompress(String url, final ProgressDialog callServiceDialog){
        WebApiUtil.GetAsync(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //tvAction.setText("Analizing data ...!!!"+ "\n" + tvAction.getText());
                    String dataStr = response.getString("Data");
                    String str = LZString.decompress(dataStr);
                    JSONObject data = new JSONObject(str);
                    JSONArray arrayData = data.getJSONArray("Data");
                    handleForDataInternet(arrayData);
                    loadUI();
                    loadUI();
                    setTextForResult("Updating from internet DONE!!!");
                } catch (Exception e) {
                    setTextForResult("Error when Analyzing data!!!");
                }
                callServiceDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                setTextForResult("Error when connect server!!!");
                callServiceDialog.dismiss();
            }
        });
    }
    private void handleForDataInternet(JSONArray arrayData) throws Exception{
        final Activity __currentActivity = (Activity)this;
        try {
            String names = "";
            for (int i = 0; i < arrayData.length(); i++) {
                JSONObject st = arrayData.getJSONObject(i);
                String name = st.getString("Name");
                names += name + ",";
                String code = name.replace(" ", "_");
                JSONObject data = st.getJSONObject("Object");
                String delay = data.getString("Delay");
                JSONArray op = data.getJSONArray("OpenPort");
                String runAppCode = data.getString("RunApp");
                JSONArray rc = data.getJSONArray("ResetCPU");
                JSONArray settings = data.getJSONArray("Settings");
                JSONArray fws = data.getJSONArray("UpdateFW");
                JSONArray fr = data.getJSONArray("FactoryReset");

                SharedPreferencesUtil.SetJSONObject(__currentActivity, code, data);


            }
            names = names.substring(0, names.length() - 1);
            SharedPreferencesUtil.SetString(__currentActivity, "Version", names);
        }
        catch (Exception ex){
            throw ex;
        }
    }
    private void loadUI(){
        final Activity __currentActivity = (Activity)this;
        loadVersion();
        loadSettings();
        loadButtonText();
        __currentActivity.invalidateOptionsMenu();
    }
    public void doUpdateInternet()
    {
        final Activity __currentActivity = (Activity)this;
        final ProgressDialog callServiceDialog = new ProgressDialog(__currentActivity );
        callServiceDialog.setTitle("IFC Update");
        callServiceDialog.setMessage("Updating from internet...!!!");
        callServiceDialog.setCanceledOnTouchOutside(false);
        callServiceDialog.show();
        increaseIndexColor();
        String urlService = SharedPreferencesUtil.GetString(this, Constants.KEY_URL_SERVICE);
        if(urlService == null || urlService.equals("")){
            loadSettingActivity();
            return;
        }
        if(COMPRESS_TYPE == COMPRESS_TYPE.LZSTRING){
            urlService += "1";
            doUpdateInternetWithLZStringCompress(urlService,callServiceDialog );
        }
        else{
            urlService += "1";
            doUpdateInternetWithNONECompress(urlService,callServiceDialog );
        }



    }

    public void doSetting(){

        try {
            JSONObject data = SharedPreferencesUtil.GetJSONObject(this, getVersion());
            JSONArray pre_sets = data.getJSONArray("WriteSetting");
            IFCSetting setting = (IFCSetting) spSetting.getSelectedItem();
            int count = pre_sets.length();
            String[] codes = setting.getCode().split(";");
            String[] cs = new String[count + codes.length];
            String actionName = data.get("WriteSettingName").toString();
            for(int i = 0 ; i < count;i++){
                cs[i] = actionName + SEPERATE + pre_sets.getString(i);
            }

            for(int i = 0 ; i < codes.length;i++){
                cs[i + count] = setting.getName() + SEPERATE + codes[i];
            }


            SendAsyncTask(cs);
        } catch (Exception e) {
            tvResult.setText("Cannot write setting");
        }
    }
    public void loadVersion(){
        try {
            String data = SharedPreferencesUtil.GetString(this, "Version").toString();

            String[] versions =data.split(",");

            _versionAdapter =  new VersionAdapter(this, Arrays.asList(versions));
            spVersion.setAdapter(_versionAdapter);
        }
        catch (Exception ex){

        }
    }
    public void loadSettings()
    {
        try {
            JSONObject data = SharedPreferencesUtil.GetJSONObject(this, getVersion());
            JSONArray settings = data.getJSONArray("Settings");
            List<IFCSetting> actions = new ArrayList<IFCSetting>();
            for (int i = 0; i < settings.length(); i++) {
                JSONObject st = settings.getJSONObject(i);
                String name = st.getString("Name");
                String code = st.getString("Code");
                actions.add(new IFCSetting(name, code.trim()));
            }
            _actionAdapter = new IFCActionAdapter(this, actions);
            spSetting.setAdapter(_actionAdapter);
        }
        catch (Exception ex){
            setTextForResult("Error when load setting \n");
        }
    }
    private void loadButtonText()
    {
        try {
            JSONObject data = SharedPreferencesUtil.GetJSONObject(this, getVersion());
            String openPortName = data.getString("OpenPortName");
            String resetCPUName = data.getString("ResetCPUName");
            String runAppName = data.getString("RunAppName");
            String writeSettingName = data.getString("WriteSettingName");


            btnOpenPort.setText(openPortName);
            btnReset.setText(resetCPUName);
            btnRunApp.setText(runAppName);
            btnSet.setText(writeSettingName);
            this.invalidateOptionsMenu();
        }
        catch (Exception ex){
            setTextForResult("Error when load setting \n");
        }
    }
    public void loadDelay()
    {
        try {
            JSONObject data = SharedPreferencesUtil.GetJSONObject(this, getVersion());
            String delay = data.getString("Delay");
            DELAY_BETWEEN_2_SEND_DATA = Integer.parseInt(delay);


            setTextForResult("Delay in " + DELAY_BETWEEN_2_SEND_DATA);
        }
        catch (Exception ex){
            setTextForResult("Error when load delay \n");
        }
    }
    public void loadDevice()
    {
        try {
            BluetoothAdapter mBluetoothAdapter = SLDeviceManager.getInstance().makesureEnableBluetooth();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    try {
                        SLBluetoothDevice btDevice = new SLBluetoothDevice(device);
                        int res = SLDeviceManager.getInstance().manage(btDevice, _handler);
                    } catch (Exception ex) {

                    }
                }
            }
            _deviceAdapter = new DeviceAdapter(this, SLDeviceManager.getInstance().getDevices());
            spDevice.setAdapter(_deviceAdapter);
        }
        catch(Exception ex)
        {
            setTextForResult("Error when load devices \n");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0,1,0,"Update Internet");
        try {
            JSONObject data = SharedPreferencesUtil.GetJSONObject(this, getVersion());
            String factoryResetName = data.getString("FactoryResetName");
            menu.add(0, 2, 0, factoryResetName);
        }
        catch(Exception ex)
        {
            menu.add(0, 2, 0, "Factory Reset");
        }
        int idx = 3;
        try {
            JSONObject data = SharedPreferencesUtil.GetJSONObject(this, getVersion());
            JSONArray fws = data.getJSONArray("UpdateFW");
            for (int i = 0; i < fws.length(); i++) {
                JSONObject st = fws.getJSONObject(i);
                String name = st.getString("Name");
                menu.add(0,idx,0,name);
                idx++;
            }
        }
        catch (Exception ex)
        {

        }

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    private void loadSettingActivity(){
        Intent i = new Intent(this, SettingActivity.class);
        startActivity(i);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == 1)
        {
            doUpdateInternet();
            return true;
        }
        else if(id == 2)
        {
            doFactoryReset();
            return true;
        }
        else if(item.getItemId() == R.id.mnuATCode)
        {
            setContentView(R.layout.atcode_layout);
            return true;
        }
        else if(item.getItemId() == R.id.mnuMain)
        {
            setContentView(R.layout.activity_main);
            return true;
        }
        else if(item.getItemId() == R.id.mnuSetting)
        {
            loadSettingActivity();
        }
        else {
            String vers = getVersion();
            try {

                JSONObject data = SharedPreferencesUtil.GetJSONObject(this, vers);
                JSONArray fws = data.getJSONArray("UpdateFW");
                for (int i = 0; i < fws.length(); i++) {
                    JSONObject st = fws.getJSONObject(i);
                    String name = st.getString("Name");
                    int display = st.optBoolean("Display",false) == true ? 1 : 0;
                    if(name.toString().equals(item.getTitle().toString()))
                    {
                        JSONArray codes = st.getJSONArray("Code");
                        String[] cs = new String[codes.length()];
                        for(int j = 0 ; j < codes.length();j++)
                        {
                            cs[j] = name + SEPERATE + codes.getString(j);
                        }
                        SendAsyncTask(display, cs);
                        break;
                    }

                }
            }catch (Exception ex)
            {
                setTextForResult("Error when UpdateFW : " + vers);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int onDeviceDisconnected(int signature) {

        try {
            SLDeviceManager.getInstance().disconnect(signature);
            _deviceAdapter.notifyDataSetChanged();
            //tvResult.setText("");
            tvAction.setText("Bluetooth device is disconnected !!!\nPlease re-connect for next action !!!" + "\n" + tvAction.getText() );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return signature;
    }


    private class SendTask extends AsyncTask<String, String, Integer> {
        protected Integer doInBackground(String... actionname_data) {
            int count = actionname_data.length;
            int res = 0;
            try {

                for (int i = 0 ; i < count; i++) {
                    String act_data = actionname_data[i];
                    String[] a_d = act_data.split(SEPERATE);

                    String actionname = a_d[0];
                    String data = a_d[1];

                    publishProgress(actionname, data, "(" + (i + 1) + "/" + count + ")",  i+"", count+"");
                    res = Send(actionname, data);
                    if(res == 0)
                    {
                        break;
                    }
                    Thread.sleep(DELAY_BETWEEN_2_SEND_DATA);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return count;
        }

        protected void onProgressUpdate(String... progress) {
            try {
                String actionname = progress[0];
                String data = progress[1];
                String prog = progress[2];
                String currentDateandTime = "<b><u>" + getNow() + "</u></b>";
                int idx = Integer.parseInt(progress[3]);
                int count = Integer.parseInt(progress[4]);
                String color = colors[indexHtml];
                if (_display == 1) {


                    String content =
                            "<font color='" + color + "'>"
                                    + "<br/>"
                                    + currentDateandTime + " : " + actionname + " " + prog
                                    + "<br/>"
                                    + data + "<br/>"
                                    + IFCBusiness.toContent(data)
                                    + "</font>";

                    tvAction.append(
                            Html.fromHtml(content)
                    );
                } else {
                    String sflash = "<font color='" + color + "'>"
                            + "|</font>";
                    String content = "";
                    if (idx == 0){

                        content =
                                "<font color='" + color + "'><br/>"
                                        + currentDateandTime
                                        + " : " + actionname
                                        + "<br/></font>";
                        tvAction.append(Html.fromHtml(content));
                    }


                    tvAction.append(Html.fromHtml(sflash));


                    if (idx == count - 1) {
                        content =
                                "<font color='" + color + "'>"
                                        + prog + "<br/></font>";
                        tvAction.append(Html.fromHtml(content));
                    }

                }
            }
            catch (Exception ex){
                Log.i("","");
            }
        }
    }
}
