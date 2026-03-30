package com.example.fitbiteapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;

    private SessionManager session;
    private String personalizedSystemPrompt;

    // 1. Initialize OkHttpClient with longer timeouts for AI responses
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        session = new SessionManager(this);
        buildSystemPrompt();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        boolean fromBmiCalc = getIntent().getBooleanExtra("from_bmi_calculator", false);

        if (fromBmiCalc) {
            float passedBmi = getIntent().getFloatExtra("passed_bmi", 0.0f);
            float passedWeight = getIntent().getFloatExtra("passed_weight", 0.0f);
            float passedHeight = getIntent().getFloatExtra("passed_height", 0.0f);

            String autoPrompt = String.format(java.util.Locale.getDefault(),
                    "I just calculated a BMI. The weight is %.1f kg, the height is %.1f cm, resulting in a BMI of %.1f. " +
                            "Please provide a diet and lifestyle plan based STRICTLY on these metrics alone. Ignore my general profile data and goals for this specific request.",
                    passedWeight, passedHeight, passedBmi);

            String displayMessage = String.format(java.util.Locale.getDefault(),
                    "Can you give me a plan for a weight of %.1f kg and height of %.1f cm (BMI: %.1f)?",
                    passedWeight, passedHeight, passedBmi);

            addMessageToChat(displayMessage, "user");

            sendMessageToGroq(autoPrompt);

        } else {
            String greeting = "Hello " + session.getUserName() + "! I am your FitBite nutrition assistant. How can I help you reach your " + session.getUserGoal().toLowerCase() + " goals today?";
            addMessageToChat(greeting, "assistant");
        }

        btnSend.setOnClickListener(v -> {
            String userText = etMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                addMessageToChat(userText, "user");
                etMessage.setText("");
                sendMessageToGroq(userText);
            }
        });
    }

    private void buildSystemPrompt() {
        android.content.SharedPreferences prefs = getSharedPreferences("SupabaseSession", MODE_PRIVATE);
        int age = prefs.getInt("user_age", 0);
        String gender = prefs.getString("user_gender", "Not specified");
        String activity = prefs.getString("user_activity", "Not specified");

        personalizedSystemPrompt = String.format(
                "You are a helpful, expert nutritionist assistant inside the FitBite mobile app. Keep answers brief, encouraging, and highly practical. " +
                        "You have access to the user's profile to help them if needed. User Name: %s, Age: %d, Gender: %s, Height: %.1f cm, Weight: %.1f kg, Activity Level: %s, Primary Goal: %s. " +
                        "IMPORTANT INSTRUCTION: " +
                        "If the user asks a general question about diet, food, or exercise, answer it normally using standard nutritional guidelines. " +
                        "ONLY use their personal profile data to tailor your response when they ask for specific advice, meal plans, or when their stats and goals are directly relevant to their question. " +
                        "Do not force their personal details into the conversation unnecessarily.",
                session.getUserName(),
                age,
                gender,
                session.getUserHeight(),
                session.getUserWeight(),
                activity,
                session.getUserGoal()
        );
    }

    private void addMessageToChat(String text, String role) {
        messageList.add(new ChatMessage(text, role));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void sendMessageToGroq(String newQuery) {
        addMessageToChat("Thinking...", "assistant");
        final int loadingIndex = messageList.size() - 1;

        String apiUrl = "https://api.groq.com/openai/v1/chat/completions";
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("model", config.MODEL);
            jsonBody.put("temperature", 0.7);

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            Log.d("ChatActivity", "Personalized System Prompt: " + personalizedSystemPrompt);

            systemMsg.put("content", personalizedSystemPrompt);
            messages.put(systemMsg);

            int historyLimit = Math.max(0, messageList.size() - 21);
            for (int i = historyLimit; i < messageList.size() - 1; i++) {
                ChatMessage msg = messageList.get(i);
                JSONObject historyItem = new JSONObject();
                historyItem.put("role", msg.getRole());
                historyItem.put("content", msg.getText());
                messages.put(historyItem);
            }

            jsonBody.put("messages", messages);

        } catch (JSONException e) {
            e.printStackTrace();
            handleError(loadingIndex);
            return;
        }

        // 2. Build OkHttp Request
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + config.key)
                .post(body)
                .build();

        // 3. Execute Async Call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ChatAPI", "OkHttp Network Error", e);
                // OkHttp callbacks are on a background thread, so we MUST run UI updates on UI thread
                runOnUiThread(() -> handleError(loadingIndex));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String aiReply = jsonResponse.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        // Update UI on main thread
                        runOnUiThread(() -> {
                            messageList.set(loadingIndex, new ChatMessage(aiReply.trim(), "assistant"));
                            adapter.notifyItemChanged(loadingIndex);
                            recyclerView.smoothScrollToPosition(messageList.size() - 1);
                        });

                    } catch (JSONException e) {
                        Log.e("ChatAPI", "JSON Parse Error", e);
                        runOnUiThread(() -> handleError(loadingIndex));
                    }
                } else {
                    Log.e("ChatAPI", "API Error: " + response.code());
                    runOnUiThread(() -> handleError(loadingIndex));
                }
            }
        });
    }

    private void handleError(int indexToRemove) {
        if (indexToRemove >= 0 && indexToRemove < messageList.size()) {
            messageList.remove(indexToRemove);
            adapter.notifyItemRemoved(indexToRemove);
            Toast.makeText(this, "Failed to get response. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}