package org.braveopendwarves.data;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.*;

/** Read-only reader for the observed GaveOverGames v3 sequential archive. */
public final class GogArchive implements Closeable {
    public static final int MAX_FILE=128*1024*1024, MAX_ENTRY=32*1024*1024;
    public static final class Entry {
        public final String name; public final int size,packedSize; public final long offset;
        Entry(String n,int s,int p,long o){name=n;size=s;packedSize=p;offset=o;}
    }
    private final RandomAccessFile file;
    private final LinkedHashMap<String,Entry> entries=new LinkedHashMap<String,Entry>();
    public final String title;
    public GogArchive(File source) throws IOException {
        file=new RandomAccessFile(source,"r");String parsedTitle;
        try {
            long length=file.length();
            if(length<256||length>MAX_FILE)throw new IOException("Nieprawidlowy rozmiar archiwum");
            byte[] header=new byte[256];file.readFully(header);
            if(!cstring(header,0,32).equals("GaveOverGames' Resource File"))throw new IOException("To nie jest archiwum Gnomes.dat");
            byte[] version={3,0,0,1,0,0};
            for(int i=0;i<6;i++)if(header[32+i]!=version[i])throw new IOException("Nieobslugiwana wersja archiwum");
            int count=le32(header,38);
            if(count<1||count>8192)throw new IOException("Nieprawidlowa liczba zasobow");
            parsedTitle=cstring(header,42,214);long position=256;
            for(int i=0;i<count;i++){
                if(position+23>length)throw new IOException("Urwany katalog");
                file.seek(position);byte[] record=new byte[22];file.readFully(record);
                long start=uint(le32(record,0)),end=uint(le32(record,8));
                int tag=le32(record,4),raw=le32(record,12),packed=le32(record,16);
                if(start!=position+22||tag!=0x001420a0||raw<0||raw>MAX_ENTRY||packed<1||packed>MAX_ENTRY||end<=start||end>length)
                    throw new IOException("Nieprawidlowy rekord "+i);
                ByteArrayOutputStream nameBytes=new ByteArrayOutputStream();int ch;
                while((ch=file.read())!=0){
                    if(ch<0||file.getFilePointer()>=end||nameBytes.size()>=512)throw new IOException("Urwana nazwa zasobu");
                    nameBytes.write(ch);
                }
                String name=new String(nameBytes.toByteArray(),Charset.forName("windows-1252")).replace('\\','/');
                if(name.length()==0||name.startsWith("/")||name.indexOf(':')>=0)throw new IOException("Nieprawidlowa nazwa zasobu");
                for(String part:name.split("/",-1))if(part.equals("..")||part.equals(".")||part.length()==0)throw new IOException("Nieprawidlowa sciezka zasobu");
                long offset=file.getFilePointer();
                if(offset+packed+1!=end)throw new IOException("Niezgodny zakres zasobu "+name);
                file.seek(end-1);if(file.read()!=0)throw new IOException("Nieprawidlowy koniec rekordu");
                String key=key(name);if(entries.containsKey(key))throw new IOException("Powtorzona nazwa zasobu");
                entries.put(key,new Entry(name,raw,packed,offset));position=end;
            }
            if(position!=length)throw new IOException("Nadmiarowe dane za katalogiem");
        }catch(IOException|RuntimeException failure){file.close();throw failure;}
        title=parsedTitle;
    }
    public List<Entry> entries(){return Collections.unmodifiableList(new ArrayList<Entry>(entries.values()));}
    public boolean contains(String name){return entries.containsKey(key(name));}
    public synchronized byte[] read(String name) throws IOException {
        Entry entry=entries.get(key(name));if(entry==null)throw new FileNotFoundException(name);
        byte[] packed=new byte[entry.packedSize];file.seek(entry.offset);file.readFully(packed);
        byte[] result=new byte[entry.size];Inflater inflater=new Inflater();
        try {
            inflater.setInput(packed);int done=0;
            while(done<result.length&&!inflater.finished()){
                int got=inflater.inflate(result,done,result.length-done);
                if(got==0)throw new IOException("Niepelny strumien zlib: "+name);done+=got;
            }
            byte[] extra=new byte[1];int overflow=inflater.inflate(extra);
            if(done!=result.length||overflow!=0||!inflater.finished()||inflater.getRemaining()!=0)throw new IOException("Niezgodna dlugosc zlib: "+name);
            return result;
        }catch(DataFormatException e){throw new IOException("Uszkodzony zlib: "+name,e);}finally{inflater.end();}
    }
    public static int le16(byte[] b,int p){return (b[p]&255)|((b[p+1]&255)<<8);}
    public static int le32(byte[] b,int p){return le16(b,p)|(le16(b,p+2)<<16);}
    private static long uint(int i){return i&0xffffffffL;}
    private static String key(String s){return s.replace('\\','/').toLowerCase(Locale.ROOT);}
    private static String cstring(byte[] b,int start,int max){int n=0;while(n<max&&b[start+n]!=0)n++;return new String(b,start,n,Charset.forName("windows-1252"));}
    @Override public void close() throws IOException{file.close();}
}
