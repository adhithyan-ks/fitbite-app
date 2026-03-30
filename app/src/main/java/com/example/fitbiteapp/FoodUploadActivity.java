package com.example.fitbiteapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FoodUploadActivity extends AppCompatActivity {
    // UI Components
    TextView txtFoodName, txtCalories, txtDescription;
    TextView txtProteinVal, txtCarbsVal, txtFatVal;
    ProgressBar progressProtein, progressCarbs, progressFat, loadingSpinner;
    LinearLayout resultsLayout;
    ImageView img;
    Button openCameraButton, selectImage;

    private Uri selectedImageUri;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 1;

    // 1. Initialize OkHttp with longer timeouts for Image AI processing
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_upload);

        // Bind Views
        img = findViewById(R.id.imgFoodPreview);
        selectImage = findViewById(R.id.btnGallery);
        openCameraButton = findViewById(R.id.btnCamera);

        // New UI Elements
        loadingSpinner = findViewById(R.id.api_loading_progress);
        resultsLayout = findViewById(R.id.resultsLayout);
        txtFoodName = findViewById(R.id.txtFoodName);
        txtCalories = findViewById(R.id.txtCalories);
        txtDescription = findViewById(R.id.txtDescription);

        // Progress Bars & Values
        progressProtein = findViewById(R.id.progressProtein);
        txtProteinVal = findViewById(R.id.txtProteinVal);
        progressCarbs = findViewById(R.id.progressCarbs);
        txtCarbsVal = findViewById(R.id.txtCarbsVal);
        progressFat = findViewById(R.id.progressFat);
        txtFatVal = findViewById(R.id.txtFatVal);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        selectImage.setOnClickListener(v -> openImagePicker());

        openCameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(FoodUploadActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(FoodUploadActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });
    }

    // --- Image Handling Code ---
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
                selectedImageUri = data.getData();
                img.setImageURI(selectedImageUri);
                processImage(selectedImageUri);

            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                img.setImageBitmap(photo);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                detectFoodNutrients(base64Image);
            }
        }
    }

    private void processImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = getBytes(inputStream);
            String base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP);
            detectFoodNutrients(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // --- API Logic (Converted to OkHttp) ---

    private void detectFoodNutrients(String base64Image) {
        // Show loading state
        loadingSpinner.setVisibility(View.VISIBLE);
        resultsLayout.setVisibility(View.GONE);

        base64Image = base64Image.trim().replaceAll("\\s+", "");
        String apiUrl = "https://api.groq.com/openai/v1/chat/completions";
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("model", config.MODEL); // Note: Ensure you are using a vision-capable model (like llama-3.2-11b-vision-preview)
            requestBody.put("temperature", 0.5);
            requestBody.put("max_completion_tokens", 1024);
            requestBody.put("stream", false);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");

            JSONArray contentArray = new JSONArray();

            // 1. TEXT PROMPT asking for JSON
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            String prompt = "Analyze this food image. Identify the food and estimate its nutritional values per serving.\n" +
                    "Return ONLY a valid JSON object (no markdown, no backticks) with these exact keys:\n" +
                    "{\n" +
                    "  \"food_name\": \"string\",\n" +
                    "  \"quantity_description\": \"string (e.g. 1 bowl)\",\n" +
                    "  \"calories\": int,\n" +
                    "  \"protein_g\": int,\n" +
                    "  \"carbs_g\": int,\n" +
                    "  \"fat_g\": int,\n" +
                    "  \"brief_tip\": \"string (short health tip)\"\n" +
                    "}";
            textContent.put("text", prompt);
            contentArray.put(textContent);

            // 2. IMAGE CONTENT
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", String.format("data:image/jpeg;base64,%s", base64Image));
            imageContent.put("image_url", imageUrl);
            contentArray.put(imageContent);

            userMessage.put("content", contentArray);
            messages.put(userMessage);
            requestBody.put("messages", messages);

        } catch (JSONException e) {
            e.printStackTrace();
            loadingSpinner.setVisibility(View.GONE);
            return;
        }

        // 3. Build OkHttp Request
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + config.key)
                .post(body)
                .build();

        // 4. Execute Async Call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GroqAPI", "OkHttp Error: " + e.getMessage());
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(FoodUploadActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        loadingSpinner.setVisibility(View.GONE);
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String content = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            // Clean content just in case the AI adds markdown blocks ```json ... ```
                            content = content.replace("```json", "").replace("```", "").trim();
                            parseAndDisplayNutrients(content);

                        } catch (JSONException e) {
                            Log.e("GroqAPI", "JSON Parse Error", e);
                            Toast.makeText(FoodUploadActivity.this, "Error parsing AI response", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown Error";
                    Log.e("GroqAPI", "API Error: " + response.code() + " - " + errorBody);
                    runOnUiThread(() -> {
                        loadingSpinner.setVisibility(View.GONE);
                        Toast.makeText(FoodUploadActivity.this, "API Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void parseAndDisplayNutrients(String jsonString) {
        try {
            JSONObject data = new JSONObject(jsonString);
            Log.d("JSON_DATA", data.toString());

            String foodName = data.optString("food_name", "Unknown Food");
            String quantity = data.optString("quantity_description", "");
            int calories = data.optInt("calories", 0);
            int protein = data.optInt("protein_g", 0);
            int carbs = data.optInt("carbs_g", 0);
            int fat = data.optInt("fat_g", 0);
            String tip = data.optString("brief_tip", "");

            // Update UI
            resultsLayout.setVisibility(View.VISIBLE);

            txtFoodName.setText(foodName);
            txtCalories.setText(calories + " kcal • " + quantity);

            // Set Progress Bars
            progressProtein.setProgress(protein);
            txtProteinVal.setText(protein + "g");

            progressCarbs.setProgress(carbs);
            txtCarbsVal.setText(carbs + "g");

            progressFat.setProgress(fat);
            txtFatVal.setText(fat + "g");

            txtDescription.setText("AI Tip: " + tip);

        } catch (JSONException e) {
            Log.e("UI_UPDATE", "Error mapping JSON to UI", e);
            Toast.makeText(this, "Failed to display nutrients", Toast.LENGTH_SHORT).show();
        }
    }
}