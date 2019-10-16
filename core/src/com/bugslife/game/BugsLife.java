package com.bugslife.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Random;

public class BugsLife extends ApplicationAdapter implements GestureDetector.GestureListener {
	SpriteBatch batch;
	Texture background;
	Texture[] man;
	Texture gotHit;
	Texture jumpFall;

	int manState = 0;
	int pause = 0;
	float gravity = 0.2f;
	float velocityY = 0;
	float touchPosX = 0;
	float touchPosY = 0;

	// the current X,Y position of man on the screen
	float currentManX = 0;
	float holderY = 0;

	int manY = 0;
	Random random;
	Rectangle manRectangle;
	int score = 0;
	int gameState = 0;
	BitmapFont scoreFont;
	BitmapFont gameOverFont;
	BitmapFont restartFont;
	BitmapFont instructionFont;

	GlyphLayout gameOverLayout;
	GlyphLayout restartLayout;
	GlyphLayout instructionLayout;

	FreeTypeFontGenerator fontGenerator;
	FreeTypeFontGenerator.FreeTypeFontParameter scoreFontParameter;
	FreeTypeFontGenerator.FreeTypeFontParameter gameOverFontParameter;
	FreeTypeFontGenerator.FreeTypeFontParameter restartFontParameter;
	FreeTypeFontGenerator.FreeTypeFontParameter instructionFontParameter;

	ArrayList<Integer> goldBarXs = new ArrayList<>();
	ArrayList<Integer> goldBarYs = new ArrayList<>();
	ArrayList<Rectangle> goldBarRectangles = new ArrayList<>();
	Texture goldBar;
	int goldBarCount;

	ArrayList<Integer> bugXs = new ArrayList<>();
	ArrayList<Integer> bugYs = new ArrayList<>();
	ArrayList<Rectangle> bugRectangles = new ArrayList<>();
	Texture bug;
	int bugCount;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.jpg");
		random = new Random();
		man = new Texture[6];
		man[0] = new Texture("frame-1.png");
		man[1] = new Texture("frame-2.png");
		man[2] = new Texture("frame-3.png");
		man[3] = new Texture("frame-4.png");
		man[4] = new Texture("frame-5.png");
		man[5] = new Texture("frame-6.png");

		manY = Gdx.graphics.getHeight() / 2;

		goldBar = new Texture("goldbar.png");
		bug = new Texture("bug.png");

		gotHit = new Texture("frame-got-hit.png");
		jumpFall = new Texture("jump_fall.png");

