package ai.tonic.fabricate.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * A utility class for flattening JSON structures from JSONL files.
 * Converts nested JSON objects and arrays into a flat structure with
 * structure markers indicating object and array boundaries.
 */
public class JsonFlattener {
    private final ObjectMapper objectMapper;
    
    public JsonFlattener() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Processes a JSONL file and returns a list of flattened JSON structures.
     * 
     * @param filePath Path to the JSONL file to process
     * @return List of flattened JSON structures
     * @throws IOException If there's an error reading the file
     */
    public List<Map<String, Object>> processJsonlFile(String filePath) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    JsonNode jsonNode = objectMapper.readTree(line);
                    Map<String, Object> flattenedRecord = flattenJsonNode(jsonNode);
                    result.add(flattenedRecord);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Converts a JSON node to a flattened structure with fields array.
     * 
     * @param jsonNode The JSON node to flatten
     * @return A map containing an "id" and a "fields" array with flattened key-value pairs
     */
    private Map<String, Object> flattenJsonNode(JsonNode jsonNode) {
        Map<String, Object> flattenedRecord = new HashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // Generate a unique ID for this record
        String recordId = java.util.UUID.randomUUID().toString().replace("-", "");
        flattenedRecord.put("id", recordId);
        
        // Process the root object - iterate through all root-level fields
        if (jsonNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = jsonNode.fields();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                flattenNode(value, key, fields);
            }
        } else {
            flattenNode(jsonNode, "", fields);
        }
        
        flattenedRecord.put("fields", fields);
        return flattenedRecord;
    }
    
    /**
     * Recursively flattens a JSON node, adding entries to the fields list.
     * 
     * @param node The JSON node to flatten
     * @param prefix The current key prefix (for nested structures)
     * @param fields The list to add flattened key-value pairs to
     */
    private void flattenNode(JsonNode node, String prefix, List<Map<String, Object>> fields) {
        if (node.isObject()) {
            // Add a special entry for the object itself
            Map<String, Object> objectField = new HashMap<>();
            objectField.put("key", prefix);
            objectField.put("value", "Structure");
            fields.add(objectField);
            
            // Then process all the object's fields
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = node.fields();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                flattenNode(value, newPrefix, fields);
            }
            
            // Add end marker for object
            Map<String, Object> endObjectField = new HashMap<>();
            endObjectField.put("key", prefix + ".");
            endObjectField.put("value", "EndStructure");
            fields.add(endObjectField);
        } else if (node.isArray()) {
            // Add array length indicator
            Map<String, Object> arrayLengthField = new HashMap<>();
            arrayLengthField.put("key", prefix);
            arrayLengthField.put("value", String.valueOf(node.size()));
            fields.add(arrayLengthField);
            
            // Process array elements
            for (int i = 0; i < node.size(); i++) {
                String newPrefix = prefix + "[" + i + "]";
                flattenNode(node.get(i), newPrefix, fields);
            }
            
            // Add end marker for array
            Map<String, Object> endArrayField = new HashMap<>();
            endArrayField.put("key", prefix + ".");
            endArrayField.put("value", "EndArray");
            fields.add(endArrayField);
        } else {
            // Primitive value
            Map<String, Object> field = new HashMap<>();
            field.put("key", prefix);
            field.put("value", getNodeValue(node));
            fields.add(field);
        }
    }
    
    /**
     * Extracts the appropriate Java value from a JSON node.
     * 
     * @param node The JSON node to extract value from
     * @return The Java object representing the node's value
     */
    private Object getNodeValue(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            if (node.isInt()) {
                return node.asInt();
            } else if (node.isLong()) {
                return node.asLong();
            } else if (node.isDouble()) {
                return node.asDouble();
            } else {
                return node.numberValue();
            }
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isNull()) {
            return null;
        } else {
            return node.asText();
        }
    }
    
    /**
     * Converts the flattened data to a pretty-printed JSON string.
     * 
     * @param flattenedData The flattened data to convert
     * @return Pretty-printed JSON string
     * @throws IOException If there's an error during JSON serialization
     */
    public String toPrettyJson(List<Map<String, Object>> flattenedData) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(flattenedData);
    }
}
