package com.example.swipelayout.activity;

import java.util.ArrayList;
import java.util.List;


import com.example.swipelayout.R;
import com.example.swipelayout.view.SwipeLayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	List<String> nums;
	MyAdapter adapter;
	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);

		listview = (ListView) findViewById(R.id.listview);
		InitData();
	}

	private void InitData() {
		nums = new ArrayList<String>();
		for (int i = 0; i < 20; i++) {
			nums.add(i + "");
		}
		adapter = new MyAdapter();
		listview.setAdapter(adapter);
	}

	private class MyAdapter extends BaseAdapter {
		

	
		@Override
		public int getCount() {
			return nums.size();
		}

		@Override
		public Object getItem(int position) {
			return nums.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			
			

			View view;
			final ViewHolder holder;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(MainActivity.this, R.layout.swipelayout_item,null);
				holder = new ViewHolder();
				
				holder.swipelayout = (SwipeLayout)view.findViewById(R.id.swipelayout);	
				holder.ll_edit = (LinearLayout)view.findViewById(R.id.ll_edit);
				holder.ll_delete = (LinearLayout)view.findViewById(R.id.ll_delete);			
				holder.tv_name = (TextView)view.findViewById(R.id.tv_name);
			
				view.setTag(holder);
				SwipeLayout.addSwipeView(holder.swipelayout);
			}
			
			
			holder.tv_name.setText(nums.get(position));
			holder.ll_delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					nums.remove(position);
					notifyDataSetChanged();
					SwipeLayout.removeSwipeView(holder.swipelayout);

				}
			});
		
			return view;
		
			
			
		}
	}

	class ViewHolder {
		SwipeLayout swipelayout;
		TextView tv_name;
		LinearLayout ll_edit;
		LinearLayout ll_delete;

	}
}
