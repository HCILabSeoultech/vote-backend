package project.votebackend.client;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.votebackend.exception.AuthException;
import project.votebackend.type.ErrorCode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class GoogleAccessTokenProvider {

    private static final List<String> SCOPES =
            List.of("https://www.googleapis.com/auth/firebase.messaging");

    private final GoogleCredentials credentials;

    public GoogleAccessTokenProvider(@Value("${fcm.key-path}") String keyPath) {
        try {
            if (keyPath.startsWith("classpath:")) {
                String path = keyPath.replace("classpath:", "");
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                    this.credentials = GoogleCredentials.fromStream(is).createScoped(SCOPES);
                }
            } else {
                try (FileInputStream fis = new FileInputStream(keyPath)) {
                    this.credentials = GoogleCredentials.fromStream(fis).createScoped(SCOPES);
                }
            }
        } catch (IOException e) {
            throw new AuthException(ErrorCode.GOOGLE_CREDENTIAL_FAILED);
        }
    }

    public synchronized String getAccessToken() {
        try {
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new AuthException(ErrorCode.GOOGLE_CREDENTIAL_FAILED);
        }
    }
}
