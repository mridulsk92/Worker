package com.example.mridul_xpetize.worker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private ViewPager mPager;
    //    private PagerAdapter mPagerAdapter;
    ProgressDialog pDialog;
    String taskid_st;
    List<String> itemsList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        //Get Intent
        Intent i = getIntent();
        taskid_st = i.getStringExtra("TaskId");

        mPager = (ViewPager) findViewById(R.id.viewpager);
        new GetCheckList().execute();

    }

    class CheckListAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public CheckListAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return itemsList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((RelativeLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View itemView = mLayoutInflater.inflate(R.layout.fragment_screen_slide_page, container, false);
            CheckBox checkItem = (CheckBox) itemView.findViewById(R.id.check_item);
            checkItem.setText(itemsList.get(position));

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }
    }

    private class GetCheckList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ChecklistActivity.this);
            pDialog.setMessage(getString(R.string.pDialog_wait));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            //Url with parameters
            String url = getString(R.string.url) + "EagleXpetizeService.svc/CheckLists/" + taskid_st;

            // Making a request to url and get response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            Log.d("url", url);
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {

                try {

                    JSONArray tasks = new JSONArray(jsonStr);

                    for (int i = 0; i < tasks.length(); i++) {
                        JSONObject c = tasks.getJSONObject(i);

                        String id = c.getString("TaskId");
                        String createdBy = c.getString("CreatedBy");
                        String modifiedBy = c.getString("ModifiedBy");
                        String item = c.getString("ItemListString");
                        String isSubTask = c.getString("IsSubTask");
                        String checked = c.getString("Checked");

//                        // adding each child node to HashMap key => value
//                        HashMap<String, Object> taskMap = new HashMap<String, Object>();
//                        taskMap.put("TaskId", id);
//                        taskMap.put("CreatedBy", createdBy);
//                        taskMap.put("ModifiedBy", modifiedBy);
//                        taskMap.put("ItemListString", item);
//                        taskMap.put("IsSubTask", isSubTask);
//                        taskMap.put("Checked", checked);
//                        checkListData.add(taskMap);

                        itemsList.add(item);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pDialog.isShowing())
                pDialog.dismiss();


            CheckListAdapter mAdapter = new CheckListAdapter(ChecklistActivity.this);
            mPager.setPageTransformer(true, new ZoomOutPageTransformer());
            mPager.setAdapter(mAdapter);

        }
    }


    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

}
