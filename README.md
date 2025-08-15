# JSON Flattening Tool

A Java application that reads JSONL (JSON Lines) files and converts nested JSON structures into a flat representation with detailed structure markers.

## Features

- Reads JSONL files line by line
- Flattens nested JSON objects and arrays
- Adds structure markers to indicate object and array boundaries
- Supports dot notation for nested objects (e.g., `location.city`)
- Supports array indexing (e.g., `nicknames[0]`, `nicknames[1]`)
- Outputs structured data with begin/end markers for objects and arrays

## Output Format

The tool converts JSON data into a structured format with the following markers:

- **Objects**: Start with `"value": "Structure"` and end with `"value": "EndStructure"`
- **Arrays**: Start with the array length as value and end with `"value": "EndArray"`
- **Primitives**: Direct key-value pairs

### Example

Input JSONL:

```json
{
  "name": "John",
  "location": { "city": "New York" },
  "nicknames": ["Jon-boy", "Johnny"]
}
```

Output:

```json
[
  {
    "fields": [
      { "key": "name", "value": "John" },
      { "key": "location", "value": "Structure" },
      { "key": "location.city", "value": "New York" },
      { "key": "location.", "value": "EndStructure" },
      { "key": "nicknames", "value": "2" },
      { "key": "nicknames[0]", "value": "Jon-boy" },
      { "key": "nicknames[1]", "value": "Johnny" },
      { "key": "nicknames.", "value": "EndArray" }
    ]
  }
]
```

## Prerequisites

- Java 17 or later
- Gradle (or use the included Gradle wrapper)

## Building and Running

### Using Gradle Wrapper (Recommended)

1. **Build the project:**

   ```bash
   ./gradlew build
   ```

2. **Run:**
   ```bash
   ./gradlew run --args="path/to/your/file.jsonl"
   ```

### Using Gradle (if installed globally)

1. **Build the project:**

   ```bash
   gradle build
   ```

2. **Run the application:**
   ```bash
   gradle run --args="path/to/your/file.jsonl"
   ```

## Project Structure

```
<root>/
├── app/
│   ├── src/main/java/ai/tonic/fabricate/tools/
│   │   └── App.java              # Main application logic
│   └── build.gradle              # App dependencies and configuration
├── data/
│   └── example.jsonl             # Sample input file
├── gradle/
│   └── wrapper/                  # Gradle wrapper files
├── gradlew                       # Gradle wrapper script (Unix)
├── gradlew.bat                   # Gradle wrapper script (Windows)
├── settings.gradle               # Project settings
└── README.md                     # This file
```

## Dependencies

- **Jackson Databind**: For JSON parsing and processing
- **JUnit**: For testing (test framework)
- **Guava**: Utility libraries

## Input File Format

The application expects JSONL (JSON Lines) format where each line contains a valid JSON object:

```jsonl
{"field1": "value1", "nested": {"field2": "value2"}}
{"field1": "value3", "array": ["item1", "item2"]}
```

## Development

To add new features or modify the flattening logic, edit the `App.java` file in `app/src/main/java/ai/tonic/fabricate/tools/`.

Key methods:

- `processJsonlFile()`: Reads and processes the JSONL file
- `flattenJsonNode()`: Converts a JSON node to the flattened structure
- `flattenNode()`: Recursively processes nested objects and arrays

## Error Handling

The application will display error messages for:

- File not found
- Invalid JSON syntax
- IO errors during file reading
