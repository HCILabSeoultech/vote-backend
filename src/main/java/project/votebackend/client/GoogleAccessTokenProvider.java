package project.votebackend.client;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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

    public GoogleAccessTokenProvider(@Value("${fcm.key-path}") Resource keyResource) {
        try (InputStream in = keyResource.getInputStream()) {
            this.credentials = GoogleCredentials.fromStream(in)
                    .createScoped(SCOPES);
        } catch (Exception e) {
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
