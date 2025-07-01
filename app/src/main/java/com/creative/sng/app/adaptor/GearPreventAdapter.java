package com.creative.sng.app.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.creative.sng.app.R;

import java.util.ArrayList;
import java.util.HashMap;

public class GearPreventAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<HashMap<String,Object>> gearList;
    private ViewHolder viewHolder;
    private Context con;
    private String name="";

    public GearPreventAdapter(Context con, ArrayList<HashMap<String, Object>> array){
        inflater = LayoutInflater.from(con);
        gearList = array;
        this.con = con;
    }

    public GearPreventAdapter(Context con, ArrayList<HashMap<String, Object>> array, String name){
        inflater = LayoutInflater.from(con);
        gearList = array;
        this.con = con;
        this.name= name;
    }

    @Override
    public int getCount() {
        return gearList.size();
    }

    @Override
    public Object getItem(int position) { return null;  }

    @Override
    public long getItemId(int position) { return 0;  }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if(v == null){
            viewHolder = new ViewHolder();

            v = inflater.inflate(R.layout.gear_check_prevent_item, parent, false);
            viewHolder.gear_data1 = (TextView) v.findViewById(R.id.textView1);
            viewHolder.gear_data2 = (TextView) v.findViewById(R.id.textView2);
            viewHolder.gear_data3 = (TextView) v.findViewById(R.id.textView3);
            viewHolder.gear_data4 = (TextView) v.findViewById(R.id.textView4);
            viewHolder.gear_data5 = (TextView) v.findViewById(R.id.textView5);
            viewHolder.gear_data6 = (TextView) v.findViewById(R.id.textView6);

            v.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)v.getTag();
        }

        viewHolder.gear_data1.setText(gearList.get(position).get("data1").toString());
        viewHolder.gear_data2.setText(gearList.get(position).get("data2").toString());
        viewHolder.gear_data3.setText(gearList.get(position).get("data3").toString());
        viewHolder.gear_data4.setText(gearList.get(position).get("data4").toString());
        viewHolder.gear_data5.setText(gearList.get(position).get("data5").toString());
        viewHolder.gear_data6.setText(gearList.get(position).get("data6").toString());


        return v;
    }

    public void setArrayList(ArrayList<HashMap<String, Object>> arrays){ this.gearList = arrays; }
    public ArrayList<HashMap<String,Object>> getArrayList(){ return gearList; }

    /*
     * ViewHolder
     */
    class ViewHolder{

        TextView gear_data1;
        TextView gear_data2;
        TextView gear_data3;
        TextView gear_data4;
        TextView gear_data5;
        TextView gear_data6;
    }
}
