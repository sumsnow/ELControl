package com.easeic.elcontrol;

/**
 * Created by sam on 2016/3/28.
 */
public class CLocation {
    public boolean	m_bLongtitude; //=0,east,1=west
    public int		m_nLongAngle; //0~180
    public int		m_nLongPoint;//0~59

    public boolean	m_bLatitude;  //=0,south,=1,north
    public int		m_nLatAngle;
    public int		m_nLatPoint;

    public boolean	m_bTimeZone; //=0,-;=1,+
    public int		m_nTimeZone; //0~14

    public void	InputText(String text){
        if(text == null || text.length()<8)
            return;
        String[] strings = text.split(";");
        m_bLongtitude = Integer.parseInt(strings[0])!=0;
        m_nLongAngle = Integer.parseInt(strings[1]);
        m_nLongPoint = Integer.parseInt(strings[2]);
        m_bLatitude = Integer.parseInt(strings[3]) != 0;
        m_nLatAngle = Integer.parseInt(strings[4]);
        m_nLatPoint = Integer.parseInt(strings[5]);
        m_bTimeZone = Integer.parseInt(strings[6]) != 0;
        m_nTimeZone = Integer.parseInt(strings[7]);
    }

    public static CLocation locationFromString(String text){
        CLocation loc = new CLocation();
        loc.InputText(text);

        return loc;
    }

}
