package com.example.pq.wificamerademo.bean;

/**
 * @author panqiang
 * @version 1.0
 * @date 2018/8/4 13:11
 * @description
 */
public class StreamInfo {
    public String mediaCodecType;
    public int width;
    public int height;
    public int bitrate;
    public int fps;

    public static StreamInfo convertToStreamInfo(String cmd){
        String[] temp;
        StreamInfo streamInfo = new StreamInfo();
        //JIRA IC-395
        if(cmd.contains("FPS")){
            temp = cmd.split("\\?|&");
            streamInfo.mediaCodecType = temp[0];
            temp[1] = temp[1].replace("W=","");
            temp[2] = temp[2].replace("H=","");
            temp[3] = temp[3].replace("BR=","");
            temp[4] = temp[4].replace("FPS=","");
            streamInfo.width = Integer.parseInt(temp[1]);
            streamInfo.height = Integer.parseInt(temp[2]);
            streamInfo.bitrate = Integer.parseInt(temp[3]);
            streamInfo.fps = Integer.parseInt(temp[4]);
        }else {
            temp = cmd.split("\\?|&");
            streamInfo.mediaCodecType = temp[0];
            temp[1] = temp[1].replace("W=","");
            temp[2] = temp[2].replace("H=","");
            temp[3] = temp[3].replace("BR=","");
            streamInfo.width = Integer.parseInt(temp[1]);
            streamInfo.height = Integer.parseInt(temp[2]);
            streamInfo.bitrate = Integer.parseInt(temp[3]);
            streamInfo.fps = 30;
        }

        return  streamInfo;
    }
}
