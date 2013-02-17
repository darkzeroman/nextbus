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
import com.doug.nextbus.custom.WakeupAsyncTask;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

/*
 * The top level, main activity. It lets the users switch between routes.
 */
public class RoutePickerActivity extends Activity implements
		OnSharedPreferenceChangeListener {

	private String[] currentRoutes;
	public static final String[] allRoutes;
	private boolean onlyActiveRoutes;
	private ViewPager pager;
	private RoutePagerAdapter pagerAdapter;
	private ImageView mapButton;
	private Context cxt;
	private TitlePageIndicator titleIndicator;

	static {
		allRoutes = new String[] { "red", "blue", "trolley", "green", "night",
				"emory" };
	}

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.route_picker);

		new WakeupAsyncTask().execute();

		cxt = this;
		Data.setConfigData(cxt);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		// Updating available routes depending on preference
		onlyActiveRoutes = prefs.getBoolean("showActiveRoutes", false);
		updateCurrentRoutes();

		// Setup ViewGroup
		pagerAdapter = new RoutePagerAdapter(currentRoutes, cxt);
		pager = (ViewPager) findViewById(R.id.routepagerviewpager);
		pager.setAdapter(pagerAdapter);

		// Setup ViewGroup Indicator
		titleIndicator = (TitlePageIndicator) findViewById(R.id.routes);
		titleIndicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
		titleIndicator.setBackgroundColor(getResources().getColor(
				R.color.subtitlecolor));
		titleIndicator.setFooterIndicatorHeight(10);
		titleIndicator.setSelectedBold(false);

		// Sets the size and color of the text
		final float scale = getResources().getDisplayMetrics().density;
		final float textSize = 20.0f;
		float pixels = textSize * scale;
		titleIndicator.setTextSize(pixels);
		titleIndicator.setViewPager(pager);
		updateColor(0);

		// Listener for PageChanging. Basically the left and right swiping
		titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageScrollStateChanged(int arg0) {
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			public void onPageSelected(int position) {
				updateColor(position);
			}
		});

		// Button and event for switching to MapView
		mapButton = (ImageView) findViewById(R.id.mapButton);
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

	/** Updates available routes depending on preference. */
	private void updateCurrentRoutes() {
		if (onlyActiveRoutes) {
			currentRoutes = APIController.getActiveRoutesList(cxt);
		} else {
			currentRoutes = allRoutes;
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stock_menu, menu);
		return true;
	}

	/** Updates text color depending on the position of view page */
	private void updateColor(int position) {
		int color = R.color.orange; // default color
		if (currentRoutes.length > 0) // if there are active routes
			color = Data.getColorFromRouteTag(currentRoutes[position]);

		titleIndicator.setSelectedColor(getResources().getColor(color));
		titleIndicator.setFooterColor(getResources().getColor(color));
	}

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

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		/*
		 * Listener for changed preferences, need to update current route and
		 * pager adapter
		 */

		if (key.equals("showActiveRoutes")) {
			onlyActiveRoutes = prefs.getBoolean("showActiveRoutes", true);
			updateCurrentRoutes();

			pagerAdapter.updateCurrentRoutes(this.currentRoutes);
			pager.setCurrentItem(0);
			pagerAdapter.notifyDataSetChanged();
			updateColor(0);
		}

	}
}
