package HttpProxy;
import java.io.*;  
import java.net.*;  
public class MyHttpProxy extends Thread {   
    static public int CONNECT_RETRIES=5;    //������Ŀ���������Ӵ���  
    static public int CONNECT_PAUSE=5;  //ÿ�ν������ӵļ��ʱ��  
    static public int TIMEOUT=5000;   //ÿ�γ������ӵ����ʱ��  
    static public int BUFSIZ=1024;  //����������ֽ���  
    static public boolean logging = false;  //�Ƿ��¼��־  
    static public OutputStream log_S=null;  //��־�����  
    static public OutputStream log_C=null;  //��־�����  
    static public String LOGFILENAME_S="log_S.txt";  
    static public String LOGFILENAME_C="log_C.txt";  
    static public String[] sever_buffer;
    static public String[] sheildURL={"https://www.baidu.com/","http://jwc.hit.edu.cn/"};//������վ�ļ���
    static public String[] fishingURL={"http://www.pku.edu.cn/","http://www.hit.edu.cn/"};//������վ�ļ���
    static public String[] sheildIP={"128.0.0.1"};//�����û��ļ���
   
    // ��ͻ���������Socket  
    protected Socket csocket;     
    public MyHttpProxy(Socket cs) {   
        csocket=cs;  
        start();   
        }  
        public void writeLog(int c, boolean browser) throws IOException {  
            if(browser) log_C.write((char)c);  
            else log_S.write((char)c);  
        }  
      
