package com.example.ab.testing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity{
	private FirebaseAnalytics mFirebaseAnalytics;
	private FirebaseRemoteConfig mFirebaseRemoteConfig;
	private String experiment1_variant;
	private TextView bubbleTop, bubbleBottom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bindWidget();

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings
				.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build();
		mFirebaseRemoteConfig.setConfigSettings(configSettings);
		mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

		fetchData();
		setEventListener();
	}

	private void bindWidget() {
		bubbleTop = (TextView) findViewById(R.id.bubble_top);
		bubbleBottom = (TextView) findViewById(R.id.bubble_bottom);
	}

	private void fetchData() {
		long cacheExpiration = 3600; // 1 hour in seconds.
		if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
			cacheExpiration = 0;
		}
		mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					mFirebaseRemoteConfig.activateFetched();
					experiment1_variant = mFirebaseRemoteConfig.getString("experiment1");
					mFirebaseAnalytics.setUserProperty("MyExperiment", experiment1_variant);

					if ("Top".equals(experiment1_variant)) {
						bubbleTop.setVisibility(View.VISIBLE);
					} else if ("Bottom".equals(experiment1_variant)) {
						bubbleBottom.setVisibility(View.VISIBLE);
					}
				}
			}
		});
	}

	private void setEventListener() {
		bubbleTop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				logEventToFirebase();
			}
		});
		bubbleBottom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				logEventToFirebase();
			}
		});
	}

	private void logEventToFirebase() {
		Bundle params = new Bundle();
		params.putString(FirebaseAnalytics.Param.LOCATION, experiment1_variant);
		mFirebaseAnalytics.logEvent("BubbleClicked", params);
		bubbleTop.setVisibility(View.GONE);
		bubbleBottom.setVisibility(View.GONE);
	}
}