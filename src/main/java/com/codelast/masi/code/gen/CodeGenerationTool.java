package com.codelast.masi.code.gen;

import com.codelast.masi.code.gen.pojo.CodeGeeXResponse;
import com.codelast.masi.code.gen.pojo.JsonPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * 调用 CodeGeeX API 生成代码。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@Slf4j
public class CodeGenerationTool {
  public static final int NUMBER = 3;  // 生成几个候选
  public static final String REQUEST_URL = "https://tianqi.aminer.cn/api/v2/multilingual_code_generate";  // 请求地址

  private final ObjectMapper objectMapper;
  private final String apiKey;  // 在"天启开放平台"上申请到的API Key
  private final String apiSecret;  // 在"天启开放平台"上申请到的API Secret

  public CodeGenerationTool(String apiKey, String apiSecret) {
    this.objectMapper = new ObjectMapper();
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
  }

  /**
   * 持续生成代码，直到没有返回。
   *
   * @param language      编程语言，e.g. "Java"
   * @param initialPrompt 待补全的代码(初始值)
   * @return 生成的代码
   */
  public String generateCodeUntilStop(String language, String initialPrompt) {
    StringBuilder ret = new StringBuilder();

    String text = initialPrompt;
    while (true) {
      String genCode;
      try {
        genCode = generateCode(language, text);
      } catch (Exception e) {
        log.error("生成代码失败，请您重试");
        break;
      }
      if (StringUtils.isEmpty(genCode)) {
        break;
      }
      /* CodeGeeX的bug，经常会遇到服务端返回类似于"{\{\{\{\{\{\{\{\{\<?{\{\{\{\<"这样的乱码，此时我们不能输出有问题的代码，而是尝试把
      之前的输入加上一个换行作为prompt再发给服务端 */
      if (genCode.contains("{\\{\\{")) {
        text = text + "\n";
      } else {
        System.out.print(genCode);  // 即时输出是为了视觉效果，并且服务端处理速度较慢，不能等所有结果都返回了再输出
        ret.append(genCode);
        text = text + genCode;  // 每次把生成的代码和之前的prompt拼接起来，作为下一次的prompt，这样可以得到完整的结果
      }
    }
    return ret.toString();
  }

  /**
   * 单次生成代码。
   *
   * @param language 编程语言，e.g. "Java"
   * @param prompt   待补全的代码
   * @return 生成的代码
   */
  public String generateCode(String language, String prompt) throws Exception {
    JsonPayload payload = new JsonPayload().setApiKey(apiKey).setApiSecret(apiSecret).setPrompt(prompt)
      .setNumber(NUMBER).setLanguage(language);
    String resp = performHttpPost(REQUEST_URL, objectMapper.writeValueAsString(payload));
    CodeGeeXResponse codeGeeXResponse = objectMapper.readValue(resp, CodeGeeXResponse.class);
    return codeGeeXResponse.getResult().getOutput().getCode().get(0);
  }

  /**
   * 发起 HTTP POST 请求。
   *
   * @param url     请求的URL
   * @param payload 请求的JSON数据
   * @return 请求返回的内容，若出错则返回 null。
   */
  public String performHttpPost(String url, String payload) {
    HttpUrl.Builder builder = null;
    try {
      HttpUrl httpUrl = HttpUrl.parse(url);
      if (httpUrl != null) {
        builder = httpUrl.newBuilder();
      }
    } catch (IllegalArgumentException e) {
      log.error("failed to create url builder", e);
    }
    if (builder == null) {
      return null;
    }
    OkHttpClient client = new OkHttpClient().newBuilder()
      .connectTimeout(20, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .build();

    RequestBody requestBody = RequestBody.create(payload, MediaType.parse("application/json; charset=utf-8"));
    Request request = new Request.Builder()
      .url(builder.build())
      .post(requestBody)
      .build();

    try {
      Response response = client.newCall(request).execute();
      ResponseBody body = response.body();
      if (body == null) {
        log.error("null response body");
        return null;
      }
      return body.string();
    } catch (IOException e) {
      log.error("failed to send POST request: " + e);
    }
    return null;
  }
}
