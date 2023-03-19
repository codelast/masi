package com.codelast.masi.code.gen.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * {@link CodeGeeXResponse} 的一部分。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Result {
  private String app;
  @JsonProperty("created_at")
  private String createdAt;
  private Input input;
  private Output output;
  @JsonProperty("process_time")
  private double processTime;
  @JsonProperty("task_id")
  private String taskId;
  @JsonProperty("updated_at")
  private String updatedAt;
  private User user;
}
