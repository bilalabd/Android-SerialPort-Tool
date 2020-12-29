package com.licheedev.serialtool.comn;

import android.os.SystemClock;
import com.licheedev.hwutils.ByteUtil;
import com.licheedev.myutils.LogPlus;
import com.licheedev.serialtool.activity.IScaleReadCallback;
import com.licheedev.serialtool.comn.message.LogManager;
import com.licheedev.serialtool.comn.message.RecvMessage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * 读串口线程
 */
public class SerialReadThread extends Thread {

    private static final String TAG = "SerialReadThread";

    private BufferedInputStream mInputStream;

    public IScaleReadCallback mScaleReadCallback;

    public SerialReadThread(InputStream is) {
        mInputStream = new BufferedInputStream(is);
    }

    @Override
    public void run() {
        byte[] received = new byte[1024];
        int size;

        LogPlus.e("开始读线程");

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            try {

                int available = mInputStream.available();

                if (available > 0) {
                    size = mInputStream.read(received);
                    if (size > 0) {
                        onDataReceive(received, size);
                    }
                } else {
                    // 暂停一点时间，免得一直循环造成CPU占用率过高
                    SystemClock.sleep(1);
                }
            } catch (IOException e) {
                LogPlus.e("读取数据失败", e);
            }
            //Thread.yield();
        }

        LogPlus.e("结束读进程");
    }

    /**
     * 处理获取到的数据
     *
     * @param received
     * @param size
     */
    private void onDataReceive(byte[] received, int size) {
        // TODO: 2018/3/22 解决粘包、分包等
        String hexStr = ByteUtil.bytes2HexStr(received, 0, size);
        String t = hexToString(hexStr);
        //LogManager.instance().post(new RecvMessage(t));

        if(mScaleReadCallback != null)
            mScaleReadCallback.onScaleReadData(t);
    }



    private String hexToString(String h) {


        String result = new String();
        char[] charArray = h.toCharArray();
        for(int i = 0; i < charArray.length; i=i+2) {
            String st = ""+charArray[i]+""+charArray[i+1];
            char ch = (char)Integer.parseInt(st, 16);
            result = result + ch;
        }
        return result;
    }

    /**
     * 停止读线程
     */
    public void close() {

        try {
            mInputStream.close();
        } catch (IOException e) {
            LogPlus.e("异常", e);
        } finally {
            super.interrupt();
        }
    }
}
