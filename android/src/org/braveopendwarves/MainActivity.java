package org.braveopendwarves;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import java.io.*;
import java.util.*;
import org.braveopendwarves.data.*;

public final class MainActivity extends Activity {
    private GameView game;
    private OriginalGameView original;
    private AnimationPreviewView preview;
    private GogArchive archive;
    private SoundPool sounds;
    private int pickup,keyPickup;
    private final Set<Integer> loadedSounds=new HashSet<Integer>();
    private TextView status;
    private boolean importing;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(4102);
        getWindow().addFlags(128);
        File data=new File(getFilesDir(),"Gnomes.dat");
        if(data.isFile())try{archive=new GogArchive(data);prepareSound();}catch(IOException e){archive=null;}
        showHome();
    }
    private TextView label(String text,int size){TextView t=new TextView(this);t.setText(text);t.setTextSize(size);t.setTextColor(0xffe1e9e6);t.setPadding(12,7,12,7);return t;}
    private void button(LinearLayout parent,String text,View.OnClickListener click){Button b=new Button(this);b.setText(text);b.setOnClickListener(click);parent.addView(b,new LinearLayout.LayoutParams(-1,-2));}
    void showHome(){
        if(preview!=null){preview.suspend();preview=null;}
        if(original!=null){original.suspend();original=null;}if(game!=null){game.suspend();game=null;}
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff111e2e);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(30,12,30,12);scroll.addView(root);
        root.addView(label("BRAVE OPEN DWARVES  /  0.32",25));
        status=label(archive==null?"Wybierz własny plik Gnomes.dat z Brave Dwarves na Windows.":archive.title+" — "+archive.entries().size()+" zasoby",17);root.addView(status);
        root.addView(label("Pliki gry są przechowywane osobno. APK zawiera tylko nowy kod silnika.",14));
        button(root,"WYBIERZ GNOMES.DAT",v->{if(importing)return;Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.setType("*/*");intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,10);});
        if(archive!=null){
            if(RunState.exists(new File(getFilesDir(),"run.bod")))button(root,"KONTYNUUJ ZAPISANĄ ROZGRYWKĘ",v->{
                try{OriginalGameView restored=new OriginalGameView(this,archive);restored.restore();original=restored;setContentView(original);}catch(IOException e){showError(e.getMessage());}
            });
            button(root,"EKSPLORUJ ORYGINALNE POZIOMY",v->{try{original=new OriginalGameView(this,archive);setContentView(original);}catch(IOException e){showError(e.getMessage());}});
            button(root,"PRZEGLĄDAJ ORYGINALNE GRAFIKI",v->browseImages());
            button(root,"PRZEGLĄDAJ ORYGINALNE ANIMACJE",v->browseAnimations());
        }
        button(root,"URUCHOM DEMO 0.1 (WŁASNA MAPA)",v->{game=new GameView(this);setContentView(game);});
        root.addView(label("Etap: oryginalne mapy i eksperymentalna walka z pięcioma typami przeciwników. Pełna logika oryginału jest w budowie.",14));
        setContentView(scroll);
    }
    private void showError(String message){new AlertDialog.Builder(this).setTitle("Nie udało się odczytać danych").setMessage(message).setPositiveButton("OK",null).show();}
    private void browseImages(){
        ArrayList<String> names=new ArrayList<String>();for(GogArchive.Entry e:archive.entries())if(e.name.toLowerCase(Locale.ROOT).endsWith(".pcx"))names.add(e.name);
        new AlertDialog.Builder(this).setTitle("Grafiki z Twojego Gnomes.dat").setItems(names.toArray(new String[0]),(dialog,which)->{
            try{PcxImage pcx=PcxImage.decode(archive.read(names.get(which)),false);ImageView image=new ImageView(this);image.setAdjustViewBounds(true);image.setImageBitmap(Bitmap.createBitmap(pcx.argb,pcx.width,pcx.height,Bitmap.Config.ARGB_8888));ScrollView scroll=new ScrollView(this);scroll.addView(image);new AlertDialog.Builder(this).setTitle(names.get(which)).setView(scroll).setPositiveButton("ZAMKNIJ",null).show();}catch(IOException e){showError(e.getMessage());}
        }).setNegativeButton("WRÓĆ",null).show();
    }
    private void browseAnimations(){
        ArrayList<String> names=new ArrayList<String>();for(GogArchive.Entry e:archive.entries())if(e.name.toLowerCase(Locale.ROOT).endsWith(".anm"))names.add(e.name);
        new AlertDialog.Builder(this).setTitle("Animacje z Twojego Gnomes.dat").setItems(names.toArray(new String[0]),(dialog,which)->{
            try{preview=new AnimationPreviewView(this,archive,names.get(which));setContentView(preview);}catch(IOException e){showError(e.getMessage());}
        }).setNegativeButton("WRÓĆ",null).show();
    }
    @Override protected void onActivityResult(int request,int result,Intent intent){
        super.onActivityResult(request,result,intent);getWindow().getDecorView().setSystemUiVisibility(4102);
        if(request!=10||result!=RESULT_OK||intent==null||intent.getData()==null)return;
        final Uri uri=intent.getData();importing=true;status.setText("Sprawdzanie archiwum, obrazów i poziomów…");
        new Thread(()->{
            File incoming=new File(getFilesDir(),"Gnomes.importing");
            try{
                try(InputStream source=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(incoming)){
                    if(source==null)throw new IOException("Nie można otworzyć wybranego pliku");
                    byte[] buffer=new byte[32768];long total=0;int n;
                    while((n=source.read(buffer))!=-1){total+=n;if(total>GogArchive.MAX_FILE)throw new IOException("Plik jest za duży");out.write(buffer,0,n);}
                }
                try(GogArchive candidate=new GogArchive(incoming)){
                    if(!candidate.contains("Data/Items.pcx")||!candidate.contains("Data/Creatures/Gnome1.pcx"))throw new IOException("Brak wymaganych zasobów Brave Dwarves");
                    for(GogArchive.Entry e:candidate.entries()){
                        byte[] bytes=candidate.read(e.name);String lower=e.name.toLowerCase(Locale.ROOT);
                        if(lower.endsWith(".pcx"))PcxImage.decode(bytes,true);
                        if(lower.matches("data/levels/level[0-9]{4}\\.dat"))new OriginalLevel(bytes);
                    }
                }
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()){incoming.delete();return;}
                    try{
                        if(archive!=null){archive.close();archive=null;}
                        File target=new File(getFilesDir(),"Gnomes.dat");
                        if(!incoming.renameTo(target))throw new IOException("Nie można zapisać zaimportowanego pliku");
                        archive=new GogArchive(target);prepareSound();importing=false;showHome();
                    }catch(IOException e){importing=false;showHome();showError(e.getMessage());}
                });
            }catch(Exception e){incoming.delete();runOnUiThread(()->{if(isFinishing()||isDestroyed())return;importing=false;showHome();showError(e.getMessage());});}
        },"Gnomes import").start();
    }
    private void prepareSound(){
        if(sounds!=null)sounds.release();loadedSounds.clear();sounds=new SoundPool.Builder().setMaxStreams(4).build();
        sounds.setOnLoadCompleteListener((pool,id,result)->{if(result==0)loadedSounds.add(id);});
        try{
            File normal=new File(getCacheDir(),"pickup.wav"),key=new File(getCacheDir(),"key.wav");
            try(OutputStream out=new FileOutputStream(normal)){out.write(archive.read("Data/Sound/DiamodPickup.wav"));}
            try(OutputStream out=new FileOutputStream(key)){out.write(archive.read("Data/Sound/KeyPickup.wav"));}
            pickup=sounds.load(normal.getPath(),1);keyPickup=sounds.load(key.getPath(),1);
            File door=new File(getCacheDir(),"door.wav");try(OutputStream out=new FileOutputStream(door)){out.write(archive.read("Data/Sound/ExitDoor.wav"));}doorSound=sounds.load(door.getPath(),1);
            File chest=new File(getCacheDir(),"chest.wav");try(OutputStream out=new FileOutputStream(chest)){out.write(archive.read("Data/Sound/GoldChest.wav"));}chestSound=sounds.load(chest.getPath(),1);
        }catch(IOException e){pickup=keyPickup=0;}
    }
    private int doorSound;
    private int chestSound;
    void playItem(int type){int id=type>=41&&type<=43?keyPickup:org.braveopendwarves.data.ItemRules.chest(type)?chestSound:pickup;if(sounds!=null&&loadedSounds.contains(id))sounds.play(id,.5f,.5f,1,0,1);}
    void playDoor(){if(sounds!=null&&loadedSounds.contains(doorSound))sounds.play(doorSound,.5f,.5f,1,0,1);}
    void playPickup(boolean key){int id=key?keyPickup:pickup;if(sounds!=null&&loadedSounds.contains(id))sounds.play(id,.5f,.5f,1,0,1);}
    @Override protected void onPause(){super.onPause();if(game!=null)game.suspend();if(original!=null)original.suspend();if(preview!=null)preview.suspend();}
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)getWindow().getDecorView().setSystemUiVisibility(4102);}
    @Override protected void onResume(){super.onResume();getWindow().getDecorView().setSystemUiVisibility(4102);if(game!=null)game.resume();if(original!=null)original.resume();if(preview!=null)preview.resume();}
    @Override public void onBackPressed(){if(original!=null||preview!=null||game!=null){showHome();}else super.onBackPressed();}
    @Override protected void onDestroy(){if(sounds!=null)sounds.release();if(archive!=null)try{archive.close();}catch(IOException ignored){}super.onDestroy();}
}



















