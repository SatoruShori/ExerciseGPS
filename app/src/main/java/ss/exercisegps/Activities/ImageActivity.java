package ss.exercisegps.Activities;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import ss.exercisegps.R;
import ss.exercisegps.Utillities.SystemUtils;
import ss.exercisegps.Utillities.ZoomableImageView;


public class ImageActivity extends AppCompatActivity {
	DrawerLayout mDrawerLayout;
	ZoomableImageView touchImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		touchImageView = (ZoomableImageView) findViewById(R.id.touchImageView);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		String imageName = getIntent().getExtras().getString("imageName", "");
		System.out.println(imageName);
		if (imageName.length() > 0) {
			SystemUtils.setFullSizeImageFromName(touchImageView, imageName);
			touchImageView.setZoomable(true);
		} else {

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(menuItem);
		}
	}
}
