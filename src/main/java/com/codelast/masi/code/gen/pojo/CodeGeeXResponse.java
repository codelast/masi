package com.codelast.masi.code.gen.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 调用 CodeGeeX API 返回的JSON对应的POJO类。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CodeGeeXResponse {
  private String message;
  private Result result;
  private int status;
}
