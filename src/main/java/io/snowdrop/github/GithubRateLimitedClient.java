package io.snowdrop.github;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.google.common.util.concurrent.RateLimiter;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubRateLimitedClient extends GitHubClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(GithubRateLimitedClient.class);

    private final RateLimiter limiter = RateLimiter.create(1);

	public GithubRateLimitedClient() {
	}

	public GithubRateLimitedClient(String hostname) {
		super(hostname);
	}

	public GithubRateLimitedClient(String hostname, int port, String scheme) {
		super(hostname, port, scheme);
	}

	@Override
	public void delete(String uri, Object params) throws IOException {
        limiter.acquire();
		super.delete(uri, params);
	}

	@Override
	public synchronized GitHubResponse get(GitHubRequest request) throws IOException {
        limiter.acquire();
		return super.get(request);
	}

	@Override
	public synchronized InputStream getStream(GitHubRequest request) throws IOException {
        limiter.acquire();
		return super.getStream(request);
	}

}
