package net.xby1993.crawler.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpClientFactory {
	private static final Logger log=LoggerFactory.getLogger(HttpClientFactory.class);
	
	public  CloseableHttpClient getClient(){
		return getClient(30000, 3);
	}
	public  CloseableHttpClient getClient(int timeout,int retry){
		RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();  
		registryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
		// Fixing: https://code.google.com/p/crawler4j/issues/detail?id=174
		                // By always trusting the ssl certificate
		SSLContext sslContext=null;
		try {
			sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {   
			    public boolean isTrusted(final X509Certificate[] chain, String authType) {   
			        return true;   
			    }   
			}).build();
		} catch (KeyManagementException e) {
			log.error("",e);
		} catch (NoSuchAlgorithmException e) {
			log.error("",e);
		} catch (KeyStoreException e) {
			log.error("",e);
		}   
		SSLConnectionSocketFactory sslsf=new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);   
		registryBuilder.register("https", sslsf);
		Registry<ConnectionSocketFactory> registry = registryBuilder.build();  
		//设置连接管理器  
		PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);  
		poolingHttpClientConnectionManager.setMaxTotal(500);
		poolingHttpClientConnectionManager.setDefaultMaxPerRoute(1000);
		
		SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
	    socketConfigBuilder.setSoKeepAlive(true).setTcpNoDelay(true);
	    socketConfigBuilder.setSoTimeout(timeout);
	    SocketConfig socketConfig = socketConfigBuilder.build();
		//构建客户端  
		CloseableHttpClient client= HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager)
				.addInterceptorFirst(new HttpRequestInterceptor() {

			        public void process(
			                final HttpRequest request,
			                final HttpContext context) throws HttpException, IOException {
			            if (!request.containsHeader("Accept-Encoding")) {
			                request.addHeader("Accept-Encoding", "gzip");
			            }
			        }
			    })
			    .setDefaultSocketConfig(socketConfig)
			    .setRetryHandler(new DefaultHttpRequestRetryHandler(retry, true))
			    .build();  
		return client;
	}
	
    
}
