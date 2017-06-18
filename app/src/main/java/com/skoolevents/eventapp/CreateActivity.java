package com.skoolevents.eventapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.models.nosql.SkooleventsDO;
//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.appindexing.Thing;
//import com.google.android.gms.common.api.GoogleApiClient;
import com.skoolevents.eventapp.R;

import java.util.Calendar;
import java.util.Date;

import static android.provider.Telephony.Carriers.PASSWORD;
import static android.text.TextUtils.isEmpty;
//import static com.skoolevents.eventapp.R.id.date;


/**
 *
 */
public class CreateActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SchoolEventsCreate";


    // >>>>>>>>>>>>>>>>>>>>>>>>
    // >>> Date Logic START >>>
    // >>>>>>>>>>>>>>>>>>>>>>>>

    Button btn;
    int year_x = 0, month_x = 0, day_x = 0;
    static final int DATE_DIALOG_ID = 10;
    boolean dateEntered = false;


    public void showDialogOnButtonClick() {
        btn = (Button)findViewById(R.id.btndate);

        btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DATE_DIALOG_ID);
                    }
                }
        );
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        if (id == DATE_DIALOG_ID) {
            return new DatePickerDialog(this, dpickerListener, year_x, month_x, day_x);
        }
        if (id == TIME_DIALOG_ID) {
            return new TimePickerDialog(CreateActivity.this, kTimePickerListener, hour_x, minute_x, false);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Log.v("@@@ >>>", "CreateActivity::DatePickerDialog.OnDateSetListener -- onDateSet  @@@@@@@@@@@");

            dateEntered = true;
            year_x = year;
            month_x = monthOfYear;
            day_x = dayOfMonth;
            Toast.makeText(CreateActivity.this, year_x + "/" + month_x + "/" + day_x, Toast.LENGTH_LONG).show();
        }
    };

    // <<<<<<<<<<<<<<<<<<<<<<
    // <<< Date Logic END <<<
    // <<<<<<<<<<<<<<<<<<<<<<




    // end of date code

    // start of time code
    Button buttonstpd;
    static final int TIME_DIALOG_ID = 20;
    int hour_x;
    int minute_x;
    boolean timeEntered = false;

    public void showTimePickerDialog () {
        buttonstpd = (Button)findViewById(R.id.btntime);
        buttonstpd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(TIME_DIALOG_ID);
                    }
                }
        );
    }


    protected TimePickerDialog.OnTimeSetListener kTimePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    Log.v("@@@ >>>", "TimePickerDialog.OnTimeSetListener -- " + hourOfDay + ":" + minute);

                    timeEntered = true;
                    hour_x = hourOfDay;
                    minute_x = minute;

                    Toast.makeText(CreateActivity.this, hour_x + " : " + minute_x, Toast.LENGTH_LONG).show();
                }
            };


    // end of time code





    private EditText nameField, descField, passwordField;
    private String eventTitle, eventDesc, passwordString;
    //private Date eventDate;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        // next 6 lines are for date code
        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        showDialogOnButtonClick();

        // next line is for time code
        showTimePickerDialog();



        Log.v("@@@ >>>", "CreateActivity::onCreate -- >>>>>>>>>>>>>>>");


        android.widget.Button add = (android.widget.Button) findViewById(R.id.submitEventButton);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Log.v("@@@ >>>", "CreateActivity::onCreate -- add.setOnClickListener()" );

                nameField = (EditText) findViewById(R.id.eventName);
                descField = (EditText) findViewById(R.id.eventDesc);
                passwordField = (EditText)  findViewById(R.id.password);

                eventTitle = nameField.getText().toString();
                Log.v("@@@ >>>", "CreateActivity::onCreate -- " + eventTitle);

                passwordString = passwordField.getText().toString();
                Log.v("@@@ >>>", "CreateActivity::onCreate -- Password: " + passwordString);

                eventDesc = descField.getText().toString();
                Log.v("@@@ >>>", "CreateActivity::onCreate -- " + eventDesc);


                Calendar c = Calendar.getInstance();
                c.set(year_x, month_x, day_x, hour_x, minute_x);
                long eventDateTimeInEpoch = c.getTimeInMillis();


                // Validate data
                if ( eventTitle == null || isEmpty(eventTitle)) {
                    Toast.makeText(CreateActivity.this, "Add valid event title", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!dateEntered) {
                    Toast.makeText(CreateActivity.this, "Add valid date", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!timeEntered) {
                    Toast.makeText(CreateActivity.this, "Add valid time", Toast.LENGTH_LONG).show();
                    return;
                }
                if (eventDesc == null || isEmpty(eventDesc)) {
                    Toast.makeText(CreateActivity.this, "Add valid description", Toast.LENGTH_LONG).show();
                    return;
                }
                if (passwordString == null || isEmpty(passwordString) || !("gojtms".equals(passwordString))) {
                    Toast.makeText(CreateActivity.this, "Enter valid password!", Toast.LENGTH_LONG).show();
                    return;
                }


                final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
                final SkooleventsDO firstItem = new SkooleventsDO(); // Initialize the Notes Object

                firstItem.setDate( (Double) new Long(eventDateTimeInEpoch).doubleValue() );
                firstItem.setTitle(eventTitle);
                firstItem.setDescription(eventDesc);


                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            dynamoDBMapper.save(firstItem);
                        } catch (final AmazonClientException ex) {
                            Log.e(LOG_TAG, "Failed saving item : " + ex.getMessage(), ex);
                            //lastException = ex;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();



                Intent mainIntent = new Intent(view.getContext(), com.skoolevents.eventapp.MainActivity.class);
                startActivityForResult(mainIntent, 0);
            }
        });




        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


}
