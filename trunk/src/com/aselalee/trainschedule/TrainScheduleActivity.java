/**
* @copyright	Copyright (C) 2011 Asela Leelaratne
* @license		GNU/GPL Version 3
* 
* This Application is released to the public under the GNU General Public License.
* 
* GNU/GPL V3 Extract.
* 15. Disclaimer of Warranty.
* THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
* EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
* PROVIDE THE PROGRAM AS IS WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
* INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
* PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
* NECESSARY SERVICING, REPAIR OR CORRECTION.
*/

package com.aselalee.trainschedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class TrainScheduleActivity extends Activity implements Runnable {
	private LinearLayout lin_lay;
	private Button get_given_btn;
	private Button get_all_btn;
	private Button swap_btn;
	private AutoCompleteTextView actv_from;
	private AutoCompleteTextView actv_to;
	private ArrayAdapter<Station> adapter;
	private ArrayAdapter<CharSequence> adapter_times_from;
	private ArrayAdapter<CharSequence> adapter_times_to;
	private Spinner spinner_times_from;
	private Spinner spinner_times_to;
	private Station stations[];
	private String[] stationsText;
	private String[] stationsVal;
	private String station_from_txt = "";
	private String station_to_txt = "";
	private String station_from_val = "";
	private String station_to_val = "";
	private String time_from_txt = "";
	private String time_to_txt = "";
	private String time_from_val = "";
	private String time_to_val = "";

	/**
	 * Default values.
	 */
	private int def_time_from = 14;
	private int def_time_to = 19;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_user_input);
		/**
		 * Setup "AutoCompleteText" views
		 */
		populateStations();
		actv_from = (AutoCompleteTextView)findViewById(R.id.search_stations_from);
		actv_to = (AutoCompleteTextView)findViewById(R.id.search_stations_to);
		adapter = new ArrayAdapter<Station>(this, R.layout.actv_drop_down_list_item, stations);
		actv_from.setAdapter(adapter);
		actv_to.setAdapter(adapter);
		actv_from.setOnItemClickListener(new ACTVFromItemClickListner());
		actv_to.setOnItemClickListener(new ACTVToItemClickListner());

		/**
		 * Setup time "spinner"s
		 */
		adapter_times_from = ArrayAdapter.createFromResource(
				this, R.array.times_from_array, R.layout.spinner);
		adapter_times_from.setDropDownViewResource(R.layout.spinner_drop_down_list_item);
		spinner_times_from = (Spinner)findViewById(R.id.search_times_from);
		spinner_times_from.setAdapter(adapter_times_from);

		adapter_times_to = ArrayAdapter.createFromResource(
				this, R.array.times_to_array, R.layout.spinner);
		adapter_times_to.setDropDownViewResource(R.layout.spinner_drop_down_list_item);
		spinner_times_to = (Spinner)findViewById(R.id.search_times_to);
		spinner_times_to.setAdapter(adapter_times_to);

		/**
		 * Setup submit buttons.
		 */
		get_given_btn = (Button)findViewById(R.id.search_get_given);
		get_given_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				time_from_val = Constants.MapTimeFrom(spinner_times_from.getSelectedItemPosition());
				time_to_val = Constants.MapTimeTo(spinner_times_to.getSelectedItemPosition());
				time_from_txt = spinner_times_from.getSelectedItem().toString();
				time_to_txt = spinner_times_to.getSelectedItem().toString();
				show_results();
				}
			});
		get_all_btn = (Button)findViewById(R.id.search_get_all);
		get_all_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/**
				 * To get the full schedule, times are mapped to least starting time and
				 * most ending time.
				 */
				time_from_val = Constants.MapTimeFrom(0);
				time_to_val = Constants.MapTimeTo(-1);
				time_from_txt = getString(R.string.earliest_from_time);
				time_to_txt = getString(R.string.latest_to_time);
				show_results();
			}
		});
		/**
		 * Setup listeners for the time select spinners.
		 */
		spinner_times_from.setOnItemSelectedListener(new FromSpinnerOnItemSelectedListener());
		spinner_times_to.setOnItemSelectedListener(new ToSpinnerOnItemSelectedListener());
		/**
		 * Get Layout handle.
		 */
		lin_lay = (LinearLayout)findViewById(R.id.search_lin_lay);
		/**
		 * Setup swap button.
		 */
		swap_btn = (Button)findViewById(R.id.search_swap);
		swap_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				lin_lay.requestFocusFromTouch();
				String tmp = station_to_txt;
				station_to_txt = station_from_txt;
				station_from_txt = tmp;
				tmp = station_to_val;
				station_to_val = station_from_val;
				station_from_val = tmp;
				tmp = null;
				actv_to.setText(station_to_txt);
				actv_from.setText(station_from_txt);
			}
		});
	}

	/**
	 * Populate stations adapter with string resources.
	 */
	private void populateStations( )
	{
		stationsText = getResources().getStringArray(R.array.stations_array);
		stationsVal = getResources().getStringArray(R.array.stations_val_array);
		stations = new Station[stationsText.length];
		int index = 0;
		for(index = 0; index < stationsText.length; index++) {
			stations[index] = new Station( stationsText[index],stationsVal[index]);
		}
	}
	/**
	 * Checks whether from time is greater than the to time.
	 */
	public class FromSpinnerOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			int toPos = spinner_times_to.getSelectedItemPosition();
			if(pos > toPos) {
				spinner_times_to.setSelection(pos);
			}
		}
		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	/**
	 * Checks whether the to time is less than the from time.
	 */
	public class ToSpinnerOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			int toPos = spinner_times_from.getSelectedItemPosition();
			if(pos < toPos) {
				spinner_times_from.setSelection(pos);
			}
		}
		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	/**
	 * From station AutoCompleteText callback
	 */
	private class ACTVFromItemClickListner implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			Station selectedStation = (Station) parent.getItemAtPosition(pos);
			station_from_txt = selectedStation.getText();
			station_from_val = selectedStation.getValue();
			actv_to.requestFocusFromTouch();
		}
	}

	/**
	 * From station AutoCompleteText callback
	 */
	private class ACTVToItemClickListner implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			Station selectedStation = (Station) parent.getItemAtPosition(pos);
			station_to_txt = selectedStation.getText();
			station_to_val = selectedStation.getValue();
			Constants.HideSoftKeyboard(actv_to, getBaseContext());
			lin_lay.requestFocusFromTouch();
		}
	}

	/**
	 * Calls the next activity to display results.
	 */
	private void show_results() {
		Constants.HideSoftKeyboard(actv_to, getBaseContext());
		if(validateStations()) {
			/**
			 * Add params to history database.
			 * This is done in a separate thread.
			 */
			Thread thread = new Thread(this);
			thread.start();

			Intent intent = Constants.GetResultViewIntent(this);
			Constants.PupulateIntentForResultsActivity(
					station_from_val, station_from_txt,
					station_to_val, station_to_txt,
					time_from_val, time_from_txt,
					time_to_val, time_to_txt,
					intent);
			startActivity(intent);
		}
		return;
	}

	/**
	 * Read spinner positions from preference file.
	 */
	private void readCurrentState(Context c) {
		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;
		int version_code = 0;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			version_code = info.versionCode;
		} catch (NameNotFoundException e) {
			Log.e(Constants.LOG_TAG, "Package name not found" + e);
		}
		/**
		 * Get the SharedPreferences object for this application
		 */
		SharedPreferences p = c.getSharedPreferences(Constants.PREFERENCES_FILE, MODE_WORLD_READABLE);
		if( p.getInt(Constants.APP_VERSION_CODE, -1) != version_code) {
			SharedPreferences.Editor e = p.edit();
			e.putInt(Constants.APP_VERSION_CODE, version_code);
			e.commit();
			return;
		}
		/**
		 * Get the position and value of the spinner from the file
		 */
		station_from_txt = p.getString(Constants.STATION_FROM_TXT, "");
		actv_from.setText(station_from_txt);
		station_from_val = p.getString(Constants.STATION_FROM_VAL, "");
		station_to_txt = p.getString(Constants.STATION_TO_TXT, "");
		actv_to.setText(station_to_txt);
		station_to_val = p.getString(Constants.STATION_TO_VAL, "");
		spinner_times_from.setSelection(p.getInt(Constants.TIME_FROM_POS, def_time_from));
		spinner_times_to.setSelection(p.getInt(Constants.TIME_TO_POS, def_time_to));
	}

	/**
	 * Write current spinner positions to preferences file.
	 */
	private boolean writeCurrentState(Context c) {
		/**
		 * Get the SharedPreferences object for this application
		 */
		SharedPreferences p = c.getSharedPreferences(Constants.PREFERENCES_FILE, MODE_WORLD_READABLE);
		/**
		 * Get the editor for this object.
		 */
		SharedPreferences.Editor e = p.edit();
		/**
		 * Write values.
		 */
		e.putString(Constants.STATION_FROM_TXT, station_from_txt);
		e.putString(Constants.STATION_FROM_VAL, station_from_val);
		e.putString(Constants.STATION_TO_TXT, station_to_txt);
		e.putString(Constants.STATION_TO_VAL, station_to_val);
		e.putInt(Constants.TIME_FROM_POS, spinner_times_from.getSelectedItemPosition());
		e.putInt(Constants.TIME_TO_POS, spinner_times_to.getSelectedItemPosition());
		return (e.commit());
	}

	@Override
	public void onPause() {
		super.onPause();
		if(!writeCurrentState(this)) {
			Toast.makeText(this, "Failed to save state!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		readCurrentState(this);
		lin_lay.requestFocusFromTouch();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void run() {
		DBDataAccess myDBAcc = new DBDataAccess(this);
		myDBAcc.PushDataHistory(station_from_txt, station_from_val,
				station_to_txt, station_to_val,
				time_from_txt, time_from_val,
				time_to_txt, time_to_val);
		myDBAcc.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.search_menu_add_to_fav:
				if(validateStations() == false) {
					return true;
				}
				time_from_val = Constants.MapTimeFrom(spinner_times_from.getSelectedItemPosition());
				time_to_val = Constants.MapTimeTo(spinner_times_to.getSelectedItemPosition());
				time_from_txt = spinner_times_from.getSelectedItem().toString();
				time_to_txt = spinner_times_to.getSelectedItem().toString();
				Constants.GetNewFavNameAndAddToFavs(this, false,
						station_from_txt, station_from_val,
						station_to_txt, station_to_val,
						time_from_txt, time_from_val,
						time_to_txt, time_to_val, handler);
				return true;
			case R.id.search_menu_switch_result_view:
				Constants.GetResultsViewChoiceFromUser(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String msgStr = (String)msg.obj;
			Toast.makeText(getBaseContext(), msgStr, Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * Private class to hold stations data.
	 * This container is used as the adapter object for auto complete text
	 * views.
	 */
	private class Station {
		private String text;
		private String value;

		public Station( String textStr, String valueStr ) {
			this.text = textStr;
			this.value = valueStr;
		}

		public String getText() {
			return this.text;
		}

		public String getValue() {
			return this.value;
		}

		public String toString() {
			return this.text;
		}
	}
	/**
	 * Utility functions.
	 */
	private boolean validateStations() {
		int from = searchString(stationsText, actv_from.getText().toString());
		int to = searchString(stationsText, actv_to.getText().toString());
		if(( from > -1) && (to > -1)){
			if( from == to) {
				Toast.makeText(this, "Station Names are Same", Toast.LENGTH_LONG).show();
				return false;
			} else {
				return true;
			}
		}
		Toast.makeText(this, "Invalid Station Names", Toast.LENGTH_LONG).show();
		return false;
	}

	private int searchString(String strArray[], String strSrc) {
		for(int i = 0; i < strArray.length; i++) {
			if( strSrc.equals(strArray[i])) {
				return i;
			}
		}
		return -1;
	}
}
