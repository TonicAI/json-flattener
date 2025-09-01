package ai.tonic.fabricate.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with the Fabricate API to generate and download JSONL data.
 */
public class FabricateClient {
    private static final String DEFAULT_API_URL = "https://fabricate.mockaroo.com/api/v1";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;
    
    public FabricateClient(String apiKey) {
        this(apiKey, DEFAULT_API_URL);
    }
    
    public FabricateClient(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Downloads JSONL data for a specific entity from Fabricate API.
     * 
     * @param workspace The workspace name
     * @param database The database name
     * @param entity The entity/table name
     * @param outputPath The path where to save the downloaded file
     * @return The path to the downloaded file
     * @throws IOException If there's an error during the API call or file download
     */
    public String downloadJsonlData(String workspace, String database, String entity, String outputPath) throws IOException {
        System.out.println(String.format("Generating data for table %s of database %s in JSONL format using %s...", 
                entity, database, apiUrl));
        
        // Create the generate task
        String taskId = createGenerateTask(workspace, database, entity);
        System.out.println(String.format("Started generating data for database %s... task id: %s", database, taskId));
        
        // Poll for completion
        GenerateTaskResult result = pollForCompletion(taskId);
        
        if (result.error != null) {
            throw new IOException("Fabricate API returned an error: " + result.error);
        }
        
        if (result.dataUrl != null) {
            System.out.println(String.format("Downloading data from %s...", result.dataUrl));
            downloadFile(result.dataUrl, outputPath);
            System.out.println(String.format("Data has been downloaded to %s.", outputPath));
            return outputPath;
        } else {
            throw new IOException("No data URL returned from Fabricate API");
        }
    }
    
    /**
     * Creates a generate task via the Fabricate API.
     */
    private String createGenerateTask(String workspace, String database, String entity) throws IOException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("format", "jsonl");
        requestBody.put("database", database);
        requestBody.put("workspace", workspace);
        requestBody.put("entity", entity);
                
        String requestBodyStr = requestBody.toString();
        RequestBody body = RequestBody.create(requestBodyStr, JSON);
        Request request = new Request.Builder()
                .url(apiUrl + "/generate_tasks")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                System.err.println("API Error Response Body: " + responseBody);
                throw new IOException("Failed to create generate task: " + response.code() + " " + response.message() + "\nResponse: " + responseBody);
            }
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            if (jsonResponse.has("error") && !jsonResponse.get("error").isNull()) {
                throw new IOException("API error: " + jsonResponse.get("error").asText());
            }
            
            return jsonResponse.get("id").asText();
        }
    }
    
    /**
     * Polls the generate task until completion.
     */
    private GenerateTaskResult pollForCompletion(String taskId) throws IOException {
        while (true) {
            Request request = new Request.Builder()
                    .url(apiUrl + "/generate_tasks/" + taskId)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) {
                    System.err.println("Polling Error Response Body: " + responseBody);
                    throw new IOException("Failed to poll task: " + response.code() + " " + response.message() + "\nResponse: " + responseBody);
                }
                
                JsonNode task = objectMapper.readTree(responseBody);
                
                if (task.get("completed").asBoolean()) {
                    GenerateTaskResult result = new GenerateTaskResult();
                    if (task.has("data_url") && !task.get("data_url").isNull()) {
                        result.dataUrl = task.get("data_url").asText();
                    }
                    if (task.has("error") && !task.get("error").isNull()) {
                        result.error = task.get("error").asText();
                    }
                    return result;
                } else if (task.has("error") && !task.get("error").isNull()) {
                    throw new IOException("Task error: " + task.get("error").asText());
                } else {
                    int progress = task.has("progress") ? task.get("progress").asInt() : 0;
                    System.out.println(String.format("Waiting for %s to complete... %d%%", taskId, progress));
                    
                    try {
                        Thread.sleep(1000); // Wait 1 second before polling again
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Polling interrupted", e);
                    }
                }
            }
        }
    }
    
    /**
     * Downloads a file from the given URL to the specified path.
     */
    private void downloadFile(String url, String outputPath) throws IOException {
        // Ensure the directory exists
        Path path = Paths.get(outputPath);
        Path parentDir = path.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // For download failures, we might not want to read the entire body if it's large
                String errorInfo = "Failed to download file: " + response.code() + " " + response.message();
                System.err.println("Download Error: " + errorInfo);
                throw new IOException(errorInfo);
            }
            
            try (InputStream inputStream = response.body().byteStream();
                 FileOutputStream outputStream = new FileOutputStream(outputPath)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }
    
    /**
     * Result object for generate task polling.
     */
    private static class GenerateTaskResult {
        String dataUrl;
        String error;
    }
    
    /**
     * Closes the HTTP client and releases resources.
     */
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
}
