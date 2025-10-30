Vemorize is a voice-only Android chatbot that helps the user memorize any content, be it a poem, a text-book, a language, meeting notes, etc.

## How it works

The user loads a text or a document into the Android app and the application will generate a course with a learning tree structure.

The learning tree contains a hierarchy of nodes with "leaves" at the end. The smallest unit of learning content is a "leaf". A learning tree is validated at loading and at startup, so it can be assumed to follow certain rules, like every learning tree should contain at least one leaf.

The user can navigate through the content using voice commands in different modes, like "quiz", "flashcard", "reading" and so on. The user can also mark pieces of content with different states of memorization (like "new", "learning", "review", "mastered") or other metadata, all using voice commands.

The user can also mark pieces of content with different states of memorization (like "new", "learning", "review", "mastered") or other metadata, all using voice commands.

The user will be able to get a visual overview of the learning progress of a course through a dashboard displayed on the Android device screen.

## Android-Specific Features

- **Voice Input**: Uses Android's SpeechRecognizer API for continuous voice command recognition
- **Text-to-Speech**: Uses Android TextToSpeech API for audio feedback and content reading
- **Offline Capability**: Local storage using Room database for offline learning
- **Background Processing**: Continues voice recognition even when app is in background (with proper permissions)
- **Accessibility**: Full support for Android accessibility services
- **Material Design**: Follows Android Material Design guidelines for visual components
- **Permissions**: Requires RECORD_AUDIO permission for voice input functionality