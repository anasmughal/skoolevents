package com.mysampleapp.demo.nosql;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.models.nosql.SkooleventsDO;

import java.util.Set;

public class DemoNoSQLSkooleventsResult implements DemoNoSQLResult {
    private static final int KEY_TEXT_COLOR = 0xFF333333;
    private final SkooleventsDO result;

    DemoNoSQLSkooleventsResult(final SkooleventsDO result) {
        this.result = result;
    }
    @Override
    public void updateItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final String originalValue = result.getDescription();
        result.setDescription(DemoSampleDataGenerator.getRandomSampleString("description"));
        try {
            mapper.save(result);
        } catch (final AmazonClientException ex) {
            // Restore original data if save fails, and re-throw.
            result.setDescription(originalValue);
            throw ex;
        }
    }

    @Override
    public void deleteItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        mapper.delete(result);
    }

    private void setKeyTextViewStyle(final TextView textView) {
        textView.setTextColor(KEY_TEXT_COLOR);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(5), dp(2), dp(5), 0);
        textView.setLayoutParams(layoutParams);
    }

    /**
     * @param dp number of design pixels.
     * @return number of pixels corresponding to the desired design pixels.
     */
    private int dp(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    private void setValueTextViewStyle(final TextView textView) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(15), 0, dp(15), dp(2));
        textView.setLayoutParams(layoutParams);
    }

    private void setKeyAndValueTextViewStyles(final TextView keyTextView, final TextView valueTextView) {
        setKeyTextViewStyle(keyTextView);
        setValueTextViewStyle(valueTextView);
    }

    private static String bytesToHexString(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("%02X", bytes[0]));
        for(int index = 1; index < bytes.length; index++) {
            builder.append(String.format(" %02X", bytes[index]));
        }
        return builder.toString();
    }

    private static String byteSetsToHexStrings(Set<byte[]> bytesSet) {
        final StringBuilder builder = new StringBuilder();
        int index = 0;
        for (byte[] bytes : bytesSet) {
            builder.append(String.format("%d: ", ++index));
            builder.append(bytesToHexString(bytes));
            if (index < bytesSet.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public View getView(final Context context, final View convertView, int position) {
        final LinearLayout layout;
        final TextView resultNumberTextView;
        final TextView schoolKeyTextView;
        final TextView schoolValueTextView;
        final TextView dateKeyTextView;
        final TextView dateValueTextView;
        final TextView descriptionKeyTextView;
        final TextView descriptionValueTextView;
        final TextView titleKeyTextView;
        final TextView titleValueTextView;
        if (convertView == null) {
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            resultNumberTextView = new TextView(context);
            resultNumberTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(resultNumberTextView);


            schoolKeyTextView = new TextView(context);
            schoolValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(schoolKeyTextView, schoolValueTextView);
            layout.addView(schoolKeyTextView);
            layout.addView(schoolValueTextView);

            dateKeyTextView = new TextView(context);
            dateValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(dateKeyTextView, dateValueTextView);
            layout.addView(dateKeyTextView);
            layout.addView(dateValueTextView);

            descriptionKeyTextView = new TextView(context);
            descriptionValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(descriptionKeyTextView, descriptionValueTextView);
            layout.addView(descriptionKeyTextView);
            layout.addView(descriptionValueTextView);

            titleKeyTextView = new TextView(context);
            titleValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(titleKeyTextView, titleValueTextView);
            layout.addView(titleKeyTextView);
            layout.addView(titleValueTextView);
        } else {
            layout = (LinearLayout) convertView;
            resultNumberTextView = (TextView) layout.getChildAt(0);

            schoolKeyTextView = (TextView) layout.getChildAt(1);
            schoolValueTextView = (TextView) layout.getChildAt(2);

            dateKeyTextView = (TextView) layout.getChildAt(3);
            dateValueTextView = (TextView) layout.getChildAt(4);

            descriptionKeyTextView = (TextView) layout.getChildAt(5);
            descriptionValueTextView = (TextView) layout.getChildAt(6);

            titleKeyTextView = (TextView) layout.getChildAt(7);
            titleValueTextView = (TextView) layout.getChildAt(8);
        }

        resultNumberTextView.setText(String.format("#%d", + position+1));
        schoolKeyTextView.setText("school");
        schoolValueTextView.setText(result.getSchool());
        dateKeyTextView.setText("date");
        dateValueTextView.setText("" + result.getDate().longValue());
        descriptionKeyTextView.setText("description");
        descriptionValueTextView.setText(result.getDescription());
        titleKeyTextView.setText("title");
        titleValueTextView.setText(result.getTitle());
        return layout;
    }
}
