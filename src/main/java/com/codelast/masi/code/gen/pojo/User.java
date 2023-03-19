package com.codelast.masi.code.gen.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
public class User {
  private String appId;
  private String id;
  private String name;
}
