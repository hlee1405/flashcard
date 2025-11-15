package com.example.flashcard.util;

import android.util.Log;

import com.example.flashcard.model.Word;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

public class GPTApiService {
    private static final String TAG = "GPTApiService";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private String apiKey;
    
    public interface GPTResponseCallback {
        void onSuccess(List<Word> words);
        void onError(String error);
    }
    
    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public GPTApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public void generateVocabulary(String topicOrWords, int wordCount, GPTResponseCallback callback) {
        generateVocabulary(topicOrWords, wordCount, "", "Mặc định", callback);
    }

    public void generateVocabulary(String topicOrWords, int wordCount, String interests, String style, GPTResponseCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API Key chưa được cấu hình. Vui lòng nhập API Key trong cài đặt.");
            return;
        }
        
        String prompt = buildPrompt(topicOrWords, wordCount, interests, style);
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        // Increase max_tokens to handle larger responses (estimate ~200 tokens per word)
        requestBody.addProperty("max_tokens", Math.max(4000, wordCount * 250));
        
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed", e);
                callback.onError("Lỗi kết nối: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "API error: " + response.code() + " - " + errorBody);
                    callback.onError("Lỗi API: " + response.code() + ". " + errorBody);
                    return;
                }
                
                String responseBody = response.body().string();
                try {
                    List<Word> words = parseResponse(responseBody);
                    callback.onSuccess(words);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    callback.onError("Lỗi phân tích dữ liệu: " + e.getMessage());
                }
            }
        });
    }
    
    private String buildPrompt(String topicOrWords, int wordCount) {
        return buildPrompt(topicOrWords, wordCount, "", "Mặc định");
    }

    private String buildPrompt(String topicOrWords, int wordCount, String interests, String style) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một giáo viên tiếng Anh chuyên nghiệp. Hãy tạo ").append(wordCount)
              .append(" từ vựng tiếng Anh dựa trên chủ đề hoặc từ khóa sau: \"").append(topicOrWords).append("\"\n\n");
        
        // Thêm yêu cầu về sở thích nếu có
        if (interests != null && !interests.trim().isEmpty()) {
            prompt.append("QUAN TRỌNG - Cá nhân hóa theo sở thích:\n");
            prompt.append("Người học có sở thích về: ").append(interests).append("\n");
            prompt.append("Hãy tạo các ví dụ minh họa liên quan đến sở thích này. ");
            prompt.append("Ví dụ: nếu sở thích là \"bóng đá\", hãy tạo ví dụ về bóng đá. ");
            prompt.append("Nếu sở thích là \"phim ảnh\", hãy tạo ví dụ về phim ảnh.\n\n");
        }
        
        // Thêm yêu cầu về phong cách
        if (style != null && !style.equals("Mặc định")) {
            prompt.append("QUAN TRỌNG - Phong cách giải thích:\n");
            switch (style) {
                case "Đơn giản":
                    prompt.append("Hãy giải thích từ vựng một cách đơn giản, dễ hiểu, dùng từ ngữ thông thường. ");
                    prompt.append("Mẹo ghi nhớ nên ngắn gọn, dễ nhớ.\n\n");
                    break;
                case "Hài hước":
                    prompt.append("Hãy giải thích từ vựng một cách hài hước, vui vẻ. ");
                    prompt.append("Mẹo ghi nhớ nên có yếu tố hài hước, có thể dùng câu chuyện vui hoặc so sánh thú vị.\n\n");
                    break;
                case "Học thuật":
                    prompt.append("Hãy giải thích từ vựng một cách học thuật, chuyên nghiệp. ");
                    prompt.append("Mẹo ghi nhớ nên có yếu tố phân tích, logic, phù hợp với người học nghiêm túc.\n\n");
                    break;
                case "Trẻ em":
                    prompt.append("Hãy giải thích từ vựng một cách đơn giản, dễ thương, phù hợp với trẻ em. ");
                    prompt.append("Mẹo ghi nhớ nên có yếu tố vui nhộn, có thể dùng hình ảnh, câu chuyện dễ thương.\n\n");
                    break;
            }
        }
        
        prompt.append("Yêu cầu:\n");
        prompt.append("1. Mỗi từ vựng phải có đầy đủ thông tin:\n");
        prompt.append("   - Từ tiếng Anh\n");
        prompt.append("   - Nghĩa tiếng Việt\n");
        prompt.append("   - Phiên âm (IPA)\n");
        prompt.append("   - Ví dụ minh họa bằng tiếng Anh");
        if (interests != null && !interests.trim().isEmpty()) {
            prompt.append(" (liên quan đến sở thích: ").append(interests).append(")");
        }
        prompt.append("\n");
        prompt.append("   - Mẹo ghi nhớ (bằng tiếng Việt");
        if (!style.equals("Mặc định")) {
            prompt.append(", theo phong cách ").append(style);
        }
        prompt.append(")\n\n");
        prompt.append("2. Trả về kết quả dưới dạng JSON array với format:\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"english\": \"từ tiếng Anh\",\n");
        prompt.append("    \"vietnamese\": \"nghĩa tiếng Việt\",\n");
        prompt.append("    \"pronunciation\": \"/phiên âm IPA/\",\n");
        prompt.append("    \"example\": \"Ví dụ câu tiếng Anh\",\n");
        prompt.append("    \"memoryTip\": \"Mẹo ghi nhớ bằng tiếng Việt\"\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        prompt.append("Chỉ trả về JSON array, không có text thêm nào khác.");
        
        return prompt.toString();
    }
    
    private List<Word> parseResponse(String responseBody) throws Exception {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray choices = jsonResponse.getAsJsonArray("choices");
        
        if (choices == null || choices.size() == 0) {
            throw new Exception("Không có dữ liệu từ API");
        }
        
        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject message = firstChoice.getAsJsonObject("message");
        String content = message.get("content").getAsString();
        
        // Loại bỏ markdown code blocks nếu có
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        }
        if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        content = content.trim();
        
        // Log the content for debugging (truncated if too long)
        if (content.length() > 500) {
            Log.d(TAG, "Response content (first 500 chars): " + content.substring(0, 500));
            Log.d(TAG, "Response content (last 500 chars): " + content.substring(Math.max(0, content.length() - 500)));
        } else {
            Log.d(TAG, "Response content: " + content);
        }
        
        // Check if JSON appears incomplete (doesn't end with ])
        if (!content.trim().endsWith("]")) {
            Log.w(TAG, "JSON response appears incomplete. Attempting to fix...");
            // Try to find the last complete JSON object
            int lastCompleteBrace = content.lastIndexOf("}");
            if (lastCompleteBrace > 0) {
                // Find the start of the last object
                int arrayStart = content.indexOf("[");
                if (arrayStart >= 0 && lastCompleteBrace > arrayStart) {
                    content = content.substring(0, lastCompleteBrace + 1) + "]";
                    Log.d(TAG, "Attempted to fix incomplete JSON");
                }
            }
        }
        
        JsonArray wordsArray;
        try {
            wordsArray = JsonParser.parseString(content).getAsJsonArray();
        } catch (com.google.gson.JsonSyntaxException e) {
            Log.e(TAG, "JSON parsing failed. Content length: " + content.length());
            Log.e(TAG, "Content preview: " + (content.length() > 200 ? content.substring(0, 200) : content));
            throw new Exception("JSON không hợp lệ hoặc không đầy đủ. Có thể do phản hồi từ API bị cắt ngắn. " + e.getMessage());
        }
        
        List<Word> words = new ArrayList<>();
        
        for (JsonElement element : wordsArray) {
            try {
                JsonObject wordObj = element.getAsJsonObject();
                
                // Check if required fields exist
                if (!wordObj.has("english") || !wordObj.has("vietnamese")) {
                    Log.w(TAG, "Skipping incomplete word object: " + wordObj);
                    continue;
                }
                
                String english = wordObj.get("english").getAsString();
                String vietnamese = wordObj.get("vietnamese").getAsString();
                String pronunciation = wordObj.has("pronunciation") ? 
                        wordObj.get("pronunciation").getAsString() : "";
                String example = wordObj.has("example") ? 
                        wordObj.get("example").getAsString() : "";
                String memoryTip = wordObj.has("memoryTip") ? 
                        wordObj.get("memoryTip").getAsString() : "";
                
                words.add(new Word(english, vietnamese, pronunciation, example, memoryTip));
            } catch (Exception e) {
                Log.w(TAG, "Error parsing word object, skipping: " + e.getMessage());
                // Continue with next word instead of failing completely
            }
        }
        
        if (words.isEmpty()) {
            throw new Exception("Không thể trích xuất từ vựng từ phản hồi. JSON có thể bị lỗi hoặc không đầy đủ.");
        }
        
        Log.d(TAG, "Successfully parsed " + words.size() + " words from response");
        return words;
    }
    
    public void sendChatMessage(String userMessage, ChatCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API Key chưa được cấu hình. Vui lòng nhập API Key trong cài đặt.");
            return;
        }
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");
        
        JsonArray messages = new JsonArray();
        
        // System message để AI hiểu context
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", 
            "Bạn là trợ lý AI chuyên giúp học tiếng Anh. " +
            "Bạn có thể tạo từ vựng, giải thích từ, tạo ví dụ, hội thoại, " +
            "hoặc trả lời bất kỳ câu hỏi nào về tiếng Anh. " +
            "Hãy trả lời bằng tiếng Việt trừ khi được yêu cầu khác.");
        messages.add(systemMessage);
        
        // User message
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 2000);
        
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Chat API call failed", e);
                callback.onError("Lỗi kết nối: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Chat API error: " + response.code() + " - " + errorBody);
                    callback.onError("Lỗi API: " + response.code() + ". " + errorBody);
                    return;
                }
                
                String responseBody = response.body().string();
                try {
                    String content = parseChatResponse(responseBody);
                    callback.onSuccess(content);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing chat response", e);
                    callback.onError("Lỗi phân tích dữ liệu: " + e.getMessage());
                }
            }
        });
    }
    
    private String parseChatResponse(String responseBody) throws Exception {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray choices = jsonResponse.getAsJsonArray("choices");
        
        if (choices == null || choices.size() == 0) {
            throw new Exception("Không có dữ liệu từ API");
        }
        
        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject message = firstChoice.getAsJsonObject("message");
        String content = message.get("content").getAsString();
        
        return content.trim();
    }
}

