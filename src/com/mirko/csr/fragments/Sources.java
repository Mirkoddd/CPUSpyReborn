/**
 * 
 */
package com.mirko.csr.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.mirko.csr.R;

public class Sources extends Fragment {
	
    private ImageButton mBrandonSource;
    private ImageButton mMirkoSource;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		View sourcesView = inflater.inflate(R.layout.sources, null);

		mBrandonSource = (ImageButton) sourcesView.findViewById(R.id.cpuspy_source);
		mBrandonSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Uri uri = Uri.parse("https://github.com/bvalosek/cpuspy");
            	 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            	 startActivity(intent);
            }
        });

		mMirkoSource = (ImageButton) sourcesView.findViewById(R.id.cpuspy_reborn_source);
		mMirkoSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Uri uri = Uri.parse("https://github.com/mirkoddd/cpuspyreborn");
            	 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            	 startActivity(intent);
            }
        });

		return sourcesView;
	}

}
