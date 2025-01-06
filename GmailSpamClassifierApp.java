import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class GmailSpamClassifierApp extends Application {

    private static final String APPLICATION_NAME = "Gmail Spam Classifier";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/gmail.readonly");
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private TextArea emailDisplayArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gmail Spam Classifier");

        // Layout
        VBox root = new VBox();
        root.setPadding(new Insets(20));
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #f4f4f9;");

        // Header
        Label headerLabel = new Label("Gmail Spam Classifier");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Buttons
        Button fetchEmailsButton = new Button("Fetch Emails");
        fetchEmailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");
        fetchEmailsButton.setOnAction(e -> fetchEmails());
        fetchEmailsButton.setOnMouseEntered(e -> fetchEmailsButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;"));
        fetchEmailsButton.setOnMouseExited(e -> fetchEmailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;"));

        // Email Display Area
        emailDisplayArea = new TextArea();
        emailDisplayArea.setEditable(false);
        emailDisplayArea.setWrapText(true);
        emailDisplayArea.setStyle("-fx-font-family: Arial, sans-serif; -fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #ffffff; -fx-border-color: #ccc;");

        // Add components to layout
        root.getChildren().addAll(headerLabel, fetchEmailsButton, emailDisplayArea);

        // Scene
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void fetchEmails() {
        emailDisplayArea.clear();
        emailDisplayArea.appendText("Fetching emails...\n");

        try {
            // Initialize Gmail service
            Credential credential = getCredentials(GoogleNetHttpTransport.newTrustedTransport());
            Gmail service = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Fetch emails
            ListMessagesResponse response = service.users().messages().list("me").setMaxResults(10L).execute();
            List<Message> messages = response.getMessages();

            if (messages == null || messages.isEmpty()) {
                emailDisplayArea.appendText("No emails found.\n");
                return;
            }

            // Process emails
            for (Message message : messages) {
                Message detailedMessage = service.users().messages().get("me", message.getId()).execute();
                String subject = detailedMessage.getPayload().getHeaders().stream()
                        .filter(header -> "Subject".equals(header.getName()))
                        .findFirst()
                        .map(header -> header.getValue())
                        .orElse("No Subject");

                String snippet = detailedMessage.getSnippet();
                boolean isSpam = detectSpam(subject + " " + snippet);

                emailDisplayArea.appendText(String.format("Subject: %s\n", subject));
                emailDisplayArea.appendText(String.format("Snippet: %s\n", snippet));
                emailDisplayArea.appendText(String.format("Spam Status: %s\n\n", isSpam ? "SPAM" : "NOT SPAM"));
            }

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            emailDisplayArea.appendText("Error fetching emails.\n");
        }
    }

    private boolean detectSpam(String content) {
        List<String> spamKeywords = Arrays.asList(
                "win", "free", "prize", "cash", "money", "urgent", "guaranteed",
                "limited offer", "click here", "congratulations", "claim now",
                "lottery", "investment", "act now", "offer expires"
        );

        String lowerContent = content.toLowerCase();
        return spamKeywords.stream().anyMatch(lowerContent::contains);
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GmailSpamClassifierApp.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setPort(8888).build()).authorize("user");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
