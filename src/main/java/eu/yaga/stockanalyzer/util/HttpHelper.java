package eu.yaga.stockanalyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andreas on 13.11.16.
 */
public class HttpHelper {

    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);

    public static String queryHTML(URL url) {
        BufferedReader br = null;
        InputStream inputStream = null;
        StringBuilder sb = new StringBuilder();


        try {
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36");

            log.info("Request URL ... " + url);

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = uc.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            log.info("Response Code ... " + status);

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = uc.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = uc.getHeaderField("Set-Cookie");

                // open the new connnection again
                uc = (HttpURLConnection) new URL(newUrl).openConnection();
                uc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36");

                System.out.println("Redirect to URL : " + newUrl);

            }

            inputStream = uc.getInputStream();

            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}
