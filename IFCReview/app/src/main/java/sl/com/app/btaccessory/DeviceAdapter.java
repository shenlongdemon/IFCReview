package sl.com.app.btaccessory;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import sl.com.lib.wirelessdevicecommunication.ISLDevice;
import sl.com.lib.wirelessdevicecommunication.SLDeviceManager;


/**
 * Created by shenlong on 9/25/2015.
 */
public class DeviceAdapter extends BaseAdapter {
    LayoutInflater inflator;
    private List<ISLDevice> _devices;
    private Context _context;

    public DeviceAdapter( Context context ,List<ISLDevice> devices)
    {
        _context = context;
        inflator = LayoutInflater.from(_context);
        _devices = devices;
    }

    @Override
    public int getCount()
    {
        return _devices.size();
    }

    @Override
    public Object getItem(int position)
    {
        return _devices.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = inflator.inflate(R.layout.devicelistitem, null);
        ISLDevice device = (ISLDevice)getItem(position);
        TextView tv = (TextView) convertView.findViewById(R.id.tvName);
        tv.setText(device.getName());
        final Button btn = (Button) convertView.findViewById(R.id.btnConnect);
        final int itemIndex = position;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnConnect_Click(btn, itemIndex);
            }
        });
        setStatus(btn, device);

        return convertView;
    }
    private void setStatus(View btn, ISLDevice device)
    {
        boolean isConnected = SLDeviceManager.getInstance().isConnected(device.getSignature());
        Button btnConnect = (Button)btn;
        if(isConnected == true)
        {
            btnConnect.setText("End");
        }
        else
        {
            btnConnect.setText("Start");
        }
    }
    private void btnConnect_Click(Button btn, int itemIndex)
    {
        try {
            TextView tvAction = (TextView)((Activity)_context).findViewById(R.id.tvAction);
            tvAction.setText("");
            ISLDevice device = (ISLDevice) getItem(itemIndex);

            boolean isConnected = SLDeviceManager.getInstance().isConnected(device.getSignature());
            if(isConnected == false) {
                try {
                    SLDeviceManager.getInstance().connect(device.getSignature());
                    tvAction.setText("Connect to device " + device.getName() + " successfully !!!" );
                }
                catch (Exception ex)
                {
                    tvAction.setText("Cannot connect device " + device.getName() + " !!!\nPlease ensure that device is available !!!");
                }
            }
            else {
                SLDeviceManager.getInstance().disconnect(device.getSignature());
            }
            setStatus(btn, device);
        }
        catch (Exception ex)
        {
            Log.i("shenlong", "DeviceItemView -> btnConnect -> " + ex.getMessage());
        }
    }

}
