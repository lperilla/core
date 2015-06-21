package org.lperilla.framework.core.httpaction;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpAction {

	protected CredentialsProvider getCredentialsProvider(HttpHost proxyHost, String username, String password) {
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		AuthScope authscope = new AuthScope(proxyHost.getHostName(), proxyHost.getPort());

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(authscope, credentials);
		return credsProvider;
	}

	protected CloseableHttpResponse execute(CloseableHttpClient httpClient, HttpUriRequest request) throws Exception {
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(request);
			consumeResponse(response);
			return response;
		} finally {
			if (response != null)
				response.close();
		}
	}

	protected void consumeResponse(CloseableHttpResponse response) throws Exception {
		try {
			HttpEntity entity = response.getEntity();
			String strResponse = EntityUtils.toString(entity);
			int statusCode = response.getStatusLine().getStatusCode();
			EntityUtils.consume(entity);

			System.out.println("Http status code for Authenticattion Request: " + statusCode);
			System.out.println("Response for Authenticattion Request: \n" + strResponse);
			System.out.println("================================================================\n");
		} catch (ClientProtocolException ex) {
			throw new Exception(ex);
		} catch (IOException ex) {
			throw new Exception(ex);
		}
	}

}
