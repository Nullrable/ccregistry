package org.lsd.ccregistery.util;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * @author nhsoft.lsd
 */
public class HttpInvoker {

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(5_000, TimeUnit.MILLISECONDS)//设置连接超时时间
            .readTimeout(1_000, TimeUnit.MILLISECONDS)//设置读取超时时间
            .build();

    public static <T> T get(final String url, Class<T> tClass) throws IOException {
        Request req = new Request.Builder().url(url).build();
        try (ResponseBody responseBody = okHttpClient.newCall(req).execute().body()) {
            assert responseBody != null;
            return JSONObject.parseObject(responseBody.string(), tClass);
        } catch (IOException ex) {
            throw ex;
        }
    }
}
