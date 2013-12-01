package jp.startupweekend.pechakucha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jp.startupweekend.pechakucha.model.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;

public class ChatActivity extends Activity implements OnClickListener {

	public static String EXTRA_KEY_MSG_CONT = "EXTRA_KEY_MSG_CONT";
	public static boolean isInFront;
	public static ChatActivity current;

	private EditText messageText;
	private TextView meLabel;
	private TextView friendLabel;
	private ViewGroup messagesContainer;
	private ScrollView scrollContainer;
	private ChatController chatController = new ChatController();
	TextView txvWaiting;
	Button btnQuestion1;
	Button btnQuestion2;
	Button btnQuestion3;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		current = this;

		// UI stuff
		messagesContainer = (ViewGroup) findViewById(R.id.messagesContainer);
		scrollContainer = (ScrollView) findViewById(R.id.scrollContainer);

		btnQuestion1 = (Button) findViewById(R.id.btnQues1);
		btnQuestion1.setOnClickListener(this);

		btnQuestion2 = (Button) findViewById(R.id.btnQues2);
		btnQuestion2.setOnClickListener(this);

		btnQuestion3 = (Button) findViewById(R.id.btnQues3);
		btnQuestion3.setOnClickListener(this);

		final Button sendMessageButton = (Button) findViewById(R.id.sendButton);
		txvWaiting = (TextView) findViewById(R.id.txvWaiting);

		sendMessageButton.setOnClickListener(onSendMessageClickListener);
		messageText = (EditText) findViewById(R.id.messageEdit);

		final View activityRootView = findViewById(R.id.activityRoot);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect r = new Rect();
						// r will be populated with the coordinates of your view
						// that area still visible.
						activityRootView.getWindowVisibleDisplayFrame(r);

