package com.doug.nextbus.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;

import com.doug.nextbus.R;
import com.doug.nextbus.backend.APIController;
import com.doug.nextbus.backend.Data;
import com.doug.nextbus.custom.RoutePagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

/*
 * The top level, main activity. It lets the users switch between routes.
 */
public class RoutePickerActivity extends Activity implements
		OnSharedPreferenceChangeListener {

	private Data data;
	private String[] currentRoutes;
	private boolean onlyActiveRoutes = false;
	private RoutePagerAdapter pagerAdapter;
	private ViewPager pager;
	private ImageView mapButton;
	private static final String[] allRoutes;
	private Context cxt;

	static {

		allRoutes = new String[] { "red", "blue", "trolley", "green", "night",
				"emory" };
	}

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.route_picker);

		cxt = this;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		onlyActiveRoutes = prefs.getBoolean("showActiveRoutes", false);

		updateCurrentRoutes();
		Data.setConfigData(cxt);
		mapButton = (ImageView) findViewById(R.id.mapButton);

		/* Setup ViewGroup */
		pagerAdapter = new RoutePagerAdapter(currentRoutes, data, cxt);
		pager = (ViewPager) findViewById(R.id.routepagerviewpager);
		pager.setAdapter(pagerAdapter);

		/* Setup ViewGroup Indicator */
		final TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.routes);
		titleIndicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
		titleIndicator.setBackgroundColor(getResources().getColor(
				R.color.subtitlecolor));
		titleIndicator.setFooterIndicatorHeight(10);
		titleIndicator.setSelectedBold(false);

		final float scale = getResources().getDisplayMetrics().density;
		final float textSize = 20.0f;
		float pixels = textSize * scale;
		titleIndicator.setTextSize(pixels);
		titleIndicator.setViewPager(pager);

		int color = R.color.orange; // default color
		if (currentRoutes.length > 0) // if there are active routes
			color = getColor(Data.hm.get(currentRoutes[0]).tag);
		titleIndicator.setSelectedColor(getResources().getColor(color));
		titleIndicator.setFooterColor(getResources().getColor(color));

		/* Listener for PageChanging. Basically the left and right swiping */
		titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			public void onPageScrollStateChanged(int arg0) {
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			public void onPageSelected(int position) {
				int color = R.color.orange; // default color
				if (currentRoutes.length > 0) // if there are active routes
					color = getColor(Data.hm.get(currentRoutes[position]).tag);

				titleIndicator.setSelectedColor(getResources().getColor(color));
				titleIndicator.setFooterColor(getResources().getColor(color));

			}
		});

		/* Button for switching to MapView */
		mapButton.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mapButton.setBackgroundColor(getResources().getColor(
							R.color.black));
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mapButton.setBackgroundColor(0);
					Intent mapActivity = new Intent(getApplicationContext(),
							MapViewActivity.class);
					startActivity(mapActivity);
					return true;
				}
				return true;
			}
		});

	}

	public int getColor(String route) {

		int color = 0;
		if (route.equals("red")) {
			color = R.color.red;
		} else if (route.equals("blue")) {
			color = R.color.blue;
		} else if (route.equals("green")) {
			color = R.color.green;
		} else if (route.equals("trolley")) {
			color = R.color.yellow;
		} else if (route.equals("night")) {
			color = R.color.night;
		} else if (route.equals("emory")) {
			color = R.color.pink;
		}
		return color;
	}

	/* Checks hideRoutes preference. */
	private void updateCurrentRoutes() {

		if (onlyActiveRoutes) {
			setCurrentRoutes(APIController.getActiveRoutesList(cxt));
		} else {

			setCurrentRoutes(allRoutes);
		}

	}

	private void setCurrentRoutes(String[] activeRoutes) {
		currentRoutes = activeRoutes;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stock_menu, menu);
		return true;
	}

	/* Options menu handler. */
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.aboutmenusitem:
			Intent aboutActivity = new Intent(getApplicationContext(),
					CreditsActivity.class);
			startActivity(aboutActivity);
			return true;
		case R.id.preferencesmenuitem:
			Intent preferenceActivity = new Intent(getApplicationContext(),
					PreferencesActivity.class);
			startActivity(preferenceActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Listener for changed preferences */
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

		if (key.equals("showActiveRoutes")) {
			onlyActiveRoutes = prefs.getBoolean("showActiveRoutes", true);
			updateCurrentRoutes();

			pagerAdapter.updateCurrentRoutes(this.currentRoutes);
			pagerAdapter.notifyDataSetChanged();
			pager.setCurrentItem(1);

		}

	}

}
