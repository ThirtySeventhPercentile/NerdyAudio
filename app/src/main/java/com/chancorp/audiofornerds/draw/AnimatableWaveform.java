package com.chancorp.audiofornerds.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.chancorp.audiofornerds.animation.MixedProperties;
import com.chancorp.audiofornerds.animation.PointsCompound;
import com.chancorp.audiofornerds.animation.PropertySet;
import com.chancorp.audiofornerds.audio.AudioPlayer;
import com.chancorp.audiofornerds.audio.Waveform;
import com.chancorp.audiofornerds.helper.ColorFiddler;

/**
 * Created by Chan on 2/25/2016.
 */
public class AnimatableWaveform extends Animatable {
    Waveform wf;
    AudioPlayer ap;
    AnimatableText notAvailableText;
    float barSpacing=0.5f;

    /**
     * Required Properties:
     * X  - The Left corner
     * Y  - The Bottom corner
     * XSize
     * YSize
     * <p/>
     * Played-R
     * Played-G
     * Played-B
     * Played-A
     * <p/>
     * Remaining-R
     * Remaining-G
     * Remaining-B
     * Remaining-A
     */


    public AnimatableWaveform(Waveform wf, AudioPlayer ap, MixedProperties basis, float density) {
        super(basis);
        this.wf = wf;
        this.ap = ap;
        this.notAvailableText = new AnimatableText(new MixedProperties("Final", new PropertySet().setValue("X", 0).setValue("Y", 0)), Color.BLACK, "Waveorm not yet prepared...", 16 * density);
        this.notAvailableText.setAlign(AnimatableText.ALIGN_CENTER);
    }

