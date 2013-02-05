package com.example.dextertest;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class Sink_ApacheHTTPClient implements Sink {

  @Override
  public void sendData(String data) {
    List<NameValuePair> nameValuePairs = Arrays.asList((NameValuePair) new BasicNameValuePair("postedData", data));
    HttpEntity postData = null;
    try {
      postData = new UrlEncodedFormEntity(nameValuePairs);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw new Error(e);
    }

    HttpPost post = new HttpPost("http://www.google.com/");
    post.setEntity(postData);

    DefaultHttpClient client = new DefaultHttpClient();
    try {
      client.execute(post);
    } catch (Throwable e) {
      // expected to fail
    }
  }

}
