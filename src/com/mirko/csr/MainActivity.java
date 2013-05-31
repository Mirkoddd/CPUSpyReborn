/** Mirko Dimartino, 2013 <mirkoddd@gmail.com> */

package com.mirko.csr;

// imports
import java.util.ArrayList;
import java.util.List;

import com.mirko.csr.CpuStateMonitor.CpuState;
import com.mirko.csr.CpuStateMonitor.CpuStateMonitorException;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

/** main activity class */
public class MainActivity extends FragmentActivity
{
    private static final String TAG = "Cpu Spy Reborn";

    private CpuSpyReborn _app = null;

    // the views
    private GridLayout    _uiStatesView = null;
    private TextView        _uiAdditionalStates = null;
    private Chronometer        _uiTotalStateTime = null;
    private TextView        _uiHeaderAdditionalStates = null;
    private TextView        _uiHeaderTotalStateTime = null;
    private TextView        _uiStatesWarning = null;
    private TextView        _uiKernelString = null;

    /** whether or not we're updating the data in the background */
    private boolean     _updatingData = false;

    /** Initialize the Activity */
    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Inflate a custom action bar view to set a custom layout for title and total time.
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_total_time, null);
        customActionBarView.findViewById(R.id.actionbar_total_time_refresh).setOnClickListener(
                // refresh states on click
        		new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        refreshData();
                    }
                });

        customActionBarView.findViewById(R.id.actionbar_total_time_refresh).setOnLongClickListener(
                // show an info toast on longclick
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        Toast.makeText(getBaseContext(), getString(R.string.toast), Toast.LENGTH_SHORT).show();
						return true;
                    }
                });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM ,
                ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView);

        // inflate the view, stash the app context, and get all UI elements
        setContentView(R.layout.main_layout);
        _app = (CpuSpyReborn)getApplicationContext();
        findViews();

        if (savedInstanceState != null) {
            _updatingData = savedInstanceState.getBoolean("updatingData");
        }
    }

    /** When the activity is about to change orientation */
    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("updatingData", _updatingData);
    }


    /** Update the view when the application regains focus */
    @Override public void onResume () {
        super.onResume();

    	refreshData();

    }


    /** Map all of the UI elements to member variables */
    private void findViews() {
        _uiStatesView = (GridLayout)findViewById(R.id.ui_states_view);
        _uiKernelString = (TextView)findViewById(R.id.ui_kernel_string);
        _uiAdditionalStates = (TextView)findViewById(R.id.ui_additional_states);
        _uiHeaderAdditionalStates = (TextView)findViewById(R.id.ui_header_additional_states);
        _uiStatesWarning = (TextView)findViewById(R.id.ui_states_warning);
        _uiTotalStateTime = (Chronometer)findViewById(R.id.ui_total_state_time);
    }

    /** called when we want to inflate the menu */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // request inflater from activity and inflate into its menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // made it
        return true;
    }

    /** called to handle a menu event */
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case R.id.menu_about:
        	Intent launchNewIntent = new Intent(MainActivity.this,About.class);
        	startActivityForResult(launchNewIntent, 0);
        	break;
        case R.id.menu_reset:
            try {
                _app.getCpuStateMonitor().setOffsets();
            } catch (CpuStateMonitorException e) {
                // TODO: something
            }

            _app.saveOffsets();
            updateView();
            break;
        case R.id.menu_restore:
            _app.getCpuStateMonitor().removeOffsets();
            _app.saveOffsets();
            updateView();
            break;
        }

        // made it
        return true;
    }

    /** Generate and update all UI elements */
    public void updateView() {
        /** Get the CpuStateMonitor from the app, and iterate over all states,
         * creating a row if the duration is > 0 or otherwise marking it in
         * extraStates (missing) */
        CpuStateMonitor monitor = _app.getCpuStateMonitor();
        _uiStatesView.removeAllViews();
        List<String> extraStates = new ArrayList<String>();
        for (CpuState state : monitor.getStates()) {
            if (state.duration > 0) {
                generateStateRow(state, _uiStatesView);
            } else {
                if (state.freq == 0) {
                    extraStates.add("Deep Sleep");
                } else {
                    extraStates.add(state.freq/1000 + " MHz");
                }
            }            

        }

        // show the red warning label if no states found
        if ( monitor.getStates().size() == 0) {
            _uiStatesWarning.setVisibility(View.VISIBLE);
            _uiHeaderTotalStateTime.setVisibility(View.GONE);
            _uiTotalStateTime.setVisibility(View.GONE);
            _uiStatesView.setVisibility(View.GONE);
        }

        // update the total state time
        long totTime = monitor.getTotalStateTime() / 100;
//TODO
        _uiTotalStateTime.setText(sToString (totTime));

        // for all the 0 duration states, add the the Unused State area
        if (extraStates.size() > 0) {
            int n = 0;
            String str = "";

            for (String s : extraStates) {
                if (n++ > 0)
                    str += ", ";
                str += s;
            }

            _uiAdditionalStates.setVisibility(View.VISIBLE);
            _uiHeaderAdditionalStates.setVisibility(View.VISIBLE);
            _uiAdditionalStates.setText(str);
        } else {
            _uiAdditionalStates.setVisibility(View.GONE);
            _uiHeaderAdditionalStates.setVisibility(View.GONE);
        }

        // kernel line
        _uiKernelString.setText(_app.getKernelVersion());
    }

    /** Attempt to update the time-in-state info */
    public void refreshData() {
        if (!_updatingData) {
        	new RefreshStateDataTask().execute((Void)null);
        }
    }


    /** @return A nicely formatted String representing tSec seconds */
    private static String sToString(long tSec) {
        long h = (long)Math.floor(tSec / (60*60));
        long m = (long)Math.floor((tSec - h*60*60) / 60);
        long s = tSec % 60;
        String sDur;
        sDur = h + ":";
        if (m < 10)
            sDur += "0";
        sDur += m + ":";
        if (s < 10)
            sDur += "0";
        sDur += s;

        return sDur;
    }

    /**
     * @return a View that correpsonds to a CPU freq state row as specified
     * by the state parameter
     */
    private View generateStateRow(CpuState state, ViewGroup parent) {
        // inflate the XML into a view in the parent
        LayoutInflater inf = LayoutInflater.from((Context)_app);
        LinearLayout theRow = (LinearLayout)inf.inflate(
                R.layout.state_layout, parent, false);

        // what percentage we've got
        CpuStateMonitor monitor = _app.getCpuStateMonitor();
        float per = (float)state.duration * 100 /
            monitor.getTotalStateTime();
        String sPer = (int)per + "%";

        // state name
        String sFreq;
        if (state.freq == 0) {
            sFreq = "Deep Sleep";
        } else {
            sFreq = state.freq / 1000 + " MHz";
        }

        // duration
        long tSec = state.duration / 100;
        String sDur = sToString(tSec);

        // map UI elements to objects
        TextView freqText = (TextView)theRow.findViewById(R.id.ui_freq_text);
        TextView durText = (TextView)theRow.findViewById(
                R.id.ui_duration_text);
        TextView perText = (TextView)theRow.findViewById(
                R.id.ui_percentage_text);
        ProgressBar bar = (ProgressBar)theRow.findViewById(R.id.ui_bar);

        // modify the row
        freqText.setText(sFreq);
        perText.setText(sPer);
        durText.setText(sDur);
        bar.setProgress((int)per);

        // add it to parent and return
        parent.addView(theRow);
        return theRow;
    }

    /** Keep updating the state data off the UI thread for slow devices */
    protected class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {

        /** Stuff to do on a seperate thread */
        @Override protected Void doInBackground(Void... v) {
            CpuStateMonitor monitor = _app.getCpuStateMonitor();
            try {
                monitor.updateStates();
            } catch (CpuStateMonitorException e) {
                Log.e(TAG, "Problem getting CPU states");
            }

            return null;
        }

       /** Executed on the UI thread right before starting the task */
        @Override protected void onPreExecute() {
            log("starting data update");
            _updatingData = true;
        }

        /** Executed on UI thread after task */
        @Override protected void onPostExecute(Void v) {
            log("finished data update");
            _updatingData = false;
            updateView();
        }
    }

    /** logging */
    private void log(String s) {
        Log.d(TAG, s);
    }



}
