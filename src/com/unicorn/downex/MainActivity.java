package com.unicorn.downex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.unicorn.downex.core.Constants;
import com.unicorn.downex.core.DownloadMessage.BooleanMessage;
import com.unicorn.downex.core.DownloadMessage.ProgressMessage;
import com.unicorn.downex.core.DownloadMessage.ProgressMessageList;
import com.unicorn.downex.widget.DownloadActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class MainActivity extends DownloadActivity {

	private BaseAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ListView list = (ListView) findViewById(R.id.list);
		mAdapter = createAdapter(createData(), createListener());
		list.setAdapter(mAdapter);
		
		
		startMonitorDownload(2);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopMonitorDownload();
	}
	
	private BaseAdapter createAdapter(final List<DownloadItem> data, final OnClickListener listener) {
    	BaseAdapter adapter = new BaseAdapter() {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder holder = null;
                DownloadItem item = (DownloadItem) getItem(position);
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = View.inflate(MainActivity.this,
                            R.layout.list_item, null);
                    holder.titleTxv = (TextView) convertView
                            .findViewById(R.id.title);
                    holder.operateBtn = (Button) convertView
                            .findViewById(R.id.operate);
                    holder.operateBtn.setOnClickListener(listener);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.titleTxv.setText(item.title);
                switch (item.status) {
                    case DownloadItem.MODEL_STATUS_UNDOWNLOAD:
                        holder.operateBtn.setText("download");
                        break;
                    case DownloadItem.MODEL_STATUS_DOWNLOADING:
                    	holder.operateBtn.setText("pause");
                        break;
                    case DownloadItem.MODEL_STATUS_PAUSED:
                    	holder.operateBtn.setText("resume");
                    	break;
                    case DownloadItem.MODEL_STATUS_DOWNLOADED:
                        break;
                    default:
                        break;
                }
                holder.operateBtn.setTag(item);
                return convertView;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public Object getItem(int position) {
                return data.get(position);
            }

            @Override
            public int getCount() {
                return data.size();
            }
        };
        return adapter;
    }

	
	private List<DownloadItem> createData() {
		List<DownloadItem> data = new ArrayList<DownloadItem>();
    	data.add(new DownloadItem(
				"Clocker",
				DownloadItem.MODEL_STATUS_UNDOWNLOAD,
				"http://test.designer.c-launcher.com/resources/jp/apk/app/20/530dcc280cf23d54d0c6acbc/pack_1393570103106_1455307.apk"));
    	data.add(new DownloadItem(
				"Twitter",
				DownloadItem.MODEL_STATUS_UNDOWNLOAD,
				"http://test.designer.c-launcher.com/resources/jp/apk/app/706/530cb4e80cf21217accdfdc2/pack_1393341739304_8500487.apk"));
    	data.add(new DownloadItem(
				"Flappy Bird",
				DownloadItem.MODEL_STATUS_UNDOWNLOAD,
				"http://test.designer.c-launcher.com/resources/jp/apk/game/480/5309444f0cf2396ac8868296/pack_1393116380143_915558.apk"));
    	data.add(new DownloadItem(
				"MX Player",
				DownloadItem.MODEL_STATUS_UNDOWNLOAD,
				"http://test.designer.c-launcher.com/resources/jp/apk/app/749/530cb1cb0cf21217accdfdbd/pack_1393340915336_8731894.apk"));
    	
    	for(DownloadItem item : data) {
    	    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + item.key + ".apk");
    	    if(file.exists()) {
    	        item.status = DownloadItem.MODEL_STATUS_DOWNLOADED;
    	    }
    	    
    	    if(file.exists()) {
                item.status = DownloadItem.MODEL_STATUS_DOWNLOADING;
            }
    	}
    	
    	
    	return data;
    }
	
	private OnClickListener createListener() {
    	OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                final DownloadItem item = (DownloadItem) v.getTag();
                switch (item.status) {
                    case DownloadItem.MODEL_STATUS_UNDOWNLOAD:
                    	item.status = DownloadItem.MODEL_STATUS_DOWNLOADING;
                    	BooleanMessage msg = startDownload(item.url,Environment.getExternalStorageDirectory().getAbsolutePath());
                    	Log.e(TAG, "start return " + msg.value);
                        break;
                    case DownloadItem.MODEL_STATUS_DOWNLOADING:
                    	item.status = DownloadItem.MODEL_STATUS_PAUSED;
                    	pauseDownload(item.key);
                        break;
                    case DownloadItem.MODEL_STATUS_PAUSED:
                    	item.status = DownloadItem.MODEL_STATUS_DOWNLOADING;
                    	resumeDownload(item.url, Environment.getExternalStorageDirectory().getAbsolutePath());
                    	break;
                    case DownloadItem.MODEL_STATUS_DOWNLOADED:
                        break;
                    default:
                        break;
                }
                mAdapter.notifyDataSetChanged();
            }
        };
        return listener;
    }        

	private static class ViewHolder {

        public TextView titleTxv;
        public Button operateBtn;

    }

    @Override
    protected Handler getHandler() {
       
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ProgressMessageList list = (ProgressMessageList)msg.obj;
                for(ProgressMessage progress : list) {
                    Log.e(TAG, progress.key + ":current/total = " + progress.currentBytes + "/" + progress.totalBytes);
                    if(Constants.Status.isStatusError(progress.status)) {
                        Log.e(TAG, "Download " + progress.key + " failed for error " + progress.status);
                    }
                }
            }
        };
    }
}
