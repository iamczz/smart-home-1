package com.android_wifi;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.util.Log;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

@SuppressLint("HandlerLeak")
public class ClientThread extends Thread {
	private String TAG = "zigbee_demo";
	
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	private Socket socket;
	private SocketAddress socketAddress;
	public static Handler childHandler;
	private boolean RxFlag = true;
	public boolean bClientThreadStart = false;
	private RxThread rxThread;
	final int TEXT_INFO = 12;
	static final int RX_EXIT = 11;
	static final int TX_DATA = 10;
	Context mainContext;
	Message msg;
	private String strIP;
	private int iPort;
 

	//�����붨��
	static final int FUN_CODE_CHECK_ALL_DATA=0x01;	//�ֻ�/PC-->zigbee ��ѯ���д���������
	static final int FUN_CODE_UPDATA_ALL_DATA=0x02;	//zigbee-->�ֻ�/PC �ϴ����д���������
	static final int FUN_CODE_UPDATA_RFID=0x03;	//zigbee-->�ֻ�/PC  �ϴ�RFID����
	static final int ZIGBEE_FUN_CODE_CTRL_LAMP=0x04;	//���Ƶ����ն��ϵƵ�״̬
	static final int ZIGBEE_FUN_CODE_STEP=0x05;	//�����ն��ϵĲ������
	static final int ZIGBEE_FUN_CODE_END1=0x06;  //�ն�1���ն��룬�����ն˵ķ�ֵ�Ϳ��ص�� 

	
	public ClientThread(String ip, int port) {
		strIP = ip;
		iPort =port;
	}	

	boolean socketConnect(){
		
		
		if(socket.isConnected()) return true;
		
		return false;
	}
	//��������
	void connect() {
		RxFlag = true;
		socketAddress = new InetSocketAddress(strIP, iPort);
		socket = new Socket();
		bClientThreadStart = false;

		try {
			socket.connect(socketAddress, iPort);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI, "���ӳɹ�");
			MainActivity.mainHandler.sendMessage(msg);

			msg = MainActivity.mainHandler.obtainMessage(MainActivity.Start_timer, "���ӳɹ�");
			MainActivity.mainHandler.sendMessageDelayed(msg, 2000);
			
			
			rxThread = new RxThread();
			rxThread.start();
			bClientThreadStart = true;
		} catch (IOException e) {	
			try {
				sleep(10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI, "�޷����ӵ�������");
			MainActivity.mainHandler.sendMessage(msg);
			e.printStackTrace();
			bClientThreadStart = false;
		} catch (NumberFormatException e) {

		}
	}

