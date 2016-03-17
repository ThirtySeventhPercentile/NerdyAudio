//Licensed under the MIT License.
//Include the license text thingy if you're gonna use this.
//Copyright (c) 2016 Chansol Yang

package com.thirtyseventhpercentile.nerdyaudio.visuals;

import android.graphics.Canvas;

import com.thirtyseventhpercentile.nerdyaudio.audio.AudioPlayer;
import com.thirtyseventhpercentile.nerdyaudio.audio.VisualizationBuffer;
import com.thirtyseventhpercentile.nerdyaudio.exceptions.BufferNotPresentException;


public abstract class BaseRenderer {
    public static final String LOG_TAG="CS_AFN";
    VisualizationBuffer vb;
    AudioPlayer ap;

    float density;

    public BaseRenderer(float density) {
        this.density = density;
        this.vb=VisualizationBuffer.getInstance();
        this.ap=AudioPlayer.getInstance();
    }

    abstract public void draw(Canvas c, int w, int h);
    abstract public void release();

    public void setVisualizationBuffer(VisualizationBuffer vb) {
        this.vb = vb;
    }

    public void setAudioPlayer(AudioPlayer ap) {
        this.ap = ap;
    }
    public short[] getLSamples(long start, long end) throws BufferNotPresentException{
        if (vb != null) {
            return vb.getFrames(start, end, VisualizationBuffer.LEFT_CHANNEL);

        }else return null;
    }
    public short[] getRSamples(long start, long end) throws BufferNotPresentException {
        if (vb != null) {

            return vb.getFrames(start, end, VisualizationBuffer.RIGHT_CHANNEL);

        }else return null;
    }
    public void deleteBefore(long samp){
        if (vb != null) {


            vb.deleteBefore(samp);

        }
    }

    public long getCurrentFrame() {
        if (ap != null) {
            return ap.getCurrentFrame();
        }else return 0;
    }

}