    float currentPosition;
    PropertySet current;
    public void draw(Canvas c, Paint pt) {
        //TODO something that tells the user if the waveform is not yet prepared.
        current = mixedProperties.update(System.currentTimeMillis());

        if (wf != null && ap != null && wf.isReady() && wf.getFilename().equals(ap.getSourceString())) {
            currentPosition=(float) (ap.getMusicCurrentFrame() / (double) wf.getNumOfFrames());
            float spacing = current.getValue("XSize") / (wf.getDivisions() * (1.0f + barSpacing) - barSpacing) * (barSpacing + 1.0f);
            float width = current.getValue("XSize") / (wf.getDivisions() * (1.0f + barSpacing) - barSpacing);
            float progressPerBar = 1 / (float) wf.getDivisions();
            for (int i = 0; i < wf.getDivisions(); i++) {


                if (i * progressPerBar > currentPosition) pt.setColor(getRemainingColor(current));
                else if ((i + 1) * progressPerBar < currentPosition) pt.setColor(getPlayedColor(current));
                else pt.setColor(ColorFiddler.rampColor(getPlayedColor(current), getRemainingColor(current), (currentPosition - i * progressPerBar) / progressPerBar));

                //Log.v(LOG_TAG, "Drawing"+(i*spacing)+" to "+(i*spacing+width));
                c.drawRect(current.getValue("X")+i * spacing, current.getValue("Y")- wf.getRatio(i)*current.getValue("YSize"), current.getValue("X")+i * spacing + width, current.getValue("Y"), pt);
            }
        }else{
            notAvailableText.draw(c,pt);
        }
    }
/*
    float currentPosition;
    PropertySet current;
    PointsCompound waveformPlayed, waveformNotPlayed, waveformIntermediate;
    PointsCompound.Builder playedBuilder, notPlayedBuilder, intermediateBuilder;
    float intermediateRatio;
    private void updatePaths(PropertySet current) {
        currentPosition = (float) (ap.getMusicCurrentFrame() / (double) wf.getNumOfFrames());
        float spacing = current.getValue("XSize") / wf.getDivisions();
        float width = current.getValue("XSize") / wf.getDivisions();
        float progressPerBar = 1 / (float) wf.getDivisions();

        playedBuilder = new PointsCompound.Builder();
        notPlayedBuilder = new PointsCompound.Builder();
        intermediateBuilder = new PointsCompound.Builder();


        int intermediateIndex = (int) Math.round(Math.floor(currentPosition / progressPerBar));

        playedBuilder.addPoint(current.getValue("X"), current.getValue("Y"));
        for (int i = 0; i < intermediateIndex; i++) {
            playedBuilder.addPoint(current.getValue("X") + i * spacing, current.getValue("Y") - wf.getRatio(i) * current.getValue("YSize"));
            playedBuilder.addPoint(current.getValue("X") + i * spacing + width, current.getValue("Y") - wf.getRatio(i) * current.getValue("YSize"));
        }
        playedBuilder.addPoint(current.getValue("X") + intermediateIndex * spacing, current.getValue("Y"));

        if (intermediateIndex<wf.getDivisions()) {
            intermediateBuilder.addPoint(current.getValue("X") + intermediateIndex * spacing, current.getValue("Y"));
            intermediateBuilder.addPoint(current.getValue("X") + intermediateIndex * spacing, current.getValue("Y") - wf.getRatio(intermediateIndex) * current.getValue("YSize"));
            intermediateBuilder.addPoint(current.getValue("X") + intermediateIndex * spacing + width, current.getValue("Y") - wf.getRatio(intermediateIndex) * current.getValue("YSize"));
            intermediateBuilder.addPoint(current.getValue("X") + intermediateIndex * spacing + width, current.getValue("Y"));
            intermediateRatio = (currentPosition - intermediateIndex * progressPerBar) / progressPerBar;
        }

        if (intermediateIndex+1<wf.getDivisions()) {
            notPlayedBuilder.addPoint(current.getValue("X") + (intermediateIndex + 1) * spacing, current.getValue("Y"));
            for (int i = intermediateIndex + 1; i < wf.getDivisions(); i++) {
                notPlayedBuilder.addPoint(current.getValue("X") + i * spacing, current.getValue("Y") - wf.getRatio(i) * current.getValue("YSize"));
                notPlayedBuilder.addPoint(current.getValue("X") + i * spacing + width, current.getValue("Y") - wf.getRatio(i) * current.getValue("YSize"));
            }
            notPlayedBuilder.addPoint(current.getValue("X") + current.getValue("XSize"), current.getValue("Y"));
        }
        waveformPlayed = playedBuilder.build();
        waveformNotPlayed = notPlayedBuilder.build();
        waveformIntermediate = intermediateBuilder.build();
    }

    public void draw(Canvas c, Paint pt) {
        current = mixedProperties.update(System.currentTimeMillis());
        notAvailableText.getMixedProperties().getBasis().setValue("X", current.getValue("X") + current.getValue("XSize") / 2.0f).setValue("Y", current.getValue("Y") - current.getValue("YSize") / 2.0f);
        if (wf != null && ap != null && wf.isReady() && wf.getFilename().equals(ap.getSourceString())) {
            updatePaths(current);


            pt.setColor(getPlayedColor(current));
            c.drawPath(waveformPlayed.toPath(), pt);

            pt.setColor(ColorFiddler.rampColor(getPlayedColor(current),getRemainingColor(current), intermediateRatio));
            c.drawPath(waveformIntermediate.toPath(),pt);

            pt.setColor(getRemainingColor(current));
            c.drawPath(waveformNotPlayed.toPath(), pt);


        } else {

            notAvailableText.draw(c, pt);
        }
    }
*/
    private int getPlayedColor(PropertySet ps) {
        //Log2.log(2,this,ps.getValue("Played-A"),ps.getValue("Played-R"),ps.getValue("Played-G"),ps.getValue("Played-B"));
        return Color.argb(Math.round(ps.getValue("Played-A") * 255),
                Math.round(ps.getValue("Played-R") * 255),
                Math.round(ps.getValue("Played-G") * 255),
                Math.round(ps.getValue("Played-B") * 255));
    }

    private int getRemainingColor(PropertySet ps) {
        return Color.argb(Math.round(ps.getValue("Remaining-A") * 255),
                Math.round(ps.getValue("Remaining-R") * 255),
                Math.round(ps.getValue("Remaining-G") * 255),
                Math.round(ps.getValue("Remaining-B") * 255));
    }


}
