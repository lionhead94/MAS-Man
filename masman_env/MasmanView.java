package masman_env;

import jason.environment.grid.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class MasmanView extends GridWorldView {
    MasmanModel masmanModel;

    public MasmanView(MasmanModel model) { 
        super(model, "MAS-Man", 700);   // model, window name, window size
        masmanModel = model;
        setVisible(true);
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if(object == masmanModel.DOT)
            drawDot(g, x, y);
        else if(object == masmanModel.NOTHING)
            drawNothing(g, x, y);
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        g.setColor(Color.black);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        if(id == 0)
            c = Color.yellow;
        else if(id == 1)
            c = Color.red;
        else if(id == 2)
            c = Color.pink;
        else if(id == 3)
            c = Color.cyan;
        else
            c = Color.orange;
        super.drawAgent(g, x, y, c, -1);
    }

    @Override
    public void drawObstacle(Graphics g, int x, int y) {
        // fills the walls of the maze
        g.setColor(Color.blue);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    public void drawDot(Graphics g, int x, int y) {
        int sizeW = cellSizeW / 3;
        int sizeH = cellSizeH / 3;
        int upperLeftX = x * cellSizeW + sizeW;
        int upperLeftY = y * cellSizeH + sizeH;
        g.setColor(Color.black);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        g.setColor(Color.orange);
        g.fillOval(upperLeftX, upperLeftY , sizeW, sizeH);
    }

    @Override
    public void drawEmpty(Graphics g, int x, int y) {
        g.setColor(Color.black);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    // used to fill black tiles that are empty from the beginning
    public void drawNothing(Graphics g, int x, int y) {
        g.setColor(Color.black);
        g.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }
}