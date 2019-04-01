import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CrawlerHttpClient {
    private static final JsonFactory jsonFactory = new JsonFactory();
    private static Map<String, String> cookies = new HashMap<String, String>();
    public static Object executeGet(String webServiceUrl, String userAgent, Class responseClass) throws IOException {
        URL url = new URL(webServiceUrl);
        Object response = null;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent",userAgent);
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = null;
                JsonParser jsonParser = null;
                try {
                    long httpStartTime = System.currentTimeMillis();
                    in = urlConnection.getInputStream();

                    long httpEndTime = System.currentTimeMillis();
                    /*System.out.println(" http T:" + (httpEndTime - httpStartTime) + " ms ");*/


                    long startTime = System.currentTimeMillis();
                    if (responseClass != null) {
                        jsonParser = jsonFactory.createJsonParser(in);
                        response = jsonParser.readValueAs(responseClass);
                    } else {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder resp = new StringBuilder();
                        String line = null;

                        while ((line = reader.readLine()) != null) {
                            resp.append(line);
                        }

                        response = resp.toString();
                    }
                    long endTime = System.currentTimeMillis();

                    /*System.out.println(" parsing T:" + (endTime - startTime) + " ms ");*/
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (jsonParser != null) {
                        jsonParser.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static InputStream loadContentByHttpClient(String url,String userAgent)
            throws ClientProtocolException, IOException {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", userAgent);
            HttpResponse response = client.execute(request);
            return response.getEntity().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream loadContentByHttpClientForIherb(String url,String userAgent)
            throws ClientProtocolException, IOException {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", userAgent);
            request.setHeader("cookie","iher-pref1=sccode=US&lan=en-US&scurcode=USD&wp=1&lchg=1&ifv=1&ctd=www&noitmes=192&pc=&ihr-code1=");
            HttpResponse response = client.execute(request);
            return response.getEntity().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Document userAgentCrawl(String searchUrl,String UserAgent) throws IOException {
        Document document = null;
        try {
            Connection.Response searchpage = Jsoup.connect(searchUrl)
                    .method(Connection.Method.GET)
                    .timeout(10000 * 1000)
                    .maxBodySize(0)
                    .execute();
            document = searchpage.parse();
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return document;

    }

    public static Document userAgentCrawlForSwansons(String searchUrl,String UserAgent) throws IOException {
        Document document = null;
        cookies.put("cookie",
                "dsddsdsdss");
        try {
            Connection searchpage = Jsoup.connect(searchUrl)
                    .userAgent(UserAgent)
                    .method(Connection.Method.GET)
                    .timeout(10000 * 1000)
                    .maxBodySize(0);
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                searchpage.cookie(cookie.getKey(), cookie.getValue());
            }
            Connection.Response response = searchpage.execute();
            cookies.clear();
            cookies.putAll(response.cookies());
            document = response.parse();
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return document;

    }
}
