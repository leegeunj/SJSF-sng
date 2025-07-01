package com.creative.sng.app.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.creative.sng.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class BoardAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private ArrayList<HashMap<String,Object>> boardList;
	private ViewHolder viewHolder;
	private Context con;
	private String name="";


	public BoardAdapter(Context con , ArrayList<HashMap<String,Object>> array){
		inflater = LayoutInflater.from(con);
		boardList = array;
		this.con = con;
	}

	public BoardAdapter(Context con , ArrayList<HashMap<String,Object>> array, String name){
		inflater = LayoutInflater.from(con);
		boardList = array;
		this.con = con;
		this.name = name;
	}

	@Override
	public int getCount() {
		return boardList.size();
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

		View v = convertview;

		if(v == null){
			viewHolder = new ViewHolder();

			if(name.equals("NoticeBoard")){
				v = inflater.inflate(R.layout.basic_list_item, parent,false);
				viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
				viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);
				viewHolder.board_data3 = (TextView)v.findViewById(R.id.textView3);
				viewHolder.board_data4 = (TextView)v.findViewById(R.id.textView4);

			}else if(name.equals("Running")) {
				v = inflater.inflate(R.layout.running_list_item, parent, false);
				viewHolder.board_data1 = (TextView) v.findViewById(R.id.textView1);
				viewHolder.board_data2 = (TextView) v.findViewById(R.id.textView2);
				viewHolder.board_data3 = (TextView) v.findViewById(R.id.textView3);
				viewHolder.board_data4 = (TextView) v.findViewById(R.id.textView4);
			}else if(name.equals("GearCheckDaily")){
				v = inflater.inflate(R.layout.gear_check_daily_item, parent, false);
				viewHolder.board_data1 = (TextView) v.findViewById(R.id.textView1);
				viewHolder.board_data2 = (TextView) v.findViewById(R.id.textView2);
				viewHolder.board_data3 = (TextView) v.findViewById(R.id.textView3);
				viewHolder.board_data4 = (TextView) v.findViewById(R.id.textView4);
				if (boardList.get(position).get("data4").toString().equals("승인요청")){
					viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_blue_round));
					viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
				} else if (boardList.get(position).get("data4").toString().equals("1차반려")||boardList.get(position).get("data4").toString().equals("2차반려")) {
					viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_red_round));
					viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
				} else {
					viewHolder.board_data4.setBackground(con.getResources().getDrawable(R.drawable.border_green_round));
					viewHolder.board_data4.setTextColor(Color.parseColor("#ffffff"));
				}
			}else{
				v = inflater.inflate(R.layout.basic_list_item2, parent,false);
				viewHolder.board_data1 = (TextView)v.findViewById(R.id.textView1);
				viewHolder.board_data2 = (TextView)v.findViewById(R.id.textView2);
				viewHolder.board_data3 = (TextView)v.findViewById(R.id.textView3);
				viewHolder.board_data4 = (TextView)v.findViewById(R.id.textView4);

			}

			v.setTag(viewHolder);

		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		viewHolder.board_data1.setText(boardList.get(position).get("data1").toString());
		viewHolder.board_data2.setText(boardList.get(position).get("data2").toString());
		viewHolder.board_data3.setText(boardList.get(position).get("data3").toString());
		viewHolder.board_data4.setText(boardList.get(position).get("data4").toString());

		return v;
	}

	
	public void setArrayList(ArrayList<HashMap<String,Object>> arrays){
		this.boardList = arrays;
	}
	
	public ArrayList<HashMap<String,Object>> getArrayList(){
		return boardList;
	}
	
	
	/*
	 * ViewHolder
	 */
	class ViewHolder{
		TextView board_data1;
		TextView board_data2;
		TextView board_data3;
		TextView board_data4;
	}
	

}