        public void writeLog(byte[] bytes,int offset, int len, boolean browser) throws IOException {  
        for (int i=0;i<len;i++)   
            writeLog((int)bytes[offset+i],browser);  
        }  
        public void run(){  
            String buffer = "";     //��ȡ����ͷ  
            String URL="";          //��ȡ����URL  
            String host="";         //��ȡĿ������host  
            String ip="";           //�û�ip
            int port=80;            //Ĭ�϶˿�80  ��http
            Socket ssocket = null;  
             //cisΪ�ͻ�����������sisΪĿ������������  
            InputStream cis = null,sis=null;  
             //cosΪ�ͻ����������sosΪĿ�����������  
            OutputStream cos = null,sos=null;             
            try{  
                csocket.setSoTimeout(TIMEOUT);  
                cis=csocket.getInputStream();  
                cos=csocket.getOutputStream();
             
                while(true){  
                    int c=cis.read();  
                    if(c==-1) break;        //-1Ϊ��β��־  
                    if(c=='\r'||c=='\n') break;//�����һ������  
                    buffer=buffer+(char)c;  
                    if (logging) writeLog(c,true);  
                }  
                   
            URL=getRequestURL(buffer);  
            System.out.println(URL);
            //������վ
            for(int i=0;i<sheildURL.length;i++)
            {
            	
            	if(URL.equals(sheildURL[i]))
            	{
            		csocket.close();  
                    cis.close();  
                    cos.close(); 
            		
            		break;
            	}
            }
            ip=csocket.getInetAddress().getHostAddress();
            //�����û�
            for(int i=0;i<sheildIP.length;i++)
            {
            	
            	if(ip.equals(sheildIP[i]))
            	{
            		csocket.close();  
                    cis.close();  
                    cos.close(); 
                    break;            		
            	}
            }
            //����
            for(int i=0;i<fishingURL.length;i++)
            {
            	if(URL.equals(fishingURL[i]))
            	{
            		URL="http://today.hit.edu.cn/";            		
            		break;
            	}
            }
            System.out.println(URL);
            
            int n;  
            //���������� 
            n=URL.indexOf("//");  
            if (n!=-1)    
                            host=URL.substring(n+2);   
            n=host.indexOf('/');  
            if (n!=-1)    
                            host=host.substring(0,n);
                  
         
            n=host.indexOf(':');  
            if (n!=-1) {   
                port=Integer.parseInt(host.substring(n+1));  
                host=host.substring(0,n);  
            }  
            int retry=CONNECT_RETRIES;  
            while (retry--!=0) {  
                try {  
                        ssocket=new Socket(host,port); 
                        break;  
                    } catch (Exception e) { }  
                Thread.sleep(CONNECT_PAUSE);  
            }  
            if(ssocket!=null){  
                ssocket.setSoTimeout(TIMEOUT);  
                sis=ssocket.getInputStream();  
                sos=ssocket.getOutputStream();  
                try{
                	File file=new File(LOGFILENAME_S);
                	BufferedReader br=new BufferedReader(new FileReader(file));
                	String str=null;
                	
                	while((str=br.readLine())!=null)
                	{
                		
                		if(str.startsWith("Date:"))
                		{                			
                			break;
                		}
                	}
                	br.close();
                	int k=str.indexOf(":");
                    str=str.substring(k+1,str.length());
                    str="If-Modified-Since:"+str;     //�޸�����������if-modified-since:ʱ��              
                    k=buffer.indexOf("host:");
                    buffer=buffer.substring(0,k)+"\r\n"+str+"\r\n"+buffer.substring(k);
                    
                    sos.write(buffer.getBytes());
                    pipe(cis,sis,sos,cos);
                }catch(Exception e)
                {
                	e.printStackTrace();
                }
                sos.write(buffer.getBytes());       //������ͷд��  
                pipe(cis,sis,sos,cos);              //����ͨ�Źܵ�  
            }                 
                }catch(Exception e){  
                e.printStackTrace();  
            }  
            finally {  
            try {   
                    csocket.close();  
                    cis.close();  
                    cos.close();  
            }   
            catch (Exception e1) {  
                    System.out.println("\nClient Socket Closed Exception:");  
                    e1.printStackTrace();  
            }  
            try {   
                    ssocket.close();  
                    sis.close();  
                    sos.close();  
            }   
            catch (Exception e2) {  
                    System.out.println("\nServer Socket Closed Exception:");  
                    e2.printStackTrace();  
            }  
            }  
        }  
        public String getRequestURL(String buffer){  
            String[] tokens=buffer.split(" ");  
            String URL="";  
            for(int index=0;index<tokens.length;index++){  
                if(tokens[index].startsWith("http://")){  
                    URL=tokens[index];  
                    break;  
                }  
            }  
            return URL;       
        }  
        public void pipe(InputStream cis,InputStream sis,OutputStream sos,OutputStream cos){  
            try {  
                int length;  
                byte bytes[]=new byte[BUFSIZ];
                while (true) {  
                	
                    try { 
                    	if ((length=cis.read(bytes))>0) {  
                            sos.write(bytes,0,length);  //���ͻ��������͸���������
                            if (logging) writeLog(bytes,0,length,true);                       
                        }  
                        else if (length<0)  
                            break;   
                    }  
                    catch(SocketTimeoutException e){}  
                    catch (InterruptedIOException e) {   
                        System.out.println("\nRequest Exception:");  
                        e.printStackTrace();  
                    } 
                    
                    try {  
                    	 if ((length=sis.read(bytes))>0) {  //��ȡ�������˵���Ӧ����
                        	String str=new String(bytes);
                        	
                        	if(str.startsWith("HTTP/1.1 304"))//�ж���Ӧ���ĵ���Ӧ״̬�Ƿ�Ϊ304������ʹ�ñ��ػ���
                        	{
                        		System.out.println("ʹ�û���");
                        		InputStream filein=new FileInputStream(LOGFILENAME_S);
                        		filein.read(bytes);
                            	cos.write(bytes);
                            	filein.close();
                        	}
                        	else{
                        		cos.write(bytes,0,length);  
                        		if (logging) writeLog(bytes,0,length,false);
                        	}          
                        }  
                        else if (length<0)   
                            break;  
                    }  
                    catch(SocketTimeoutException e){}  
                    catch (InterruptedIOException e) {  
                        System.out.println("\nResponse Exception:");  
                        e.printStackTrace();  
                    }  
                } 
            } catch (Exception e0) {  
                System.out.println("Pipe�쳣: " + e0);  
            }  
        } 
        public static  void startProxy(int port,Class clobj) {   
            try {   
                ServerSocket ssock=new ServerSocket(port);   
                while (true) {   
                Class [] sarg = new Class[1];   
                Object [] arg= new Object[1];   
                sarg[0]=Socket.class;   
                try {   
                java.lang.reflect.Constructor cons = clobj.getDeclaredConstructor(sarg);   
                arg[0]=ssock.accept();   
                cons.newInstance(arg); 
                } catch (Exception e) {   
                Socket esock = (Socket)arg[0];   
                try { 
                	esock.close();
                } catch (Exception ec) {}   
                }   
                }   
            } catch (IOException e) {   
            System.out.println("\nStartProxy Exception:");   
            e.printStackTrace();   
            }   
            }   
                    
            static public void main(String args[]) throws FileNotFoundException {   
            System.out.println("�ڶ˿�8080�������������\n");   
            OutputStream file_S=new FileOutputStream(new File(LOGFILENAME_S));   
            OutputStream file_C=new FileOutputStream(new File(LOGFILENAME_C));   
            MyHttpProxy.log_S=file_S;   
            MyHttpProxy.log_C=file_C;   
            MyHttpProxy.logging=true;   
            MyHttpProxy.startProxy(8080,MyHttpProxy.class);   
            }  
    }  
