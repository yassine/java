package io.ipdata.client;

import feign.httpclient.ApacheHttpClient;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.Currency;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class CurrencyTest {

  private static final TestContext TEST_CONTEXT = new TestContext("https://api.ipdata.co");

  @Parameterized.Parameter
  public IpdataService ipdataService;

  @Test
  @SneakyThrows
  public void testCurrency() {
    Currency currency = ipdataService.currency("8.8.8.8");
    String actual = TEST_CONTEXT.mapper().writeValueAsString(currency);
    String expected = TEST_CONTEXT.get("/8.8.8.8/currency", null);
    TEST_CONTEXT.assertEqualJson(actual, expected);
    if (ipdataService == TEST_CONTEXT.cachingIpdataService()) {
      //value will be returned from cache now
      currency = ipdataService.currency("8.8.8.8");
      actual = TEST_CONTEXT.mapper().writeValueAsString(currency);
      TEST_CONTEXT.assertEqualJson(actual, expected);
    }
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testCurrencyError() {
    URL url = new URL("https://api.ipdata.co");
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(url)
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())
      ).get();
    serviceWithInvalidKey.currency("8.8.8.8");
  }

  @Parameterized.Parameters
  public static Iterable<IpdataService> data() {
    return Arrays.asList(TEST_CONTEXT.ipdataService(), TEST_CONTEXT.cachingIpdataService());
  }

}