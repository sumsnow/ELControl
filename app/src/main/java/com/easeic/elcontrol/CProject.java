package com.easeic.elcontrol;

import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.util.Size;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by sam on 2016/3/24.
 */
public class CProject extends CBase implements Runnable{
    public String stringPathFile;
    public String stringNote;
    public String stringEditDate;
    public String stringResource;
    public CLocation m_location;
    public int  defaultPage = 0;

    //singgleton
    public static CProject sProject = null;
    public static boolean alreadyLoad = false;
    public static CProject createProject(){
        alreadyLoad = true;
        return (sProject = createProject(null));
    }
    @Override
    public void run() {
        while(running){
            CFunctionRun fr = null;

            synchronized (arrayFunctionRuns){
                if(arrayFunctionRuns.size()>0)
                {
                    fr = arrayFunctionRuns.elementAt(0);
                    arrayFunctionRuns.remove(0);
                }
            }

            if(fr != null && fr.funBase != null)
            {
                synchronized (arrayFunctionRuns){
                    Iterator<CFunctionRun> itr = arrayFunctionRuns.iterator();
                    while(itr.hasNext())
                    {
                        CFunctionRun item = itr.next();
                        if(item.funBase == fr.funBase && item.mRun)
                            itr.remove();
                    }
                    /*for (CFunctionRun item : arrayFunctionRuns){
                        if(item.funBase == fr.funBase && item.mRun)
                            arrayFunctionRuns.remove(item);
                    }*/
                }

                if(fr.mRun)
                {
                    int ret = fr.execute();
                    if(ret == 1)
                        synchronized (arrayFunctionRuns){
                            arrayFunctionRuns.add(fr);
                        }
                }
            }
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void updateUIsFromBus(int bus,byte[] buf){
        if(buf == null || buf.length<1)
            return;
        for (CUIPage page :
                arrayPages) {
            page.updateDataFromBus(bus,buf);
        }
    }

    public void start(){
        if(running)
            return;
/*        if(mView != null)
        {
            MainActivity act = (MainActivity)mView.getContext();
            act.setTitle(mName);
        }*/
        running = true;
        for (CTransport item:arraryTransports)
            item.start();
        new Thread(this).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(running)
                {
                    for (CTransport transport:arraryTransports) {
                        byte[] data = transport.recv();
                        updateUIsFromBus(transport.mID,data);
                        if(data != null && data.length>=8)
                            decoder(transport.mID,data);
                    }

                    try{
                        Thread.sleep(50);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        if(ELConfigure.mHomepage>0 && mName.compareTo(ELConfigure.mProjectName)==0 && getPage(ELConfigure.mHomepage)!=null)
            setActivePage(ELConfigure.mHomepage);
        else
            setActivePage(defaultPage);
    }

    public void stop(){
        if(!running)
            return ;
        running = false;
        for (CTransport item:arraryTransports)
            item.stop();
    }

    public boolean running = false;
    public Point sizePage = new Point(320,568);

    public ITSView  mView;

    public Vector<CTransport>    arraryTransports = new Vector<>();
    public Vector<CUIPage> arrayPages = new Vector<>();
    public Vector<CPageEvent>       arrayEvents = new Vector<>();
    public Vector<CPageTask>        arrayTasks = new Vector<>();
    public Vector<CPageTiming>      arrayTimings = new Vector<>();
    public Vector<CChannelValue>    arrayChannelValues = new Vector<>();
    public Vector<CFunctionRun>     arrayFunctionRuns = new Vector<>();

    String stringPath = "";

    CUIPage activePage = null;

    public SoundPool mSnd = null;
    public int mSoundID = -1;

    public static CProject createProject(ITSView view){
        CProject ret = new CProject(view);
        if(ret.loadProject())
            return ret;
        return null;
    }

    public CProject(ITSView view) {
        mView = view;
        if (Build.VERSION.SDK_INT >= 21){
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
             mSnd = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
        }
        else
            mSnd = new SoundPool(2,AudioManager.STREAM_MUSIC,0);
        mSoundID = mSnd.load(MyApplication.getAppContext(), R.raw.start, 1);

    }

    public Point getSize()
    {
        return sizePage;
    }

    public void uiSend(CFunction fun){
        if(fun == null)
            return;
        switch (fun.mFunction){
            case 0://direct send
                if(fun.mBus>0 && fun.codeData != null && fun.codeData.length>0)
                    uiSend(fun.mBus,fun.codeData);
                break;
            case 1:
                runUIFunction(fun.mFunction,fun.mEvent,fun.mRunning);
                break;
            case 2:
                runUIFunction(fun.mFunction,fun.mTask,fun.mRunning);
                break;
            case 3:
                runUIFunction(fun.mFunction,0,fun.mRunning);
        }
    }

    public void uiSend(int transport,byte[] buf){
        //syn cmd
        updateUIsFromBus(transport,buf);
        CTransport obj = getTransport(transport);
        if(obj != null && buf !=null && buf.length>0) {

            byte[] tmpBuf = new byte[buf.length];
            System.arraycopy(buf,0,tmpBuf,0,buf.length);

            Log.i("project",String.format("buf lenth %d,buf[0] %x",buf.length,buf[0]));
            if(buf.length == 8 && buf[0] == (byte)0xd2)
                decoder(transport,tmpBuf);
            obj.send(tmpBuf);
        }
    }

    public void decoder(int transport,byte[] buf){
        switch (buf[1]){
            case 1:
                setChannelValue(transport,(int)(buf[2]&0xff),(int)(buf[3]&0xff),0);
                break;
            case 2:
                setChannelValue(transport,buf[2]&0xff,(int)(buf[3]&0xff),buf[6]&0xff);
                break;
            case 7:
                setChannelValue(transport,buf[2]&0xff,buf[3]&0xff,buf[6],buf[6] == 2,true);
                break;
            case 0x15:
            case 0x2a:
            case 0x33:
                setChannelValue(transport,buf[2]&0xff,buf[3]&0xff,buf[5]&0xff);
                break;
            case 0x28:
            {
                if(buf[2]>0 && buf[3]>0){
                    CChannelValue cv = getChannelValue(transport,buf[2]&0xff,buf[3]&0xff);
                    if(cv == null)
                    {
                        //new cv
                        cv = addChannelValue(transport,buf[2]&0xff,buf[3]&0xff);
                    }
                    if(cv != null)
                    {
                        // is setemp

                            cv.handleTempCmd(buf);

                    }
                }
            }
            break;
            case 0x3f:
            {
                if(buf[2]>0 && buf[3]>0){
                    CChannelValue cv = getChannelValue(transport,buf[2]&0xff,buf[3]&0xff);
                    if(cv == null)
                    {
                        //new cv
                        cv = addChannelValue(transport,buf[2]&0xff,buf[3]&0xff);
                    }
                    if(cv != null)
                    {
                        cv.handleTempCmd(buf);

                    }
                }

            }
            break;
            case 0x39:
            {
                setChannelValue(transport,(int)(buf[2]&0xff),(int)(buf[3]&0xff),buf[5]&0xff);
            }
            break;
        }
    }

    public CTransport getTransport(int transport){
        for(CTransport item:arraryTransports)
        {
            if(item.mID == transport)
                return item;
        }

        return null;
    }

    public void setActivePage(int page){
        CUIPage pPage = getPage(page);
        if(pPage != null) {
            activePage = pPage;
            if (activePage != null) {
                activePage.onShow();
                activePage.updateUI();
            }
        }
    }

    public CUIPage getActivePage(){
        return activePage;
    }

    public CUIPage getPage(int page)
    {
        for (CUIPage item:arrayPages)
        {
            if(item.mID == page)
                return item;
        }

        return null;
    }

    public String getResFile(String file){
        return stringPath + File.separator + stringResource + File.separator + file;
    }

    public CChannelValue addChannelValue(int bus,int area,int ch){
        CChannelValue ret = getChannelValue(bus,area,ch);
        if(ret == null) {
            ret = new CChannelValue();
            ret.mBus = bus;
            ret.mArea = area;
            ret.mCh = ch;

            arrayChannelValues.add(ret);
        }

        return ret;
    }

    public CChannelValue getChannelValue(int bus,int area,int ch){
        for (CChannelValue item:arrayChannelValues)
        {
            if (item.mBus == bus && item.mCh == ch && item.mArea == area)
                return item;
        }

        return null;
    }

    public void setChannelValue(int bus,int area,int ch,int value){
        if(area == 0 && ch == 0){
            for (CChannelValue item:arrayChannelValues)
            {
                if (item.mBus == bus )
                    item.setValue(value);
            }
        }
        else if(area == 0){
            for (CChannelValue item:arrayChannelValues)
            {
                if (item.mBus == bus && ch == item.mCh )
                    item.setValue(value);
            }
        }
        else if(ch == 0){
            for (CChannelValue item:arrayChannelValues)
            {
                if (item.mBus == bus && area == item.mArea )
                    item.setValue(value);
            }
        }
        else {
            CChannelValue cv = getChannelValue(bus, area, ch);
            if (cv != null)
                cv.setValue(value);
        }
    }

    public void setChannelValue(int bus,int area,int ch,int value,boolean invert,boolean switchValue){
        CChannelValue cv = getChannelValue(bus,area,ch);
        if(cv != null)
        {
            if(invert)
                value = cv.mValue==0?1:0;
            if(switchValue && value == 1)
                value = 255;
            cv.setValue(value);
        }
    }

    public void clickAudio(){
        if(!ELConfigure.mMute && mSoundID >= 0 && mSnd != null)
            mSnd.play(mSoundID, 1, 1, 1, 0, 1);
    }

    void runUIFunction(int functionType,int funID,boolean isRun){
        switch (functionType)
        {
            case 1:
            {
                for (CPageEvent event:arrayEvents){
                    if(event.mID == funID)
                    {
                        synchronized (arrayFunctionRuns) {
                            arrayFunctionRuns.add(new CFunctionRun(event, isRun));
                        }
                        break;
                    }
                }
                break;
            }//end event case
            case 2:
            {
                for (CPageTask task:arrayTasks){
                    if(task.mID == funID)
                    {
                        synchronized (arrayFunctionRuns) {
                            arrayFunctionRuns.add(new CFunctionRun(task, isRun));
                        }
                        break;
                    }
                }
                break;
            }//end task case
            case 3:
            {
                if(isRun)
                {
                    for (CPageTiming timing:arrayTimings)
                    {
                        boolean exist = false;
                        synchronized (arrayFunctionRuns){
                            for (CFunctionRun fr:arrayFunctionRuns)
                            {
                                if(fr.funBase == timing)
                                {
                                    exist = true;
                                    break;
                                }
                            }
                            if(!exist)
                            {
                                arrayFunctionRuns.add(new CFunctionRun(timing,true));
                            }
                        }//end arrayfunctionRuns
                    }
                }
                else
                {
                    synchronized (arrayFunctionRuns)
                    {
                        Iterator<CFunctionRun> itr = arrayFunctionRuns.iterator();
                        while(itr.hasNext())
                        {
                            CFunctionRun fr = itr.next();
                            if(fr.funBase.getFunctionClass() == CFunctionBase.functionTiming)
                                itr.remove();
                        }
                       /* for (CFunctionRun fr:arrayFunctionRuns)
                        {
                            if(fr.funBase.getFunctionClass() == CFunctionBase.functionTiming)
                                arrayFunctionRuns.remove(fr);
                        }*/
                    }
                }
                break;
            }//end timing case
        }
    }

    public boolean loadProject(){
        stringPath = ELConfigure.mSaveWhere+"/UEA";
        try {
            File f = new File(stringPath);
            File[] fs = f.listFiles();

            for (File ff : fs) {
                if (ff.isFile()) {
                    String str = ff.getName();
                    if (str.matches(".+\\.[Zz][Ee][Aa]")) {
                        // find it

                        File projectDir = new File(stringPath,"Project");
                        MainActivity.deleteDirectoryOrFile(projectDir);
                        projectDir.mkdir();

                        ZipFile zipFile = null;
                        List<FileHeader> headers = null;
                        try {
                            zipFile = new ZipFile(stringPath+File.separator+str);
                            zipFile.setFileNameCharset("GBK");
                            headers = zipFile.getFileHeaders();

                        } catch (ZipException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if(headers != null)
                        {
                            for(int i=0;i<headers.size();i++)
                            {
                                try {
                                    zipFile.extractFile(headers.get(i),(stringPath+File.separator+"Project"));

                                } catch (ZipException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }


                        }

                        File zea = new File(stringPath+File.separator+str);
                        if(!zea.delete())
                            Log.i("project","delete zea fail");

                        break;
                    }
                }
            }// end for

            stringPath += "/Project";
            f = new File(stringPath);
            fs = f.listFiles();
            for (File ff : fs) {
                if (ff.isFile()) {
                    String str = ff.getName();
                    if (str.matches(".+\\.[Uu][Ee][Aa]")) {
                        // find it
                        return loadProject(stringPath+File.separator+str);
                    }
                }
            }//end for
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    final static String S_LOCATION = 	"Location";
    final static String S_SIZE = "Size";
    final static String S_EDITDATE	 = "EditDate";
    final static String S_DEFAULTPAGE	= "DefaultPage";
    final static String S_PAGES = "Pages";
    final static String S_TRANSPORTS =	"Transports";
    final static String S_TRANSPORT = "Transport";
    final static String S_PARAM = "Param";
    final static String S_EVENTS = "Events";
    final static String S_EVENT = "Event";
    final static String S_TASKS = "Tasks";
    final static String S_TASK	 = "Task";
    final static String S_TIMINGS =	"Timings";
    final static String S_TIMING = "Timing";
    final static String S_RES	= "Resource";

    public boolean loadProject(String stringFile){
        File file = new File(stringFile);
        if(!file.isFile())
            return false;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //解析XML输入流，得到Document对象，表示一个XML文档
            Document document = builder.parse(inputStream);
            Element root =document.getDocumentElement();
            if(root == null)
                return false;

            mName = root.getAttribute(S_NAME);
            stringNote = root.getAttribute(S_NOTE);
            stringEditDate = root.getAttribute(S_EDITDATE);
            stringResource = root.getAttribute(S_RES);
            String stringSize = root.getAttribute(S_SIZE);
            if(stringSize != null)
            {
                String[] strings = stringSize.split("X");
                if(strings.length == 2){
                    sizePage = new Point(Integer.parseInt(strings[0]),Integer.parseInt(strings[1]));
                }
            }
            m_location = CLocation.locationFromString(root.getAttribute(S_LOCATION));
            defaultPage = Integer.parseInt(root.getAttribute(S_DEFAULTPAGE));

            stringPathFile = stringFile;
            //for child
            //transports
            {
                NodeList xmlTransportsList = root.getElementsByTagName(S_TRANSPORTS);
                if(xmlTransportsList != null && xmlTransportsList.getLength()>0)
                {
                    Node xmlTransports = xmlTransportsList.item(0);
                    NodeList xmlTransportList = xmlTransports.getChildNodes();
                    for (int i=0;xmlTransportList != null && i<xmlTransportList.getLength();i++)
                    {
                        Node xmlTransport = xmlTransportList.item(i);
                        if(xmlTransport.getNodeType() == Node.ELEMENT_NODE)
                        {
                            CTransport transport = CTransprotFactory.transportCreateFromXML((Element)xmlTransport,this);
                            if(transport != null)
                                arraryTransports.add(transport);
                        }
                    }
                }
            }//end transports
            //events
            {
                NodeList xmlEventsList = root.getElementsByTagName(S_EVENTS);
                if(xmlEventsList != null && xmlEventsList.getLength()>0)
                {
                    Node xmlEvents = xmlEventsList.item(0);
                    NodeList xmlEventList = xmlEvents.getChildNodes();
                    for (int i=0;xmlEventList!= null && i<xmlEventList.getLength();i++)
                    {
                        if(xmlEventList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            CPageEvent event = CPageEvent.eventCreateFromXML((Element) xmlEventList.item(i), this);
                            if (event != null)
                                arrayEvents.add(event);
                        }
                    }
                }
            }//end events
            //tasks
            {
                NodeList xmlTasksList = root.getElementsByTagName(S_TASKS);
                if(xmlTasksList != null && xmlTasksList.getLength()>0)
                {
                    Node xmlTasks = xmlTasksList.item(0);
                    NodeList xmlTaskList = xmlTasks.getChildNodes();
                    for (int i=0;xmlTaskList!= null && i<xmlTaskList.getLength();i++)
                    {
                        if(xmlTaskList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            CPageTask task = CPageTask.taskCreateFromXML((Element) xmlTaskList.item(i), this);
                            if (task != null)
                                arrayTasks.add(task);
                        }
                    }
                }
            }//end events
            //timing
            {
                NodeList xmlTimingsList = root.getElementsByTagName(S_TIMINGS);
                if(xmlTimingsList != null && xmlTimingsList.getLength()>0)
                {
                    Node xmlTimings = xmlTimingsList.item(0);
                    NodeList xmlTimingList = xmlTimings.getChildNodes();
                    for (int i=0;xmlTimingList!= null && i<xmlTimingList.getLength();i++)
                    {
                        if(xmlTimingList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            CPageTiming timing = CPageTiming.timingCreateFromXML((Element) xmlTimingList.item(i), this);
                            if (timing != null)
                                arrayTimings.add(timing);
                        }
                    }
                }
            }//end timing
            //pages
            {
                NodeList xmlPagesList = root.getElementsByTagName(S_PAGES);
                if(xmlPagesList != null && xmlPagesList.getLength()>0)
                {
                    Node xmlPages = xmlPagesList.item(0);
                    NodeList xmlPageList = xmlPages.getChildNodes();
                    for (int i=0;xmlPageList!= null && i<xmlPageList.getLength();i++)
                    {
                        if(xmlPageList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            CUIPage page = CUIPage.pageCreateFromXML((Element)xmlPageList.item(i),this);
                            if(page != null)
                                arrayPages.add(page);
                        }
                    }
                }
            }//end pages

            File f = new File(stringFile);
            stringPath = f.getParent();

            //update
            loadTCPConf();

            return  true;
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public void loadTCPConf(){
        if(ELConfigure.mTCPConfMap != null)
        {
            Iterator iter = ELConfigure.mTCPConfMap.entrySet().iterator();
            while(iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String)entry.getKey();
                String val = (String)entry.getValue();

                CTransport transport = getTransport(Integer.parseInt(key));
                if(transport == null || !(transport instanceof CTCPTransport))
                    continue;
                String[] strings = val.split(":");
                if(strings.length != 2)
                    continue;
                CTCPTransport tcp = (CTCPTransport)transport;
                tcp.login(strings[0],strings[1]);
            }
        }
    }

    public void saveTCPConf(){
        if(ELConfigure.mTCPConfMap == null)
            ELConfigure.mTCPConfMap = new HashMap<>();
        ELConfigure.mTCPConfMap.clear();

        for (CTransport item :
                arraryTransports) {
            if (item instanceof CTCPTransport) {
                CTCPTransport tcp = (CTCPTransport)item;
                if(tcp.mUser != null && tcp.mUser.length()>0 && tcp.mUser.length()<=16
                        && tcp.mPsw != null && tcp.mPsw.length()>0 && tcp.mPsw.length()<=8)
                    ELConfigure.mTCPConfMap.put(String.valueOf(tcp.mID),tcp.mUser+":"+tcp.mPsw);
            }
        }

        ELConfigure.save();
    }

}
