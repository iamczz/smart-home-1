package com.android_wifi;

public class EndDeviceDataInfo{

//1���ն�1����������ʪ�ȡ���ֵ������--���촰ɹ̫�����¶ȹ���--ͨ�磬ʪ�ȹ���----��ˮ��
    byte end1_light; //����
    byte end1_temp;  //�¶�
    byte end1_hum;  //ʪ��

	//�ն�1�ķ�ֵ����
    byte wenduLimit;//�¶ȷ�ֵ
    byte shiduLimit;//ʪ�ȷ�ֵ
    byte lightLimit;//���շ�ֵ


//2���ն�2:������(�Ŵ�)�����塢��ʪ�Ⱥͼ̵�����
    byte end2_people; //����
    byte end2_mq2; //����
    byte end2_temp;  //�¶�
    byte end2_hum;  //ʪ��
    byte end2_lamp;  //�̵��������Ƶ�״̬

//3���ն�3��RFIDˢ��ϵͳ,ͳ�Ƴ��⳵�������������5�ſ���ɡ�
    //RFID���ü�¼����
    //ˢ����ֱ���ϴ�
    
//4���ն�4���������,��ת����ת��ֹͣ���Ӽ��١�
    //û�����ݴ洢

//5���ն�5: ��GPS���ϴ���γ��,ʱ�䣬�ٶ�,�Զ��ϴ���3��1�Ρ�
    byte gpsData[];


	public EndDeviceDataInfo(){
		gpsData=new byte[22];
	}

	public int getSize()
	{
		//C++��sizeof����֪��������ô��
		return 30;
	}
}
