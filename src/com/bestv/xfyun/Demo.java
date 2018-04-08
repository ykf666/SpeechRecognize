package com.bestv.xfyun;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.iflytek.cloud.speech.RecognizerListener;
import com.iflytek.cloud.speech.RecognizerResult;
import com.iflytek.cloud.speech.Setting;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechRecognizer;
import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.util.JsonParser;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;

public class Demo {

    /** 最大超时时间，单位ms */
    private int maxWaitTime = 5000;
    /** 每次等待时间，单位ms */
    private int perWaitTime = 1000;

    private StringBuffer mResult = new StringBuffer();

    private static String FILENAME = "D:/IdeaProjects/SpeechRecognize/resource/26_clip.wav";

    static {
        Setting.setShowLog(true);
        SpeechUtility.createUtility(SpeechConstant.APPID + "=5ac43e6b");
    }

    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.audio2word(FILENAME);
    }

    private void audio2word(String audioPath) {
        // 1.创建SpeechRecognizer对象
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer();

        // 2.设置听写参数
        // 短信和日常用语：iat (默认);视频：video;地图：poi;音乐：music
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        // 简体中文：zh_cn（默认）;美式英文：en_us
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 普通话：mandarin（默认）;粤语：cantonese;四川话：lmz;河南话：henanese
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 是否使用SDK自带的录音机，默认1为使用，-1为使用音频流
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");

        // 3.开始听写
        mIat.startListening(mRecoListener);

        byte[] audioBytes = streamAudio(audioPath);
        List<byte[]> buffers = splitBuffer(audioBytes, audioBytes.length, 4800);
        for (int i = 0; i < buffers.size(); i++) {
            mIat.writeAudio(buffers.get(i), 0, buffers.get(i).length);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 4.关闭听写
        mIat.stopListening();
        while (mIat.isListening()) {
            if (maxWaitTime < 0) {
                mResult.setLength(0);
                mResult.append("解析超时！");
                break;
            }
            try {
                Thread.sleep(perWaitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            maxWaitTime -= perWaitTime;
        }
        System.out.println("解析结果：" + mResult.toString());
        System.out.println(JsonParser.parseIatResult(mResult.toString()));
    }

    /**
     * <b>Description:</b>按照块大小切割字节数组<br>
     *
     * @param buffer
     * @param length
     * @param spsize
     * @return
     * @Note <b>Author:</b> yankefei <br>
     *       <b>Date:</b> 2018年4月4日 下午3:28:25
     */
    private ArrayList<byte[]> splitBuffer(byte[] buffer, int length, int spsize) {
        ArrayList<byte[]> array = new ArrayList<byte[]>();
        if (spsize <= 0 || length <= 0 || buffer == null || buffer.length < length)
            return array;
        int size = 0;
        while (size < length) {
            int left = length - size;
            if (spsize < left) {
                byte[] sdata = new byte[spsize];
                System.arraycopy(buffer, size, sdata, 0, spsize);
                array.add(sdata);
                size += spsize;
            } else {
                byte[] sdata = new byte[left];
                System.arraycopy(buffer, size, sdata, 0, left);
                array.add(sdata);
                size += left;
            }
        }
        return array;
    }

    /**
     * <b>Description:</b>读取音频文件<br>
     *
     * @param audioPath
     * @return
     * @Note <b>Author:</b> yankefei <br>
     *       <b>Date:</b> 2018年4月4日 下午3:29:23
     */
    private byte[] streamAudio(String audioPath) {
        FileInputStream fis = null;
        byte[] voiceBuffer = null;
        try {
            fis = new FileInputStream(new File(audioPath));
            voiceBuffer = new byte[fis.available()];
            fis.read(voiceBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return voiceBuffer;
    }

    /**
     * 听写监听器
     */
    private RecognizerListener mRecoListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onVolumeChanged(int volume) {
        }

        @Override
        public void onResult(RecognizerResult result, boolean islast) {
            mResult.append(result.getResultString());
        }

        @Override
        public void onError(SpeechError error) {
//            try {
//                voice2words(fileName);
//                maxQueueTimes--;
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new RuntimeException(e);
//            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int agr2, String msg) {
            System.out.println("onenvent:" + eventType);
            System.out.println(msg);
        }

    };

}
