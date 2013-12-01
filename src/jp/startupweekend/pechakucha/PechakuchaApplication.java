package jp.startupweekend.pechakucha;

import java.util.HashMap;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;

import com.parse.ParseUser;

import android.app.Application;

public class PechakuchaApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Add your initialization code here
		Parse.initialize(this, "x5OHg87BsqqV4PMSkuLJ5LXsnP92HOzHOWtD0Tax", "qF7YvU4RKwI3GMzU1WhJy92wtljg4PGZFO0rjIV1");


		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
	    
		// If you would like all objects to be private by default, remove this line.
		defaultACL.setPublicReadAccess(true);
		
		ParseACL.setDefaultACL(defaultACL, true);
		
		
		
		PushService.setDefaultPushCallback(this, ChatActivity.class);
		
		
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}
	
	

}
