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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import eu.thecoder4.gpl.pleftdroid.json.ADate;
import eu.thecoder4.gpl.pleftdroid.json.APerson;
import eu.thecoder4.gpl.pleftdroid.json.EventOverview;

/**
 * @author r.massera
 * 
 * This is the most complicated Class that handles the whole process
 * of showing the Event detail with the dates and availabilities for
 * all participants and allows the user to vote.
 *
 */
public class EventDetailActivity extends Activity implements OnItemClickListener {
	//Activities Constants
	public static final int ACT_COMMENT = 1;
	public static final String AR_COMMENT="COMMENT";
	public static final String AR_DTID="DATEID";
	protected static final int ACT_ADDINVITEE=2;
	public static final String AR_EMAIL="THEEMAIL";
	protected static final int ACT_NEWDATE=3;
	public static final String AR_DATE="THEDATE";
	public static final String AR_TIME="THETIME";
	
	//Menu Constants
	private static final int RESENDINV_ID = Menu.FIRST;
	private static final int INVANOTHER_ID = Menu.FIRST+1;
	private static final int PROPOSEDATE_ID = Menu.FIRST+2;
	private static final int DONE_ID = Menu.FIRST+3;
	
	protected static final String AID = "AID";
	protected static final String THEURL = "THEURL";
	protected static final String JRESULT = "JRESULT";
	protected static final String FROMHL = "FROMHL";
	private int aid;
	private ArrayList<ADate> adates;
	private ArrayList<APerson> apeople;
	private Map<String, Map<String, ArrayList<String>>> avails;
	private ListView lvp;
	private ListView lvd;
	private int invitee;
	private int curpid;
	private int curppos=0;
	private ADateAdapter adateadapter;
	private VoteADateAdapter voteadateadapter;
	private APersonAdapter apeopleadapter;
	PleftDroidDbAdapter mDbAdapter;
	
	private boolean isAvailMod=false;
	private String adesc;

	private String arole;
	private String apserver;
	private String auser;
	private String avcode;
	private ProgressDialog mProgressDialog;
	private String theurl;
	private Runnable initEvent;
	private boolean initOK=true;
	private boolean back2main=false;
	private boolean canproposedates=false;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if(AppPreferences.INSTANCE.getUsePleftTheme()) { setTheme(R.style.Theme_Pleft); }

    	super.onCreate(savedInstanceState);

    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	initOK=true;

    	Bundle extras = getIntent().getExtras();
    	aid=extras.getInt(AID);
    	theurl = extras.getString(THEURL);

    	if(extras.getString(FROMHL)!=null) { back2main=true; }
    	
    	setContentView(R.layout.event_detail);
    	
    	// Initialize Adapters and ListViews
    	lvd = (ListView) findViewById(R.id.pdateslv);
    	adates= new ArrayList<ADate>();
        adateadapter = new ADateAdapter(this,R.layout.detail_invitee_rowc,adates);
        voteadateadapter = new VoteADateAdapter(this,R.layout.detail_voting_rowdbc,adates);
        lvd.setAdapter(voteadateadapter);
        
        lvp = (ListView) findViewById(R.id.peoplelv);
        apeople = new ArrayList<APerson>();
        apeopleadapter=new APersonAdapter(this,R.layout.detail_ppl_row,apeople);
        lvp.setAdapter(apeopleadapter);
        
        lvp.setOnItemClickListener(this);
        
        lvp.setSelection(curppos); lvp.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
    	initEvent = new Runnable(){
            @Override
            public void run() {
    	initializeEventDetail(theurl);
            }
        };
        Thread thread =  new Thread(null, initEvent, "Initializer");
        thread.start();
        
        mProgressDialog = ProgressDialog.show(this, this.getString(R.string.dialog_title_pleasewaitdetails),
        		                              this.getString(R.string.dialog_msg_pleasewaitdetails), true);
        
