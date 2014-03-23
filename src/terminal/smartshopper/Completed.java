package terminal.smartshopper;



import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

public class Completed extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	NfcAdapter mNfcAdapter;
	TextView recieved;

	// TextView customername;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push);
		recieved=(TextView) findViewById(R.id.textView1);
		recieved.setText("Scan your device to confirm checkout!");
		
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

	private static final String MIME_TYPE = "application/smartshopper.menu";
	private static final String PACKAGE_NAME = "smartshopper.menu";

	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		String text = "Checkout successful";
		NdefMessage msg = new NdefMessage(new NdefRecord[] {
				NfcUtils.createRecord(MIME_TYPE, text.getBytes()),
				NdefRecord.createApplicationRecord(PACKAGE_NAME) });
		return msg;
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
				
				if(recieved.getText()!=null){
					Thread timer = new Thread() {
						public void run() {
							try {
								sleep(7500);
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							} finally {
								System.out.println("Time up");
								Intent go = new Intent(Completed.this,BeamActivity.class);
								startActivity(go);
							}
						}
					};
				timer.start();
				}
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
		
	}

	

}
