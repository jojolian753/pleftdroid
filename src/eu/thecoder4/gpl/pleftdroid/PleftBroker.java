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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

/**
 * @author r.massera
 * 
 * Singleton that performs all interactions with the Pleft server
 * Implemented using the thread-safe Enum technique.
 */
public enum PleftBroker {
	INSTANCE;
	
	protected static final String HTTP_PROTO = "http://";
	
	protected static final String REQ_CREATE = "/create";

	protected static final String REQ_SET_AVAILABILITY = "/set-availability";
	protected static final String REQ_RESENDINV = "/resend-invitation";
	protected static final String REQ_INVANOTHER ="/add-invitees";
	protected static final String REQ_PROPOSEDATE ="/add-dates";
	protected static final String REQ_SETLANG ="/i18n/setlang";
	protected static final String[] supp_langs = new String [] { "de" , "en", "es", "fr" , "it", "nl" };

	private static final int CONN_TIMEOUT = 5000;
	private static final int SO_TIMEOUT = 5000;
	
	public static final int SC_TIMEOUT = 99990;
	public static final int SC_NOPLEFTCOOKIE = 99997;
	public static final int SC_CLIREDIR = 99998;
	public static final int SC_NOTPLEFTSERV = 99999;
	
	/**
	 * @param desc
	 * @param invitees
	 * @param dates
	 * @param pserver
	 * @param uname
	 * @param uemail
	 */
	static protected int createAppointment(String desc, String invitees,
			String dates, String pserver, String uname, String uemail, boolean proposemore) {
		int SC = SC_CLIREDIR;
		
		if(checkServer(pserver)==HttpStatus.SC_OK) {
			SC = HttpStatus.SC_INTERNAL_SERVER_ERROR;
			HttpClient client = getDefaultClient();

			HttpPost request = new HttpPost(pserver + REQ_CREATE);
			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(6);
			postParameters.add(new BasicNameValuePair("description", desc));
			postParameters.add(new BasicNameValuePair("name", uname));
			postParameters.add(new BasicNameValuePair("email", uemail));
			postParameters.add(new BasicNameValuePair("invitees", invitees));
			postParameters.add(new BasicNameValuePair("dates", dates));
			postParameters.add(new BasicNameValuePair("propose_more",
					(proposemore ? "1" : "0")));
			try {
				request.setEntity(new UrlEncodedFormEntity(postParameters));
				Log.i("PD d", desc);
				Log.i("PD d", uname);
				Log.i("PD d", uemail);
				Log.i("PD d", invitees);
				Log.i("PD d", dates);

				Log.i("PD", request.getURI().toString());
				Log.i("PD", request.getEntity().toString());
				HttpResponse response = client.execute(request);
				SC = response.getStatusLine().getStatusCode();
				Log.i("PD sc", " " + SC);

				response.getEntity().getContent().close(); //You need to open and close the IS to release the connection
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return SC;
        
	}
	
	/**
	 * 
	 * @param aclient
	 * @param aid
	 * @param pserver
	 * @param user
	 * @param vcode
	 * @return
	 */
	static protected int doAuthnForAppointment(HttpClient aclient, int aid, String pserver, String user, String vcode) {
		String aurl = pserver+"/a?id="+aid+"&u="+user+"&p="+vcode;
		
		return doAuthnForAppointment(aclient, aurl);
	}
	
	/**
	 * 
	 */
	static protected int doAuthnForAppointment(HttpClient aclient, String aurl) {
		int SC = SC_CLIREDIR;
		String pserver = aurl.substring(0,aurl.lastIndexOf("/"));
		if(checkServer(pserver)==HttpStatus.SC_OK) {
			
			SC=HttpStatus.SC_INTERNAL_SERVER_ERROR;
			Log.i("PB aurl"," "+aurl);
			//First request is for AUTHN
			HttpGet request = new HttpGet(aurl);
			HttpResponse response;
			try {
				response = aclient.execute(request);
				SC=response.getStatusLine().getStatusCode();
				Log.i("PB sc1"," "+SC);
				
				response.getEntity().getContent().close(); //You need to open and close the IS to release the connection for next GET!!!
				return SC;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return SC;
	}
	
	static protected String getJSONforAppointment(int aid, String pserver, String user, String vcode) {
		String aurl = pserver+"/a?id="+aid+"&u="+user+"&p="+vcode;
		
		return getJSONforAppointment(aid, aurl);
	}

	/**
	 * 
	 */
	static protected String getJSONforAppointment(int aid, String jurl) {

		String jresult=null;
		Log.i("PB jurl"," "+jurl);
		HttpClient client = getDefaultClient();

		//First request is for AUTHN
		try {

			HttpResponse response;
			int SC=doAuthnForAppointment(client,jurl);
			if(SC==HttpStatus.SC_OK) { //200
				String pserver = jurl.substring(0,jurl.lastIndexOf("/"));

				//Second request for Data
				HttpGet req2 = new HttpGet(pserver+"/data?id="+aid);
				// Set language to the locale 
				req2.addHeader("Accept-Language", getLanguage_i18n());
				Log.i("PB getjson"," "+pserver+"/data?id="+aid);
				response = client.execute(req2);
				SC=response.getStatusLine().getStatusCode();
				Log.i("PB sc2"," "+SC);
				if(SC==HttpStatus.SC_OK) { //200
					jresult = EntityUtils.toString(response.getEntity());
					return jresult;
					//Toast.makeText(this, result, Toast.LENGTH_LONG).show();

				} else { return null; }
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}

	/**
	 * @param response
	 * @return the response as string
	 * @throws IOException
	 */
	protected static String getResponseAsString(HttpResponse response)
			throws IOException {
		String sresult;
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer sb = new StringBuffer("");
		String line = "";
		String NL = System.getProperty("line.separator");
		while ((line = in.readLine()) != null) {
			sb.append(line + NL);
		}
		in.close();
		sresult = sb.toString();
		
		return sresult;
	}
	
	/**
	 * 
	 */
	static protected int setAvailability(String avails, int aid, String pserver, String user, String vcode) {
		int SC=HttpStatus.SC_INTERNAL_SERVER_ERROR;
		String aurl = pserver+"/a?id="+aid+"&u="+user+"&p="+vcode;
		HttpClient client = getDefaultClient();
		
		SC=doAuthnForAppointment(client,aurl);

		HttpPost request = new HttpPost(pserver+REQ_SET_AVAILABILITY);
		
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>(2);
        postParameters.add(new BasicNameValuePair("a", avails));
        postParameters.add(new BasicNameValuePair("id", Integer.toString(aid)));
        Log.i("PB", "a="+avails+",id="+aid);
		try {
        request.setEntity(new UrlEncodedFormEntity(postParameters));
        Log.i("PB",request.getURI().toString());
        Log.i("PB",request.getEntity().toString());
        HttpResponse response = client.execute(request);
        SC=response.getStatusLine().getStatusCode();
        Log.i("PB sc"," "+SC);
        Log.i("PB",response.toString());
        
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return SC;
	}
	static protected int resendInvitation(int aid, int ruid, String pserver, String user, String vcode) {
		
		String aurl = pserver+"/a?id="+aid+"&u="+user+"&p="+vcode;
		HttpClient client = getDefaultClient();
		int SC=doAuthnForAppointment(client,aurl);
		HttpPost request = new HttpPost(pserver+REQ_RESENDINV);
		List<NameValuePair> postParameters = new ArrayList<NameValuePair>(2);
        postParameters.add(new BasicNameValuePair("id", Integer.toString(aid)));
        postParameters.add(new BasicNameValuePair("invitee", Integer.toString(ruid)));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters));
            Log.i("PB",request.getURI().toString());
            HttpResponse response = client.execute(request);
            SC=response.getStatusLine().getStatusCode();
            Log.i("PB sc"," "+SC);
    		} catch (Exception e) {
    			e.printStackTrace(); SC=HttpStatus.SC_INTERNAL_SERVER_ERROR;
    		} 
		return SC;
	}
	static protected int inviteAnotherParticipant(int aid, String participant, String pserver, String user, String vcode) {
		
		String aurl = pserver+"/a?id="+aid+"&u="+user+"&p="+vcode;
		HttpClient client = getDefaultClient();
		int SC=doAuthnForAppointment(client,aurl);
		HttpPost request = new HttpPost(pserver+REQ_INVANOTHER);
		List<NameValuePair> postParameters = new ArrayList<NameValuePair>(2);
        postParameters.add(new BasicNameValuePair("id", Integer.toString(aid)));
        postParameters.add(new BasicNameValuePair("a", participant));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters));
            Log.i("PB",request.getURI().toString());
            HttpResponse response = client.execute(request);
            SC=response.getStatusLine().getStatusCode();
            Log.i("PB sc"," "+SC);
    		} catch (Exception e) {
    			e.printStackTrace(); SC=HttpStatus.SC_INTERNAL_SERVER_ERROR;
    		} 
		return SC;
	}
	static protected int proposeNewDate(int aid, String pdate, String pserver, String user, String vcode) {
		
		String aurl = pserver+"/a?id="+aid+"&u="+user+"&p="+vcode;
		HttpClient client = getDefaultClient();
		int SC=doAuthnForAppointment(client,aurl);
		HttpPost request = new HttpPost(pserver+REQ_PROPOSEDATE);
		List<NameValuePair> postParameters = new ArrayList<NameValuePair>(2);
        postParameters.add(new BasicNameValuePair("id", Integer.toString(aid)));
        postParameters.add(new BasicNameValuePair("d", pdate));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters));
            Log.i("PB",request.getURI().toString());
            HttpResponse response = client.execute(request);
            SC=response.getStatusLine().getStatusCode();
            Log.i("PB sc"," "+SC);
    		} catch (Exception e) {
    			e.printStackTrace(); SC=HttpStatus.SC_INTERNAL_SERVER_ERROR;
    		}
		return SC;
	}
	
	/**
	 * 
	 */
	static protected int doVerification(String vrfyurl) {
		HttpClient client = getDefaultClient();
		//First request for AUTHN
		HttpGet request = new HttpGet(vrfyurl);
		HttpResponse response;
		try {
			response = client.execute(request);
			int SC=response.getStatusLine().getStatusCode();

			response.getEntity().getContent().close(); //You need to open and close the IS to release the connection !!!
			if(SC==HttpStatus.SC_OK) { //200
				return SC;
			} else { return HttpStatus.SC_INTERNAL_SERVER_ERROR; }
		}catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}
	
	/**
	 * Gets the the mobile user's preferred language from Locale
	 * @return the App supported language or "en" (default)
	 */
	static protected String getLanguage_i18n() {
		String lang = Locale.getDefault().getLanguage();
		if(!Arrays.asList(supp_langs).contains(lang) ) lang="en";

		if(lang.equals("es")) { lang="es-es"; }

		return lang;
	}
	
	/**
	 * @return Returns SC_CLIREDIR if the client is being redirected (eg. for net authentication)
	 *         or SC_NOTPLEFTSERV if the server is not Pleft Server
	 *         or HttpStatus.SC_OK if everything is OK
	 */
	protected static int checkServer(String pserver) {
		DefaultHttpClient client=getDefaultClient();
		HttpContext context = new BasicHttpContext();
		//We request the index
		HttpGet request = new HttpGet(pserver);
		HttpResponse response=null;
		try {
			response = client.execute(request, context);
			int SC=response.getStatusLine().getStatusCode();
			Log.i("PB","checkServ SC:" + SC);
		} catch (Exception e) {
			return SC_TIMEOUT;
		}
		HttpHost currentHost = (HttpHost)  context.getAttribute(
                ExecutionContext.HTTP_TARGET_HOST);

        String host = currentHost.getHostName();

		Log.i("PB","checkServ finalhost=" + host + "=="+ getHostName(pserver) +"=pserverhost ? " + host.equals(getHostName(pserver)) );
		if( !host.equals(getHostName(pserver)) ) {
			try {
				response.getEntity().getContent().close(); //You need to open and close the IS to release the connection !!!
			} catch (Exception e) { } 
			return SC_CLIREDIR;
		}
        //InputStream is;
		try {
			String html = EntityUtils.toString(response.getEntity()); //It consumes content !
			if(html.contains("Pleft")) { return HttpStatus.SC_OK; }
			else { Log.i("PB","checkServ NOPLEFT!"); return SC_NOTPLEFTSERV; }
		} catch (Exception e) {
			Log.i("PB","checkServ Got exception2:" + e);
			return SC_NOTPLEFTSERV;
		}
		
	}
	
	/**
	 * @return the client
	 */
	protected static DefaultHttpClient getDefaultClient() {
		// Init HTTP params
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, CONN_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, SO_TIMEOUT);
		httpParameters.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		httpParameters.setParameter("http.protocol.content-charset", "UTF-8");
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "PleftDroid/1.0 (Linux; U; Android "+
				                                                       Build.VERSION.RELEASE+"/"+Build.VERSION.CODENAME+"; "+
				                                                       Locale.getDefault().getLanguage()+"; "+
				                                                       Build.MODEL+")");
		
		return client;
	}
	
	protected static boolean isInetConnAvailable(Activity a) {
	    ConnectivityManager cm = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
	    // test for connection
	    if (cm.getActiveNetworkInfo() != null
	            && cm.getActiveNetworkInfo().isAvailable()
	            && cm.getActiveNetworkInfo().isConnected()) {
	        return true;
	    } else {
	        Log.i("check inet", "Internet Connection Not Present");
	        return false;
	    }
	}
	
	/**
	 * @return
	 */
	protected static Map<String, String> getParamsFromURL(String url) {
		String hostpart = url.substring(0,url.lastIndexOf("/"));
		String valpart = url.substring(url.indexOf("?") + 1);
		StringTokenizer st = new StringTokenizer(valpart, "&");
		Map<String, String> arr = new HashMap<String, String>();
		arr.put("pserver", hostpart);
		while (st.hasMoreTokens()) {
			String pair = st.nextToken();
			String lhs = pair.substring(0, pair.indexOf("="));
			String rhs  = pair.substring(pair.indexOf("=") + 1);
			arr.put(lhs, rhs);
		}
		return arr;
	}
	/**
	 * @return The host Name
	 */
	protected static String getHostName(String url) {
		String hostpart=url;
		if(hostpart.startsWith(HTTP_PROTO)) hostpart = url.substring(HTTP_PROTO.length());
		if(hostpart.lastIndexOf("/")>0 ) hostpart = hostpart.substring(0,hostpart.lastIndexOf("/"));
		
		return hostpart;
	}

}