	void initChildHandler() {
		
		Looper.prepare();  //�����߳��д���Handler�����ʼ��Looper

		childHandler = new Handler() {
			//���߳���Ϣ��������
			public void handleMessage(Message msg) {

				//�������̼߳������̵߳���Ϣ������...
				switch (msg.what) {
				case TX_DATA:
					int len = msg.arg1;				

					try {
						outputStream.write((byte [])msg.obj, 0, len);
						outputStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;

				case RX_EXIT:
					RxFlag = false;
					try {
						if (socket.isConnected()) {
							inputStream.close();
							outputStream.close();
							socket.close();
						}
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					childHandler.getLooper().quit();// ������Ϣ����

					break;

				default:
					break;
				}

			}
		};

		// �������̵߳���Ϣ����
		Looper.loop();

	}

	public void run() {
		connect();
		initChildHandler();
		msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI, "��������Ͽ�����");
		MainActivity.mainHandler.sendMessage(msg);
	}
		
	//socket �����߳�
	public class RxThread extends Thread {
		public void run() {
			int i=0;
			byte ParseBuf[] = new byte[256];
			byte ParseLen=0;
			
			try {
				while (socket.isConnected() && RxFlag) {
					byte RxBuf[] = new byte[256];
					int len = inputStream.read(RxBuf);

					if(false){
						continue;
					}


					msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI,
						"="+RxBuf[0]+","+RxBuf[1]+","+RxBuf[2]+","+RxBuf[3]+","+RxBuf[4]+","+RxBuf[5]+","+RxBuf[6]+","+RxBuf[7]+","+RxBuf[8]+","+RxBuf[9]+","+RxBuf[10]+",ParseLen="+ParseLen);
				//	MainActivity.mainHandler.sendMessage(msg);
					
					
					if(len>0)
					{
						ParseframeData(RxBuf, (byte)len);
/*						
						for(i=0; i<len; i++)
						{
							if(RxBuf[i]=='\r' || RxBuf[i]=='\n')//���͵���������"\r\n"��β��
							{
								if(ParseLen>0)
								{
									msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI,
										"="+ParseBuf[0]+","+ParseBuf[1]+","+ParseBuf[2]+","+ParseBuf[3]+","+ParseBuf[4]+","+ParseBuf[5]+","+ParseBuf[6]+","+ParseBuf[7]+","+ParseBuf[8]+","+ParseBuf[9]+","+ParseBuf[10]+",ParseLen="+ParseLen);
									MainActivity.mainHandler.sendMessage(msg);
								
									ParseframeData(ParseBuf, ParseLen);
								}

								ParseLen=0;
							}else{
								ParseBuf[ParseLen]=RxBuf[i];
								ParseLen++;
							}
						}
*/						
					}else{
						msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI,
						"��������Ͽ�����");
						MainActivity.mainHandler.sendMessage(msg);

						//�˳������߳�
						msg = childHandler.obtainMessage(RX_EXIT);
						childHandler.sendMessage(msg);
						break;
					}					
				}
				
				if (socket.isConnected())
					socket.close();
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void ParseframeData(byte data[], byte len)
	{
	    byte dataLen=0;
	    byte fc=0;
	    byte addr=0;
		byte i=0,j=0;
		byte sum=0;
		
	    if(len==0) return;

	    //���Ȳ�һ��
	    if(data[0]!=len) return;

	    //У��Ͳ�һ��
	    dataLen=(byte) (data[0]-2);
		sum=CheckSum(data, dataLen);
	    if(data[1]!=sum) return;

	    //������
	    fc=data[2];

		//msg = MainActivity.mainHandler.obtainMessage(MainActivity.TIPS_UPDATE_UI,
		//		"h"+data[0]+","+data[1]+","+data[2]+","+data[3]+","+data[4]+","+data[5]+","+data[6]+","+data[7]+","+data[8]+","+data[9]+","+data[10]+",fc="+fc+"sum="+sum+"len="+len);
		//MainActivity.mainHandler.sendMessage(msg);

		//if(true) return;
	    
	    //�ַ�����
	    switch(fc)
    	{
			case FUN_CODE_CHECK_ALL_DATA://�ֻ�/PC-->zigbee ��ѯ���д���������
			break;
			case FUN_CODE_UPDATA_ALL_DATA://zigbee-->�ֻ�/PC �ϴ����д���������
			{				
				i=3;
				//�ն�1
			    MainActivity.endDevInfo.end1_light=data[i++]; //����
			    MainActivity.endDevInfo.end1_temp=data[i++];  //�¶�
			    MainActivity.endDevInfo.end1_hum=data[i++];  //ʪ��

				MainActivity.endDevInfo.wenduLimit=data[i++];//�¶ȷ�ֵ
				MainActivity.endDevInfo.shiduLimit=data[i++];//ʪ�ȷ�ֵ
				MainActivity.endDevInfo.lightLimit=data[i++];//���շ�ֵ


			//2���ն�2:������(�Ŵ�)�����塢��ʪ�Ⱥͼ̵�����
			    MainActivity.endDevInfo.end2_people=data[i++]; //����
			    MainActivity.endDevInfo.end2_mq2=data[i++]; //����
			    MainActivity.endDevInfo.end2_temp=data[i++];  //�¶�
			    MainActivity.endDevInfo.end2_hum=data[i++];  //ʪ��
			    MainActivity.endDevInfo.end2_lamp=data[i++];  //�̵��������Ƶ�״̬

			//3���ն�3��RFIDˢ��ϵͳ,ͳ�Ƴ��⳵�������������5�ſ���ɡ�
			    //RFID���ü�¼����
			    //ˢ����ֱ���ϴ�
			    
			//4���ն�4���������,��ת����ת��ֹͣ���Ӽ��١�
			    //û�����ݴ洢

			//5���ն�5: ��GPS���ϴ���γ��,ʱ�䣬�ٶ�,�Զ��ϴ���3��1�Ρ�
				
				System.arraycopy(data, i, MainActivity.endDevInfo.gpsData, 0, 22);
				
				msg = MainActivity.mainHandler.obtainMessage(MainActivity.RX_DATA_UPDATE_UI,"Connect");
				MainActivity.mainHandler.sendMessage(msg);
			}
			break;
			case FUN_CODE_UPDATA_RFID://zigbee-->�ֻ�/PC  �ϴ�RFID����
			{
				byte card_buff[]=new byte[8];
				String strBuff;
				
				System.arraycopy(data, 3, card_buff, 0, 8);

				msg = MainActivity.mainHandler.obtainMessage(MainActivity.RX_RFID_UPDATE_UI, new String(card_buff));
				MainActivity.mainHandler.sendMessage(msg);
			}
			break;
			case ZIGBEE_FUN_CODE_CTRL_LAMP://���Ƶ����ն��ϵƵ�״̬
			break;
			case ZIGBEE_FUN_CODE_STEP://�����ն��ϵĲ������
			break;
			case ZIGBEE_FUN_CODE_END1://�ն�1���ն��룬�����ն˵ķ�ֵ�Ϳ��ص�� 
			break;
    	}
	}

	byte CheckSum(byte pdata[], byte len)
	{
		byte i;
		byte check_sum=0;

		for(i=0; i<len; i++)
		{
			check_sum += pdata[2+i];
		}
		return check_sum;
	}
	

}
