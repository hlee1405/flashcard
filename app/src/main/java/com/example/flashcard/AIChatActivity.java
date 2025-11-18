package com.example.flashcard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.BuildConfig;
import com.example.flashcard.adapter.ChatMessageAdapter;
import com.example.flashcard.model.ChatMessage;
import com.example.flashcard.util.GPTApiService;

import java.util.ArrayList;
import java.util.List;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private android.widget.Button btnSend;
    private ContentLoadingProgressBar progressBar;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messages;
    private GPTApiService gptApiService;
    private String configApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.green_primary, null));
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        
        setContentView(R.layout.activity_ai_chat);

        gptApiService = new GPTApiService();
        configApiKey = BuildConfig.OPENAI_API_KEY;
        
        if (TextUtils.isEmpty(configApiKey)) {
            Toast.makeText(this, "API Key chưa được cấu hình. Vui lòng cấu hình API Key trong build.gradle.", Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AI Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar.getNavigationIcon() != null) {
            android.graphics.drawable.Drawable navigationIcon = toolbar.getNavigationIcon();
            navigationIcon = DrawableCompat.wrap(navigationIcon);
            DrawableCompat.setTint(navigationIcon, getResources().getColor(R.color.white, null));
            toolbar.setNavigationIcon(navigationIcon);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        messages = new ArrayList<>();
        adapter = new ChatMessageAdapter(messages);
        
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(adapter);
        
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getAction() == android.view.KeyEvent.ACTION_DOWN 
                    && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER 
                    && !event.isShiftPressed()) {
                sendMessage();
                return true;
            }
            return false;
        });

        addWelcomeMessage();

        View inputContainer = findViewById(R.id.inputContainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            int top = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottom = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            
            if (toolbar != null) {
                float density = getResources().getDisplayMetrics().density;
                int paddingTopDp = (int) (8 * density);
                toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    top + paddingTopDp,
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom()
                );
            }
            
            if (inputContainer != null) {
                float density = getResources().getDisplayMetrics().density;
                
                int extraPadding = (int) (120 * density);
                int totalPaddingBottom = bottom + extraPadding;
                
                if (bottom == 0) {
                    totalPaddingBottom = (int) (160 * density);
                }
                
                int minPadding = (int) (180 * density);
                totalPaddingBottom = Math.max(totalPaddingBottom, minPadding);
                
                Log.d("AIChatActivity", "Bottom insets: " + bottom + ", Total padding: " + totalPaddingBottom);
                
                inputContainer.setPadding(
                    inputContainer.getPaddingLeft(),
                    inputContainer.getPaddingTop(),
                    inputContainer.getPaddingRight(),
                    totalPaddingBottom
                );
                
                android.view.ViewGroup.MarginLayoutParams params = 
                    (android.view.ViewGroup.MarginLayoutParams) inputContainer.getLayoutParams();
                if (params != null) {
                    params.bottomMargin = 0;
                    inputContainer.setLayoutParams(params);
                }
                
                android.view.View linearLayout = ((android.view.ViewGroup) inputContainer).getChildAt(0);
                if (linearLayout != null) {
                    int linearLayoutPadding = (int) (48 * density);
                    linearLayout.setPadding(
                        linearLayout.getPaddingLeft(),
                        linearLayout.getPaddingTop(),
                        linearLayout.getPaddingRight(),
                        linearLayoutPadding
                    );
                }
            }
            
            recyclerViewChat.setPadding(
                recyclerViewChat.getPaddingLeft(),
                recyclerViewChat.getPaddingTop(),
                recyclerViewChat.getPaddingRight(),
                bottom > 0 ? bottom + (int)(16 * getResources().getDisplayMetrics().density) : (int)(16 * getResources().getDisplayMetrics().density)
            );
            
            return windowInsets;
        });
    }

    private void addWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
            ChatMessage.Sender.AI,
            "Xin chào! Tôi là trợ lý AI giúp bạn học tiếng Anh. "
        );
        messages.add(welcomeMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        
        if (TextUtils.isEmpty(configApiKey)) {
            Toast.makeText(this, "API Key chưa được cấu hình. Vui lòng cấu hình API Key.", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage userMessage = new ChatMessage(ChatMessage.Sender.USER, messageText);
        messages.add(userMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        etMessage.setText("");
        
        etMessage.setEnabled(false);
        btnSend.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        gptApiService.setApiKey(configApiKey);
        gptApiService.sendChatMessage(messageText, new GPTApiService.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    ChatMessage aiMessage = new ChatMessage(ChatMessage.Sender.AI, response);
                    messages.add(aiMessage);
                    adapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();
                    
                    etMessage.setEnabled(true);
                    btnSend.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    ChatMessage errorMessage = new ChatMessage(
                        ChatMessage.Sender.AI,
                        "Xin lỗi, đã có lỗi xảy ra: " + error
                    );
                    messages.add(errorMessage);
                    adapter.notifyItemInserted(messages.size() - 1);
                    scrollToBottom();
                    
                    etMessage.setEnabled(true);
                    btnSend.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    
                    Toast.makeText(AIChatActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            recyclerViewChat.post(() -> {
                recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
            });
        }
    }
}

