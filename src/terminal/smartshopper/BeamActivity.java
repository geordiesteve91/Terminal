package terminal.smartshopper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

public class BeamActivity extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	NfcAdapter mNfcAdapter;
	TextView recieved;

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push);

		recieved = (TextView) findViewById(R.id.HelloTag);

		recieved.setText("Scan to checkout");

		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "Sorry, NFC is not available on this device",
					Toast.LENGTH_SHORT).show();
		} else {
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
	}

	private static final String MIME_TYPE = "application/terminal.smartshopper";
	private static final String PACKAGE_NAME = "terminal.smartshopper";

	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		String text = "";
		NdefMessage msg = new NdefMessage(new NdefRecord[] {
				NfcUtils.createRecord(MIME_TYPE, text.getBytes()),
				NdefRecord.createApplicationRecord(PACKAGE_NAME) });
		return msg;
	}
	@Override
	public void onBackPressed() {
	}

	private static final int MESSAGE_SENT = 1;

	/** This handler receives a message from onNdefPushComplete */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "Message sent!",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	/**
	 * Implementation for the OnNdefPushCompleteCallback interface
	 */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}

	/**
	 * Parses the NDEF Message from the intent and toast to the user
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// in this context, only one message was sent over beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		String payload = new String(msg.getRecords()[0].getPayload());
		recieved.setText("Checkout in progress");
		
		CharSequence signature="piCpVqeI1zKkpXkh6VNulMOoxikuL/wuIKRttYs9YrA=";
		
		if(recieved.getText()=="Checkout in progress"&&payload.contains(signature)){
			System.out.println("Called Async task!");
			
		new MyAsyncTask().execute(payload);
		}
		else
		{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					BeamActivity.this);
			System.out.println("Incorrect basket signature");
			alertDialogBuilder.setTitle("Basket cannot be read");
			alertDialogBuilder.setMessage("Unsigned basket,your basket doesn't" +
					"have a valid issuing signature," +
					"please ask a member of staff for assistance.");
			// set positive button: Yes message
			alertDialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							
						}
					});
			

			AlertDialog alertDialog = alertDialogBuilder.create();
			// show alert
			alertDialog.show();
			System.out.println("Invalid basket");
		}
	}

	private class MyAsyncTask extends AsyncTask<String, Integer, Double> {

		@Override
		protected Double doInBackground(String... params) {

			postData(params[0]);
			return null;
		}

		protected void onPostExecute(Double result) {
			if (recieved.getText() == "Checkout in progress") {
				recieved.setText("Checkout complete");

				Toast.makeText(getApplicationContext(), "Successfully sent",
						Toast.LENGTH_LONG).show();
				Thread timer = new Thread() {
					public void run() {
						try {
							sleep(5000);
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							
							Intent go = new Intent(BeamActivity.this, Completed.class);
							startActivity(go);	
							
							
							
						}
					}
				};
				timer.start();
			}

		}
	}

	public void postData(String valueIWantToSend) {
		System.out.println(valueIWantToSend);
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://homepages.cs.ncl.ac.uk/s.c.g.campbell/smartshopper/map/trans.php");
		

		try {

			int i = valueIWantToSend.indexOf(',',
					1 + valueIWantToSend.indexOf(','));

			String emailAndBasket = valueIWantToSend.substring(0, i);

			String items = valueIWantToSend.substring(i + 1);

			System.out.println("Email " + emailAndBasket);
			String[] splited = emailAndBasket.split("\\s*,\\s*");
			System.out.println("Splited " + splited[0]);

			System.out.println("Splited " + splited[1]);

			System.out.println("Something " + items);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", splited[0]));
			nameValuePairs.add(new BasicNameValuePair("numItems", splited[1]));
			// nameValuePairs.add(new
			// BasicNameValuePair("listItems",items));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request and get response from the server
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = httpclient.execute(httppost, responseHandler);
			// String response = httpclient.execute(httppost);
			System.out.println("Response is" + response);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

}
