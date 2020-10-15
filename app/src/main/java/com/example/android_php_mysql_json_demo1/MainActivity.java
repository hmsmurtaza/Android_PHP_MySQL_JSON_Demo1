package com.example.android_php_mysql_json_demo1;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private String spinnerSelectedItemText, text;
    private int checkedItemId;
    private TextInputEditText mUserName, mPassword, mConfirmPassword;
    private Button mLocation, mSubmit;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButtonMale, mRadioButtonFemale;
    private ProgressBar mProgressBar;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserName = findViewById(R.id.name);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.confirmPassword);

        mRadioGroup = findViewById(R.id.genderRadioGroup);
        mRadioButtonMale = findViewById(R.id.radioButtonMale);
        mRadioButtonFemale = findViewById(R.id.radioButtonFemale);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (mRadioButtonMale.isChecked()) {
                    text = "male";
                }
                if (mRadioButtonFemale.isChecked()) {
                    text = "female";
                }
            }
        });

        mSpinner = findViewById(R.id.userTypeSpinner);
        mSpinner.setOnItemSelectedListener(MainActivity.this);
        ArrayAdapter<CharSequence> userTypeAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.user_types, android.R.layout.simple_spinner_dropdown_item);
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSpinner.setAdapter(userTypeAdapter);

        mLocation = findViewById(R.id.buttonLocation);
        mSubmit = findViewById(R.id.buttonSubmitData);

        mProgressBar = findViewById(R.id.progressBarLocation);
        mProgressBar.setVisibility(View.INVISIBLE);
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            String name = mUserName.getText().toString();
            String password = mPassword.getText().toString();
            String confirmPassword = mConfirmPassword.getText().toString();
            String male = mRadioButtonMale.getText().toString();
            String female = mRadioButtonFemale.getText().toString();

            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "" +
                         name + " " +
                         password + " " +
                         confirmPassword + " " +
                         male + " " +
                        female + " " +
                        spinnerSelectedItemText + " " +
                        " checked text " + text, Toast.LENGTH_SHORT).show();
                new ServerData().execute(name, password, confirmPassword, male, female, spinnerSelectedItemText);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // both lines work
        spinnerSelectedItemText = String.valueOf(mSpinner.getSelectedItem());
        // spinnerSelectedItemText = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Toast.makeText(this, "no item selected", Toast.LENGTH_SHORT).show();
    }

    private class ServerData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        HttpURLConnection httpURLConnection;
        URL url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Reading data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL("http://192.168.10.2/skilled-person/show_users.php");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder();
                builder.appendQueryParameter("name", strings[0]);
                builder.appendQueryParameter("password", strings[1]);
                builder.appendQueryParameter("confirm_assword", strings[2]);
                builder.appendQueryParameter("gender", strings[3]);
                builder.appendQueryParameter("customer_type", strings[4]);
                builder.appendQueryParameter("location", strings[5]);
                String query = builder.build().getEncodedQuery();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(query);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                httpURLConnection.connect();

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    return (stringBuilder.toString());
                } else {
                    return "Unsuccessful";
                }


            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                httpURLConnection.disconnect();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if (result.equalsIgnoreCase("true")) {
                Toast.makeText(MainActivity.this, "successful", Toast.LENGTH_SHORT).show();
            } else if (result.equalsIgnoreCase("false")) {
                Toast.makeText(MainActivity.this, "failed, enter correct email or password", Toast.LENGTH_SHORT).show();
            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(MainActivity.this, "Oops! something went wrong, Connection problem", Toast.LENGTH_SHORT).show();
            }
        }
    }

}