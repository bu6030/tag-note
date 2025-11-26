# Tag Note Application

A Spring Boot application for managing notes with tags, using SQLite as the database. Includes a simple web frontend for interacting with the application.

## Features

- Create, read, update, and delete notes
- Create, read, and delete tags
- Associate multiple tags with notes
- Search notes by title or tags
- Web-based user interface for easy interaction

## Technologies Used

- Spring Boot 3.1.0
- Spring Data JPA
- SQLite database
- Maven for dependency management
- HTML, CSS, and JavaScript for frontend

## Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher

## Getting Started

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```
   mvn clean install
   ```
4. Run the application:
   ```
   mvn spring-boot:run
   ```
   
   Or alternatively:
   ```
   java -jar target/tag-note-0.0.1-SNAPSHOT.jar
   ```

## Frontend

The application includes a web-based user interface accessible at `http://localhost:8080`.

## API Endpoints

### Notes

- `GET /api/notes` - Get all notes
- `GET /api/notes/{id}` - Get a specific note by ID
- `POST /api/notes` - Create a new note
- `PUT /api/notes/{id}` - Update an existing note
- `DELETE /api/notes/{id}` - Delete a note
- `GET /api/notes/search?title={title}` - Search notes by title
- `GET /api/notes/search?tags={tag1,tag2}` - Search notes by tags

### Tags

- `GET /api/tags` - Get all tags
- `GET /api/tags/{id}` - Get a specific tag by ID
- `POST /api/tags` - Create a new tag
- `DELETE /api/tags/{id}` - Delete a tag

## Database

The application uses SQLite as its database. The database file `tag-note.db` will be created automatically when the application starts in the project root directory.

## Sample Note DTO

```json
{
  "id": 1,
  "title": "Sample Note",
  "content": "This is a sample note content",
  "createdAt": "2023-06-01T10:00:00",
  "updatedAt": "2023-06-01T10:00:00",
  "tags": ["important", "todo"]
}
```

## Sample Tag DTO

```json
{
  "id": 1,
  "name": "important",
  "createdAt": "2023-06-01T10:00:00"
}
```