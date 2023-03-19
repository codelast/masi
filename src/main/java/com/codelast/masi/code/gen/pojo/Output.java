package com.codelast.masi.code.gen.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * {@link Result} 的一部分。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Output {
  private List<String> code;
  @JsonProperty("code_dict")
  private List<CodeDict> codeDicts;
  @JsonProperty("completion_token_num")
  private int completionTokenNum;
  @JsonProperty("errcode")
  private int errCode;
  @JsonProperty("prompt_token_num")
  private int promptTokenNum;
}
