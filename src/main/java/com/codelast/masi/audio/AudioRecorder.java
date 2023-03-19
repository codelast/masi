package com.codelast.masi.audio;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 把声音转换成音频文件。
 *
 * @author Darran Zhang @ codelast.com
 * @version 2023-03-18
 */
@Slf4j
public class AudioRecorder {
  private static final int SAMPLE_RATE = 16000;  // 采样率
  private static final double LOW_VOLUME_THRESHOLD = 4000;  // 根据实际音量调节
  private static final int STOP_RECORDING_AFTER_SILENCE_FOR_MS = 5000;  // 检测到低音量后，若持续5秒，则停止录音

  /**
   * 开始录音。
   *
   * @param outputAudioFile 音频文件的输出路径
   */
  public void startRecord(String outputAudioFile) {
    try {
      AudioInputStream audioInputStream = getAudioInputStream();
      AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
      TargetDataLine targetDataLine = getTargetDataLine();

      targetDataLine.open();
      targetDataLine.start();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      long silenceStartMs = -1;
      while (silenceStartMs == -1 || System.currentTimeMillis() - silenceStartMs < STOP_RECORDING_AFTER_SILENCE_FOR_MS) {
        byte[] dataBuffer = new byte[targetDataLine.getBufferSize() / 5];
        int numBytesRead = targetDataLine.read(dataBuffer, 0, dataBuffer.length);

        // Detect low volume and start tracking silence time
        boolean isSilentSoundInput = isLowVolume(dataBuffer, numBytesRead);
        if (isSilentSoundInput && silenceStartMs == -1) {
          silenceStartMs = System.currentTimeMillis();
        } else if (!isSilentSoundInput && silenceStartMs != -1) {
          silenceStartMs = -1;
        }
        out.write(dataBuffer, 0, numBytesRead);
      }

      targetDataLine.stop();
      ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
      AudioSystem.write(new AudioInputStream(bais, audioInputStream.getFormat(), out.size()), fileType,
        new File(outputAudioFile));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static AudioInputStream getAudioInputStream() throws Exception {
    DataLine.Info info =
      new DataLine.Info(TargetDataLine.class, getAudioFormat());
    if (!AudioSystem.isLineSupported(info)) {
      throw new Exception("not supported");
    }
    return AudioSystem.getAudioInputStream(getAudioFormat(),
      new AudioInputStream(
        new BufferedInputStream(
          new InputStream() {
            @Override
            public int read() {
              return 0;
            }
          }),
        getAudioFormat(),
        AudioSystem.NOT_SPECIFIED));
  }

  private static TargetDataLine getTargetDataLine() throws LineUnavailableException {
    DataLine.Info info =
      new DataLine.Info(TargetDataLine.class, getAudioFormat());
    TargetDataLine line =
      (TargetDataLine) AudioSystem.getLine(info);
    line.open(getAudioFormat());
    return line;
  }

  /**
   * 判断音频信号是否具有低音量
   *
   * @param audioSignal 音频信号的原始数据
   */
  private static boolean isLowVolume(byte[] audioSignal, int nBytesRead) {
    // 将字节数组转换为16位整数数组（每个值在-32768到32767之间）
    short[] audioData = new short[nBytesRead / 2];
    ByteBuffer.wrap(audioSignal).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

    // 计算音频数据中所有样本值的平均值
    long sum = 0;
    for (short sample : audioData) {
      sum += Math.abs(sample);
    }
    double average = (double) sum / audioData.length;
    return average < LOW_VOLUME_THRESHOLD;  // 如果平均值小于某个阈值，则认为信号具有低音量
  }

  private static AudioFormat getAudioFormat() {
    int sampleSizeInBits = 24; // For better quality use a larger sample size than the normal CD standard of 16 bits.
    int channels = 1; // Mono with one channel or stereo with two channels.
    boolean signed = true; // true if the data is signed, false otherwise.
    boolean bigEndian = true; // true if the data is stored in big-endian order, false if little-endian.

    return new AudioFormat((float) SAMPLE_RATE, sampleSizeInBits, channels, signed, bigEndian);
  }
}
