package com.easeic.elcontrol;

import android.util.Log;

import org.w3c.dom.Element;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by sam on 2016/4/3.
 */
public class CTCPTransport extends CTransport implements Runnable {
    public String transportIP;
    public int transportPort = 1024;
    public int subNetID = 1;

    static boolean forDebug = false;

    final static byte[] S_KEEP = {0x45, 0, 17, 0x41,4, 0, 0, 0, (byte) 0xd2, (byte) 0x92, 0, 0, (byte) 0xf0, 0, 0, 1, 0x0f};
    byte[] mCheckData = {0x45,0,0,0x41,0x04,0,0,0,(byte)0xd2,(byte)0xb7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    boolean running = false;
    boolean mConnected = false;
    long tickACK;
    long tickAlive;
    Vector<byte[]> arrayWrites = new Vector<>();
    final static int BUFFERSIZE = 1024;
    ByteBuffer readBuffer = ByteBuffer.allocate(BUFFERSIZE);

    Vector<CUIBase> arrayBases = new Vector<>();

    public String   mUser = null;
    public String   mPsw = null;
    boolean mChecked = false;

    public CTCPTransport() {
        readBuffer.limit(0);
    }

    final static String S_PARAM = "Param";

    public static CTCPTransport transportFromXML(Element xmlData, CProject project) {
        CTCPTransport transport = new CTCPTransport();

        transport.mProject = project;
        transport.mID = Integer.parseInt(xmlData.getAttribute(S_ID));
        transport.mName = xmlData.getAttribute(S_NAME);
        transport.mNote = xmlData.getAttribute(S_NOTE);
        transport.decoderString(xmlData.getAttribute(S_PARAM));

        return transport;
    }

    public void login(String user,String psw){
        if(user == null || user.length()<1 || user.length()>16
                || psw == null || psw.length()<1 || psw.length()>8)
            return;
        if(mUser != null && mPsw != null && user.compareTo(mUser)==0 && psw.compareTo(mPsw) == 0)
            return;

        mUser = user;
        mPsw = psw;

        byte[] userData = mUser.getBytes();
        System.arraycopy(userData,0,mCheckData,12,userData.length>16?16:userData.length);
        byte[] pswData = mPsw.getBytes();
        System.arraycopy(pswData,0,mCheckData,28,pswData.length>8?8:pswData.length);
        mCheckData[1] = 0;
        mCheckData[2] = 37;
        mCheckData[36] = CBase.calcCheck(mCheckData,36);

        mChecked = false;
    }

    @Override
    public void run() {
        long mtickAck = System.currentTimeMillis();
        long tickAlive = 0;
        SocketChannel sc = null;
        Selector selector = null;
        String address = transportIP;
        if (address == null)
            return;
        int port = transportPort;
        if(port == 0)
            return;
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        if(socketAddress == null || socketAddress.isUnresolved())
            return;
        long scCloseTickcount = System.currentTimeMillis();
        while (running) {
            //
            if (sc == null) {
                if(mConnected)
                    setConnected(false);
                try {
                    sc = SocketChannel.open();
                    sc.configureBlocking(false);
                    if (selector == null) {
                        selector = Selector.open();
                    }
                    sc.register(selector, SelectionKey.OP_READ);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (sc == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                continue;
            }//end sc
            //connect
            if (!sc.isConnected() && !sc.isConnectionPending()) {
                try {
                    if (sc.connect(socketAddress)) {
                        if (sc.finishConnect()) {
                            tickAlive = System.currentTimeMillis();
                            tickACK = tickAlive;
                            //mConnected = true;
                        }
                    }
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                    try {
                        sc.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    sc = null;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    continue;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    long tickCount = System.currentTimeMillis();
				/*	if((tickCount-scCloseTickcount) > 2000){
						scCloseTickcount = tickCount;
						try {
							sc.close();
						} catch (IOException e1) {
						// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						sc = null;
					}
*/
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    continue;
                }
            } else if (!sc.isConnected()) {
                try {
                    if (sc.finishConnect()) {
                        tickAlive = System.currentTimeMillis();
                        tickACK = tickAlive;
                       // mConnected = true;
                    }
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    //e1.printStackTrace();
                    long tickCount = System.currentTimeMillis();
					/*if((tickCount-scCloseTickcount) > 2000){
						scCloseTickcount = tickCount;
						try {
							sc.close();
						} catch (IOException e) {
						// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sc = null;
					}*/

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    continue;
                }
            }
            if (sc.isConnected()) {
                try {
                    if (!forDebug && (System.currentTimeMillis() - tickAlive) > 21000) {
                        try {
                            setConnected(false);
                            sc.close();
                        } catch (IOException ee) {

                        }
                        sc = null;
                        continue;
                    }

                    if (selector.select(10) > 0) {
                        Set<SelectionKey> keys = selector.selectedKeys();

                        Iterator<SelectionKey> it = keys.iterator();

                        while (it.hasNext()) {
                            SelectionKey sk = (SelectionKey) it.next();
                            it.remove();

                            if (sk.isReadable()) {
                                if (readData(sc) <= 0) {
                                    try {
                                        sc.close();
                                    } catch (IOException ee) {

                                    }
                                    sc = null;
                                    continue;
                                }

                                tickAlive = System.currentTimeMillis();
                                tickACK = tickAlive;

/*                                Handler handler = mWs.getRWHandler();
                                if(handler != null ){
                                    Message msg = handler.obtainMessage(ICSWorkspace.MESSAGE_READ,this);

                                    handler.sendMessage(msg);
                                }*/
                            }
                        }//end while
                        if (sc == null)
                            continue;
                    }

                    //write
                    writeData(sc);

                    if (mConnected && (System.currentTimeMillis() - tickACK) > 10000) {
                        tickACK = System.currentTimeMillis();
                        //need query
                        if (!forDebug)
                            sc.write(ByteBuffer.wrap(S_KEEP));
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }//end while
        if(sc != null)
        {
            try {
                Log.i("tcptransport","thread end ,sc close");
                sc.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int send(byte[] data) {
        if (!isConnected())
            return 0;
        synchronized (arrayWrites) {
            arrayWrites.add(data);
        }

        return data.length;
    }

    @Override
    public byte[] recv() {
        synchronized (readBuffer) {
            if (readBuffer.limit() <= 0)
                return null;
            byte[] buf = readBuffer.array();
            byte[] ret = new byte[readBuffer.limit()];
            System.arraycopy(buf, 0, ret, 0, readBuffer.limit());
            readBuffer.limit(0);
            readBuffer.position(0);

            return ret;
        }
    }

    @Override
    public void flush() {
        synchronized (arrayWrites) {
            arrayWrites.clear();
        }
        synchronized (readBuffer) {
            readBuffer.limit(0);
            readBuffer.position(0);
        }
    }

    @Override
    public boolean isConnected() {
        return mConnected;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        running = true;
        new Thread(this).start();
    }

    @Override
    public void stop() {
        running = false;
        mConnected = false;
        Log.i("Tcptrasnport","stop");
    }

    @Override
    public boolean decoderString(String param) {
        String[] strings = param.split(":");
        if (strings.length < 2)
            return false;
        transportIP = strings[0];
        transportPort = Integer.parseInt(strings[1]);
        if(strings.length>2)
            subNetID = Integer.parseInt(strings[2]);

        return true;
    }

    @Override
    public void editUI(CUIBase ui) {
        super.editUI(ui);

        if(arrayBases == null)
            arrayBases = new Vector<CUIBase>();
        for (CUIBase item :
                arrayBases) {
            if(item == ui)
                return;
        }

        arrayBases.add(ui);

    }

    void setConnected(boolean bConnect){
        if(!bConnect)
            mChecked = false;
        if(mConnected == bConnect)
            return;
        mConnected = bConnect;
        for (CUIBase item :
                arrayBases) {
            item.updateUI();
        }
    }

    int readData(SocketChannel sc) throws IOException {
        synchronized (readBuffer) {
            int oldLen = readBuffer.limit();
            if (oldLen == BUFFERSIZE)
                oldLen = 0;
            ByteBuffer buffer = ByteBuffer.allocate(BUFFERSIZE);
            int len = sc.read(buffer);
            if (len > 0) {

                byte[] raw = decodeTCP(buffer.array(),len);
                if (raw != null) {
                    readBuffer.limit(BUFFERSIZE);
                    readBuffer.position(oldLen);

                    int newLen = oldLen + raw.length;
                    readBuffer.put(raw);

                    readBuffer.limit(newLen);
                    readBuffer.position(0);
                }
            }//end len>0

            return len;
        }
    }

    void writeData(SocketChannel sc) throws IOException {
        if(!mChecked)
        {
            if(mUser == null || mPsw == null)
            {
                mChecked = true;
                return;
            }

            sc.write(ByteBuffer.wrap(mCheckData));
            tickACK = System.currentTimeMillis();
            mChecked = true;
            return;
        }
        byte[] data = null;
        while (true) {
            synchronized (arrayWrites) {
                if (arrayWrites.size() < 1)
                    return;
                data = arrayWrites.firstElement();
                arrayWrites.remove(0);
            }
            if (data == null)
                return;
            sc.write(ByteBuffer.wrap(encodeTCP(data)));
            data = null;
        }
    }

    byte[] decodeTCP(byte[] buf,int nLen) {
        if (nLen < 8)
            return null;
        int pos = 0;
        while (pos < nLen) {
            if (buf[pos] == 0x45) {
                if ((pos + 3) > nLen)
                    break;
                if (buf[pos + 3] != 0x41 || (buf[pos + 4] != 2 && buf[pos + 4] != 5)) {
                    pos++;
                    continue;
                }
                int len = buf[pos + 1] * 0x100 + buf[pos + 2];
                if ((pos + len) > nLen)
                    break;
                byte[] ret = new byte[len-9];
                System.arraycopy(buf, pos+8, ret, 0, len-9);
                if(ret[0]==(byte)(0xd2&0xff) && ret[1] == (byte)0xb8)
                {
                    setConnected(ret[7] == 1);
                    mChecked = true;
                    return null;
                }
                return ret;
            }
            else
                pos++;
        }
        return null;
    }


    byte[] encodeTCP(byte[] buf) {
        int newLength = 9 + buf.length;
        byte[] ret = new byte[newLength];
        ret[0] = 0x45;
        ret[1] = (byte) (newLength >> 8);
        ret[2] = (byte) (newLength & 0xff);
        ret[3] = 0x41;
        ret[4] = 1;
        ret[5] = 0;
        ret[6] = (byte)subNetID;
        ret[7] = 0;
        System.arraycopy(buf, 0, ret, 8, buf.length);
        ret[newLength - 1] = CBase.calcCheck(ret, newLength - 1);

        return ret;

    }
}