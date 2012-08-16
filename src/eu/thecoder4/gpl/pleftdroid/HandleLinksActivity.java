/*
 * Copyright (C) 2011, 2012 Riccardo Massera, r.massera@thecoder4.eu
 * 
 * This file is part of PleftDroid.
 * 
 * PleftDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PleftDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PleftDroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.thecoder4.gpl.pleftdroid;

import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author r.massera
 * 
 * This class is a transient activity whose purpose is to parse,
 * and validate the Pleft links.
 * Then it handles the link communicating with the Pleft server
 * and storing event details in the sqlite DB of the app.
 * If everything is ok, shows the appropriate activity.
 *
 */
public class HandleLinksActivity extends Activity {
	
	private String theurl;
	private int aid=0;
	private String pserver;
	private String user="";
	private String vcode="";
	// DB Adapter
	PleftDroidDbAdapter mDbAdapter;
	private Runnable handleLnk;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView tv = new TextView(this);
		
	    tv.setText("Verifying Link...");
	    setContentView(tv);


		Intent intent = getIntent();
		if (intent.getAction().equals("android.intent.action.VIEW")){ 
			try{ 
				Uri uri = getIntent().getData(); 
				theurl = uri.toString();
				
			}catch(Exception e){ 
				Toast.makeText(this, R.string.toast_couldnotopenurl, Toast.LENGTH_LONG).show(); 
			} 
		} else if (Intent.ACTION_SEND.equals(intent.getAction())) {

			Bundle extras = intent.getExtras();
			theurl =extras.getCharSequence(Intent.EXTRA_TEXT).toString();
		} else { theurl=""; }
		//Toast.makeText(this, "The URL  =  "+theurl, Toast.LENGTH_LONG).show();

		// Parse the URL
		// VERIFICATION: verify?id=&u=&p=
		// INVITATION: a?id=&u=&p=
		if(theurl.indexOf("?")<0 && theurl.indexOf("/verify?")<0 && theurl.indexOf("/a?")<0){
			Toast.makeText(this, R.string.toast_linknotsupp, Toast.LENGTH_LONG).show();
			finish();
		} else {
			Map<String, String> arr = PleftBroker.getParamsFromURL(theurl);
			
			pserver=arr.get("pserver");
			if(arr.get("id")!=null) {aid=Integer.parseInt(arr.get("id")); }
			else { aid=0; }
			vcode=arr.get("p");
			user=arr.get("u");
			if(aid==0 || vcode==null || user==null) {
				Toast.makeText(this, R.string.toast_shlinknotvalid, Toast.LENGTH_LONG).show();
				finish();
			} else { // we have a valid Link
				
				handleLnk = new Runnable(){
		            @Override
		            public void run() {
		            	handleLink();
		            }
		        };
		        Thread thread =  new Thread(null, handleLnk, "Handlethrd");
		        thread.start();
			} // End - if Link is Valid
		}
	}

	/**
	 * Handles the link
	 */
	@SuppressWarnings("static-access")
	private void handleLink() {
		// If we have a valid Link, we'll need a net connection
		if( ! PleftBroker.INSTANCE.isInetConnAvailable(this) ) {

			this.runOnUiThread(new Runnable(){

				@Override
				public void run(){
					new AlertDialog.Builder(HandleLinksActivity.this)
					.setTitle(R.string.dialog_title_nonet)
					.setMessage(R.string.dialog_msg_nonet)
					.setPositiveButton("OK", new OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							// End activity
							finish();
						}
					}).show();
				}
			});

		} else { //If net connectivity
			Log.i("HL", "aid="+aid+"\nThe URL  =  "+theurl+"\naid="+aid+"\nuser="+user+"\nvcode="+vcode);
			mDbAdapter = new PleftDroidDbAdapter(this);
			mDbAdapter.open();
			// If the Pleft id is in our DB, we are either the Invitor
			// that has yet verified the e-mail
			// or is using the Overview link
			// OR we have yet handled the Invitee URL,
			// so we just need to show the EventDetail Activity
			// check DB for AID
			if(mDbAdapter.getId(aid)>0) { // Aid is in DB
				if(mDbAdapter.getRole(aid).equals(PleftDroidDbAdapter.R_INVITOR)) {
					mDbAdapter.setStatusVerified(aid, vcode);
				}
				
				mDbAdapter.close();
				Log.i("HL","In DB aid="+aid);
				//and go to Detail activity
				goEventDetail(aid, null);
			} else {

				// If it is a verify URL, we do verification on Pleft Server
				// (which triggers invitations to be sent)
				// then we go to PleftDroid Activity
				if(theurl.indexOf("/verify?")>0) {
					//Check that we have not yet verified this URL
					Log.i("HL","VERIFY");
					Log.i("HL","aid="+aid);
					//Toast.makeText(this, "The URL  =  "+theurl+"\nAID="+aid+"\nuser="+user+"\nvcode="+vcode, Toast.LENGTH_LONG).show();

					//Actually do the verification
					int SC=PleftBroker.INSTANCE.doVerification(theurl);
					
					//Verification is done, we change Status to verified
					//change Status to verified
					if(SC==HttpStatus.SC_OK) { mDbAdapter.setStatusVerified(aid, vcode); }
					mDbAdapter.close();
					//and go to Detail activity
					goEventDetail(aid, null);

				} else {
					if(mDbAdapter.getId(0)>0) { // Aid 0 is in DB
						if(mDbAdapter.getRole(0).equals(PleftDroidDbAdapter.R_INVITOR)) { // and Role is Invitor
							// => Already verified on server, user clicked on Overview link
							mDbAdapter.setStatusVerified(aid, vcode);
							mDbAdapter.close();
							//and go to Detail activity
							goEventDetail(aid, null);
						}
					} else {
					// Else we are only one Invitee,
					// so we have to:
					// - create the event in DB with role Invitee
					// - start the Activity to vote the dates

					mDbAdapter.createAppointmentAsInvitee(aid, getString(R.string.label_newappt), pserver, user, vcode);
					mDbAdapter.close();
					
					goEventDetail(aid, theurl);

					}
				}
			}

			finish();
		} // if net connectivity
	}
	
	private void goEventDetail(int vaid, String vurl) {
		Intent i = new Intent(this, EventDetailActivity.class);
		i.putExtra(EventDetailActivity.AID, vaid);
		if(vurl!=null) { i.putExtra(EventDetailActivity.THEURL, vurl); }
		i.putExtra(EventDetailActivity.FROMHL, "Y");
		startActivity(i);
	}

}
