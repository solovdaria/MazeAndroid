package com.example.mazegame;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {
    private enum Direction
    {UP, DOWN, LEFT, RIGHT}

    private Cell[][] cells;
    private Cell player, exit;
    private static final int COLS = 20, ROWS = 14;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random rand;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        wallPaint=new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint=new Paint();
        playerPaint.setColor(Color.RED);

        exitPaint=new Paint();
        exitPaint.setColor(Color.YELLOW);

        rand = new Random();

        createMaze();
    }

    private Cell getNeightbour(Cell cell)
    {
        ArrayList<Cell> neighbours = new ArrayList<>();

        //left
        if(cell.col>0)
            if(!cells[cell.col-1][cell.row].visited)
                neighbours.add(cells[cell.col-1][cell.row]);

        //right
        if(cell.col<COLS-1)
            if(!cells[cell.col+1][cell.row].visited)
                neighbours.add(cells[cell.col+1][cell.row]);

        //top
        if(cell.row>0)
            if(!cells[cell.col][cell.row-1].visited)
                neighbours.add(cells[cell.col][cell.row-1]);

        //bottom
        if(cell.row<ROWS-1)
            if(!cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col][cell.row+1]);

        if(neighbours.size()>0) {
            int index = rand.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    private void removeWall(Cell curr, Cell next)
    {
        if(curr.col==next.col && curr.row==next.row+1)
        {
            curr.topWall=false;
            next.bottWall=false;
        }
        if(curr.col==next.col && curr.row==next.row-1)
        {
            curr.bottWall=false;
            next.topWall=false;
        }
        if(curr.col==next.col+1 && curr.row==next.row)
        {
            curr.leftWall=false;
            next.rightWall=false;
        }
        if(curr.col==next.col-1 && curr.row==next.row)
        {
            curr.rightWall=false;
            next.leftWall=false;
        }
    }

    public void createMaze()
    {
        Stack<Cell> stack = new Stack<>();
        Cell curr, next;

        cells = new Cell[COLS][ROWS];

        for(int x=0; x<COLS; x++)
            for(int y=0; y<ROWS; y++)
            {
                cells[x][y]=new Cell(x, y);
            }

        player=cells[0][0];
        exit=cells[COLS-1][ROWS-1];

        curr= cells[0][0];
        curr.visited =true;
        do {
            next = getNeightbour(curr);
            if (next != null) {
                removeWall(curr, next);
                stack.push(curr);
                curr = next;
                curr.visited = true;
            } else
                curr = stack.pop();
        }while (!stack.empty());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GREEN);

        int width = getWidth();
        int heigh = getHeight();

        if(width/heigh<COLS/ROWS)
            cellSize=width/(COLS+1);
        else
            cellSize=heigh/(ROWS+1);

        hMargin=(width-COLS*cellSize)/2;
        vMargin=(heigh-ROWS*cellSize)/2;

        canvas.translate(hMargin, vMargin);

        for(int x=0; x<COLS; x++)
            for(int y=0; y<ROWS; y++)
            {
                if (cells[x][y].topWall)
                    canvas.drawLine(x*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            y*cellSize,
                            wallPaint);

                if (cells[x][y].leftWall)
                    canvas.drawLine(x*cellSize,
                            y*cellSize,
                            x*cellSize,
                            (y+1)*cellSize,
                            wallPaint);

                if (cells[x][y].bottWall)
                    canvas.drawLine(x*cellSize,
                            (y+1)*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint);

                if (cells[x][y].rightWall)
                    canvas.drawLine((x+1)*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint);
            }

        float margin = cellSize/10;

        canvas.drawRect(
                player.col*cellSize+margin,
                player.row*cellSize+margin,
                (player.col+1)*cellSize-margin,
                (player.row+1)*cellSize-margin,
                playerPaint
        );

        canvas.drawRect(
                exit.col*cellSize,
                exit.row*cellSize,
                (exit.col+1)*cellSize,
                (exit.row+1)*cellSize,
                exitPaint
        );

    }

    private void movePlayer(Direction dir)
    {
        switch (dir)
        {
            case UP:
                if(!player.topWall)
                    player=cells[player.col][player.row-1];
                break;
            case DOWN:
                if(!player.bottWall)
                    player=cells[player.col][player.row+1];
                break;
            case LEFT:
                if(!player.leftWall)
                    player=cells[player.col-1][player.row];
                break;
            case RIGHT:
                if(!player.rightWall)
                    player=cells[player.col+1][player.row];
                break;
        }
        checkExit();
        invalidate();

    }

    private void checkExit()
    {
        if(player==exit)
            createMaze();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
            return true;

        if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            float x = event.getX();
            float y = event.getY();

            float playerCenterX=hMargin+(player.col+0.5f)*cellSize;
            float playerCenterY=vMargin+(player.row+0.5f)*cellSize;

            float dx=x-playerCenterX;
            float dy = y-playerCenterY;

            float absDx=Math.abs(dx);
            float absDy=Math.abs(dy);

            if(absDx>cellSize || absDy>cellSize)
            {
                if(absDx>absDy)
                {
                    if (dx>0)
                        movePlayer(Direction.RIGHT);
                    else
                        movePlayer(Direction.LEFT);
                }
                else
                {
                    if (dy>0)
                        movePlayer(Direction.DOWN);
                    else
                        movePlayer(Direction.UP);
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private class Cell{
        boolean topWall=true;
        boolean leftWall=true;
        boolean bottWall=true;
        boolean rightWall=true;
        boolean visited = false;

        int row, col;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
