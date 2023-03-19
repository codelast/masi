package com.codelast.masi;

import com.codelast.masi.audio.AudioRecorder;
import com.codelast.masi.audio.TencentAudioToTextConverter;
import com.codelast.masi.code.gen.CodeGenerationTool;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * 主入口类。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@Slf4j
public class MaSi {
  private static class Options {
    @Option(name = "--tqApiKey", usage = "the API key applied on tianqi platform", required = true)
    String tqApiKey;

    @Option(name = "--tqApiSecret", usage = "the API secret applied on tianqi platform", required = true)
    String tqApiSecret;

    @Option(name = "--txApiSecretId", usage = "the API secretId applied on Tencent cloud platform", required = true)
    String txApiSecretId;

    @Option(name = "--txApiSecretKey", usage = "the API secretKey applied on Tencent cloud platform", required = true)
    String txApiSecretKey;

    @Option(name = "--outputAudioFile", usage = "the output .wav audio file on local fs", required = true)
    String outputAudioFile;
  }

  public static void main(String[] args) {
    Options opts = new Options();
    CmdLineParser parser = new CmdLineParser(opts);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      log.error("failed to parse command line arguments", e);
      System.exit(1);
    }

    MaSi maSi = new MaSi();
    maSi.start(opts.tqApiKey, opts.tqApiSecret, opts.txApiSecretId, opts.txApiSecretKey, opts.outputAudioFile);
  }

  public void start(String tqApiKey, String tqApiSecret,
                    String txApiSecretId, String txApiSecretKey,
                    String outputAudioFile) {
    File fileToDelete = new File(outputAudioFile);
    if (fileToDelete.exists() && fileToDelete.isFile()) {
      if (fileToDelete.delete()) {
        log.debug("file [{}] deleted", outputAudioFile);
      } else {
        log.error("failed to delete file [{}]", outputAudioFile);
        return;
      }
    }

    // 录音
    log.info("我是码斯，请问我能为你做什么？我在听");
    AudioRecorder audioRecorder = new AudioRecorder();
    audioRecorder.startRecord(outputAudioFile);

    // 语音转文字
    log.info("好的，我正在努力记住你的话");
    TencentAudioToTextConverter converter = new TencentAudioToTextConverter(txApiSecretId, txApiSecretKey);
    String text;
    try {
      text = converter.convert(outputAudioFile);
    } catch (Exception e) {
      log.error("failed to convert audio to text");
      return;
    }

    // 文字生成代码
    log.info("你刚才说的是：" + text);
    log.info("正在为您生成方案：");
    log.info("=================================================================================================");
    CodeGenerationTool codeGenTool = new CodeGenerationTool(tqApiKey, tqApiSecret);
    codeGenTool.generateCodeUntilStop("Java", text);
    log.info("=================================================================================================");
    log.info("生成完毕，请检查");
  }
}
