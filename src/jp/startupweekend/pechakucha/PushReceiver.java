package jp.startupweekend.pechakucha;

import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class PushReceiver extends BroadcastReceiver {
	private static final String TAG = "PushReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			String channel = intent.getExtras().getString("com.parse.Channel");
			JSONObject json = new JSONObject(intent.getExtras().getString(
					"com.parse.Data"));

			Log.d(TAG, "got action " + action + " on channel " + channel
					+ " with:");

			Intent startActivityIntent = new Intent(context, ChatActivity.class);

			String type = "";
			
			Iterator itr = json.keys();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				if (key.equalsIgnoreCase("alert")) {
					startActivityIntent.putExtra(
							ChatActivity.EXTRA_KEY_MSG_CONT,
							json.getString(key));
				} else if (key.equalsIgnoreCase("type")){
					type = json.getString(key);
				}
				
				Log.d(TAG, "..." + key + " => " + json.getString(key));
			}

			startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getRunning(context);
			
			if(type.equalsIgnoreCase("connected")){
				ChatActivity.getInstance().showWait(false);
			}
			
			if (ChatActivity.isInFront == true) {
				// context.startActivity(startActivityIntent);
				ChatActivity
						.getInstance()
						.showMessage(
								startActivityIntent
										.getStringExtra(ChatActivity.EXTRA_KEY_MSG_CONT),
								false);
			}
			
			//TODO: if the app is NOT in front, or not running, create a notification by yourself
		} catch (JSONException e) {
			Log.d(TAG, "JSONException: " + e.getMessage());
		}
	}

	public void getRunning(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List l = am.getRunningAppProcesses();
		Iterator i = l.iterator();
		PackageManager pm = context.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
					.next());
			try {
				CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(
						info.processName, PackageManager.GET_META_DATA));
				Log.w("LABEL", c.toString());
//				runningApplications.add(c.toString());
			} catch (Exception e) {
				// Name Not Found Exception
			}
		}

	}
}