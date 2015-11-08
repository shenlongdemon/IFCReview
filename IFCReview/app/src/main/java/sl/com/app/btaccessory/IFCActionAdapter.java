package sl.com.app.btaccessory;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shenlong on 9/25/2015.
 */
public class IFCActionAdapter extends BaseAdapter {
    LayoutInflater inflator;
    private List<IFCSetting> _action;

    public IFCActionAdapter(Context context, List<IFCSetting> action)
    {
        inflator = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _action = action;
    }

    @Override
    public int getCount()
    {
        return _action.size();
    }

    @Override
    public Object getItem(int position)
    {
        return _action.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = inflator.inflate(R.layout.actionlistitem, null);
        IFCSetting action = (IFCSetting)getItem(position);
        TextView tv = (TextView) convertView.findViewById(R.id.tvActionName);
        tv.setText(action.getName());
        final Button btn = (Button) convertView.findViewById(R.id.btnDetail);
        final int itemIndex = position;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDetail_Click(btn, itemIndex);
            }
        });

        return convertView;
    }
    private void btnDetail_Click(Button btn, int itemIndex)
    {
        try {

        }
        catch (Exception ex)
        {
            Log.i("shenlong", "DeviceItemView -> btnDetail_Click -> " + ex.getMessage());
        }
    }
}