        //Toast.makeText(this, ev.toString(), Toast.LENGTH_LONG).show();

    	
    }
    
    private Runnable endInit = new Runnable() {

        @Override
        public void run() {
            mProgressDialog.dismiss();
            if(initOK) {
            	adateadapter.notifyDataSetChanged();
            	voteadateadapter.notifyDataSetChanged();
            	apeopleadapter.notifyDataSetChanged();

            	EventDetailActivity.this.setTitle(adesc);
            } else {
            	Toast.makeText(EventDetailActivity.this, R.string.toast_problemdetails, Toast.LENGTH_LONG).show();
            	finish();
            }
        }
    };

	/**
	 * @param theurl
	 */
	@SuppressWarnings("static-access")
	protected void initializeEventDetail(String theurl) {
		mDbAdapter = new PleftDroidDbAdapter(this);
        mDbAdapter.open();
    	
    	String json=null; boolean updateDesc;
    	// THEURL will be null when called from Main Activity
    	if(theurl==null) {
    		updateDesc=false;
    		// get pserver,user,vcode for this aid from DB
    		Cursor adc=mDbAdapter.fetchAppointmentDetails(aid);
    		while (adc.moveToNext()) {
    			adesc = adc.getString(adc.getColumnIndex(PleftDroidDbAdapter.COL_DESC));
    			arole = adc.getString(adc.getColumnIndex(PleftDroidDbAdapter.COL_ROLE));
        		apserver = adc.getString(adc.getColumnIndex(PleftDroidDbAdapter.COL_PSERVER));
        		auser = adc.getString(adc.getColumnIndex(PleftDroidDbAdapter.COL_USER));
        		avcode = adc.getString(adc.getColumnIndex(PleftDroidDbAdapter.COL_VCODE));
        	}
    		adc.close();
    		if(adesc==null || adesc.startsWith( getString(R.string.label_newappt) )) updateDesc=true;
    		if(adesc==null || adesc.startsWith("New Appointment:")) updateDesc=true;
    		json=PleftBroker.INSTANCE.getJSONforAppointment(aid,apserver,auser,avcode);
    		
    	} else { // From Handle Links
    		updateDesc=true;

            Map<String, String> arr = PleftBroker.getParamsFromURL(theurl);
			
			apserver=arr.get("pserver");
			if(arr.get("id")!=null) {aid=Integer.parseInt(arr.get("id")); }
			else { aid=0; }
			avcode=arr.get("p");
			auser=arr.get("u");
			json=PleftBroker.INSTANCE.getJSONforAppointment(aid,theurl);
			Log.i("EVDT:","JSON="+json);
    		
    	}
    	//Toast.makeText(this, "ADesc: "+adesc+"\nupdDesc="+updateDesc, Toast.LENGTH_LONG).show();
    	
    	Gson gson = new Gson();
        EventOverview ev=null;
        try {
			ev = gson.fromJson(json, EventOverview.class);
		} catch (Exception e) {
			Log.i("EVDT:",e.toString());
			ev=null;
			initOK=false;
		}
        if(ev!=null) {
        	invitee=ev.getInvitee(); // The invitee param Actually indicates the Invitor!!!
        	curpid=invitee;
        	avails=ev.getAvailability();

        	for (ADate ad : ev.getDates()) {
        		adates.add(ad);
        	}

        	if(updateDesc) {
        		adesc=ev.getMeta().getTitle();
        		mDbAdapter.updateDesc(aid, adesc);
        	}
        	// Initialize Adapters and ListViews

        	// Now we search the Invitor and we put it in the front to have it in the first Tab
        	for(APerson ap: ev.getPeople()) {
        		apeople.add(ap);
        	}
        	int i=0;
        	for(APerson ap: apeople) {
        		if(ap.getId()==invitee) break;
        		i++;
        	}
        	APerson a = apeople.remove(i);
        	apeople.add(0, a);
        	
        	// And we initialize availabilities for people who did not yet vote
        	for(APerson ap: apeople) {
        		String pids=Integer.toString(ap.getId());
        		for(ADate ad: adates) {
        			String dids=Integer.toString(ad.getId());
        			Map<String, ArrayList<String>> p = avails.get(pids);
        	    	if(p==null) {
        	    		p= new HashMap<String, ArrayList<String>>();
        	    		avails.put(pids, p);
        	    	}
        	    	if(p.get(dids)==null) {
        	    		ArrayList<String> al = new ArrayList<String>();
        	    		al.add(new String("0")); al.add(new String(""));
        	    		p.put(dids, al);
        	    		
        	    	}
        		}
        	}
        	
        	canproposedates=ev.getMeta().isProposeMore();
        } else {
        	initOK=false;
        }
        mDbAdapter.close();

        runOnUiThread(endInit);
	}
    
    /**
	 * @see android.app.Activity#onStop()
	 * 
	 * If the user goes back or selects a new activity we commit his changes
	 */
	@Override
	protected void onStop() {
		commitAvailabilities();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if(arole==null) arole=PleftDroidDbAdapter.R_INVITEE;
		if(arole.equals(PleftDroidDbAdapter.R_INVITOR)) {
			menu.add(0, RESENDINV_ID,0, R.string.menu_resendinv).setIcon(android.R.drawable.ic_menu_send);
			menu.add(0, INVANOTHER_ID,0, R.string.menu_invanother).setIcon(R.drawable.ic_menu_invite);
		}
		if(arole.equals(PleftDroidDbAdapter.R_INVITOR) || canproposedates) { //if Invitor or can propose dates is set
			menu.add(0, PROPOSEDATE_ID,0, R.string.menu_proposedate).setIcon(android.R.drawable.ic_menu_today);
		}
		menu.add(0, DONE_ID,0, R.string.menu_done).setIcon(android.R.drawable.ic_menu_save);
        return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if(arole.equals(PleftDroidDbAdapter.R_INVITOR)) {
    		menu.findItem(RESENDINV_ID).setVisible(curpid==invitee ? false : true);
    	}
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	int SC;
		switch (item.getItemId()) {
		case RESENDINV_ID:
			SC=PleftBroker.resendInvitation(aid, curpid, apserver, auser, avcode);
			if(SC>=HttpStatus.SC_BAD_REQUEST) { //400
				Toast.makeText(this, R.string.toast_problemrequest, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, R.string.toast_contactreinvited, Toast.LENGTH_LONG).show();
			}
        	return true;
		case INVANOTHER_ID:
			Intent iai = new Intent(this, SelectContactsActivity.class);
			iai.putExtra(SelectContactsActivity.ASK_CONFIRM, true);
        	startActivityForResult(iai, ACT_ADDINVITEE);
        	return true;
		case PROPOSEDATE_ID:
			Intent iad = new Intent(this, PickSingleDateDialogActivity.class);
        	startActivityForResult(iad, ACT_NEWDATE);
        	return true;
        case DONE_ID:
	        commitAvailabilities();
	        if(back2main) {goMainActivity();}
        	finish();
        	return true;
        }
		return super.onOptionsItemSelected(item);
	}
    /**
     * 
     * @author rmassera
     *
     */
    public class CommitTask extends AsyncTask<Void, Void, Void> {

    	public CommitTask() {
    	}

    	public void onPreExecute() {
    	}

    	@Override
    	protected Void doInBackground(Void... unused) {
    		int SC;
    		String avs="";
    		for(ADate ad: adates) {
    			avs+=ad.getId()+":"
    			+EventDetailActivity.this.getAvailForPerson(invitee, ad.getId())
    			+":";
    			if(EventDetailActivity.this.getAvailCommentForPerson(invitee, ad.getId())==null) {
    				avs+="\n";
    			} else {
    				avs+=EventDetailActivity.this.getAvailCommentForPerson(invitee, ad.getId())+"\n";
    			}
    		}
    		//*Toast.makeText(this, "SetAvails:\n"+avs, Toast.LENGTH_LONG).show();
    		SC=PleftBroker.setAvailability(avs, aid, apserver, auser, avcode);
    		if(SC==HttpStatus.SC_OK) { //200
    			mDbAdapter.open();
    			mDbAdapter.setStatusVoted(aid);
    			mDbAdapter.close();
    		}
    		return null;
    	}

    	public void onPostExecute(Void unused) {
    	}

    }

	/**
	 * Commits the Availabilities
	 */
	protected void commitAvailabilities() {
		//int SC;
		if(isAvailMod==true) {
			Toast.makeText(this, R.string.toast_apptsaved, Toast.LENGTH_LONG).show();
			new CommitTask().execute();
		}
	}
	
    private void goMainActivity() {
		Intent ipd = new Intent(this, PleftDroidActivity.class);
        ipd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(ipd);
	}
    
    private class APersonAdapter extends ArrayAdapter<APerson> {

        private ArrayList<APerson> items;

        public APersonAdapter(Context context, int textViewResourceId, ArrayList<APerson> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.detail_ppl_row, null);
                }
                APerson ap = items.get(position);
                if (ap != null) {
                        TextView tt = (TextView) v.findViewById(R.id.nickname);
                        if (tt != null) {
                        	tt.setText(ap.getName());
                        	//tt.setTag(""+position);
                        	if(position==curppos){
                        		tt.setBackgroundColor(0xFF33CCFF);//0xFFFF8C00);
                        		tt.setTextColor(0xFF002050);//0xFF000060);
                        	} else {
                        		tt.setBackgroundColor(0xFF001840);//0xFF000040);
                        		tt.setTextColor(0xFF33CCFF);//0xFFCDCDCD);
                        	}
                        }
                }
                
                return v;
        }
        
    }
    
    private class ADateAdapter extends ArrayAdapter<ADate> {

        private ArrayList<ADate> items;

        public ADateAdapter(Context context, int textViewResourceId, ArrayList<ADate> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.detail_invitee_rowc, null);
                }
                
                ADate ad = items.get(position);
                if (ad != null) {
                        TextView tt = (TextView) v.findViewById(R.id.adate);
                        if (tt != null) {
                        	if(AppPreferences.INSTANCE.getUsePleftTheme()) tt.setTextColor(Color.DKGRAY);
                        	tt.setText(ad.getS());
                        }
                }
                String av=getAvailForPerson(curpid,ad.getId());
                ImageView icon = (ImageView) v.findViewById(R.id.avail);
                // 0=? 1=OK -1=KO
                if(av.equals("-1")) {
                	icon.setImageResource(R.drawable.fdn);
                } else if(av.equals("1")) {
                	icon.setImageResource(R.drawable.fup);
                } else {
                	icon.setImageResource(R.drawable.qm);
                }
                String comment=getAvailCommentForPerson(curpid,ad.getId());
                if(comment==null || comment.length()<1) {
                	((ImageView) v.findViewById(R.id.cmnticon)).setVisibility(View.INVISIBLE);
                	((TextView) v.findViewById(R.id.dcomment)).setVisibility(View.INVISIBLE);
                } else {
                	((ImageView) v.findViewById(R.id.cmnticon)).setVisibility(View.VISIBLE);
                	TextView dc = (TextView) v.findViewById(R.id.dcomment);
                	dc.setVisibility(View.VISIBLE);
                	dc.setText(comment);
                	
                }
                return v;
        }
        
    }
    
    private class VoteADateAdapter extends ArrayAdapter<ADate> {

        private ArrayList<ADate> items;
		private OnClickListener c;
		private OnLongClickListener ac;
		private OnClickListener acom;

        public VoteADateAdapter(Context context, int textViewResourceId, ArrayList<ADate> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.detail_voting_rowdbc, null);
                }
                
                ac = new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						addToCalendar(((Integer)v.getTag()).intValue());
						return true;
					}
                    
                };
                acom = new OnClickListener() {

					@Override
					public void onClick(View v) {
						addComment(((Integer)v.getTag()).intValue());
					}
                    
                };
                
                ADate ad = items.get(position);
                if (ad != null) {
                		DateVoteBarView dvb = (DateVoteBarView) v.findViewById(R.id.vadate);
                		if (dvb != null) {
                        	dvb.setText(ad.getS());
                        	dvb.setTag(new Integer(position));
                        	dvb.setOnLongClickListener(ac);
                        }
                		ArrayList<String> davs = getAvailsForDate(ad.getId());
                		dvb.setmVotes(davs);
                        
                        ImageView icomm= (ImageView) v.findViewById(R.id.addcommentico);
                        if (icomm!=null) {
                        	icomm.setTag(new Integer(position));
                        	icomm.setOnClickListener(acom);
                        }
                }
                //TODO unify get avail and get availcomment
                String comment=getAvailCommentForPerson(curpid,ad.getId());
                if(comment!=null && comment.length()>=0) {
                	TextView dc = (TextView) v.findViewById(R.id.dcomment);
                	dc.setText(comment);
                }
                //TODO unify get avail and get availcomment
                String av=getAvailForPerson(curpid,ad.getId());
                ToggleButton butok = (ToggleButton) v.findViewById(R.id.isok);
                butok.setTag(new Integer(position));
                ToggleButton butko = (ToggleButton) v.findViewById(R.id.isko);
                butko.setTag(new Integer(position));
                ToggleButton butqm = (ToggleButton) v.findViewById(R.id.maybe);
                butqm.setTag(new Integer(position));
                // 0=? 1=OK -1=KO
                if(av.equals("-1")) {
                	butok.setChecked(false);
                	butko.setChecked(true);
                	butqm.setChecked(false);
                } else if(av.equals("1")) {
                	butok.setChecked(true);
                	butko.setChecked(false);
                	butqm.setChecked(false);
                } else {
                	butok.setChecked(false);
                	butko.setChecked(false);
                	butqm.setChecked(true);
                }
                c = new OnClickListener() {
                    public void onClick(View bv) {
                    	LinearLayout v=(LinearLayout) bv.getParent();
                    	ToggleButton butok = (ToggleButton) v.findViewById(R.id.isok);
                    	ToggleButton butko = (ToggleButton) v.findViewById(R.id.isko);
                    	ToggleButton butqm = (ToggleButton) v.findViewById(R.id.maybe);
                    	
                    	ToggleButton tb = (ToggleButton) bv;
                    	Integer butpos = (Integer) tb.getTag();
                    	// Perform action on clicks
                        if (tb.isChecked()) {
                        	if (bv.getId()==R.id.isok) {
                        		butok.setChecked(true);
                        		butko.setChecked(false);
                            	butqm.setChecked(false);
                            	updateAvailForPerson(curpid, adates.get(butpos).getId(), "1");

                            	voteadateadapter.notifyDataSetChanged();
                        	} else if (bv.getId()==R.id.isko) {
                        		butko.setChecked(true);
                        		butok.setChecked(false);
                            	butqm.setChecked(false);
                            	updateAvailForPerson(curpid, adates.get(butpos).getId(), "-1");
                            	voteadateadapter.notifyDataSetChanged();
                        	} else { //qm
                        		butqm.setChecked(true);
                        		butok.setChecked(false);
                        		butko.setChecked(false);
                        		updateAvailForPerson(curpid, adates.get(butpos).getId(), "0");
                        		voteadateadapter.notifyDataSetChanged();
                        	}
                            //*Toast.makeText(EventDetailActivity.this, "Checked,Position:"+butpos, Toast.LENGTH_SHORT).show();
                        } else {
                        	if (bv.getId()==R.id.isok) {
                        		butok.setChecked(true);
                        	} else if (bv.getId()==R.id.isko) {
                        		butko.setChecked(true);
                        	} else { //qm
                        		butqm.setChecked(true);
                        	}
                            //*Toast.makeText(EventDetailActivity.this, "Not checked,Position:"+butpos, Toast.LENGTH_SHORT).show();
                        }
                        
                    }
                };
                butok.setOnClickListener(c);
                butko.setOnClickListener(c);
                butqm.setOnClickListener(c);
                
                return v;
        }
        
    }

    private ArrayList<String> getAvailsForDate(int dtid) {
    	ArrayList<String> avs = new ArrayList<String>();
    	ArrayList<String> ta;
    	for(APerson ap: apeople) {
    		ta = avails.get(Integer.toString(ap.getId())).get(Integer.toString(dtid));
    		if(ta==null) { avs.add("0"); }
    		else         { avs.add(ta.get(0)); }
    	}
		return avs;
	}

	public String getAvailForPerson(int pid, int dateid) {
    	return avails.get(Integer.toString(pid)).get(Integer.toString(dateid)).get(0);
    }
	public void updateAvailForPerson(int pid, int dateid, String avail) {
		//Log.d//("EVDT:uAFP:", " pid="+pid+", dateid="+dateid+", avail="+avail);
		avails.get(Integer.toString(pid)).get(Integer.toString(dateid)).set(0, avail);
		isAvailMod=true;
	}
	public String getAvailCommentForPerson(int pid, int dateid) {
    	
    	return avails.get(Integer.toString(pid)).get(Integer.toString(dateid)).get(1);
    }
	public void updateAvailCommentForPerson(int pid, int dateid, String comment) {
		//Log.d//("EVDT:uAC FP:", " pid="+pid+", dateid="+dateid+", comment="+comment);
		avails.get(Integer.toString(pid)).get(Integer.toString(dateid)).set(1, comment);
		
		isAvailMod=true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		curppos=position;
		
		curpid=apeople.get(position).getId();
		if(curpid==invitee) { // The invitee param Actually indicates the Invitor!!!
			lvd.setAdapter(voteadateadapter);
			voteadateadapter.notifyDataSetChanged();
		} else {
			lvd.setAdapter(adateadapter);
			adateadapter.notifyDataSetChanged();
		}
		
	}
	public void addComment(int i) {
		Intent iacom = new Intent(this, EditCommentActivity.class);
		iacom.putExtra(AR_DTID, i);
    	startActivityForResult(iacom, ACT_COMMENT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data!=null) {
			Bundle extras = data.getExtras();
			int SC;

			switch(requestCode) {
			case ACT_COMMENT:
				String comment=extras.getString(AR_COMMENT);
				int datepos=extras.getInt(AR_DTID);
				getAvailForPerson(curpid,adates.get(datepos).getId());
				updateAvailCommentForPerson(curpid, adates.get(datepos).getId(), comment);
				voteadateadapter.notifyDataSetChanged();
				break;
			case ACT_ADDINVITEE:
				String participant=extras.getString(AR_EMAIL);
				SC=PleftBroker.inviteAnotherParticipant(aid, participant, apserver, auser, avcode);
				if(SC>=HttpStatus.SC_BAD_REQUEST) { //400
					Toast.makeText(this, R.string.toast_problemrequest, Toast.LENGTH_LONG).show();
				} else {
					startActivity(getIntent()); finish();
				}
				break;
			case ACT_NEWDATE:
				int[] thedate=extras.getIntArray(AR_DATE);
				int[] thetime=extras.getIntArray(AR_TIME);
				PDate td = new PDate(thedate, thetime);
				String newdate=td.getPleftDate();
				//TODO Check if this date and time is the same of one of the current dates!!!
				SC=PleftBroker.proposeNewDate(aid, newdate, apserver, auser, avcode);
				if(SC>=HttpStatus.SC_BAD_REQUEST) { //400
					Toast.makeText(this, R.string.toast_problemrequest, Toast.LENGTH_LONG).show();
				} else {
					startActivity(getIntent()); finish();
				}
				break;
			}
		}
	}
	
	public void addToCalendar(int i) {
		ADate ad = adates.get(i);
		String pd = ad.getD();
		String dpart = pd.substring(0, pd.indexOf('T'));
		String tpart = pd.substring(pd.indexOf('T')+1);
		SimpleDateFormat sdf =
	          new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		Date d=null;
		try {
			d = sdf.parse(dpart+" "+tpart);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.setTime(d);

		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("beginTime", cal.getTimeInMillis());
		intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
		intent.putExtra("title", adesc);
		startActivity(intent);
	}
    

}
