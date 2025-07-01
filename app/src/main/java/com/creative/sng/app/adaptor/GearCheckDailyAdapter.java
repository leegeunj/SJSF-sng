package com.creative.sng.app.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.creative.sng.app.R;
import com.creative.sng.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

public class GearCheckDailyAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<HashMap<String,Object>> gearcheckList;
    private ViewHolder viewHolder;
    private Context con;
    private String name="";


    public GearCheckDailyAdapter(Context con , ArrayList<HashMap<String,Object>> array){
        inflater = LayoutInflater.from(con);
        gearcheckList = array;
        this.con = con;
    }

    public GearCheckDailyAdapter(Context con , ArrayList<HashMap<String,Object>> array, String name){
        inflater = LayoutInflater.from(con);
        gearcheckList = array;
        this.con = con;
        this.name = name;
    }

    @Override
    public int getCount() {
        return gearcheckList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, final View convertview, ViewGroup parent) {

//        UtilClass.logD("포지션0 : ", String.valueOf(position));
//        UtilClass.logD("상태값0 : ", gearcheckList.get(position).get("data4").toString());

        View v = convertview;

        if(v == null){
            viewHolder = new ViewHolder();
//            UtilClass.logD("포지션1 : ", String.valueOf(position));
//            UtilClass.logD("상태값1 : ", gearcheckList.get(position).get("data4").toString());

            if(name.equals("GearCheckDaily")){
                v = inflater.inflate(R.layout.gear_check_daily_item, parent, false);
                viewHolder.board_data1 = (TextView) v.findViewById(R.id.textView1);
                viewHolder.board_data2 = (TextView) v.findViewById(R.id.textView2);
                viewHolder.board_data3 = (TextView) v.findViewById(R.id.textView3);
                viewHolder.board_data4 = (TextView) v.findViewById(R.id.textView4);
//                UtilClass.logD("포지션2 : ", String.valueOf(position));
//                UtilClass.logD("상태값2 : ", gearcheckList.get(position).get("data4").toString());
                if (gearcheckList.get(position).get("data4").toString().equals("승인요청")){
                    viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_blue_round));
                    viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
                } else if (gearcheckList.get(position).get("data4").toString().equals("1차반려")||gearcheckList.get(position).get("data4").toString().equals("2차반려")) {
                    viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_red_round));
                    viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
                } else {
                    viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_green_round));
                    viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
                }
                viewHolder.board_data5 = (TextView) v.findViewById(R.id.textView5);
            }else{
                v = inflater.inflate(R.layout.basic_list_item2, parent,false);
                viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
                viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);
                viewHolder.board_data3 = (TextView)v.findViewById(R.id.textView3);
                viewHolder.board_data4 = (TextView)v.findViewById(R.id.textView4);
                viewHolder.board_data5 = (TextView)v.findViewById(R.id.textView5);

            }

            v.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder)v.getTag();

            if(name.equals("GearCheckDaily")) {

                if (gearcheckList.get(position).get("data4").toString().equals("승인요청")) {
                    viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_blue_round));
                    viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
                } else if (gearcheckList.get(position).get("data4").toString().equals("1차반려") || gearcheckList.get(position).get("data4").toString().equals("2차반려")) {
                    viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_red_round));
                    viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
                } else {
                    viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_green_round));
                    viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
                }
            }

        }
        viewHolder.board_data1.setText(gearcheckList.get(position).get("data1").toString());
        viewHolder.board_data2.setText(gearcheckList.get(position).get("data2").toString());
        viewHolder.board_data3.setText(gearcheckList.get(position).get("data3").toString());
        viewHolder.board_data4.setText(gearcheckList.get(position).get("data4").toString());
        viewHolder.board_data5.setText(gearcheckList.get(position).get("data5").toString());

        return v;
    }


    public void setArrayList(ArrayList<HashMap<String,Object>> arrays){
        this.gearcheckList = arrays;
    }

    public ArrayList<HashMap<String,Object>> getArrayList(){
        return gearcheckList;
    }



    /*
     * ViewHolder
     */
    class ViewHolder{
        TextView board_data1;
        TextView board_data2;
        TextView board_data3;
        TextView board_data4;
        TextView board_data5;
    }


}
