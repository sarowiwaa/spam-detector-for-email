

Gmail Spam Classifier
This JavaFX application connects to the Gmail API and classifies incoming emails as spam or not spam based on their subject and content. The app uses Google's Gmail API to fetch the latest emails and analyze them for common spam keywords.

Features
Fetch Emails: Retrieves the latest emails from your Gmail account.
Spam Detection: Analyzes the subject and snippet of each email and labels it as "SPAM" or "NOT SPAM" based on predefined keywords.
User-Friendly Interface: Simple, intuitive interface built with JavaFX for a seamless experience.
Requirements
Java 8+: This application uses JavaFX, which is supported in Java 8 and later.
Google API Credentials: You need to set up a Google Cloud project and enable the Gmail API.
Create a project on the Google Cloud Console.
Enable the Gmail API and create OAuth 2.0 credentials.
Download the credentials.json file and place it in the root directory of the project.
Setup
Clone the Repository

bash
Copy code
git clone https://github.com/sarowiwaa/spam-detector-for-email.git
cd gmail-spam-classifier
Install Dependencies Ensure you have Java 8 or later installed and set up properly.

Set Up Google API Credentials

Follow the steps above to create a Google Cloud project and download the credentials.json file.
Place the credentials.json file in the root directory of the project.
Run the Application In the terminal, navigate to the project directory and run the following command:

Copy code
javac GmailSpamClassifierApp.java
java GmailSpamClassifierApp
Authenticate When you run the application for the first time, a browser window will open, asking for permission to access your Gmail account. Log in and grant access to the app.

Use the App

Click on Fetch Emails to retrieve the latest emails from your Gmail account.
The app will display the subject, snippet, and spam classification for each email.
Spam Detection Logic
The app uses a simple spam detection algorithm based on predefined keywords. If the content of the email (subject or snippet) contains any of the following words, it is flagged as spam:

win, free, prize, cash, money, urgent, guaranteed, limited offer
click here, congratulations, claim now, lottery, investment, act now, offer expires
You can expand or modify the list of spam keywords in the detectSpam() method to improve the accuracy of spam detection.

License
This project is licensed under the MIT License - see the LICENSE file for details.
