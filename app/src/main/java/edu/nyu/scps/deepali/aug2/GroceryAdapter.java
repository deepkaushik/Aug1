package edu.nyu.scps.deepali.aug2;

/**
 * Created by 205263 on 8/13/15.
 */


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;


public class GroceryAdapter extends BaseAdapter {
    private Context context;
    List<ParseObject> parseObjects;

    public GroceryAdapter(Context context, List<ParseObject> parseObjects) {
        this.context = context;
        this.parseObjects = parseObjects;
    }

    @Override
    public int getCount() {
        return parseObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return parseObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            linearLayout = (LinearLayout)inflater.inflate(android.R.layout.simple_list_item_2, null);
        } else {
            linearLayout = (LinearLayout)convertView;
        }

        ParseObject parseObject = parseObjects.get(position);
        TextView textView = (TextView)linearLayout.findViewById(android.R.id.text1);
        textView.setText(parseObject.getString("name"));
        textView = (TextView)linearLayout.findViewById(android.R.id.text2);
        textView.setText(parseObject.getObjectId() + "\n"
                + parseObject.getCreatedAt() + "\n"
                + parseObject.getUpdatedAt());
        return linearLayout;
    }
}