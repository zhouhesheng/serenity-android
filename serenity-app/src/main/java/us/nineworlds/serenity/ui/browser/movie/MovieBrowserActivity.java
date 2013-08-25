/**
 * The MIT License (MIT)
 * Copyright (c) 2012 David Carver
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package us.nineworlds.serenity.ui.browser.movie;

import java.util.ArrayList;
import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;

import us.nineworlds.serenity.core.model.CategoryInfo;
import us.nineworlds.serenity.core.model.MenuDrawerItem;
import us.nineworlds.serenity.core.model.VideoContentInfo;
import us.nineworlds.serenity.core.model.impl.MenuDrawerItemImpl;
import us.nineworlds.serenity.core.services.CategoryRetrievalIntentService;
import us.nineworlds.serenity.ui.activity.SerenityActivity;
import us.nineworlds.serenity.ui.adapters.MenuDrawerAdapter;
import us.nineworlds.serenity.ui.video.player.SerenitySurfaceViewVideoActivity;
import us.nineworlds.serenity.widgets.SerenityGallery;

import us.nineworlds.serenity.MainActivity;
import us.nineworlds.serenity.R;
import us.nineworlds.serenity.SerenityApplication;

import com.google.analytics.tracking.android.EasyTracker;
import com.jess.ui.TwoWayGridView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class MovieBrowserActivity extends SerenityActivity {

	private static String key;
	private static Spinner categorySpinner;
	private boolean restarted_state = false;
	private Handler categoryHandler;
	public static boolean IS_GRID_VIEW = false;
	public static int CLICKED_GRID_VIEW_ITEM = 0;

	private static Activity context;
	private MenuDrawer menuDrawer;
	private ListView menuOptions;
	
	/* (non-Javadoc)
	 * @see us.nineworlds.serenity.ui.activity.SerenityActivity#createSideMenu()
	 */
	@Override
	protected void createSideMenu() {
		menuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		IS_GRID_VIEW = prefs.getBoolean("movie_layout_grid", false);
		if (IS_GRID_VIEW) {
			menuDrawer.setContentView(R.layout.activity_movie_browser_gridview);
		} else {
			menuDrawer.setContentView(R.layout.activity_movie_browser);
		}
		menuDrawer.setMenuView(R.layout.menu_drawer);
		
		List<MenuDrawerItem> drawerMenuItem = new ArrayList<MenuDrawerItem>();
		drawerMenuItem.add(new MenuDrawerItemImpl("Grid View", R.drawable.ic_action_collections_view_as_grid));
		drawerMenuItem.add(new MenuDrawerItemImpl("Detail View", R.drawable.ic_action_collections_view_detail));
		
		menuOptions = (ListView)menuDrawer.getMenuView().findViewById(R.id.menu_options);
		menuOptions.setAdapter(new MenuDrawerAdapter(this, drawerMenuItem));
		menuOptions.setOnItemClickListener(new MovieMenuDrawerOnItemClickedListener(menuDrawer));
		hideMenuItems();
		
		View menu = findViewById(R.id.menu_button);
		menu.setOnClickListener(new MenuDrawerOnClickListener(menuDrawer));
	}
	
	/**
	 * @param listView
	 */
	public void hideMenuItems() {
		if (!getPackageManager().hasSystemFeature("android.hardware.touchscreen")) {
			menuOptions.setVisibility(View.INVISIBLE);
		}
	}
	
	public void showMenuItems() {
		if (!getPackageManager().hasSystemFeature("android.hardware.touchscreen")) {
			menuOptions.setVisibility(View.VISIBLE);
		}
		
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMenuItems();
			menuDrawer.toggleMenu();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && menuDrawer.isMenuVisible()) {
			hideMenuItems();
			menuDrawer.toggleMenu();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		key = getIntent().getExtras().getString("key");
		createSideMenu();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		if (restarted_state == false) {
			categoryHandler = new CategoryHandler();
			Messenger messenger = new Messenger(categoryHandler);
			Intent categoriesIntent = new Intent(this,
					CategoryRetrievalIntentService.class);
			categoriesIntent.putExtra("key", key);
			categoriesIntent.putExtra("MESSENGER", messenger);
			startService(categoriesIntent);
			context = this;
		}
		restarted_state = false;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_movie_browser, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		restarted_state = true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (data != null && data.getAction().equals("com.mxtech.intent.result.VIEW")) {
				SerenityGallery gallery = (SerenityGallery) findViewById(R.id.moviePosterGallery);
				if (gallery != null) {
					VideoContentInfo video = (VideoContentInfo) gallery.getSelectedItem();
					if (video != null) {
						updateProgress(data, video);
					}
				} else {
					TwoWayGridView gridView = (TwoWayGridView) findViewById(R.id.movieGridView);
					if (gridView != null) {
						VideoContentInfo video = (VideoContentInfo) gridView.getSelectedItem();
						if (video == null) {
							video = (VideoContentInfo) gridView.getItemAtPosition(CLICKED_GRID_VIEW_ITEM);
						}
						if (video != null) {
							updateProgress(data, video);
						}
					}
				}
			}
		}
		
		boolean externalPlayer = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("external_player", false);
		if (!externalPlayer) {
			if (!SerenityApplication.getVideoPlaybackQueue().isEmpty()) {
				Intent vpIntent = new Intent(this,
						SerenitySurfaceViewVideoActivity.class);
				startActivityForResult(vpIntent, MainActivity.BROWSER_RESULT_CODE);
			}
		}
	}	
	
	private static class CategoryHandler extends Handler {

		private ArrayList<CategoryInfo> categories;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj != null) {
				categories = (ArrayList<CategoryInfo>) msg.obj;
				setupMovieBrowser();
			}
		}

		/**
		 * Setup the Gallery and Category spinners
		 */
		protected void setupMovieBrowser() {
			ArrayAdapter<CategoryInfo> spinnerArrayAdapter = new ArrayAdapter<CategoryInfo>(
					context, R.layout.serenity_spinner_textview, categories);
			spinnerArrayAdapter
					.setDropDownViewResource(R.layout.serenity_spinner_textview_dropdown);

			categorySpinner = (Spinner) context
					.findViewById(R.id.movieCategoryFilter);
			categorySpinner.setVisibility(View.VISIBLE);
			categorySpinner.setAdapter(spinnerArrayAdapter);
			categorySpinner
					.setOnItemSelectedListener(new CategorySpinnerOnItemSelectedListener(
							"all", key));
			categorySpinner.requestFocus();
		}

	}
}
