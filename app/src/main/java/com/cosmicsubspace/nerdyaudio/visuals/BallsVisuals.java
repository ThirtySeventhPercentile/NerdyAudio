package com.cosmicsubspace.nerdyaudio.visuals;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.cosmicsubspace.nerdyaudio.exceptions.BufferNotPresentException;
import com.cosmicsubspace.nerdyaudio.helper.ColorFiddler;
import com.cosmicsubspace.nerdyaudio.helper.Log2;
import com.cosmicsubspace.nerdyaudio.helper.SimpleMaths;
import com.cosmicsubspace.nerdyaudio.settings.FloatSliderElement;
import com.cosmicsubspace.nerdyaudio.settings.SettingElement;
import com.cosmicsubspace.nerdyaudio.settings.SliderElement;

import java.util.ArrayList;
import java.util.List;


public class BallsVisuals extends FftRenderer {
    Paint pt;

    ArrayList<Ball> balls = new ArrayList<>();

    /*
    int iterations=10;
    float sensitivity=100, bounciness=30;
    float stickyness=0.3f;
    float lowpass=0.4f;
    */

    SliderElement iterations = new SliderElement("Simulation Iterations", 1, 100, 30);
    FloatSliderElement sensitivity = new FloatSliderElement("Sensitivity", 0, 300, 100, 1000);
    FloatSliderElement bounciness = new FloatSliderElement("Bounciness", 0, 100, 10, 100);
    FloatSliderElement stickyness = new FloatSliderElement("Stickyness", 0, 1, 0.3f, 100);
    FloatSliderElement lowpass = new FloatSliderElement("Lowpass", 0, 1, 0.3f, 100);


    @Override
    public List<SettingElement> getSettings() {
        List<SettingElement> res = super.getSettings();
        res.add(iterations);
        res.add(sensitivity);
        res.add(bounciness);
        res.add(stickyness);
        return res;
    }


    @Override
    public String getKey() {
        return "BallsVisuals";
    }

    public BallsVisuals(Context ctxt) {
        super(ctxt);
        pt = new Paint(Paint.ANTI_ALIAS_FLAG);

        initializeSimulation();
    }


    @Override
    public void dimensionsChanged(int w, int h) {

    }

    private void initializeSimulation() {
        Log2.log(0, this, "Sim Init...");
        balls.clear();
        for (int i = 0; i < 4; i++) {
            balls.add(new Ball(i * 100, 100, 0, 0, ColorFiddler.rampColor(Color.RED, Color.BLUE, i / 5.0f)));
        }
    }

    @Override
    public void drawVisuals(Canvas c, int w, int h) {

        long currentFrame = getCurrentFrame();
        float lowpass = this.lowpass.getFloatValue();
        float sensitivity = this.sensitivity.getFloatValue();
        int iterations = this.iterations.getValue();
        try {
            updateFFT(currentFrame);
            //Log2.log(2,this, "Sensitivity: "+sensitivity);
            //Bass
            balls.get(0).r = (1.0f - lowpass) * balls.get(0).r + lowpass * SimpleMaths.linearMapClamped(getMagnitudeRange(50, 150, true), 0, 300, 50, 50 + sensitivity * 3);
            //Low
            balls.get(1).r = (1.0f - lowpass) * balls.get(1).r + lowpass * SimpleMaths.linearMapClamped(getMagnitudeRange(150, 450, true), 0, 150, 50, 50 + sensitivity * 3);
            //Mid
            balls.get(2).r = (1.0f - lowpass) * balls.get(2).r + lowpass * SimpleMaths.linearMapClamped(getMagnitudeRange(450, 1000, true), 0, 100, 50, 50 + sensitivity * 3);
            //High
            balls.get(3).r = (1.0f - lowpass) * balls.get(3).r + lowpass * SimpleMaths.linearMapClamped(getMagnitudeRange(1000, 10000, true), 0, 50, 50, 50 + sensitivity * 3);


            for (int i = 0; i < iterations; i++) { //Sim Iterations
                for (Ball ball : balls) {
                    ball.update(balls, iterations);
                    ball.collision(balls, bounciness.getFloatValue());
                    ball.wallIn(0, 0, w, h);
                }
            }

            //Balls attract each other.
            for (Ball ball : balls) {
                for (Ball ball2 : balls) {
                    if (ball == ball2) continue;

                    ball.attract(ball2.x, ball2.y, stickyness.getFloatValue());
                }
            }

            //We draw the balls, attract them to the center, and damp them.
            for (Ball ball : balls) {
                ball.draw(c, pt);
                ball.attract(w / 2, h / 2, 1);
                ball.damp(0.98f);
            }

        } catch (BufferNotPresentException e) {
            Log2.log(1, this, "Buffer not present! Requested around " + currentFrame);
        } catch (NullPointerException e) {
            Log2.log(1, this, "NPE @ BallsVisuals " + currentFrame);
        }
    }


    class Ball {
        float x, y;
        float vx, vy;
        float r;
        int color;

        public Ball(float x, float y, float vx, float vy, int color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.r = 30;
            this.color = color;
        }

        protected float distance(Ball b) {
            return (float) Math.sqrt(Math.pow(b.x - this.x, 2) + Math.pow(b.y - this.y, 2));
        }

        protected float intersect(Ball b) {
            return distance(b) - b.r - this.r;
        }

        public void collision(ArrayList<Ball> balls, float bounciness) {
            for (Ball ball : balls) {
                if (ball.equals(this)) continue;
                if (intersect(ball) < 0) {
                    float dx = this.x - ball.x;
                    float dy = this.y - ball.y;
                    float mag = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                    float dxn = dx / mag;
                    float dyn = dy / mag;

                    vx += dxn * -intersect(ball) * bounciness / 100.0f;
                    vy += dyn * -intersect(ball) * bounciness / 100.0f;
                }
            }
        }

        public void draw(Canvas c, Paint pt) {
            pt.setColor(color);
            c.drawCircle(x, y, r, pt);
        }

        public void update(ArrayList<Ball> balls, int division) {
            x += vx / division;
            y += vy / division;
        }

        public void attract(float x, float y, float power) {
            float dx = this.x - x;
            float dy = this.y - y;
            float mag = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
            float dxn = dx / mag;
            float dyn = dy / mag;
            vx -= dxn * power;
            vy -= dyn * power;
        }

        public void damp(float power) {
            vx *= power;
            vy *= power;
        }

        public void wallIn(float minX, float minY, float maxX, float maxY) {
            if (x - r < minX) {
                vx = -vx;
                x = -x + 2 * r; //(x-r)=-(x-r)
            } else if (x + r > maxX) {
                vx = -vx;
                x = maxX * 2 - x - 2 * r;//(x-r)=2maxX-(x-r)
            }
            if (y - r < minY) {
                vy = -vy;
                y = -y + 2 * r;
            } else if (y + r > maxY) {
                vy = -vy;
                y = maxY * 2 - y - 2 * r;
            }
        }
    }

}
