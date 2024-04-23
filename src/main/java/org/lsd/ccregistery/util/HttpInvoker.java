package org.lsd.ccregistery.util;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author nhsoft.lsd
 */
public class HttpInvoker {

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(5_000, TimeUnit.MILLISECONDS)//设置连接超时时间
            .readTimeout(1_000, TimeUnit.MILLISECONDS)//设置读取超时时间
            .build();

    @SneakyThrows
    public static <T> T get(final String url, Class<T> tClass) throws IOException {
        Request req = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            // 将响应的 JSON 字符串转换为 MyResponse 对象
            assert response.body() != null;
            String jsonResponse = response.body().string();

            return JSON.parseObject(jsonResponse).toJavaObject(tClass);
        }
    }
}
