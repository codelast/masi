package com.codelast.masi.audio;

import com.tencentcloudapi.asr.v20190614.AsrClient;
import com.tencentcloudapi.asr.v20190614.models.*;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 调用腾讯云的语音识别API，把语音转换成文字。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@Slf4j
public class TencentAudioToTextConverter {
  private final String tencentSecretId;  // 在腾讯云上申请到的secretId
  private final String tencentSecretKey;  // 在腾讯云上申请到的secretKey

  public TencentAudioToTextConverter(String tencentSecretId, String tencentSecretKey) {
    this.tencentSecretId = tencentSecretId;
    this.tencentSecretKey = tencentSecretKey;
  }

  /**
   * 把音频文件转换成文字(语音识别)。
   *
   * @param audioFile wav音频文件路径
   * @return 转换成的文字
   */
  public String convert(String audioFile) throws Exception {
    String ret = null;

    // 设置密钥信息
    Credential cred = new Credential(tencentSecretId, tencentSecretKey);
    // 实例化一个客户端配置对象
    ClientProfile clientProfile = new ClientProfile();
    AsrClient client = new AsrClient(cred, "ap-guangzhou", clientProfile);
    // 构造请求对象
    CreateRecTaskRequest req = new CreateRecTaskRequest();

    // 设置请求参数
    req.setEngineModelType("16k_zh");  // 识别引擎，16k中文普通话
    req.setChannelNum(1L);  // 声道数，单声道
    req.setResTextFormat(0L);  // 结果文本编码格式，0表示UTF-8
    req.setSourceType(1L);  // 语音数据来源，1表示语音URL

    String fileContent = Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(audioFile)));
    req.setDataLen((long) fileContent.length());
    req.setData(fileContent);

    // 发送请求，并返回结果对象
    CreateRecTaskResponse resp = client.CreateRecTask(req);

    // API 会立即返回 taskId，之后要根据 taskId 查询识别结果（异步）
    Task task = resp.getData();
    if (task == null) {
      log.error("ret task is null");
      return null;
    }
    long taskId = task.getTaskId();
    log.debug("got taskId [{}], will check recognition result by taskId...", taskId);

    int count = 0;
    while (true) {
      DescribeTaskStatusRequest taskStatusReq = new DescribeTaskStatusRequest();  // 查询任务状态请求对象
      taskStatusReq.setTaskId(taskId);
      DescribeTaskStatusResponse taskStatusResp = client.DescribeTaskStatus(taskStatusReq);  // 发起查询任务状态请求
      TaskStatus taskStatus = taskStatusResp.getData();
      if (taskStatus == null) {
        log.error("ret taskStatus is null");
        return null;
      }
      long status = taskStatus.getStatus();
      if (status == 0) {
        log.debug("audio recognition task is waiting");
      } else if (status == 1) {
        log.debug("audio recognition task is on going, please be patient");
      } else if (status == 2) {
        log.debug("audio recognition task done");
        ret = ConvertResult2OneStr(taskStatusResp.getData().getResult());
        break;
      } else if (status == 3) {
        log.error("audio recognition task failed");
        break;
      }
      log.debug("recognition result is not ready, status is [{}], will check again in 1 second", status);
      Thread.sleep(3000);

      // 查询了一定的次数还没有结果，就放弃
      if (count++ > 60) {
        log.debug("recognition result is not ready after 60 seconds, quit");
        break;
      }
    }

    return ret;
  }

  /**
   * 把返回的多段文字合并成一段。
   *
   * @param str 腾讯云API返回的语音识别文字，当语音较长时，会被分成多段，每段文字之间用换行符分隔，e.g. "[0:0.740,0:5.500]  给定一个整数数组和一个整数目标值。\n[0:5.500,0:13.560]  请你在该数组中找出合为目标值的那两个整数，并返回它们的数组下标。\n[0:13.560,0:23.120]  你可以假设每种输入只会对应一个答案，但是数组中同一个元素在答案里不能重复出现。\n[0:23.120,0:26.320]  你可以按任意顺序返回答案。"
   * @return 合并后的文字，e.g. "给定一个整数数组和一个整数目标值，请你在该数组中找出合为目标值的那两个整数，并返回它们的数组下标，你可以假设每种输入只会对应一个答案，但是数组中同一个元素在答案里不能重复出现，你可以按任意顺序返回答案"
   */
  private String ConvertResult2OneStr(String str) {
    if (StringUtils.isEmpty(str)) {
      return null;
    }
    StringBuilder ret = new StringBuilder();

    String[] textArray = StringUtils.split(str, "\n");
    for (String s : textArray) {
      String[] fields = StringUtils.split(s, "]");
      if (fields.length < 2 || fields[1].trim().length() == 0) {
        continue;
      }
      String text = fields[1].trim();  // e.g. "你可以按任意顺序返回答案。"
      ret.append(text);
    }
    // 依次进行如下处理：(1) 把ret中的所有句号("。")换成逗号("，")；(2) 如果ret是以逗号("，")结尾，则把逗号换成句号("。")；(3) 如果ret结尾没有句号("。")，则添加上句号
    return ret.toString().replace("。", "，").replaceAll("，$", "。").replaceAll("。$", "。");
  }
}