		gameOverLayout = new GlyphLayout();
		restartLayout = new GlyphLayout();
		instructionLayout = new GlyphLayout();

		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("PixelEmulator-xq08.ttf"));
		scoreFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		scoreFontParameter.size = 150;
		scoreFontParameter.color = Color.WHITE;
		scoreFont = fontGenerator.generateFont(scoreFontParameter);

		gameOverFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		gameOverFontParameter.size = 250;
		gameOverFontParameter.color = Color.WHITE;
		gameOverFont = fontGenerator.generateFont(gameOverFontParameter);

		restartFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		restartFontParameter.size = 100;
		restartFontParameter.color = Color.WHITE;
		restartFont = fontGenerator.generateFont(restartFontParameter);

		instructionFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		instructionFontParameter.size = 40;
		instructionFontParameter.color = Color.WHITE;
		instructionFont = fontGenerator.generateFont(instructionFontParameter);

		currentManX = Gdx.graphics.getWidth()/2 - man[manState].getWidth() / 2;

		InputMultiplexer im = new InputMultiplexer();
		GestureDetector gd = new GestureDetector(this);
		im.addProcessor(gd);

		Gdx.input.setInputProcessor(gd);

	}

	public void makeGoldBar() {
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		goldBarYs.add((int)height);
		goldBarXs.add(Gdx.graphics.getWidth());
	}

	public void makeBug() {
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		bugYs.add((int)height);
		bugXs.add(Gdx.graphics.getWidth());
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if(gameState == 1) {
			// GAME IS LIVE
			//GOLD BARS
			if(goldBarCount < 100) {
				goldBarCount++;
			} else {
				goldBarCount = 0;
				makeGoldBar();
			}

			goldBarRectangles.clear();
			for(int i = 0; i < goldBarXs.size(); i++) {
				batch.draw(goldBar, goldBarXs.get(i), goldBarYs.get(i));
				// Move goldbars slowly to the left
				goldBarXs.set(i, goldBarXs.get(i) - 4);
				goldBarRectangles.add(new Rectangle(goldBarXs.get(i), goldBarYs.get(i), goldBar.getWidth()
						, goldBar.getHeight()));
			}

			//BUGS
			if(bugCount < 200) {
				bugCount++;
			} else {
				bugCount = 0;
				makeBug();
			}

			bugRectangles.clear();
			for(int i = 0; i < bugXs.size(); i++) {
				batch.draw(bug, bugXs.get(i), bugYs.get(i));
				// Move bugs slowly to the left
				bugXs.set(i, bugXs.get(i) - 6);
				bugRectangles.add(new Rectangle(bugXs.get(i), bugYs.get(i), bug.getWidth()
						, bug.getHeight()));
			}

			if(Gdx.input.isTouched()) {
				Gdx.app.log("FLING", "isTouched() triggered");
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				touchPosX = touchPos.x;
				touchPosY = touchPos.y;

				currentManX = (touchPosX - currentManX)/50 + currentManX;

				Gdx.app.log("isTouched", "touchPositionY is " + touchPosY);
				Gdx.app.log("isTouched", "manY is " + manY);

			}


			if(Gdx.input.justTouched()) {
				velocityY = -10;
				holderY = 0;
				Gdx.app.log("FLING", "justTouched() triggered");
			}



			if(pause < 4) {
				pause++;
			} else {
				pause = 0;
				if(manState < 5) {
					manState++;
				} else{
					manState = 0;
				}
			}

			velocityY += gravity;
			manY -= velocityY;

			Gdx.app.log("FLING", "Render, manY is " + manY);

			if(manY <= 0) {
				manY = 0;
			}

			if(manY > Gdx.graphics.getHeight() - man[manState].getHeight()* 1 / 4 ){
				manY = Gdx.graphics.getHeight() - man[manState].getHeight()* 1 / 4;

			}

		} else if(gameState == 0) {
			// Waiting for game to start
			instructionLayout.setText(instructionFont, "TAP TO JUMP" + "\n\n" + "SWIPE DOWN TO FALL QUICKLY" + "\n\n" + "COLLECT GOLD BARS AND AVOID BUGS!");
			float instructionWidth = instructionLayout.width;
			float instructionHeight = instructionLayout.height;

			instructionFont.draw(batch, "TAP TO JUMP" + "\n\n" + "SWIPE DOWN TO FALL QUICKLY" + "\n\n" + "COLLECT GOLD BARS AND AVOID BUGS!", Gdx.graphics.getWidth()/2 - instructionWidth / 2 , Gdx.graphics.getHeight()/2 - instructionHeight);
			if(Gdx.input.justTouched()) {
				gameState = 1;
				currentManX = Gdx.graphics.getWidth()/2 - man[manState].getWidth() / 2;

			}
		} else if(gameState == 2) {
			// GAME OVER
			if(Gdx.input.justTouched()) {
				gameState = 1;
				manY = Gdx.graphics.getHeight() / 2;
				score = 0;
				velocityY = 0;
				goldBarXs.clear();
				goldBarYs.clear();
				goldBarRectangles.clear();
				goldBarCount = 0;

				bugXs.clear();
				bugYs.clear();
				bugRectangles.clear();
				bugCount = 0;

				currentManX = Gdx.graphics.getWidth()/2 - man[manState].getWidth() / 2;

			}
		}

		// If game over
		if(gameState == 2 ) {
			batch.draw(gotHit, currentManX, manY);
			//batch.draw(gameOver,Gdx.graphics.getWidth()/2 - gameOver.getWidth()/2, Gdx.graphics.getHeight()/2 - gameOver.getHeight()/2 );
			gameOverLayout.setText(gameOverFont,"GAME OVER");
			restartLayout.setText(restartFont, "TAP TO RESTART");

			float gameOverWidth = gameOverLayout.width;
			float gameOverHeight = gameOverLayout.height;

			float restartWidth = restartLayout.width;


			gameOverFont.draw(batch, "GAME OVER", Gdx.graphics.getWidth()/2 - gameOverWidth / 2, Gdx.graphics.getHeight()/2 + gameOverHeight / 2);
			restartFont.draw(batch,"TAP TO RESTART", Gdx.graphics.getWidth()/2 - restartWidth / 2, Gdx.graphics.getHeight()/3);
		} else{


			if(holderY > manY && manY != 0)  {
				batch.draw(jumpFall, currentManX, manY);
				Gdx.app.log("Falling", "holderY is " + holderY);
				Gdx.app.log("Falling", "manY is " + manY);
				Gdx.app.log("Falling", "man is falling");
			} else {
				batch.draw(man[manState], currentManX, manY);
				Gdx.app.log("FLING", "Draw man, manY is " + manY);
			}

		}

		manRectangle = new Rectangle(currentManX, manY,
				man[manState].getWidth(), man[manState].getHeight());

		for(int i = 0; i < goldBarRectangles.size();i++) {
			if(Intersector.overlaps(manRectangle, goldBarRectangles.get(i))) {
				score++;

				goldBarRectangles.remove(i);
				goldBarXs.remove(i);
				goldBarYs.remove(i);
				break;
			}
		}

		for(int i = 0; i < bugRectangles.size();i++) {
			if(Intersector.overlaps(manRectangle, bugRectangles.get(i))) {
				Gdx.app.log("Bug!", "Collision!" );
				gameState = 2;
			}
		}
		scoreFont.draw(batch, String.valueOf(score), 100, 200);

		batch.end();

	}
	
	@Override
	public void dispose () {
		batch.dispose();

	}


    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
		return false;
    }

    @Override
    public boolean fling(float velX, float velY, int button) {
        holderY = manY;

        //minimal jump upwards if fling
        manY -= velY / 200;
        if(holderY > manY) {
			velocityY = 0;
			manY -= velY / 10 ;
		}

		Gdx.app.log("FLING", "Fling happened! holderY = " + holderY);
		Gdx.app.log("FLING", "Fling happened! manY = " + manY);
		Gdx.app.log("FLING", "Fling happened! velocityY = " + velocityY);
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
