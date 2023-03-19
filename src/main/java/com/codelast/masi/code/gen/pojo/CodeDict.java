package com.codelast.masi.code.gen.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * {@link Output} 的一部分。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CodeDict {
  private String context;
  @JsonProperty("cum_log_probs")
  private double cumLogProbs;
  private String generated;
}
