package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private NasaApiService nasaApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.nasa.gov/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        nasaApiService = retrofit.create(NasaApiService.class);

        Button fetchTodayButton = findViewById(R.id.btnFetchToday);
        Button datePickerButton = findViewById(R.id.btnChooseDate);

        fetchTodayButton.setOnClickListener(v -> fetchPhoto(null));
        datePickerButton.setOnClickListener(v -> showDatePickerDialog());
    }

    private void fetchPhoto(String date) {
        String apiKey = "cTZK2yWWUhz3z8yWEG1x896SB4hELifByRn0MVYB";

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        nasaApiService.getPhotoOfTheDay(apiKey, date).enqueue(new Callback<NasaPhoto>() {
            @Override
            public void onResponse(Call<NasaPhoto> call, Response<NasaPhoto> response) {
                progressBar.setVisibility(ProgressBar.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    NasaPhoto photo = response.body();

                    TextView titleView = findViewById(R.id.titleTextView);
                    TextView descriptionView = findViewById(R.id.descriptionTextView);
                    ImageView imageView = findViewById(R.id.imageView);

                    titleView.setText(photo.getTitle());
                    descriptionView.setText(photo.getExplanation());

                    Picasso.get().load(photo.getUrl()).into(imageView);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load photo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NasaPhoto> call, Throwable t) {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
            fetchPhoto(selectedDate);
            calculateDaysFromToday(selectedDate);
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void calculateDaysFromToday(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date selectedDate = sdf.parse(date);
            Date currentDate = new Date();
            long diff = currentDate.getTime() - selectedDate.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            Toast.makeText(this, days + " days ago", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
