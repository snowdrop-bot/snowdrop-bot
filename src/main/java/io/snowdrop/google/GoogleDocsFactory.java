package io.snowdrop.google;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.snowdrop.BotException;

@ApplicationScoped
public class GoogleDocsFactory {

  private static final String APPLICATION_NAME = "Snowdrop Bot";

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS);

  @ConfigProperty(name = "google.docs.credentials.file")
  String credentialsFile;

  @Produces
  public Docs createService()  {
	try {
		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        return new Docs.Builder(transport, JSON_FACTORY, getCredentials(transport)).setApplicationName(APPLICATION_NAME).build();
	} catch (Exception e) {
      throw BotException.launderThrowable(e);
	}
  }

  /**
   * Creates an authorized Credential object.
   *
   * @param transport The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private Credential getCredentials(HttpTransport transport) throws IOException {
    // Load client secrets.
    try (InputStream in = new FileInputStream(credentialsFile)) {
      if (in == null) {
        throw new FileNotFoundException("Resource not found: " + credentialsFile);
      }
      GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

      // Build flow and trigger user authorization request.
      GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(transport, JSON_FACTORY, clientSecrets, SCOPES)
              .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
              .setAccessType("offline").build();

      LocalServerReceiver receier = new LocalServerReceiver.Builder().build();
      return new AuthorizationCodeInstalledApp(flow, receier).authorize("user");
    } catch (Throwable t) {
      throw BotException.launderThrowable(t);
    }
  }

}
