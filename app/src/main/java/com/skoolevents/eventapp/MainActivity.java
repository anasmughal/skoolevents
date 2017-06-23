package com.skoolevents.eventapp;

import android.app.ListActivity;
import android.content.Context;
import android.net.Uri;
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
//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.appindexing.Thing;
//import com.google.android.gms.common.api.GoogleApiClient;
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

    //private Thread awsThread;
    private Handler handler;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;


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


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        //showLoadingMessage();

        fetchAWSDynamoDBData();

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


        /**
         *
         */
        class AWSThread implements Runnable {


            /** how many results to retrieve per service call. */
            private static final int RESULTS_PER_RESULT_GROUP = 40;

            //private Iterator<SkooleventsDO> resultsIterator;


            public void run () {

                try {
                    Log.v(LOG_TAG, "Before scanning dynamoDB");

                    final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
                    //final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

                    /*
                    Reply replyKey = new Reply();
                    replyKey.setId("1493535646759");

                    DynamoDBQueryExpression<SkooleventsDO> queryExpression = new DynamoDBQueryExpression<SkooleventsDO>();

                    PaginatedQueryList<SkooleventsDO> results = dynamoDBMapper.query(SkooleventsDO.class, queryExpression);
                    */

                    /*
                     *   http://stackoverflow.com/questions/30840093/how-to-do-query-in-dynamodb-on-the-basis-of-hashkey-and-range-key
                     *
                     *   this is what we need to do:
                     *
                     *   http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html#DDB-Query-request-KeyConditionExpression
                     *
                     */

                    //PaginatedScanList<SkooleventsDO> results ;//= dynamoDBMapper.scan(SkooleventsDO.class, scanExpression);






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


                    /*if (results != null) {
                        Iterator<SkooleventsDO> resultsIterator = results.iterator();
                        if (resultsIterator.hasNext()) {
                            return true;
                        }
                    }*/



                    if (results != null && results.size() != 0) {

                        //results.sort(new EventComparator());

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
                    //lastException = ex;
                } catch (Exception e) {
                    {
                        e.printStackTrace();
                    }
                }
            }

    }




    /**
     *
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
    void showLoadingMessage() {

        String[] myitems = { "Loading events..." };

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


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    /*public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
    */



    /** Called when the user clicks the Send button */
    public void createScreen(View view) {
        //Intent homeIntent = new Intent(MainActivity.this, activity_create.class);
        //MainActivity.this.startActivity(homeIntent);

        Log.v("ahleo", "sowel");




    }

}