						int heightDiff = activityRootView.getRootView()
								.getHeight() - (r.bottom - r.top);
						if (heightDiff > 100) { // if more than 100 pixels, its
												// probably a keyboard...
							btnQuestion1.setVisibility(View.GONE);
							btnQuestion2.setVisibility(View.GONE);
							btnQuestion3.setVisibility(View.GONE);
						} else if (heightDiff < 100) {
							btnQuestion1.setVisibility(View.VISIBLE);
							btnQuestion2.setVisibility(View.VISIBLE);
							btnQuestion3.setVisibility(View.VISIBLE);
						}
					}
				});

		ParseAnalytics.trackAppOpened(getIntent());

		startConversation();

		Intent intent = getIntent();
		String message = intent.getStringExtra(ChatActivity.EXTRA_KEY_MSG_CONT);
		if (message != null && !message.isEmpty())
			showMessage(message, false);

	}

	public static ChatActivity getInstance() {
		if (current != null) {
			return current;
		} else {
			return new ChatActivity();
		}
	}

	public void onResume() {
		super.onResume();
		isInFront = true;

		// get question set
		// HashMap<String, Object> params = new HashMap<String, Object>();
		//
		// params.put("user", ParseInstallation.getCurrentInstallation()
		// .getInstallationId());
		// ParseInstallation.getCurrentInstallation().addUnique(
		// "channels",
		// "id-"
		// + ParseInstallation.getCurrentInstallation()
		// .getInstallationId());
		// ParseInstallation.getCurrentInstallation().saveInBackground();
		// ParseCloud.callFunctionInBackground("get_questionset", params,
		// new FunctionCallback<ArrayList<String>>() {
		//
		// public void done(ArrayList<String> result, ParseException e) {
		// if (e == null) {
		// btnQuestion1.setText(result.get(0));
		// btnQuestion2.setText(result.get(1));
		// btnQuestion3.setText(result.get(2));
		// }
		// }
		//
		// });
		// ------
		this.updateQuestionSet();

	}

	private void updateQuestionSet() {

		// get question set
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put("user", ParseInstallation.getCurrentInstallation()
				.getInstallationId());
		ParseInstallation.getCurrentInstallation().addUnique(
				"channels",
				"id-"
						+ ParseInstallation.getCurrentInstallation()
								.getInstallationId());
		ParseInstallation.getCurrentInstallation().saveInBackground();
		ParseCloud.callFunctionInBackground("get_questionset", params,
				new FunctionCallback<ArrayList<String>>() {

					public void done(ArrayList<String> result, ParseException e) {
						if (e == null) {
							btnQuestion1.setText(result.get(0));
							btnQuestion2.setText(result.get(1));
							btnQuestion3.setText(result.get(2));
						}
					}

				});
		// ------
	}

	public void onPause() {
		super.onPause();
		isInFront = false;
	}

	private void startConversation() {
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put("user", ParseInstallation.getCurrentInstallation()
				.getInstallationId());
		ParseInstallation.getCurrentInstallation().addUnique(
				"channels",
				"id-"
						+ ParseInstallation.getCurrentInstallation()
								.getInstallationId());
		ParseInstallation.getCurrentInstallation().saveInBackground();
		ParseCloud.callFunctionInBackground("start_conversation", params,
				new FunctionCallback<String>() {
					public void done(String result, ParseException e) {
						if (e == null) {
							if (result.equalsIgnoreCase("waiting")) {
								txvWaiting.setVisibility(View.VISIBLE);
							} else {
								txvWaiting.setVisibility(View.GONE);
							}
						}
					}

				});
	}

	private View.OnClickListener onSendMessageClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			sendMessage();
			hideKeyboard();
			updateQuestionSet();
		}
	};

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.messageText.getWindowToken(), 0);
	}

	public void showWait(boolean isShowWait) {
		if (isShowWait) {
			this.txvWaiting.setVisibility(View.VISIBLE);
		} else {
			this.txvWaiting.setVisibility(View.GONE);
		}
	}

	private void sendMessage() {
		if (messageText != null) {
			Message messageString = new Message(messageText.getText()
					.toString());
			chatController.sendMessage(messageString);

			HashMap<String, Object> params = new HashMap<String, Object>();

			params.put("user", ParseInstallation.getCurrentInstallation()
					.getInstallationId());
			params.put("message", messageString.getContent());
			ParseCloud.callFunctionInBackground("send_message", params,
					new FunctionCallback<String>() {
						public void done(String result, ParseException e) {
							if (e == null) {
								// showMessage(ratings, false);
							}
						}

					});

			messageText.setText("");
			showMessage(messageString.getContent(), true);
		}
	}

	public void showMessage(String message, boolean leftSide) {
		final TextView textView = new TextView(ChatActivity.this);
		textView.setTextColor(Color.BLACK);
		textView.setText(message);

		int bgRes = R.drawable.left_message_bg;

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		if (!leftSide) {
			bgRes = R.drawable.right_message_bg;
			params.gravity = Gravity.RIGHT;
		}

		textView.setLayoutParams(params);

		textView.setBackgroundResource(bgRes);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				messagesContainer.addView(textView);

				// Scroll to bottom
				if (scrollContainer.getChildAt(0) != null) {
					scrollContainer.scrollTo(scrollContainer.getScrollX(),
							scrollContainer.getChildAt(0).getHeight());
				}
				scrollContainer.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int viewID = v.getId();
		switch (viewID) {
		case R.id.btnQues1:
		case R.id.btnQues2:
		case R.id.btnQues3:
			this.messageText.setText(((Button) v).getText());
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_activity_action, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_next:
			// TODO: next conversation here
			messagesContainer.removeAllViews();
			this.showWait(true);
			this.nextConversation();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void nextConversation() {
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put("user", ParseInstallation.getCurrentInstallation()
				.getInstallationId());
		ParseInstallation.getCurrentInstallation().addUnique(
				"channels",
				"id-"
						+ ParseInstallation.getCurrentInstallation()
								.getInstallationId());
		ParseInstallation.getCurrentInstallation().saveInBackground();
		ParseCloud.callFunctionInBackground("new_conversation", params,
				new FunctionCallback<String>() {
					public void done(String result, ParseException e) {
						if (e == null) {
							if (result.equalsIgnoreCase("waiting")) {
								txvWaiting.setVisibility(View.VISIBLE);
							} else {
								txvWaiting.setVisibility(View.GONE);
							}
						}
					}

				});
	}

}
