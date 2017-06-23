package com.skoolevents.eventapp;

import android.app.ListActivity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.util.Property;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Message;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.models.nosql.SkooleventsDO;

import com.google.android.gms.common.api.GoogleApiClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.skoolevents.eventapp.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

import android.os.Handler;

/**
 *
 */
public class MainActivity extends ListActivity {

    private static final String LOG_TAG = "SchoolEventsMain";

    Context context;

    private Handler handler;




    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("@@@ >>>", "MainActivity::onCreate -- >>>>>>>>>>>>>>>");

        super.onCreate(savedInstanceState);
        this.context = this;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        android.widget.Button fab = (android.widget.Button) findViewById(R.id.createEventButton);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.v("@@@ >>>", "MainActivity::onCreate -- create button pressed");

                Intent myIntent = new Intent(view.getContext(), com.skoolevents.eventapp.CreateActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });



        //fetchAWSDynamoDBData();

        fetchAWSDynamoDBDataUsingAsyncTask();

    }


    /**
     *
     * @return
     */
    boolean fetchAWSDynamoDBData() {
        Thread thread = new Thread(new AWSThread());
        thread.start();


        handler = new Handler() {
            public void handleMessage(Message msg) {
                Log.v("@@@", ">>>>>>>>>>>>>>>>>>>>>>>> Handle Message >>>>>>>>>>>>>" + msg.obj);


                PaginatedQueryList<SkooleventsDO> results = (PaginatedQueryList<SkooleventsDO>) msg.obj;
                if (results != null) {
                    // Just to get log output
                    Iterator<SkooleventsDO> resultsIterator = results.iterator();
                    ArrayList<SkooleventsDO> eventList = new ArrayList<SkooleventsDO>();

                    while (resultsIterator.hasNext()) {
                        SkooleventsDO se = resultsIterator.next();
                        Log.v(LOG_TAG + "_H", "Found item : " + se.getDate() + ", " + se.getTitle() + ", " + se.getDescription());

                        SkooleventsDO event = new SkooleventsDO();
                        event.setDate(se.getDate());
                        event.setDescription(se.getDescription());
                        event.setTitle(se.getTitle());
                        eventList.add(event);
                    }

                    ArrayAdapter<Property> adapter = new com.skoolevents.eventapp.EventArrayAdapter(getListView().getContext(), 0, eventList);
                    ListView listView = (ListView) findViewById(android.R.id.list);
                    listView.setAdapter(adapter);

                } else {
                    showNoEventsMessage();
                }
            }
        };

        return true;
    }



    boolean fetchAWSDynamoDBDataUsingAsyncTask() {

        new LongOperation().execute(new String[] { "" });

        return true;
    }




    /**
     *
     */
    class AWSThread implements Runnable {


        /** how many results to retrieve per service call. */
        private static final int RESULTS_PER_RESULT_GROUP = 40;


        public void run () {

            try {
                Log.v(LOG_TAG, "Before scanning dynamoDB");

                final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

                final SkooleventsDO itemToFind = new SkooleventsDO();
                itemToFind.setSchool("jtms");

                /*final Condition rangeKeyCondition = new Condition()
                        .withComparisonOperator(ComparisonOperator.LT.toString())
                        .withAttributeValueList(new AttributeValue().withN(Double.toString(1496667610262)));*/
                final DynamoDBQueryExpression<SkooleventsDO> queryExpression = new DynamoDBQueryExpression<SkooleventsDO>()
                        .withHashKeyValues(itemToFind)
                        //.withRangeKeyCondition("date", rangeKeyCondition)
                        .withScanIndexForward(true)
                        .withConsistentRead(false)
                        .withLimit(RESULTS_PER_RESULT_GROUP);

                PaginatedQueryList<SkooleventsDO> results = dynamoDBMapper.query(SkooleventsDO.class, queryExpression);

                Log.v(LOG_TAG, "After executing query against dynamoDB");


                if (results != null && results.size() != 0) {

                    Iterator<SkooleventsDO> resultsIterator = results.iterator();
                    while (resultsIterator.hasNext()) {
                        SkooleventsDO se = resultsIterator.next();
                        Log.v(LOG_TAG, "Found item : " + se.getDate() + ", " + se.getTitle() + ", " + se.getDescription());
                    }

                    Message msg = Message.obtain();
                    msg.obj = results;
                    handler.sendMessage(msg);

                } else {
                    Message msg = Message.obtain();
                    msg.obj = null;
                    handler.sendMessage(msg);
                }


            } catch (final AmazonClientException ex) {

                Log.e(LOG_TAG, "Failed scanning for data : " + ex.getMessage(), ex);

            } catch (Exception e) {

                e.printStackTrace();

            }
        }

    }


    /**
     *
     */
    private class LongOperation extends AsyncTask<String, Void, String> {

        /** how many results to retrieve per service call. */
        private static final int RESULTS_PER_RESULT_GROUP = 40;


        PaginatedQueryList<SkooleventsDO> results = null;


        /**
         *
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(String... params) {

            Log.v(LOG_TAG, "Before querying dynamoDB");

            try {

                final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

                final SkooleventsDO itemToFind = new SkooleventsDO();
                itemToFind.setSchool("jtms");

                /*final Condition rangeKeyCondition = new Condition()
                        .withComparisonOperator(ComparisonOperator.LT.toString())
                        .withAttributeValueList(new AttributeValue().withN(Double.toString(1496667610262)));*/
                final DynamoDBQueryExpression<SkooleventsDO> queryExpression = new DynamoDBQueryExpression<SkooleventsDO>()
                        .withHashKeyValues(itemToFind)
                        //.withRangeKeyCondition("date", rangeKeyCondition)
                        .withScanIndexForward(true)
                        .withConsistentRead(false)
                        .withLimit(RESULTS_PER_RESULT_GROUP);

                results = dynamoDBMapper.query(SkooleventsDO.class, queryExpression);

                Log.v(LOG_TAG, "After executing query against dynamoDB");


            } catch (final AmazonClientException ex) {

                Log.e(LOG_TAG, "Failed scanning for data : " + ex.getMessage(), ex);

            } catch (Exception e) {

                e.printStackTrace();

            }


            return "Executed";
        }


        /**
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {

            Log.v("@@@", ">>>>>>>>>>>>>>>>>>>>>>>> Handle Message >>>>>>>>>>>>>" );


            if (results != null && results.size() != 0) {
                // Just to get log output
                Iterator<SkooleventsDO> resultsIterator = results.iterator();
                ArrayList<SkooleventsDO> eventList = new ArrayList<SkooleventsDO>();

                while (resultsIterator.hasNext()) {
                    SkooleventsDO se = resultsIterator.next();
                    Log.v(LOG_TAG + "_H", "Found item : " + se.getDate() + ", " + se.getTitle() + ", " + se.getDescription());

                    SkooleventsDO event = new SkooleventsDO();
                    event.setDate(se.getDate());
                    event.setDescription(se.getDescription());
                    event.setTitle(se.getTitle());
                    eventList.add(event);
                }

                ArrayAdapter<Property> adapter = new com.skoolevents.eventapp.EventArrayAdapter(getListView().getContext(), 0, eventList);
                ListView listView = (ListView) findViewById(android.R.id.list);
                listView.setAdapter(adapter);

            } else {
                showNoEventsMessage();
            }


        }


        /**
         *
         */
        @Override
        protected void onPreExecute() {}


        /**
         *
         * @param values
         */
        @Override
        protected void onProgressUpdate(Void... values) {}
    }











    /**
     * sample data -- not used anymore
     */
    void populateItems() {

        String[] myitems = { "Top 25% Cookie", "Blue and Orange", "Good Student Celebration Event", "How to prepare for exams seminar" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getListView().getContext(),
                android.R.layout.simple_list_item_1,
                myitems
        );

        getListView().setAdapter(adapter);

    }



    /**
     *
     */
    void showNoEventsMessage() {

        String[] myitems = { "No events found..." };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getListView().getContext(),
                android.R.layout.simple_list_item_1,
                myitems
        );

        getListView().setAdapter(adapter);

    }


    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
