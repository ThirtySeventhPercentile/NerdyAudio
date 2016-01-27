package com.chancorp.audiofornerds.visuals;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import com.chancorp.audiofornerds.helper.ErrorLogger;

public class VisualsRenderThread extends Thread{
    public static final String LOG_TAG="CS_AFN";

    Paint pt;

    int w,h;
    long lastDrawn=0, currentDrawn=1;
    float fps=0;
    float maxFPS=60.0f;
    int minDelay=16;

    BaseRenderer renderer;

    SurfaceHolder sf;

    boolean active=true;

    Canvas c;
    public VisualsRenderThread(){
        pt = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    public void setSurfaceHolder(SurfaceHolder sf){ //TODO is this valid?
        this.sf=sf;
    }
    public void stopRender(){
        active=false;
    }

    public void setRenderer(BaseRenderer r){
        this.renderer=r;
    }
    public void setSize(int w,int h){
        this.w=w;
        this.h=h;
    }
    public void setMaxFPS(float maxFPS){
        this.maxFPS=maxFPS;
        this.minDelay=(int)(1000.0f/maxFPS);
    }
    public BaseRenderer getRenderer(){
        return renderer;
    }
    @Override
    public void run(){
        while (active) {
            c = sf.lockCanvas();

            if (c==null) {
                Log.i(LOG_TAG,"VisualsRenderThread: lockCanvas() returned null. breaking loop.");
                break;
            }

            c.drawColor(Color.WHITE);

            if (renderer!=null) renderer.draw(c,w,h);

            //Log.d(LOG_TAG,"DB: "+lastDrawn+" | "+minDelay+" | "+System.currentTimeMillis());
            while (lastDrawn+minDelay>System.currentTimeMillis()){
                try {
                    //Log.d(LOG_TAG,"Sleeping(FPS too high)");
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    ErrorLogger.log(e);
                }
            }

            currentDrawn=System.currentTimeMillis();
            fps=1000.0f/((currentDrawn-lastDrawn));
            pt.setTextSize(36.0f);

            pt.setStyle(Paint.Style.STROKE);
            pt.setStrokeWidth(8);
            pt.setColor(Color.BLACK);
            c.drawText("FPS: " + (int) fps, 10, 40, pt);

            pt.setStyle(Paint.Style.FILL);
            pt.setColor(Color.WHITE);
            c.drawText("FPS: "+(int)fps, 10, 40, pt);


            lastDrawn=currentDrawn;
            sf.unlockCanvasAndPost(c);
        }

    }
}
