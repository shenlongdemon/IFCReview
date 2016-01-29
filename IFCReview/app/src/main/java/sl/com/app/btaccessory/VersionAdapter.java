package sl.com.app.btaccessory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


/**
 * Created by shenlong on 9/25/2015.
 */
public class VersionAdapter extends BaseAdapter {
    LayoutInflater inflator;
    private List<String> _versions;
    private Context _context;

    public VersionAdapter(Context context, List<String> versions)
    {
        _context = context;
        inflator = LayoutInflater.from(_context);
        _versions = versions;
    }

    @Override
    public int getCount()
    {
        return _versions.size();
    }

    @Override
    public Object getItem(int position)
    {
        return _versions.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = inflator.inflate(R.layout.versionlistitem, null);
        String vers = (String)getItem(position);
        TextView tv = (TextView) convertView.findViewById(R.id.tvVersionName);
        tv.setText(vers);
        return convertView;
    }

}
